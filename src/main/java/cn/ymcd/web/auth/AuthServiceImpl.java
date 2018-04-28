package cn.ymcd.web.auth;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.ymcd.web.auth.model.ModuleResourceModel;
import cn.ymcd.web.auth.model.UserModuleModel;
import cn.ymcd.web.filter.Request;

/**
 * 抽象权限基类；
 * 
 * @projectName:pine-web
 * @author:fuh
 * @date:2017年8月9日 下午4:31:01
 * @version 1.0
 */
public abstract class AuthServiceImpl implements AuthService {

    private static Logger LOG = Logger.getLogger(AuthServiceImpl.class);

    @Override
    public boolean hasAuth(HttpServletRequest request) {
        if (request instanceof Request) {
            boolean res = checkDesRequest((Request)request);
            if (res) {
                return res;
            }
        }
        // 验证权限
        String method = request.getMethod();
        String url = request.getRequestURL().toString();
        // 不需要登陆
        if (isUrlSkipAbleBeforeLogin(url)) {
            return true;
        }
        // 如果没有登陆，拒绝
        if (!hasLogin(request)) {
            return false;
        }
        if (isUrlSkipAbleAfterLogin(url)) {
            return true;
        }
        // 判断权限
        boolean auth = false;
        if (!isNeedCheck(request)) {
            return true;
        }
        List<UserModuleModel> userModules = findUserModulesByRequest(request);
        if (userModules != null) {
            for (UserModuleModel userModule : userModules) {
                Integer moduleId = userModule.getModuleId();
                String murls = getModuleUrls(moduleId);
                auth = checkUrls(method, url, murls);
                if (auth) {// 有权限，退出判断
                    break;
                }
                List<ModuleResourceModel> resources = userModule.getResources();
                if (resources != null) {
                    for (ModuleResourceModel resource : resources) {
                        Integer resourceId = resource.getResourceId();
                        String rurls = getResourceUrls(resourceId);
                        auth = checkUrls(method, url, rurls);
                        if (auth) {// 有权限，退出判断
                            break;
                        }
                    }
                }
                if (auth) {
                    break;
                }
            }
        }
        return auth;
    }

    /**
     * 检查加密的请求权限；
     * 
     * @param request
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:19:42
     */
    protected boolean checkDesRequest(Request request) {
        return true;
    }

    /**
     * 是否需要检查权限；子类可以通过覆写该方法来自主判断；该检查登录后有效；
     * 
     * @param request
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:04:18
     */
    protected boolean isNeedCheck(HttpServletRequest request) {
        return true;
    }

    /**
     * 子类需要覆写该方法，判断请求是否登录了；
     * 
     * @param request
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:13:15
     */
    protected abstract boolean hasLogin(HttpServletRequest request);

    /**
     * 子类需覆写该方法，判断该url是否跳过检查；登录前；
     * 
     * @param url
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:15:03
     */
    protected abstract boolean isUrlSkipAbleBeforeLogin(String url);

    /**
     * 子类需覆写该方法，判断该url是否跳过检查；登录后；
     * 
     * @param url
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:15:56
     */
    protected abstract boolean isUrlSkipAbleAfterLogin(String url);

    /**
     * 获取这个菜单id能访问的url集合；使用长字符串，url之间用,分隔
     * 
     * @param moduleId
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:16:33
     */
    protected abstract String getModuleUrls(Integer moduleId);

    /**
     * 获取这个资源id能访问的url集合；使用长字符串，url之间用,分隔
     * 
     * @param resourceId
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:16:33
     */
    protected abstract String getResourceUrls(Integer resourceId);

    /**
     * 根据请求获取用户具有的菜单权限；其实是根据session中的用户获取；
     * 
     * @param request
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:16:47
     */
    protected abstract List<UserModuleModel> findUserModulesByRequest(HttpServletRequest request);

    /**
     * 需要提供给外部访问的接口需要覆写这两个方法；提供des揭秘的iv和key；
     * 
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:26:20
     */
    @Override
    public String getDesIv() {
        return null;
    }

    /**
     * 需要提供给外部访问的接口需要覆写这两个方法；提供des揭秘的iv和key；
     * 
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:27:35
     */
    @Override
    public String getDesKey() {
        return null;
    }

    /**
     * 检查url的权限；url为请求的url;murls为特定格式的允许访问的url列表；子类可覆写此方法来定义自己的逻辑；
     * 
     * @param method
     * @param url
     * @param murls
     * @return
     * @author:fuh
     * @createTime:2017年8月9日 下午4:20:37
     */
    protected boolean checkUrls(String method, String url, String murls) {
        if (murls == null) {
            return false;
        }
        String[] split = murls.split(",");
        for (String murl : split) {
            String[] split2 = murl.split("\\|");
            if (split2.length == 2) {
                String allowMethod = split2[0].toUpperCase();
                murl = "^.*" + split2[1] + "/.*";
                LOG.info("url:" + url + ",moduleUrl:" + murl + ",method:" + method + ",allowMethod:" + allowMethod);
                // url匹配上且method匹配
                if (method.equals(allowMethod) && url.matches(murl)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 空实现，子类可覆写处理异常；
     * 
     * @param e
     * @param request
     * @param response
     * @author:fuh
     * @createTime:2017年8月9日 下午4:22:09
     */
    @Override
    public void dealWithException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        e.printStackTrace();
    }

    /**
     * 空实现，子类可覆写处理请求结束后其它操作；
     * 
     * @param request
     * @param response
     * @author:fuh
     * @createTime:2017年8月9日 下午4:22:42
     */
    @Override
    public void dealWithRequestComplete(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public boolean parseMultipart() {
        return false;
    }

    /**
     * 是否压缩
     * @return
     * @author:fuh
     * @createTime:2018年4月24日 上午11:41:47
     */
    @Override
    public boolean hasZip() {
        return false;
    }

}
