package SolrServer;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/2 16:29
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainImportData {
    public static void main(String[] args) {
        String baseURL = "http://183.174.228.23:8080/solr/law";
        String collection = "law";
        ImportData importDataToSolr = new ImportData(baseURL, collection);
        SolrServer solrServer = importDataToSolr.getSolrServer();
        try {
            solrServer.deleteAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        importDataToSolr.doImport();
    }
}
