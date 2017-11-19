package PkulawSpider;

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

    public static synchronized boolean addJob(String url) {
        Document document = new Document("url", url)
                .append("getTime", getCurrentTime())
                .append("isCraw", false)
                .append("beginTime", "")
                .append("doneTime", "");
        return mongoDB.saveCrawJobDocument(document);
    }

    public static synchronized boolean doneJob(String url) {
        Document jobInDB = mongoDB.getJobUseUrl(url);
        if (jobInDB == null) {
            return false;
        }
        Document doneJob = new Document("url", jobInDB.getString("url"))
                .append("getTime", jobInDB.getString("getTime"))
                .append("isCraw", true)
                .append("beginTime", jobInDB.getString("beginTime"))
                .append("doneTime", getCurrentTime());

        return mongoDB.updateCrawJob(new Document("url", url), doneJob);
    }

    public static synchronized boolean resetJob(String url) {
        Document jobInDB = mongoDB.getJobUseUrl(url);
        if (jobInDB == null) {
            return false;
        }
        Document doneJob = new Document("url", jobInDB.getString("url"))
                .append("getTime", getCurrentTime())
                .append("isCraw", false)
                .append("beginTime", "")
                .append("doneTime", "");
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
                .append("getTime", job.get("getTime"))
                .append("isCraw", true)
                .append("beginTime", getCurrentTime())
                .append("doneTime", job.get("doneTime"));
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
