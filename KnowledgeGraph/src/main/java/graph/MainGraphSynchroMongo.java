package graph;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/2/3 15:15
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainGraphSynchroMongo {
    public static void main(String[] args){
        Graph graph = new Graph("law");
        graph.deleteNodeById("5a3b57eded8e9d35307daabe");
    }

}
