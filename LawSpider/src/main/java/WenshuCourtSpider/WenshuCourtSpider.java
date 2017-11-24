package WenshuCourtSpider;

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
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2017/11/24 15:43
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */

public class WenshuCourtSpider extends LawSpider {
    private static Logger LOGGER = LawLogger.getLawLogger(WenshuCourtSpider.class); //日志类

    public WenshuCourtSpider(String indexUrl, int crawHtmlthreadCount, String chinacourt_crawCollection, String chinacourt_lawCollection) {
        super(indexUrl, crawHtmlthreadCount, chinacourt_crawCollection, chinacourt_lawCollection);
    }

    @Override
    public Document getJsoupConnection(String htmlUrl) throws IOException {
        return Jsoup.connect(htmlUrl)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Host", "wenshu.court.gov.cn")
                .header("Connection", "keep-alive")
                .header("Cache-Control", "max-age=0")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36")
                .timeout(8000)
                .get();
    }

    @Override
    public HtmlElement getContentPage(HtmlPage nextClickPage) {
        HtmlElement content = null;
        try {
            content = (HtmlElement) nextClickPage.getByXPath("//*[@id=\"resultList\"]").get(0);
        } catch (Exception e) {
            LOGGER.error("Get content page error: " + e.getMessage());
        }
        return content;
    }

    public HtmlElement getNextPageContent(HtmlPage nextClickPage){
        HtmlElement content = null;
        try {
            content = (HtmlElement) nextClickPage.getByXPath("//*[@id=\"pageNumber\"]").get(0);
        } catch (Exception e) {
            LOGGER.error("Get content page error: " + e.getMessage());
        }
        return content;
    }

    public HtmlAnchor getNextAnchor(HtmlDivision content){
        DomNodeList<HtmlElement> currentClickAnchorNodes = content.getElementsByTagName("a");
        System.out.println(currentClickAnchorNodes.size());
        for (int i = 0; i < currentClickAnchorNodes.size(); i++){
            HtmlAnchor contentAnchor = (HtmlAnchor) currentClickAnchorNodes.get(i);
            if(contentAnchor.asText().trim().equals("下一页")){
                return contentAnchor;
            }
        }
        return null;
    }

    public void clickPageCrawUrl(HtmlAnchor anchor, HtmlPage page){

        HtmlDivision clickContent = (HtmlDivision) getNextPageContent(page);
        getNextAnchor(clickContent);

        HtmlDivision reulstContent = (HtmlDivision) getContentPage(page);
        String categoryName = anchor.asText();
        String clickPageHtml = "";
        DomNodeList<HtmlElement> resultContentAnchorNodes = reulstContent.getElementsByTagName("a");
        int count = 0;
        int pagenum = 0;
        do {
            //遍历当前页
            for (int i = 0; i < resultContentAnchorNodes.size(); i++) {
                HtmlAnchor contentAnchor = (HtmlAnchor) resultContentAnchorNodes.get(i);
                if(contentAnchor.getAttribute("style").trim().contains("display:none")){
                    continue;
                }
                if(contentAnchor.getHrefAttribute().isEmpty()){
                    continue;
                }
                count++;
                if (this.addUrl(categoryName, getIndexUrl() + contentAnchor.getHrefAttribute())) {
                    LOGGER.info("Sava success url:[" + categoryName + "][" + pagenum + "][" + count + "]" + getIndexUrl() + contentAnchor.getHrefAttribute());
                } else {
                    LOGGER.info("alerady exits url:[" + categoryName + "][" + pagenum + "][" + count + "]" + getIndexUrl() + contentAnchor.getHrefAttribute());
                }
            }
            try {
                Thread.sleep(1000, 2000);
            }catch (InterruptedException e){
                LOGGER.error("sleep thread error: " + e.getMessage());
            }
            HtmlAnchor nextPageAnchor = getNextAnchor(clickContent);
            if (nextPageAnchor == null) {
                break;
            } else {
                pagenum++;
                count = 0;
                try {
                    HtmlPage nextClickPage = nextPageAnchor.click();
                    Thread.sleep(3000, 4000);
                    HtmlDivision clickfinshedContent = (HtmlDivision) getContentPage(nextClickPage);
                    if (clickfinshedContent.asXml().trim().equals(clickPageHtml)) {
                        break;
                    }
                    clickPageHtml = clickfinshedContent.asXml().trim();
                    resultContentAnchorNodes = clickfinshedContent.getElementsByTagName("a");
                    clickContent = (HtmlDivision) getNextPageContent(nextClickPage);
                } catch (Exception e) {
                    LOGGER.error("Deep craw content error: " + e.getMessage());
                }
            }
        } while (true);

    }
    @Override
    public void crawOneSoureceUrlField(String xpath) {
        WebClient client = HtmlUnitClient.getSingletonHtmlUntiClent();
        List<HtmlElement> anchoresNodes = getSoureceUrlField(client, xpath);
        LOGGER.info("Get source filed url count:" + anchoresNodes.size());
        for (int m = 0; m < anchoresNodes.size(); m++) {
            try {
                HtmlAnchor anchor = (HtmlAnchor) anchoresNodes.get(m);
                HtmlPage clickPage = anchor.click();
                Thread.sleep(getRandomWaitTime(3000, 5000));
                clickPageCrawUrl(anchor, clickPage);
            } catch (Exception e) {
                LOGGER.error("Get content error:" + e.getMessage());
            }
        }
    }

