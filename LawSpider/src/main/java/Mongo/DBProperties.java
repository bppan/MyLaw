package Mongo;

import Log.LawLogger;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Author : Administrator
 * @Date : 2017/11/14 17:19
 * @Description :
 */
public class DBProperties {
    private static Logger LOGGER = LawLogger.getLawLogger(DBProperties.class);
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
