package com.yepstudio.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.text.TextUtils;


/**
 * 用作模拟浏览器发起HTTP请求
 * 
 * @author zzljob@gmail.com
 * 
 */
public class HttpHelper {
	
	private static Logger logger = Logger.getLogger(HttpHelper.class);
	
	public static String WAP_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; zh-cn; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
	public static String WEB_USER_AGENT = "Mozilla/5.0 (Windows NT 5.1; rv:15.0) Gecko/20100101 Firefox/15.0.1";
	
	// private static String[] encodeArr = {"multipart/form-data", "application/x-www-form-urlencoded"};
	private static Map<String, String> defaultHeaders = new HashMap<String, String>();
	static {
		defaultHeaders.put("User-Agent", WEB_USER_AGENT);
		defaultHeaders.put("Accept", "*/*");
		defaultHeaders.put("Accept-Language", "zh-cn");
		defaultHeaders.put("Accept-Charset", "utf-8");
		defaultHeaders.put("Connection", "keep-alive");
		defaultHeaders.put("Cache-Control", "no-cache");
	}

	private int requestTimeout;
	private int waitingTimeout;
	private int maxRetryTimes;
	private boolean useGZIP;
	private DefaultHttpClient httpClient;
	
	private Context context;
	
	public HttpHelper(Context context){
		this(context, 1000*60*1, 1000*60*1, 3, true);
	}
	
	public HttpHelper(Context context, int requestTimeout, int waitingTimeout, int maxRetryTimes, boolean useGZIP) {
		super();
		this.requestTimeout = requestTimeout;
		this.waitingTimeout = waitingTimeout;
		this.maxRetryTimes = maxRetryTimes;
		this.useGZIP = useGZIP;
		this.context = context;
		this.httpClient = this.createHttpClient(this.context);
	}
	
	public CookieStore getCookieStore() {
		return httpClient.getCookieStore();
	}

	public void addCookie(Cookie cookie) {
		httpClient.getCookieStore().addCookie(cookie);
	}

	public String doGetBody(URL url, String referer) {
		return doGetBody(url, referer, false, null);
	}
	
	public String createGetParams(List<NameValuePair> list) {
		if(list == null){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (NameValuePair p : list) {
			sb.append(i <= 0 ? "" : "&");
			sb.append(p.getName()).append("=").append(p.getValue());
			i++;
		}
		return sb.toString();
	}
	
	public String doGetBody(URL url, String referer, boolean isAjax, String userAgent) {
		// step 1:发送请求，获得字节流
		HttpEntity entity = doGet(url, referer, isAjax, userAgent);
		if (entity == null) {
			return null;
		}

		String resultBody = null;
		// step 2:输出字节流到resultBody
		try {
			resultBody = EntityUtils.toString(entity);
			entity.consumeContent();
		} catch (org.apache.http.ParseException e) {
			logger.error("EntityUtils.toString(entity, HTTP.UTF_8) has error. header elements cannot be parsed.", e);
		} catch (IOException e) {
			logger.error("get getbody after request has IOException.", e);
		}

		logger.trace("GetBody content: " + resultBody);
		return resultBody;
	}

	public InputStream doGetStream(URL url, String referer) {
		return doGetStream(url, referer, false, null);
	}
	
	public InputStream doGetStream(URL url, String referer, boolean isAjax, String userAgent) {
		HttpEntity entity = doGet(url, referer, isAjax, userAgent);
		if (entity == null) {
			return null;
		}
		
		InputStream stream = null;
		try {
			File target = getStoreFile(url);
			FileHelper.writeFile(entity.getContent(), target, false);
			stream = new FileInputStream(target);
			entity.consumeContent();
			logger.debug(String.format("write file{%s} form url{%s}.", target.getAbsolutePath(), url.toString()));
		} catch (Exception e) {
			logger.error("get Stream after doGet fail.", e);
		}
		return stream;
	}
	
	private HttpEntity doGet(URL url, String referer, boolean isAjax, String userAgent) {
		if (url == null) {
			return null;
		}
		logger.debug("GET method Request URL : " + url.toString());
		
		BasicHttpContext httpContext = new BasicHttpContext();
		HttpGet httpget = new HttpGet(url.toString());
		// step 1:处理User-Agent和Referer
		if (!TextUtils.isEmpty(userAgent)) {
			httpget.addHeader("User-Agent", userAgent);
		}
		if (!TextUtils.isEmpty(referer)) {
			httpget.addHeader("Referer", referer);
		}
		if (isAjax) {
			httpget.addHeader("X-Requested-With", "XMLHttpRequest");
		}
		try {
			//step 2:发送请求
			HttpResponse response = httpClient.execute(httpget, httpContext);
			
			//step 3:处理请求结果
			HttpEntity entity = response.getEntity();
			if(entity == null){
				logger.error("HttpEntity is null in HttpResponse.");
				return null;
			}
			if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
				entity.consumeContent();
				logger.error("doGetBody return null. HttpResponse StatusCode : " + response.getStatusLine().getStatusCode());
				return null;
			}
			
			logger.trace("ContentType : " + entity.getContentType());
			logger.trace("ContentLength : " + entity.getContentLength());
			logger.trace("ContentEncoding : " + entity.getContentEncoding());
			return entity;
		} catch (IOException e) {
			httpget.abort();
			logger.error("has IOException.", e);
		}
		return null;
	}
	
