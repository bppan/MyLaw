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
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
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
            String title = doc.select("#tbl_content_main > tbody > tr:nth-child(1) > td > span > strong").first().childNode(0).toString();
            String regEx_delet = "\\(.*北大法宝.*\\)";//定义空格回车换行符
            Pattern p_delete = Pattern.compile(regEx_delet, Pattern.CASE_INSENSITIVE);
            Matcher m_delete = p_delete.matcher(title);
            title = m_delete.replaceAll("");
            lawDocument.setTitle(title);
        } catch (NullPointerException e) {
            LOGGER.warn("no tile of law");
        }
        Element elementTbody = doc.select("#tbl_content_main > tbody").first();
        Elements elements = elementTbody.getElementsByTag("tr");
        for (Element element : elements) {
            List<Node> nodes = element.childNodes();
            if (nodes.size() == 0) {
                continue;
            }
            if (isRightAttribute(nodes, "【发布部门】")) {
                String department = getContentAttribute(nodes, "【发布部门】");
                lawDocument.setDepartment(department);
            }
            if (isRightAttribute(nodes, "【发文字号】")) {
                String release_number = getContentAttribute(nodes, "【发文字号】");
                lawDocument.setRelease_number(release_number);
            }
            if (isRightAttribute(nodes, "【发布日期】")) {
                String release_date = getContentAttribute(nodes, "【发布日期】");
                lawDocument.setRelease_data(release_date);
            }
            if (isRightAttribute(nodes, "【实施日期】")) {
                String implement_date = getContentAttribute(nodes, "【实施日期】");
                lawDocument.setImplement_date(implement_date);
            }
            if (isRightAttribute(nodes, "【时效性】")) {
                String timeless = getContentAttribute(nodes, "【时效性】");
                lawDocument.setTimeless(timeless);
            }
            if (isRightAttribute(nodes, "【法规类别】")) {
                String category = getContentAttribute(nodes, "【法规类别】");
                lawDocument.setCategory(category);
            }
            if (isRightAttribute(nodes, "【效力级别】")) {
                String level = getContentAttribute(nodes, "【效力级别】");
                lawDocument.setLevel(level);
            }
        }
        try {
            doc.select("#div_content > font").first().remove();
            doc.select(".TiaoYinV2").remove();
            doc.select(".TiaoYin").remove();
            String html = doc.select("#div_content").first().html();
            String cleanHtmlContent = cleanHtml(html);
            lawDocument.setCleanHtml(cleanHtmlContent);
            List<LawArticle> articleList = getLawArticleAndParagraph(cleanHtmlContent);
            lawDocument.setArticle(articleList);
            lawDocument.setTiaoNum(articleList.size());
        } catch (Exception e) {
            LOGGER.error("Get article error...");
            LOGGER.error(e);
        }
        try {
            Thread.sleep(getRandomWaitTime(1000, 2000));
        } catch (InterruptedException e) {
            LOGGER.error("wait thread error: " + e.getMessage());
        }
        return lawDocument;
    }

    private boolean isRightAttribute(List<Node> nodes, String tag) {
        for (Node node : nodes) {
            String result = node.toString();
            if (result.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private String getContentAttribute(List<Node> nodes, String tag) {
        for (Node node : nodes) {
            String result = node.toString();
            String regEx2_html = "<[^>]+>"; // 定义HTML标签的正则表达式
            Pattern p2_html = Pattern.compile(regEx2_html, Pattern.CASE_INSENSITIVE);
            Matcher m2_html = p2_html.matcher(result);
            result = m2_html.replaceAll("").replaceAll(" ", "").trim(); // 过滤html标签
            if (result.contains(tag)) {
                return result.replace(tag, "").trim();
            }
        }
        return "";
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
                .header("Host", "www.pkulaw.cn")
                .header("Connection", "keep-alive")
                .header("Cache-Control", "max-age=0")
                .header("Referer", htmlUrl)
                .header("Cookie", "FWinCookie=1; ASP.NET_SessionId=4ac2zisbbniegmcv3bdjvgla; Hm_lvt_58c470ff9657d300e66c7f33590e53a8=1513927410,1513952533,1514297277,1514384451; click0=2017/12/30 15:02:24; CookieId=4ac2zisbbniegmcv3bdjvgla; CheckIPAuto=0; CheckIPDate=2017-12-30 15:02:07; 4ac2zisbbniegmcv3bdjvgla3isIPlogin=1; User_User=%d6%d0%b9%fa%c8%cb%c3%f1%b4%f3%d1%a7; Hm_lpvt_58c470ff9657d300e66c7f33590e53a8=1514618165")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36")
                .timeout(50000)
                .get();
        return doc;
    }
}
