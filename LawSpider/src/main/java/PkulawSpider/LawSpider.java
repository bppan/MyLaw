package PkulawSpider;

import Interface.Spider;
import Log.LawLogger;
import Mongo.MongoDB;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author : Administrator
 * @Date : 2017/11/10 17:21
 * @Description :
 */
public class LawSpider extends Spider {
    private static final Object signal = new Object();   //线程间通信变量
    private static Logger LOGGER = LawLogger.getLawLogger(LawSpider.class);
    private final int waitToCrawNextHtmlTime = (int) (1000 + Math.random() * 5000);
    private List<org.bson.Document> urlList;
    private String indexUrl;
    private int crawHtmlthreadCount;
    private int waitCrawHtmlThreadCount;
    private MongoDB mongoDB;

    public LawSpider(String indexUrl, int crawHtmlthreadCount) {
        this.crawHtmlthreadCount = crawHtmlthreadCount;
        this.indexUrl = indexUrl;
        this.mongoDB = MongoDB.getMongoDB();
        this.waitCrawHtmlThreadCount = 0;
    }

    @Override
    public boolean addUrl(String url) {
        if (urlSet.add(url)) {
            this.urlList.add(url);
            if (this.waitCrawHtmlThreadCount > 0) {//如果有等待的线程，则唤醒
                synchronized (signal) {  //---------------------（2）
                    LOGGER.info("Wake up thread，current waiting thread num:" + (this.waitCrawHtmlThreadCount - 1));
                    this.waitCrawHtmlThreadCount--;
                    signal.notify();
                }
            }
            return true;
        }
        return false;
    }



