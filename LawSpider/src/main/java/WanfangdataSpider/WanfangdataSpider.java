package WanfangdataSpider;

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

import java.util.*;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/1 15:22
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */

public class  WanfangdataSpider extends LawSpider {
    private static Logger LOGGER = LawLogger.getLawLogger(WanfangdataSpider.class);
    private static WebClient client = HtmlUnitClient.getSingletonHtmlUntiClent();
    private Hashtable clientSet = new Hashtable();

    public WanfangdataSpider(String indexUrl, int crawHtmlthreadCount, String pkulawSpider_crawJobCollection, String pkulawSpider_lawCollection) {
        super(indexUrl, crawHtmlthreadCount, pkulawSpider_crawJobCollection, pkulawSpider_lawCollection);
        clientSet.put(HtmlUnitClient.getNewHtmlUnitClient(0), false);
        clientSet.put(HtmlUnitClient.getNewHtmlUnitClient(1), false);
        clientSet.put(HtmlUnitClient.getNewHtmlUnitClient(2), false);
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

    public WebClient getUnusedClient() {
        Iterator iter = clientSet.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (!(boolean) entry.getValue()) {
                clientSet.put((WebClient) entry.getKey(), true);
                return (WebClient) entry.getKey();
            }
        }
        return null;
    }

    public void closeClient(WebClient currentClient) {
        this.clientSet.put(currentClient, false);
    }

    public void crawOneSoureceUrlField(String xpath) {
        try {
            HtmlPage page = client.getPage("http://s.wanfangdata.com.cn/Claw.aspx?f=claw.Cateogory&q=DBID%3aDFFG&p=17919");
            Thread.sleep(3000);
            HtmlDivision content = getContentPage(page);
            this.crawUrl("地方法规规章", content);

//            List<HtmlElement> anchoresNodes = getSoureceUrlField(client, xpath);
//            LOGGER.info("Get source filed url count:" + anchoresNodes.size());
//            for (int m = 6; m < anchoresNodes.size(); m++) {
//                try {
//                    HtmlAnchor anchor = (HtmlAnchor) anchoresNodes.get(m);
//                    if (anchor.asText().trim().contains("合同范本")){
//                        continue;
//                    }
//                    if (anchor.asText().trim().contains("法律文书样式")){
//                        continue;
//                    }
//                    HtmlPage clickPage = anchor.click();
//                    Thread.sleep(3000);
//                    HtmlDivision content = getContentPage(clickPage);
//                    this.crawUrl(anchor.asText(), content);
//                } catch (Exception e) {
//                    LOGGER.error("Get content error:" + e.getMessage());
//                }
//            }
        } catch (Exception e) {
            LOGGER.error("get page error: " + e.getMessage());
        } finally {
            client.close();
        }
    }

    //获取爬取内容的页面
    public HtmlDivision getContentPage(HtmlPage nextClickPage) {
        HtmlDivision content = null;
        try {
            content = (HtmlDivision) nextClickPage.getByXPath("/html/body/div[2]/div[2]/div[2]").get(0);
            return content;
        } catch (Exception e) {
            LOGGER.error("Get content page error: " + e.getMessage());
            return null;
        }
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
        HtmlPage page = null;
        WebClient currentClient = getUnusedClient();
        try {
            page = currentClient.getPage(htmlUrl);
            Thread.sleep(3000);
        } catch (Exception e) {
            LOGGER.error("Get parseLawHtml page error: " + e.getMessage());
            closeClient(currentClient);
            return null;
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            LOGGER.error("wait thread interrupt error: " + e.getMessage());
        }
        LawDocument doc = parseLawHtmlGetDocument(page);
        HtmlAnchor contentAnchor = null;
        try {
            contentAnchor = (HtmlAnchor) page.getByXPath("//*[@id=\"div_a\"]/div/div[2]/div[1]/div[3]/a[2]").get(0);
        } catch (NullPointerException e) {
            LOGGER.error("Not find content anchor");
            page.cleanUp();
            closeClient(currentClient);
            return doc;
        }
        if (contentAnchor != null) {
            HtmlPage contentPage = null;
            try {
                contentPage = contentAnchor.click();
                Thread.sleep(3000);
                String html = contentPage.asXml();
                doc.setRawHtml(html);
                String cleanContent = cleanHtml(html);
                doc.setCleanHtml(cleanContent);
                List<LawArticle> articleList = getLawArticleAndParagraph(cleanContent);
                doc.setArticle(articleList);
            } catch (Exception e) {
                LOGGER.error("pase html content error: " + e.getMessage());
            } finally {
                if (contentPage != null) {
                    contentPage.cleanUp();
                }
                page.cleanUp();
                closeClient(currentClient);
            }
        } else {
            closeClient(currentClient);
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
            String title = doc.title();
            lawDocument.setTitle(title.trim());
        } catch (Exception e) {
            LOGGER.warn("No tile of content");
        }
        List<Node> nodes = doc.select("#div_a > div > div.left_con > div.left_con_top > ul").first().childNodes();
        for (Node node : nodes) {
            if (node.childNodes().size() != 5) {
                continue;
            }
            String name = node.childNode(1).toString();
            String attribute = node.childNode(3).childNode(0).toString().replaceAll("\n", "").trim();
            if (name.contains("发文文号")) {
                lawDocument.setRelease_number(attribute);
            }
            if (name.contains("颁布部门") || name.contains("终审法院")) {
                lawDocument.setDepartment(attribute);
            }
            if (name.contains("效力级别")) {
                lawDocument.setLevel(attribute);
            }
            if (name.contains("时效性")) {
                lawDocument.setTimeless(attribute);
            }
            if (name.contains("颁布日期") || name.contains("终审日期")) {
                lawDocument.setRelease_data(attribute);
            }
            if (name.contains("实施日期") || name.contains("终审日期")) {
                lawDocument.setImplement_date(attribute);
            }
            if (name.contains("内容分类")) {
                lawDocument.setCategory(attribute);
            }
            if (name.contains("库别名称")) {
                lawDocument.setLevel(attribute);
            }
        }
        return lawDocument;
    }

    public void deepCrawContentUrl(String categoryName, DomNodeList<HtmlElement> clickAnchorNodes) {
        String clickPageHtml = "";
        DomNodeList<HtmlElement> currentClickAnchorNodes = clickAnchorNodes;
        int count = 0;
        int page = 17919;
        //遍历当前页
        do {
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
                HtmlPage nextClickPage = null;
                try {
                    nextClickPage = nextPageAnchor.click();
                    Thread.sleep(1300);
                    HtmlDivision content = getContentPage(nextClickPage);
                    if (content == null) {
                        Thread.sleep(2000);
                        content = getContentPage(nextClickPage);
                    }
                    if (content != null) {
                        if (content.asXml().equals(clickPageHtml)) {
                            break;
                        }
                        clickPageHtml = content.asXml();
                        currentClickAnchorNodes = content.getElementsByTagName("a");
                    } else {
                        LOGGER.error("Connect server wait 4 seconds no response content!");
                        break;
                    }
                } catch (Exception e) {
                    LOGGER.error("Deep craw content error: " + e.getMessage());
                    break;
                } finally {
                    if (nextClickPage != null) {
                        nextClickPage.cleanUp();
                    }
                }
            }
        } while (true);
    }
}