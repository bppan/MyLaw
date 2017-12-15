package FayiSpider;


import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/11/28 15:26
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainFayiSpider {
    public static void main(String[] args) {
        FayiSpider spider = new FayiSpider("http://law.fayi.com.cn", 1, "fayi_crawJob", "fayi_law");
        List<String> urlFieldXpath = new ArrayList<String>();
        urlFieldXpath.add("");
        spider.setXpathList(urlFieldXpath);
        spider.doCraw();
//        spider.crawManySoureceUrlField();
//        spider.getWholeContent("http://law.fayi.com.cn/qtl/244395.html");
//        System.out.println(spider.getWholeContent("http://law.fayi.com.cn/qtl/244395.html"));

    }
}
