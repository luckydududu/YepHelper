package com.yepstudio.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字符串处理方法
 * 
 * @author zzljob@gmail.com
 * @date 2013-3-1
 * 
 */
public class StringHelper {

	/**
	 * 是否是空字符串
	 * <ul>
	 * <li>isEmpty(null) = true;</li>
	 * <li>isEmpty("") = true;</li>
	 * <li>isEmpty("    ") = false;</li>
	 * <li>isEmpty("  aaa  ") = false;</li>
	 * </ul>
	 * 
	 * @param string
	 * @return
	 */
	public static boolean isEmpty(String string) {
		return (string == null || string.length() < 1);
	}

	public static boolean hasEmpty(String... strs) {
		if (strs == null || strs.length < 1) {
			return true;
		}
		for (String string : strs) {
			if (isEmpty(string)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasContains(String string, String... str) {
		if (isEmpty(string)) {
			return false;
		}
		if (hasEmpty(str)) {
			return true;
		}
		for (String s : str) {
			if (string.contains(s)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean allContains(String string, String... str) {
		if (isEmpty(string)) {
			return false;
		}
		for (String s : str) {
			if (!string.contains(s)) {
				return false;
			}
		}
		return true;
	}

	public static boolean allEmpty(String... strs) {
		if (strs == null || strs.length < 1) {
			return true;
		}
		for (String string : strs) {
			if (!isEmpty(string)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 字符串是否是空白的
	 * <ul>
	 * <li>isBlank(null) = true;</li>
	 * <li>isBlank("") = true;</li>
	 * <li>isBlank("    ") = true;</li>
	 * <li>isBlank("  aaa  ") = false;</li>
	 * </ul>
	 * 
	 * @param string
	 * @return
	 */
	public static boolean isBlank(String string) {
		return isEmpty(string) || isEmpty(string.trim());
	}

	public static boolean hasBlank(String... strs) {
		if (strs == null || strs.length < 1) {
			return true;
		}
		for (String string : strs) {
			if (isBlank(string)) {
				return true;
			}
		}
		return false;
	}

	public static boolean allBlank(String... strs) {
		if (strs == null || strs.length < 1) {
			return true;
		}
		for (String string : strs) {
			if (!isBlank(string)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 去掉前后空白
	 * <ul>
	 * <li>trim("") = ""</li>
	 * <li>trim(null) = ""</li>
	 * <li>trim("   aa   ") = "aa"</li>
	 * <li>trim("aa") = "aa"</li>
	 * </ul>
	 * 
	 * @param string
	 * @return
	 */
	public static String trim(String string) {
		return isEmpty(string) ? "" : string.trim();
	}

	public static String[] split(String string, String regularExpression,
			boolean removeEmpty) {
		if (isEmpty(string)) {
			return null;
		}
		String[] strArr = string.split(regularExpression);
		if (strArr == null || strArr.length < 1) {
			return null;
		}
		if (!removeEmpty) {
			return strArr;
		}
		List<String> list = new ArrayList<String>();
		for (String str : strArr) {
			if (!isBlank(str)) {
				list.add(str);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * 截取一段字符串
	 * <ul>
	 * <li>subStringBefore(null, "a") = ""</li>
	 * <li>subStringBefore("", "a") = ""</li>
	 * <li>subStringBefore("abcd,ed", ",") = "abcd"</li>
	 * <li>subStringBefore("abcd,ed,f", ",") = "abcd"</li>
	 * </ul>
	 * 
	 * @param text
	 * @param s
	 * @return
	 */
	public static String subStringBefore(String text, String s) {
		return subStringBefore(text, s, false, false);
	}

	/**
	 * 截取一段字符串
	 * <ul>
	 * <li>subStringBefore(null, "a", true/false) = ""</li>
	 * <li>subStringBefore("", "a", true/false) = ""</li>
	 * <li>subStringBefore("abcd,ed", ",", true/false) = "abcd"</li>
	 * <li>subStringBefore("abcd,ed,f", ",", false) = "abcd"</li>
	 * <li>subStringBefore("abcd,ed,f", ",", true) = "abcd,ed"</li>
	 * </ul>
	 * 
	 * @param text
	 * @param s
	 * @param isLast
	 * @return
	 */
	public static String subStringBefore(String text, String s, boolean isLast) {
		return subStringBefore(text, s, isLast, false);
	}

	/**
	 * 截取一段字符串
	 * <ul>
	 * <li>subStringBefore(null, "a", true/false, true/false) = ""</li>
	 * <li>subStringBefore("", "a", true/false, true/false) = ""</li>
	 * <li>subStringBefore("abcd,ed", ",", true/false, true) = "abcd,"</li>
	 * <li>subStringBefore("abcd,ed", ",", true/false, false) = "abcd"</li>
	 * <li>subStringBefore("abcd,ed,f", ",", false, false) = "abcd"</li>
	 * <li>subStringBefore("abcd,ed,f", ",", false, true) = "abcd,"</li>
	 * <li>subStringBefore("abcd,ed,f", ",", true, false) = "abcd,ed"</li>
	 * <li>subStringBefore("abcd,ed,f", ",", true, true) = "abcd,ed,"</li>
	 * </ul>
	 * 
	 * @param text
	 * @param s
	 * @param isLast
	 * @param isContain
	 * @return
	 */
	public static String subStringBefore(String text, String s, boolean isLast,
			boolean isContain) {
		if (isEmpty(text)) {
			return "";
		}
		if (isEmpty(s)) {
			return text;
		}
		int index = isLast ? text.lastIndexOf(s) : text.indexOf(s);
		if (index < 0) {
			return text;
		}
		int end = isContain ? (index + s.length()) : index;
		end = Math.min(end, text.length());
		return text.substring(0, end);
	}

	public static String subStringAfter(String text, String s) {
		return subStringAfter(text, s, false, false);
	}

	public static String subStringAfter(String text, String s, boolean isFrist) {
		return subStringAfter(text, s, isFrist, false);
	}

	public static String subStringAfter(String text, String s, boolean isFrist,
			boolean isContain) {
		if (isEmpty(text)) {
			return "";
		}
		if (isEmpty(s)) {
			return text;
		}
		int index = !isFrist ? text.lastIndexOf(s) : text.indexOf(s);
		if (index < 0) {
			return text;
		}
		int start = isContain ? index : (index + s.length());
		int length = text.length();
		start = Math.min(start, length);
		return text.substring(start, length);
	}

	public static String subStringBetween(String text, String start, String end) {
		return subStringBetween(text, start, true, end, true);
	}

	public static String subStringBetween(String text, String start,
			boolean isFrist, String end, boolean isLast) {
		return subStringBetween(text, start, isFrist, false, end, isLast, false);
	}

	public static String subStringBetween(String text, String start,
			boolean isFrist, boolean isContainStart, String end,
			boolean isLast, boolean isContainEnd) {
		if (isEmpty(text)) {
			return "";
		}
		int length = text.length();
		int s;
		if (isEmpty(start)) {
			s = 0;
		} else {
			s = isFrist ? text.indexOf(start) : text.lastIndexOf(start);
			s = isContainStart ? s : s + start.length();
		}
		s = Math.min(s, length);
		int e;
		if (isEmpty(end)) {
			e = length;
		} else {
			e = isLast ? text.lastIndexOf(end) : text.indexOf(end);
			e = isContainEnd ? e + end.length() : e;
		}
		e = Math.min(e, length);
		return text.substring(Math.min(s, e), Math.max(s, e));
	}

	public static String join(List<?> list, String separator) {
		String output = "";

		int length = list != null ? list.size() : 0;
		if (length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(list.get(0));

			for (int i = 1; i < length; i++) {
				sb.append(list.get(i));
				sb.append(separator);
			}

			output = sb.toString();
		}

		return output;
	}

	public static String join(Set<?> set, String separator) {
		String output = "";

		if (set != null) {
			int i = 0;
			StringBuilder sb = new StringBuilder();
			for (Object object : set) {
				sb.append(i <= 0 ? "" : separator);
				sb.append(object);
				i++;
			}
			output = sb.toString();
		}

		return output;
	}

	public static String join(Map<?, ?> map, String separator,
			boolean isKeyOrValue) {
		String output = "";
		if (map == null) {
			return output;
		}
		if (isKeyOrValue) {
			output = join(map.keySet(), separator);
		} else {
			int i = 0;
			StringBuilder sb = new StringBuilder();
			for (Object key : map.keySet()) {
				sb.append(i <= 0 ? "" : separator);
				sb.append(map.get(key));
				i++;
			}
			output = sb.toString();
		}
		return output;
	}

	public static String join(Map<?, ?> map, String connector, String separator) {
		String output = "";

		if (map != null) {
			int i = 0;
			StringBuilder sb = new StringBuilder();
			for (Object key : map.keySet()) {
				sb.append(i <= 0 ? "" : separator);
				sb.append(key).append(connector).append(map.get(key));
				i++;
			}
			output = sb.toString();
		}

		return output;
	}

	public static String join(String[] inputArray, String separator) {
		String output = "";

		if (inputArray != null && inputArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(inputArray[0]);

			for (int i = 1; i < inputArray.length; i++) {
				sb.append(separator);
				sb.append(inputArray[i]);
			}

			output = sb.toString();
		}

		return output;
	}

}
