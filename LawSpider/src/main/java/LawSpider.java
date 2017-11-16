import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
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
    private Set urlSet;
    private List<String> urlList;
    private String indexUrl;
    private String rootUrl;
    private int crawHtmlthreadCount;
    private int waitCrawHtmlThreadCount;
    private int poolUrlSize;
    private MongoDB mongoDB;

    public LawSpider(String indexUrl, String rootUrl, int crawHtmlthreadCount) {
        this.crawHtmlthreadCount = crawHtmlthreadCount;
        this.indexUrl = indexUrl;
        this.rootUrl = rootUrl;
        this.urlSet = Collections.synchronizedSet(new HashSet<String>());
        this.urlList = Collections.synchronizedList(new ArrayList<String>());
        this.mongoDB = MongoDB.getMongoDB();
        this.waitCrawHtmlThreadCount = 0;
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
                                    waitCrawHtmlThreadCount++;
                                    System.out.println("当前有[" + waitCrawHtmlThreadCount + "]个: " + Thread.currentThread().getName() + "在等待...");
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

//        this.parseHomePage(this.indexUrl);
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
            LOGGER.debug("jsoup get document url: "+ htmlUrl);
            Document doc = Jsoup.connect(htmlUrl)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Host", this.rootUrl)
                    .header("Connection", "keep-alive")
                    .header("Cache-Control", "max-age=0")
                    .header("Referer", htmlUrl)
                    .header("Cookie", "ASP.NET_SessionId=xj3z4sf5qzljnxibhdce3sim; bdyh_record=1970324846989066%2C1970324846989064%2C1970324845702078%2C1970324845892060%2C1970324845892065%2C1970324845702079%2C1970324846989065%2C1970324846989063%2C; CheckIPAuto=0; CheckIPDate=2017-11-15 21:44:03; xj3z4sf5qzljnxibhdce3sim3isIPlogin=1; click0=2017/11/14 22:51:31; click1=2017/11/14 22:51:31; click2=2017/11/15 21:45:18; click3=2017/11/15 22:10:44; Hm_lvt_58c470ff9657d300e66c7f33590e53a8=1510045402,1510119472,1510301767; Hm_lpvt_58c470ff9657d300e66c7f33590e53a8=1510755043; CookieId=xj3z4sf5qzljnxibhdce3sim; User_User=%d6%d0%b9%fa%c8%cb%c3%f1%b4%f3%d1%a7; FWinCookie=1")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36")
                    .timeout(500000)
                    .get();
            LawDocument lawDocument = this.parseLawHtml(doc);
            lawDocument.setUrl(htmlUrl);
            lawDocument.setRawHtml(doc.html());
            this.mongoDB.saveDocument(lawDocument.getDocument());
            LOGGER.debug("save document successful");
        } catch (Exception e) {
            this.addUrl(htmlUrl);
            LOGGER.error("get html err: "+e.getMessage());
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
                this.addUrl(this.rootUrl + "/" + ele.attr("href"));
            }
            if(this.waitCrawHtmlThreadCount > 0){//如果有等待的线程，则唤醒
                synchronized(signal) {  //---------------------（2）
                    System.out.println("唤醒了爬取线程，当前等待线程:"+ (this.waitCrawHtmlThreadCount-1));
                    this.waitCrawHtmlThreadCount--;
                    signal.notify();
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    //从获取主页上分类的url
    public LawDocument parseLawHtml(Document doc) {
        LawDocument lawDocument = new LawDocument();
        try{
            String title = doc.select("#tbl_content_main > tbody > tr:nth-child(1) > td > span > strong").first().childNode(0).toString();
            lawDocument.setTitle(title);
        }catch (NullPointerException e){
            LOGGER.warn("no tile of law");
        }
        try {
            String department = doc.select("#tbl_content_main > tbody > tr:nth-child(2) > td:nth-child(1) > a").first().childNode(0).toString();
            lawDocument.setDepartment(department);
        }catch (NullPointerException e){
            LOGGER.warn("no department of law");
        }
        try{
            String implement_date = doc.select("#tbl_content_main > tbody > tr:nth-child(3) > td:nth-child(2)").first().childNode(1).toString();
            lawDocument.setImplement_date(implement_date);
        }catch (NullPointerException e){
            LOGGER.warn("no implement_date of law");
        }
        try{
            String relase_date = doc.select("#tbl_content_main > tbody > tr:nth-child(3) > td:nth-child(1)").first().childNode(1).toString();
            lawDocument.setRelease_data(relase_date);
        }catch (NullPointerException e){
            LOGGER.warn("no relase_date of law");
        }
        try{
            String level = doc.select("#tbl_content_main > tbody > tr:nth-child(5) > td").first().text();;
            lawDocument.setLevel(level);
        }catch (NullPointerException e){
            LOGGER.warn("no level of law");
        }
        try{
            String timeless = doc.select("#tbl_content_main > tbody > tr:nth-child(4) > td:nth-child(1) > a").first().childNode(0).toString();
            lawDocument.setTimeless(timeless);
        }catch (NullPointerException e){
            LOGGER.warn("no timeless of law");
        }
        try{
            String category = doc.select("#tbl_content_main > tbody > tr:nth-child(5) > td > a").first().childNode(0).toString();
            lawDocument.setCategory(category);
        }catch (NullPointerException e){
            LOGGER.warn("no category of law");
        }
        try{
            Elements eles = doc.getElementsByClass("TiaoNoA");
            lawDocument.setTiaoNum(eles.toArray().length);
        }catch (NullPointerException e){
            lawDocument.setTiaoNum(0);
            LOGGER.warn("no TiaoNoA of law");
        }
        try {
            String html = doc.select(" #div_content").first().html();
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

            List<String> current_tiao_kuan = new ArrayList<String>();
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

        }catch (Exception e){
            LOGGER.error("get article error");
            LOGGER.error(e);
        }
        return lawDocument;
    }

    public synchronized boolean isEmptyPoolUrl() {
        if (this.poolUrlSize > this.urlList.size()) {
            return false;
        }
        return true;
    }
}
