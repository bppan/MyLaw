package test;

import model.GraphPath;
import service.Neo4jService;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/3/17 15:02
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class testNeo4j {
    public static void main(String[] args){
        List<GraphPath> graphPathList = new ArrayList<>();
        try {
            Neo4jService neo4jService = new Neo4jService();
            graphPathList = neo4jService.getGraph("5a2e5745fd1c49151460fbfb", 3, 25, "all");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
