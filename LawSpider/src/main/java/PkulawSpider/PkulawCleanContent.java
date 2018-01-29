package PkulawSpider;

import Log.LawLogger;
import Mongo.MongoDB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/1/29 15:00
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class PkulawCleanContent {
    private static Logger LOGGER = LawLogger.getLawLogger(PkulawCleanContent.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private MongoCollection<Document> lawCollecion;
    private MongoCollection<Document> cleanCollection;

    public PkulawCleanContent(String lawCollection, String cleanCollection) {
        this.lawCollecion = mongoDB.getCollection(lawCollection);
        this.cleanCollection = mongoDB.getCollection(cleanCollection);
    }

    public static void main(String[] args) {
        PkulawCleanContent pkulawCleanContent = new PkulawCleanContent("pkulaw_clean", "law2");
        pkulawCleanContent.cleanContent();
    }

    public void cleanContent() {
        LOGGER.info("Begin do pkulawCleanContent...");
        FindIterable<Document> iterables = this.lawCollecion.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                String url = law.getString("url");
                FindIterable<Document> iterablesclean = this.cleanCollection.find(new Document("url", url)).noCursorTimeout(true).limit(1);
                if (iterablesclean.first() != null) {
                    LOGGER.info("clean url: " + url);
                    Document cleanlaw = iterablesclean.first();
                    String title = cleanlaw.getString("title").replaceAll("&#xfffd;D", "-").trim();
                    cleanlaw.put("title", title);
                    mongoDB.updateDocument(this.cleanCollection, cleanlaw);
                }
                num++;
                LOGGER.info("pkulawCleanContent clean num: " + num);
            }
        } catch (Exception e) {
            LOGGER.error("pkulawCleanContent find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        LOGGER.info("Done do pkulawCleanContent...");
    }
}
