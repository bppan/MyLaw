package util;

import log.MyLogger;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/3/1 15:26
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class DateParse {
    private static Logger LOGGER = MyLogger.getMyLogger(DateParse.class);

    public static Date getStringToDate(String time) {
        List<SimpleDateFormat> formatList = new ArrayList<>();
        formatList.add(new SimpleDateFormat("yyyy-MM-dd"));
        formatList.add(new SimpleDateFormat("yyyy-M-d"));
        formatList.add(new SimpleDateFormat("yyyy年MM月dd日"));
        formatList.add(new SimpleDateFormat("yyyy年M月d日"));
        formatList.add(new SimpleDateFormat("yyyy.MM.dd"));
        formatList.add(new SimpleDateFormat("yyyy.M.d"));
        formatList.add(new SimpleDateFormat("yyyy年MM月"));
        formatList.add(new SimpleDateFormat("yyyy年M月"));
        formatList.add(new SimpleDateFormat("yyyy年"));
        Date date = null;
        for (SimpleDateFormat format : formatList) {
            try {
                date = format.parse(time);
                break;
            } catch (ParseException e) {
                continue;
            }
        }
        return date;
    }

    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public static int getMonthOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getDayofMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }
}
