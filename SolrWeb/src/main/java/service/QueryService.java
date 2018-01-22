package service;

import dao.SolrServer;
import log.MyLogger;
import model.Document;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/9 15:23
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class QueryService {
    private static Logger LOGGER = MyLogger.getMyLogger(QueryService.class);
    private SolrServer solrServer;
    private long numFound;
    private int QTime;
    private List<Document> documentList;

    public QueryService() {
        this.solrServer = SolrServer.getSolrServer();
    }

    public long getNumFound() {
        return numFound;
    }

    public int getQTime() {
        return QTime;
    }

    public List<Document> getQueryResult(String queryString, int start, int rows) {
        this.documentList = new ArrayList<>();
        SolrQuery solrQuery = new SolrQuery();
        String weightQueryString = generateWeightQueryString(queryString);
        solrQuery.setQuery(weightQueryString);
        solrQuery.setHighlight(true);
        solrQuery.addHighlightField("content");
        solrQuery.addHighlightField("title");
        solrQuery.addHighlightField("department");
        solrQuery.addHighlightField("category");
        solrQuery.addHighlightField("level");
        solrQuery.addHighlightField("release_number");
        solrQuery.addHighlightField("timeless");
        solrQuery.setHighlightSimplePre("<font color='#dd4b39'>");
        solrQuery.setHighlightSimplePost("</font>");
        solrQuery.setHighlightSnippets(1);//结果分片数，默认为1
        solrQuery.setHighlightFragsize(200);//每个分片的最大长度，默认为100
        solrQuery.setStart(start);
        solrQuery.setRows(rows);
        solrQuery.set("wt", "json");
        QueryResponse rsp = null;
        try {
            rsp = this.solrServer.getSolrClient().query(solrQuery);
        } catch (Exception e) {
            LOGGER.error("Query to solr err:" + e.toString());
            e.printStackTrace();
            return this.documentList;
        }
        //设置head
        SolrDocumentList docs = rsp.getResults();
        this.numFound = docs.getNumFound();
        this.QTime = rsp.getQTime();

        Map<String, Map<String, List<String>>> highlightResult = rsp.getHighlighting();
        for (SolrDocument doc : docs) {
            Document document = new Document(doc);
            document.setHighLight(highlightResult);
            this.documentList.add(document);
        }
        return this.documentList;
    }
    public String generateWeightQueryString(String queryString){
        StringBuilder weightQueryString = new StringBuilder("");
        weightQueryString.append("(title:").append(queryString).append(")^3");
        weightQueryString.append(" OR ");
        weightQueryString.append("(content:").append(queryString).append(")");
        return weightQueryString.toString();
    }
}
