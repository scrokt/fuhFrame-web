package cn.ymcd.web.jvc.model;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import cn.ymcd.ods.db.base.search.BaseSearchForm;
import cn.ymcd.ods.db.base.search.Page;
import cn.ymcd.ods.gson.WebGsonAdapterFactory;
import cn.ymcd.ods.util.DateUtil;
import cn.ymcd.web.filter.RequestHolder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SubmitResponseModel {

    private String jvcpagename;
    private String jvcoldpageName;
    private String cmd;
    private String code;
    private String returnval;
    // res.recordsperpage
    private String recordsperpage;
    // RecordsPerPage
    private String recordsPerPage;
    // res.size
    private String size;
    private long count;

    private Object[] res;
    private JsonArray respage;
    // returnview
    private String returnview;

    public <T> SubmitResponseModel(List<T> list, BaseSearchForm searchForm) {
        init(list, searchForm.getPage());
    }

    public <T> SubmitResponseModel(List<T> list, Page page) {
        init(list, page);
    }

    private <T> void init(List<T> list, Page page) {
        if (list == null) {
            list = new ArrayList<T>();
        }
        if (page == null) {
            page = new Page();
        }
        jvcoldpageName = "oldpage";
        jvcpagename = "newpage";
        cmd = "query";
        code = "100";
        res = list.toArray();
        size = list.size() + "";
        recordsperpage = page.getPageSize() + "";
        recordsPerPage = page.getPageSize() + "";
        count = page.getRowTotal();
        buildPage(list, page);
        returnview = "@json";
    }

    private <T> void buildPage(List<T> list, Page page) {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("RecordsCount", page.getRowTotal());
        boolean hasNextPage = page.getPageTotal() != 0 && page.getPageIndex() != page.getPageTotal();
        jsonObject.addProperty("hasNextPage", hasNextPage);
        boolean hasPreviousPage = page.getPageIndex() != 1;
        jsonObject.addProperty("hasPreviousPage", hasPreviousPage);
        jsonObject.addProperty("CurPage", page.getPageIndex());
        jsonObject.addProperty("RecordsPerPage", page.getPageSize());
        jsonObject.addProperty("TotalPageCount", page.getPageTotal());
        jsonArray.add(jsonObject);
        respage = jsonArray;
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jvcpagename", jvcpagename);
        jsonObject.addProperty("jvcoldpageName", jvcoldpageName);
        jsonObject.addProperty("cmd", cmd);
        jsonObject.addProperty("code", code);
        jsonObject.addProperty("res.recordsperpage", recordsperpage);
        jsonObject.addProperty("res.size", size);
        jsonObject.addProperty("RecordsPerPage", recordsPerPage);
        jsonObject.addProperty("returnview", returnview);
        jsonObject.addProperty("count", count);
        jsonObject.add("res.page", respage);
        //
        JsonArray jsonArray = new JsonArray();
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new WebGsonAdapterFactory())
                .setDateFormat(DateUtil.DATE_TIME_PATTERN).create();
        int row = 1;
        for (Object r : res) {
            String json = gson.toJson(r);
            JsonElement parse = new JsonParser().parse(json);
            parse.getAsJsonObject().addProperty("row", row++);
            jsonArray.add(parse);
        }
        jsonObject.add("res", jsonArray);
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

    public Object[] getRes() {
        return res;
    }

    public void setRes(Object[] res) {
        this.res = res;
    }

    public String getRecordsperpage() {
        return recordsperpage;
    }

    public void setRecordsperpage(String recordsperpage) {
        this.recordsperpage = recordsperpage;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(String recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public String getReturnview() {
        return returnview;
    }

    public void setReturnview(String returnview) {
        this.returnview = returnview;
    }

    public String getReturnval() {
        return returnval;
    }

    public void setReturnval(String returnval) {
        this.returnval = returnval;
    }

}
