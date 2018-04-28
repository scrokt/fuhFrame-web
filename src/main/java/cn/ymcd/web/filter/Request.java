package cn.ymcd.web.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import cn.ymcd.ods.util.Base64Util;
import cn.ymcd.ods.util.LogUtil;
import cn.ymcd.web.util.DesSecurity;
import cn.ymcd.web.util.Tea;
import cn.ymcd.web.util.ZipUtil;

public class Request implements HttpServletRequest {
    public static final int BAR2DS = 0;
    public static final int DS2DS = 1;
    public static final int HOTEL2DS = 2;

    private Map<String, List<String>> map = new java.util.HashMap<String, List<String>>();
    private String data = null;
    private HttpServletRequest request = null;
    private int type = 0;

    public Request(HttpServletRequest request, String desKey, String desIv) {
        this.request = request;
        String d = request.getParameter("d"); // des加密，用于网盾
        String d1 = request.getParameter("d1"); // 明文传输
        String d2 = request.getParameter("d2"); // Tea加密，用于非经营旁路
        String fd = request.getParameter("fd"); // Tea加密， 用于外部系统通信

        if (!StringUtils.isEmpty(d1)) {
            data = d1;
            type = DS2DS;
        } else if (!StringUtils.isEmpty(d2)) {
            data = Tea.decryptByTea(d2);
        } else if (!StringUtils.isEmpty(fd)) {
            data = Tea.decryptByTea1(fd);
        } else {
            data = unDes64(d, desKey, desIv);
        }

        if (data == null) {
            System.out.println("解密错误:" + d);
            return;
        }
        // 解析参数
        analysisParam();
    }

