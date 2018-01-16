import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/15 15:19
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Neo4jDriver {
    public static void main(String[] args){
        Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "111111" ) );
        Session session = driver.session();

//        session.run( "CREATE ( id :{id}, a:Person {name: {name}, title: {title}, id: {id}})",
//                parameters( "name", "Arthur", "title", "King", "id", 0) );
//        session.run( "CREATE (a:中国刑法 {name: {name}, title: {title}, id: {id}})",
//                parameters( "name", "中国刑法", "title", "monitor", "id", 1) );
//
//        StatementResult result = session.run( "MATCH (a:Person) WHERE a.name = {name} " +
//                        "RETURN a.name AS name, a.title AS title",
//                parameters( "name", "Arthur" ) )
        StatementResult result = session.run("CREATE INDEX ON Person:(姓名)");
        StatementResult result2 = session.run("CREATE (n:Person {姓名: '潘北平'}) RETURN n");
//        StatementResult result2 = session.run("CREATE (n:Person { name: 'Dan' }) RETURN n");
//        StatementResult result3 = session.run("MATCH (a:Person { name: 'Ann' }), (b:Person { name: 'Dan' }) CREATE (a)-[:依据]->(b)");

        while ( result.hasNext() )
        {
            Record record = result.next();
            System.out.println( record.get( "title" ).asString() + " " + record.get( "name" ).asString() );
        }

        session.close();
        driver.close();
    }

}
