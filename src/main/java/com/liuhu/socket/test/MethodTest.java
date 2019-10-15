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
    private Integer start=1;
    private Integer length=20;

    public Integer getPageNum() {
     /*   if (start==null||length==null){
            return 1;
        }*/
        return start/length + 1;
    }
    public Integer getStart() {
        return start;
    }

    public void setStart(final Integer start) {
        this.start = start;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(final Integer length) {
        this.length = length;
    }

    public static void main(String[] args) {
        MethodTest basePageQuery = null;
        System.out.println(basePageQuery.getPageNum());
    }

}
