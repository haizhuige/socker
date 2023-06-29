package com.liuhu.socket.common.config;

import com.alibaba.fastjson.JSON;


import com.liuhu.socket.common.ResponseResult;
import com.liuhu.socket.common.annotation.NotNull;
import com.liuhu.socket.common.annotation.Validator;
import com.liuhu.socket.enums.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author liuhu
 * @date 2019-07-15
 */
@Component
@Aspect
@Slf4j
public class HandlerAop {

    @Pointcut("@annotation(com.liuhu.socket.common.config.RequestHandler)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object handler(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequestHandler handler = AnnotationUtils.findAnnotation(method, RequestHandler.class);

        if (handler == null) {
            return joinPoint.proceed();
        }

        if (!checkParams(method, joinPoint.getArgs())) {
            return ResponseResult.failed(ResponseEnum.ILLEGAL_ARGUMENT.getValue());
        }

        String descriptor = handler.descriptor();
        log.info("REQ {} request: {}", descriptor, JSON.toJSONString(handleArgs(joinPoint)));
        try {
            Object result = joinPoint.proceed();
            log.info("RESP {} response: {}", descriptor, result);
            return result;
        } catch (Throwable throwable) {
            log.error("RESP {} error: {}", descriptor, throwable);
            return new ResponseResult<>(false).fail(handler.responseEnum());
        }
    }

    private Object[] handleArgs(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs()).filter(Objects::nonNull).filter(this::isExcludeType).toArray();
    }

    private boolean isExcludeType(Object arg) {
        return !(arg instanceof HttpServletResponse) &&
                !(arg instanceof HttpServletRequest) &&
                !(arg instanceof MultipartFile);
    }

    private boolean checkParams(Method method, Object[] args) throws Exception {
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            Object arg = args[i];

            if (ObjectUtils.isEmpty(arg)) {
                return false;
            }

            if (!isExcludeType(arg)) {
                continue;
            }

            Validator validator = AnnotationUtils.findAnnotation(type, Validator.class);
            if (validator == null) {
                continue;
            }

            List<Field> fields = new ArrayList<>();
            ReflectionUtils.doWithFields(type, fields::add);

            for (Field field : fields) {
                NotNull notNull = AnnotationUtils.findAnnotation(field, NotNull.class);
                if (notNull == null) {
                    continue;
                }
                ReflectionUtils.makeAccessible(field);
                if (ObjectUtils.isEmpty(field.get(arg))) {
                    return false;
                }
            }

        }
        return true;
    }

}
