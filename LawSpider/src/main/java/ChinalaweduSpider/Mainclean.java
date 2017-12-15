package ChinalaweduSpider;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2017/12/15 16:48
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Mainclean {
    public static void main(String[] args){
        Clean clean = new Clean("chinaLawedu_crawJob", "chinaLawedu_law", "chinaLawedu_law");
        clean.doClean();
    }
}
