package dao;

import log.MyLogger;
import org.apache.log4j.Logger;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/3/16 21:03
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Neo4jDriver {
    private static Logger LOGGER = MyLogger.getMyLogger(Neo4jDriver.class);

    private Driver driver;

    private Neo4jDriver() {

        WebProperties dbProperties = WebProperties.getWebProperties();
        java.util.Properties prop = dbProperties.getProp();
        if (prop != null) {
            String baseURL = prop.getProperty("neo4jServerAddress");
            String username = prop.getProperty("neo4jServerUsername");
            String password = prop.getProperty("neo4jServerPassword");
            LOGGER.info("Begin connect neo4j server: " + baseURL);
            try {
                this.driver = GraphDatabase.driver(baseURL, AuthTokens.basic(username, password));
            } catch (Exception e) {
                LOGGER.error("Connect neo4j server failure: " + e.getMessage());
            }
            LOGGER.info("Connect neo4j server successfully....");
        } else {
            LOGGER.error("graph Properties not found!");
        }
    }

    public static Neo4jDriver getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Driver getDriver() {
        return driver;
    }

    private static class SingletonHolder {
        private static final Neo4jDriver INSTANCE = new Neo4jDriver();
    }
}
