package SolrServer;

import Log.LawLogger;
import Mongo.MongoDB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2017/12/31 17:56
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class ImportDataToSolr {

    private static Logger LOGGER = LawLogger.getLawLogger(ImportDataToSolr.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private MongoCollection<Document> collection;
    private SolrServer solrServer;
    private List<SolrInputDocument> cacheList;
    private static final int CACHESIZE = 2500;

    public MongoCollection<Document> getCollection() {
        return this.collection;
    }

    public SolrServer getSolrServer() {
        return this.solrServer;
    }

    public void doImport(){
        String baseURL = "http://localhost:8080/solr/law";
        SolrServer solrServer = new SolrServer(baseURL);
        HttpSolrClient solrClient = solrServer.getHttpSolrClient();

        LOGGER.info("Begin do import data to solr...");
        FindIterable<Document> iterables = getCollection().find(new Document("url", "")).noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                try {
//                    doDocument(url, category);
                    num++;
                } catch (Exception e) {
                    LOGGER.error("Do document error: " + e.getMessage());
                }
                LOGGER.info("doClean clean num: " + num);
            }
        } catch (Exception e) {
            LOGGER.error("do clean find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
    }

    public ImportDataToSolr(String solrServerURL, String MongodbCollection){
        this.solrServer = new SolrServer(solrServerURL);
        this.collection = mongoDB.getCollection(MongodbCollection);
        this.cacheList = new ArrayList<>();
    }

    public void addDocumentToCache(Document law){
        SolrInputDocument doc = new SolrInputDocument();

        doc.setField("id", law.getObjectId("_id").toString());

        String title = getAttributeOfDocument(law, "title");
        doc.setField("title", title);

        String department = getAttributeOfDocument(law, "department");
        doc.setField("department", department);

        String release_date = getAttributeOfDocument(law, "release_date");
        doc.setField("release_date", release_date);

        String release_number = getAttributeOfDocument(law, "release_number");
        doc.setField("release_number", release_number);

        String implement_date = getAttributeOfDocument(law, "implement_date");
        doc.setField("implement_date", implement_date);

        String category = getAttributeOfDocument(law, "category");
        doc.setField("category", category);

        String level = getAttributeOfDocument(law, "level");
        doc.setField("content", level);

        String timeless = getAttributeOfDocument(law, "timeless");
        doc.setField("timeless", timeless);

        String content = getAttributeOfDocument(law, "content");
        doc.setField("content", content);

        String url = getAttributeOfDocument(law, "url");
        doc.setField("url", url);
        try {
            solrServer.getHttpSolrClient().add(doc);
            solrServer.getHttpSolrClient().commit();
        }catch (Exception e){
            LOGGER.error("error: " + e.getMessage());
        }


    }

    public String getAttributeOfDocument(Document law, String name){
        String attribute = law.getString(name);
        if(attribute == null){
            attribute = "";
        }
        return attribute;
    }
}
