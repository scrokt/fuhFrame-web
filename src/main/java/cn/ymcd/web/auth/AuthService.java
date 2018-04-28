package cn.ymcd.web.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthService {

    boolean hasAuth(HttpServletRequest request);

    boolean parseMultipart();

    void dealWithNoAuth(HttpServletRequest request, HttpServletResponse response);

    String getDesKey();

    String getDesIv();

    void dealWithException(Exception e, HttpServletRequest request, HttpServletResponse response);

    void dealWithRequestComplete(HttpServletRequest request, HttpServletResponse response);
    
    boolean hasZip();
}
