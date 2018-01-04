package PkulawSpider;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author : Administrator
 * @Date : 2017/11/15 17:21
 * @Description :
 */
public class MainPkulawSpider {
    public static void main(String[] args) {
        String indexUrl = "http://www.pkulaw.cn";
        PkulawSpider spider = new PkulawSpider(indexUrl, 1, "pkulaw_crawJob", "pkulaw_law");
        List<String> urlFieldXpath = new ArrayList<String>();
        urlFieldXpath.add("//*[@id=\"0\"]/td");
        urlFieldXpath.add("//*[@id=\"1\"]/td");
        urlFieldXpath.add("//*[@id=\"2\"]/td");
        urlFieldXpath.add("//*[@id=\"3\"]/td");
        spider.setXpathList(urlFieldXpath);
        spider.doCraw();

    }
}
