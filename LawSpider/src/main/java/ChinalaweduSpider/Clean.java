package ChinalaweduSpider;

import Interface.LawSpider;
import Log.LawLogger;
import Mongo.MongoDB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.UpdateResult;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/11 17:10
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Clean {
    private static Logger LOGGER = LawLogger.getLawLogger(Clean.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private MongoCollection<Document> crawJobcollection;
    private MongoCollection<Document> lawCollecion;

    public Clean( String crawJobCollection, String lawCollection){
        this.crawJobcollection = mongoDB.getCollection(crawJobCollection);
        this.lawCollecion = mongoDB.getCollection(lawCollection);
    }
    public void doClean(){
        FindIterable<Document> iterables = this.crawJobcollection.find();
        MongoCursor<Document> cursor = iterables.iterator();
        while (cursor.hasNext()){
            Document crawjob = cursor.next();
            String url = crawjob.getString("url");
            String category = crawjob.getString("title");
            try {
                doDocument(url, category);
            }catch (Exception e){
                LOGGER.error("Do document error: " + e.getMessage());
            }
        }
    }

    public void doDocument(String url, String category){
        FindIterable<Document> iterables = this.lawCollecion.find(new Document("url", url)).limit(1);
        if(iterables.first() == null){
            LOGGER.warn("No exits in law url:" + url);
            return;
        }
        Document law = iterables.first();
        String html = law.getString("rawHtml");
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        String content = doc.select("#fontzoom").first().html();
        String cleanHtml = LawSpider.cleanHtml(content);

        String[] contentList = cleanHtml.split("\n");
        StringBuilder updateContent = new StringBuilder();
        for (String contentpar:contentList) {
            if(contentpar.trim().equals("\n")){
                continue;
            }
            if(contentpar.trim().contains("责任编辑：")){
                continue;
            }
            updateContent.append(contentpar.trim()).append("\n");
        }
        updateDocumentContent(url, "法律法规", category, "现行有效", updateContent.toString());
    }

    public static void main(String[] args){
        Clean clean = new Clean("chinaLawedu_crawJob", "chinaLawedu_law");
//        clean.testUpdate();
        clean.doClean();
//        clean.doDocument("http://www.chinalawedu.com/falvfagui/21752/wa1710171278.shtml", "法律");
//        String test = "责任编辑";
//        System.out.println(test.substring(0,2));
    }

    public void updateDocumentContent(String url, String level, String category, String timeless, String content){
        Document filter = new Document();
        filter.append("url", url);
        Document update = new Document();
        update.append("$set", new Document("content", content).append("category", category).append("level", level).append("timeless", timeless));
        UpdateResult result = this.lawCollecion.updateOne(filter, update);
        LOGGER.info("Matched count = " + result.getMatchedCount());
    }
    public void deleteRepeat(String url, String title, String content){
        FindIterable<Document> iterables = this.lawCollecion.find(new Document("url", url));
        deleteAttributeRepeat(iterables);

        FindIterable<Document> iterablesTitle = this.lawCollecion.find(new Document("title", title));
        deleteAttributeTitleRepeat(iterablesTitle, content);

    }
    public void deleteAttributeRepeat(FindIterable<Document> iterables){
        MongoCursor<Document> cursor = iterables.iterator();
        boolean isFirst = false;
        while (cursor.hasNext()){
            if(!isFirst){
                cursor.next();
                isFirst = true;
            }else {
                Document law = cursor.next();
                Document filter = new Document();
                filter.append("_id", law.getString("_id"));
                this.lawCollecion.deleteOne(filter);
            }
        }
    }
    public void deleteAttributeTitleRepeat(FindIterable<Document> iterables, String content){
        MongoCursor<Document> cursor = iterables.iterator();
        boolean isFirst = false;
        while (cursor.hasNext()){
            if(!isFirst){
                cursor.next();
                isFirst = true;
            }else {
                Document law = cursor.next();
                if(law.getString("content").trim().equals(content)){
                    Document filter = new Document();
                    filter.append("_id", law.getString("_id"));
                    this.lawCollecion.deleteOne(filter);
                }
            }
        }
    }
}
