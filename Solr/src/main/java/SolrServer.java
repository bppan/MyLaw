import com.alibaba.fastjson.JSON;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.Collection;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/25 20:44
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class SolrServer {
    private static String baseURL = "http://localhost:8080/solr/law";
    private static HttpSolrClient solrServer = new HttpSolrClient(baseURL);

    public static HttpSolrClient getHttpSolrClient() {
        return new HttpSolrClient(baseURL);
    }

    public static void main(String[] args) {
        SolrServer solrServer = new SolrServer();
        try {
            solrServer.get("");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void get(String name) throws IOException, SolrServerException {
        HttpSolrClient solrServer = getHttpSolrClient();
        solrServer.setSoTimeout(5000);
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*"); //查询所有
//        query.setQuery(name+"*");
        query.setRows(1000);
        query.setStart(0);
        QueryResponse response = solrServer.query(query, SolrRequest.METHOD.POST);
        SolrDocumentList list = response.getResults();
        System.err.println(list.getNumFound());   //总页数
        System.err.println(JSON.toJSONString(list));
    }

    public void getOne() throws IOException, SolrServerException {
        solrServer.setSoTimeout(5000);
        SolrQuery prams = new SolrQuery();
        prams.set("q", "*:*");

        QueryResponse query = solrServer.query(prams, SolrRequest.METHOD.POST);
        SolrDocumentList list = query.getResults();
        System.err.println(JSON.toJSONString(list));
        for (SolrDocument document : list) {
            Collection<String> fieldNames = document.getFieldNames();
            for (String field : fieldNames) {
                System.err.println(document.get(field));
            }
        }
    }

    public void add(String id, String name) throws IOException, SolrServerException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", id);
        doc.setField("title", name);
        doc.setField("description", "这是我的测试!!!");
        solrServer.add(doc);
        solrServer.commit();
    }

    public void update(String id, String name) throws IOException, SolrServerException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", id);
        doc.setField("name", name);
        doc.setField("description", "这是我的测试!!!");
        solrServer.add(doc);
        solrServer.commit();
    }

    public void delete(String id) throws IOException, SolrServerException {
        solrServer.deleteById(id);
        solrServer.commit();
    }

    public void deleteAll() throws IOException, SolrServerException {
        solrServer.deleteByQuery("*:*");
        solrServer.commit();
    }
}

