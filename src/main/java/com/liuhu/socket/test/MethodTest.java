package com.liuhu.socket.test;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author liuhu-jk
 * @Date 2019/9/8 16:13
 * @Description
 **/
public class MethodTest {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("2");
        init(list);
        System.out.println(JSONObject.toJSON(list));
    }

    private static void init(List<String> list){
        list.add("3");
    }


}
