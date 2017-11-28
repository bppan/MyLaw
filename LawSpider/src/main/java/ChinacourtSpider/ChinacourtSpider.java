package ChinacourtSpider;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

///**
// * @Author : Administrator
// * @Date : 2017/11/10 17:21
// * @Description :
// */

public class ChinacourtSpider extends LawSpider {
    private static Logger LOGGER = LawLogger.getLawLogger(ChinacourtSpider.class); //日志类

    public ChinacourtSpider(String indexUrl, int crawHtmlthreadCount, String chinacourt_crawCollection, String chinacourt_lawCollection) {
        super(indexUrl, crawHtmlthreadCount, chinacourt_crawCollection, chinacourt_lawCollection);
    }

    @Override
    public Document getJsoupConnection(String htmlUrl) throws IOException {
        return Jsoup.connect(htmlUrl)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Host", "www.chinacourt.org")
                .header("Connection", "keep-alive")
                .header("Cache-Control", "max-age=0")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36")
                .timeout(50000)
                .get();
    }

    @Override
    public HtmlElement getContentPage(HtmlPage nextClickPage) {
        HtmlElement content = null;
        try {
            content = (HtmlElement) nextClickPage.getByXPath("//*[@id=\"flwk\"]/div[1]/div[2]").get(0);
        } catch (Exception e) {
            LOGGER.error("Get content page error: " + e.getMessage());
        }
        return content;
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
                Thread.sleep(5000);
                HtmlDivision content = (HtmlDivision) getContentPage(clickPage);
                this.crawUrl(anchor.asText(), content);
            } catch (Exception e) {
                LOGGER.error("Get content error:" + e.getMessage());
            }
        }
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
        return false;
    }

    @Override
    public void deepCrawContentUrl(String categoryName, DomNodeList<HtmlElement> clickAnchorNodes) {
        String clickPageHtml = "";
        DomNodeList<HtmlElement> currentClickAnchorNodes = clickAnchorNodes;
        boolean hasNextPage = false;
        int count = 0;
        int page = 0;
        do {
            //遍历当前页
            HtmlAnchor nextPageAnchor = null;
            for (int i = 0; i < currentClickAnchorNodes.size(); i++) {
                HtmlAnchor contentAnchor = (HtmlAnchor) currentClickAnchorNodes.get(i);
                if (!isPageNext(contentAnchor.asText())) {
                    count++;
                    if (this.addUrl(categoryName, "http://www.chinacourt.org" + contentAnchor.getHrefAttribute())) {
                        LOGGER.info("Sava success url:[" + categoryName + "][" + page + "][" + count + "]" + "http://www.chinacourt.org" + contentAnchor.getHrefAttribute());
                    } else {
                        LOGGER.info("alerady exits url:[" + categoryName + "][" + page + "][" + count + "]" + "http://www.chinacourt.org" + contentAnchor.getHrefAttribute());
                    }
                }
                if (contentAnchor.asText().trim().contains("下一页")) {
                    nextPageAnchor = contentAnchor;
                    hasNextPage = true;
                }
            }
            if (nextPageAnchor == null) {
                hasNextPage = false;
            } else {
                page++;
                count = 0;
                try {
                    HtmlPage nextClickPage = nextPageAnchor.click();
                    Thread.sleep(3000);
                    HtmlDivision content = (HtmlDivision) getContentPage(nextClickPage);
                    if (content.asXml().trim().equals(clickPageHtml)) {
                        break;
                    }
                    clickPageHtml = content.asXml().trim();
                    currentClickAnchorNodes = content.getElementsByTagName("a");
                } catch (Exception e) {
                    LOGGER.error("Deep craw content error: " + e.getMessage());
                }
            }
        } while (hasNextPage);
    }

    @Override
    public LawDocument parseLawHtml(String htmlUrl) {
        Document doc = null;
        try {
            doc = this.getJsoupConnection(htmlUrl);
        }catch (IOException e){
            LOGGER.warn("Jsoup get html error"+ e.getMessage());
        }

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
        DomNodeList<HtmlElement> clickAnchorNodes = content.getElementsByTagName("a");
        deepCrawContentUrl(categoryName, clickAnchorNodes);
    }

    @Override
    public HtmlPage getSoureUrlPage(WebClient client, String xpath) {
        HtmlPage page = null;
        try {
            page = client.getPage(getIndexUrl());
            //等待5秒后获取页面
            Thread.sleep(3000);
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
        HtmlUnorderedList sourceFiled = (HtmlUnorderedList) page.getByXPath(xpath).get(0);
        //展开全部
        DomNodeList<HtmlElement> anchoresNodes = sourceFiled.getElementsByTagName("a");
        //去除页面上立法追踪
        for (int i = 1; i < anchoresNodes.size(); i++) {
            HtmlAnchor tempAnchor = (HtmlAnchor) anchoresNodes.get(i);
            System.out.println(tempAnchor.asText());
            anchorsList.add(tempAnchor);
        }
        return anchorsList;
    }
}
