package PkulawSpider;

import Interface.LawClean;
import Log.LawLogger;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description：
 * Author: Administrator
 * Created:  2017/12/21 13:13
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class PkulawClean extends LawClean {

    private static Logger LOGGER = LawLogger.getLawLogger(PkulawClean.class);

    public PkulawClean(String crawJobCollection, String lawCollection, String cleanCollection) {
        super(crawJobCollection, lawCollection, cleanCollection);
    }

    public String getContentHtmlBySelect(String html) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            doc.select("#div_content > font").first().remove();
            doc.select(".TiaoYinV2").remove();
            return doc.select("#div_content").first().html();
        } catch (Exception e) {
            return "";
        }
    }

    public void cleanContent(Document law) {
        String html = law.getString("rawHtml");
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        Element elementTbody = doc.select("#tbl_content_main > tbody").first();
        Elements elements = elementTbody.getElementsByTag("tr");
        for (Element element : elements) {
            List<Node> nodes = element.childNodes();
            if (nodes.size() == 0) {
                continue;
            }
            if (isRightAttribute(nodes, "【发布部门】")) {
                String department = getContentAttribute(nodes, "【发布部门】");
                law.put("department", department);
            }
            if (isRightAttribute(nodes, "【发文字号】")) {
                String release_number = getContentAttribute(nodes, "【发文字号】");
                law.put("release_number", release_number);
            }
            if (isRightAttribute(nodes, "【发布日期】")) {
                String release_date = getContentAttribute(nodes, "【发布日期】");
                law.put("release_date", release_date);
            }
            if (isRightAttribute(nodes, "【实施日期】")) {
                String implement_date = getContentAttribute(nodes, "【实施日期】");
                law.put("implement_date", implement_date);
            }
            if (isRightAttribute(nodes, "【时效性】")) {
                String timeless = getContentAttribute(nodes, "【时效性】");
                law.put("timeless", timeless);
            }
            if (isRightAttribute(nodes, "【法规类别】")) {
                String category = getContentAttribute(nodes, "【法规类别】");
                law.put("category", category);
            }
            if (isRightAttribute(nodes, "【效力级别】")) {
                String level = getContentAttribute(nodes, "【效力级别】");
                law.put("level", level);
            }
        }
        super.cleanContent(law);
    }

    private boolean isRightAttribute(List<Node> nodes, String tag) {
        for (Node node : nodes) {
            String result = node.toString();
            if (result.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private String getContentAttribute(List<Node> nodes, String tag) {
        for (Node node : nodes) {
            String result = node.toString();
            String regEx2_html = "<[^>]+>"; // 定义HTML标签的正则表达式
            Pattern p2_html = Pattern.compile(regEx2_html, Pattern.CASE_INSENSITIVE);
            Matcher m2_html = p2_html.matcher(result);
            result = m2_html.replaceAll("").replaceAll(" ", "").trim(); // 过滤html标签
            if (result.contains(tag)) {
                return result.replace(tag, "");
            }
        }
        return "";
    }
}
