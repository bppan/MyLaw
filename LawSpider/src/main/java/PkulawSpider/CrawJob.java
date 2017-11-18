package PkulawSpider;

import Mongo.MongoDB;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;

/**
 * @Author : Administrator
 * @Date : 2017/11/18 17:13
 * @Description :
 */
public class CrawJob {
    private String url;
    private String getTime;
    private boolean isCraw;

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

    public synchronized boolean saveToDB(){
        MongoDB mongoDB = MongoDB.getMongoDB();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String currentTime = df.format(new Date());
        Document document = new Document("url", this.getUrl()).append("getTime", currentTime).append("isCraw", false);
        return mongoDB.saveCrawJobDocument(document);
    }

}
