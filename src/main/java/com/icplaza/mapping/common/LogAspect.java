package com.icplaza.mapping.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日志切面类
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    // ..表示包及子包 该方法代表controller层的所有方法
    @Pointcut("execution(public * com..*.controller..*.*(..))")
    public void controllerMethod() {
    }

    @Before("controllerMethod()")
    public void LogRequestInfo(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        try {
            // 打印请求内容
            log.info("============================================= 请求内容 START =============================================");
            log.info("请求地址           ：{}", request.getRequestURL().toString());
            log.info("请求方式           ：{}", request.getMethod());
            log.info("请求类方法         ：{}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            log.info("请求IP地址         ：{}", request.getRemoteAddr());

            Object[] args = joinPoint.getArgs();
            List<Object> logArgs = Arrays.stream(args)
                    .filter(arg -> (!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse) && !(arg instanceof BindingResult)))
                    .collect(Collectors.toList());
            log.info("请求参数           ：{}", JSONObject.toJSONString(logArgs));
            log.info("============================================= 请求内容 END =============================================");
        } catch (Exception e) {
            log.error("请求参数解析异常：" + e.getMessage());
        }
    }

    @AfterReturning(returning = "result", pointcut = "controllerMethod()")
    public void logResultVOInfo(Object result) {
        log.info("============================================= 返回结果 START =============================================");
        log.info("Response内容：" + JSON.toJSONString(result));
        log.info("============================================= 返回结果 END =============================================");
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
            writer.flush();
        } catch (IOException e) {
            log.error("拦截器输出流异常" + e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
