package cn.ymcd.web.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class RequestHolder {

	
	private static ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<HttpServletRequest>();
	private static ThreadLocal<HttpServletResponse> responseHolder=new ThreadLocal<HttpServletResponse>();
	
	public static void set(HttpServletRequest request,HttpServletResponse response){
		requestHolder.set(request);
		responseHolder.set(response);
	}
	
	public static HttpServletRequest getRequest(){
		HttpServletRequest httpServletRequest = requestHolder.get();
		return httpServletRequest;
	}
	
	public static HttpSession getSession(){
		return getRequest().getSession();
	}
	
	public static HttpServletResponse getResponse(){
		return responseHolder.get();
	}
	
	public static void remove(){
		requestHolder.remove();
		responseHolder.remove();
	}
}
