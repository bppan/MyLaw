import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/2/28 15:16
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Test2 {
    public static void main(String[] args){
//        String zhang = "《(.*?)》";//定义章数
//        String zhang = "（(.*?)）";//定义章数
        String zhang = "依据";//定义章数
        String par = "为规范医疗器械分类，依据《医疗器械监督管理条例》（以下简称行政复议法），依据《医疗器械监督管理条例23》";
        Pattern regEx_zhang = Pattern.compile(zhang, Pattern.CASE_INSENSITIVE);
        Matcher m_zhang = regEx_zhang.matcher(par);
        while (m_zhang.find()) {
            String book = m_zhang.group(0);
            System.out.println(book);
            System.out.println(m_zhang.start());
            System.out.println(par.charAt(m_zhang.start()+zhang.length()));
        }

//        System.out.println(par.);
//        System.out.println(par.indexOf(zhang));
    }
}
