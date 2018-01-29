package ChinacourtSpider;

import Log.LawLogger;
import Mongo.MongoDB;
import SimHash.SimHash;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/1/26 22:14
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class ChinacourtCleanContent {
    private static Logger LOGGER = LawLogger.getLawLogger(ChinacourtCleanContent.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private MongoCollection<Document> lawCollecion;
    private MongoCollection<Document> cleanCollection;

    public ChinacourtCleanContent(String lawCollection, String cleanCollection) {
        this.lawCollecion = mongoDB.getCollection(lawCollection);
        this.cleanCollection = mongoDB.getCollection(cleanCollection);
    }

    public static void main(String[] args) {
        ChinacourtCleanContent chinacourtCleanContent = new ChinacourtCleanContent("chinacourt_clean", "law2");
        chinacourtCleanContent.cleanContent();
    }

    public void cleanContent() {
        LOGGER.info("Begin do cleanContent...");
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
                    String[] contentList = cleanlaw.getString("content").split("\n");
                    StringBuilder updateContent = new StringBuilder();
                    boolean department = false;
                    boolean release_date = false;
                    boolean implement_date = false;
                    boolean release_number = false;
                    boolean timeless = false;
                    boolean category = false;
                    boolean source = false;
                    for (String contentpar : contentList) {
                        if (contentpar.indexOf("【发布单位】") == 0 && !department) {
                            department = true;
                            continue;
                        }
                        if (contentpar.indexOf("【发布日期】") == 0 && !release_date) {
                            release_date = true;
                            continue;
                        }
                        if (contentpar.indexOf("【生效日期】") == 0 && !implement_date) {
                            implement_date = true;
                            continue;
                        }
                        if (contentpar.indexOf("【失效日期】") == 0 && !timeless) {
                            timeless = true;
                            continue;
                        }
                        if (contentpar.indexOf("【发布文号】") == 0 && !release_number) {
                            release_number = true;
                            continue;
                        }
                        if (contentpar.indexOf("【所属类别】") == 0 && !category) {
                            category = true;
                            continue;
                        }
                        if (contentpar.indexOf("【文件来源】") == 0 && !source) {
                            source = true;
                            continue;
                        }
                        updateContent.append(contentpar.trim()).append("\n");
                    }
                    cleanlaw.put("content", updateContent.toString());
                    SimHash simHash = new SimHash(updateContent.toString());
                    cleanlaw.put("simHash", simHash.getIntSimHash().toString());
                    cleanlaw.put("simHashPart1", simHash.getStrSimHash().substring(0, 16));
                    cleanlaw.put("simHashPart2", simHash.getStrSimHash().substring(16, 32));
                    cleanlaw.put("simHashPart3", simHash.getStrSimHash().substring(32, 48));
                    cleanlaw.put("simHashPart4", simHash.getStrSimHash().substring(48, 64));
                    mongoDB.updateDocument(this.cleanCollection, cleanlaw);
                }
                num++;
                LOGGER.info("doClean clean num: " + num);
            }
        } catch (Exception e) {
            LOGGER.error("do clean find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        LOGGER.info("Done do cleanContent...");
    }
}
