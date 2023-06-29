package com.liuhu.socket.lifeCycle;

/**
 * @Author liuhu-jk
 * @Date 2019/11/8 16:28
 * @Description
 **/
public class Person {

    private School school;
    public Person() {
        System.out.println("Person构造函数执行");
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public School getSchool() {
        return school;
    }
}
