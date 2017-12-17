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

    public LawClean(String crawJobCollection, String lawCollection, String cleanCollection) {
        this.crawJobcollection = mongoDB.getCollection(crawJobCollection);
        this.lawCollecion = mongoDB.getCollection(lawCollection);
        this.cleanCollection = mongoDB.getCollection(cleanCollection);
    }

    public MongoCollection<Document> getCrawJobcollection() {
        return crawJobcollection;
    }

    public MongoCollection<Document> getLawCollecion() {
        return lawCollecion;
    }

    public MongoCollection<Document> getCleanCollection() {
        return cleanCollection;
    }

    public void doClean() {
        LOGGER.info("Begin do clean...");
        FindIterable<Document> iterables = getCrawJobcollection().find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document crawjob = cursor.next();
                String url = crawjob.getString("url");
                LOGGER.info("do clean url: " + url);
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
        } catch (Exception e) {
            LOGGER.error("do clean find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
    }

    public void doDocument(String url, String category) {
        FindIterable<Document> iterables = getLawCollecion().find(new Document("url", url)).noCursorTimeout(true).batchSize(10000);
        if (iterables.first() == null) {
            LOGGER.warn("No exits in law url:" + url);
        } else {
            MongoCursor<Document> cursor = iterables.iterator();
            try {
                while (cursor.hasNext()) {
                    Document law = cursor.next();
                    law.put("category", category);
                    cleanContent(law);
                }
            } finally {
                cursor.close();
            }
        }
    }

    public void doCleanRepeat() {
        FindIterable<Document> iterables = getLawCollecion().find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        try {
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
        } finally {
            cursor.close();
        }
    }

    public void cleanContent(Document law) {
        String html = law.getString("rawHtml");
        String content = getContentHtmlBySelect(html);
        String cleanHtml = LawSpider.cleanHtml(content);
        String updateContent = getCleanContent(cleanHtml);

        List<LawArticle> articleList = LawSpider.getLawArticleAndParagraph(updateContent);
        List<Document> interlDocuments = LawDocument.getArticleDocument(articleList);

        law.put("article_num", interlDocuments.size());
        law.put("content", updateContent);
        law.put("articles", interlDocuments);

        updateDocumentContent(law);
    }

    public abstract String getContentHtmlBySelect(String html);

    public String getCleanContent(String cleanHtml) {
        String[] contentList = cleanHtml.split("\n");
        StringBuilder updateContent = new StringBuilder();
        for (String contentpar : contentList) {
            if (contentpar.isEmpty()) {
                continue;
            }
            updateContent.append(contentpar.trim()).append("\n");
        }
        return updateContent.toString();
    }

    public void updateDocumentContent(Document law) {
        org.bson.types.ObjectId id = law.getObjectId("_id");
        Document filter = new Document();
        filter.append("_id", id);
        law.remove("_id");
        getLawCollecion().replaceOne(filter, law);
    }

    public void saveToCleanCollection(Document law) {
        String url = law.getString("url");
        FindIterable<Document> iterables = getCleanCollection().find(new Document("url", url)).noCursorTimeout(true).batchSize(10000);
        deleteAttributeURLRepeat(law, iterables);

        String lawTitle = law.getString("title");
        FindIterable<Document> iterableTitle = getCleanCollection().find(new Document("title", lawTitle)).noCursorTimeout(true).batchSize(10000);
        deleteAttributeTitleRepeat(law, iterableTitle);

        String lawContent = law.getString("content");
        FindIterable<Document> iterableContent = getCleanCollection().find(new Document("content", lawContent)).noCursorTimeout(true).batchSize(10000);
        deleteAttributeContentRepeat(law, iterableContent);

        getCleanCollection().insertOne(law);
    }

    public void deleteAttributeURLRepeat(Document law, FindIterable<Document> iterables) {
        if (iterables.first() != null) {
            MongoCursor<Document> cursor = iterables.iterator();
            try {
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
            } finally {
                cursor.close();
            }
        }
    }

    public void deleteAttributeTitleRepeat(Document law, FindIterable<Document> iterables) {
        if (iterables.first() != null) {
            MongoCursor<Document> cursor = iterables.iterator();
            try {
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
            } finally {
                cursor.close();
            }
        }
    }

    public void deleteAttributeContentRepeat(Document law, FindIterable<Document> iterables) {
        if (iterables.first() != null) {
            MongoCursor<Document> cursor = iterables.iterator();
            try {
                List<Document> needDelete = new ArrayList<Document>();
                while (cursor.hasNext()) {
                    Document cleanLaw = cursor.next();
                    needDelete.add(cleanLaw);
                }
                deleteDocumentById(needDelete);
                needDelete.clear();
            } finally {
                cursor.close();
            }
        }
    }

    private void deleteDocumentById(List<Document> needDelete) {
        for (Document deleteLaw : needDelete) {
            Document filter = new Document();
            filter.append("_id", deleteLaw.getObjectId("_id"));
            getCleanCollection().deleteOne(filter);
        }
    }
}
