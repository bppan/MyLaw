package mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import log.MyLogger;
import neo4jDriver.GraphProperties;
import org.apache.log4j.Logger;
import org.bson.Document;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/16 16:48
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MongoServer {
    private static Logger LOGGER = MyLogger.getMyLogger(MongoServer.class);
    private static MongoServer mongoDB = new MongoServer();
    private MongoDatabase mongoDatabase;
    private MongoServer() {
        //初始化mongodb
        GraphProperties dbProperties = GraphProperties.getWebProperties();
        java.util.Properties prop = dbProperties.getProp();
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
            LOGGER.error("graph Properties not found!");
        }
    }

    public static MongoServer getMongoDB() {
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
}
