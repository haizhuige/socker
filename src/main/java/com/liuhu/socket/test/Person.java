package com.liuhu.socket.test;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @Author liuhu-jk
 * @Date 2019/10/16 16:53
 * @Description
 **/
@Setter
@Getter
public class Person {

    private String name;

    private String age;

    private String birthPlace;

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList("a","b","c","d"));
        Iterator<String> iter = list.iterator();
        while(iter.hasNext()){
            String s = iter.next();
            if(s.equals("a")){
                iter.remove();
            }
        }
        System.out.println(list);



    }

}
