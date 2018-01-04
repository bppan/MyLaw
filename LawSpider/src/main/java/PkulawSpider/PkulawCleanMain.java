package PkulawSpider;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/21 14:48
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class PkulawCleanMain {
    public static void main(String[] args) {
        PkulawClean pkulawClean = new PkulawClean("pkulaw_crawJob", "pkulaw_law", "pkulaw_clean");
        pkulawClean.doCleanRepeat();
    }
}
