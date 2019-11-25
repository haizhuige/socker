package com.liuhu.socket.lifeCycle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author liuhu-jk
 * @Date 2019/11/3 10:32
 * @Description
 **/
public class MyconfigTest {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext  configApplicationContext = new AnnotationConfigApplicationContext(MyConfigByLifeCycle.class);
        configApplicationContext.setAllowCircularReferences(true);
      //  School school = configApplicationContext.getBean(School.class);
       // School school1 = configApplicationContext.getBean(School.class);
        getBeanName(configApplicationContext);
        configApplicationContext.close();

    }

    public static void getBeanName(AnnotationConfigApplicationContext  configApplicationContext){

        String []beanNames = configApplicationContext.getBeanDefinitionNames();

        for (int i=0;i<beanNames.length;i++){
            System.out.println(beanNames[i]);
        }

    }
}
