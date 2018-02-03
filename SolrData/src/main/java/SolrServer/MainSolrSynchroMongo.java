package SolrServer;

import Log.MyLogger;
import org.apache.log4j.Logger;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/2/3 15:09
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainSolrSynchroMongo {
    private static Logger LOGGER = MyLogger.getMyLogger(MainSolrSynchroMongo.class);
    public static void main(String[] args){
        String baseURL = "http://183.174.228.23:9993/solr/law";
        String collection = "law3";
        ImportData importDataToSolr = new ImportData(baseURL, collection);
        SolrServer solrServer = importDataToSolr.getSolrServer();
        try {
            solrServer.deleteById("5a117970ed8e9d2e68aa4844");
            LOGGER.info("Done delete!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
