package graph;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import log.MyLogger;
import mongo.MongoServer;
import neo4jDriver.Neo4jDriver;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/16 16:14
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Graph {
    private static Logger LOGGER = MyLogger.getMyLogger(Graph.class);
    private Driver driver;

    public Graph() {
        this.driver = Neo4jDriver.getInstance().getDriver();
    }

    public static void main(String[] args) {
        Graph graph = new Graph();
        graph.importDataToGraph("law");
    }

    public void createNode(Document law) {
        Session session = driver.session();
        try {
            StringBuilder createNodecyphe = new StringBuilder("CREATE (n:law {");
            createNodecyphe.append("name:'").append(law.getString("title")).append("'");
            createNodecyphe.append(", id:'").append(law.getObjectId("_id").toString()).append("'");
            if (law.getString("department") != null && !law.getString("department").isEmpty()) {
                createNodecyphe.append(", 发布单位:'").append(law.getString("department")).append("'");
            }
            if (law.getString("release_date") != null && !law.getString("release_date").isEmpty()) {
                createNodecyphe.append(", 发布日期:'").append(law.getString("release_date")).append("'");
            }
            if (law.getString("release_number") != null && !law.getString("release_number").isEmpty()) {
                createNodecyphe.append(", 文号:'").append(law.getString("release_number")).append("'");
            }
            if (law.getString("implement_date") != null && !law.getString("implement_date").isEmpty()) {
                createNodecyphe.append(", 生效日期:'").append(law.getString("implement_date")).append("'");
            }
            if (law.getString("category") != null && !law.getString("category").isEmpty()) {
                createNodecyphe.append(", 类别:'").append(law.getString("category")).append("'");
            }
            if (law.getString("level") != null && !law.getString("level").isEmpty()) {
                createNodecyphe.append(", 法律级别:'").append(law.getString("level")).append("'");
            }
            if (law.getString("timeless") != null && !law.getString("timeless").isEmpty()) {
                createNodecyphe.append(", 时效性:'").append(law.getString("timeless")).append("'");
            }
            if (law.getString("content") != null && !law.getString("content").isEmpty()) {
                createNodecyphe.append(", 内容:'").append(law.getString("content")).append("'");
            }
            if (law.getInteger("article_num") != null) {
                createNodecyphe.append(", 法条数:").append(law.getInteger("article_num"));
            }
            createNodecyphe.append("})");
            session.run(createNodecyphe.toString());
            LOGGER.info("create node done: " + law.getString("title"));
        } catch (Exception e) {
            LOGGER.info("create node in neo4j err: " + e);
        } finally {
            session.close();
        }

    }

    public void createChildNode() {
        Session session = driver.session();

    }

    public void createRelationShip(Document law) {
        List<Document> documentList = (List<Document>) law.get("articles");
        if (documentList.size() == 0) {
            return;
        }
    }

    @SuppressWarnings("unchecked")
    public void importDataToGraph(String collectionName) {
        MongoServer mongoServer = MongoServer.getMongoDB();
        MongoCollection<Document> collection = mongoServer.getCollection(collectionName);
        FindIterable<Document> iterables = collection.find(new Document("url", "http://www.pkulaw.cn/fulltext_form.aspx?Db=chl&Gid=401d47f76b65500d&keyword=&EncodingName=&Search_Mode=accurate&Search_IsTitle=0")).noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
//                List<Document> documentList = (List<Document>)law.get("articles");
//                for (Document document:documentList) {
//                    if(document.getString("name").isEmpty()){
//                        System.out.println("dddddddd");
//                    }
//                    System.out.println(document.getString("name"));
//                }
//                this.createNode(law);
            }
        } catch (Exception e) {
            LOGGER.info("read data from mongodb err: " + e);
        } finally {
//            driver.close();
            cursor.close();
        }
    }
}
