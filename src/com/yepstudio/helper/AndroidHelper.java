package com.yepstudio.helper;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.View;
import android.webkit.WebView;

/**
 * 
 * @author zzljob@gmail.com
 * @date 2012-12-21
 *
 */
public class AndroidHelper {

	private static String LOG_TAG = AndroidHelper.class.getSimpleName();
	
	private final static String configFileName = "config.xml";
	//private final static String exeFunctionInJS = "exeFunction";
	public final static String JS_INTERFACE_NAME = "javaInterface";
	private static Map<String, String> constantMap = null;
	
	public static String getConstant(Context context, String key) {
		if (constantMap == null) {
			constantMap = new HashMap<String, String>();
			try {
				AssetManager am = context.getAssets();
				InputStream is = am.open(configFileName);
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				Document document = builder.parse(is);
				Element element = document.getDocumentElement();
				NodeList constantNodeList = element.getElementsByTagName("constant");
				if(constantNodeList == null) {
					return null;
				}
				int length = constantNodeList.getLength();
				String k, v;
				for (int i = 0; i < length; i++) {
					Element e = (Element) constantNodeList.item(i);
					k = e.getAttribute("key");
					v = e.getAttribute("value");
					if(TextUtils.isEmpty(v)){
						v = e.getFirstChild().getNodeValue();
					}
					constantMap.put(k, v);
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, "read config file failed.");
			}
		}
		return constantMap.get(key);
	}
	
	/**
	 * 设置menu菜单的背景为黑色
	 * @param activity
	 */
	public static void setMenuBackground(Activity activity) {
		final LayoutInflater layoutInflater = activity.getLayoutInflater();
		layoutInflater.setFactory(new Factory() {
			@Override
			public View onCreateView(String name, Context context, AttributeSet attrs) {
				if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")) {
					try { // Ask our inflater to create the view
						LayoutInflater f = layoutInflater;
						final View view = f.createView(name, null, attrs);
						new Handler().post(new Runnable() {
							public void run() {
								// view.setBackgroundResource(
								// R.drawable.menu_backg);//设置背景图片
								view.setBackgroundColor(Color.BLACK);// 设置背景色
							}
						});
						return view;
					} catch (InflateException e) {
					} catch (ClassNotFoundException e) {
					}
				}
				return null;
			}
		});
	}

	public static String getAppVersionName(Context context) {
		try {
			return getPackageInfo(context).versionName;
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, "can not find versionName for app.", e);
		}
		return null;
	}

	public static int getAppVersionCode(Context context) {
		try {
			return getPackageInfo(context).versionCode;
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, "can not find versionName for app.", e);
		}
		return -1;
	}

	private static PackageInfo getPackageInfo(Context context)
			throws NameNotFoundException {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
			return info;
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, "can not find PackageInfo for app.", e);
			throw new NameNotFoundException("can not find PackageInfo for app.");
		}
	}

	/**
	 * 根据手机的分辨率从 dip  的单位 转成为 px(像素)
	 * @param context
	 * @param dpValue
	 * @return pxValue
	 */
	public static int dip2px(Context context, float dpValue) {
		return (int) (dpValue * getDensity(context) + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dip
	 * @param context
	 * @param pxValue
	 * @return dpValue
	 */
	public static int px2dip(Context context, float pxValue) {
		return (int) (pxValue / getDensity(context) + 0.5f);
	}
	
	/**
	 * 获取屏幕密度
	 * @param context
	 * @return
	 */
	public static float getDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}

	/**
	 * 获取屏幕宽度
	 * @param context
	 * @return  px(像素)
	 */
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 获取屏幕高度
	 * @param context
	 * @return  px(像素)
	 */
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}
	
	public static boolean isConnectingToInternet(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++){
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean hasWifiConnecting(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return info.getState() ==  NetworkInfo.State.CONNECTED;
	}
	
	private static boolean isSDCardMounted() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	private static boolean isSDCardMountedReadOnly() {
		return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState()); 
	}
	
	public static boolean isSDCardCanWrite(){
		return (isSDCardMounted() && !isSDCardMountedReadOnly());
	}
	
	public static File getStoreDir(Context context){
		String packageName = "com.yepstudio";
		try {
			packageName = getPackageInfo(context).packageName;
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, "can not find packageName.", e);
		} 
		StringBuilder builder = new StringBuilder();
		if(!isSDCardCanWrite()){
			builder.append(Environment.getDataDirectory().getAbsolutePath());
			builder.append(File.separator);
			builder.append("data").append(File.separator);
			builder.append(packageName).append(File.separator);
		}else{
			builder.append(Environment.getExternalStorageDirectory().getAbsolutePath());
			builder.append(File.separator);
			builder.append(packageName);
		}
		File file = new File(builder.toString());
		if(!file.exists()){
			file.mkdirs();
		}
		return file;
	}
		
	public static void exeSimpleJsFunction(WebView webView, String functionName, String ...args){
		StringBuilder js = new StringBuilder("javascript:");
		js.append(functionName).append("(");
		int length = args == null ? 0 : args.length;
		String str = "";
		for (int i = 0; i < length; i++) {
			str = args[i].replaceAll("\n", "");
			str = str.replaceAll("'", "\'");
			js.append("'").append(str).append("',");
		}
		if(length > 0){
			js.deleteCharAt(js.length() - 1);
		}
		js.append(")");
		//Log.d(LOG_TAG, js.toString());
		webView.loadUrl(js.toString());
	}
}
