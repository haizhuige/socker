package com.liuhu.socket.test.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author liuhu-jk
 * @Date 2019/10/29 17:34
 * @Description
 **/
public class JavaProxy implements InvocationHandler {
    private Object object;
    public JavaProxy(Object object){
        this.object =object;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("执行方法前。。。。");
        method.invoke(object,args);
        System.out.println("执行方法后。。。。");
        return null;
    }

    public static void main(String[] args) throws  Exception {
        HelloInterface hello = new HelloImpl();
        JavaProxy proxy = new JavaProxy(hello);
        HelloInterface helloInterface = (HelloInterface) Proxy.newProxyInstance(hello.getClass().getClassLoader(),hello.getClass().getInterfaces(),proxy);
       // helloInterface.helloWorld();
    }
}
