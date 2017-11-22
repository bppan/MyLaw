package Mongo;

import Mongo.LawArticle;
import Mongo.MongoDB;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author : Administrator
 * @Date : 2017/11/14 22:07
 * @Description :
 */
public class LawDocument {
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    private String title;
    private String department;
    private String release_data;
    private String release_number;
    private String implement_date;
    private String category;
    private String level;
    private String timeless;
    private List<LawArticle> article;
    private String rawHtml;
    private String cleanHtml;
    private String url;
    private int tiaoNum;
    private MongoCollection<Document> lawcollection;

    public LawDocument(){}

    public LawDocument(String lawDocumentName){
        lawcollection = mongoDB.getCollection(lawDocumentName);
    }

    public String getRelease_number() {
        return release_number;
    }

    public void setRelease_number(String release_number) {
        this.release_number = release_number;
    }

    public void setCollection(MongoCollection<Document> collection){
        lawcollection = collection;
    }

    public MongoCollection<Document> getLawcollection(){
        return lawcollection;
    }

    public static boolean isExits(MongoCollection<Document> lawcollection, String url) {
        return mongoDB.isLawDocumentExits(lawcollection, url);
    }

    public String getCleanHtml() {
        return cleanHtml;
    }

    public void setCleanHtml(String cleanHtml) {
        this.cleanHtml = cleanHtml;
    }

    public int getTiaoNum() {
        return tiaoNum;
    }

    public void setTiaoNum(int tiaoNum) {
        this.tiaoNum = tiaoNum;
    }

    public String getRawHtml() {
        return rawHtml;
    }

    public void setRawHtml(String rawHtml) {
        this.rawHtml = rawHtml;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getImplement_date() {
        return implement_date;
    }

    public void setImplement_date(String implement_date) {
        this.implement_date = implement_date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTimeless() {
        return timeless;
    }

    public void setTimeless(String timeless) {
        this.timeless = timeless;
    }

    public List<LawArticle> getArticle() {
        return article;
    }

    public void setArticle(List<LawArticle> article) {
        this.article = article;
    }

    public String getRelease_data() {
        return release_data;
    }

    public void setRelease_data(String release_data) {
        this.release_data = release_data;
    }

    public static boolean saveToDB(LawDocument lawDocument) {
        MongoDB mongoDB = MongoDB.getMongoDB();
        List<Document> interlDocuments = new ArrayList<Document>();
        for (int i = 0; i < lawDocument.article.size(); i++) {
            Document par = new Document("name", lawDocument.article.get(i).getName()).append("paragraph", lawDocument.article.get(i).getParagraph());
            interlDocuments.add(par);
        }
        Document document = new Document("title", lawDocument.getTitle()).
                append("department", lawDocument.getDepartment()).
                append("release_date", lawDocument.getRelease_data()).
                append("release_number", lawDocument.getRelease_number()).
                append("implement_date", lawDocument.getImplement_date()).
                append("category", lawDocument.getCategory()).
                append("level", lawDocument.getLevel()).
                append("timeless", lawDocument.getTimeless()).
                append("articles", interlDocuments).
                append("url", lawDocument.getUrl()).
                append("rawHtml", lawDocument.getRawHtml()).
                append("content", lawDocument.getCleanHtml()).
                append("article_num", lawDocument.getTiaoNum());

        return mongoDB.saveLawDocument(lawDocument.getLawcollection(), document);
    }

}

