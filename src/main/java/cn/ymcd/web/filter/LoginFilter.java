package cn.ymcd.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import cn.ymcd.ods.util.GsonUtil;
import cn.ymcd.ods.util.PojoUtils;
import cn.ymcd.web.auth.AuthService;

import com.google.gson.JsonObject;

@Component("login")
public class LoginFilter extends GenericFilterBean {

    private static final Logger LOG = Logger.getLogger(LoginFilter.class);
    @Autowired(required = false)
    @Qualifier("authService")
    private AuthService authService;

    /**
     * 所有http请求经过这个过滤器
     * 
     * @param req
     * @param res
     * @param chain
     * @throws IOException
     * @throws ServletException
     * @author:lianss
     * @createTime:2017年8月9日 上午11:09:46
     */
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        if (authService.parseMultipart()) {
            CommonsMultipartResolver cmr = new CommonsMultipartResolver(request.getServletContext());
            boolean multipart = cmr.isMultipart(request);
            if (multipart) {
                MultipartHttpServletRequest resolveMultipart = cmr.resolveMultipart(request);
                request = resolveMultipart;
            }
        }
        // 解密
        if (request.getParameter("d") != null || request.getParameter("d1") != null
                || request.getParameter("d2") != null || request.getParameter("fd") != null) {
            request = new Request(request, authService.getDesKey(), authService.getDesIv(), authService.hasZip());
        }
        if (authService == null) {
            throw new IllegalArgumentException("authService未指定");
        }

        boolean hasAuth = authService.hasAuth(request);
        if (hasAuth) {
            RequestHolder.set(request, response);
            if (PojoUtils.getCurrentUser() == null) {
                setCurrentUser(request);
            }
            try {
                chain.doFilter(request, response);
                authService.dealWithRequestComplete(request, response);
            } catch (Exception e) {
                authService.dealWithException(e, request, response);
            }
            PojoUtils.removeCurrentUser();
            RequestHolder.remove();
        } else {
            LOG.warn(request.getMethod() + " " + request.getRequestURL() + " 没有权限");
            authService.dealWithNoAuth(request, response);
        }

    }

    private void setCurrentUser(HttpServletRequest request) {
        if (request instanceof Request) {
            PojoUtils.setCurrentUser(request.getRemoteAddr());// 远程调用，用户设置为IP
        } else {
            String currentUserJson = (String)request.getSession().getAttribute("_userJson");
            if (StringUtils.isEmpty(currentUserJson)) {// 没有用户
                PojoUtils.setCurrentUser(request.getRemoteAddr());

            } else {
                JsonObject userJsonObject = GsonUtil.parseObjectFrom(currentUserJson);
                if (userJsonObject == null) {// 解析失败
                    PojoUtils.setCurrentUser(request.getRemoteAddr());
                } else {
                    String userId = null;
                    userId = GsonUtil.getString(userJsonObject.get("userId"));
                    if (userId == null) {// 尝试小写取值
                        userId = GsonUtil.getString(userJsonObject.get("userid"));
                        if (userId == null) {
                            userId = GsonUtil.getString(userJsonObject.get("userName"));
                            if (userId == null) {
                                userId = GsonUtil.getString(userJsonObject.get("username"));
                            }
                        }
                    }
                    PojoUtils.setCurrentUser(userId == null ? request.getRemoteAddr() : userId);
                }
            }
        }
    }
}
