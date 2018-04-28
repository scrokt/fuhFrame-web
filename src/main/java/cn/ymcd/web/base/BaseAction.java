package cn.ymcd.web.base;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

import cn.ymcd.ods.db.base.search.BaseSearchForm;
import cn.ymcd.ods.db.base.search.Page;
import cn.ymcd.ods.util.GsonUtil;
import cn.ymcd.ods.util.PojoUtils;
import cn.ymcd.web.filter.RequestHolder;
import cn.ymcd.web.jvc.model.SaveResponseModel;
import cn.ymcd.web.jvc.model.SubmitResponseModel;
import cn.ymcd.web.model.SimpleRetMsg;

import com.google.gson.JsonObject;

/**
 * 提供一些封装方法
 * 
 * @author fuh
 * @since
 */
public class BaseAction {

    protected final String ERROR_CODE = "-1";
    protected final String SUCCESS_CODE = "1";

    /**
     * 返回jvc Json数据，用于查询
     * 
     * @param result
     * @param searchForm
     * @return
     */
    protected <T> String returnList(List<T> result, BaseSearchForm searchForm) {
        return new SubmitResponseModel(result, searchForm).toString();
    }

    protected <T> String returnList(List<T> result, Page page) {
        return new SubmitResponseModel(result, page).toString();
    }

    /**
     * 返回jvc Json数据，用于表单提交
     * 
     * @param returnvalue
     * @param message
     * @return
     */
    protected String returnMsg(String returnvalue, String message) {
        return new SaveResponseModel(returnvalue, message).toString();
    }

    protected String returnMsg(String returnvalue, int message) {
        return new SaveResponseModel(returnvalue, message + "").toString();
    }

    /**
     * 返回jvc Json数据，用于表单提交
     * 
     * @param message
     * @return
     */
    protected String returnMsg(String message) {
        return new SaveResponseModel(message).toString();
    }
    
    protected String getPath() {
        HttpServletRequest request = RequestHolder.getRequest();
        return request.getContextPath();
    }

    protected ModelAndView getBaseMav(String path) {
        ModelAndView mav = new ModelAndView(path);
        HttpServletRequest request = RequestHolder.getRequest();
        if (request != null) {
            mav.addObject("path", request.getContextPath());
            String localPath = new StringBuilder().append(request.getScheme()).append("://")
                    .append(request.getServerName()).append(":").append(request.getServerPort())
                    .append(request.getContextPath()).append("/").toString();
            mav.addObject("localPath", localPath);
            String userId = PojoUtils.getCurrentUser();
            ;
            mav.addObject("currentUserId", userId);
            String websocketRootPath = request.getServerName() + ":" + request.getServerPort()
                    + request.getContextPath();
            mav.addObject("websocketRootPath", websocketRootPath);
        }
        return mav;
    }

    /**
     * 返回jvc Json数据，用于返回code,message
     * 
     * @param message
     * @return
     */
    protected String returnCodeMsg(String code, String message) {
        return returnCodeMsg(code, message, null);
    }

    protected String returnCodeMsg(String code, String message, Object obj) {
        SimpleRetMsg simpleRetMsg = new SimpleRetMsg(code, message, obj);
        return "var json_name=" + GsonUtil.toJson(simpleRetMsg);
    }

    /**
     * 纯json
     * 
     * @param code
     * @param message
     * @return
     * @author:fuh
     * @createTime:2017年12月29日 下午5:35:12
     */
    protected String returnMessage(String code, String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("code", code);
        jsonObject.addProperty("message", message);
        return jsonObject.toString();
    }

    /**
     * 纯json
     * 
     * @param obj
     * @return
     * @author:fuh
     * @createTime:2017年12月29日 下午5:35:44
     */
    protected String returnJson(Object obj) {
        return GsonUtil.toJson(obj);
    }

}
