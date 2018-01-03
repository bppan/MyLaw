package Mongo;

import Log.MyLogger;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/2 16:25
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class DBProperties {
    private static Logger LOGGER = MyLogger.getMyLogger(DBProperties.class);
    private static DBProperties dbProperties = new DBProperties();
    private Properties prop;

    private DBProperties() {
        //初始化DBProperties
        this.prop = new Properties();
        InputStream properties = null;
        try {
            properties = new BufferedInputStream(new FileInputStream("config/db.properties"));
            prop.load(properties);
            LOGGER.info("Load db properties successfully");
            properties.close();
        } catch (Exception e) {
            LOGGER.info("Load db properties error");
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    public static DBProperties getDBProperties() {
        return dbProperties;
    }

    public Properties getProp() {
        return this.prop;
    }
}
