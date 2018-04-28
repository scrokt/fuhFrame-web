package cn.ymcd.web.auth.model;

import java.util.List;

public class UserModuleModel {
    private Integer moduleId;
    private String moduleName;
    private Integer moduleOrder;
    private Integer parentModuleId;
    private List<UserModuleModel> childs;
    private List<ModuleResourceModel> resources;

    public Integer getParentModuleId() {
        return parentModuleId;
    }

    public void setParentModuleId(Integer parentModuleId) {
        this.parentModuleId = parentModuleId;
    }

    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Integer getModuleOrder() {
        return moduleOrder;
    }

    public void setModuleOrder(Integer moduleOrder) {
        this.moduleOrder = moduleOrder;
    }

    public List<UserModuleModel> getChilds() {
        return childs;
    }

    public void setChilds(List<UserModuleModel> childs) {
        this.childs = childs;
    }

    public List<ModuleResourceModel> getResources() {
        return resources;
    }

    public void setResources(List<ModuleResourceModel> resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return "UserModuleModel [moduleId=" + moduleId + ", moduleName=" + moduleName + ", moduleOrder=" + moduleOrder + ", parentModuleId="
                + parentModuleId + ", childs=" + childs + ", resources=" + resources + "]";
    }

}
