package service;

import dao.Neo4jDriver;
import log.MyLogger;
import model.Document;
import model.GraphNode;
import model.GraphPath;
import org.apache.log4j.Logger;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/3/16 21:35
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Neo4jService {
    private static Logger LOGGER = MyLogger.getMyLogger(Neo4jDriver.class);
    private Driver driver;

    public Neo4jService() {
        this.driver = Neo4jDriver.getInstance().getDriver();
    }

    public List<GraphPath> getGraph(String id, int layerNum, int limitNum, String toNodeType) {
        String nodeType = getNodeType(id);
        List<GraphPath> graphPaths = new ArrayList<>();
        try {
            StringBuilder getGraphcyphe = new StringBuilder("MATCH p=");
            getGraphcyphe.append("(n:").append(nodeType).append(")").append("-[*1..").append(layerNum).append("]->");
            if (toNodeType.trim().equals("law") || toNodeType.trim().equals("article") || toNodeType.trim().equals("paragraph")) {
                getGraphcyphe.append("(m:").append(toNodeType).append(")");
            } else {
                getGraphcyphe.append("()");
            }
            getGraphcyphe.append(" where n.id='").append(id).append("' RETURN p").append(" LIMIT ").append(limitNum);
            getpaths(graphPaths, getGraphcyphe.toString());

            getGraphcyphe = new StringBuilder("MATCH p=");
            if (toNodeType.trim().equals("law") || toNodeType.trim().equals("article") || toNodeType.trim().equals("paragraph")) {
                getGraphcyphe.append("(m:").append(toNodeType).append(")");
            } else {
                getGraphcyphe.append("()");
            }
            getGraphcyphe.append("-[*1..").append(layerNum).append("]->(n:").append(nodeType).append(")");
            getGraphcyphe.append(" where n.id='").append(id).append("' RETURN p").append(" LIMIT ").append(limitNum);
            getpaths(graphPaths, getGraphcyphe.toString());

            if (graphPaths.size() == 0) {
                GraphNode firstNode = getNodeById(id);
                graphPaths.add(new GraphPath(firstNode, new GraphNode(), ""));
            }
        } catch (Exception e) {
            LOGGER.info("Neo4jService getPath err：" + e.getMessage());
        }

        return graphPaths;
    }

    private String getNodeType(String nodeId) {
        String[] splitId = nodeId.split("-");
        String nodeType = "law";
        if (splitId.length == 2) {
            nodeType = "article";
        } else if (splitId.length == 3) {
            nodeType = "paragraph";
        }
        return nodeType;
    }

    public List<GraphPath> getNodeGraph(String id, int limitNum) {
        String nodeType = getNodeType(id);
        List<GraphPath> graphPaths = new ArrayList<>();
        try {
            StringBuilder getGraphcyphe = new StringBuilder("MATCH p=");
            getGraphcyphe.append("(n:").append(nodeType).append(")").append("-[]->()");
            getGraphcyphe.append(" where n.id='").append(id).append("' RETURN p").append(" LIMIT ").append(limitNum);
            getpaths(graphPaths, getGraphcyphe.toString());

            getGraphcyphe = new StringBuilder("MATCH p=");
            getGraphcyphe.append("()").append("-[]->(n:").append(nodeType).append(")");
            getGraphcyphe.append(" where n.id='").append(id).append("' RETURN p").append(" LIMIT ").append(limitNum);
            getpaths(graphPaths, getGraphcyphe.toString());

            if (graphPaths.size() == 0) {
                GraphNode firstNode = getNodeById(id);
                graphPaths.add(new GraphPath(firstNode, new GraphNode(), ""));
            }
        } catch (Exception e) {
            LOGGER.info("Neo4jService getNodeGraph err：" + e.getMessage());
        }
        return graphPaths;
    }


    public void getpaths(List<GraphPath> graphPaths, String cypher) {
        Session session = driver.session();
        List<GraphPath> currentgraphPaths = new ArrayList<>();
        LOGGER.info("getPaths cypher: " + cypher);
        try {
            StatementResult resultStart = session.run(cypher);
            while (resultStart.hasNext()) {
                //get one path
                Record record = resultStart.next();
                org.neo4j.driver.v1.types.Path path = record.get("p").asPath();
                int startAddpathIndex = currentgraphPaths.size();
                //get node of path
                Iterable<Node> pNodeIterable = path.nodes();
                Iterator<Node> pNodeIterator = pNodeIterable.iterator();
                List<GraphNode> pathNode = new ArrayList<>();
                while (pNodeIterator.hasNext()) {
                    Node node = pNodeIterator.next();
                    String nodeId = node.get("id").asString();
                    String nodeName = node.get("name").asString();
                    GraphNode graphNode = new GraphNode(nodeId, nodeName);
                    pathNode.add(graphNode);
                }
                if (pathNode.size() == 0) {
                    continue;
                }
                GraphNode firstNode = pathNode.get(0);
                GraphNode secondNode = new GraphNode();
                if (pathNode.size() == 1) {
                    currentgraphPaths.add(new GraphPath(firstNode, secondNode, ""));
                    continue;
                }
                secondNode = pathNode.get(1);
                currentgraphPaths.add(new GraphPath(firstNode, secondNode));
                for (int i = 2; i < pathNode.size(); i++) {
                    firstNode = secondNode;
                    secondNode = pathNode.get(i);
                    currentgraphPaths.add(new GraphPath(firstNode, secondNode));
                }
                //get relationship of node
                Iterable<Relationship> relationshipIterable = path.relationships();
                Iterator<Relationship> relationshipIterator = relationshipIterable.iterator();
                int i = startAddpathIndex;
                while (relationshipIterator.hasNext()) {
                    Relationship relationship = relationshipIterator.next();
                    String relationType = relationship.type();
                    currentgraphPaths.get(i).setRelationShip(relationType);
                    i++;
                }
            }
        } catch (Exception e) {
            LOGGER.error("get path from neo4j error: " + e);
        } finally {
            session.close();

        }
        for (GraphPath graphPath : currentgraphPaths) {
            addPathInGraph(graphPaths, graphPath.getStartNode(), graphPath.getEndNode(), graphPath.getRelationShip());
        }
    }

    private boolean addPathInGraph(List<GraphPath> graphPaths, GraphNode startNode, GraphNode endNode, String relationship) {
        if (graphPaths == null) {
            graphPaths = new ArrayList<>();
            graphPaths.add(new GraphPath(startNode, endNode, relationship));
            return true;
        }
        for (GraphPath graphPath : graphPaths) {
            if (graphPath.getStartNode().getId().equals(startNode.getId()) &&
                    graphPath.getEndNode().getId().equals(endNode.getId()) && graphPath.getRelationShip().equals(relationship)) {
                return false;
            }
        }
        graphPaths.add(new GraphPath(startNode, endNode, relationship));
        return true;
    }

    public GraphNode getNodeById(String id) {
        Session session = driver.session();
        GraphNode node = null;
        try {
            String nodeType = getNodeType(id);
            StringBuilder getGraphcyphe = new StringBuilder("MATCH ");
            getGraphcyphe.append("(n:").append(nodeType).append(")");
            getGraphcyphe.append(" where n.id='").append(id).append("'").append(" return n");
            StatementResult resultStart = session.run(getGraphcyphe.toString());
            while (resultStart.hasNext()) {
                Record record = resultStart.next();
                String nodeId = record.get("n").get("id").asString();
                String nodeName = record.get("n").get("name").asString();
                node = new GraphNode(nodeId, nodeName);
            }
            return node;
        } catch (Exception e) {
            LOGGER.info("getNodeById from neo4j error: " + e);
            return node;
        } finally {
            session.close();
        }
    }

    public List<Document> getRelationshipLaw(String id, int limitNum) {
        Session session = driver.session();
        List<Document> resultLaw = new ArrayList<>();
        try {
            String nodeType = getNodeType(id);
            StringBuilder getGraphcyphe = new StringBuilder("MATCH p=");
            getGraphcyphe.append("(n:").append(nodeType).append(")").append("-[*1..60]->(m:law)");
            getGraphcyphe.append(" where n.id='").append(id).append("' RETURN p").append(" LIMIT ").append(limitNum);
            getRelationshipLawRunCypher(resultLaw, getGraphcyphe.toString(), id);
            if (resultLaw.size() < limitNum) {
                getGraphcyphe = new StringBuilder("MATCH p=");
                getGraphcyphe.append("(m:law)").append("-[*1..60]->");
                getGraphcyphe.append("(n:").append(nodeType).append(")");
                getGraphcyphe.append(" where n.id='").append(id).append("' RETURN p").append(" LIMIT ").append(limitNum);
                getRelationshipLawRunCypher(resultLaw, getGraphcyphe.toString(), id);
            }
            return resultLaw;
        } catch (Exception e) {
            LOGGER.info("getRelationshipLaw from neo4j error: " + e);
            return resultLaw;
        } finally {
            session.close();
        }
    }

    private void getRelationshipLawRunCypher(List<Document> resultLaw, String cypher, String id) {
        List<GraphPath> graphPaths = new ArrayList<>();
        getpaths(graphPaths, cypher);
        for (GraphPath graphPath : graphPaths) {
            String startNodeId = graphPath.getStartNode().getId();
            String endNodeId = graphPath.getEndNode().getId();
            if (!startNodeId.equals(id) && getNodeType(startNodeId).equals("law")) {
                String name = graphPath.getStartNode().getName();
                addRelationLaw(resultLaw, startNodeId, name);
            }
            if (!endNodeId.equals(id) && getNodeType(endNodeId).equals("law")) {
                String name = graphPath.getEndNode().getName();
                addRelationLaw(resultLaw, endNodeId, name);
            }
        }
    }

    private boolean addRelationLaw(List<Document> resultRelationship, String id, String name) {
        for (Document document : resultRelationship) {
            if (document.getId().equals("id")) {
                return false;
            }
        }
        Document document = new Document();
        document.setId(id);
        document.setTitle(name);
        resultRelationship.add(document);
        return true;
    }

}
