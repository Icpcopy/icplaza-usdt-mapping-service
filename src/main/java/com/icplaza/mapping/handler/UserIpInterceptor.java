package com.icplaza.mapping.handler;

import com.alibaba.fastjson.JSON;
import com.icplaza.mapping.common.Constant;
import com.icplaza.mapping.common.ResponseCommon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class UserIpInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String id = request.getHeader("project-id");
        if (id == null || !id.equals(Constant.PROJECT_ID)) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            response = attributes.getResponse();
            returnJson(response);
            return false;
        }
        return true;
    }


    private void returnJson(HttpServletResponse response) {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            writer = response.getWriter();
            ResponseCommon responseCommon = ResponseCommon.fail("请传递正确的project-id");
            String jsonStr = JSON.toJSON(responseCommon).toString();
            writer.print(jsonStr);
        } catch (IOException e) {
            log.error("拦截器输出流异常" + e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
