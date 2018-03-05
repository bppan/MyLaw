package graph;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import log.MyLogger;
import mongo.MongoServer;
import neo4jDriver.Neo4jDriver;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
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
    private MongoCollection<Document> sourceCollection;
    private Driver driver;

    public Graph() {
        this.driver = Neo4jDriver.getInstance().getDriver();
    }

    public Graph(String sourceCollectionName) {
        MongoServer mongoServer = MongoServer.getMongoDB();
        this.sourceCollection = mongoServer.getCollection(sourceCollectionName);
        this.driver = Neo4jDriver.getInstance().getDriver();
    }

    public MongoCollection<Document> getSourceCollection() {
        return sourceCollection;
    }

    public boolean createNode(Document law, Session session) {
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
            LOGGER.error("create node in neo4j err: " + e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean createChildNode(Document law, Session session) {
        String lawName = law.getString("title");
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
                String articleName = lawName + documentList.get(i).getString("name");
                String articleId = law.getObjectId("_id").toString() + "-" + i;
                StringBuilder createNodecyphe = new StringBuilder("MERGE (a:article {");
                createNodecyphe.append("name:'").append(articleName).append("'");
                createNodecyphe.append(", id:'").append(articleId).append("'");
                //创建法条款项
                List<String> para = (List<String>) documentList.get(i).get("paragraph");
                createNodecyphe.append(", paragraph_num:").append(para.size());
                if (para.size() == 1) {
                    createNodecyphe.append(", content:'").append(para.get(0).trim()).append("'");
                    createNodecyphe.append("})");
                    session.run(createNodecyphe.toString());
                    continue;
                }
                createNodecyphe.append("})");
                session.run(createNodecyphe.toString());

                for (int j = 0; j < para.size(); j++) {
                    String paraName = articleName + "第" + NumberChange.numberToChinese(j + 1) + "款";
                    String paragraphId = articleId + "-" + j;
                    StringBuilder createChildNodecyphe = new StringBuilder("MERGE (p:paragraph {");
                    createChildNodecyphe.append("name:'").append(paraName).append("'");
                    createChildNodecyphe.append(", id:'").append(paragraphId).append("'");
                    createChildNodecyphe.append(", content:'").append(para.get(j).trim()).append("'");
                    createChildNodecyphe.append("})");
                    session.run(createChildNodecyphe.toString());
                }
            }
            LOGGER.info("create first layer child node done: " + law.getString("title"));
            return true;
        } catch (Exception e) {
            LOGGER.error("create first layer child node in neo4j err: " + e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean createRelationship(Document law, Session session) {
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
                StringBuilder createNodecyphe = new StringBuilder("MATCH (n:law {");
                createNodecyphe.append("id: '").append(law.getObjectId("_id").toString()).append("'}), ");
                String articleId = law.getObjectId("_id").toString() + "-" + i;
                createNodecyphe.append("(a:article {");
                createNodecyphe.append("id: '").append(articleId).append("'}) ");
                createNodecyphe.append("MERGE (n)").append("-[:").append("有").append("]").append("->(a)");
                session.run(createNodecyphe.toString());
                //创建法条款项关系
                List<String> para = (List<String>) documentList.get(i).get("paragraph");
                if (para.size() <= 1) {
                    continue;
                }
                for (int j = 0; j < para.size(); j++) {
                    StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (a:article {");
                    createNodeChildRelationshipcyphe.append("id: '").append(articleId).append("'}), ");
                    String paragraphId = articleId + "-" + j;
                    createNodeChildRelationshipcyphe.append("(p:paragraph {");
                    createNodeChildRelationshipcyphe.append("id: '").append(paragraphId).append("'}) ");
                    createNodeChildRelationshipcyphe.append("MERGE (a)").append("-[:").append("有").append("]").append("->(p)");
                    session.run(createNodeChildRelationshipcyphe.toString());
                }
            }
            LOGGER.info("create relationShip done: " + law.getString("title"));
            return true;
        } catch (Exception e) {
            LOGGER.error("create child node relationShip in neo4j err: " + e);
            return false;
        }
    }

    public void importDataToGraph() {
        FindIterable<Document> iterables = this.sourceCollection.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        Session session = driver.session();
        int num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                long startTime = System.currentTimeMillis();
                this.createNode(law, session);
                this.createChildNode(law, session);
                this.createRelationship(law, session);
                long endTime = System.currentTimeMillis();
                num++;
                LOGGER.info("import law num:" + num + " cost time:" + (endTime - startTime));
            }
        } catch (Exception e) {
            LOGGER.error("read data from mongodb err: " + e);
        } finally {
            session.close();
            driver.close();
            cursor.close();
        }
    }

    public void deleteLawNodeSimple() {
        FindIterable<Document> iterables = this.sourceCollection.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        Session session = driver.session();
        int num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                long startTime = System.currentTimeMillis();
                this.deleteNode(law, session);
                long endTime = System.currentTimeMillis();
                num++;
                LOGGER.info("import law num:" + num + " cost time:" + (endTime - startTime));
            }
        } catch (Exception e) {
            LOGGER.error("read data from mongodb err: " + e);
        } finally {
            session.close();
            driver.close();
            cursor.close();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean deleteNode(Document law, Session session) {
        LOGGER.info("deleteNode begin...");
        String lawId = law.getObjectId("_id").toString();
        List<Document> documentList = (List<Document>) law.get("articles");
        try {
            if (documentList.size() > 1 && !documentList.get(0).getString("name").isEmpty()) {
                LOGGER.info("deleteNode article...");
                for (int i = 0; i < documentList.size(); i++) {
                    //创建法律和法条
                    String articleId = law.getObjectId("_id").toString() + "-" + i;
                    //创建法条款项关系
                    List<String> para = (List<String>) documentList.get(i).get("paragraph");
                    if (para.size() > 1) {
                        LOGGER.info("deleteNode paragraph...");
                        for (int j = 0; j < para.size(); j++) {
                            String paragraphId = articleId + "-" + j;
                            StringBuilder deleteNodeCyphe = new StringBuilder("MATCH (a:article)-[r]-(p:paragraph) where a.id = '");
                            deleteNodeCyphe.append(articleId).append("'");
                            deleteNodeCyphe.append(" and ").append("p.id = '").append(paragraphId).append("'");
                            deleteNodeCyphe.append(" DETACH delete p,r");
                            session.run(deleteNodeCyphe.toString());
                        }
                    }
                    StringBuilder deleteNodeCyphe = new StringBuilder("MATCH (n:law)-[r]-(a:article) where n.id = '");
                    deleteNodeCyphe.append(lawId).append("'");
                    deleteNodeCyphe.append(" and ").append("a.id = '").append(articleId).append("'");
                    deleteNodeCyphe.append(" DETACH delete a,r");
                    session.run(deleteNodeCyphe.toString());
                }
            }
            StringBuilder deleteNodeCyphe = new StringBuilder("MATCH (n:law) where n.id = '");
            deleteNodeCyphe.append(lawId).append("'").append(" delete n");
            session.run(deleteNodeCyphe.toString());
            return true;
        } catch (Exception e) {
            LOGGER.error("deleteNode err: " + e);
            return false;
        } finally {
            LOGGER.info("deleteNode end...");
        }
    }

    public boolean deleteNodeById(String id) {
        FindIterable<Document> iterables = this.sourceCollection.find(new Document("_id", new ObjectId(id))).noCursorTimeout(true).batchSize(10000);
        Session session = driver.session();
        try {
            if (iterables.first() != null) {
                Document law = iterables.first();
                LOGGER.info("delete law title: " + law.getString("title"));
                this.deleteNode(law, session);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("deleteNodeById err: " + e);
            return false;
        } finally {
            session.close();
        }
    }

    //法律-法律
    public void createRelationshipLawToLaw(String formId, String toId, String relationShipTag) {
        LOGGER.info("createRelationshipLawToLaw begin");
        Session session = driver.session();
        try {
            StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (n:law {");
            createNodeChildRelationshipcyphe.append("id: '").append(formId).append("'}), ");
            createNodeChildRelationshipcyphe.append("(m:law {");
            createNodeChildRelationshipcyphe.append("id: '").append(toId).append("'}) ");
            createNodeChildRelationshipcyphe.append("MERGE (n)").append("-[:").append(relationShipTag).append("]").append("->(m)");
            session.run(createNodeChildRelationshipcyphe.toString());
        } catch (Exception e) {
            LOGGER.error("createRelationshipLawToLaw err: " + e);
        } finally {
            LOGGER.info("createRelationshipLawToLaw done");
            session.close();
        }
    }

    //法律-条
    public void createRelationshipLawToTiao(String formId, String toId, String relationShipTag) {
        LOGGER.info("createRelationshipLawToTiao begin");
        Session session = driver.session();
        try {
            StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (n:law {");
            createNodeChildRelationshipcyphe.append("id: '").append(formId).append("'}), ");
            createNodeChildRelationshipcyphe.append("(a:article {");
            createNodeChildRelationshipcyphe.append("id: '").append(toId).append("'}) ");
            createNodeChildRelationshipcyphe.append("MERGE (n)").append("-[:").append(relationShipTag).append("]").append("->(a)");
            session.run(createNodeChildRelationshipcyphe.toString());
        } catch (Exception e) {
            LOGGER.error("createRelationshipLawToTiao err: " + e);
        } finally {
            LOGGER.info("createRelationshipLawToTiao done");
            session.close();
        }

    }

    //法律-款
    public void createRelationshipLawToKuan(String formId, String toId, String relationShipTag) {
        LOGGER.info("createRelationshipLawToKuan begin");
        Session session = driver.session();
        try {
            StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (n:law {");
            createNodeChildRelationshipcyphe.append("id: '").append(formId).append("'}), ");
            createNodeChildRelationshipcyphe.append("(p:paragraph {");
            createNodeChildRelationshipcyphe.append("id: '").append(toId).append("'}) ");
            createNodeChildRelationshipcyphe.append("MERGE (n)").append("-[:").append(relationShipTag).append("]").append("->(p)");
            session.run(createNodeChildRelationshipcyphe.toString());
        } catch (Exception e) {
            LOGGER.error("createRelationshipLawToKuan err: " + e);
        } finally {
            LOGGER.info("createRelationshipLawToKuan done");
            session.close();
        }

    }

    //条-法律
    public void createRelationshipTiaoToLaw(String formId, String toId, String relationShipTag) {
        LOGGER.info("createRelationshipTiaoToLaw begin");
        Session session = driver.session();
        try {
            StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (a:article {");
            createNodeChildRelationshipcyphe.append("id: '").append(formId).append("'}), ");
            createNodeChildRelationshipcyphe.append("(n:law {");
            createNodeChildRelationshipcyphe.append("id: '").append(toId).append("'}) ");
            createNodeChildRelationshipcyphe.append("MERGE (a)").append("-[:").append(relationShipTag).append("]").append("->(n)");
            session.run(createNodeChildRelationshipcyphe.toString());
        } catch (Exception e) {
            LOGGER.error("createRelationshipTiaoToLaw err: " + e);
        } finally {
            LOGGER.info("createRelationshipTiaoToLaw done");
            session.close();
        }
    }

    //条-条
    public void createRelationshipTiaoToTiao(String formId, String toId, String relationShipTag) {
        LOGGER.info("createRelationshipTiaoToTiao begin");
        Session session = driver.session();
        try {
            StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (a:article {");
            createNodeChildRelationshipcyphe.append("id: '").append(formId).append("'}), ");
            createNodeChildRelationshipcyphe.append("(b:article {");
            createNodeChildRelationshipcyphe.append("id: '").append(toId).append("'}) ");
            createNodeChildRelationshipcyphe.append("MERGE (a)").append("-[:").append(relationShipTag).append("]").append("->(b)");
            session.run(createNodeChildRelationshipcyphe.toString());
        } catch (Exception e) {
            LOGGER.error("createRelationshipTiaoToTiao err: " + e);
        } finally {
            LOGGER.info("createRelationshipTiaoToTiao done");
            session.close();
        }
    }

    //条-款
    public void createRelationshipTiaoToPar(String formId, String toId, String relationShipTag) {
        LOGGER.info("createRelationshipTiaoToPar begin");
        Session session = driver.session();
        try {
            StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (a:article {");
            createNodeChildRelationshipcyphe.append("id: '").append(formId).append("'}), ");
            createNodeChildRelationshipcyphe.append("(p:paragraph {");
            createNodeChildRelationshipcyphe.append("id: '").append(toId).append("'}) ");
            createNodeChildRelationshipcyphe.append("MERGE (a)").append("-[:").append(relationShipTag).append("]").append("->(p)");
            session.run(createNodeChildRelationshipcyphe.toString());
        } catch (Exception e) {
            LOGGER.error("createRelationshipTiaoToPar err: " + e);
        } finally {
            LOGGER.info("createRelationshipTiaoToPar done");
            session.close();
        }
    }

    //款-法律
    public void createRelationshipParToLaw(String formId, String toId, String relationShipTag) {
        LOGGER.info("createRelationshipParToLaw begin");
        Session session = driver.session();
        try {
            StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (p:paragraph {");
            createNodeChildRelationshipcyphe.append("id: '").append(formId).append("'}), ");
            createNodeChildRelationshipcyphe.append("(n:law {");
            createNodeChildRelationshipcyphe.append("id: '").append(toId).append("'}) ");
            createNodeChildRelationshipcyphe.append("MERGE (p)").append("-[:").append(relationShipTag).append("]").append("->(n)");
            session.run(createNodeChildRelationshipcyphe.toString());
        } catch (Exception e) {
            LOGGER.error("createRelationshipParToLaw err: " + e);
        } finally {
            LOGGER.info("createRelationshipParToLaw done");
            session.close();
        }
    }

    //款-条
    public void createRelationshipParToTiao(String formId, String toId, String relationShipTag) {
        LOGGER.info("createRelationshipParToTiao begin");
        Session session = driver.session();
        try {
            StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (p:paragraph {");
            createNodeChildRelationshipcyphe.append("id: '").append(formId).append("'}), ");
            createNodeChildRelationshipcyphe.append("(a:article {");
            createNodeChildRelationshipcyphe.append("id: '").append(toId).append("'}) ");
            createNodeChildRelationshipcyphe.append("MERGE (p)").append("-[:").append(relationShipTag).append("]").append("->(a)");
            session.run(createNodeChildRelationshipcyphe.toString());
        } catch (Exception e) {
            LOGGER.error("createRelationshipParToTiao err: " + e);
        } finally {
            LOGGER.info("createRelationshipParToTiao done");
            session.close();
        }
    }

    //款-款
    public void createRelationshipParToPar(String formId, String toId, String relationShipTag) {
        LOGGER.info("createRelationshipParToPar begin");
        Session session = driver.session();
        try {
            StringBuilder createNodeChildRelationshipcyphe = new StringBuilder("MATCH (b:paragraph {");
            createNodeChildRelationshipcyphe.append("id: '").append(formId).append("'}), ");
            createNodeChildRelationshipcyphe.append("(p:paragraph {");
            createNodeChildRelationshipcyphe.append("id: '").append(toId).append("'}) ");
            createNodeChildRelationshipcyphe.append("MERGE (b)").append("-[:").append(relationShipTag).append("]").append("->(p)");
            session.run(createNodeChildRelationshipcyphe.toString());
        } catch (Exception e) {
            LOGGER.error("createRelationshipParToPar err: " + e);
        } finally {
            LOGGER.info("createRelationshipParToPar done");
            session.close();
        }
    }

    //根据传入的id判断创建何种节点之间的关系
    public boolean createRelationshipLawAuto(String formId, String toId, String relationShipTag) {
        int fromIdLength = formId.split("-").length;
        int toIdLength = toId.split("-").length;
        LOGGER.info("createRelationshipLawAuto begin fromIdLength: " + fromIdLength + " toIdLength: " + toIdLength);
        if (fromIdLength == 1 && toIdLength == 1) {
            createRelationshipLawToLaw(formId, toId, relationShipTag);
            return true;
        }
        if (fromIdLength == 1 && toIdLength == 2) {
            createRelationshipLawToTiao(formId, toId, relationShipTag);
            return true;
        }
        if (fromIdLength == 1 && toIdLength == 3) {
            createRelationshipLawToKuan(formId, toId, relationShipTag);
            return true;
        }

        if (fromIdLength == 2 && toIdLength == 1) {
            createRelationshipTiaoToLaw(formId, toId, relationShipTag);
            return true;
        }
        if (fromIdLength == 2 && toIdLength == 2) {
            createRelationshipTiaoToTiao(formId, toId, relationShipTag);
            return true;
        }
        if (fromIdLength == 2 && toIdLength == 3) {
            createRelationshipTiaoToPar(formId, toId, relationShipTag);
            return true;
        }

        if (fromIdLength == 3 && toIdLength == 1) {
            createRelationshipParToLaw(formId, toId, relationShipTag);
            return true;
        }
        if (fromIdLength == 3 && toIdLength == 2) {
            createRelationshipParToTiao(formId, toId, relationShipTag);
            return true;
        }
        if (fromIdLength == 3 && toIdLength == 3) {
            createRelationshipParToPar(formId, toId, relationShipTag);
            return true;
        }
        return false;
    }

    //关闭driver
    public boolean CloseDriver() {
        try {
            this.driver.close();
        } catch (Exception e) {
            LOGGER.error("neo4j driver close error: " + e);
            return false;
        }
        return true;
    }
}
