package Mongo;

import Interface.DB;
import Log.LawLogger;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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

    public static MongoDB getMongoDB() {
        return mongoDB;
    }

    public synchronized MongoCollection<Document> getCollection(String collectionName) {
        try {
            return mongoDB.mongoDatabase.getCollection(collectionName);
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        }
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
        } finally {
            mongoCursor.close();
        }

        return result;
    }

    public synchronized boolean updateCrawJob(MongoCollection<Document> crawJobcollection, Document oldJob, Document newJob) {
        try {
            crawJobcollection.replaceOne(oldJob, newJob);
            return true;
        } catch (Exception e) {
            LOGGER.error("Update craw job error:" + e.getMessage());
        }
        return false;
    }

    public Document getJobUseUrl(MongoCollection<Document> crawJobcollection, String url) {
        FindIterable<Document> findIterable = crawJobcollection.find(new Document("url", url)).limit(1).noCursorTimeout(true);
        return findIterable.first();
    }

    public Document getCrawJob(MongoCollection<Document> crawJobcollection) {
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

    public synchronized void replaceDocument(MongoCollection<Document> collection, Document law) {
        org.bson.types.ObjectId id = law.getObjectId("_id");
        Document filter = new Document();
        filter.append("_id", id);
        law.remove("_id");
        UpdateResult result = collection.replaceOne(filter, law);
        LOGGER.info(collection.getNamespace().getCollectionName() + " replaceDocument num: " + result.getModifiedCount());
    }

    public synchronized void updateDocument(MongoCollection<Document> collection, Document law) {
        org.bson.types.ObjectId id = law.getObjectId("_id");
        Document filter = new Document();
        filter.append("_id", id);
        law.remove("_id");
        Document update = new Document();
        update.append("$set", law);
        UpdateResult result = collection.updateOne(filter, update);
        LOGGER.info(collection.getNamespace().getCollectionName() + " updateDocument num: " + result.getModifiedCount());
    }

    public synchronized void deleteDocumentManyById(MongoCollection<Document> collection, List<Document> needDelete) {
        int count = 0;
        for (Document deleteLaw : needDelete) {
            Document filter = new Document();
            filter.append("_id", deleteLaw.getObjectId("_id"));
            try {
                collection.deleteOne(filter);
                count++;
            }catch (Exception e){
                LOGGER.error("deleteDocumentManyById id :" + deleteLaw.getObjectId("_id").toString() + " err:"+e);
            }
        }
        LOGGER.info(collection.getNamespace().getCollectionName() + " delete count: " + count);
    }

    public synchronized void deleteDocumentOneById(MongoCollection<Document> collection, Document needDelete) {
        Document filter = new Document();
        filter.append("_id", needDelete.getObjectId("_id"));
        DeleteResult result = collection.deleteOne(filter);
        LOGGER.info(collection.getNamespace().getCollectionName() + " delete count: " + result.getDeletedCount());
    }
}

