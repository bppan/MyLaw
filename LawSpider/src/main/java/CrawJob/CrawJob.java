package CrawJob;

import Log.LawLogger;
import Mongo.MongoDB;
import com.mongodb.client.MongoCollection;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author : Administrator
 * @Date : 2017/11/18 17:13
 * @Description :
 */
public class CrawJob {
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private static Logger LOGGER = LawLogger.getLawLogger(CrawJob.class);
    private String url;
    private String getTime;
    private boolean isCraw;
    private String beginTime;
    private String doneTime;
    private String comments;
    private String title;
    private MongoCollection<Document> crawJobcollection;

    public CrawJob() {
    }

    public CrawJob(String crawJobcollectionName, String title, String url, String... comments) {
        this.crawJobcollection = mongoDB.getCollection(crawJobcollectionName);
        this.title = title;
        this.url = url;
        if (comments.length != 0) {
            this.comments = comments[0];
        }
    }

    public CrawJob(String collectionName) {
        this.setcollection(mongoDB.getCollection(collectionName));
    }

    public static synchronized boolean addJob(MongoCollection<Document> crawJobcollection, String title, String url, String... comments) {
        Document document = new Document("url", url)
                .append("title", title)
                .append("getTime", getCurrentTime())
                .append("isCraw", false)
                .append("beginTime", "")
                .append("doneTime", "");
        if (comments.length != 0) {
            document.append("comments", "Add job:" + comments[0]);
        } else {
            document.append("comments", "Add job!");
        }
        return mongoDB.saveCrawJobDocument(crawJobcollection, document);
    }

    public static synchronized boolean doneJob(MongoCollection<Document> crawJobcollection, String url, String... comments) {
        Document jobInDB = mongoDB.getJobUseUrl(crawJobcollection, url);
        if (jobInDB == null) {
            return false;
        }
        Document doneJob = new Document("url", jobInDB.getString("url"))
                .append("title", jobInDB.getString("title"))
                .append("getTime", jobInDB.getString("getTime"))
                .append("isCraw", true)
                .append("beginTime", jobInDB.getString("beginTime"))
                .append("doneTime", getCurrentTime());
        if (comments.length != 0) {
            doneJob.append("comments", jobInDB.getString("comments") + "\nDone job:" + comments[0]);
        } else {
            doneJob.append("comments", jobInDB.getString("comments") + "\nDone job!");
        }
        return mongoDB.updateCrawJob(crawJobcollection, new Document("url", url), doneJob);
    }

    public static synchronized boolean resetJob(MongoCollection<Document> crawJobcollection, String url, String... comments) {
        Document jobInDB = mongoDB.getJobUseUrl(crawJobcollection, url);
        if (jobInDB == null) {
            return false;
        }
        Document doneJob = new Document("url", jobInDB.getString("url"))
                .append("title", jobInDB.getString("title"))
                .append("getTime", getCurrentTime())
                .append("isCraw", false)
                .append("beginTime", "")
                .append("doneTime", "");
        if (comments.length != 0) {
            doneJob.append("comments", jobInDB.getString("comments") + "\nReset job:" + comments[0]);
        } else {
            doneJob.append("comments", jobInDB.getString("comments") + "\nReset job!");
        }
        return mongoDB.updateCrawJob(crawJobcollection, new Document("url", url), doneJob);
    }

    private static synchronized String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());
    }

    public static synchronized String getJob(MongoCollection<Document> crawJobcollection) {
        Document jobDocument = mongoDB.getCrawJob(crawJobcollection);
        if (jobDocument == null) {
            return "";
        }
        Document doneJob = new Document("url", jobDocument.get("url"))
                .append("title", jobDocument.getString("title"))
                .append("getTime", jobDocument.get("getTime"))
                .append("isCraw", true)
                .append("beginTime", getCurrentTime())
                .append("doneTime", jobDocument.get("doneTime"))
                .append("comments", jobDocument.getString("comments") + "\nGet job!");

        mongoDB.updateCrawJob(crawJobcollection, jobDocument, doneJob);
        return jobDocument.getString("url");
    }

    public static synchronized int getNum(MongoCollection<Document> crawJobcollection) {
        return mongoDB.loadAllCrawJob(crawJobcollection).size();
    }

    public void setcollection(MongoCollection<Document> crawJobcollection) {
        this.crawJobcollection = crawJobcollection;
    }

    public MongoCollection<Document> getCrawJobcollection() {
        return this.crawJobcollection;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getDoneTime() {
        return doneTime;
    }

    public void setDoneTime(String doneTime) {
        this.doneTime = doneTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGetTime() {
        return getTime;
    }

    public void setGetTime(String getTime) {
        this.getTime = getTime;
    }

    public boolean isCraw() {
        return isCraw;
    }

    public void setCraw(boolean craw) {
        isCraw = craw;
    }

}
