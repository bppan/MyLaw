package WenshuCourtSpider;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/19 14:22
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MainWenshuCourtClean {
    public static void main(String[] args) {
        Clean clean = new Clean("wenshucourt_crawJob", "wenshucourt_law", "wenshucourt_clean");
        clean.doClean();
    }
}
