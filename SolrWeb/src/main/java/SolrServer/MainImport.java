package SolrServer;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2017/12/31 18:13
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainImport {
    public static void main(String[] args){
        String baseURL = "http://localhost:8080/solr/law";
        String collection = "wanfangdata_law";
        ImportDataToSolr importDataToSolr = new ImportDataToSolr(baseURL, collection);
        SolrServer solrServer = importDataToSolr.getSolrServer();
        try {
            solrServer.deleteAll();
        }catch (Exception e){
            e.printStackTrace();
        }

        importDataToSolr.doImport();
   }
}
