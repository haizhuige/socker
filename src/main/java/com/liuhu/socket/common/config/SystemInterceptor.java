package com.liuhu.socket.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author liuhu
 * @date 2019-08-15
 */
@Slf4j
public class SystemInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<Long> time = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        time.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Long startTime = time.get();
        long endTime = System.currentTimeMillis();
        log.info("method : [{}] , request url : [{}] , 处理耗时 : {} ms", request.getMethod(), request.getRequestURL(), endTime - startTime);
        time.remove();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //当前JVM占用的内存总数(M)
        long total = (Runtime.getRuntime().totalMemory()) / (1024 * 1024);
        //JVM最大可用内存总数(M)
        long max = (Runtime.getRuntime().maxMemory()) / (1024 * 1024);
        //JVM空闲内存(M)
        long free = (Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        //可用内存内存(M)
        long mayuse = (max - total + free);
        //已经使用内存(M)
        long used = (total - free);
        log.info("JVM总内存(M) : {} , 可用内存内存(M) : {}, 已经使用内存(M) : {}", total, mayuse, used);
    }
}
