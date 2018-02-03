package FayiSpider;

import Interface.LawClean;
import Interface.LawSpider;
import Log.LawLogger;
import Mongo.MongoDB;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/17 10:34
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Clean extends LawClean {

    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private static Logger LOGGER = LawLogger.getLawLogger(Clean.class);

    public Clean(String crawJobCollection, String lawCollection, String cleanCollection) {
        super(crawJobCollection, lawCollection, cleanCollection);
    }

    public String getContentHtmlBySelect(String html) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("#articleContnet").first().html();
        } catch (Exception e) {
            return "";
        }
    }

    public void cleanContent(Document law) {
        if (isValid(law)) {
            law.put("category", "");
            law.put("timeless", "现行有效");
            law.put("level", "法律法规");
            String html = law.getString("rawHtml");
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            Elements elements = doc.select("#fontzoom > p.bh");
            for (Element ele : elements) {
                try {
                    String tag = ele.childNode(0).childNode(0).toString();
                    if (tag.trim().equals("生效日期")) {
                        String implement_date = ele.childNode(1).toString().replace("：", "");
                        law.put("implement_date", implement_date);
                        break;
                    }
                } catch (NullPointerException e) {
                    LOGGER.warn("No this tag..." + e.getMessage());
                }
            }
            super.cleanContent(law);
        } else {
            mongoDB.deleteDocumentOneById(getLawCollecion(), law);
            LOGGER.info("this law is unvaild deleted...");
        }
    }

    public boolean saveToCleanCollection(Document law) {
        if (isValid(law)) {
            return super.saveToCleanCollection(law);
        } else {
            LOGGER.info("this law is unvaild not save to clean collection...");
            return false;
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
            updateContent.append(contentpar.trim()).append("\n");
        }
        return updateContent.toString();
    }

    public boolean isValid(Document law) {
        String html = law.getString("rawHtml");
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        String content = getContentHtmlBySelect(html);
        String cleanHtml = LawSpider.cleanHtml(content);
        if (!cleanHtml.contains("此文章仅供VIP会员浏览") && !hasNextpage(doc)) {
            return true;
        }
        return false;
    }

    public boolean hasNextpage(org.jsoup.nodes.Document doc) {
        try {
            if (doc.select("#pe100_page_contentpage").first().childNodes().size() == 0) {
                return false;
            } else {
                return true;
            }
        } catch (NullPointerException e) {
            return false;
        }
    }
}
