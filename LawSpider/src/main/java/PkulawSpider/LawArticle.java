package PkulawSpider;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author : Administrator
 * @Date : 2017/11/14 22:37
 * @Description :
 */
public class LawArticle {
    private String name;

    private List<String> paragraph;

    public LawArticle() {
        this.name = "";
        this.paragraph = new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getParagraph() {
        return paragraph;
    }

    public void setParagraph(List<String> paragraph) {
        this.paragraph = paragraph;
    }
}
