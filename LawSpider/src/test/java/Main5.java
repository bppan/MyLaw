import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author : Administrator
 * @Date : 2017/11/17 15:36
 * @Description :
 */
public class Main5 {
    public static void main(String[] args){
//        MongoDB mongoDB = MongoDB.getMongoDB();
//        System.out.println(mongoDB.getNamespace());
//
//        FindIterable<Document> findIterable = mongoDB.collection.find(new Document("url","http://www.pkulaw.cn/fulltext_form.aspx?Db=chl&Gid=a5270ef910a13181&keyword=&EncodingName=&Search_Mode=&Search_IsTitle=0"));
//        findIterable.forEach(new Block<Document>() {
//            public void apply(final Document document) {
//                System.out.println(document.get("title"));
//            }
//        });
//        if (findIterable.first() == null){
//            System.out.println("aaa");
//        }

//        CrawJob.doneJob("http://www.pkulaw.cn/fulltext_form.aspx?Db=chl&Gid=105c796092fb2031&keyword=&EncodingName=&Search_Mode=accurate&Search_IsTitle=0");
//        CrawJob.getJob();


        String title = "中华人民共和国海关法(2017北大法宝整理版)";
        String regEx_delet = "\\(.*北大法宝.*\\)";//定义空格回车换行符
        Pattern p_space = Pattern.compile(regEx_delet, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(title);
        title = m_space.replaceAll("");
        System.out.println(":" + title);
//        lawDocument.setTitle(title);
//        if(mongoDB.collection.find(new Document("department","全国人民代表大会")).to != null){
//            System.out.println("no");
//        }else {
//            System.out.println("yes");
//        }
    }
}
