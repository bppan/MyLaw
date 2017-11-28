package Interface;

import CrawJob.CrawJob;
import Log.LawLogger;
import Mongo.LawArticle;
import Mongo.LawDocument;
import WebCraw.HtmlUnitClient;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
public abstract class LawSpider extends Spider {

    private static final Object signal = new Object();   //线程间通信变量
    private static Logger LOGGER = LawLogger.getLawLogger(LawSpider.class); //日志类
    protected String indexUrl; //根url
    private int crawHtmlthreadCount; //爬取线程数
    private int waitCrawHtmlThreadCount = 0; //记录等待爬取线程数
    private List<String> xpathList;
    private LawDocument lawDocumen;
    private CrawJob crawJob;

    //构造函数
    public LawSpider(String indexUrl, int crawHtmlthreadCount, String crawJobCollectionName, String lawCollectionName) {
        this.crawHtmlthreadCount = crawHtmlthreadCount;
        this.indexUrl = indexUrl;
        this.setCrawJob(crawJobCollectionName);
        this.setLawDocumen(lawCollectionName);
    }

    //模拟人工翻页等待时间
    public synchronized int getRandomWaitTime(int left, int right){
        if(left <= right){
            return (int) (left + Math.random() * (right - left));
        }else {
            return 0;
        }
    }

    public LawDocument getLawDocumen() {
        return lawDocumen;
    }

    public void setLawDocumen(String lawCollection) {
        lawDocumen = new LawDocument(lawCollection);
    }

    public CrawJob getCrawJob() {
        return crawJob;
    }

    public void setCrawJob(String crawJobCollection) {
        crawJob = new CrawJob(crawJobCollection);
    }

    public String getIndexUrl() {
        return this.indexUrl;
    }

    public void setXpathList(List<String> xpathList) {
        this.xpathList = xpathList;
    }

    //添加url
    @Override
    public boolean addUrl(String title, String url) {
        if(CrawJob.addJob(getCrawJob().getCrawJobcollection(), title, url)){
            //如果有等待的线程，则唤醒
            if (waitCrawHtmlThreadCount > 0) {
                synchronized (signal) {
                    //双重检查
                    if(waitCrawHtmlThreadCount > 0){
                        LOGGER.info("Wake up thread，current waiting thread num:" + (waitCrawHtmlThreadCount - 1));
                        waitCrawHtmlThreadCount--;
                        signal.notify();
                    }
                }
            }
            return true;
        }
        return false;
    }

    //开始爬取
    @Override
    public void doCraw() {
        //爬取页面线程
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
                            LOGGER.info(Thread.currentThread().getName() + " cost time:" + (endTime - startTime) + " craw done...");
                        } else {
                            synchronized (signal) {
                                try {
                                    waitCrawHtmlThreadCount++;
                                    LOGGER.info("Current wait thread num: " + waitCrawHtmlThreadCount);
                                    signal.wait();
                                } catch (InterruptedException e) {
                                    LOGGER.error("Get signal error " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }, "Thread-spider-craw-" + i).start();
        }
        //爬取url
//        crawManySoureceUrlField();
    }

    @Override
    public String getUrl() {
        return CrawJob.getJob(getCrawJob().getCrawJobcollection());
    }

    public void crawHtml(String htmlUrl) {
        if (LawDocument.isExits(getLawDocumen().getLawcollection(), htmlUrl)) {
            LOGGER.info("Save document to MongoDB skip: the document already exits....");
            boolean result = CrawJob.doneJob(getCrawJob().getCrawJobcollection(), htmlUrl, "Save document skip: the document already exits....");
            if (result) {
                LOGGER.info("Craw job url[" + htmlUrl + "] done....");
            } else {
                LOGGER.info("Craw job url[" + htmlUrl + "] fail....");
            }
            return;
        }

        int retry_count = 3;//默认重试3次
        int current_retry_count = 0;
        boolean is_retry = false;
        do {
            LOGGER.info("Spider crawing url：" + htmlUrl);
            if (current_retry_count >= retry_count) {
                break;
            }
            try {
                LOGGER.debug("Jsoup get document url: " + htmlUrl);
                LawDocument lawDocument = this.parseLawHtml(htmlUrl);
                lawDocument.setUrl(htmlUrl);
                lawDocument.setCollection(getLawDocumen().getLawcollection());
                String comments = "";
                if (LawDocument.saveToDB(lawDocument)) {
                    is_retry = false;
                    comments = "Save document to MongoDB success....";

                } else {
                    comments = "Save document to MongoDB skip: the document already exits....";
                }
                LOGGER.info(comments);
                if (CrawJob.doneJob(getCrawJob().getCrawJobcollection(), htmlUrl, comments)) {
                    LOGGER.info("Craw job url[" + htmlUrl + "] done....");
                } else {
                    LOGGER.info("Craw job url[" + htmlUrl + "] fail....");
                }
            } catch (Exception e) {
                is_retry = true;
                current_retry_count++;
                LOGGER.error("Jsoup get html err: " + e.getMessage());
                CrawJob.resetJob(getCrawJob().getCrawJobcollection(), htmlUrl, "Jsoup get html error: " + e.getMessage());
            }
            try {
                Thread.sleep(getRandomWaitTime(2000, 5000));
            } catch (InterruptedException e) {
                LOGGER.error("WaitToCrawNextHtml thread sleep error: " + e.getMessage());
            }
        } while (is_retry);

    }

    //解析主页
    public abstract LawDocument parseLawHtml(String url);

    public void crawManySoureceUrlField() {
        for (String xpath : this.xpathList) {
            crawOneSoureceUrlField(xpath);
        }
    }

    public String cleanHtml(String html) {
        String result = html;
        String regEx_space = "\\s*|\t|\r|\n";//定义空格回车换行符
        Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(result);
        result = m_space.replaceAll("").replaceAll("　", ""); // 过滤空格回车标签

        String regEx_html = "<br>|<br />|<br/>|</p>|</div>"; // 定义HTML标签的正则表达式
        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(result);
        result = m_html.replaceAll("\n"); // 过滤html标签

        String regEx2_html = "<[^>]+>"; // 定义HTML标签的正则表达式
        Pattern p2_html = Pattern.compile(regEx2_html, Pattern.CASE_INSENSITIVE);
        Matcher m2_html = p2_html.matcher(result);
        result = m2_html.replaceAll(""); // 过滤html标签
        return result;
    }

    public List<LawArticle> getLawArticleAndParagraph(String cleanHtml) {
        String[] result = cleanHtml.split("\r|\n");
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
        return lawArticleList;
    }

    public Document getJsoupConnection(String htmlUrl) throws IOException {
        return Jsoup.connect(htmlUrl).get();
    }

    public abstract void crawUrl(String categoryName, HtmlElement content);

    //爬取特地域的url
    public abstract void crawOneSoureceUrlField(String xpath);

    public abstract HtmlPage getSoureUrlPage(WebClient client, String xpath);

    public abstract List<HtmlElement> getSoureceUrlField(WebClient client, String xpath);

    public abstract HtmlElement getContentPage(HtmlPage nextClickPage);

    public abstract void deepCrawContentUrl(String categoryName, DomNodeList<HtmlElement> clickAnchorNodes);
}
