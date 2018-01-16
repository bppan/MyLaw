import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import java.io.File;
public class Test3 {

    public static void main(String[] args) {

        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
        File dbfile = new File("D:\\neo4j\\neo4j-community-3.1.7\\data\\databases\\graph.db");
        GraphDatabaseService db = dbFactory.newEmbeddedDatabase(dbfile);

        try (Transaction tx = db.beginTx()) {
            Node javaNode = db.createNode(Tutorials.JAVA);
            javaNode.setProperty("TutorialID", "JAVA001");
            javaNode.setProperty("Title", "Learn Java");
            javaNode.setProperty("NoOfChapters", "25");
            javaNode.setProperty("Status", "Completed");

            Node scalaNode = db.createNode(Tutorials.SCALA);
            scalaNode.setProperty("TutorialID", "SCALA001");
            scalaNode.setProperty("Title", "Learn Scala");
            scalaNode.setProperty("NoOfChapters", "20");
            scalaNode.setProperty("Status", "Completed");

            Relationship relationship = javaNode.createRelationshipTo
                    (scalaNode, TutorialRelationships.JVM_LANGIAGES);
            relationship.setProperty("Id", "1234");
            relationship.setProperty("OOPS", "YES");
            relationship.setProperty("FP", "YES");

            tx.success();
            System.out.println("Done ddddd");
        }
        System.out.println("Done successfully");
    }

    public enum Tutorials implements Label {
        JAVA, SCALA, SQL, NEO4J;
    }

    public enum TutorialRelationships implements RelationshipType {
        JVM_LANGIAGES, NON_JVM_LANGIAGES;
    }
}