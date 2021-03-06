package PkulawSpider;

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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author : Administrator
 * @Date : 2017/11/10 17:21
 * @Description :
 */
public class PkulawSpider extends LawSpider {
    private static Logger LOGGER = LawLogger.getLawLogger(PkulawSpider.class);

    public PkulawSpider(String indexUrl, int crawHtmlthreadCount, String pkulawSpider_crawJobCollection, String pkulawSpider_lawCollection) {
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
            //等待5秒后获取页面
            Thread.sleep(3000);
            //获取局部source url
            HtmlTableDataCell sourceFiled = (HtmlTableDataCell) page.getByXPath(xpath).get(0);
            //展开全部
            DomNodeList<HtmlElement> anchoresExpandAllNodes = sourceFiled.getElementsByTagName("a");
            HtmlAnchor expandAnchor = null;
            for (int i = 0; i < anchoresExpandAllNodes.size(); i++) {
                if (anchoresExpandAllNodes.get(i).asText().trim().contains("展开全部")) {
                    expandAnchor = (HtmlAnchor) anchoresExpandAllNodes.get(i);
                    break;
                }
            }
            if (expandAnchor != null) {
                page = expandAnchor.click();
                Thread.sleep(3000);
            }
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
                Thread.sleep(5000);
                HtmlTableDataCell content = getContentPage(clickPage);
                this.crawUrl(anchor.asText(), content);
            } catch (Exception e) {
                LOGGER.error("Get content error:" + e.getMessage());
            }
        }
    }

    //获取爬取内容的页面
    public HtmlTableDataCell getContentPage(HtmlPage nextClickPage) {
        HtmlTableDataCell content = null;
        try {
            content = (HtmlTableDataCell) nextClickPage.getByXPath("//*[@id=\"td_jianbian\"]").get(0);
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
            HtmlTableDataCell sourceFiled = (HtmlTableDataCell) page.getByXPath(xpath).get(0);
            //展开全部
            DomNodeList<HtmlElement> anchoresNodes = sourceFiled.getElementsByTagName("a");
            for (int i = 0; i < anchoresNodes.size(); i++) {
                HtmlAnchor tempAnchor = (HtmlAnchor) anchoresNodes.get(i);
                if (tempAnchor.getAttribute("class").trim().equals("dian1-0")) {
                    anchorsList.add(tempAnchor);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("get sourece filed url InterruptedException: " + e.getMessage());
        }
        return anchorsList;
    }

    //从获取主页上分类的url
    public LawDocument parseLawHtml(Document doc) {
        LawDocument lawDocument = new LawDocument();
        try {
            lawDocument.setRawHtml(doc.html());
            String title = doc.select("#tbl_content_main > tbody > tr:nth-child(1) > td > span > strong").first().childNode(0).toString();
            String regEx_delet = "\\(.*北大法宝.*\\)";//定义空格回车换行符
            Pattern p_delete = Pattern.compile(regEx_delet, Pattern.CASE_INSENSITIVE);
            Matcher m_delete = p_delete.matcher(title);
            title = m_delete.replaceAll("");
            lawDocument.setTitle(title);
        } catch (NullPointerException e) {
            LOGGER.warn("no tile of law");
        }
        try {
            String department = doc.select("#tbl_content_main > tbody > tr:nth-child(2) > td:nth-child(1) > a").first().childNode(0).toString();
            lawDocument.setDepartment(department);
        } catch (NullPointerException e) {
            LOGGER.warn("no department of law");
        }
        try {
            String implement_date = doc.select("#tbl_content_main > tbody > tr:nth-child(3) > td:nth-child(2)").first().childNode(1).toString();
            lawDocument.setImplement_date(implement_date);
        } catch (NullPointerException e) {
            LOGGER.warn("no implement_date of law");
        }
        try {
            String relase_date = doc.select("#tbl_content_main > tbody > tr:nth-child(3) > td:nth-child(1)").first().childNode(1).toString();
            lawDocument.setRelease_data(relase_date);
        } catch (NullPointerException e) {
            LOGGER.warn("no relase_date of law");
        }
        try {
            String level = doc.select("#tbl_content_main > tbody > tr:nth-child(5) > td").first().text();
            lawDocument.setLevel(level);
        } catch (NullPointerException e) {
            LOGGER.warn("no level of law");
        }
        try {
            String timeless = doc.select("#tbl_content_main > tbody > tr:nth-child(4) > td:nth-child(1) > a").first().childNode(0).toString();
            lawDocument.setTimeless(timeless);
        } catch (NullPointerException e) {
            LOGGER.warn("no timeless of law");
        }
        try {
            String category = doc.select("#tbl_content_main > tbody > tr:nth-child(5) > td > a").first().childNode(0).toString();
            lawDocument.setCategory(category);
        } catch (NullPointerException e) {
            LOGGER.warn("no category of law");
        }
        try {
            Elements eles = doc.getElementsByClass("TiaoNoA");
            lawDocument.setTiaoNum(eles.toArray().length);
        } catch (NullPointerException e) {
            lawDocument.setTiaoNum(0);
            LOGGER.warn("no TiaoNoA of law");
        }
        try {
            doc.select(".TiaoYinV2").remove();
            String html = doc.select("#div_content").first().html();
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
                if (contentAnchor.getAttribute("class").trim().equals("main-ljwenzi")) {
                    count++;
                    if (this.addUrl(categoryName, getIndexUrl() + "/" + contentAnchor.getHrefAttribute())) {
                        LOGGER.info("Sava success url:[" + categoryName + "][" + page + "][" + count + "]" + getIndexUrl() + "/" + contentAnchor.getHrefAttribute());
                    } else {
                        LOGGER.info("alerady exits url:[" + categoryName + "][" + page + "][" + count + "]" + getIndexUrl() + "/" + contentAnchor.getHrefAttribute());
                    }
                }
                if (contentAnchor.asText().trim().contains("下一页")) {
                    nextPageAnchor = contentAnchor;
                    hasNextPage = true;
                }
            }
            if (nextPageAnchor == null) {
                hasNextPage = false;
            }
            //如果有下一页点击下一页
            if (hasNextPage) {
                page++;
                count = 0;
                try {
                    HtmlPage nextClickPage = nextPageAnchor.click();
                    Thread.sleep(3000);
                    HtmlTableDataCell content = getContentPage(nextClickPage);
                    if (content.asXml().trim().equals(clickPageHtml)) {
                        break;
                    }
                    clickPageHtml = content.asText().trim();
                    currentClickAnchorNodes = content.getElementsByTagName("a");
                } catch (Exception e) {
                    LOGGER.error("Deep craw content error: " + e.getMessage());
                }

            }
        } while (hasNextPage);
    }

    public Document getJsoupConnection(String htmlUrl) throws IOException {
        Document doc = Jsoup.connect(htmlUrl)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Host", getIndexUrl())
                .header("Connection", "keep-alive")
                .header("Cache-Control", "max-age=0")
                .header("Referer", htmlUrl)
                .header("Cookie", "ASP.NET_SessionId=xj3z4sf5qzljnxibhdce3sim; bdyh_record=1970324846989066%2C1970324846989064%2C1970324845702078%2C1970324845892060%2C1970324845892065%2C1970324845702079%2C1970324846989065%2C1970324846989063%2C; CheckIPAuto=0; CheckIPDate=2017-11-15 21:44:03; xj3z4sf5qzljnxibhdce3sim3isIPlogin=1; click0=2017/11/14 22:51:31; click1=2017/11/14 22:51:31; click2=2017/11/15 21:45:18; click3=2017/11/15 22:10:44; Hm_lvt_58c470ff9657d300e66c7f33590e53a8=1510045402,1510119472,1510301767; Hm_lpvt_58c470ff9657d300e66c7f33590e53a8=1510755043; CookieId=xj3z4sf5qzljnxibhdce3sim; User_User=%d6%d0%b9%fa%c8%cb%c3%f1%b4%f3%d1%a7; FWinCookie=1")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36")
                .timeout(50000)
                .get();
        return doc;
    }
}
