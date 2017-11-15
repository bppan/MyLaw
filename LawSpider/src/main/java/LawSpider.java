import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * @Author : Administrator
 * @Date : 2017/11/10 17:21
 * @Description :
 */
public class LawSpider extends Spider {
    private static Logger LOGGER = LawLogger.getLawLogger(LawSpider.class);
    private static final Object signal = new Object();   //线程间通信变量
    private Set urlSet;
    private List<String> urlList;
    private String indexUrl;
    private String rootUrl;
    private int crawHtmlthreadCount;
    private int crawHtmlThread;
    private int poolUrlSize;

    public LawSpider(String indexUrl, String rootUrl,int crawHtmlthreadCount) {
        this.crawHtmlthreadCount = crawHtmlthreadCount;
        this.indexUrl = indexUrl;
        this.rootUrl = rootUrl;
        this.urlSet = Collections.synchronizedSet(new HashSet<String>());
        this.urlList = Collections.synchronizedList(new ArrayList<String>());
    }

    @Override
    public boolean addUrl(String url) {
        if (urlSet.add(url)) {
            this.urlList.add(url);
            return true;
        }
        return false;
    }

    @Override
    public void doCraw() {
        //获取种子url对应的下一个爬取的url
        for (int i = 0; i < crawHtmlthreadCount; i++) {
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        String crawUrl = getUrl();
                        if (!crawUrl.isEmpty()) {
                            System.out.println("当前" + Thread.currentThread().getName() + "在执行...");
                            crawHtml(crawUrl);
                            System.out.println("当前" + Thread.currentThread().getName() + "执行完毕...");
                        } else {
                            synchronized (signal) {
                                try {
                                    crawHtmlThread++;
                                    System.out.println("当前有[" + crawHtmlThread + "]个: " + Thread.currentThread().getName() + "在等待...");
                                    signal.wait();
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }, "Thread-spider-getHtml-" + i).start();
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
        try {
            Document doc = Jsoup.connect(htmlUrl)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Host", this.rootUrl)
                    .header("Connection", "keep-alive")
                    .header("Cache-Control", "max-age=0")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36")
                    .timeout(5000)
                    .get();
            this.parseHtml(doc);
        }catch (IOException e){

        }


    }
    //从获取主页上分类的url
    public void parseHomePage(String sUrl) {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        // 这里是配置一下不加载css和javaScript,配置起来很简单，是不是
        webClient.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
        webClient.getOptions().setCssEnabled(false); // 禁用css支持
        webClient.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
        webClient.getOptions().setTimeout(50000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待
        try {
            HtmlPage page1 = (HtmlPage) webClient.getPage(sUrl);
            Document doc = Jsoup.parse(page1.asXml());
            Elements eles = doc.getElementsByClass("main-ljwenzi");
            for (Element ele : eles) {
                this.addUrl(this.rootUrl+"/"+ele.attr("href"));
            }
        }catch (Exception e){
            LOGGER.error(e);
        }
    }
    //从获取主页上分类的url
    public void parseHtml(Document doc) {
        LawCollection lawCollection = new LawCollection();
        //获取标题
        String title = doc.select("#tbl_content_main > tbody > tr:nth-child(1) > td > span > strong").first().childNode(0).toString();
        String department = doc.select("#tbl_content_main > tbody > tr:nth-child(2) > td:nth-child(1) > a").first().childNode(0).toString();


//        lawCollection.setTitle();
    }
    public synchronized boolean isEmptyPoolUrl() {
        if (this.poolUrlSize > this.urlList.size()) {
            return false;
        }
        return true;
    }
}
