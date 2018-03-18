package model;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/3/16 22:05
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class GraphNode {
    private String name;
    private String id;

    public GraphNode() {
        this.id = "";
        this.name = "";
    }

    public GraphNode(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
