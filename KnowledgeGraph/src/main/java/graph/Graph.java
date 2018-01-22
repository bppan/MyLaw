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
import util.NumberChange;

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

    public boolean createNode(Document law) {
        Session session = driver.session();
        try {
            StringBuilder createNodecyphe = new StringBuilder("MERGE (n:law {");
            createNodecyphe.append("name:'").append(law.getString("title")).append("'");
            createNodecyphe.append(", id:'").append(law.getObjectId("_id").toString()).append("'");
            if (law.getString("department") != null && !law.getString("department").isEmpty()) {
                createNodecyphe.append(", department:'").append(law.getString("department")).append("'");
            }
            if (law.getString("release_date") != null && !law.getString("release_date").isEmpty()) {
                createNodecyphe.append(", release_date:'").append(law.getString("release_date")).append("'");
            }
            if (law.getString("release_number") != null && !law.getString("release_number").isEmpty()) {
                createNodecyphe.append(", release_number:'").append(law.getString("release_number")).append("'");
            }
            if (law.getString("implement_date") != null && !law.getString("implement_date").isEmpty()) {
                createNodecyphe.append(", implement_date:'").append(law.getString("implement_date")).append("'");
            }
            if (law.getString("category") != null && !law.getString("category").isEmpty()) {
                createNodecyphe.append(", category:'").append(law.getString("category")).append("'");
            }
            if (law.getString("level") != null && !law.getString("level").isEmpty()) {
                createNodecyphe.append(", level:'").append(law.getString("level")).append("'");
            }
            if (law.getString("timeless") != null && !law.getString("timeless").isEmpty()) {
                createNodecyphe.append(", timeless:'").append(law.getString("timeless")).append("'");
            }
            if (law.getInteger("article_num") != null) {
                createNodecyphe.append(", article_num:").append(law.getInteger("article_num"));
            }
            createNodecyphe.append("})");
            session.run(createNodecyphe.toString());
            LOGGER.info("create node done: " + law.getString("title"));
            return true;
        } catch (Exception e) {
            LOGGER.info("create node in neo4j err: " + e);
            return false;
        } finally {
            session.close();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean createChildNode(Document law) {
        Session session = driver.session();
        try {
            List<Document> documentList = (List<Document>) law.get("articles");
            if (documentList.size() == 0) {
                LOGGER.info("the law have no child");
                return true;
            }
            if (documentList.size() == 1 && documentList.get(0).getString("name").trim().isEmpty()) {
                LOGGER.info("the law have no child");
                return true;
            }

            for (int i = 0; i < documentList.size(); i++) {
                //创建法条
                String name = documentList.get(i).getString("name");
                String childId = law.getObjectId("_id").toString() + "-" + i;
                StringBuilder createNodecyphe = new StringBuilder("MERGE (n:article {");
                createNodecyphe.append("name:'").append(name).append("'");
                createNodecyphe.append(", id:'").append(childId).append("'");
                createNodecyphe.append("})");
                session.run(createNodecyphe.toString());
                //创建法条款项
                List<String> para = (List<String>) documentList.get(i).get("paragraph");
                if (para.size() <= 1) {
                    continue;
                }
                for (int j = 0; j < para.size(); j++) {
                    String childName = "第" + NumberChange.numberToChinese(j + 1) + "款";
                    String childChildId = childId + "-" + j;
                    StringBuilder createChildNodecyphe = new StringBuilder("MERGE (n:paragraph {");
                    createChildNodecyphe.append("name:'").append(childName).append("'");
                    createChildNodecyphe.append(", id:'").append(childChildId).append("'");
                    createChildNodecyphe.append("})");
                    session.run(createChildNodecyphe.toString());
                }
            }
            LOGGER.info("create first layer child node done: " + law.getString("title"));
            return true;
        } catch (Exception e) {
            LOGGER.info("create first layer child node in neo4j err: " + e);
            return false;
        } finally {
            session.close();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean createRelationship(Document law) {
        Session session = driver.session();
        try {
            List<Document> documentList = (List<Document>) law.get("articles");
            if (documentList.size() == 0) {
                LOGGER.info("the law have no child relationship");
                return true;
            }
            if (documentList.size() == 1 && documentList.get(0).getString("name").trim().isEmpty()) {
                LOGGER.info("the law have no child relationship");
                return true;
            }
            for (int i = 0; i < documentList.size(); i++) {
                //创建法律和法条
                StringBuilder createNodecyphe = new StringBuilder("MATCH (a:law {");
                createNodecyphe.append("id: '").append(law.getObjectId("_id").toString()).append("'}), ");
                String childId = law.getObjectId("_id").toString() + "-" + i;
                createNodecyphe.append("(b:article {");
                createNodecyphe.append("id: '").append(childId).append("'}) ");
                createNodecyphe.append("MERGE (a)").append("-[:").append("有").append("]").append("->(b)");
                session.run(createNodecyphe.toString());
                //创建法条款项关系
                List<String> para = (List<String>) documentList.get(i).get("paragraph");
                if (para.size() <= 1) {
                    continue;
                }
                for (int j = 0; j < para.size(); j++) {
                    StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (a:article {");
                    createNodeChildRelationshipcyphe.append("id: '").append(childId).append("'}), ");
                    String childChildId = childId + "-" + j;
                    createNodeChildRelationshipcyphe.append("(b:paragraph {");
                    createNodeChildRelationshipcyphe.append("id: '").append(childChildId).append("'}) ");
                    createNodeChildRelationshipcyphe.append("MERGE (a)").append("-[:").append("有").append("]").append("->(b)");
                    session.run(createNodeChildRelationshipcyphe.toString());
                }
            }
            LOGGER.info("create relationShip done: " + law.getString("title"));
            return true;
        } catch (Exception e) {
            LOGGER.info("create child node relationShip in neo4j err: " + e);
            return false;
        } finally {
            session.close();
        }
    }

    public void importDataToGraph(String collectionName) {
        MongoServer mongoServer = MongoServer.getMongoDB();
        MongoCollection<Document> collection = mongoServer.getCollection(collectionName);
        FindIterable<Document> iterables = collection.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                this.createNode(law);
                this.createChildNode(law);
                this.createRelationship(law);
            }
        } catch (Exception e) {
            LOGGER.info("read data from mongodb err: " + e);
        } finally {
            driver.close();
            cursor.close();
        }
    }
}