	public String doPostBody(URL url, HttpEntity formEntity, String referer) {
		return doPostBody(url, formEntity, referer, false, null);
	}
	
	public String doPostBody(URL url, HttpEntity formEntity, String referer, boolean isAjax, String userAgent) {
		// step 1:发送请求，获得字节流
		HttpEntity entity = doPost(url, formEntity, referer, isAjax, userAgent);

		String resultBody = null;
		// step 2:输出字节流到resultBody
		try {
			resultBody = EntityUtils.toString(entity);
			entity.consumeContent();
		} catch (org.apache.http.ParseException e) {
			logger.error("EntityUtils.toString(entity, HTTP.UTF_8) has error. header elements cannot be parsed.", e);
		} catch (IOException e) {
			logger.error("get doPostBody after request has IOException.", e);
		}

		logger.trace("doPostBody content: " + resultBody);
		return resultBody;
	}
	
	public InputStream doPostStream(URL url, HttpEntity formEntity, String referer) {
		return doPostStream(url, formEntity, referer, false, null);
	}
	
	public InputStream doPostStream(URL url, HttpEntity formEntity, String referer, boolean isAjax, String userAgent) {
		HttpEntity entity = doPost(url, formEntity, referer, isAjax, userAgent);
		if (entity == null) {
			return null;
		}
		
		InputStream stream = null;
		try {
			File target = getStoreFile(url);
			FileHelper.writeFile(entity.getContent(), target, false);
			stream = new FileInputStream(target);
			entity.consumeContent();
			logger.debug(String.format("write file{%s} for do post url{%s}.", target.getAbsolutePath(), url.toString()));
		} catch (Exception e) {
			logger.error("get Stream after doPost fail.", e);
		}
		return stream;
	}
	
