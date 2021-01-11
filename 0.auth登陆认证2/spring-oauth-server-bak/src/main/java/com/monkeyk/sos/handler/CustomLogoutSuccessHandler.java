package com.monkeyk.sos.handler;

import com.monkeyk.sos.domain.shared.security.SOSUserDetails;
import com.monkeyk.sos.service.CheckUserStatusServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {

    @Autowired
    private CheckUserStatusServiceImpl service;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SOSUserDetails map = (SOSUserDetails) authentication.getPrincipal();
        String username = map.user().username();
        service.delete(username);
        // 将子系统的cookie删掉
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        System.out.println("退出应用的session的id:" + sessionId);
        String returnUrl = request.getParameter("returnUrl");
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        if (null != returnUrl) {
            response.sendRedirect(returnUrl);
        }
        super.handle(request, response, authentication);
    }
}
