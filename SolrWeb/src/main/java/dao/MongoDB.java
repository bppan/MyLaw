package dao;

import log.MyLogger;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/2 16:26
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MongoDB {
    private static Logger LOGGER = MyLogger.getMyLogger(MongoDB.class);
    private static MongoDB mongoDB = new MongoDB();
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> mongoCollection;

    private MongoDB() {
        //初始化mongodb
        WebProperties dbProperties = WebProperties.getWebProperties();
        java.util.Properties prop = dbProperties.getProp();
        if (prop != null) {
            String db_host = prop.getProperty("mongodb_host");
            int db_port = Integer.parseInt(prop.getProperty("mongodb_port"));
            String db_database = prop.getProperty("mongodb_database");
            String db_collection = prop.getProperty("mongodb_database_collection");
            try {
                // 连接到 mongodb 服务
                MongoClient mongoClient = new MongoClient(db_host, db_port);
                // 连接到数据库
                this.mongoDatabase = mongoClient.getDatabase(db_database);
                LOGGER.info("Connect to mongodb database successfully");
                this.mongoCollection = mongoDatabase.getCollection(db_collection);
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

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public MongoCollection<Document> getMongoCollection() {
        return mongoCollection;
    }

    public Document getDocumentById(String id){
        try {
            Document filter = new Document();
            filter.append("_id", new ObjectId(id));
            FindIterable<Document> iterables = getMongoCollection().find(filter).noCursorTimeout(true).batchSize(10000);
            if(iterables.first() != null){
                return iterables.first();
            }else {
                return null;
            }
        }catch (Exception e){
            LOGGER.error("getDocumentById err:" + e);
            return null;
        }
    }

    public List<Document>getDocumentByName(String name){
        List<Document> reusltDocuments = new ArrayList<>();
        FindIterable<Document> iterables = getMongoCollection().find(new Document("title", name)).sort(new Document("getTime", -1)).noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                reusltDocuments.add(law);
            }
        }catch (Exception e){
            LOGGER.error("getDocumentByName err:" + e);
            return reusltDocuments;
        }finally {
            cursor.close();
        }
        return reusltDocuments;
    }


}
