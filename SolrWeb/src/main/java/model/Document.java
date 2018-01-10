package model;

import com.sun.xml.internal.ws.api.ha.StickyFeature;
import org.apache.solr.common.SolrDocument;

import java.util.List;
import java.util.Map;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/9 15:52
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Document {
    private String title;
    private String id;
    private String department;
    private String release_date;
    private String release_number;
    private String implement_date;
    private String category;
    private String level;
    private String timeless;
    private String url;
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content.replaceAll("\n", " ");
    }
    public Document(SolrDocument doc){
        this.setId(doc.getFieldValue("id").toString());
        this.setTitle(doc.getFieldValue("title").toString());
        this.setDepartment(doc.getFieldValue("department").toString());
        this.setRelease_date(doc.getFieldValue("release_date").toString());
        this.setRelease_number(doc.getFieldValue("release_number").toString());
        this.setImplement_date(doc.getFieldValue("implement_date").toString());
        this.setCategory(doc.getFieldValue("category").toString());
        this.setLevel(doc.getFieldValue("level").toString());
        this.setTimeless(doc.getFieldValue("timeless").toString());
        this.setUrl(doc.getFieldValue("url").toString());
        this.setContent(doc.getFieldValue("content").toString());
    }

    public void setHighLight(Map<String, Map<String, List<String>>> highlightresult){
        if (highlightresult.get(this.getId()) != null) {
            Map<String, List<String>> hightMap = highlightresult.get(this.getId());
            if(hightMap.get("content") != null){
                String fieldHL = hightMap.get("content").get(0);
                this.setContent(fieldHL);
            }else {
                String subContent = this.getContent().substring(0, 200);
                this.setContent(subContent);
            }
            if(hightMap.get("title") != null){
                String fieldHL = hightMap.get("title").get(0);
                this.setTitle(fieldHL);
            }
            if(hightMap.get("department") != null){
                String fieldHL = hightMap.get("department").get(0);
                this.setDepartment(fieldHL);
            }
            if(hightMap.get("release_number") != null){
                String fieldHL = hightMap.get("release_number").get(0);
                this.setRelease_number(fieldHL);
            }
            if(hightMap.get("category") != null){
                String fieldHL = hightMap.get("category").get(0);
                this.setCategory(fieldHL);
            }
            if(hightMap.get("level") != null){
                String fieldHL = hightMap.get("category").get(0);
                this.setCategory(fieldHL);
            }
            if(hightMap.get("timeless") != null){
                String fieldHL = hightMap.get("category").get(0);
                this.setCategory(fieldHL);
            }
        }
    }
    public Document(){ }
}
