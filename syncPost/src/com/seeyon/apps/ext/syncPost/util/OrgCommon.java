package com.seeyon.apps.ext.syncPost.util;

/**
 * Created by Administrator on 2019-7-29.
 */
public class OrgCommon {

    private ReadConfigTools configTools = null;

    private String orgAccountId = "";
    private String orgPostId = "";

    private String durl = "";
    private String dusername = "";
    private String dpassword = "";
    private String ddriver = "";


    private String restUrl = "";
    private String restUsername = "";
    private String restPwd = "";

    public OrgCommon() {
        configTools = new ReadConfigTools();
        orgAccountId = configTools.getString("gcxy.orgAccountId");
//
        durl = configTools.getString("gcxy.midDataLink.url");
        dusername = configTools.getString("gcxy.midDataLink.username");
        dpassword = configTools.getString("gcxy.midDataLink.password");
        ddriver = configTools.getString("gcxy.midDataLink.driver");
//
        restUrl = configTools.getString("gcxy.restInfo.url");
        restUsername = configTools.getString("gcxy.restInfo.username");
        restPwd = configTools.getString("gcxy.restInfo.password");

    }

    public String getRestUrl() {
        return restUrl;
    }

    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }

    public String getRestUsername() {
        return restUsername;
    }

    public void setRestUsername(String restUsername) {
        this.restUsername = restUsername;
    }

    public String getRestPwd() {
        return restPwd;
    }

    public void setRestPwd(String restPwd) {
        this.restPwd = restPwd;
    }

    public ReadConfigTools getConfigTools() {
        return configTools;
    }

    public void setConfigTools(ReadConfigTools configTools) {
        this.configTools = configTools;
    }

    public String getOrgAccountId() {
        return orgAccountId;
    }

    public void setOrgAccountId(String orgAccountId) {
        this.orgAccountId = orgAccountId;
    }

    public String getOrgPostId() {
        return orgPostId;
    }

    public void setOrgPostId(String orgPostId) {
        this.orgPostId = orgPostId;
    }

    public String getDurl() {
        return durl;
    }

    public void setDurl(String durl) {
        this.durl = durl;
    }

    public String getDusername() {
        return dusername;
    }

    public void setDusername(String dusername) {
        this.dusername = dusername;
    }

    public String getDpassword() {
        return dpassword;
    }

    public void setDpassword(String dpassword) {
        this.dpassword = dpassword;
    }

    public String getDdriver() {
        return ddriver;
    }

    public void setDdriver(String ddriver) {
        this.ddriver = ddriver;
    }
}
