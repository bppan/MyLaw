package Mongo;

import Interface.DB;
import Log.LawLogger;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author : Administrator
 * @Date : 2017/11/10 17:29
 * @Description :
 */
public class MongoDB extends DB {
    private static Logger LOGGER = LawLogger.getLawLogger(MongoDB.class);
    private static MongoDB mongoDB = new MongoDB();
    private final String CrawJobCollectionName = "chinacourt_crawJob";
    private MongoCollection<Document> lawcollection;
    private MongoCollection<Document> crawJobcollection;

    private MongoDB() {
        //初始化mongodb
        DBProperties dbProperties = DBProperties.getDBProperties();
        Properties prop = dbProperties.getProp();
        if (prop != null) {
            String db_host = prop.getProperty("mongodb_host");
            int db_port = Integer.parseInt(prop.getProperty("mongodb_port"));
            String db_database = prop.getProperty("mongodb_database");
            String db_collection = prop.getProperty("mongodb_law_collection");
            try {
                // 连接到 mongodb 服务
                MongoClient mongoClient = new MongoClient(db_host, db_port);
                // 连接到数据库
                MongoDatabase mongoDatabase = mongoClient.getDatabase(db_database);
                LOGGER.info("Connect to mongodb database successfully");
                if (loadCollection(mongoDatabase, db_collection)) {
                    LOGGER.info("select all colleciont successfully");
                } else {
                    LOGGER.error("select all colleciont failure");
                }
            } catch (Exception e) {
                LOGGER.error("Connect to mongodb database Error!");
                LOGGER.error(e);
            }
        } else {
            LOGGER.error("mongodb Properties not found!");
        }
    }

    public static MongoDB getMongoDB() {
        return mongoDB;
    }

    //保存法律法规文档
    public synchronized boolean saveLawDocument(Document document) {
        FindIterable<Document> findIterable = this.lawcollection.find(new Document("url", document.get("url")));
        if (findIterable.first() == null) {
            this.lawcollection.insertOne(document);
            return true;
        }
        return false;
    }

    //保存爬取任务文档
    public synchronized boolean saveCrawJobDocument(Document document) {
        FindIterable<Document> findIterable = this.crawJobcollection.find(new Document("url", document.get("url")));
        if (findIterable.first() == null) {
            this.crawJobcollection.insertOne(document);
            return true;
        }
        return false;
    }

    private boolean loadCollection(MongoDatabase mongoDatabasem, String db_collection) {
        try {
            lawcollection = mongoDatabasem.getCollection(db_collection);
            LOGGER.info("select " + db_collection + " colleciont successfully");
            crawJobcollection = mongoDatabasem.getCollection(CrawJobCollectionName);
            LOGGER.info("select " + CrawJobCollectionName + " colleciont successfully");
        } catch (Exception e) {
            LOGGER.error(e);
            return false;
        }
        return true;
    }

    public List<Document> loadAllCrawJob() {
        List<Document> result = Collections.synchronizedList(new LinkedList<>());
        FindIterable<Document> findIterable = this.crawJobcollection.find(new Document("isCraw", false)).sort(new Document("getTime", 1));
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        while (mongoCursor.hasNext()) {
            result.add(mongoCursor.next());
        }
        return result;
    }

    public synchronized boolean updateCrawJob(Document oldJob, Document newJob){
        try {
            this.crawJobcollection.replaceOne(oldJob, newJob);
            return true;
        }catch (Exception e){
            LOGGER.error("Update craw job error:" + e.getMessage());
        }
        return false;
    }
    public Document getJobUseUrl(String url){
        FindIterable<Document> findIterable = this.crawJobcollection.find(new Document("url", url));
        return findIterable.first();
    }

    public Document getCrawJob(){
        FindIterable<Document> findIterable = this.crawJobcollection.find(new Document("isCraw", false)).sort(new Document("getTime", 1));
        return findIterable.first();
    }

    public boolean isLawDocumentExits(String url) {
        FindIterable<Document> findIterable = this.lawcollection.find(new Document("url", url));
        if (findIterable.first() == null) {
            return false;
        }
        return true;
    }
}

