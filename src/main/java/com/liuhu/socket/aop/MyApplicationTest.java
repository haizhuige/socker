package com.liuhu.socket.aop;

import com.liuhu.socket.aop.aopConfig.MyConfigAop;
import com.liuhu.socket.aop.aspectMethod.Calculate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author liuhu-jk
 * @Date 2019/11/2 16:27
 * @Description
 **/
public class MyApplicationTest {

    public static void main(String[] args) {
        applicationAop();
    }

    private static void applicationAop() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(MyConfigAop.class);
        Calculate myConfigAop = (Calculate) applicationContext.getBean("getMyConfigAop");
        myConfigAop.multiply(3,5);
    }
}
