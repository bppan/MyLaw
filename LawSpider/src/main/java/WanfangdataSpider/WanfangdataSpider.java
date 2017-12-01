package WanfangdataSpider;

import CrawJob.CrawJob;
import Interface.LawSpider;
import Log.LawLogger;
import Mongo.LawArticle;
import Mongo.LawDocument;
import WebCraw.HtmlUnitClient;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/1 15:22
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */

public class WanfangdataSpider extends LawSpider {
    private static Logger LOGGER = LawLogger.getLawLogger(WanfangdataSpider.class);

    public WanfangdataSpider(String indexUrl, int crawHtmlthreadCount, String pkulawSpider_crawJobCollection, String pkulawSpider_lawCollection) {
        super(indexUrl, crawHtmlthreadCount, pkulawSpider_crawJobCollection, pkulawSpider_lawCollection);
    }

    public void crawUrl(String categoryName, HtmlElement content) {
        DomNodeList<HtmlElement> clickAnchorNodes = content.getElementsByTagName("a");
        deepCrawContentUrl(categoryName, clickAnchorNodes);
    }

    public HtmlPage getSoureUrlPage(WebClient client, String xpath) {
        HtmlPage page = null;
        try {
            page = client.getPage(getIndexUrl());
        } catch (Exception e) {
            LOGGER.error("Get SoureUrlPage error: " + e.getMessage());
        }
        return page;
    }

    public void crawOneSoureceUrlField(String xpath) {
        WebClient client = HtmlUnitClient.getSingletonHtmlUntiClent();
        List<HtmlElement> anchoresNodes = getSoureceUrlField(client, xpath);
        LOGGER.info("Get source filed url count:" + anchoresNodes.size());
        for (int m = 0; m < anchoresNodes.size(); m++) {
            try {
                HtmlAnchor anchor = (HtmlAnchor) anchoresNodes.get(m);
                HtmlPage clickPage = anchor.click();
                Thread.sleep(3000);
                HtmlDivision content = getContentPage(clickPage);
                this.crawUrl(anchor.asText(), content);
                break;
            } catch (Exception e) {
                LOGGER.error("Get content error:" + e.getMessage());
            }
        }
    }

    //获取爬取内容的页面
    public HtmlDivision getContentPage(HtmlPage nextClickPage) {
        HtmlDivision content = null;
        try {
            content = (HtmlDivision) nextClickPage.getByXPath("/html/body/div[2]/div[2]/div[2]").get(0);
        } catch (Exception e) {
            LOGGER.error("Get content page error: " + e.getMessage());
        }
        return content;
    }

    //获取带爬取的url区域
    public List<HtmlElement> getSoureceUrlField(WebClient client, String xpath) {
        List<HtmlElement> anchorsList = new ArrayList<HtmlElement>();
        try {
            HtmlPage page = getSoureUrlPage(client, xpath);
            //等待5秒后获取页面
            Thread.sleep(3000);
            //获取局部source url
            HtmlDivision sourceFiled = (HtmlDivision) page.getByXPath(xpath).get(0);
            //展开全部
            DomNodeList<HtmlElement> anchoresNodes = sourceFiled.getElementsByTagName("a");
            for (int i = 0; i < anchoresNodes.size(); i++) {
                HtmlAnchor tempAnchor = (HtmlAnchor) anchoresNodes.get(i);
                anchorsList.add(tempAnchor);
            }
        } catch (InterruptedException e) {
            LOGGER.error("get sourece filed url InterruptedException: " + e.getMessage());
        }
        return anchorsList;
    }

    //从获取主页上分类的url
    public LawDocument parseLawHtml(String htmlUrl) {
        return null;
    }

