package com.lzz.util;

import java.text.SimpleDateFormat;

/**
 * Created by gl49 on 2018/1/20.
 */
public class TimeUtil {
    public static String timeFormat(long time){
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d = format.format(time);
        return d;
    }

    public static int getTime(){
        return (int) (System.currentTimeMillis());
    }
}
