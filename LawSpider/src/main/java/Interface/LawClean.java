package Interface;

import Log.LawLogger;
import Mongo.LawArticle;
import Mongo.LawDocument;
import Mongo.MongoDB;
import SimHash.SimHash;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public LawClean(String lawCollection, String cleanCollection) {
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
        } catch (Exception e) {
            LOGGER.error("do clean find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        doCleanRepeat();
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
                num++;
                try {
                    if (saveToCleanCollection(law)) {
                        LOGGER.info("save success law :" + law.getObjectId("_id"));
                    } else {
                        LOGGER.info("exists law :" + law.getObjectId("_id"));
                    }
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
        SimHash simHash = new SimHash(updateContent);
        law.append("simHash", simHash.getIntSimHash().toString());
        law.append("simHashPart1", simHash.getStrSimHash().substring(0, 16));
        law.append("simHashPart2", simHash.getStrSimHash().substring(16, 32));
        law.append("simHashPart3", simHash.getStrSimHash().substring(32, 48));
        law.append("simHashPart4", simHash.getStrSimHash().substring(48, 64));

        updateDocumentContent(law);
    }

    public abstract String getContentHtmlBySelect(String html);

    public String getCleanContent(String cleanHtml) {
        String[] contentList = cleanHtml.split("\n");
        StringBuilder updateContent = new StringBuilder();
        for (String contentpar : contentList) {
            if (contentpar.trim().isEmpty()) {
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

    public boolean saveToCleanCollection(Document law) {
        String url = law.getString("url");
        FindIterable<Document> iterables = getCleanCollection().find(new Document("url", url)).noCursorTimeout(true).batchSize(10000);
        if (haveAttributeURLRepeat(law, iterables)) {
            LOGGER.info("Url repeat :" + url);
            return false;
        }

        String lawTitle = law.getString("title");
        FindIterable<Document> iterableTitle = getCleanCollection().find(new Document("title", lawTitle)).noCursorTimeout(true).batchSize(10000);
        if (haveAttributeTitleRepeat(law, iterableTitle)) {
            LOGGER.info("lawTitle repeat :" + lawTitle);
            return false;
        }

        String simHashPart1 = law.getString("simHashPart1");
        String simHashPart2 = law.getString("simHashPart2");
        String simHashPart3 = law.getString("simHashPart3");
        String simHashPart4 = law.getString("simHashPart4");

        Map<ObjectId, Document> haveSimilarity = new HashMap<ObjectId, Document>();
        FindIterable<Document> iterableContent1 = getCleanCollection().find(new Document("simHashPart1", simHashPart1)).noCursorTimeout(true).batchSize(10000);
        haveAttributeContentRepeat(haveSimilarity, iterableContent1);

        FindIterable<Document> iterableContent2 = getCleanCollection().find(new Document("simHashPart2", simHashPart2)).noCursorTimeout(true).batchSize(10000);
        haveAttributeContentRepeat(haveSimilarity, iterableContent2);

        FindIterable<Document> iterableContent3 = getCleanCollection().find(new Document("simHashPart3", simHashPart3)).noCursorTimeout(true).batchSize(10000);
        haveAttributeContentRepeat(haveSimilarity, iterableContent3);

        FindIterable<Document> iterableContent4 = getCleanCollection().find(new Document("simHashPart4", simHashPart4)).noCursorTimeout(true).batchSize(10000);
        haveAttributeContentRepeat(haveSimilarity, iterableContent4);

        String simhash1 = law.getString("simHash");
        String release_number = law.getString("release_number");
        if (release_number == null) {
            release_number = "";
        }
        String releaseDate = getFormateStringDate(law.getString("release_date"));
        String implementDate = getFormateStringDate(law.getString("implement_date"));

        String content = law.getString("content").replaceAll("\n", "");
        for (Map.Entry<ObjectId, Document> entry : haveSimilarity.entrySet()) {
            String simhash2 = entry.getValue().getString("simHash");
            String theTitle = entry.getValue().getString("title");
            String the_release_number = entry.getValue().getString("release_number");
            String theContent = entry.getValue().getString("content").replaceAll("\n", "");
            String the_releaseDate = getFormateStringDate(entry.getValue().getString("release_date"));
            String the_implementDate = getFormateStringDate(entry.getValue().getString("implement_date"));
            if (the_release_number == null) {
                the_release_number = "";
            }
            if (content.equals(theContent)) {
                LOGGER.info("simHash similarity content equal:" + simhash1 + " : " + simhash2);
                return false;
            }
            if (isSimilarityContent(simhash1, simhash2)) {
                if (release_number.equals(the_release_number) && !release_number.isEmpty()) {
                    LOGGER.info("simHash similarity release_number equal:" + release_number + " : " + the_release_number);
                    return false;
                }
                if (release_number.isEmpty() || the_release_number.isEmpty()) {
                    if (releaseDate.equals(the_releaseDate) && !releaseDate.isEmpty() &&
                            implementDate.equals(the_implementDate) && !implementDate.isEmpty() && lawTitle.equals(theTitle)) {
                        LOGGER.info("simHash similarity releaseDate、the_implementDate and title equal:" + lawTitle + " : " + theTitle);
                        return false;
                    }
                }
            }
        }

        law.put("release_date", releaseDate);
        law.put("implement_date", implementDate);
        getCleanCollection().insertOne(law);
        return true;
    }

    public boolean isSimilarityContent(String strSimhash1, String strSimhash2) {
        BigInteger intSimhash1 = new BigInteger(strSimhash1);
        BigInteger intSimhash2 = new BigInteger(strSimhash2);
        return SimHash.hammingDistance(intSimhash1, intSimhash2) <= 3;
    }

    public boolean haveAttributeURLRepeat(Document law, FindIterable<Document> iterables) {
        if (iterables.first() != null) {
            MongoCursor<Document> cursor = iterables.iterator();
            try {
                String content = law.getString("content").replaceAll("\n", "");
                String title = law.getString("title");
                while (cursor.hasNext()) {
                    Document cleanLaw = cursor.next();
                    String title2 = cleanLaw.getString("title");
                    String theContent = cleanLaw.getString("content").replaceAll("\n", "");
                    if (title.equals(title2)) {
                        return true;
                    } else if (content.equals(theContent)) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }

    public boolean haveAttributeTitleRepeat(Document law, FindIterable<Document> iterables) {
        if (iterables.first() != null) {
            MongoCursor<Document> cursor = iterables.iterator();
            try {
                String content = law.getString("content").replaceAll("\n", "");
                while (cursor.hasNext()) {
                    Document cleanLaw = cursor.next();
                    String theContent = cleanLaw.getString("content").replaceAll("\n", "");
                    if (content.equals(theContent)) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }

    public void haveAttributeContentRepeat(Map<ObjectId, Document> havesimilarity, FindIterable<Document> iterables) {
        if (iterables.first() != null) {
            MongoCursor<Document> cursor = iterables.iterator();
            try {
                while (cursor.hasNext()) {
                    Document cleanLaw = cursor.next();
                    havesimilarity.put(cleanLaw.getObjectId("_id"), cleanLaw);
                }
            } finally {
                cursor.close();
            }
        }
    }

    public void deleteDocumentMonyById(MongoCollection<Document> collection, List<Document> needDelete) {
        for (Document deleteLaw : needDelete) {
            Document filter = new Document();
            filter.append("_id", deleteLaw.getObjectId("_id"));
            collection.deleteOne(filter);
        }
    }

    public void deleteDocumentOneById(MongoCollection<Document> collection, Document needDelete) {
        Document filter = new Document();
        filter.append("_id", needDelete.getObjectId("_id"));
        collection.deleteOne(filter);
    }

    public String getFormateStringDate(String time) {
        String formateDateString = "";
        if (time != null && !time.isEmpty()) {
            Date date = stringToDate(time);
            if (date != null) {
                formateDateString = dateToString(date);
            }
        }
        return formateDateString;
    }

    /**
     * 字符串转换为java.util.Date<br>
     *
     * @param time String 字符串<br>
     * @return Date 日期<br>
     */
    public Date stringToDate(String time) {
        List<SimpleDateFormat> formatList = new ArrayList<>();
        formatList.add(new SimpleDateFormat("yyyy-MM-dd"));
        formatList.add(new SimpleDateFormat("yyyy-M-d"));
        formatList.add(new SimpleDateFormat("yyyy年M月d日"));
        formatList.add(new SimpleDateFormat("yyyy年MM月dd日"));
        formatList.add(new SimpleDateFormat("yyyy.MM.dd"));
        formatList.add(new SimpleDateFormat("yyyy.M.d"));
        Date date = null;
        for (SimpleDateFormat format : formatList) {
            try {
                date = format.parse(time);
                break;
            } catch (ParseException e) {
                continue;
            }
        }
        return date;
    }

    public String dateToString(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.M.d");
        return formatter.format(time);
    }
}
