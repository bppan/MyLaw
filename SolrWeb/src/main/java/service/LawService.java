package service;

import log.MyLogger;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        StringBuilder contentHtml = new StringBuilder();
        String title = law.getString("title").trim();
        String[] contentArray = law.getString("content").split("\n");
        String zhang = "第[零一二三四五六七八九十百千万]+章";//定义章数
        String tiao = "第[零一二三四五六七八九十百千万]+条(之[一二三四五六七八九十百千万]+)?";//定义条数
        String jie = "第[零一二三四五六七八九十百千万]+节";//定义条数
        for (int i = 0; i < contentArray.length; i++) {
            String par = contentArray[i].trim();
            if (i == 0) {
                if (par.equals(title)) {
                    contentHtml.append("<p class='text-center paperTitle'><b>").append(par).append("</b></p>");
                    continue;
                } else {
                    contentHtml.append("<p class='text-center paperTitle'><b>").append(title).append("</b></p>");
                }
            }
            Pattern regEx_zhang = Pattern.compile(zhang, Pattern.CASE_INSENSITIVE);
            Matcher m_zhang = regEx_zhang.matcher(par);
            if (m_zhang.find() && m_zhang.start() == 0) {
                String name = par.substring(m_zhang.start(), m_zhang.end());
                String content = par.substring(m_zhang.end(), par.length());
                contentHtml.append("<p class='text-center'>").append(name).append(" ").append(content).append("</p>");
                continue;
            }
            Pattern regEx_jie = Pattern.compile(jie, Pattern.CASE_INSENSITIVE);
            Matcher m_jie = regEx_jie.matcher(par);
            if (m_jie.find() && m_jie.start() == 0) {
                String name = par.substring(m_jie.start(), m_jie.end());
                String content = par.substring(m_jie.end(), par.length());
                contentHtml.append("<p class='text-center'>").append(name).append(" ").append(content).append("</p>");
                continue;
            }
            Pattern regEx_tiao = Pattern.compile(tiao, Pattern.CASE_INSENSITIVE);
            Matcher m_tiao = regEx_tiao.matcher(par.trim());
            if (m_tiao.find() && m_tiao.start() == 0) {
                String name = par.substring(m_tiao.start(), m_tiao.end());
                String content = par.substring(m_tiao.end(), par.length());
                contentHtml.append("<p class='paperContent'>").append(name).append(" ").append(content).append("</p>");
                continue;
            }
            if (par.equals("目录")) {
                contentHtml.append("<p class='text-center'>").append(par).append("</p>");
                continue;
            }
            contentHtml.append("<p class='paperContent'>").append(par).append("</p>");
        }
        return contentHtml.toString();
    }
}