	private HttpEntity doPost(URL url, HttpEntity formEntity, String referer, boolean isAjax, String userAgent){
		logger.debug("POST method Request URL : " + url.toString());
		//step 1:处理User-Agent
		BasicHttpContext httpContext = new BasicHttpContext();
		HttpPost httpPost = new HttpPost(url.toString());
		if (!TextUtils.isEmpty(referer)) {
			httpPost.setHeader("Referer", referer);
		}
		if (!TextUtils.isEmpty(userAgent)) {
			httpPost.setHeader("User-Agent", userAgent);
		}
		if (isAjax) {
			httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
		}
		
		//step 2:处理编码问题
		try {
			httpPost.setEntity(formEntity);
			
			//step 3:发送请求
			HttpResponse response = httpClient.execute(httpPost, httpContext);
			
			HttpEntity entity = response.getEntity();
			if(entity == null){
				logger.error("HttpEntity is null in HttpResponse.");
				return null;
			}
			//step 4:处理请求
			if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
				entity.consumeContent();
				logger.debug("doPostBody return null. HttpResponse StatusCode : " + response.getStatusLine().getStatusCode());
				return null;
			}
			
			logger.debug("ContentType : " + entity.getContentType());
			logger.debug("ContentLength : " + entity.getContentLength());
			logger.debug("ContentEncoding : " + entity.getContentEncoding());
			
			return entity;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public HttpEntity createFormEntity(Map<String, String> postParams) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		if (postParams != null) {
			for (String key : postParams.keySet()) {
				list.add(new BasicNameValuePair(key, postParams.get(key)));
				logger.debug(String.format("createFormEntity:%s=%s", key, postParams.get(key)));
			}
		}
		return createFormEntity(list, HTTP.UTF_8);
	} 
	
