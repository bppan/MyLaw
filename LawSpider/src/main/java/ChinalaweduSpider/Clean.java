package ChinalaweduSpider;

import Interface.LawClean;
import Mongo.MongoDB;
import org.bson.Document;
import org.jsoup.Jsoup;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/11 17:10
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Clean extends LawClean {
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    public Clean(String crawJobCollection, String lawCollection, String cleanCollection) {
        super(crawJobCollection, lawCollection, cleanCollection);
    }

    public String getContentHtmlBySelect(String html) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("#fontzoom").first().html();
        } catch (Exception e) {
            return "";
        }
    }

    public String getCleanContent(String cleanHtml) {
        String[] contentList = cleanHtml.split("\n");
        StringBuilder updateContent = new StringBuilder();
        for (String contentpar : contentList) {
            if (contentpar.isEmpty()) {
                continue;
            }
            if (contentpar.trim().contains("责任编辑：")) {
                continue;
            }
            if (contentpar.trim().contains("转发分享：")) {
                continue;
            }
            updateContent.append(contentpar.trim()).append("\n");
        }
        return updateContent.toString();
    }

    public void cleanContent(Document law) {
        String title = law.getString("title");
        if (title == null) {
            String html = law.getString("rawHtml");
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            String theTitle = doc.title();
            law.put("title", theTitle.replace("-法律教育网", ""));
        }
        super.cleanContent(law);
    }

    public void updateDocumentContent(Document law) {
        law.put("timeless", "现行有效");
        law.put("level", "法律法规");
        mongoDB.replaceDocument(getLawCollecion(), law);
    }

}
