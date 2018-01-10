package dao;

import log.MyLogger;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.util.Collection;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/8 14:06
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class SolrServer {
    private static Logger LOGGER = MyLogger.getMyLogger(SolrServer.class);
    private static SolrServer solrServer = new SolrServer();
    private HttpSolrClient solrClient;

    private SolrServer() {
        //初始化mongodb
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        WebProperties webPropertiesProperties = WebProperties.getWebProperties();
        java.util.Properties prop = webPropertiesProperties.getProp();
        if (prop != null) {
            String baseURL = prop.getProperty("solrServerAddress");
            LOGGER.info("Begin connect solr server: " + baseURL);
            try {

                this.solrClient = new HttpSolrClient.Builder(baseURL).build();
            } catch (Exception e) {
                LOGGER.error("Connect solr server failure: " + e.getMessage());
            }
            LOGGER.info("Connect solr server successfully....");
        } else {
            LOGGER.error("Web Properties not found!");
        }
    }

    public static SolrServer getSolrServer() {
        return solrServer;
    }

    public HttpSolrClient getSolrClient() {
        return this.solrClient;
    }

    public void closeServer() {
        try {
            this.solrClient.close();
            LOGGER.info("Close solr server successfully...");
        } catch (IOException e) {
            LOGGER.error("Close solr server err: " + e.getMessage());
        }
    }
    public void get(String name) throws IOException, SolrServerException {

        HttpSolrClient solrServer = this.solrClient;
        solrServer.setSoTimeout(5000);
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*"); //查询所有
//        query.setQuery(name+"*");
        query.setRows(1000);
        query.setStart(0);
        QueryResponse response = solrServer.query(query, SolrRequest.METHOD.POST);
        SolrDocumentList list = response.getResults();
        System.err.println(list.getNumFound());   //总页数
//        System.err.println(JSON.toJSONString(list));
    }

}
