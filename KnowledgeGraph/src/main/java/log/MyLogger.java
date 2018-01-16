package log;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/2 16:23
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class MyLogger extends Logger {
    protected MyLogger(String name) {
        super(name);
    }

    public static Logger getMyLogger(Class<?> c) {
        Logger logger = Logger.getLogger(c);
        Properties p = new Properties();
        try {
            p.load(MyLogger.class.getResourceAsStream("/log4j.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        PropertyConfigurator.configure(p);
        return logger;
    }
}
