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
import org.jsoup.nodes.Node;

import java.io.IOException;
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
            LOGGER.error("get source filed url InterruptedException: " + e.getMessage());
        }
        return anchorsList;
    }

    //从获取主页上分类的url
    public LawDocument parseLawHtml(String htmlUrl) {
        WebClient client = HtmlUnitClient.getSingletonHtmlUntiClent();
        HtmlPage page = null;
        try {
            page = client.getPage(htmlUrl);
            Thread.sleep(1000);
        } catch (Exception e) {
            LOGGER.error("Get SoureUrlPage error: " + e.getMessage());
        }
        LawDocument doc = parseLawHtmlGetDocument(page);
        HtmlAnchor contentAnchor = null;
        try {
            contentAnchor = (HtmlAnchor) page.getByXPath("/html/body/div[3]/div/div[1]/a[2]").get(0);
        }catch (NullPointerException e){
            LOGGER.error("Not find content anchor");
        }
        if (contentAnchor != null) {
            try {
                HtmlPage contentPage = contentAnchor.click();
                Thread.sleep(2000);
                String html = contentPage.asXml();
                doc.setRawHtml(html);
                String cleanContent = cleanHtml(html);
                doc.setCleanHtml(cleanContent);
                List<LawArticle> articleList = getLawArticleAndParagraph(cleanContent);
                doc.setArticle(articleList);
            } catch (Exception e) {
                LOGGER.error("pase html content error: " + e.getMessage());
            }
        }
        return doc;
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

    public LawDocument parseLawHtmlGetDocument(HtmlPage page) {
        Document doc = null;
        try {
            doc = Jsoup.parse(page.asXml());
        } catch (Exception e) {
            LOGGER.warn("Jsoup get html error:" + e.getMessage());
        }
        LawDocument lawDocument = new LawDocument();
        try {
            String title = doc.select("body > div.fixed-width.baseinfo.clear > div > h1").first().childNode(0).toString();
            lawDocument.setTitle(title.trim());
        } catch (Exception e) {
            LOGGER.warn("No tile of content");
        }
        List<Node> nodes = doc.select("body > div.fixed-width-wrap.fixed-width-wrap-feild > div").first().childNodes();
        for (Node node:nodes) {
            if(node.childNodes().size() != 5){
                System.out.println("skip");
                continue;
            }
            String name = node.childNode(1).toString();
            if(name.contains("发文文号")){
                String release_number = node.childNode(3).childNode(0).toString();
                lawDocument.setRelease_number(release_number);
            }
            if (name.contains("颁布部门")){
                String department = node.childNode(3).childNode(0).toString();
                lawDocument.setDepartment(department);
            }
            if (name.contains("效力级别")){
                String level = node.childNode(3).childNode(0).toString();
                lawDocument.setLevel(level);
            }
            if (name.contains("时效性")){
                String timeless = node.childNode(3).childNode(0).toString();
                lawDocument.setTimeless(timeless);
            }
            if (name.contains("颁布日期")){
                String release_data = node.childNode(3).childNode(0).toString();
                lawDocument.setRelease_data(release_data);
            }
            if (name.contains("实施日期")){
                String implement_data = node.childNode(3).childNode(0).toString();
                lawDocument.setImplement_date(implement_data);
            }
            if (name.contains("内容分类")){
                String category = node.childNode(3).childNode(0).toString();
                lawDocument.setCategory(category);
            }
        }
        return lawDocument;
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
                if (!isPageNext(contentAnchor.asText()) && contentAnchor.getAttribute("class").trim().equals("title")) {
                    count++;
                    if (this.addUrl(categoryName, contentAnchor.getHrefAttribute())) {
                        LOGGER.info("Save success url:[" + categoryName + "][" + page + "][" + count + "]" + contentAnchor.getHrefAttribute());
                    } else {
                        LOGGER.info("Already exists url:[" + categoryName + "][" + page + "][" + count + "]" + contentAnchor.getHrefAttribute());
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