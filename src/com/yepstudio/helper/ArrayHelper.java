package com.yepstudio.helper;

import java.util.LinkedList;
import java.util.List;

/**
 * 数组操作工具类
 * @author zzljob@gmail.com
 * @date 2012-12-21
 *
 */
public class ArrayHelper {
	
	/**
	 * 把数组组装成字符串
	 * <p>推荐使用StringHelper.join(String[] inputArray, String separator)</p>
	 * @param inputArray
	 * @param glueString
	 * @return
	 */
	@Deprecated
	public static String implodeArray(String[] inputArray, String glueString) {
		return StringHelper.join(inputArray, glueString);
	}
	
	/**
	 * 合并字符串数组
	 * @param args0
	 * @param args1
	 * @return
	 */
	public static String[] mergeArray(String[] args0, String[] args1){
		Object[] objs = mergeArray(args0, args1, null);
		int length = objs.length;
		String[] strs = new String[objs.length];
		for (int i = 0; i < length; i++) {
			strs[i] = objs[i].toString();
		}
		return strs;
	}
	
	/**
	 * 合并数组
	 * @param args
	 * @return
	 */
	public static Object[] mergeArray(Object[] ...args){
		if(args == null || args.length < 1){
			return null;
		}
		List<Object> list = new LinkedList<Object>();
		for (Object[] objects : args) {
			if(objects != null && objects.length > 0){
				for (Object object : objects) {
					list.add(object);
				}
			}
		}
		return list.toArray();
	}
}
