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

        mongoDB.replaceDocument(getLawCollecion(), law);
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

    @SuppressWarnings("unchecked")
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

        String simHash1 = law.getString("simHash");
        String release_number = law.getString("release_number");
        if (release_number == null) {
            release_number = "";
        }

        String content = law.getString("content").replaceAll("\n", "");
        List<Document> articleList1 = (List<Document>) law.get("articles");
        String release_date = law.getString("release_date");
        String implement_date = law.getString("implement_date");

        for (Map.Entry<ObjectId, Document> entry : haveSimilarity.entrySet()) {
            String simHash2 = entry.getValue().getString("simHash");
            String theTitle = entry.getValue().getString("title");
            String theContent = entry.getValue().getString("content").replaceAll("\n", "");
            String the_release_number = entry.getValue().getString("release_number");
            if (the_release_number == null) {
                the_release_number = "";
            }
            List<Document> articleList2 = (List<Document>) entry.getValue().get("articles");
            String the_release_date = entry.getValue().getString("release_date");
            String the_implement_date = entry.getValue().getString("implement_date");

            if (isSimilarityContent(simHash1, simHash2)) {
                //两篇文章完全相同
                if (content.equals(theContent)) {
                    LOGGER.info("content equal:" + lawTitle + " : " + theTitle);
                    return false;
                }
                //两篇文章的法律条数不同
                if (articleList1.size() != articleList2.size()) {
                    continue;
                }
                //只有一条法条
                if (articleList1.size() == 1) {
                    Document lawArticle1 = articleList1.get(0);
                    Document lawArticle2 = articleList2.get(0);
                    //不是法律
                    if (lawArticle1.getString("name").trim().isEmpty() && lawArticle2.getString("name").trim().isEmpty()) {
                        if (lawTitle.equals(theTitle)) {
                            if (release_number.trim().equals(the_release_number.trim()) && !release_number.trim().isEmpty()) {
                                LOGGER.info("title and release_number equal:" + lawTitle + " : " + theTitle);
                                return false;
                            }
                            if (release_number.trim().isEmpty() || the_release_number.trim().isEmpty()) {
                                if (release_date.trim().equals(the_release_date.trim()) && !release_date.trim().isEmpty()) {
                                    LOGGER.info("title、release_number and the_release_date equal:" + lawTitle + " : " + theTitle);
                                    return false;
                                }
                                if (release_date.trim().isEmpty() && the_release_date.isEmpty()) {
                                    LOGGER.info("title、release_number and the_release_date empty equal:" + lawTitle + " : " + theTitle);
                                    return false;
                                }
                            }
                        }
                    }
                    continue;
                }

                if (hasEqualParagraph(articleList1, articleList2)) {
                    if (lawTitle.equals(theTitle)) {
                        LOGGER.info("title、hasEqualParagraph equal:" + lawTitle + " : " + theTitle);
                        return false;
                    }
                    if (release_number.trim().length() <= 3 || the_release_number.length() <= 3) {
                        if (release_date.equals(the_release_date) && !release_date.isEmpty() && the_implement_date.equals(implement_date) && !implement_date.isEmpty()) {
                            LOGGER.info("release_number wrong、hasEqualParagraph、date equal:" + lawTitle + " : " + theTitle);
                            return false;
                        }
                        if (release_date.isEmpty() || the_release_date.isEmpty() || implement_date.isEmpty() || the_implement_date.isEmpty()) {
                            if (lawTitle.equals(theTitle)) {
                                LOGGER.info("release_number wrong、date empty、title、hasEqualParagraph equal:" + lawTitle + " : " + theTitle);
                                return false;
                            }
                        }
                        continue;
                    }
                    if (release_number.equals(the_release_number)) {
                        LOGGER.info("release_number、hasEqualParagraph equal:" + lawTitle + " : " + theTitle);
                        return false;
                    }
                } else {
                    if (lawTitle.equals(theTitle) && release_date.equals(the_release_date) && !release_date.isEmpty() &&
                            the_implement_date.equals(implement_date) && !implement_date.isEmpty()) {
                        LOGGER.info("title、release_date、implement_date equal:" + lawTitle + " : " + theTitle);
                        return false;
                    }
                }
            }
        }
        getCleanCollection().insertOne(law);
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean isLawContentEqual(Document law1, Document law2) {
        List<Document> articleList1 = (List<Document>) law1.get("articles");
        List<Document> articleList2 = (List<Document>) law2.get("articles");
        String simHash1 = law1.getString("simHash");
        String simHash2 = law2.getString("simHash");
        if (articleList1.size() != articleList2.size()) {
            return false;
        }
        if (articleList1.size() == 1) {
            Document lawArticle1 = articleList1.get(0);
            Document lawArticle2 = articleList2.get(0);
            if (lawArticle1.getString("name").trim().isEmpty() && lawArticle2.getString("name").trim().isEmpty()) {
                if (isSimilarityContent(simHash1, simHash2)) {
                    return true;
                }
            }
        }
        if (hasEqualParagraph(articleList1, articleList2)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean hasEqualParagraph(List<Document> articleList1, List<Document> articleList2) {
        if (articleList1.size() != articleList2.size()) {
            return false;
        }
        for (int i = 0; i < articleList1.size(); i++) {
            Document lawArticle1 = articleList1.get(i);
            Document lawArticle2 = articleList2.get(i);
            List<String> paragraphList1 = (List<String>) lawArticle1.get("paragraph");
            List<String> paragraphList2 = (List<String>) lawArticle2.get("paragraph");
            if (paragraphList1.size() != paragraphList2.size()) {
                return false;
            }
            for (int j = 0; j < paragraphList1.size(); j++) {
                if (!paragraphList1.get(j).trim().equals(paragraphList2.get(j).trim())) {
                    return false;
                }
            }
        }
        return true;
    }


    public boolean isSimilarityContent(String strSimhash1, String strSimhash2) {
        BigInteger intSimhash1 = new BigInteger(strSimhash1);
        BigInteger intSimhash2 = new BigInteger(strSimhash2);
        return SimHash.hammingDistance(intSimhash1, intSimhash2) < 3;
    }

    public boolean haveAttributeURLRepeat(Document law, FindIterable<Document> iterables) {
        if (iterables.first() != null) {
            MongoCursor<Document> cursor = iterables.iterator();
            try {
                String title = law.getString("title");
                while (cursor.hasNext()) {
                    Document cleanLaw = cursor.next();
                    String title2 = cleanLaw.getString("title");
                    if (title.equals(title2)) {
                        return true;
                    } else if (isLawContentEqual(law, cleanLaw)) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean haveAttributeTitleRepeat(Document law, FindIterable<Document> iterables) {
        if (iterables.first() != null) {
            String release_number1 = law.getString("release_number");
            String release_date1 = law.getString("release_date");
            String implement_date = law.getString("implement_date");
            String simHash1 = law.getString("simHash");
            List<Document> articleList1 = (List<Document>) law.get("articles");
            MongoCursor<Document> cursor = iterables.iterator();
            try {
                while (cursor.hasNext()) {
                    Document cleanLaw = cursor.next();
                    String release_number2 = cleanLaw.getString("release_number");
                    List<Document> articleList2 = (List<Document>) cleanLaw.get("articles");
                    String release_date2 = cleanLaw.getString("release_date");
                    String implement_date2 = cleanLaw.getString("implement_date");
                    String simHash2 = cleanLaw.getString("simHash");
                    if (articleList1.size() != articleList2.size()) {
                        continue;
                    }
                    if (articleList1.size() == 1) {
                        Document lawArticle1 = articleList1.get(0);
                        Document lawArticle2 = articleList2.get(0);
                        if (lawArticle1.getString("name").trim().isEmpty() && lawArticle2.getString("name").trim().isEmpty()) {
                            if (release_date1.trim().equals(release_date2) && !release_date1.trim().isEmpty()) {
                                return true;
                            }
                            if (release_date1.trim().isEmpty() || release_date2.trim().isEmpty()) {
                                if(implement_date.equals(implement_date2) && !implement_date.isEmpty()){
                                    return true;
                                }
                            }
                            continue;
                        }
                        if(release_date1.trim().isEmpty() && implement_date.trim().isEmpty()){
                            return true;
                        }
                        if (isSimilarityContent(simHash1, simHash2)) {
                            return true;
                        }
                    } else {
                        if (hasEqualParagraph(articleList1, articleList2)) {
                            return true;
                        }
                        if(release_number1.equals(release_number2) && !release_number1.isEmpty()){
                            return true;
                        }
                        if(release_number1.isEmpty() || release_number2.isEmpty()){
                            if (release_date1.equals(release_date2) && !release_date1.isEmpty() && implement_date.equals(implement_date2) && !implement_date.isEmpty()) {
                                return true;
                            }
                            if(release_date1.isEmpty() && release_date2.isEmpty() && implement_date.isEmpty() && implement_date2.isEmpty()){
                                return true;
                            }
                        }
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