    private boolean isPageNum(String num) {
        try {
            Integer.valueOf(num);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private boolean isPageNext(String page) {
        if (isPageNum(page.trim())) {
            return true;
        }
        if (page.trim().equals("首页")) {
            return true;
        }
        if (page.trim().equals("上一页")) {
            return true;
        }
        if (page.trim().equals("下一页")) {
            return true;
        }
        if (page.trim().equals("尾页")) {
            return true;
        }
        if (page.trim().equals("GO")) {
            return true;
        }
        if (page.trim().equals("下一页>>")) {
            return true;
        }
        return false;
    }

    public LawDocument parseLawHtml(HtmlPage page) {
        Document doc = null;
        try {
            doc = Jsoup.parse(page.asXml());
        } catch (Exception e) {
            LOGGER.warn("Jsoup get html error:" + e.getMessage());
        }
        LawDocument lawDocument = new LawDocument();
        lawDocument.setRawHtml(page.asXml());
        try {
            String title = doc.select("body > div.fixed-width.baseinfo.clear > div > h1").first().childNode(0).toString();
            lawDocument.setTitle(title.trim());
        } catch (NullPointerException e) {
            LOGGER.warn("No tile of content");
        }
        try {
            String release_number = doc.select("body > div.fixed-width-wrap.fixed-width-wrap-feild > div > div:nth-child(2) > span.text").first().childNode(0).toString();
            lawDocument.setRelease_number(release_number);
        } catch (NullPointerException e) {
            LOGGER.warn("No release_number of content");
        }
        try {
            String department = doc.select("body > div.fixed-width-wrap.fixed-width-wrap-feild > div > div:nth-child(3) > span.text").first().childNode(0).toString();
            lawDocument.setDepartment(department);
        } catch (NullPointerException e) {
            LOGGER.warn("No department of content");
        }
        try {
            String level = doc.select("body > div.fixed-width-wrap.fixed-width-wrap-feild > div > div:nth-child(4) > span.text").first().childNode(0).toString();
            lawDocument.setLevel(level);
        } catch (NullPointerException e) {
            LOGGER.warn("No level of content");
        }
        try {
            String timeless = doc.select("body > div.fixed-width-wrap.fixed-width-wrap-feild > div > div:nth-child(5) > span.text").first().childNode(0).toString();
            lawDocument.setTimeless(timeless);
        } catch (NullPointerException e) {
            LOGGER.warn("No level of content");
        }
        try {
            String release_data = doc.select("body > div.fixed-width-wrap.fixed-width-wrap-feild > div > div:nth-child(6) > span.text").first().childNode(0).toString();
            lawDocument.setRelease_data(release_data);
        } catch (NullPointerException e) {
            LOGGER.warn("No release_data of content");
        }
        try {
            String implement_data = doc.select("body > div.fixed-width-wrap.fixed-width-wrap-feild > div > div:nth-child(7) > span.text").first().childNode(0).toString();
            lawDocument.setImplement_date(implement_data);
        } catch (NullPointerException e) {
            LOGGER.warn("No release_data of content");
        }
        try {
            String category = doc.select("body > div.fixed-width-wrap.fixed-width-wrap-feild > div > div:nth-child(8) > span.text").first().childNode(0).toString();
            lawDocument.setCategory(category);
        } catch (NullPointerException e) {
            LOGGER.warn("No category of content");
        }
        return lawDocument;
    }

    public void extratHtmlAndSave(String url, HtmlPage page, LawDocument document) {
        document.setUrl(page.getBaseURI());
        document.setRawHtml(page.asXml());
        String html = page.asXml();
        String cleanContent = cleanHtml(html);
        document.setCleanHtml(cleanContent);
        List<LawArticle> articleList = getLawArticleAndParagraph(cleanContent);
        document.setArticle(articleList);
        document.setCollection(getLawDocumen().getLawcollection());
        String comments = "";
        if (LawDocument.saveToDB(document)) {
            comments = "Save document to MongoDB success....";

        } else {
            comments = "Save document to MongoDB skip: the document already exits....";
        }
        LOGGER.info(comments);
        if (CrawJob.doneJob(getCrawJob().getCrawJobcollection(), url, comments)) {
            LOGGER.info("Craw job url[" + url + "] done....");
        } else {
            LOGGER.info("Craw job url[" + url + "] fail....");
        }
    }

    public void deepCrawContentUrl(String categoryName, DomNodeList<HtmlElement> clickAnchorNodes) {
        String clickPageHtml = "";
        DomNodeList<HtmlElement> currentClickAnchorNodes = clickAnchorNodes;
        int count = 0;
        int page = 0;
        do {
            //遍历当前页
            HtmlAnchor nextPageAnchor = null;
            for (int i = 0; i < currentClickAnchorNodes.size(); i++) {
                HtmlAnchor contentAnchor = (HtmlAnchor) currentClickAnchorNodes.get(i);
                if (!isPageNext(contentAnchor.asText())) {
                    count++;
                    try {
                        HtmlPage lawContent = contentAnchor.click();
                        Thread.sleep(2000);
                        LawDocument document = parseLawHtml(lawContent);
                        HtmlAnchor allContent = (HtmlAnchor) lawContent.getByXPath("/html/body/div[3]/div/div[1]/a[2]").get(0);
                        if (allContent != null) {
                            HtmlPage page1 = allContent.click();
                            Thread.sleep(3000);
                            if (this.addUrl(categoryName, page1.getBaseURI())) {
                                LOGGER.info("Sava success url:[" + categoryName + "][" + page + "][" + count + "]" + allContent.getHrefAttribute());
                            } else {
                                LOGGER.info("Alerady exits url:[" + categoryName + "][" + page + "][" + count + "]" + allContent.getHrefAttribute());
                            }
                            extratHtmlAndSave(page1.getBaseURI(), page1, document);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Click content anchor error:" + e.getMessage());
                    }
                }
                if (contentAnchor.asText().trim().contains("下一页>>")) {
                    nextPageAnchor = contentAnchor;
                }
            }
            if (nextPageAnchor == null) {
                break;
            } else {
                page++;
                count = 0;
                try {
                    HtmlPage nextClickPage = nextPageAnchor.click();
                    Thread.sleep(2000);
                    HtmlDivision content = getContentPage(nextClickPage);
                    if (content.asXml().trim().equals(clickPageHtml)) {
                        break;
                    }
                    clickPageHtml = content.asXml().trim();
                    currentClickAnchorNodes = content.getElementsByTagName("a");

                } catch (Exception e) {
                    LOGGER.error("Deep craw content error: " + e.getMessage());
                }
            }
        } while (true);
    }
}