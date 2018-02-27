package WenshuCourtSpider;

import Log.LawLogger;
import Mongo.MongoDB;
import WanfangdataSpider.WanfangCleanContent;
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
 * Created:  2018/2/26 17:04
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class WenshuCourtContent {
    private static Logger LOGGER = LawLogger.getLawLogger(WenshuCourtContent.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private MongoCollection<Document> lawCollecion;
    private MongoCollection<Document> cleanCollection;
    public WenshuCourtContent(String lawCollection, String cleanCollection) {
        this.lawCollecion = mongoDB.getCollection(lawCollection);
        this.cleanCollection = mongoDB.getCollection(cleanCollection);
    }

    public static void main(String[] args) {
        WenshuCourtContent wenshuCourtContent = new WenshuCourtContent("wenshucourt_clean", "law3");
        wenshuCourtContent.addContentHtml();
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
                    Elements contentHtml = lawHtmlDocument.select("#zoom");
                    contentHtml.select("table").remove();
                    contentHtml.select("title").remove();
                    contentHtml.select("script").remove();
                    contentHtml.select("meta").remove();
                    contentHtml.select("link").remove();
                    contentHtml.select("*").removeAttr("class");
                    contentHtml.select("*").removeAttr("color");
                    contentHtml.select("font").removeAttr("face");
                    contentHtml.select("font").removeAttr("size");
                    Elements esd = contentHtml.select("[style]");
                    Iterator<Element> iterator = esd.iterator();
                    while (iterator.hasNext())
                    {
                        Element etemp = iterator.next();
                        String styleStr = etemp.attr("style");
                        etemp.removeAttr("style");
                        styleStr =styleStr.replaceAll("LINE-HEIGHT: 25pt;", "LINE-HEIGHT: 22pt;");
                        styleStr = styleStr.replaceAll("FONT-SIZE: 15pt;", "FONT-SIZE: 12pt;");
                        styleStr =styleStr.replaceAll("FONT-SIZE: 18pt;", "FONT-SIZE: 14pt;");
                        etemp.attr("style", styleStr);
                    }
                    cleanlaw.append("contentHtml", contentHtml.html());
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
