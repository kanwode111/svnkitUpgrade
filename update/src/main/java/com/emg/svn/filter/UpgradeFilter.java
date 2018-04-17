package com.emg.svn.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

/**
 * 接口拦截类
 * @author Administrator
 *
 */
public class UpgradeFilter implements ContainerRequestFilter{

	@Override
	public void filter(ContainerRequestContext context) throws IOException {
		//打印出所有请求头  
	    MultivaluedMap<String, String> map =  context.getHeaders();  
	    for(Entry<String, List<String>> entry : map.entrySet()){  
	        String key = entry.getKey();  
	        List<String> valueList = entry.getValue();  
	        String values = valueList.toString();  
	        System.out.println(key + ":"+values.substring(1,values.length()-1));  
	    }  
	}

}
