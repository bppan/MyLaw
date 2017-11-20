package CrawJob;

import Log.LawLogger;
import Mongo.MongoDB;
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
    private String comments ;
    private String title;

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

    public static synchronized boolean addJob(String title, String url, String... comments) {
        Document document = new Document("url", url)
                .append("title",title)
                .append("getTime", getCurrentTime())
                .append("isCraw", false)
                .append("beginTime", "")
                .append("doneTime", "");
        if(comments.length != 0){
            document.append("comments", "Add job:" + comments[0]);
        }else {
            document.append("comments", "Add job!");
        }
        return mongoDB.saveCrawJobDocument(document);
    }

    public static synchronized boolean doneJob(String url, String... comments) {
        Document jobInDB = mongoDB.getJobUseUrl(url);
        if (jobInDB == null) {
            return false;
        }
        Document doneJob = new Document("url", jobInDB.getString("url"))
                .append("title", jobInDB.getString("title"))
                .append("getTime", jobInDB.getString("getTime"))
                .append("isCraw", true)
                .append("beginTime", jobInDB.getString("beginTime"))
                .append("doneTime", getCurrentTime());

        if (comments.length != 0){
            doneJob.append("comments", jobInDB.getString("comments") + "\nDone job:" + comments[0]);
        }else {
            doneJob.append("comments", jobInDB.getString("comments") + "\nDone job!");
        }
        return mongoDB.updateCrawJob(new Document("url", url), doneJob);
    }

    public static synchronized boolean resetJob(String url, String... comments) {
        Document jobInDB = mongoDB.getJobUseUrl(url);
        if (jobInDB == null) {
            return false;
        }
        Document doneJob = new Document("url", jobInDB.getString("url"))
                .append("title", jobInDB.getString("title"))
                .append("getTime", getCurrentTime())
                .append("isCraw", false)
                .append("beginTime", "")
                .append("doneTime", "");
        if (comments.length != 0){
            doneJob.append("comments", jobInDB.getString("comments") + "\nReset job:" + comments[0]);
        }else {
            doneJob.append("comments", jobInDB.getString("comments") + "\nReset job!");
        }

        return mongoDB.updateCrawJob(new Document("url", url), doneJob);
    }

    private static String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());
    }

    public static synchronized String getJob() {
        Document job = mongoDB.getCrawJob();
        if (job == null) {
            return "";
        }
        Document doneJob = new Document("url", job.get("url"))
                .append("title", job.getString("title"))
                .append("getTime", job.get("getTime"))
                .append("isCraw", true)
                .append("beginTime", getCurrentTime())
                .append("doneTime", job.get("doneTime"))
                .append("comments", job.getString("comments") + "\nGet job!");

        mongoDB.updateCrawJob(job, doneJob);

        return job.getString("url");
    }

    public static synchronized int getCrawJobNum() {
        return mongoDB.loadAllCrawJob().size();
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
