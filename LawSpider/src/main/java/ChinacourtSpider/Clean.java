package ChinacourtSpider;

import Interface.LawClean;
import Log.LawLogger;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/11 17:07
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
            return doc.select("body > div.container > div > div.law_content").first().html().replaceAll("&nbsp;", "");
        } catch (Exception e) {
            return "";
        }
    }

    public void cleanContent(Document law) {
        if (isValid(law)) {
            law.put("level", "法律法规");
            law.put("category", "");
            law.put("timeless", "现行有效");
            super.cleanContent(law);
        }else {
            LOGGER.info("this law is unvaild...");
        }
    }

    public void saveToCleanCollection(Document law) {
        if (isValid(law)) {
            super.saveToCleanCollection(law);
        } else {
            LOGGER.info("this law is unvaild...");
        }
    }

    public boolean isValid(Document law) {
        String category = law.getString("category");
        if (category.trim().equals("国家法律法规") || category.trim().equals("地方法规")) {
            return true;
        }
        return false;
    }
}
