import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author : Administrator
 * @Date : 2017/11/14 22:07
 * @Description :
 */
public class LawCollection {
    private String title;
    private String department;
    private String release_data;
    private String implement_date;
    private String category;
    private String level;
    private String timeless;
    private List<LawArticle> article;

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

    public void setArticle( List<LawArticle> article) {
        this.article = article;
    }

    public String getRelease_data() {
        return release_data;
    }

    public void setRelease_data(String release_data) {
        this.release_data = release_data;
    }
    public Document getCollection(){

        List<Document> interlDocuments = new ArrayList<Document>();
        for (int i = 0; i < article.size(); i++){
            Document par = new Document("name", article.get(i).getName()).append("paragraph", article.get(i).getParagraph());
            interlDocuments.add(par);
        }
        Document document = new Document("title", this.getTitle()).
                append("department", this.getDepartment()).
                append("release_date", this.getRelease_data()).
                append("implement_date", this.getImplement_date()).
                append("category", this.getCategory()).
                append("level", this.getLevel()).
                append("timeless", this.getTimeless()).
                append("articles", interlDocuments);

        return document;

    }

}

