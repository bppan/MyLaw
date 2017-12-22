package FayiSpider;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2017/12/15 16:48
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainFayiClean {
    public static void main(String[] args) {
        Clean clean = new Clean("fayi_crawJob", "fayi_law", "");
        clean.doClean();
    }
}