    public Request(HttpServletRequest request, String desKey, String desIv, boolean hasZip) {
        this.request = request;
        String d = request.getParameter("d"); // des加密，用于网盾
        String d1 = request.getParameter("d1"); // 明文传输
        String d2 = request.getParameter("d2"); // Tea加密，用于非经营旁路
        String fd = request.getParameter("fd"); // Tea加密， 用于外部系统通信

        if (!StringUtils.isEmpty(d1)) {
            data = d1;
            type = DS2DS;
        } else if (!StringUtils.isEmpty(d2)) {
            data = Tea.decryptByTea(d2);
        } else if (!StringUtils.isEmpty(fd)) {
            data = Tea.decryptByTea1(fd);
        } else {
            data = unDes64(d, desKey, desIv);
        }

        if (data == null) {
            System.out.println("解密错误:" + d);
            return;
        }
        if (hasZip && data.startsWith("zip=")) {
            try {
                data = data.substring(4);
                data = new String(ZipUtil.unZip3(Base64Util.decode(data)), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LogUtil.ERROR_LOG.error("request data解压异常");
            }
        }
        // 解析参数
        analysisParam();
    }

    public Request(String d, String desKey, String desIv) {
        data = unDes64(d, desKey, desIv);
        if (data == null) {
            System.out.println("解密错误:" + d);
            return;
        }
        // 解析参数
        analysisParam();
    }

    public boolean isMultipart() {
        return request instanceof MultipartHttpServletRequest;
    }

    public MultipartHttpServletRequest getMultipartReqeust() {
        return (MultipartHttpServletRequest)request;
    }

    public static String unDes64(String data, String key, String iv) {
        try {
            DesSecurity des = new DesSecurity(key, iv);
            byte[] b = des.decrypt64(data);
            if (b == null)
                return null;
            return new String(b, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Request(String strUrl) {
        data = new String(strUrl);
        // 解析参数
        analysisParam();
    }

    /**
     * 解析参数
     * 
     * 2014-6-6
     * 
     * @author yup
     */
    private void analysisParam() {
        String[] p = data.split("&");
        for (int i = 0; i < p.length; i++) {
            int pos = p[i].indexOf("=");
            if (pos == -1)
                continue;
            String name = p[i].substring(0, pos);
            String value = p[i].substring(pos + 1);
            List<String> list = map.get(name);
            if (list == null) {
                list = new ArrayList<String>();
                list.add(value);
                map.put(name, list);
            } else {
                list.add(value);
            }
        }
    }

    public String getData() {
        return data;
    }

    public String getParameter(String name) {
        List<String> list = map.get(name);
        if (list == null)
            return null;
        return list.get(0);
    }

    public String[] getParameterValues(String name) {
        List<String> list = map.get(name);
        if (list == null)
            return null;
        String[] values = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            values[i] = list.get(i);
        }
        return values;
    }

    public AsyncContext getAsyncContext() {
        return request.getAsyncContext();
    }

    public Object getAttribute(String arg0) {
        return request.getAttribute(arg0);
    }

    public Enumeration<String> getAttributeNames() {
        return request.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    public int getContentLength() {
        return request.getContentLength();
    }

    public String getContentType() {
        return request.getContentType();
    }

    public DispatcherType getDispatcherType() {
        return request.getDispatcherType();
    }

    public ServletInputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    public String getLocalAddr() {
        return request.getLocalAddr();
    }

    public String getLocalName() {
        return request.getLocalName();
    }

    public int getLocalPort() {

        return request.getLocalPort();
    }

    public Locale getLocale() {

        return request.getLocale();
    }

    public Enumeration<Locale> getLocales() {

        return request.getLocales();
    }

    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> m = new HashMap<String, String[]>();
        Set<String> keys = map.keySet();
        for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
            String key = iter.next();
            List<String> list = map.get(key);
            String[] vals = new String[list.size()];
            list.toArray(vals);
            m.put(key, vals);
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    public Enumeration<String> getParameterNames() {
        final Iterator<String> iterator = map.keySet().iterator();
        return new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public String nextElement() {
                return iterator.next();
            }
        };
    }

    public String getProtocol() {
        return request.getProtocol();
    }

    public BufferedReader getReader() throws IOException {
        return request.getReader();
    }

    public String getRealPath(String arg0) {
        return request.getRealPath(arg0);
    }

    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    public int getRemotePort() {
        return request.getRemotePort();
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
        return request.getRequestDispatcher(arg0);
    }

    public String getScheme() {
        return request.getScheme();
    }

    public String getServerName() {
        return request.getServerName();
    }

    public int getServerPort() {
        return request.getServerPort();
    }

    public ServletContext getServletContext() {
        return request.getServletContext();
    }

    public boolean isAsyncStarted() {
        return request.isAsyncStarted();
    }

    public boolean isAsyncSupported() {
        return request.isAsyncSupported();
    }

    public boolean isSecure() {
        return request.isSecure();
    }

    public void removeAttribute(String arg0) {
        request.removeAttribute(arg0);

    }

    public void setAttribute(String arg0, Object arg1) {
        request.setAttribute(arg0, arg1);

    }

    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
        request.setCharacterEncoding(arg0);

    }

    public AsyncContext startAsync() {

        return request.startAsync();
    }

    public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) {

        return request.startAsync(arg0, arg1);
    }

    public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {

        return request.authenticate(arg0);
    }

    public String getAuthType() {

        return request.getAuthType();
    }

    public String getContextPath() {

        return request.getContextPath();
    }

    public Cookie[] getCookies() {

        return request.getCookies();
    }

    public long getDateHeader(String arg0) {

        return request.getDateHeader(arg0);
    }

    public String getHeader(String arg0) {

        return request.getHeader(arg0);
    }

    public Enumeration<String> getHeaderNames() {

        return request.getHeaderNames();
    }

    public Enumeration<String> getHeaders(String arg0) {

        return request.getHeaders(arg0);
    }

    public int getIntHeader(String arg0) {

        return request.getIntHeader(arg0);
    }

    public String getMethod() {

        return request.getMethod();
    }

    public Part getPart(String arg0) throws IOException, IllegalStateException, ServletException {

        return request.getPart(arg0);
    }

    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {

        return request.getParts();
    }

    public String getPathInfo() {
        return request.getPathInfo();
    }

    public String getPathTranslated() {

        return request.getPathTranslated();
    }

    public String getQueryString() {

        return data;
    }

    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    public String getRequestURI() {

        return request.getRequestURI();
    }

    public StringBuffer getRequestURL() {

        return request.getRequestURL();
    }

    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    public String getServletPath() {
        return request.getServletPath();
    }

    public HttpSession getSession() {
        return request.getSession();
    }

    public HttpSession getSession(boolean arg0) {
        return request.getSession(arg0);
    }

    public Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return request.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return request.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromUrl() {

        return request.isRequestedSessionIdFromUrl();
    }

    public boolean isRequestedSessionIdValid() {

        return request.isRequestedSessionIdValid();
    }

    public boolean isUserInRole(String arg0) {

        return request.isUserInRole(arg0);
    }

    public void login(String arg0, String arg1) throws ServletException {
        request.login(arg0, arg1);

    }

    public void logout() throws ServletException {

        request.logout();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
