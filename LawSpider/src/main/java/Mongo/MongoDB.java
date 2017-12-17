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
    private MongoDatabase mongoDatabase;

    private MongoDB() {
        //初始化mongodb
        DBProperties dbProperties = DBProperties.getDBProperties();
        Properties prop = dbProperties.getProp();
        if (prop != null) {
            String db_host = prop.getProperty("mongodb_host");
            int db_port = Integer.parseInt(prop.getProperty("mongodb_port"));
            String db_database = prop.getProperty("mongodb_database");
            try {
                // 连接到 mongodb 服务
                MongoClient mongoClient = new MongoClient(db_host, db_port);
                // 连接到数据库
                mongoDatabase = mongoClient.getDatabase(db_database);
                LOGGER.info("Connect to mongodb database successfully");
            } catch (Exception e) {
                LOGGER.error("Connect to mongodb database Error!");
                LOGGER.error(e);
            }
        } else {
            LOGGER.error("mongodb Properties not found!");
        }
    }

    public synchronized MongoCollection<Document> getCollection(String collectionName){
        try {
            return mongoDB.mongoDatabase.getCollection(collectionName);
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        }
    }

    public static MongoDB getMongoDB() {
        return mongoDB;
    }

    //保存法律法规文档
    public synchronized boolean saveLawDocument(MongoCollection<Document> lawcollection, Document document) {
        FindIterable<Document> findIterable = lawcollection.find(new Document("url", document.get("url"))).limit(1).noCursorTimeout(true);
        if (findIterable.first() == null) {
            lawcollection.insertOne(document);
            return true;
        }
        return false;
    }

    //保存爬取任务文档
    public synchronized boolean saveCrawJobDocument(MongoCollection<Document> crawJobcollection, Document document) {
        FindIterable<Document> findIterable = crawJobcollection.find(new Document("url", document.get("url"))).noCursorTimeout(true);
        if (findIterable.first() == null) {
            crawJobcollection.insertOne(document);
            return true;
        }
        return false;
    }

    public List<Document> loadAllCrawJob(MongoCollection<Document> collection) {
        List<Document> result = Collections.synchronizedList(new LinkedList<>());
        FindIterable<Document> findIterable = collection.find(new Document("isCraw", false)).sort(new Document("getTime", 1)).noCursorTimeout(true);
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        try {
            while (mongoCursor.hasNext()) {
                result.add(mongoCursor.next());
            }
        }finally {
            mongoCursor.close();
        }

        return result;
    }

    public synchronized boolean updateCrawJob(MongoCollection<Document> crawJobcollection, Document oldJob, Document newJob){
        try {
            crawJobcollection.replaceOne(oldJob, newJob);
            return true;
        }catch (Exception e){
            LOGGER.error("Update craw job error:" + e.getMessage());
        }
        return false;
    }
    public Document getJobUseUrl(MongoCollection<Document> crawJobcollection, String url){
        FindIterable<Document> findIterable = crawJobcollection.find(new Document("url", url)).limit(1).noCursorTimeout(true);
        return findIterable.first();
    }

    public Document getCrawJob(MongoCollection<Document> crawJobcollection){
        FindIterable<Document> findIterable = crawJobcollection.find(new Document("isCraw", false)).sort(new Document("getTime", 1)).limit(1).noCursorTimeout(true);
        return findIterable.first();
    }

    public boolean isLawDocumentExits(MongoCollection<Document> lawcollection, String url) {
        FindIterable<Document> findIterable = lawcollection.find(new Document("url", url)).limit(1).noCursorTimeout(true);
        if (findIterable.first() == null) {
            return false;
        }
        return true;
    }
}

