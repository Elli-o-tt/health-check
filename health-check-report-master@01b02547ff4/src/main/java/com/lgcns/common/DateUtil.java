package com.lgcns.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static String addDay(Date date, int day){
        SimpleDateFormat sdf = new SimpleDateFormat(MsgCode.DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);

        return sdf.format(calendar.getTime());
    }
}
