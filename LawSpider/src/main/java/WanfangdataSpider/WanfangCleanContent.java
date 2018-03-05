package WanfangdataSpider;

import Log.LawLogger;
import Mongo.MongoDB;
import SimHash.SimHash;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/1/26 22:14
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class WanfangCleanContent {
    private static Logger LOGGER = LawLogger.getLawLogger(WanfangCleanContent.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private MongoCollection<Document> lawCollecion;
    private MongoCollection<Document> cleanCollection;

    public WanfangCleanContent(String lawCollection, String cleanCollection) {
        this.lawCollecion = mongoDB.getCollection(lawCollection);
        this.cleanCollection = mongoDB.getCollection(cleanCollection);
    }

    public static void main(String[] args) {
        WanfangCleanContent wanfangCleanContent = new WanfangCleanContent("wanfangdata_clean", "law3");
        wanfangCleanContent.cleanTitle();
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
                    boolean release_date = false;
                    boolean implement_date = false;
                    boolean timeless = false;
                    for (String contentpar : contentList) {
                        if (contentpar.indexOf("【颁布日期】") == 0 && !release_date) {
                            release_date = true;
                            continue;
                        }
                        if (contentpar.indexOf("【实施日期】") == 0 && !implement_date) {
                            implement_date = true;
                            continue;
                        }
                        if (contentpar.indexOf("【有效性】") == 0 && !timeless) {
                            timeless = true;
                            continue;
                        }
                        if (contentpar.trim().isEmpty()) {
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
                    Elements lawHtmlHead = lawHtmlDocument.select("body > *:nth-child(1)");
                    if (lawHtmlHead.html().contains("【颁布日期】")) {
                        lawHtmlHead.remove();
                    }
                    Elements lawHtmlHead2 = lawHtmlDocument.select("body > *:nth-child(1)");
                    if (lawHtmlHead2.html().contains("【有效性】")) {
                        lawHtmlHead2.remove();
                    }
                    Elements contentHtml = lawHtmlDocument.getElementsByTag("body");
                    contentHtml.select("title").remove();
                    contentHtml.select("script").remove();
                    contentHtml.select("meta").remove();
                    contentHtml.select("link").remove();
                    contentHtml.select("*").removeAttr("class");
                    contentHtml.select("*").removeAttr("style");
                    contentHtml.select("*").removeAttr("color");
                    contentHtml.select("font").removeAttr("face");
                    contentHtml.select("font").removeAttr("size");
                    cleanlaw.append("contentHtml", contentHtml.html());
//                    System.out.println(contentHtml.html());
                    try {
                        mongoDB.updateDocument(this.cleanCollection, cleanlaw);
                    }catch (Exception e){
                        LOGGER.error("addContentHtml updatedocument error: " + e);
                    }

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
                    Pattern titleRemoveAlter = Pattern.compile("(（\\d{1,4}((年)?修(正|订|改)+)?）)(\\[失效\\])?|(\\[失效\\])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
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
