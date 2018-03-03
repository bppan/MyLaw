package CleanContent;

import Interface.LawClean;
import Interface.LawSpider;
import Log.LawLogger;
import Mongo.LawArticle;
import Mongo.LawDocument;
import Mongo.MongoDB;
import SimHash.SimHash;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/11 17:06
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class CleanRedundancy extends LawClean {
    private static Logger LOGGER = LawLogger.getLawLogger(CleanRedundancy.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();

    public CleanRedundancy(String lawCollection, String cleanCollection) {
        super(lawCollection, cleanCollection);
    }

    public static void main(String[] args) {
        CleanRedundancy cleanRedundancy = new CleanRedundancy("law3", "law3");
        cleanRedundancy.redoRemoveTitleEqualLawStrict();
//        cleanRedundancy.redoRemoveAndAlterLaw();
    }

    public String getContentHtmlBySelect(String html) {
        return "";
    }

    public void redoRemoveAndAlterLaw() {
        LOGGER.info("Begin do redoRemoveAndAlter...");
        FindIterable<Document> iterables = this.getLawCollecion().find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                num++;
                LOGGER.info("doClean clean num: " + num);
                String lawTitle = law.getString("title");
                String content = law.getString("content");
                if (content.indexOf("万方数据知识服务平台-法规检索结果【新版入口】") == 0) {
                    LOGGER.info("contains 万方数据知识服务平台-法规检索结果:" + lawTitle);
                    mongoDB.deleteDocumentOneById(this.getLawCollecion(), law);
                    continue;
                }
                if (content.trim().indexOf("系统忙，请稍候再试") == 0) {
                    LOGGER.info("find -系统忙，请稍候再试 replace that" + lawTitle);
                    mongoDB.deleteDocumentOneById(this.getLawCollecion(), law);
                    continue;
                }
                if (lawTitle.contains("-法律教育网")) {
                    LOGGER.info("find -法律教育网" + lawTitle);
                    String[] contentList = law.getString("content").split("\n");
                    StringBuilder updateContent = new StringBuilder();
                    for (String contentpar : contentList) {
                        if (contentpar.trim().isEmpty()) {
                            continue;
                        }
                        if (contentpar.trim().equals("感动同情无聊愤怒搞笑难过高兴路过")) {
                            break;
                        }
                        updateContent.append(contentpar.trim()).append("\n");
                    }
                    law.put("content", updateContent.toString());
                    List<LawArticle> articleList = LawSpider.getLawArticleAndParagraph(updateContent.toString());
                    List<Document> interlDocuments = LawDocument.getArticleDocument(articleList);
                    law.put("article_num", interlDocuments.size());
                    law.put("articles", interlDocuments);
                    SimHash simHash = new SimHash(updateContent.toString());
                    law.put("simHash", simHash.getIntSimHash().toString());
                    law.put("simHashPart1", simHash.getStrSimHash().substring(0, 16));
                    law.put("simHashPart2", simHash.getStrSimHash().substring(16, 32));
                    law.put("simHashPart3", simHash.getStrSimHash().substring(32, 48));
                    law.put("simHashPart4", simHash.getStrSimHash().substring(48, 64));
                    law.put("title", lawTitle.replace("-法律教育网", ""));
                }
                if (content.indexOf("万方数据法律数据库") == 0) {
                    LOGGER.info("find -万方数据法律数据库 replace that" + lawTitle);
                    law.put("content", law.getString("content").replace("万方数据法律数据库", ""));
                }
                String releaseDate = getFormateStringDate(law.getString("release_date"));
                String implementDate = getFormateStringDate(law.getString("implement_date"));
                law.put("release_date", releaseDate);
                law.put("implement_date", implementDate);
                String release_number = law.getString("release_number");
                if (release_number == null) {
                    release_number = "";
                } else {
                    release_number = release_number.replaceAll("〔", "[").replaceAll("〕", "]");
                }
                law.put("release_number", release_number);
                mongoDB.updateDocument(getLawCollecion(), law);
            }
        } catch (Exception e) {
            LOGGER.error("do clean find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        LOGGER.info("Done do redoRemoveAndAlter...");
    }

    public void redoRemoveTitleEqualLaw() {
        LOGGER.info("Begin do redoRemoveTitleEqualLaw...");
        FindIterable<Document> iterables = this.getLawCollecion().find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                num++;
                LOGGER.info("do redoRemoveTitleEqualLaw num: " + num);
                String title = law.getString("title");
                int article_num = law.getInteger("article_num");
                String release_date = law.getString("release_date");
                List<Document> repeateTitleList = new ArrayList<>();
                FindIterable<Document> iterablesRepeateTitle = this.getCleanCollection().find(new Document("title", title)).noCursorTimeout(true).batchSize(10000);
                MongoCursor<Document> cursorRepeateTitle = iterablesRepeateTitle.iterator();
                try {
                    while (cursorRepeateTitle.hasNext()) {
                        Document law_repeate = cursorRepeateTitle.next();
                        int repeate_article_num = law_repeate.getInteger("article_num");
                        String repeate_release_date = law_repeate.getString("release_date");
                        if (article_num == repeate_article_num && release_date.equals(repeate_release_date)) {
                            repeateTitleList.add(law_repeate);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("find clean Collection err:" + e);
                } finally {
                    cursorRepeateTitle.close();
                }
                LOGGER.info("Title:" + title + " " + repeateTitleList.size());
                if (repeateTitleList.size() <= 1) {
                    continue;
                }
                for (int i = 0; i < repeateTitleList.size() - 1; i++) {
                    mongoDB.deleteDocumentOneById(getCleanCollection(), repeateTitleList.get(i));
                    LOGGER.info("delete: " + i + " success...");
                }
            }
        } catch (Exception e) {
            LOGGER.error("do clean find error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        LOGGER.info("Done do redoRemoveTitleEqualLaw...");
    }

    public void redoRemoveTitleEqualLawStrict() {
        LOGGER.info("Begin do redoRemoveTitleEqualLawStrict...");
        FindIterable<Document> iterables = this.getLawCollecion().find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        long num = 0;
        try {
            while (cursor.hasNext()) {
                Document law = cursor.next();
                num++;
                LOGGER.info("do redoRemoveTitleEqualLawStrict num: " + num);
                String title = law.getString("title");
                String release_date = law.getString("release_date");
                List<Document> repeateTitleList = new ArrayList<>();
                FindIterable<Document> iterablesRepeateTitle = this.getCleanCollection().find(new Document("title", title)).noCursorTimeout(true).batchSize(10000);
                MongoCursor<Document> cursorRepeateTitle = iterablesRepeateTitle.iterator();
                try {
                    while (cursorRepeateTitle.hasNext()) {
                        Document law_repeate = cursorRepeateTitle.next();
                        String repeate_release_date = law_repeate.getString("release_date");
                        if (release_date.equals(repeate_release_date)) {
                            repeateTitleList.add(law_repeate);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("find redoRemoveTitleEqualLawStrict err:" + e);
                } finally {
                    cursorRepeateTitle.close();
                }
                LOGGER.info("Title:" + title + " " + repeateTitleList.size());
                if (repeateTitleList.size() <= 1) {
                    continue;
                }
                for (int i = 0; i < repeateTitleList.size() - 1; i++) {
                    mongoDB.deleteDocumentOneById(getCleanCollection(), repeateTitleList.get(i));
                    LOGGER.info("delete: " + i + " success...");
                }
            }
        } catch (Exception e) {
            LOGGER.error("do redoRemoveTitleEqualLawStrict error: " + e.getMessage());
        } finally {
            cursor.close();
        }
        LOGGER.info("Done do redoRemoveTitleEqualLawStrict...");
    }

}
