package model;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/3/16 22:02
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class GraphPath {
    private GraphNode startNode;
    private GraphNode endNode;
    private String relationShip;

    public GraphNode getStartNode() {
        return startNode;
    }

    public void setStartNode(GraphNode startNode) {
        this.startNode = startNode;
    }

    public GraphNode getEndNode() {
        return endNode;
    }

    public void setEndNode(GraphNode endNode) {
        this.endNode = endNode;
    }

    public String getRelationShip() {
        return relationShip;
    }

    public void setRelationShip(String relationShip) {
        this.relationShip = relationShip;
    }
    public GraphPath(GraphNode startNode, GraphNode endNode){
        this.startNode = startNode;
        this.endNode = endNode;
    }
    public GraphPath(GraphNode startNode, GraphNode endNode, String relationShip){
        this.startNode = startNode;
        this.endNode = endNode;
        this.relationShip = relationShip;
    }
}
