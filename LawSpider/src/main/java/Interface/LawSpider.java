package Interface;

import CrawJob.CrawJob;
import Log.LawLogger;
import Mongo.LawDocument;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

/**
 * @Author : Administrator
 * @Date : 2017/11/10 17:21
 * @Description :
 */
public abstract class LawSpider extends Spider {

    private static final Object signal = new Object();   //线程间通信变量
    private static Logger LOGGER = LawLogger.getLawLogger(LawSpider.class); //日志类
    private final int waitToCrawNextHtmlTime = (int) (1000 + Math.random() * 5000); //模拟翻页等待时间
    protected String indexUrl; //根url
    private int crawHtmlthreadCount; //爬取线程数
    private int waitCrawHtmlThreadCount = 0; //记录等待爬取线程数
    private List<String> xpathList;

    //构造函数
    public LawSpider(String indexUrl, int crawHtmlthreadCount) {
        this.crawHtmlthreadCount = crawHtmlthreadCount;
        this.indexUrl = indexUrl;
    }

    public void setXpathList(List<String> xpathList){
        this.xpathList = xpathList;
    }
    //添加url
    @Override
    public boolean addUrl(String title, String url) {
        return CrawJob.addJob(title, url);
    }

    //开始爬取
    @Override
    public void doCraw() {
        //监视数据库获取爬取任务线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (CrawJob.getCrawJobNum() > 0 && waitCrawHtmlThreadCount > 0) {//如果有等待的线程，则唤醒
                        synchronized (signal) {
                            LOGGER.info("Wake up thread，current waiting thread num:" + (waitCrawHtmlThreadCount - 1));
                            waitCrawHtmlThreadCount--;
                            signal.notify();
                        }
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        LOGGER.error("monitor craw job thread sleep error: " + e.getMessage());
                    }
                }
            }
        }).start();

        //获取种子url对应的下一个爬取的url线程
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
                                LOGGER.error("WaitToCrawNextHtml thread sleep error: " + e.getMessage());
                            }
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

        crawManySoureceUrlField();
    }

    @Override
    public String getUrl() {
        return CrawJob.getJob();
    }

    private void crawHtml(String htmlUrl) {
        if (LawDocument.isExits(htmlUrl)) {
            LOGGER.info("Save document to MongoDB skip: the document already exits....");
            if (CrawJob.doneJob(htmlUrl, "Save document to MongoDB skip: the document already exits....")) {
                LOGGER.info("Craw job url[" + htmlUrl + "] done....");
            } else {
                LOGGER.info("Craw job url[" + htmlUrl + "] fail....");
            }
            return;
        }

        int retry_count = 3;//默认重试3次
        int retry_time = 3000;//每次重试间隔3秒
        int current_retry_count = 0;
        boolean is_retry = false;
        do {
            LOGGER.info("Spider crawing url：" + htmlUrl);
            if (current_retry_count > retry_count) {
                break;
            }
            try {
                LOGGER.debug("jsoup get document url: " + htmlUrl);
                Document doc = this.getJsoupConnection(htmlUrl);
                LawDocument lawDocument = this.parseLawHtml(doc);
                lawDocument.setUrl(htmlUrl);
                lawDocument.setRawHtml(doc.html());
                String comments = "";
                if (lawDocument.saveToDB()) {
                    is_retry = false;
                    comments = "Save document to MongoDB success....";

                } else {
                    comments = "Save document to MongoDB skip: the document already exits....";
                }
                LOGGER.info(comments);
                if (CrawJob.doneJob(htmlUrl, comments)) {
                    LOGGER.info("Craw job url[" + htmlUrl + "] done....");
                } else {
                    LOGGER.info("Craw job url[" + htmlUrl + "] fail....");
                }
            } catch (Exception e) {
                is_retry = true;
                current_retry_count++;
                LOGGER.error("Jsoup get html err: " + e.getMessage());
                CrawJob.resetJob(htmlUrl, "Jsoup get html err: " + e.getMessage());
            }
            try {
                Thread.sleep(retry_time);
            } catch (InterruptedException e) {
                LOGGER.error("Thread sleep error: " + e.getMessage());
            }
        } while (is_retry);

    }

    //解析主页
    public abstract LawDocument parseLawHtml(Document doc);

    public void crawManySoureceUrlField() {
        for (String xpath : this.xpathList) {
            crawOneSoureceUrlField(xpath);
        }
    }

    public Document getJsoupConnection(String htmlUrl) throws IOException{
        Document doc = Jsoup.connect(htmlUrl).get();
        return doc;
    }

    public abstract void crawUrl(String categoryName, HtmlElement content);

    //爬取特地域的url和抓取特定页面部分
    public abstract void crawOneSoureceUrlField(String xpath);

    public abstract HtmlPage getSoureUrlPage(WebClient client, String xpath);

    public abstract List<HtmlElement> getSoureceUrlField(WebClient client, String xpath);

    public abstract HtmlElement getContentPage(HtmlPage nextClickPage);

    public abstract void deepCrawContentUrl(String categoryName, DomNodeList<HtmlElement> clickAnchorNodes);
}
