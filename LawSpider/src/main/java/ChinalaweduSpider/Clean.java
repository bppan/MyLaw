package ChinalaweduSpider;

import Interface.LawClean;
import Interface.LawSpider;
import Log.LawLogger;
import Mongo.LawArticle;
import Mongo.LawDocument;
import Mongo.MongoDB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.UpdateResult;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.E;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;

import java.util.ArrayList;
import java.util.List;
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
public class Clean extends LawClean{

    public Clean( String crawJobCollection, String lawCollection, String cleanCollection){
        super(crawJobCollection, lawCollection, cleanCollection);
    }

    public String getContentHtmlByselect(String html){
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("#fontzoom").first().html();
        }catch (Exception e){
            return "";
        }
    }

    public void updateDocumentContent(String category, String content, List<Document> interlDocuments, Document law) {
        String level = "法律法规";
        String timeless = "现行有效";
        org.bson.types.ObjectId id = law.getObjectId("_id");
        Document filter = new Document();
        filter.append("_id", id);
        Document update = new Document();
        update.append("$set", new Document("content", content)
                .append("category", category)
                .append("level", level)
                .append("timeless", timeless)
                .append("article_num", interlDocuments.size())
                .append("articles", interlDocuments));
        getLawCollecion().updateOne(filter, update);
    }

    public String getCleanContent(String cleanHtml){
        String[] contentList = cleanHtml.split("\n");
        StringBuilder updateContent = new StringBuilder();
        for (String contentpar:contentList) {
            if(contentpar.isEmpty()){
                continue;
            }
            if(contentpar.trim().contains("责任编辑：")){
                continue;
            }
            updateContent.append(contentpar.trim()).append("\n");
        }
        return updateContent.toString();
    }
}