    @Override
    public void doCraw() {
        //加载crawJobUrl
        this.urlList = mongoDB.loadAllCrawJob();
        //获取种子url对应的下一个爬取的url
        for (int i = 0; i < crawHtmlthreadCount; i++) {
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        String crawUrl = getUrl();
                        if (!crawUrl.isEmpty()) {
                            LOGGER.info(Thread.currentThread().getName() + " craw begin...");
                            long startTime = System.currentTimeMillis();
                            crawHtml(crawUrl);
                            long endTime = System.currentTimeMillis();
                            try {
                                Thread.sleep(waitToCrawNextHtmlTime);
                            } catch (InterruptedException e) {
                                LOGGER.error("waitToCrawNextHtml thread sleep error: " + e.getMessage());
                            }
                            LOGGER.info(Thread.currentThread().getName() + " cost time:" + (endTime - startTime) + " craw done...");
                        } else {
                            synchronized (signal) {
                                try {
                                    waitCrawHtmlThreadCount++;
                                    LOGGER.info("Current wait thread num: " + waitCrawHtmlThreadCount);
                                    signal.wait();
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }, "Thread-spider-craw-" + i).start();
        }
    }

    @Override
    public String getUrl() {
        String tempUrl = "";
        if (this.urlList != null && this.urlList.size() != 0) {
            tempUrl = this.urlList.get(0);
            this.urlList.remove(0);
        }
        return tempUrl;
    }

    public void crawHtml(String htmlUrl) {
        int retry_count = 3;//默认重试3次
        int retry_time = 3000;//每次重试间隔3秒
        int current_retry_count = 0;
        boolean is_retry = false;
        do {
            System.out.println("Spider crawing url：" + htmlUrl);
            if (current_retry_count > retry_count) {
                break;
            }
            try {
                LOGGER.debug("jsoup get document url: " + htmlUrl);
                Document doc = Jsoup.connect(htmlUrl)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("Accept-Encoding", "gzip, deflate")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("Host", this.indexUrl)
                        .header("Connection", "keep-alive")
                        .header("Cache-Control", "max-age=0")
                        .header("Referer", htmlUrl)
                        .header("Cookie", "ASP.NET_SessionId=xj3z4sf5qzljnxibhdce3sim; bdyh_record=1970324846989066%2C1970324846989064%2C1970324845702078%2C1970324845892060%2C1970324845892065%2C1970324845702079%2C1970324846989065%2C1970324846989063%2C; CheckIPAuto=0; CheckIPDate=2017-11-15 21:44:03; xj3z4sf5qzljnxibhdce3sim3isIPlogin=1; click0=2017/11/14 22:51:31; click1=2017/11/14 22:51:31; click2=2017/11/15 21:45:18; click3=2017/11/15 22:10:44; Hm_lvt_58c470ff9657d300e66c7f33590e53a8=1510045402,1510119472,1510301767; Hm_lpvt_58c470ff9657d300e66c7f33590e53a8=1510755043; CookieId=xj3z4sf5qzljnxibhdce3sim; User_User=%d6%d0%b9%fa%c8%cb%c3%f1%b4%f3%d1%a7; FWinCookie=1")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36")
                        .timeout(50000)
                        .get();
                LawDocument lawDocument = this.parseLawHtml(doc);
                lawDocument.setUrl(htmlUrl);
                lawDocument.setRawHtml(doc.html());
                if (lawDocument.saveToDB()) {
                    LOGGER.info("Save document to MongoDB success....");
                } else {
                    LOGGER.info("Save document to MongoDB skip: the document already exits....");
                }
            } catch (Exception e) {
                is_retry = true;
                current_retry_count++;
                LOGGER.error("Jsoup get html err: " + e.getMessage());
            }
            try {
                Thread.sleep(retry_time);
            } catch (InterruptedException e) {
                LOGGER.error("Thread sleep error: " + e.getMessage());
            }
        } while (is_retry);

    }

    //从获取主页上分类的url
    public LawDocument parseLawHtml(Document doc) {
        LawDocument lawDocument = new LawDocument();
        try {
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
            ;
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
            String regEx_space = "\\s*|\t|\r|\n";//定义空格回车换行符
            Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
            Matcher m_space = p_space.matcher(html);
            html = m_space.replaceAll("").replaceAll("　", ""); // 过滤空格回车标签
            String regEx_html = "<br>|<br />|<br/>|</p>|</div>"; // 定义HTML标签的正则表达式
            Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            Matcher m_html = p_html.matcher(html);
            html = m_html.replaceAll("\n"); // 过滤html标签
            String regEx2_html = "<[^>]+>"; // 定义HTML标签的正则表达式
            Pattern p2_html = Pattern.compile(regEx2_html, Pattern.CASE_INSENSITIVE);
            Matcher m2_html = p2_html.matcher(html);
            html = m2_html.replaceAll(""); // 过滤html标签
            lawDocument.setCleanHtml(html);

            String[] result = html.split("\r|\n");
            String tiao = "第[一二三四五六七八九十百千万]+条";//定义条数
            String xiang = "（[一二三四五六七八九十百千万]+）";//定义项数
            boolean tiao_in = false;
            String current_kuan = "";

            List<LawArticle> lawArticleList = new ArrayList<LawArticle>();
            LawArticle currentLaw = new LawArticle();
            for (int i = 0; i < result.length; i++) {
                String par = result[i].trim();
                if (!par.isEmpty()) {
                    Pattern regEx_tiao = Pattern.compile(tiao, Pattern.CASE_INSENSITIVE);
                    Matcher m_tiao = regEx_tiao.matcher(par);

                    Pattern regEx_tiao_xiang = Pattern.compile(xiang, Pattern.CASE_INSENSITIVE);
                    Matcher m_tiao_xiang = regEx_tiao_xiang.matcher(par);

                    if (m_tiao.find() && m_tiao.start() == 0) {
                        if (!tiao_in) {
                            tiao_in = true;
                        }
                        if (!current_kuan.isEmpty()) {
                            currentLaw.getParagraph().add(current_kuan);
                            lawArticleList.add(currentLaw);
                        }

                        LawArticle law = new LawArticle();
                        String name = par.substring(m_tiao.start(), m_tiao.end());
                        law.setName(name);
                        current_kuan = par.substring(m_tiao.end(), par.length()).trim();
                        currentLaw = law;
                        continue;
                    }

                    if (m_tiao_xiang.find() && tiao_in && m_tiao_xiang.start() == 0) {
                        current_kuan += par;
                        continue;
                    }
                    if (tiao_in) {
                        if (!current_kuan.isEmpty()) {
                            currentLaw.getParagraph().add(current_kuan);
                        }
                        current_kuan = par.trim();
                    }
                }
            }
            currentLaw.getParagraph().add(current_kuan);
            lawArticleList.add(currentLaw);
            lawDocument.setArticle(lawArticleList);

        } catch (Exception e) {
            LOGGER.error("Get article error...");
            LOGGER.error(e);
        }
        return lawDocument;
    }

    public void crawManySoureceUrlField(List<String> xpathList) {
        for (String xpath : xpathList) {
            crawOneSoureceUrlField(xpath);
        }
    }

    //爬取特地域的url和抓取特定页面部分
    public void crawOneSoureceUrlField(String xpath) {
        WebClient client = HtmlUnitClient.getSingletonHtmlUntiClent();
        List<HtmlAnchor> anchoresNodes = getSoureceUrlField(client, xpath);
        LOGGER.info("Get source filed url count:" + anchoresNodes.size());
        for (int m = 0; m < anchoresNodes.size(); m++) {
            try {
                HtmlAnchor anchor = anchoresNodes.get(m);
                HtmlPage clickPage = anchor.click();
                Thread.sleep(5000);
                HtmlTableDataCell content = getContentPage(clickPage);
                this.crawUrl(anchor.asText(), content);
            } catch (Exception e) {
                LOGGER.error("Get content error:" + e.getMessage());
            }
        }
    }

    public HtmlPage getSoureUrlPage(WebClient client, String xpath) {
        HtmlPage page = null;
        try {
            page = client.getPage(this.indexUrl);
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

    public List<HtmlAnchor> getSoureceUrlField(WebClient client, String xpath) {
        List<HtmlAnchor> anchorsList = new ArrayList<HtmlAnchor>();
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

    public void crawUrl(String categoryName, HtmlTableDataCell content) {
        DomNodeList<HtmlElement> clickAnchorNodes = content.getElementsByTagName("a");
        deepCrawContentUrl(categoryName, clickAnchorNodes);
        LOGGER.info("当前url池中个数:[" + this.urlList.size() + "]");
    }

    public HtmlTableDataCell getContentPage(HtmlPage nextClickPage) {
        HtmlTableDataCell content = null;
        try {
            content = (HtmlTableDataCell) nextClickPage.getByXPath("//*[@id=\"td_jianbian\"]").get(0);
        } catch (Exception e) {
            LOGGER.error("get content page error: " + e.getMessage());
        }
        return content;
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
                HtmlAnchor contentAnchor = (HtmlAnchor) clickAnchorNodes.get(i);
                if (contentAnchor.getAttribute("class").trim().equals("main-ljwenzi")) {
                    count++;
                    LOGGER.info("当前url[" + categoryName + "][" + page + "][" + count + "]" + this.indexUrl + "/" + contentAnchor.getHrefAttribute());
                    this.addUrl(this.indexUrl + "/" + contentAnchor.getHrefAttribute());
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


}
