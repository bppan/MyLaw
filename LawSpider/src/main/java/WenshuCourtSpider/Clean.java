package WenshuCourtSpider;

import Interface.LawClean;
import Interface.LawSpider;
import Log.LawLogger;
import Mongo.LawArticle;
import Mongo.LawDocument;
import SimHash.SimHash;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/19 14:10
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Clean extends LawClean {

    private static Logger LOGGER = LawLogger.getLawLogger(Clean.class);

    public Clean(String crawJobCollection, String lawCollection, String cleanCollection) {
        super(crawJobCollection, lawCollection, cleanCollection);
    }

    public String getContentHtmlBySelect(String html) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("#zoom").first().html().replaceAll("&nbsp;", "");
        } catch (Exception e) {
            return "";
        }
    }

    public String getCleanContent(String cleanHtml) {
        String[] contentList = cleanHtml.split("\n");
        StringBuilder updateContent = new StringBuilder();
        for (String contentpar : contentList) {
            if (contentpar.trim().isEmpty() || contentpar.trim().equals("/**/")) {
                continue;
            }
            updateContent.append(contentpar.trim()).append("\n");
        }
        return updateContent.toString();
    }

    public void cleanContent(Document law) {
        law.put("level", "裁判文书");
        law.put("timeless", "现行有效");
        String html = law.getString("rawHtml");
        String content = getContentHtmlBySelect(html);
        String cleanHtml = LawSpider.cleanHtml(content);
        String updateContent = getCleanContent(cleanHtml);
        String[] contentList = cleanHtml.split("\n");

        try {
            String category = contentList[1];
            law.put("category", category.trim().replaceAll(" ", ""));
        } catch (NullPointerException e) {
            LOGGER.warn("No category of content");
        }

        try {
            String department = contentList[0];
            law.put("department", department.trim());
        } catch (NullPointerException e) {
            LOGGER.warn("No department of content");
        }

        try {
            String release_number = contentList[2];
            law.put("release_number", release_number.trim());
        } catch (NullPointerException e) {
            LOGGER.warn("No release_number of content");
        }

        List<LawArticle> articleList = new ArrayList<LawArticle>();
        List<Document> interlDocuments = LawDocument.getArticleDocument(articleList);

        law.put("article_num", interlDocuments.size());
        law.put("content", updateContent);
        law.put("articles", interlDocuments);

        SimHash simHash = new SimHash(updateContent);
        law.append("simHash", simHash.getStrSimHash());
        law.append("simHashPart1", simHash.getStrSimHash().substring(0, 16));
        law.append("simHashPart2", simHash.getStrSimHash().substring(16, 32));
        law.append("simHashPart3", simHash.getStrSimHash().substring(32, 48));
        law.append("simHashPart4", simHash.getStrSimHash().substring(48, 64));

        updateDocumentContent(law);
    }

    public void saveToCleanCollection(Document law) {
        super.saveToCleanCollection(law);
    }

}
