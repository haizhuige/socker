package com.liuhu.socket.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author liuhu-jk
 * @Date 2019/10/14 16:13
 * @Description
 **/
public class Fanxing {

    public static void main(String[] args) {
        List<String> stringArrayList = new ArrayList<String>();
        List<Integer> integerArrayList = new ArrayList<Integer>();

        Class classStringArrayList = stringArrayList.getClass();
        Class classIntegerArrayList = integerArrayList.getClass();

        if(classStringArrayList.equals(classIntegerArrayList)){
            System.out.println("类型相同");
        }
    }
}
