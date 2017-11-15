import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Author : Administrator
 * @Date : 2017/11/10 17:29
 * @Description :
 */
public class MongoDB extends DB {
    private static Logger LOGGER = LawLogger.getLawLogger(MongoDB.class);
    private static MongoDB mongoDBClint = new MongoDB();

    private MongoDB() {
        //初始化mongodb
        DBProperties dbProperties = DBProperties.getDBProperties();
        Properties prop = dbProperties.getProp();
        if(prop != null){
            String db_host = prop.getProperty("mongodb_host");
            int db_port = Integer.parseInt(prop.getProperty("mongodb_port"));
            String db_database = prop.getProperty("mongodb_database");
            String db_collection = prop.getProperty("mongodb_collection");
            try {
                // 连接到 mongodb 服务
                MongoClient mongoClient = new MongoClient(db_host, db_port);
                // 连接到数据库
                MongoDatabase mongoDatabase = mongoClient.getDatabase(db_database);
                LOGGER.info("Connect to mongodb database successfully");
                MongoCollection<Document> collection = mongoDatabase.getCollection(db_collection);
                LOGGER.info("select "+db_collection+" colleciont successfully");
                LawCollection mycollection = new LawCollection();
                mycollection.setTitle("123");
                mycollection.setCategory("ppp");
                mycollection.setDepartment("456");
                mycollection.setImplement_date("655656");
                mycollection.setRelease_data("0000");
                mycollection.setLevel("llll");
                mycollection.setTimeless("2017:11");

                List<LawArticle> articles = new ArrayList<LawArticle>();
                for(int i = 0; i < 10; i++){
                    LawArticle article = new LawArticle();
                    List<String> par = new ArrayList<String>();
                    for(int j = 0; j < 3; j++){
                        par.add(i + "par " + j);
                    }
                    article.setName(String.valueOf(i));
                    article.setParagraph(par);
                    articles.add(article);
                }
                mycollection.setArticle(articles);

//                Document document = new Document("title", "MongoDB").
//                        append("description", "database").
//                        append("likes", 100).
//                        append("by", "Fly");
                List<Document> documents = new ArrayList<Document>();
                documents.add(mycollection.getCollection());
                collection.insertMany(documents);
                System.out.println("文档插入成功");
            } catch (Exception e) {
                LOGGER.error("Connect to mongodb database Error!");
                LOGGER.error(e);
            }
        }else {
            LOGGER.error("mongodb Properties not found!");
        }
    }

    public static MongoDB getMongoDBClint() {
        return mongoDBClint;
    }

}
