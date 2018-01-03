package ChinacourtSpider;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/17 17:10
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainChinacourtClean {
    public static void main(String[] args) {
        Clean clean = new Clean("chinacourt_crawJob", "chinacourt_law", "chinacourt_clean");
        clean.doClean();
    }
}
