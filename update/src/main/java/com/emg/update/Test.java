package com.emg.update;

import java.io.File;
import java.util.List;

import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;

import com.emg.svn.conf.SvnConfig;
import com.emg.svn.factory.DemoSvn;
import com.emg.svn.inf.ISvn;

import net.sf.json.JSONObject;

public class Test {
	String account = "admin";
	String password = "admin";
	String path = "svn://127.0.0.1/autoUpgrade";
	String targetHead = "d:/测试";
	ISvn svn;
	public static void main() {
		Test test = new Test();
		test.getFile();
	}
	
	public void getFile() {
		String files = "{\"files\":[\"fileName\":\"d:\\test\\pom.xml\", \"updatetime\": \"20180418 16:03\", \"filesize\":\"102B\" ]}";
		JSONObject json = JSONObject.fromObject(files);	 
		System.out.println(json.toString());
	}
	
	/**
	 * 样例
	 * 
	 * @throws Exception
	 * @author liuniu
	 * @date 2016年8月12日
	 */

	private void testCore() throws Exception {
		SVNRepositoryFactoryImpl.setup();
		// 初始化实例
		DemoSvn ts = new DemoSvn(account, password, path);
		// 获得操作对象
		this.svn = ts.execute(SvnConfig.log);
		// 得到版本库信息
		SVNRepository repository = svn.createSVNRepository();
		// 得到基础操作对象
		SVNClientManager manager = svn.createSVNClientManager();
		
		// 关闭库容器
		svn.closeRepo();
		// SVNURL url1 = getRepositoryAccess().getTargetURL(target1);

	}

}
