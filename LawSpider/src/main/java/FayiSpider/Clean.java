package FayiSpider;

import Interface.LawClean;
import org.bson.Document;
import org.jsoup.Jsoup;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/17 10:34
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Clean extends LawClean {

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
        String html = law.getString("rawHtml");
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        if (!FayiSpider.hasNextpage(doc)) {
            law.put("category", "");
            super.cleanContent(law);
        }
    }

    public void saveToCleanCollection(Document law) {
        String html = law.getString("rawHtml");
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        if (!FayiSpider.hasNextpage(doc)) {
            super.saveToCleanCollection(law);
        }
    }

    public void updateDocumentContent(Document law) {
        law.put("timeless", "现行有效");
        law.put("level", "法律法规");
        super.updateDocumentContent(law);
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


}
