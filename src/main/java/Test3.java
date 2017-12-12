import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.io.IOException;

public class Test3 {

    public enum Tutorials implements Label {
        JAVA,SCALA,SQL,NEO4J;
    }

    public enum TutorialRelationships implements RelationshipType{
        JVM_LANGIAGES,NON_JVM_LANGIAGES;
    }

    public static void main(String[] args) {
        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
        File dbfile = new File("graph.db");
        GraphDatabaseService db= dbFactory.newEmbeddedDatabase(dbfile);
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
                    (scalaNode,TutorialRelationships.JVM_LANGIAGES);
            relationship.setProperty("Id","1234");
            relationship.setProperty("OOPS","YES");
            relationship.setProperty("FP","YES");

            tx.success();
        }
        System.out.println("Done successfully");
    }
}