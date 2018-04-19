package com.emg.update.tool;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataUtil {

	/**
	 * 把指定格式的字符串转换成日期型
	 * 
	 * @param dateStr
	 * @return
	 */
	public static Date stringToDate(String dateStr) {
		// 注意：SimpleDateFormat构造函数的样式与strDate的样式必须相符
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 加上时间
		// 必须捕获异常
		try {
			return sDateFormat.parse(dateStr);
		} catch (ParseException px) {
			px.printStackTrace();
		}
		return null;
	}

	/***
	 * 把日期转换成字符串
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date) {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return f.format(date);
	}

	/**
	 * 对采用UTF-8形式编码的数据进行解码
	 * 
	 * @param value
	 * @return
	 */
	public static String decodeValue(String value) {
		if (value == null || value.trim().length() == 0)
			return null;
		try {
			return java.net.URLDecoder.decode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}
	
	/**
	 * 对采用UTF-8形式编码的数据进行解码
	 * 
	 * @param value
	 * @return
	 */
	public static String encode(String value) {
		if (value == null || value.trim().length() == 0)
			return null;
		try {
			return java.net.URLEncoder.encode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

}
