import Log.LawLogger;
import Mongo.MongoDB;
import org.apache.log4j.Logger;

/**
 * @Author : Administrator
 * @Date : 2017/11/10 17:17
 * @Description :
 */
public class Main {
    private static Logger LOGGER = LawLogger.getLawLogger(MongoDB.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    public static void main(String[] args) {
        LOGGER.error("test");
    }
}