	public HttpEntity createFormEntity(List<NameValuePair> list, String encoding) {
		HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(list, encoding);
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException", e);
		}
		return entity;
	}
	
	public HttpEntity createFormEntity(Map<String, String> postParams, Map<String, File> fileParams) {
		MultipartEntity entity = new MultipartEntity();
		logger.debug("CreateFormEntity for HttpRequest : ");
		int pl = 0, fl = 0;
		if(postParams != null){
			for (String key : postParams.keySet()) {
				try {
					entity.addPart(key, new StringBody(postParams.get(key), Charset.forName(HTTP.UTF_8)));
					logger.trace("\t\t  " + key + " : " + postParams.get(key));
				} catch (UnsupportedEncodingException e) {
					logger.error("UnsupportedEncodingException", e);
				}
			}
			pl = postParams.size();
		}
		if(fileParams != null){
			for (String key : fileParams.keySet()) {
				entity.addPart(key, new FileBody(fileParams.get(key)));
				logger.trace("\t\t  " + key + " : " + fileParams.get(key).getAbsolutePath());
			}
			pl = fileParams.size();
		}
		logger.trace(String.format("Print HttpRequest PostParams End. Count : %s.", pl + fl));
		return entity;
	}
	
	private DefaultHttpClient createHttpClient(Context context) {
		logger.debug(String.format("HttpClient is creating, request Timeout : %s. Waiting for data Timeout : %s. Max Retry time : %s.", requestTimeout, waitingTimeout, maxRetryTimes));
		
		BasicHttpParams httpParams = new BasicHttpParams();
		
		HttpConnectionParams.setConnectionTimeout(httpParams, requestTimeout);
		HttpConnectionParams.setSoTimeout(httpParams, waitingTimeout);
		
		HttpClientParams.setRedirecting(httpParams, true);//自动重定向
		HttpClientParams.setCookiePolicy(httpParams, CookiePolicy.BROWSER_COMPATIBILITY);// 浏览器一样的宽松cookie验证
		
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
	    
		DefaultHttpClient httpclient = null;
		try{
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);
	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);//忽略域名验证
		
	        SchemeRegistry schemeRegistry = new SchemeRegistry();
	        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        logger.debug(String.format("register Scheme for %s, default port : %s.", "http", 80));
	        schemeRegistry.register(new Scheme("https", sf, 443));
	        logger.debug(String.format("register Scheme for https, protocol : %s. default port : %s.", "https", 443));
			
			ClientConnectionManager connonManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
			
			//创建HttpClient
			httpclient = new DefaultHttpClient(connonManager, httpParams);
			
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                // 获取当前正在使用的APN接入点
                Uri uri = Uri.parse("content://telephony/carriers/preferapn");
                Cursor mCursor = context.getContentResolver().query(uri, null, null, null, null);
                if (mCursor != null && mCursor.moveToFirst()) {
                    // 游标移至第一条记录，当然也只有一条
                    String proxyStr = mCursor.getString(mCursor.getColumnIndex("proxy"));
                    if (proxyStr != null && proxyStr.trim().length() > 0) {
                        HttpHost proxy = new HttpHost(proxyStr, 80);
                        httpclient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
                    }
                    mCursor.close();
                }
            }
		}catch (Exception e) {
			logger.error("init HttpClient with SchemeRegistry fail.", e);
			httpclient = new DefaultHttpClient();
		}
		
		// 设置恢复策略，在发生异常时候将自动重试N次
		httpclient.setHttpRequestRetryHandler(new HttpRequestRetryHandler(){

			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				if (executionCount > maxRetryTimes) {
					// 如果超过最大重试次数，那么就不要继续了
					logger.debug("request has IOException, but retry too many times. so retry stop. ");
					return false;
				}
				if (exception instanceof NoHttpResponseException) {
					// 如果服务器丢掉了连接，那么就重试
					logger.debug("request has NoHttpResponseException, request retry ... ");
					return true;
				}
				if (exception instanceof SSLHandshakeException) {
					// 不要重试SSL握手异常
					logger.debug("request has SSLHandshakeException, request retry stop. ");
					return false;
				}
				HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
				if(!(request instanceof HttpEntityEnclosingRequest)){
					// 如果请求被认为是幂等的，那么就重试
					logger.debug("request was HttpEntityEnclosingRequest, request retry ... ");
					return true;
				}
				logger.debug("request has unknowable IOException, request retry ... ");
				return true;
			}
			
		});
		
		httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
				request.removeHeaders("Accept-Encoding");
				if (useGZIP) {
					request.addHeader("Accept-Encoding", "gzip");
					logger.debug("request can be used GZIP for reduced.  Accept-Encoding : gzip.");
				}
				
				for (String key : defaultHeaders.keySet()) {
					if (!request.containsHeader(key)) {
						request.addHeader(key, defaultHeaders.get(key));
					}
				}

				printHeader(request.getAllHeaders());
				
				CookieStore cookieStore = (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
				printCookies(cookieStore);
			}
		});
		httpclient.addResponseInterceptor(new HttpResponseInterceptor() {
			public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
				CookieStore cookieStore = (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
				//HttpHelper.this.cookieStore = cookieStore;
				
				printCookies(cookieStore);
				printHeader(response.getAllHeaders());
				
				HttpEntity entity = response.getEntity();
                Header ceheader = entity.getContentEncoding();
                if (ceheader != null) {
                    HeaderElement[] codecs = ceheader.getElements();
                    for (int i = 0; i < codecs.length; i++) {
                        if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
			}
		});
		return httpclient;
	}
	
	private static class GzipDecompressingEntity extends HttpEntityWrapper {

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent()
            throws IOException, IllegalStateException {

            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }

    }
	
	private static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
                KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
	
	public void clearCookie(){
		httpClient.getCookieStore().clear();
	}
	
	public void shutdownHttpClent(){
		this.httpClient.getConnectionManager().shutdown();
	}
	
	private File getStoreFile(URL url){
		File file = AndroidHelper.getStoreDir(context);
		File storeDir = new File(file, "network");
		if(!storeDir.exists()){
			storeDir.mkdirs();
		}
		File target = new File(storeDir, FileHelper.getMD5HexString(url.toString()));
		return target;
	}
	
	private void printCookies(CookieStore cookieStore) {
		List<Cookie> cookies = cookieStore.getCookies();
		if (cookies.isEmpty()) {
			logger.debug("HttpRequest Cookies is empty.");
		} else {
			logger.debug("HttpRequest Cookies : ");
			for (int i = 0; i < cookies.size(); i++) {
				logger.debug("\t\t  " + (i + 1) + "、" + cookies.get(i).toString());
			}
			logger.debug("HttpRequest Cookies End. Count : " + cookies.size());
		}
	}
	
	private void printHeader(Header[] headers){
		int count = headers != null ?headers.length : 0;
		logger.debug("HttpRequest headers : ");
		for (int i = 0; i < count; i++) {
			logger.debug("\t\t  " + headers[i].getName() + " : " + headers[i].getValue());
		}
		logger.debug("HttpRequest headers End. Count : " + count);
	}
	
	/**
	 * <p>
	 * 创建一个ClientCookie.
	 * </p>
	 * 字符串的格式:
	 * <ol>
	 * <li>name=cookieName;value=cookieValue;path=/;domain=domain.com;expires=2012-12-20 12:12:00;secure;</li>
	 * <li>name=cookieName;value=cookieValue;path=/;domain=domain.com;expires=2012-12-20 12:12:00;secure=true;</li>
	 * </ol>
	 * <p>name,value必须</p>
	 * @param cookieStr Cookie字符串
	 * @return BasicClientCookie 
	 * @author zzljob@gmail.com 
	 */
	public static BasicClientCookie createClientCookie(String cookieStr) {
		if (TextUtils.isEmpty(cookieStr)) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		String[] cookieArr = TextUtils.split(cookieStr, ";");
		int length = cookieArr != null ? cookieArr.length : 0;
		for (int i = 0; i < length; i++) {
			String[] cookieVal = TextUtils.split(cookieArr[i], "=");
			if (cookieVal != null && cookieVal.length == 2) {
				map.put(cookieVal[0], cookieVal[1]);
			}
			if ("Secure".equalsIgnoreCase(cookieArr[i])) {
				map.put("secure", "true");
			}
		}
		if (!map.containsKey("name") && !map.containsKey("value")) {
			return null;
		}

		BasicClientCookie cookie = new BasicClientCookie(map.get("name"), map.get("value"));
		String temp = map.get("domain");
		if (!TextUtils.isEmpty(temp)) {
			cookie.setDomain(temp);
		}
		temp = map.get("path");
		if (!TextUtils.isEmpty(temp)) {
			cookie.setPath(temp);
		}
		temp = map.get("secure");
		if (!TextUtils.isEmpty(temp)) {
			cookie.setSecure(Boolean.parseBoolean(temp));
		}
		temp = map.get("expires");
		if (!TextUtils.isEmpty(temp)) {
			cookie.setExpiryDate(DateHelper.parse(temp));
		}
		return cookie;
	}
	
	/**
	 * Cookie格式化成字符串，便于存储
	 * @param cookie
	 * @return
	 */
	public static String formatCookie(Cookie cookie){
		String format = "name=%s;value=%s;path=%s;domain=%s;expires=%s;secure=%s;";
		String n = cookie.getName();
		String v = cookie.getValue();
		String p = cookie.getPath();
		String d = cookie.getDomain();
		String e = DateHelper.format(cookie.getExpiryDate());
		String s = Boolean.toString(cookie.isSecure());
		return String.format(format, n, v, p, d, e, s);
	}
	
	/**
	 * 根据相对URL获取完整的访问URL
	 * @param startUrl 相对的基准URL
	 * @param innerURL 相对URL
	 * @return 完整的访问URL
	 * @author zzljob@gmail.com
	 */
	public String getAbsoluteURL(String startUrl, String innerURL){
		String targetUrl = "";
		logger.debug("getAbsoluteURL innerURL : " + innerURL + " . startUrl : " + startUrl);
		if(TextUtils.indexOf(innerURL, "://") > -1){
			targetUrl = innerURL.replaceAll("#(.*)", "");
		}else{
			URL start;
			URL tempUrl;
			try {
				start = new URL(startUrl);
				tempUrl = new URL(start, innerURL);
				targetUrl = tempUrl.toString();
			} catch (MalformedURLException e) {
				targetUrl = "";
			}
		}
		logger.debug("getAbsoluteURL targetUrl : " + targetUrl);
		return targetUrl;
	}
	
}
