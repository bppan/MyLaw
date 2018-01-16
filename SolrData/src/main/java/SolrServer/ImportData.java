package SolrServer;

import Log.MyLogger;
import Mongo.MongoDB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/2 16:29
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class ImportData {
    private static final int CACHESIZE = 5000;
    private static Logger LOGGER = MyLogger.getMyLogger(ImportData.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private MongoCollection<Document> collection;
    private SolrServer solrServer;
    private List<SolrInputDocument> cacheList;

    public ImportData(String solrServerURL, String MongodbCollection) {
        this.solrServer = new SolrServer(solrServerURL);
        this.collection = mongoDB.getCollection(MongodbCollection);
        this.cacheList = new ArrayList<>();
    }

    public MongoCollection<Document> getCollection() {
        return this.collection;
    }

    public SolrServer getSolrServer() {
        return this.solrServer;
    }

    public void doImport() {
        LOGGER.info("Begin do import data to solr...");
        FindIterable<Document> iterables = getCollection().find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                addDocumentToCache(law);
                if (this.cacheList.size() >= CACHESIZE) {
                    commitDataToSolr();
                    this.cacheList.clear();
                    Thread.sleep(1500);
                }
                num++;
                LOGGER.info("Import data num: " + num);
            }
            if (this.cacheList.size() != 0) {
                commitDataToSolr();
                this.cacheList.clear();
            }
            this.solrServer.getHttpSolrClient().close();
        } catch (Exception e) {
            LOGGER.error("Do import data to solr server error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        LOGGER.info("Done do import data to solr...");
    }

    public void commitDataToSolr() {
        for (SolrInputDocument doc : this.cacheList) {
            try {
                this.solrServer.getHttpSolrClient().add(doc);
            } catch (Exception e) {
                LOGGER.error("Add data to solr server error: " + e.getMessage());
            }
        }
        try {
            this.solrServer.getHttpSolrClient().commit();
        } catch (Exception e) {
            LOGGER.error("Commit data to solr server error: " + e.getMessage());
        }
    }

    public void addDocumentToCache(Document law) {
        SolrInputDocument doc = new SolrInputDocument();

        doc.addField("id", law.getObjectId("_id").toString());

        String title = getAttributeOfDocument(law, "title");
        doc.addField("title", title);

        String department = getAttributeOfDocument(law, "department");
        doc.addField("department", department);

        String release_date = getAttributeOfDocument(law, "release_date");
        doc.addField("release_date", release_date);

        String release_number = getAttributeOfDocument(law, "release_number");
        doc.addField("release_number", release_number);

        String implement_date = getAttributeOfDocument(law, "implement_date");
        doc.addField("implement_date", implement_date);

        String category = getAttributeOfDocument(law, "category");
        doc.addField("category", category);

        String level = getAttributeOfDocument(law, "level");
        doc.addField("level", level);

        String timeless = getAttributeOfDocument(law, "timeless");
        doc.addField("timeless", timeless);

        String content = getAttributeOfDocument(law, "content");
        doc.addField("content", content);

        String url = getAttributeOfDocument(law, "url");
        doc.addField("url", url);

        this.cacheList.add(doc);
    }

    public String getAttributeOfDocument(Document law, String name) {
        String attribute = law.getString(name);
        if (attribute == null) {
            attribute = "";
        }
        return attribute.trim();
    }
}
