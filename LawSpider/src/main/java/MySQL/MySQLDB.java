package MySQL;

/**
 * @Author : Administrator
 * @Date : 2017/11/14 15:52
 * @Description :
 */
public class MySQLDB {
    private MySQLDB() {

    }

    public static MySQLDB getMySQLDBClinet() {
        return SingletonHelp.instance;
    }

    private static class SingletonHelp {
        private static MySQLDB instance = new MySQLDB();
    }

}
