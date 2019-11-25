package com.liuhu.socket.aop.logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

import java.util.Arrays;

/**
 * @Author liuhu-jk
 * @Date 2019/11/2 16:16
 * @Description
 **/
@Aspect
public class AopLogger {
    @Pointcut("@annotation(com.liuhu.socket.aop.aopConfig.DefAutowired)")
    public void pointCut(){
    }
    @Before("pointCut()")
    public void preMeThod(JoinPoint joinPoint){
        System.out.println("方法执行前传入的参数为"+ Arrays.asList(joinPoint.getArgs()));
    }
    @After("pointCut()")
    public void afterMethod(){
        System.out.println("方法执行后");
    }

    @AfterReturning(value = "pointCut()",returning = "result")
    public void afterReturnning(JoinPoint joinPoint,Object result){
        System.out.println("方法名为"+joinPoint.getSignature().getName()+"获取的结果为"+result);
    }

}
