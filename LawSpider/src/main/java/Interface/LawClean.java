package Interface;

import Log.LawLogger;
import Mongo.LawArticle;
import Mongo.LawDocument;
import Mongo.MongoDB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2017/12/15 16:20
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public abstract class LawClean {
    private static Logger LOGGER = LawLogger.getLawLogger(LawClean.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private MongoCollection<Document> crawJobcollection;
    private MongoCollection<Document> lawCollecion;
    private MongoCollection<Document> cleanCollection;

    public MongoCollection<Document> getCrawJobcollection() {
        return crawJobcollection;
    }

    public MongoCollection<Document> getLawCollecion() {
        return lawCollecion;
    }

    public MongoCollection<Document> getCleanCollection() {
        return cleanCollection;
    }

    public LawClean(String crawJobCollection, String lawCollection, String cleanCollection) {
        this.crawJobcollection = mongoDB.getCollection(crawJobCollection);
        this.lawCollecion = mongoDB.getCollection(lawCollection);
        this.cleanCollection = mongoDB.getCollection(cleanCollection);
    }

    public void doClean() {
        FindIterable<Document> iterables = this.crawJobcollection.find();
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        while (cursor.hasNext()) {
            Document crawjob = cursor.next();
            String url = crawjob.getString("url");
            String category = crawjob.getString("title");
            try {
                doDocument(url, category);
                num++;
            } catch (Exception e) {
                LOGGER.error("Do document error: " + e.getMessage());
            }
            LOGGER.info("doClean clean num: " + num);
        }
        doCleanRepeat();
    }

    public void doCleanRepeat() {
        FindIterable<Document> iterables = this.lawCollecion.find();
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        while (cursor.hasNext()) {
            Document law = cursor.next();
            try {
                saveToCleanCollection(law);
                num++;
            } catch (Exception e) {
                LOGGER.error("Do document error: " + e.getMessage());
            }
            LOGGER.info("doCleanRepeat clean num: " + num);
        }
    }

    public void doDocument(String url, String category) {
        FindIterable<Document> iterables = this.lawCollecion.find(new Document("url", url));
        if (iterables.first() == null) {
            LOGGER.warn("No exits in law url:" + url);
            return;
        }else {
            MongoCursor<Document> cursor = iterables.iterator();
            while (cursor.hasNext()) {
                Document law = cursor.next();
                cleanContent(law, category);
            }
        }
    }
    public void cleanContent(Document law, String category){
        String html = law.getString("rawHtml");
        String content = getContentHtmlByselect(html);
        String cleanHtml = LawSpider.cleanHtml(content);
        List<LawArticle> articleList = LawSpider.getLawArticleAndParagraph(cleanHtml);
        List<Document> interlDocuments = LawDocument.getArticleDocument(articleList);

        String updateContent = getCleanContent(cleanHtml);
        updateDocumentContent(category, updateContent, interlDocuments, law);
    }

    public abstract String getContentHtmlByselect(String html);

    public abstract String getCleanContent(String cleanHtml);

    public abstract void updateDocumentContent(String category, String content, List<Document> interlDocuments, Document law);

    public void saveToCleanCollection(Document law) {
        String url = law.getString("url");
        FindIterable<Document> iterables = this.cleanCollection.find(new Document("url", url));
        deleteAttributeURLRepeat(law, iterables);

        String lawtitle = law.getString("title");
        FindIterable<Document> iterableTitle = this.cleanCollection.find(new Document("title", lawtitle));
        deleteAttributeTitleRepeat(law, iterableTitle);

        String lawContent = law.getString("content");
        FindIterable<Document> iterableContent = this.cleanCollection.find(new Document("content", lawContent));
        deleteAttributeContentRepeat(law, iterableContent);

        this.cleanCollection.insertOne(law);
    }

    public void deleteAttributeURLRepeat(Document law, FindIterable<Document> iterables) {
        if (iterables.first() == null) {
            return;
        } else {
            MongoCursor<Document> cursor = iterables.iterator();
            List<Document> needDelete = new ArrayList<Document>();
            while (cursor.hasNext()) {
                Document cleanLaw = cursor.next();
                String title = law.getString("title");
                String title2 = cleanLaw.getString("title");
                String content = law.getString("content");
                String content2 = cleanLaw.getString("content");
                if (title.equals(title2)) {
                    needDelete.add(cleanLaw);
                } else if (content.equals(content2)) {
                    needDelete.add(cleanLaw);
                }
            }
            deleteDocumentById(needDelete);
            needDelete.clear();
        }
    }

    public void deleteAttributeTitleRepeat(Document law, FindIterable<Document> iterables) {
        if (iterables.first() == null) {
            return;
        } else {
            MongoCursor<Document> cursor = iterables.iterator();
            List<Document> needDelete = new ArrayList<Document>();
            while (cursor.hasNext()) {
                Document cleanLaw = cursor.next();
                String content = law.getString("content");
                String content2 = cleanLaw.getString("content");
                if (content.equals(content2)) {
                    needDelete.add(cleanLaw);
                }
            }
            deleteDocumentById(needDelete);
            needDelete.clear();
        }
    }

    public void deleteAttributeContentRepeat(Document law, FindIterable<Document> iterables) {
        if (iterables.first() == null) {
            return;
        } else {
            MongoCursor<Document> cursor = iterables.iterator();
            List<Document> needDelete = new ArrayList<Document>();
            while (cursor.hasNext()) {
                Document cleanLaw = cursor.next();
                needDelete.add(cleanLaw);
            }
            deleteDocumentById(needDelete);
            needDelete.clear();
        }
    }

    private void deleteDocumentById(List<Document> needDelete) {
        for (Document deleteLaw : needDelete) {
            Document filter = new Document();
            filter.append("_id", deleteLaw.getObjectId("_id"));
            this.cleanCollection.deleteOne(filter);
        }
    }
}
