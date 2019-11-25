package com.liuhu.socket.test;

import com.alibaba.fastjson.JSONObject;
import com.liuhu.socket.domain.MarketInput2Domain;
import com.liuhu.socket.domain.MarketInputDomain;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @Author liuhu-jk
 * @Date 2019/9/11 15:01
 * @Description
 **/
@Slf4j
public class BeanCopyList<T, V> {

    V target;
    T origin;

    public void copyList(List<T> originList, List<V> targetList) {
        Assert.notNull(originList, "originList must not be null");
        Assert.notNull(targetList, "targetList must not be null");
        for (int i = 0; i < originList.size(); i++) {
            T obj = originList.get(i);
            V tar = target;
            BeanUtils.copyProperties(obj, tar);
            targetList.add(tar);
        }
    }
    public BeanCopyList(Class<T> tiz, Class<V> clz) {
        try {
            target = clz.newInstance();
            origin = tiz.newInstance();
        } catch (Exception e) {
           log.error("ERROR 泛型初始化失败");
        }
    }

    public static void main(String[] args) {

        List<MarketInputDomain> list =null;
        MarketInputDomain marketInputDomain = new MarketInputDomain();
        marketInputDomain.setShareName("001");
        marketInputDomain.setShareCode("002");
        list.add(marketInputDomain);
        List<MarketInput2Domain> copyList = new ArrayList<>();
        new BeanCopyList(MarketInputDomain.class, MarketInput2Domain.class).copyList(list, copyList);
        new HashSet<>().add("a");
        System.out.println(JSONObject.toJSON(copyList));
    }

}
