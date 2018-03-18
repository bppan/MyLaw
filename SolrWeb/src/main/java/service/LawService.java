package service;

import dao.MongoDB;
import dao.WebProperties;
import log.MyLogger;
import model.Article;
import org.apache.log4j.Logger;
import org.bson.Document;
import util.NumberChange;

import java.util.ArrayList;
import java.util.List;

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
    private static MongoDB mongoDB = MongoDB.getMongoDB();

    public Document getLawDocument(String lawDoucumentId) {
        Document law = null;
        try {
            law = mongoDB.getDocumentById(lawDoucumentId);
        } catch (Exception e) {
            LOGGER.error("WebServer get document from db error: " + e);
        }
        return law;
    }

    public String getTitleHtml(Document law) {
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

    public String getContentHtml(Document law) {
        String contentHtml = "";
        try {
            contentHtml = law.getString("contentHtml").trim();
        } catch (Exception e) {
            LOGGER.error("getContentHtml error: " + e);
        }
        return contentHtml;
    }

    @SuppressWarnings("unchecked")
    public List<Article> getArtcileContent(String lawName, String lawTiaoName) {
        List<Document> lawList = mongoDB.getDocumentByName(lawName);
        List<Article> resultList = new ArrayList<>();
        for (Document law : lawList) {
            List<Document> documentList = (List<Document>) law.get("articles");
            for (int i = 0; i < documentList.size(); i++) {
                Document article = documentList.get(i);
                String articleId = law.getObjectId("_id").toString() + "-" + i;
                if (article.getString("name").trim().equals(lawTiaoName)) {
                    Article articleReturn = new Article();
                    articleReturn.setProperty(law);
                    articleReturn.setId(articleId);
                    articleReturn.setName(lawTiaoName);

                    StringBuilder articleConent = new StringBuilder("");
                    List<String> paraArray = (List<String>) article.get("paragraph");
                    for (String para : paraArray) {
                        articleConent.append(para);
                    }
                    articleReturn.setArticleContent(articleConent.toString());
                    resultList.add(articleReturn);
                }
            }
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public Article getLawContent(String id) {
        Article resultArticle = new Article();
        String[] splitId = id.split("-");
        String lawId = splitId[0];
        Document law = this.getLawDocument(lawId);
        String lawName = law.getString("title");
        if (splitId.length == 3) {
            String articleId = splitId[1];
            String parId = splitId[2];
            List<Document> documentList = (List<Document>) law.get("articles");
            for (int i = 0; i < documentList.size(); i++) {
                if (String.valueOf(i).equals(articleId)) {
                    Document article = documentList.get(i);
                    List<String> paraArray = (List<String>) article.get("paragraph");
                    for (int j = 0; j < paraArray.size(); j++) {
                        if (String.valueOf(j).equals(parId)) {
                            String content = paraArray.get(j);
                            String name = lawName + "第" + NumberChange.numberToChinese(i + 1) + "条" + "第" + NumberChange.numberToChinese(j + 1) + "款";
                            resultArticle.setId(id);
                            resultArticle.setName(name);
                            resultArticle.setArticleContent(content);
                            break;
                        }
                    }
                    break;
                }
            }
            return resultArticle;
        }
        if (splitId.length == 2) {
            String articleId = splitId[1];
            List<Document> documentList = (List<Document>) law.get("articles");
            for (int i = 0; i < documentList.size(); i++) {
                if (String.valueOf(i).equals(articleId)) {
                    Document article = documentList.get(i);
                    List<String> paraArray = (List<String>) article.get("paragraph");
                    StringBuilder articleContent = new StringBuilder();
                    for (String par : paraArray) {
                        articleContent.append(par);
                    }
                    String name = lawName + "第" + NumberChange.numberToChinese(i + 1) + "条";
                    resultArticle.setName(name);
                    resultArticle.setId(id);
                    resultArticle.setArticleContent(articleContent.toString());
                    break;
                }
            }
            return resultArticle;
        }
        resultArticle.setId(lawId);
        resultArticle.setName(lawName);
        String absContent = law.getString("content");
        if (absContent.length() > 250) {
            absContent = absContent.substring(0, 250) + "...";
        }
        resultArticle.setArticleContent(absContent);
        return resultArticle;
    }

    public String getPaperUrl(String id) {
        if (id == null || id.isEmpty()) {
            return "";
        }
        String[] splitId = id.split("-");
        String lawId = splitId[0];
        WebProperties dbProperties = WebProperties.getWebProperties();
        java.util.Properties prop = dbProperties.getProp();
        String paperBaseURL = prop.getProperty("paperBaseUrl");
        return paperBaseURL + "?id=" + lawId;
    }
}
