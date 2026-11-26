package org.spiderflow.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

/**
 * 请求对象包装类
 * @author Administrator
 *
 */
public class HttpRequest {
	
	private Connection connection = null;
	
	public static HttpRequest create(){
		return new HttpRequest();
	}
	
	public HttpRequest url(String url){
		this.connection = Jsoup.connect(url);
		this.connection.method(Method.GET);
		this.connection.timeout(60000);
		return this;
	}
	
	public HttpRequest headers(Map<String,String> headers){
		this.connection.headers(headers);
		return this;
	}
	
	public HttpRequest header(String key,String value){
		this.connection.header(key, value);
		return this;
	}
	
	public HttpRequest header(String key,Object value){
		if(value != null){
			this.connection.header(key,value.toString());
		}
		return this;
	}

	public HttpRequest cookies(Map<String,String> cookies){
		this.connection.cookies(cookies);
		return this;
	}

	public HttpRequest cookie(String name, String value) {
		if (value != null) {
			this.connection.cookie(name, value);
		}
		return this;
	}
	
	public HttpRequest contentType(String contentType){
		this.connection.header("Content-Type", contentType);
		return this;
	}
	
	public HttpRequest data(String key,String value){
		this.connection.data(key, value);
		return this;
	}
	
	public HttpRequest data(String key,Object value){
		if(value != null){
			this.connection.data(key, value.toString());
		}
		return this;
	}
	
	public HttpRequest data(String key,String filename,InputStream is){
		this.connection.data(key, filename, is);
		return this;
	}
	
	public HttpRequest data(Object body){
		if(body != null){
			this.connection.requestBody(body.toString());	
		}
		return this;
	}
	
	public HttpRequest data(Map<String,String> data){
		this.connection.data(data);
		return this;
	}
	
	public HttpRequest method(String method){
		this.connection.method(Method.valueOf(method));
		return this;
	}
	
	public HttpRequest followRedirect(boolean followRedirects){
		this.connection.followRedirects(followRedirects);
		return this;
	}
	
	public HttpRequest timeout(int timeout){
		this.connection.timeout(timeout);
		return this;
	}
	
	public HttpRequest proxy(String host,int port){
		this.connection.proxy(host, port);
		return this;
	}
	
	public HttpRequest validateTLSCertificates(boolean value){
		connection.sslContext(value ? null : UnverifiedTls.CONTEXT);
		return this;
	}

	private static class UnverifiedTls {
		private static final SSLContext CONTEXT = createContext();

		private static SSLContext createContext() {
			try {
				SSLContext context = SSLContext.getInstance("TLS");
				// 仅在流程显式关闭证书验证时使用当前请求的 SSLContext
				context.init(null, new TrustManager[] {new X509ExtendedTrustManager() {
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType) {
					}

					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType) {
					}

					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
					}

					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
					}

					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
					}

					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
					}
				}}, null);
				return context;
			} catch (GeneralSecurityException e) {
				throw new IllegalStateException("无法初始化请求的 TLS 配置", e);
			}
		}
	}
	
	public HttpResponse execute() throws IOException{
		this.connection.ignoreContentType(true);
		this.connection.ignoreHttpErrors(true);
		this.connection.maxBodySize(0);

		Response response = connection.execute();
		return new HttpResponse(response);
	}
}
