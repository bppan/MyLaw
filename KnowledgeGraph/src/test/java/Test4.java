import util.DateParse;
import util.NumberChange;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/2/28 17:31
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class Test4 {
    public static void main(String[] args){

        String test = "通知(2-23)";
        Pattern regEx_zhang = Pattern.compile("通知" +"((（(.*?)）)|(\\((.*?)\\)))*" , Pattern.CASE_INSENSITIVE);
        Matcher m_zhang = regEx_zhang.matcher(test);
        if(m_zhang.find()){
            System.out.println(m_zhang.groupCount());
            System.out.println("length:" + test.length());
            System.out.println("end:" + m_zhang.end());
            System.out.println(m_zhang.group(0));
            String ee = m_zhang.group(1);
            System.out.println(m_zhang.group(1));
            System.out.println(m_zhang.group(2));
            System.out.println(m_zhang.group(3));
            System.out.println(m_zhang.group(4));
        }

        String pp = "第五百条";
        System.out.println(pp.split("-").length);
        System.out.println(pp.substring(0, 2));

        String subSentence = "9（1234）";
        String kuaoHao = "（(.*?)）";//定义括号
        Pattern regEx_kuaoHao = Pattern.compile(kuaoHao, Pattern.CASE_INSENSITIVE);
        Matcher m_kuaoHao = regEx_kuaoHao.matcher(subSentence);
        System.out.println(subSentence.length());
        if(m_kuaoHao.find()){
            System.out.println(m_kuaoHao.end());
        }
        Pattern p = Pattern.compile("(\\d{1,4}[-|\\/|年|\\.]\\d{1,2}[-|\\/|月|\\.]\\d{1,2}([日|号])+)", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
        Matcher matcher = p.matcher("卫生部2000年1月16日发布的");
        if(matcher.find()){
            System.out.println(matcher.group());
        }
        Pattern pm = Pattern.compile("(\\d{1,4}[-|\\/|年|\\.]\\d{1,2}月)", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
        Matcher matcher2 = pm.matcher("卫生部2000年1月16日发布的");
        if(matcher2.find()){
            System.out.println(matcher2.group());
        }
        Pattern py = Pattern.compile("(\\d{1,4}年)", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
        Matcher matcher3 = py.matcher("卫生部2000年1月16日发布的");
        if(matcher3.find()){
            System.out.println(matcher3.group());
        }

        try{
            String time= "2014年5月";
            Calendar cal = Calendar.getInstance();
            cal.setTime(DateParse.getStringToDate(time));
            System.out.println(cal.get(Calendar.YEAR));
            System.out.println(cal.get(Calendar.MONTH) + 1);
            System.out.println(cal.get(Calendar.DAY_OF_MONTH));
//            System.out.println(DateParse.getStringToDate("2014年10月23").get);
        }catch (Exception e){
            System.out.println(e);
        }
        Pattern titleRemoveAlter = Pattern.compile("(《(.*?)》)(第[零一二三四五六七八九十百千万]+条)?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        String test2 = "dsd《中华人民共和国刑法》第五条sdsd";
        Matcher matcher3p = titleRemoveAlter.matcher(test2);
        while (matcher3p.find()){
            System.out.println("start "+ matcher3p.start());
            System.out.println("start "+ test2.substring(matcher3p.start()));
            System.out.println(matcher3p.group(0));
            System.out.println(matcher3p.group(1));
            System.out.println(matcher3p.group(2));
            System.out.println(matcher3p.group(3));

        }



    }
}
