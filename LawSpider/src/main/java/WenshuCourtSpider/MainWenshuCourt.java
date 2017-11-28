package WenshuCourtSpider;

import ChinacourtSpider.ChinacourtSpider;
import WebCraw.HtmlUnitClient;
import com.gargoylesoftware.htmlunit.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/11/24 15:44
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainWenshuCourt {
    public static void main(String[] args){
        WenshuCourtSpider spider = new WenshuCourtSpider("http://www.court.gov.cn/wenshu.html", 1, "wenshucourt_crawJob", "wenshucourt_law");
        List<String> urlFieldXpath = new ArrayList<String>();
        urlFieldXpath.add("//*[@id=\"container\"]");
        spider.setXpathList(urlFieldXpath);
        spider.doCraw();
    }
}
