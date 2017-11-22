package ChinacourtSpider;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author : Administrator
 * @Date : 2017/11/20 17:16
 * @Description :
 */
public class MainChinacourt {
    public static void main(String[] args) {
        ChinacourtSpider spider = new ChinacourtSpider("http://www.chinacourt.org/law", 1, "chinacourt_crawJob", "chinacourt_law");
        List<String> urlFieldXpath = new ArrayList<String>();
        urlFieldXpath.add("//*[@id=\"mycontenta\"]/ul");
        spider.setXpathList(urlFieldXpath);
        spider.doCraw();
//        spider.crawHtml("http://www.chinacourt.org/law/detail/2017/07/id/149523.shtml");
    }
}
