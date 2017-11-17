import PkulawSpider.LawArticle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author : Administrator
 * @Date : 2017/11/15 13:35
 * @Description :
 */
public class Main2 {
    public static void main(String[] args) {
        String htmlUrl = "http://www.pkulaw.cn/fulltext_form.aspx?Db=chl&Gid=3bd7d2e9ab95ff64&keyword=&EncodingName=&Search_Mode=&Search_IsTitle=0";
        try {
            Document doc = Jsoup.connect(htmlUrl)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Host", "http://www.pkulaw.cn")
                    .header("Connection", "keep-alive")
                    .header("Cache-Control", "max-age=0")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36")
                    .timeout(5000)
                    .get();

            String title = doc.select("#tbl_content_main > tbody > tr:nth-child(1) > td > span > strong").first().childNode(0).toString();
            System.out.println(title);
            String department = doc.select("#tbl_content_main > tbody > tr:nth-child(2) > td:nth-child(1) > a").first().childNode(0).toString();
            System.out.println(department);
            String implement_date = doc.select("#tbl_content_main > tbody > tr:nth-child(3) > td:nth-child(2)").first().childNode(1).toString();
            System.out.println(implement_date);
            String relase_date = doc.select("#tbl_content_main > tbody > tr:nth-child(3) > td:nth-child(1)").first().childNode(1).toString();
            System.out.println(relase_date);
            String level = doc.select("#tbl_content_main > tbody > tr:nth-child(5) > td").first().text();
            System.out.println(level);
            String timeless = doc.select("#tbl_content_main > tbody > tr:nth-child(4) > td:nth-child(1) > a").first().childNode(0).toString();
            System.out.println(timeless);
            String category = doc.select("#tbl_content_main > tbody > tr:nth-child(5) > td > a").first().childNode(0).toString();
            System.out.println(category);
            Elements eles = doc.getElementsByClass("TiaoNoA");
            System.out.println(eles.toArray().length);

            String html = doc.select(" #div_content").first().html();
//            System.out.println(html);
            String regEx_space = "\\s*|\t|\r|\n";//定义空格回车换行符
            Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
            Matcher m_space = p_space.matcher(html);
            html = m_space.replaceAll("").replaceAll("　", ""); // 过滤空格回车标签
            String regEx_html = "<br>|<br />|<br/>|</p>|</div>"; // 定义HTML标签的正则表达式
            Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            Matcher m_html = p_html.matcher(html);
            html = m_html.replaceAll("\n"); // 过滤html标签
//            System.out.println(html);
            String regEx2_html = "<[^>]+>"; // 定义HTML标签的正则表达式
            Pattern p2_html = Pattern.compile(regEx2_html, Pattern.CASE_INSENSITIVE);
            Matcher m2_html = p2_html.matcher(html);
            html = m2_html.replaceAll(""); // 过滤html标签
            System.out.println(html);

            String[] result = html.split("\r|\n");
            String tiao = "第[一二三四五六七八九十百千万]+条";//定义空格回车换行符
            String xiang = "（[一二三四五六七八九十百千万]+）";//定义空格回车换行符
            boolean tiao_in = false;
            String current_tiao = "";
            String current_kuan = "";

            List<String> current_tiao_kuan = new ArrayList<String>();
            List<LawArticle> lawArticleList = new ArrayList<LawArticle>();
            LawArticle currentLaw = new LawArticle();
            for (int i = 0; i < result.length; i++) {
                String par = result[i].trim();
                if (!par.isEmpty()) {
                    Pattern regEx_tiao = Pattern.compile(tiao, Pattern.CASE_INSENSITIVE);
                    Matcher m_tiao = regEx_tiao.matcher(par);

                    Pattern regEx_tiao_xiang = Pattern.compile(xiang, Pattern.CASE_INSENSITIVE);
                    Matcher m_tiao_xiang = regEx_tiao_xiang.matcher(par);

                    if (m_tiao.find() && m_tiao.start() == 0) {
                        if (!tiao_in) {
                            tiao_in = true;
                        }
                        if (!current_kuan.isEmpty()) {
                            currentLaw.getParagraph().add(current_kuan);
                            lawArticleList.add(currentLaw);
                        }

                        LawArticle law = new LawArticle();
                        String name = par.substring(m_tiao.start(), m_tiao.end());
                        law.setName(name);
                        current_kuan = par.substring(m_tiao.end(), par.length()).trim();
                        currentLaw = law;
                        continue;
                    }

                    if (m_tiao_xiang.find() && tiao_in && m_tiao_xiang.start() == 0) {
                        current_kuan += par;
                        continue;
                    }
                    if (tiao_in) {
                        if (!current_kuan.isEmpty()) {
                            currentLaw.getParagraph().add(current_kuan);
                        }
                        current_kuan = par.trim();
                    }
                }
            }
            currentLaw.getParagraph().add(current_kuan);
            lawArticleList.add(currentLaw);


            String test = "dddd第三三三千条";
            String regEx_space_tiao = "第[一二三四五六七八九十百千万]+条";//定义空格回车换行符
            Pattern regEx_tiao = Pattern.compile(regEx_space_tiao, Pattern.CASE_INSENSITIVE);
            Matcher m_space_2 = regEx_tiao.matcher(test);
            System.out.println(m_space_2.find());
            System.out.println(test.substring(m_space_2.start(), m_space_2.end()));
//            for(int i - 0; i < result.)
//            String regEx_space = "\\s*";//定义空格回车换行符
//            Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
//            Matcher m_space = p_space.matcher(html);
//            html = m_space.replaceAll(""); // 过滤空格回车标签

//            System.out.println(html);

        } catch (IOException e) {

        }


    }
}
