package com.liuhu.socket.test.suanfa;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author liuhu-jk
 * @Date 2019/10/24 11:03
 * @Description
 **/
public class Player {
    public static int maxPlay(int mCar, List<Integer> list2) {//mcar 传入的补卡数量   list2 偷懒天数
        list2 = list2.stream().sorted().collect(Collectors.toList());
        int len = 120;
        List<List<Integer>> partList = new ArrayList<>();
        List<Integer> initList = new ArrayList<>();
        for (int i = 1; i <= len; i++) {
            for (int j = 0; j < list2.size(); j++) {
                if (i < list2.get(j)) {
                    initList.add(i);
                    break;
                }
                if (i == list2.get(j)) {
                    if (initList.size() > 0) {
                        partList.add(initList);
                        initList = new ArrayList<>();
                    }
                    initList.add(i);
                    partList.add(initList);
                    initList = new ArrayList<>();
                    break;
                }
            }
            if (i > list2.get(list2.size() - 1)) {
                initList.add(i);
            }
        }
        if (initList.size() > 0) {
            partList.add(initList);
        }
        int max =0;//最大连续天数
        for (int i = 0; i < partList.size(); i++) {
            int sum = 0;//连续天数
            int count = 0;//记录补卡的数量
            for (int j = i; j < partList.size(); j++) {
                if (count == mCar) {
                    if (partList.get(j).size() > 1) {
                        sum += partList.get(j).size();
                    }
                    break;
                }
                if (partList.get(j).size() == 1) {
                    sum = sum + partList.get(j).size();
                    count++;
                } else if (partList.get(j).size() > 1) {
                    sum = sum + partList.get(j).size();
                }

            }
            if (sum>max){
                max =sum;
            }
        }
        return max;
    }

    public static void main(String[] args) {
        List list2 = new ArrayList();
        list2.add(10);
        list2.add(30);
        list2.add(55);
        list2.add(56);
        list2.add(90);
        list2.add(99);
        list2.add(110);
        System.out.println(maxPlay(3, list2));
    }
}
