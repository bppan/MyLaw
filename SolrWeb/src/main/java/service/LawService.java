package service;

import log.MyLogger;
import org.apache.log4j.Logger;
import org.bson.Document;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/1/30 15:33
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class LawService {
    private static Logger LOGGER = MyLogger.getMyLogger(LawService.class);
    private Document law;

    public LawService(Document law) {
        this.law = law;
    }

    public String getTitleHtml() {
        StringBuilder titleHtml = new StringBuilder();
        if (law.getString("department") != null && !law.getString("department").trim().isEmpty()) {
            titleHtml.append("<p>").append("【发布部门】 ").append(law.getString("department").trim()).append("</p>");
        }
        if (law.getString("release_number") != null && !law.getString("release_number").trim().isEmpty()) {
            titleHtml.append("<p>").append("【发文字号】 ").append(law.getString("release_number").trim()).append("</p>");
        }
        if (law.getString("release_date") != null && !law.getString("release_date").trim().isEmpty()) {
            titleHtml.append("<p>").append("【发布日期】 ").append(law.getString("release_date").trim()).append("</p>");
        }
        if (law.getString("implement_date") != null && !law.getString("implement_date").trim().isEmpty()) {
            titleHtml.append("<p>").append("【实施日期】 ").append(law.getString("implement_date").trim()).append("</p>");
        }
        if (law.getString("level") != null && !law.getString("level").trim().isEmpty()) {
            titleHtml.append("<p>").append("【效力级别】 ").append(law.getString("level").trim()).append("</p>");
        }
        if (law.getString("category") != null && !law.getString("category").trim().isEmpty()) {
            titleHtml.append("<p>").append("【法规类别】 ").append(law.getString("category").trim()).append("</p>");
        }
        if (law.getString("timeless") != null && !law.getString("timeless").trim().isEmpty()) {
            titleHtml.append("<p>").append("【时效性】 ").append(law.getString("timeless").trim()).append("</p>");
        }
        titleHtml.append("<p>").append("【文件来源】 ").append("<a href='").append(law.getString("url")).append("' target='_blank' style='color:#006621;word-break:break-all;'>")
                .append(law.getString("url").trim()).append("</a></p>");
        return titleHtml.toString();
    }

    public String getContentHtml() {
        String contentHtml = "";
        try {
            contentHtml = law.getString("contentHtml").trim();
        } catch (Exception e) {
            LOGGER.error("getContentHtml error: " + e);
        }
        return contentHtml;
    }
}
