package dao;

import log.MyLogger;
import org.apache.log4j.Logger;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/2 16:25
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class WebProperties {
    private static Logger LOGGER = MyLogger.getMyLogger(WebProperties.class);
    private static WebProperties webProperties = new WebProperties();
    private java.util.Properties prop;

    private WebProperties() {
        //初始化DBProperties
        this.prop = new java.util.Properties();
        try {
            prop.load(WebProperties.class.getResourceAsStream("/solrWeb.properties"));
            LOGGER.info("Load web properties successfully");
        } catch (Exception e) {
            LOGGER.info("Load web properties error");
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    public static WebProperties getWebProperties() {
        return webProperties;
    }

    public java.util.Properties getProp() {
        return this.prop;
    }
}
