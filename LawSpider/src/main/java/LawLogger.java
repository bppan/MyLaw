import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;

/**
 * @Author : Administrator
 * @Date : 2017/11/14 16:52
 * @Description :
 */
public class LawLogger extends Logger {
    protected LawLogger(String name) {
        super(name);
    }

    public static Logger getLawLogger(Class<?> c) {
        Logger logger = Logger.getLogger(c);
        Properties p = new Properties();
        try {
            p.load(LawLogger.class.getResourceAsStream("/log4j.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        PropertyConfigurator.configure(p);
        return logger;
    }
}
