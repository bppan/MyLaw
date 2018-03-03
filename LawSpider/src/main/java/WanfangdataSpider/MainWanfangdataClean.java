package WanfangdataSpider;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/4 20:03
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainWanfangdataClean {
    public static void main(String[] args) {
        WanfangdataClean wanfangdataClean = new WanfangdataClean("wanfangdata_crawJob", "wanfangdata_clean2", "law3");
//        wanfangdataClean.doClean2();
        wanfangdataClean.doCleanRepeat();
    }
}
