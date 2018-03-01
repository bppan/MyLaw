package ChinalaweduSpider;

import Log.LawLogger;
import Mongo.MongoDB;
import SimHash.SimHash;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/1/26 22:14
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class ChinalaweduCleanContent {
    private static Logger LOGGER = LawLogger.getLawLogger(ChinalaweduCleanContent.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private MongoCollection<Document> lawCollecion;
    private MongoCollection<Document> cleanCollection;

    public ChinalaweduCleanContent(String lawCollection, String cleanCollection) {
        this.lawCollecion = mongoDB.getCollection(lawCollection);
        this.cleanCollection = mongoDB.getCollection(cleanCollection);
    }

    public static void main(String[] args) {
        ChinalaweduCleanContent chinalaweduCleanContent = new ChinalaweduCleanContent("chinaLawedu_clean", "law3");
        chinalaweduCleanContent.addContentHtml();
    }

    public void cleanContent() {
        LOGGER.info("Begin do cleanContent...");
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
                    String[] contentList = cleanlaw.getString("content").split("\n");
                    StringBuilder updateContent = new StringBuilder();
                    boolean department = false;
                    boolean release_date = false;
                    boolean implement_date = false;
                    boolean release_number = false;
                    boolean timeless = false;
                    for (String contentpar : contentList) {
                        if (contentpar.indexOf("发文单位：") == 0 && !department) {
                            department = true;
                            continue;
                        }
                        if (contentpar.indexOf("发布日期：") == 0 && !release_date) {
                            release_date = true;
                            continue;
                        }
                        if (contentpar.indexOf("执行日期：") == 0 && !implement_date) {
                            implement_date = true;
                            continue;
                        }
                        if (contentpar.indexOf("生效日期：") == 0 && !timeless) {
                            timeless = true;
                            continue;
                        }
                        if (contentpar.indexOf("文号：") == 0 && !release_number) {
                            release_number = true;
                            continue;
                        }
                        updateContent.append(contentpar.trim()).append("\n");
                    }
                    cleanlaw.put("content", updateContent.toString());
                    SimHash simHash = new SimHash(updateContent.toString());
                    cleanlaw.put("simHash", simHash.getIntSimHash().toString());
                    cleanlaw.put("simHashPart1", simHash.getStrSimHash().substring(0, 16));
                    cleanlaw.put("simHashPart2", simHash.getStrSimHash().substring(16, 32));
                    cleanlaw.put("simHashPart3", simHash.getStrSimHash().substring(32, 48));
                    cleanlaw.put("simHashPart4", simHash.getStrSimHash().substring(48, 64));
                    mongoDB.updateDocument(this.cleanCollection, cleanlaw);
                }
                num++;
                LOGGER.info("doClean clean num: " + num);
            }
        } catch (Exception e) {
            LOGGER.error("do clean find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        LOGGER.info("Done do cleanContent...");
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
                    Elements contentHtml = lawHtmlDocument.select("#fontzoom");
                    Elements esd = contentHtml.select("p");
                    Iterator<Element> iterator = esd.iterator();
                    while (iterator.hasNext())
                    {
                        Element etemp = iterator.next();
                        String styleStr = etemp.text();
                        if(styleStr.contains("发文单位")){
                            etemp.remove();
                            continue;
                        }
                        if(styleStr.contains("发布日期")){
                            etemp.remove();
                            continue;
                        }
                        if(styleStr.contains("生效日期")){
                            etemp.remove();
                            continue;
                        }
                        if(styleStr.contains("文　　号")){
                            etemp.remove();
                            continue;
                        }
                        if(styleStr.contains("执行日期")){
                            etemp.remove();
                            continue;
                        }
                        if(styleStr.contains("失效日期")){
                            etemp.remove();
                        }
                    }
                    Elements esdDiv = contentHtml.select("div");
                    Iterator<Element> iteratorDiv = esdDiv.iterator();
                    while (iteratorDiv.hasNext())
                    {
                        Element etemp = iteratorDiv.next();
                        String styleStr = etemp.text();
                        if(styleStr.contains("责任编辑：")){
                            etemp.remove();
                            continue;
                        }
                        if(styleStr.contains("转发分享：")){
                            etemp.remove();
                        }
                    }
                    Elements esda = contentHtml.select("a");
                    Iterator<Element> iteratora = esda.iterator();
                    while (iteratora.hasNext())
                    {
                        Element etemp = iteratora.next();
                        etemp.removeAttr("href");
                        etemp.removeAttr("title");
                        etemp.removeAttr("target");
                        etemp.removeAttr("name");
                    }
                    contentHtml.select("title").remove();
                    contentHtml.select("script").remove();
                    contentHtml.select("meta").remove();
                    contentHtml.select("link").remove();
                    contentHtml.select("*").removeAttr("class");
                    contentHtml.select("*").removeAttr("color");
                    contentHtml.select("font").removeAttr("face");
                    contentHtml.select("font").removeAttr("size");
                    cleanlaw.append("contentHtml", contentHtml.html().replaceAll("<a>","").replaceAll("</a>",""));
//                    System.out.println(contentHtml.html().replaceAll("<a>","").replaceAll("</a>",""));
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
}
