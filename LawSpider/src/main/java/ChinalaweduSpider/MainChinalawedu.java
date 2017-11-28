package ChinalaweduSpider;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/11/28 19:54
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainChinalawedu {
    public static void main(String[] args){
        ChinalaweduSpider spider = new ChinalaweduSpider("http://www.chinalawedu.com/falvfagui/23411/", 1, "chinaLawedu_crawJob", "chinaLawedu_law");
        List<String> urlFieldXpath = new ArrayList<String>();
        urlFieldXpath.add("/html/body/div[3]/div[1]/div");
        spider.setXpathList(urlFieldXpath);
        spider.doCraw();
    }
}
