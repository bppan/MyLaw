package ChinacourtSpider;

import Interface.LawClean;
import Log.LawLogger;
import Mongo.MongoDB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/11 17:07
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Clean extends LawClean {

    private static Logger LOGGER = LawLogger.getLawLogger(Clean.class);

    public Clean(String crawJobCollection, String lawCollection, String cleanCollection) {
        super(crawJobCollection, lawCollection, cleanCollection);
    }

    public static boolean hasTitle(org.jsoup.nodes.Document doc) {
        try {
            if (doc.select("body > div.container > div > div.law_content > div > p:nth-child(1) > font").first().childNodes().size() == 0) {
                return false;
            } else {
                return true;
            }
        } catch (NullPointerException e) {
            return false;
        }
    }

    public String getContentHtmlBySelect(String html) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("body > div.container > div > div.law_content").first().html().replaceAll("&nbsp;", "");
        } catch (Exception e) {
            return "";
        }
    }

    public void cleanContent(Document law) {
        String category = law.getString("category");
        law.put("level", "法律法规");
        law.put("timeless", "现行有效");
        if (category.equals("司法解释")) {
            law.put("level", "司法解释");
        }

        String title = law.getString("title");
        String content = law.getString("content");
        if (title == null || content.equals(title)) {
            LOGGER.info("find one url:" + law.getString("url"));
            resetTitle(law, content);
        }

        super.cleanContent(law);
    }

    public void CleanTitle(String collection) {
        MongoDB mongoDB = MongoDB.getMongoDB();
        MongoCollection<Document> cleanLawColletion = mongoDB.getCollection(collection);
        FindIterable<Document> iterables = cleanLawColletion.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                num++;
                String rawHtml = law.getString("rawHtml");
                LOGGER.info("current Title: " + law.getString("title"));
                String title = law.getString("title");
                org.jsoup.nodes.Document doc = Jsoup.parse(rawHtml);
                if (hasTitle(doc)) {
                    title = doc.select("body > div.container > div > div.law_content > div > p:nth-child(1) > font").first().toString();
                    LOGGER.info("find one title is wrong:" + law.getString("url"));
                } else {
                    LOGGER.info("title is right:" + law.getString("url"));
                }
                String regEx2_html = "<[^>]+>"; // 定义HTML标签的正则表达式
                Pattern p2_html = Pattern.compile(regEx2_html, Pattern.CASE_INSENSITIVE);
                Matcher m2_html = p2_html.matcher(title);
                title = m2_html.replaceAll("").replaceAll(" ", "").trim(); // 过滤html标签
                law.put("title", title);
                updateDocumentContent(law);
                LOGGER.info("current num: " + num);
            }
        } catch (Exception e) {
            LOGGER.error("do clean title error: " + e.getMessage());
        } finally {
            cursor.close();
        }
    }

    public void resetTitle(Document law, String content) {
        String[] contentlist = content.split("\n");
        boolean find = false;
        for (String par : contentlist) {
            if (!find && par.contains("【文件来源】")) {
                find = true;
                continue;
            }
            if (find && !par.trim().isEmpty()) {
                law.put("title", par);
                break;
            }
        }
    }

    public void CleanTitleAgain(String collection) {
        MongoDB mongoDB = MongoDB.getMongoDB();
        MongoCollection<Document> cleanLawColletion = mongoDB.getCollection(collection);
        FindIterable<Document> iterables = cleanLawColletion.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                num++;
                String title = law.getString("title");
                String content = law.getString("content");
                if (title == null || title.length() > 300) {
                    LOGGER.info("find one url:" + law.getString("url"));
                    resetTitle(law, content);
                }
                updateDocumentContent(law);
                LOGGER.info("current num: " + num);
            }
        } catch (Exception e) {
            LOGGER.error("do clean title error: " + e.getMessage());
        } finally {
            cursor.close();
        }
    }
}
