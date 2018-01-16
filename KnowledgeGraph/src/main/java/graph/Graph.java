package graph;

import neo4jDriver.Neo4jDriver;
import org.bson.Document;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/16 16:14
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Graph {
    private Driver driver;

    public Graph() {
        this.driver = Neo4jDriver.getInstance().getDriver();
    }

    public void createNode(Document law) {
        Session session = driver.session();
        try {
//            StatementResult result = session.run("CREATE (n:Person { 名称: '潘北平' }) RETURN n");

            StringBuilder createNodecyphe = new StringBuilder("");

            session.run("CREATE (n:law {" +
                            "name: {name}, " +
                            "文档id: {lawId}, " +
                            "发布单位: {department}, " +
                            "发布日期: {release_date}, " +
                            "文号: {release_number}, " +
                            "生效日期: {implement_date}, " +
                            "类别: {category}, " +
                            "法律级别: {level}, " +
                            "时效性: {timeless}, " +
                            "内容: {content}, " +
                            "发条数: {article_num}, " +
                            "})",
                    parameters("name", "中国刑法", "title", "monitor", "id", 1));
        } catch (Exception e) {

        } finally {
            session.close();
            driver.close();
        }
    }

    public void createRelationShip() {

    }

}
