package com.emg.update.controller;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;

import com.emg.svn.conf.SvnConfig;
import com.emg.svn.factory.DemoSvn;
import com.emg.svn.inf.ISvn;
import com.emg.svn.inf.service.ISvnDbLog;
import com.emg.update.service.IUserService;

@Controller
@RequestMapping("/upgradeManager")
public class UpdateController {
	@Resource
	private IUserService userService;
	
	public static final String ERROR_NO_HOST_CODE =  "1";
	public static final String ERROR_NO_HOST = "SVN账号不能为空";
	
	public static final String ERROR_LOGIN_INFO_CODE = "2";
	public static final String ERROR_LOGIN_INFO = "登陆信息不正确,请检查用户名密码,或者SVN路径是否正确";
	
	public static final String ERROR_HOST_ADDRESS_CODE = "指定的SVN路径不存在";
	public static final String ERROR_HOST_ADDRESS = "指定的SVN路径不存在";
	
	public static final String NO_NEED_UPDATE = "0";
	public static final String YES_NEED_UPDATE = "1";

	

	/*@RequestMapping("/addUser")
	public String addUser(HttpServletRequest request, Model model) {
		User user = new User();
		user.setName(String.valueOf(request.getParameter("name")));
		user.setPassword(String.valueOf(request.getParameter("password")));
		userService.addUser(user);
		return "redirect:/user/userList";
	}*/

	// SVN服务接口
	private ISvn svn;
	
	
	/**
	 * 通过不同的协议初始化版本库
	 */
	private void setupLibrary() {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
		FSRepositoryFactory.setup();
	}

	/**
	 * 用来连接SVN
	 * 
	 * @param host
	 *            SVN地址
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/connectSVN", method = RequestMethod.GET,produces="application/json;charset=UTF-8")
	public String connectSVN(String host, String username, String password) {
		// boolean flag = false;
		
		try {
			
			if(host == null || host.trim().length() == 0) return "{\"result\":\"failure\", \"errorInfo\":\"" + ERROR_NO_HOST + "\"}";
			
			setupLibrary();
			
			// 初始化实例
			DemoSvn ts = new DemoSvn(username, password, host);
			// 获得操作对象
			this.svn = ts.execute(SvnConfig.log);
			
			// 得到版本库信息
			SVNRepository repository = svn.createSVNRepository();
			if(repository == null) return  "{\"result\":\"failure\", \"errorInfo\":\"" + ERROR_LOGIN_INFO + "\"}";
			// 得到基础操作对象
			svn.createSVNClientManager();
		} catch (Exception e) {

			e.printStackTrace();
			return  "{\"result\":\"failure\", \"errorInfo\":\"" + ERROR_LOGIN_INFO + "\"}";
			// flag = false;
		}
		return  "{\"result\":\"success\"}";

	}

	/**
	 * 判断是否需要更新
	 * 
	 * @param path
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/needUpgrade", method = RequestMethod.GET,produces="application/json;charset=UTF-8")
	public String needUpgrade(String path) {
		// boolean flag = false;
		List<String> result = svn.diffAllPath(path);

		return "{\"result\":\"yes\"}";
	}

	@ResponseBody
	@RequestMapping(value = "/updateProject", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	public String updateProject(boolean isAll, String host, String path) {
		// List<String> result = svn.diffAllPath(path);
		if (isAll) {
			this.updateAllProject(host, path);
		} else {
			this.updateDesignedProject(path, "开始更新", false);
		}
		return  "{\"result\":\"success\"}";
	}

	/**
	 * 全部更新，覆盖本地文件
	 * 
	 * @param url
	 * @param path
	 */
	@ResponseBody
	@RequestMapping(value = "/updateAllProject", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	public void updateAllProject(String url, String path) {
		System.out.println(svn.getClass().getName() + "checkout");
		svn.checkOut(url, path);
	}

	@RequestMapping("/updateDesignedProject")
	public void updateDesignedProject(String path, String message, boolean uLock) {
		svn.update(path, message, uLock, new ISvnDbLog<String>() {
			@Override
			public boolean addLog(String name, SvnConfig dbType, long versionId, File[] files) {
				for (File file : files) {
					System.out.println(file.getName() + "已经更新");
				}
				return true;
			}

			@Override
			public List<String> getLog(String name, Date startTime, Date endTime) {
				// System.out.println("get 到 log 了");
				return null;
			}
		});
	}
}