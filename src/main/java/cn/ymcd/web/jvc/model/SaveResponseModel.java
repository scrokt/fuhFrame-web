package cn.ymcd.web.jvc.model;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import cn.ymcd.web.filter.RequestHolder;

import com.google.gson.JsonObject;

public class SaveResponseModel {

    private String jvcpagename = "newpage";
    private String jvcoldpageName = "oldpage";
    private String cmd = "save";
    private String code = "100";
    private String returnvalue;
    private String message;
    private String returnview;

    /**
     * 返回returnvalue和message
     * @param returnvalue
     * @param message
     */
    public SaveResponseModel(String returnvalue,String message) {
        this.returnvalue = returnvalue;
        this.message = message;
        this.returnview = "@json";
    }
    
    /**
     * returnvalue默认为1
     * @param message
     */
    public SaveResponseModel(String message) {
        this("1",message);
    }
    
    /**
     * returnvalue默认为1,message默认为“操作成功”
     */
    public SaveResponseModel() {
       this("操作成功");
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jvcpagename",jvcpagename);
        jsonObject.addProperty("jvcoldpageName",jvcoldpageName);
        jsonObject.addProperty("cmd",cmd);
        jsonObject.addProperty("code",code);
        jsonObject.addProperty("returnview",returnview);
        jsonObject.addProperty("message",message);
        jsonObject.addProperty("returnvalue",returnvalue);
        HttpServletRequest request = RequestHolder.getRequest();
        if (request != null) {
            String layui = request.getParameter("layui");
            if (StringUtils.isNotEmpty(layui)) {
                return jsonObject.toString();
            }
        }
        return "var json_name=" + jsonObject.toString();
    }

    public String getJvcpagename() {
        return jvcpagename;
    }

    public void setJvcpagename(String jvcpagename) {
        this.jvcpagename = jvcpagename;
    }

    public String getJvcoldpageName() {
        return jvcoldpageName;
    }

    public void setJvcoldpageName(String jvcoldpageName) {
        this.jvcoldpageName = jvcoldpageName;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReturnvalue() {
        return returnvalue;
    }

    public void setReturnvalue(String returnvalue) {
        this.returnvalue = returnvalue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReturnview() {
        return returnview;
    }

    public void setReturnview(String returnview) {
        this.returnview = returnview;
    }

}
