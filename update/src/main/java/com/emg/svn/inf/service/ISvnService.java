package com.emg.svn.inf.service;

import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;

/**
 * svn主服务创建
 * 
 * @author Allen
 * @date 2016年8月8日
 */
public interface ISvnService {

	/**
	 * 创建SNV版本库服务
	 * 
	 * @author Allen
	 * @date 2016年8月11日
	 */
	public SVNRepository createSVNRepository();

	/**
	 * 关闭版本库容器,便于刷新重连等
	 * 
	 * @author Allen
	 * @date 2016年8月11日
	 */
	public void closeRepo();

	/**
	 * 创建svn客户操作服务
	 * 
	 * @author Allen
	 * @date 2016年8月11日
	 */
	public SVNClientManager createSVNClientManager();
}
