import Log.LawLogger;
import org.apache.log4j.Logger;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/11/30 14:29
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Main7 {
    private static Logger LOGGER = LawLogger.getLawLogger(Main7.class);

    public static void main(String[] args) {
        LOGGER.debug("debug");
        LOGGER.error("error");
        LOGGER.info("infor");
        LOGGER.warn("warn");
        LOGGER.fatal("fatal");
    }
}
