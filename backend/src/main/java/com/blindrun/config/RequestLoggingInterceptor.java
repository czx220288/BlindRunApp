
package com.blindrun.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 只记录SOS相关的请求
        if (uri.contains("/sos/")) {
            System.out.println("========================================");
            System.out.println("检测到SOS请求: " + method + " " + uri);
            System.out.println("时间: " + new java.util.Date());
            System.out.println("========================================");
        }
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String uri = request.getRequestURI();
        if (uri.contains("/sos/")) {
            System.out.println("SOS请求处理完成，响应码: " + response.getStatus());
        }
    }
}
