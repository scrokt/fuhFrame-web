package cn.ymcd.web.auth.model;

/**
 * 模块资源model
 * @author fuh
 * @since
 */
public class ModuleResourceModel {
    private Integer resourceId;
    private String resourceType;
    private String moduleId;
    private String resourceSet;
    private String resourceName;
    private String resourceAttr;
    private Integer parentResourceId;

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getResourceSet() {
        return resourceSet;
    }

    public void setResourceSet(String resourceSet) {
        this.resourceSet = resourceSet;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceAttr() {
        return resourceAttr;
    }

    public void setResourceAttr(String resourceAttr) {
        this.resourceAttr = resourceAttr;
    }

    public Integer getParentResourceId() {
        return parentResourceId;
    }

    public void setParentResourceId(Integer parentResourceId) {
        this.parentResourceId = parentResourceId;
    }

    @Override
    public String toString() {
        return "ModuleResourceModel [resourceId=" + resourceId + ", resourceType=" + resourceType + ", moduleId=" + moduleId + ", resourceSet="
                + resourceSet + ", resourceName=" + resourceName + ", resourceAttr=" + resourceAttr + ", parentResourceId=" + parentResourceId + "]";
    }

}
