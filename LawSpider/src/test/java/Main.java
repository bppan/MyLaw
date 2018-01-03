import Log.LawLogger;
import Mongo.DBProperties;
import Mongo.MongoDB;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


/**
 * @Author : Administrator
 * @Date : 2017/11/10 17:17
 * @Description :
 */
public class Main {
    public static void main(String[] args) {
        new Thread(new Runnable() {

            public void run() {
                System.out.println(System.currentTimeMillis());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("okayu");
            }
        }, "fofsdf").start();

        new Thread(new Runnable() {
            public void run() {
                System.out.println(System.currentTimeMillis());
                System.out.println("cccccccc");
            }
        }, "ggggg").start();


        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        // 这里是配置一下不加载css和javaScript,配置起来很简单，是不是
        webClient.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true
        webClient.getOptions().setCssEnabled(false); // 禁用css支持
        webClient.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
        webClient.getOptions().setTimeout(50000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待
        try {
            System.out.println("aaaaaaa");
//            HtmlPage page1 = (HtmlPage) webClient.getPage("http://www.pkulaw.cn/doSearch.ashx?" +
//                    "Db=chl&" +
//                    "clusterwhere=%2525e6%252595%252588%2525e5%25258a%25259b%2525e7%2525ba%2525a7%2525e5%252588%2525ab%25253dXA01&" +
//                    "clust_db=chl&" +
//                    "Search_Mode=accurate&" +
//                    "range=name&" +
//                    "menu_item=law&" +
//                    "EncodingName=&" +
//                    "time=0.5651965288821565");
            HtmlPage page1 = (HtmlPage) webClient.getPage("http://www.pkulaw.cn/doSearch.ashx?");
            System.out.println(page1.asXml());
            Document doc = Jsoup.parse(page1.asXml());
            Elements eles = doc.getElementsByClass("main-ljwenzi");
            String url = "";
            for (Element ele : eles) {
                System.out.println(ele.childNode(0).toString());
                System.out.println(ele.attr("href"));
                url = "http://www.pkulaw.cn" + "/" + ele.attr("href");

            }
            System.out.println(url);
            Document doc2 = Jsoup.connect(url).get();
//            doc2.select("//*[@id=\"tbl_content_main\"]/tbody/tr[1]/td/span/strong").first();

            System.out.println("======  " + doc2.select("#tbl_content_main > tbody > tr:nth-child(1) > td > span > strong").first().toString());

//            HtmlAnchor input = (HtmlAnchor) page1.getByXPath("//*[@id=\"dir_sub_div\"]/table/tbody/tr[2]/td/table/tbody/tr[1]/td/table/tbody/tr[2]/td/table/tbody/tr/td[1]/a[10]").get(0);
//            System.out.println("bbbbbbbbbbbbb");
//            System.out.println(input.getHrefAttribute());
//            input.setValueAttribute("HtmlUnit");
//            System.out.println(input.toString());
//            // 获取搜索按钮并点击
//            HtmlInput btnSearch = (HtmlInput) page1.getHtmlElementById("su");
//            HtmlPage page2 = btnSearch .click();
//            // 输出新页面的文本
//            System.out.println(page2.asXml());
            try {
                // 连接到 mongodb 服务
                MongoClient mongoClient = new MongoClient("localhost", 27017);

                // 连接到数据库
                MongoDatabase mongoDatabase = mongoClient.getDatabase("mycol");
                System.out.println("Connect to database successfully");

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger logger = LawLogger.getLawLogger(Main.class);
        logger.info("WWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
        logger.error("sdghskjdhgkjshjkdh");
        logger.error("qqqqqqqqqqqqqqqqqqqqqqqqqqqqq");

        DBProperties properties = DBProperties.getDBProperties();

        MongoDB db = MongoDB.getMongoDB();


    }
}
