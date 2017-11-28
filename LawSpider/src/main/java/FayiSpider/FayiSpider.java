package FayiSpider;

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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/11/28 15:20
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class FayiSpider extends LawSpider {
    private static Logger LOGGER = LawLogger.getLawLogger(FayiSpider.class);

    public FayiSpider(String indexUrl, int crawHtmlthreadCount, String pkulawSpider_crawJobCollection, String pkulawSpider_lawCollection) {
        super(indexUrl, crawHtmlthreadCount, pkulawSpider_crawJobCollection, pkulawSpider_lawCollection);
    }

    public void crawUrl(String categoryName, HtmlElement content) {
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

    public HtmlDivision getNextPageContent(HtmlPage nextClickPage) {
        HtmlDivision content = null;
        try {
            content = (HtmlDivision) nextClickPage.getByXPath("/html/body/div[4]/div[2]/div[3]/div[2]").get(0);
        } catch (Exception e) {
            LOGGER.error("Get content page error: " + e.getMessage());
        }
        return content;
    }

    public void crawUrl(String categoryName, HtmlPage page) {
        HtmlDivision clickContent = getNextPageContent(page);
        HtmlUnorderedList content = getHtmlContentPage(page);
        DomNodeList<HtmlElement> clickAnchorNodes = content.getElementsByTagName("a");

        String clickPageHtml = "";
        int count = 0;
        int pageNum = 0;
        do {
            //遍历当前页
            for (int i = 0; i < clickAnchorNodes.size(); i++) {
                HtmlAnchor contentAnchor = (HtmlAnchor) clickAnchorNodes.get(i);
                if (contentAnchor.getHrefAttribute().isEmpty()) {
                    continue;
                }
                count++;
                if (this.addUrl(categoryName, getIndexUrl() + contentAnchor.getHrefAttribute())) {
                    LOGGER.info("Sava success url:[" + categoryName + "][" + pageNum + "][" + count + "]" + getIndexUrl() + contentAnchor.getHrefAttribute());
                } else {
                    LOGGER.info("alerady exits url:[" + categoryName + "][" + pageNum + "][" + count + "]" + getIndexUrl() + contentAnchor.getHrefAttribute());
                }
            }
            HtmlAnchor nextPageAnchor = getNextAnchor(clickContent);
            if (nextPageAnchor == null) {
                break;
            } else {
                pageNum++;
                count = 0;
                try {
                    HtmlPage nextClickPage = nextPageAnchor.click();
                    Thread.sleep(getRandomWaitTime(2000, 5000));
                    HtmlUnorderedList clickfinshedContent = getHtmlContentPage(nextClickPage);
                    if (clickfinshedContent.asText().equals(clickPageHtml)) {
                        break;
                    } else {
                        clickPageHtml = clickfinshedContent.asText();
                    }

                    clickAnchorNodes = clickfinshedContent.getElementsByTagName("a");
                    clickContent = getNextPageContent(nextClickPage);
                } catch (Exception e) {
                    LOGGER.error("Deep craw content error: " + e.getMessage());
                }
            }
        } while (true);
    }

    public HtmlPage getSoureUrlPage(WebClient client, String xpath) {
        return null;
    }

    public void crawOneSoureceUrlField(String xpath) {
        WebClient client = HtmlUnitClient.getSingletonHtmlUntiClent();
        try {
            HtmlPage page = client.getPage(getIndexUrl());
            //等待5秒后获取页面
            Thread.sleep(3000);
            HtmlElement category = (HtmlElement) page.getByXPath("/html/body/div[4]/div[2]/div[3]/h3/span/em").get(0);
            this.crawUrl(category.asText(), page);
        } catch (IOException e) {
            LOGGER.error("Get Html page error:" + e.getMessage());
        } catch (InterruptedException e) {
            LOGGER.error("Sleep thread error:" + e.getMessage());
        }

    }

    //获取爬取内容的页面
    public HtmlTableDataCell getContentPage(HtmlPage nextClickPage) {
        return null;
    }

    public HtmlUnorderedList getHtmlContentPage(HtmlPage Page) {
        HtmlUnorderedList content = null;
        try {
            content = (HtmlUnorderedList) Page.getByXPath("/html/body/div[4]/div[2]/div[3]/ul").get(0);
        } catch (Exception e) {
            LOGGER.error("Get content page error: " + e.getMessage());
        }
        return content;
    }


    //获取带爬取的url区域
    public List<HtmlElement> getSoureceUrlField(WebClient client, String xpath) {
        return null;
    }

    //从获取主页上分类的url
    public LawDocument parseLawHtml(String htmlUrl) {
        Document doc = null;
        try {
            doc = this.getJsoupConnection(htmlUrl);
        } catch (IOException e) {
            LOGGER.error("Jsoup parse html error" + e.getMessage());
        }
        LawDocument lawDocument = new LawDocument();
        try {
            lawDocument.setRawHtml(doc.html());
        } catch (NullPointerException e) {
            LOGGER.warn("no raw html of law");
        }
        try {
            String title = doc.select("body > div.wrapper.mtop10 > div.content > div.article.mtop10 > div.article_content > h1 > span > font").first().childNode(0).toString();
            lawDocument.setTitle(title);
        } catch (NullPointerException e) {
            LOGGER.warn("no title of law");
        }
        Elements elements = doc.select("#fontzoom > p.bh");
        for (Element ele : elements) {
            String tag = ele.childNode(0).childNode(0).toString();
            if (tag.trim().equals("发文单位")) {
                String content = ele.childNode(1).toString().replace("：", "");
                lawDocument.setDepartment(content);
            }
            if (tag.trim().equals("发布日期")) {
                String content = ele.childNode(1).toString().replace("：", "");
                lawDocument.setRelease_data(content);
            }
            if (tag.trim().equals("执行日期")) {
                String content = ele.childNode(1).toString().replace("：", "");
                lawDocument.setImplement_date(content);
            }
            if (tag.trim().equals("文　　号")) {
                String content = ele.childNode(1).toString().replace("：", "");
                lawDocument.setRelease_number(content);
            }
        }
        try {
            String html = doc.select("#articleContnet").first().html();
            String cleanHtmlContent = cleanHtml(html);
            lawDocument.setCleanHtml(cleanHtmlContent);
            List<LawArticle> articleList = getLawArticleAndParagraph(cleanHtmlContent);
            lawDocument.setArticle(articleList);
        } catch (Exception e) {
            LOGGER.error("Get article error...");
            LOGGER.error(e);
        }
        return lawDocument;
    }

    public void deepCrawContentUrl(String categoryName, DomNodeList<HtmlElement> clickAnchorNodes) {
    }

    public Document getJsoupConnection(String htmlUrl) throws IOException {
        Document doc = Jsoup.connect(htmlUrl)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Host", "law.fayi.com.cn")
                .header("Connection", "keep-alive")
                .header("Cache-Control", "max-age=0")
                .header("Referer", htmlUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36")
                .timeout(50000)
                .get();
        return doc;
    }
}