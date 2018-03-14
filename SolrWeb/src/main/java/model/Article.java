package model;

import org.bson.Document;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/3/14 16:46
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Article {
    private String name;
    private String id;
    private String department;
    private String release_date;
    private String release_number;
    private String implement_date;
    private String category;
    private String level;
    private String timeless;
    private String articleContent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getRelease_number() {
        return release_number;
    }

    public void setRelease_number(String release_number) {
        this.release_number = release_number;
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


    public String getArticleContent() {
        return articleContent;
    }

    public void setArticleContent(String articleContent) {
        this.articleContent = articleContent;
    }

    public void setProperty(Document lawDocument){
        if(lawDocument.getString("department") != null){
            this.department = lawDocument.getString("department");
        }
        if(lawDocument.getString("release_date") != null){
            this.department = lawDocument.getString("release_date");
        }
        if(lawDocument.getString("release_number") != null){
            this.department = lawDocument.getString("release_number");
        }
        if(lawDocument.getString("implement_date") != null){
            this.department = lawDocument.getString("implement_date");
        }
        if(lawDocument.getString("category") != null){
            this.department = lawDocument.getString("category");
        }
        if(lawDocument.getString("level") != null){
            this.department = lawDocument.getString("level");
        }
        if(lawDocument.getString("timeless") != null){
            this.department = lawDocument.getString("timeless");
        }
    }
}
