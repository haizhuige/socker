package com.liuhu.socket.test.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @Author liuhu-jk
 * @Date 2019/10/29 17:50
 * @Description
 **/
public class CglibProxy implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("执行方法前。。。。");
     //   method.invoke(o,objects);
        Object obj =  methodProxy.invokeSuper(o,objects);
        System.out.println("执行方法后。。。。");
        return null;
    }

    public static void main(String[] args) {
        HelloInterface  hello = new HelloImpl();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(hello.getClass());
        enhancer.setCallback(new CglibProxy());
        HelloInterface helloInterface = (HelloImpl) enhancer.create();
        helloInterface.helloWorld();
    }
}
