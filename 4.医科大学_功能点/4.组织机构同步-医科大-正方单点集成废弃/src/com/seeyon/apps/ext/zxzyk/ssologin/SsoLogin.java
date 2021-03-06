package com.seeyon.apps.ext.zxzyk.ssologin;

import com.neusoft.education.tp.sso.client.filter.CASFilterRequestWrapper;
import com.seeyon.apps.ext.zxzyk.util.StringHandle;
import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager;
import com.seeyon.ctp.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

public class SsoLogin {

    private static Logger log = LoggerFactory.getLogger(SsoLogin.class);

    public static void login(HttpServletRequest request, HttpServletResponse response) {
        CASFilterRequestWrapper reqWrapper = new CASFilterRequestWrapper(request);
        String loginName = reqWrapper.getRemoteUser();
        String encodeloginName = StringHandle.encode(loginName);
        if (null != loginName) {
            try {
                response.sendRedirect("login/sso?from=xzykSso&ticket=" + encodeloginName);
            } catch (IOException e) {
                log.error("单点登录OA系统出错了，错误信息：" + e.getMessage());
            }
        }
    }

    public static void oaBill(HttpServletRequest request, HttpServletResponse response) {
        try {
            String ticket = request.getParameter("ticket");
            String url = request.getParameter("redirectUrl");
//            String url = "/seeyon/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId=7575228852124339003";
            SSOTicketManager.getInstance().newTicketInfo(ticket, ticket, "xzykSso");
            String urlt = "/seeyon/main.do?method=login&ticket=" + ticket + "&login.destination=" + URLEncoder.encode(url.substring(url.indexOf("seeyon")-1));
            response.sendRedirect(urlt);
        } catch (IOException e) {
            log.error("医科系统打开Oa代办事项出错了，错误信息：" + e.getMessage());
        }
    }

    public static void oaBill2(HttpServletRequest request, HttpServletResponse response){
        try {
            String ticket = request.getParameter("ticket");
            String url = request.getParameter("redirectUrl");
//            String url = "222.193.95.137/seeyon/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId=7575228852124339003";

            //单点登录
            String encodeloginName = StringHandle.encode(ticket);
            String loginUrl="222.193.95.137:80/seeyon/login/sso?from=xzykSso&ticket=" + encodeloginName;
            HttpClientUtil u = new HttpClientUtil();
            u.open(loginUrl, "post");
            u.send();
            u.close();

//            String encodeloginName = StringHandle.encode(ticket);
//            if (null != ticket) {
//                try {
//                    response.sendRedirect("login/sso?from=xzykSso&ticket=" + encodeloginName);
//                } catch (IOException e) {
//                    log.error("单点登录OA系统出错了，错误信息：" + e.getMessage());
//                }
//            }


            SSOTicketManager.getInstance().newTicketInfo(ticket, ticket, "xzykSso");
            String urlt = "/seeyon/main.do?method=login&ticket=" + ticket + "&login.destination=" + URLEncoder.encode(url);
            response.sendRedirect(urlt);
        } catch (IOException e) {
            log.error("医科系统打开Oa代办事项出错了，错误信息：" + e.getMessage());
        }
    }


    public static void test(HttpServletRequest request, HttpServletResponse response) {
        String ticket = request.getParameter("ticket");
        String loginname="999992016999";
        String memberId="";
        System.out.println("获取ticket:" + ticket);

    }
}
