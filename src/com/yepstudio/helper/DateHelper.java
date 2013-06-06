package com.yepstudio.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.text.TextUtils;
import android.util.Log;

/**
 * 日期辅助类
 * 
 * @author zzljob@gmail.com
 * @date 2012-12-19
 * 
 */
public class DateHelper {

	private static String LOG_TAG = DateHelper.class.getSimpleName();

	private final static Locale DEFAULT_LOCALE = Locale.getDefault();
	private final static String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static String format(Date date, String format, Locale locale) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		return sdf.format(date);
	}

	public static String format(Date date, String format, String defValue) {
		String v = format(date, format, DEFAULT_LOCALE);
		return TextUtils.isEmpty(v) ? defValue : v;
	}

	public static String format(Date date, String format) {
		return format(date, format, DEFAULT_LOCALE);
	}

	public static String format(Date date) {
		return format(date, DEFAULT_FORMAT);
	}

	public static Date parse(String string, String format, Locale locale) {
		if (TextUtils.isEmpty(string)) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		try {
			return sdf.parse(string);
		} catch (ParseException e) {
			Log.e(LOG_TAG, String.format(
					"String parse to Date fail, String:%s, format:%s", string,
					format), e);
		}
		return null;
	}

	public static Date parse(String string, String format) {
		return parse(string, format, DEFAULT_LOCALE);
	}

	public static Date parse(String string) {
		return parse(string, DEFAULT_FORMAT);
	}

	public static Integer compareTo(Date a, Date b) {
		if (a == null || b == null) {
			return null;
		}
		return a.compareTo(b);
	}

	public static Integer compareTo(Date a, boolean formatA, String dateB,
			String format) {
		Date d1 = formatA ? parse(format(a, format), format) : a;
		Date d2 = parse(dateB, format);
		return compareTo(d1, d2);
	}

	public static Integer compareTo(Date a, Date b, String format) {
		Date d1 = parse(format(a, format), format);
		Date d2 = parse(format(b, format), format);
		return compareTo(d1, d2);
	}

	public static Integer compareTo(String dateA, String formatA, String dateB,
			String formatB) {
		return compareTo(parse(dateA, formatA), parse(dateB, formatB));
	}

	public static Integer compareTo(String dateA, String dateB, String format) {
		return compareTo(parse(dateA, format), parse(dateB, format));
	}
}
