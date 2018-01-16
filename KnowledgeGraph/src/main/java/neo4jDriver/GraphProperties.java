package neo4jDriver;

import log.MyLogger;
import org.apache.log4j.Logger;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/16 16:27
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class GraphProperties {
    private static Logger LOGGER = MyLogger.getMyLogger(GraphProperties.class);
    private static GraphProperties graphProperties = new GraphProperties();
    private java.util.Properties prop;

    private GraphProperties() {
        //初始化DBProperties
        this.prop = new java.util.Properties();
        try {
            prop.load(GraphProperties.class.getResourceAsStream("/graph.properties"));
            LOGGER.info("Load web properties successfully");
        } catch (Exception e) {
            LOGGER.info("Load web properties error");
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    public static GraphProperties getWebProperties() {
        return graphProperties;
    }

    public java.util.Properties getProp() {
        return this.prop;
    }
}
