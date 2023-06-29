package com.liuhu.socket.test;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @Author liuhu-jk
 * @Date 2019/9/8 16:13
 * @Description
 **/
@Setter
@Getter
public class MethodTest {
    private Integer start=1;
    private Integer length=20;

    public Integer getPageNum() {
     /*   if (start==null||length==null){
            return 1;
        }*/
        return start/length + 1;
    }
    public static void main(String[] args) {

    }

}
