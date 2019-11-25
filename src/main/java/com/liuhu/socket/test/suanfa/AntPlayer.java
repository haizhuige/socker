package com.liuhu.socket.test.suanfa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author liuhu-jk
 * @Date 2019/10/24 15:31
 * @Description
 **/
public class AntPlayer {

    public static  long findAntCusterCount(int antCount,int chaValue){
       List<Node> nodeList = new ArrayList<>();
        if (antCount<=0&&antCount>1000){
            return 0;
        }
        nodeList.add(new Node(antCount));
        nodeList=fenLie( new CopyOnWriteArrayList<>(nodeList),chaValue);
      return  nodeList.stream().filter(node1 -> node1.getFlag().equals("1")).count();
    }

    public static List<Node> fenLie(List<Node> list,int chaValue){
         int initCount = list.size();
         for (Node node:list){
             if (node.getFlag().equals("1")){
                 if (node.getCenter()-chaValue>0&&(node.getCenter()-chaValue)%2==0){
                       int minValue = (node.getCenter()-chaValue)/2;
                       node.setFlag("2");
                       list.add(new Node(minValue));
                       list.add(new Node(minValue+chaValue));
                 }
             }
         }
         int lastCount = list.size();
         if (initCount == lastCount){
             return list;
         }else{
             return fenLie(list,chaValue);
         }
    }
    public static void main(String[] args){
     //   System.out.println("方法1-------------开始时间"+new Date().getTime());
     //   System.out.println(findAntCusterCount(166,2));
     //   System.out.println("方法1-------------结束时间"+new Date().getTime());
        long start = System.currentTimeMillis();
          System.out.println(test(166,2));
      //  System.out.println(findAntCusterCount(166,2));
        long end =System.currentTimeMillis();
        System.out.println(end-start);

    }
    public static int test1(int n){
        return 1;
    }
    public static int test(int n,int k){
       int count =1;
       if ((k+n)%2==0){
           int x= (k+n)/2;
           int y = n-x;
           if (x>0&&y>0){
               count=0;
               count = count+test(x,k);
               count = count+test(y,k);
           }
       }
       return count;
    }

}
 class Node{


    private int center;

    private String flag ="1";//1;叶子节点  2：非叶子节点


     public void setCenter(int center) {
         this.center = center;
     }

     public int getCenter() {
         return center;
     }

     public void setFlag(String flag) {
         this.flag = flag;
     }

     public String getFlag() {
         return flag;
     }
     public Node(int center){
         this.center =center;
     }
     public Node(){

     }
 }