    @Override
    public void deepCrawContentUrl(String categoryName, DomNodeList<HtmlElement> clickAnchorNodes) {
    }

    @Override
    public LawDocument parseLawHtml(Document doc) {
        LawDocument lawDocument = new LawDocument();
        try {
            lawDocument.setRawHtml(doc.html());
            Element head = doc.select("body > div.container > div > div.law_content > span").first();
            if (head == null) {
                LOGGER.warn("No head of content");
            } else {
                for (Node node : head.childNodes()) {
                    String headContent = node.toString().trim().replace("&nbsp;", "");
                    if (headContent.contains("发布单位")) {
                        lawDocument.setDepartment(headContent.replace("【发布单位】", ""));
                    }
                    if (headContent.contains("发布日期")) {
                        lawDocument.setRelease_data(headContent.replace("【发布日期】", ""));
                    }
                    if (headContent.contains("生效日期")) {
                        lawDocument.setImplement_date(headContent.replace("【生效日期】", ""));
                    }
                    if (headContent.contains("失效日期")) {
                        lawDocument.setTimeless(headContent.replace("【失效日期】", ""));
                    }
                    if (headContent.contains("所属类别")) {
                        lawDocument.setCategory(headContent.replace("【所属类别】", ""));
                    }
                    if (headContent.contains("发布文号")) {
                        lawDocument.setRelease_number(headContent.replace("【发布文号】", ""));
                    }
                }
            }
            try {
                String title = doc.select("body > div.container > div > div.law_content > div > p:nth-child(1) > strong").first().childNode(0).toString();
                lawDocument.setTitle(title.trim().replace("&nbsp;", ""));
            } catch (NullPointerException e) {
                LOGGER.warn("No tile of content");
            }
            String html = doc.select("body > div.container > div > div.law_content").first().html().replaceAll("&nbsp;", "");
            String cleanContent = cleanHtml(html);
            lawDocument.setCleanHtml(cleanContent);
            List<LawArticle> articleList = getLawArticleAndParagraph(cleanContent);
            lawDocument.setArticle(articleList);
        } catch (Exception e) {
            LOGGER.error("parse Html error:" + e.getMessage());
        }

        return lawDocument;
    }

    @Override
    public void crawUrl(String categoryName, HtmlElement content) {
    }

    @Override
    public HtmlPage getSoureUrlPage(WebClient client, String xpath) {
        HtmlPage page = null;
        try {
            page = client.getPage(getIndexUrl());
            //等待5秒后获取页面
            Thread.sleep(getRandomWaitTime(3000, 5000));
        } catch (Exception e) {
            LOGGER.error("Get SoureUrlPage error: " + e.getMessage());
        }
        return page;
    }

    @Override
    public List<HtmlElement> getSoureceUrlField(WebClient client, String xpath) {
        List<HtmlElement> anchorsList = new ArrayList<HtmlElement>();
        HtmlPage page = getSoureUrlPage(client, xpath);
        //获取局部source url
        HtmlDivision sourceFiled = (HtmlDivision) page.getByXPath(xpath).get(0);
        DomNodeList<HtmlElement> anchoresNodes = sourceFiled.getElementsByTagName("a");
        //去除页面上不需要的anchor
        for (int i = 0; i < anchoresNodes.size(); i++) {
            HtmlAnchor tempAnchor = (HtmlAnchor) anchoresNodes.get(i);
            if(tempAnchor.asText().trim().equals("刑事案件")){
                anchorsList.add(tempAnchor);
            }
            if(tempAnchor.asText().trim().equals("民事案件")){
                anchorsList.add(tempAnchor);
            }
            if(tempAnchor.asText().trim().equals("行政案件")){
                anchorsList.add(tempAnchor);
            }
            if(tempAnchor.asText().trim().equals("赔偿案件")){
                anchorsList.add(tempAnchor);
            }
            if(tempAnchor.asText().trim().equals("执行案件")){
                anchorsList.add(tempAnchor);
            }
        }
        return anchorsList;
    }
}
