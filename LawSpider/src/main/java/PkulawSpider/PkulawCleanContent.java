package PkulawSpider;

import Log.LawLogger;
import Mongo.MongoDB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/1/29 15:00
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class PkulawCleanContent {
    private static Logger LOGGER = LawLogger.getLawLogger(PkulawCleanContent.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private MongoCollection<Document> lawCollecion;
    private MongoCollection<Document> cleanCollection;

    public PkulawCleanContent(String lawCollection, String cleanCollection) {
        this.lawCollecion = mongoDB.getCollection(lawCollection);
        this.cleanCollection = mongoDB.getCollection(cleanCollection);
    }

    public static void main(String[] args) {
        PkulawCleanContent pkulawCleanContent = new PkulawCleanContent("pkulaw_clean", "law3");
        pkulawCleanContent.cleanTitle();
    }

    public void cleanContent() {
        LOGGER.info("Begin do pkulawCleanContent...");
        FindIterable<Document> iterables = this.lawCollecion.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                String url = law.getString("url");
                FindIterable<Document> iterablesclean = this.cleanCollection.find(new Document("url", url)).noCursorTimeout(true).limit(1);
                if (iterablesclean.first() != null) {
                    LOGGER.info("clean url: " + url);
                    Document cleanlaw = iterablesclean.first();
                    String title = cleanlaw.getString("title").replaceAll("&#xfffd;D", "-").trim();
                    cleanlaw.put("title", title);
                    mongoDB.updateDocument(this.cleanCollection, cleanlaw);
                }
                num++;
                LOGGER.info("pkulawCleanContent clean num: " + num);
            }
        } catch (Exception e) {
            LOGGER.error("pkulawCleanContent find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        LOGGER.info("Done do pkulawCleanContent...");
    }
    public void addContentHtml() {
        LOGGER.info("Begin do addContentHtml...");
        FindIterable<Document> iterables = this.lawCollecion.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                String url = law.getString("url");
                FindIterable<Document> iterablesclean = this.cleanCollection.find(new Document("url", url)).noCursorTimeout(true).limit(1);
                if (iterablesclean.first() != null) {
                    LOGGER.info("addContentHtml url: " + url);
                    Document cleanlaw = iterablesclean.first();
                    String html = cleanlaw.getString("rawHtml");
                    org.jsoup.nodes.Document lawHtmlDocument = Jsoup.parse(html);
                    lawHtmlDocument.select("#div_content > font").first().remove();
                    lawHtmlDocument.select(".TiaoYinV2").remove();
                    lawHtmlDocument.select(".TiaoYin").remove();
                    Elements contentHtml = lawHtmlDocument.select("#div_content");
                    contentHtml.select("title").remove();
                    contentHtml.select("script").remove();
                    contentHtml.select("meta").remove();
                    contentHtml.select("link").remove();
                    contentHtml.select("*").removeAttr("style");
                    contentHtml.select("*").removeAttr("color");
                    contentHtml.select("font").removeAttr("face");
                    contentHtml.select("font").removeAttr("size");
                    Elements esd = contentHtml.select("a");
                    Iterator<Element> iterator = esd.iterator();
                    while (iterator.hasNext())
                    {
                        Element etemp = iterator.next();
                        String styleStr = etemp.attr("class");
                        if(styleStr.equals("alink")){
                            if(!etemp.hasAttr("onmouseover")){
                                etemp.remove();
                                continue;
                            }
                        }
                        if(etemp.hasAttr("name")){
                            etemp.removeAttr("name");
                        }
                        etemp.removeAttr("title");
                        etemp.removeAttr("target");
                        etemp.removeAttr("onmouseover");
                    }
                    contentHtml.select("*").removeAttr("class");
                    String resultHtml = contentHtml.html();
//                    System.out.println(resultHtml);
                    String regEx2_html = "<a [^>]*href[^>]*>"; // 定义HTML标签的正则表达式
                    Pattern p2_html = Pattern.compile(regEx2_html, Pattern.CASE_INSENSITIVE);
                    Matcher m2_html = p2_html.matcher(resultHtml);
                    resultHtml = m2_html.replaceAll("").replaceAll("</a>", "").replaceAll("<a>", ""); // 过滤html标签
                    resultHtml = resultHtml.replaceAll("<br /> 　　　　","");
                    resultHtml = resultHtml.replaceAll("<br />　　　　","");
                    resultHtml = resultHtml.replaceAll("<br> 　　　　","");
                    cleanlaw.append("contentHtml", resultHtml);
//                    System.out.println(resultHtml);
                    mongoDB.updateDocument(this.cleanCollection, cleanlaw);
                }
                num++;
                LOGGER.info("addContentHtml num: " + num);
            }
        } catch (Exception e) {
            LOGGER.error("addContentHtml find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        LOGGER.info("Done do addContentHtml...");
    }
    public void cleanTitle() {
        LOGGER.info("Begin do cleanTitle...");
        FindIterable<Document> iterables = this.lawCollecion.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                String url = law.getString("url");
                FindIterable<Document> iterablesclean = this.cleanCollection.find(new Document("url", url)).noCursorTimeout(true).limit(1);
                if (iterablesclean.first() != null) {
                    LOGGER.info("cleanTitle url: " + url);
                    Document cleanlaw = iterablesclean.first();
                    String title = cleanlaw.getString("title").trim();
                    LOGGER.info("cleanTitle title: " + title);
                    Pattern titleRemoveAlter = Pattern.compile("(\\(\\d{1,4}((年)?修(正|订|改)+)?\\))(\\[失效\\])?|(\\[失效\\])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                    Matcher matcherTitle = titleRemoveAlter.matcher(title);
                    while (matcherTitle.find()){
                        if(matcherTitle.end() == title.length()){
                            title = title.substring(0, matcherTitle.start());
                            LOGGER.info("cleanTitle title change: " + title);
                            cleanlaw.put("title", title);
                            mongoDB.updateDocument(this.cleanCollection, cleanlaw);
                        }
                    }
                }
                num++;
                LOGGER.info("cleanTitle num: " + num);
            }
        } catch (Exception e) {
            LOGGER.error("cleanTitle find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        LOGGER.info("Done do cleanTitle...");
    }

}
