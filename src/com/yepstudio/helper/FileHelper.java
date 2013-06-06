package com.yepstudio.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * 文件操作工具类
 * 
 * @author zzljob@gmail.com
 * @date 2013-2-17
 * 
 */
public class FileHelper {

	private static String LOG_TAG = FileHelper.class.getSimpleName();

	public static void copyFile(File source, File target) throws IOException {
		InputStream inStream = new FileInputStream(source);
		writeFile(inStream, target, false);
		inStream.close();
	}

	public static String readFile(File file) {
		if (file == null) {
			return null;
		}
		try {
			return readFile(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			Log.e(LOG_TAG, "file can not find : " + file.getAbsolutePath());
		}
		return null;
	}

	public static String readFile(InputStream inStream) {
		try {
			StringBuilder builder = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(inStream);
			int l = 1024;
			char[] buffer = new char[l];
			int lenghtRead = 0;
			while ((lenghtRead = reader.read(buffer)) > 0) {
				builder.append(buffer, 0, lenghtRead);
			}
			reader.close();
			return builder.toString();
		} catch (IOException e) {
			Log.e(LOG_TAG, "read file from InputStream fail.", e);
		}
		return null;
	}

	public static void writeFile(String content, File target, boolean append)
			throws IOException {
		target.getParentFile().mkdirs();
		if (!append) {
			deleteOldFileIfExists(target);
		}
		FileWriter writer = new FileWriter(target);
		writer.append(content);
		writer.flush();
		writer.close();
	}

	public static void writeFile(InputStream sourceStream, File target,
			boolean append) throws IOException {
		target.getParentFile().mkdirs();
		if (!append) {
			deleteOldFileIfExists(target);
		}
		OutputStream outStream = new FileOutputStream(target);
		write(sourceStream, outStream);
		outStream.flush();
		outStream.close();
	}

	public static void deleteOldFileIfExists(File file) {
		deleteFileAndDir(file);
	}

	public static void write(InputStream sourceStream, OutputStream targetStream)
			throws IOException {
		int lenght = 1024;
		byte[] buffer = new byte[lenght];
		int lenghtRead = 0;
		while ((lenghtRead = sourceStream.read(buffer, 0, lenght)) > 0) {
			targetStream.write(buffer, 0, lenghtRead);
		}
		targetStream.flush();
	}

	public static String getMD5HexString(String str) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_TAG,
					"NoSuchAlgorithmException when MessageDigest.getInstance(MD5)");
		}
		try {
			return byteArrayToHexString(md.digest(str.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Log.e(LOG_TAG, "getMD5HexString fail, str:" + str, e);
		}
		return null;
	}

	public static String getMD5HexString(File file) throws IOException {
		FileInputStream inStream = new FileInputStream(file);
		return getMD5HexString(inStream);
	}

	public static String getMD5HexString(InputStream inStream)
			throws IOException {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_TAG,
					"NoSuchAlgorithmException when MessageDigest.getInstance(MD5)");
		}
		int lenght = 1024;
		byte[] buffer = new byte[lenght];
		int lenghtRead = 0;
		while ((lenghtRead = inStream.read(buffer, 0, lenght)) > 0) {
			md.update(buffer, 0, lenghtRead);
		}
		return byteArrayToHexString(md.digest());
	}

	private static String byteArrayToHexString(byte[] array) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : array) {
			int intVal = b & 0xff;
			if (intVal < 0x10) {
				hexString.append("0");
			}
			hexString.append(Integer.toHexString(intVal));
		}
		return hexString.toString();
	}

	public static Bitmap getBitmapOfImage(InputStream inStream) {
		if (inStream == null) {
			return null;
		}
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		return BitmapFactory.decodeStream(inStream, null, opt);
	}

	public static Bitmap getBitmapOfImage(File file) {
		if (file == null) {
			return null;
		}
		try {
			return getBitmapOfImage(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			Log.e(LOG_TAG, "when getBitmapOfImage FileNotFoundException, file:"
					+ file.getAbsolutePath(), e);
		}
		return null;
	}

	public static Bitmap getBitmapOfImage(String url) {
		URL urlObj = null;
		InputStream inStream = null;
		Bitmap bitmap = null;
		HttpURLConnection conn = null;
		try {
			urlObj = new URL(url);
			conn = (HttpURLConnection) urlObj.openConnection();
			// conn.setRequestProperty("Accept-Encoding", "identity");
			conn.connect();
			if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
				Log.e(LOG_TAG,
						String.format(
								"download image fail when getBitmapOfImage from url:%s. ResponseCode:%s",
								url, conn.getResponseCode()));
				return null;
			}
			inStream = conn.getInputStream();

			bitmap = getBitmapOfImage(inStream);
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, String.format("is not a good url:%s", url), e);
		} catch (IOException e) {
			Log.e(LOG_TAG, "has IOException", e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return bitmap;
	}

	public static BigDecimal getTotalSpace(File file, String type, int scale) {
		if (file == null || !file.exists()) {
			return new BigDecimal(0);
		}
		if (file.isFile()) {
			return getSpace(new BigDecimal(file.length()), type, scale);
		}
		File[] list = file.listFiles();
		BigDecimal bdSpace = new BigDecimal(0);
		int length = list == null ? 0 : list.length;
		File child = null;
		for (int i = 0; i < length; i++) {
			child = list[i];
			bdSpace = bdSpace.add(getTotalSpace(child, "", 50));
		}
		return getSpace(bdSpace, type, 2);
	}

	/**
	 * 主要是兼容android2.2,2.3
	 * @param bdSpace
	 * @param type
	 * @param scale
	 * @return
	 */
	private static BigDecimal getSpace(BigDecimal bdSpace, String type,
			int scale) {
		if (bdSpace == null) {
			return null;
		}
		BigDecimal divide = null;
		if ("G".equalsIgnoreCase(type)) {
			divide = new BigDecimal(1024 * 1024 * 1024);
		} else if ("M".equalsIgnoreCase(type)) {
			divide = new BigDecimal(1024 * 1024);
		} else if ("K".equalsIgnoreCase(type)) {
			divide = new BigDecimal(1024);
		} else {
			divide = new BigDecimal(1);
		}
		return bdSpace.divide(divide, scale, BigDecimal.ROUND_HALF_UP);
	}

	public static boolean deleteFileAndDir(File file) {
		if (file == null || !file.equals(file)) {
			return true;
		}
		if (file.isFile()) {
			return file.delete();
		}
		File[] list = file.listFiles();
		int length = list == null ? 0 : list.length;
		for (int i = 0; i < length; i++) {
			deleteFileAndDir(list[i]);
			Log.d(LOG_TAG, String.format("delete file : %s",
					list[i].getAbsolutePath()));
		}
		return file.delete();
	}
}
