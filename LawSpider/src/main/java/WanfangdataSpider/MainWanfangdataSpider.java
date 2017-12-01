package WanfangdataSpider;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/1 15:34
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainWanfangdataSpider {
    public static void main(String[] args) {
        WanfangdataSpider spider = new WanfangdataSpider("http://c.wanfangdata.com.cn/Claw.aspx", 1, "wanfangdata_crawJob", "wanfangdata_law");
        List<String> urlFieldXpath = new ArrayList<String>();
        urlFieldXpath.add("/html/body/div[4]/div[1]/div[2]/div");
        spider.setXpathList(urlFieldXpath);
        spider.crawManySoureceUrlField();
    }
}
