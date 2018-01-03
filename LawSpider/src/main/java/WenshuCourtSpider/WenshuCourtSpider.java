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
                .header("Cookie", "COURTID=p9didu5o5g9nncl06uv2p9u4h4; _gscs_125736681=t11844468nu6xjr19|pv:1; _gscbrs_125736681=1; _gscu_125736681=11161573uztglt17; Hm_lvt_9e03c161142422698f5b0d82bf699727=1511161574,1511353052,1511524853,1511840275; Hm_lpvt_9e03c161142422698f5b0d82bf699727=1511845363")
                .header("Host", "www.court.gov.cn")
                .header("Referer", htmlUrl)
                .header("Cache-Control", "max-age=0")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36")
                .timeout(8000)
                .get();
    }

    @Override
    public HtmlElement getContentPage(HtmlPage nextClickPage) {
        HtmlElement content = null;
        try {
            content = (HtmlElement) nextClickPage.getByXPath("//*[@id=\"container\"]/div/div[3]/div[2]").get(0);
        } catch (Exception e) {
            LOGGER.error("Get content page error: " + e.getMessage());
        }
        return content;
    }

    public HtmlElement getNextPageContent(HtmlPage nextClickPage) {
        HtmlElement content = null;
        try {
            content = (HtmlElement) nextClickPage.getByXPath("//*[@id=\"container\"]/div/div[3]/div[3]").get(0);
        } catch (Exception e) {
            LOGGER.error("Get content page error: " + e.getMessage());
        }
        return content;
    }

    public HtmlAnchor getNextAnchor(HtmlDivision content) {
        DomNodeList<HtmlElement> currentClickAnchorNodes = content.getElementsByTagName("a");
        System.out.println(currentClickAnchorNodes.size());
        for (int i = 0; i < currentClickAnchorNodes.size(); i++) {
            HtmlAnchor contentAnchor = (HtmlAnchor) currentClickAnchorNodes.get(i);
            if (contentAnchor.asText().trim().equals("下一页")) {
                return contentAnchor;
            }
        }
        return null;
    }

    public void clickPageCrawUrl(HtmlAnchor anchor, HtmlPage page) {
        HtmlDivision clickContent = (HtmlDivision) getNextPageContent(page);
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
                if (contentAnchor.getHrefAttribute().contains("downloadPdf")) {
                    continue;
                }
                if (contentAnchor.getHrefAttribute().isEmpty()) {
                    continue;
                }
                count++;
                if (this.addUrl(categoryName, "http://www.court.gov.cn" + contentAnchor.getHrefAttribute())) {
                    LOGGER.info("Sava success url:[" + categoryName + "][" + pagenum + "][" + count + "]" + "http://www.court.gov.cn" + contentAnchor.getHrefAttribute());
                } else {
                    LOGGER.info("alerady exits url:[" + categoryName + "][" + pagenum + "][" + count + "]" + "http://www.court.gov.cn" + contentAnchor.getHrefAttribute());
                }
            }
            HtmlAnchor nextPageAnchor = getNextAnchor(clickContent);
            if (nextPageAnchor == null) {
                break;
            } else {
                pagenum++;
                count = 0;
                try {
                    HtmlPage nextClickPage = nextPageAnchor.click();
                    Thread.sleep(getRandomWaitTime(2000, 5000));
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
    public LawDocument parseLawHtml(String htmlUrl) {
        Document doc = null;
        try {
            doc = this.getJsoupConnection(htmlUrl);
        } catch (IOException e) {
            LOGGER.warn("Jsoup get html error" + e.getMessage());
        }
        LawDocument lawDocument = new LawDocument();
        try {
            lawDocument.setRawHtml(doc.html());
            try {
                String title = doc.select("#container > div > div > div.clearfix.detail_mes > ul > li.fl.print").first().childNode(0).toString();
                lawDocument.setRelease_data(title.trim().replace("&nbsp;", ""));
            } catch (NullPointerException e) {
                LOGGER.warn("No release_data of content");
            }
            try {
                String title = doc.select("#container > div > div > div.title").first().childNode(0).toString();
                lawDocument.setTitle(title.trim().replace("&nbsp;", ""));
            } catch (NullPointerException e) {
                LOGGER.warn("No tile of content");
            }
            String html = doc.select("#zoom").first().html().replaceAll("&nbsp;", "");
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
        System.out.println(page.asXml());
        //获取局部source url
        HtmlDivision sourceFiled = (HtmlDivision) page.getByXPath(xpath).get(1);
        System.out.println(sourceFiled.asXml());
        DomNodeList<HtmlElement> anchoresNodes = sourceFiled.getElementsByTagName("a");
        System.out.println(anchoresNodes.size());
        //去除页面上不需要的anchor
        for (int i = 0; i < anchoresNodes.size(); i++) {
            HtmlAnchor tempAnchor = (HtmlAnchor) anchoresNodes.get(i);
            if (tempAnchor.asText().trim().equals("裁判文书")) {
                anchorsList.add(tempAnchor);
            }
            if (tempAnchor.asText().trim().equals("刑事案件")) {
                anchorsList.add(tempAnchor);
            }
            if (tempAnchor.asText().trim().equals("民事案件")) {
                anchorsList.add(tempAnchor);
            }
            if (tempAnchor.asText().trim().equals("行政案件")) {
                anchorsList.add(tempAnchor);
            }
            if (tempAnchor.asText().trim().equals("赔偿案件")) {
                anchorsList.add(tempAnchor);
            }
            if (tempAnchor.asText().trim().equals("执行案件")) {
                anchorsList.add(tempAnchor);
            }
        }
        return anchorsList;
    }
}
