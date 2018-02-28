package graph;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import log.MyLogger;
import mongo.MongoServer;
import org.apache.log4j.Logger;
import org.bson.Document;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/2/28 14:55
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class CreateBetweenLawRelationShip {
    private static MongoServer mongoServer = MongoServer.getMongoDB();
    private static Logger LOGGER = MyLogger.getMyLogger(CreateBetweenLawRelationShip.class);
    private static String[] relation = {"依据", "根据", ""};

    public void Create(String fromCollectionName, String toCollectionName){
        MongoCollection<Document> fromCollection = mongoServer.getCollection(fromCollectionName);
        MongoCollection<Document> toCollection = mongoServer.getCollection(toCollectionName);
        FindIterable<Document> iterables = fromCollection.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        int num = 0;
        try {
            while (cursor.hasNext()) {
                Document fromLaw = cursor.next();
                String url = fromLaw.getString("url");
                FindIterable<Document> findIterable = toCollection.find(new Document("url", url)).limit(1).noCursorTimeout(true);
                if(findIterable.first() != null){
                    Document toLaw = findIterable.first();

                }
                long startTime = System.currentTimeMillis();
                long endTime = System.currentTimeMillis();
                num++;
                LOGGER.info("import law num:" + num + " cost time:"+(endTime - startTime));
            }
        } catch (Exception e) {
            LOGGER.info("read data from mongodb err: " + e);
        } finally {
            cursor.close();
        }
    }


}
