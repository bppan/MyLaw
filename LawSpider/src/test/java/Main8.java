import WebCraw.HtmlUnitClient;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/4 13:20
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Main8 {
    public static void main(String[] args){
        WebClient client = HtmlUnitClient.getSingletonHtmlUntiClent();
        HtmlPage page = null;
        try {
            page = client.getPage("https://www.itslaw.com/bj");
            Thread.sleep(5000);
            System.out.println(page.asXml());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
