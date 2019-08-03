package com.liuhu.socket.test;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

public class MyFactoryBean implements FactoryBean<Object> {
	 private static final Logger logger = LoggerFactory.getLogger(MyFactoryBean.class);   
	 private Object proxyObj;    
	@Override
	public Object getObject() throws Exception {
		 logger.debug("getObject......");
	        return proxyObj;
	}

	@Override
	public Class<?> getObjectType() {
		 return proxyObj == null ? Object.class : proxyObj.getClass();
	}
public static void main(String[] args) {
	Thread t = new Thread(new Runnable() {
		
		@Override
		public void run() {
			boolean flag = true;
			while(flag) {
				try {
					Thread.sleep(3000);
					System.out.println(new Date());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	});
	t.start();
}

private String inverseString(String in,String sep) {
	String [] array = in.split(sep);
	StringBuffer buf =new StringBuffer();
	for(int i = array.length-1;i>=0;i--) {
		buf.append(array[i]+sep);
	}
	return buf.toString();
}
}
