package ChinacourtSpider;

import WebCraw.HtmlUnitClient;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * @Author : Administrator
 * @Date : 2017/11/20 17:16
 * @Description :
 */
public class TestChinacourt {
    public static void main(String[] args){
        ChinacourtSpider spider = new ChinacourtSpider("http://www.chinacourt.org/law", 1);
        WebClient client = HtmlUnitClient.getSingletonHtmlUntiClent();
        spider.crawOneSoureceUrlField("//*[@id=\"mycontenta\"]/ul");
    }
}
