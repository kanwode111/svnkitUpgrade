package com.emg.update.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;

import com.emg.svn.conf.SvnConfig;
import com.emg.svn.factory.DemoSvn;
import com.emg.svn.inf.ISvn;
import com.emg.svn.inf.service.ISvnDbLog;
import com.emg.update.dto.DownloadErrCodeEnum;
import com.emg.update.dto.DownloadFileRequestModel;
import com.emg.update.dto.DownloadFileResponseModel;
import com.emg.update.service.IUserService;
import com.emg.update.tool.FileUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/updateManager")
public class UpdateController {
	private static final Logger logger = LoggerFactory.getLogger(UpdateController.class);
	@Resource
	private IUserService userService;
	
	private String username;
	private String password;
	private String url;
	private String path;
	


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public static final String ERROR_NO_HOST_CODE =  "1";
	public static final String ERROR_NO_HOST = "SVN账号不能为空";
	public static final String ERROR_NO_USER = "用户名和密码不能为空";
	
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
	@RequestMapping(value = "/connectSVN", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	public String connectSVN(String host, String username, String password) {
		// boolean flag = false;
		host = this.decodeValue(host);
		username = this.decodeValue(username);
		password = this.decodeValue(password);
		try {
			
			if(host == null || host.trim().length() == 0) return "{\"result\":\"failure\", \"errorInfo\":\"" + ERROR_NO_HOST + "\"}";
			
			setupLibrary();
			
			if(username == null || username.trim().length() == 0 || password == null || password.trim().length() == 0)
				return "{\"result\":\"failure\", \"errorInfo\":\"" + ERROR_NO_USER + "\"}";
			
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
		this.username = username;
		this.password = password;
		this.url = host;
		return  "{\"result\":\"success\"}";

	}

	/**
	 * 判断是否需要更新
	 * 
	 * @param path
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/needUpgrade", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	public String needUpgrade(String path) {
		// boolean flag = false;
		path = this.decodeValue(path);
		if((path == null || path.trim().length() == 0)) return "{\"result\":\"no\"}";
		if(this.url == null || this.url.length() == 0) return "{\"result\":\"no\"}";
		// boolean result = svn.isNeeadUpdate(path);
		boolean result = svn.isNeeadUpdateURL(this.url, path);
		if (result) return "{\"result\":\"yes\"}";
		return "{\"result\":\"no\"}";
	}

	/*@ResponseBody
	@RequestMapping(value = "/updateProject", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	public String updateProject(boolean isAll, String host, String path) {
		// List<String> result = svn.diffAllPath(path);
		host = this.decodeValue(host);
		path = this.decodeValue(path);
		if((host == null || host.trim().length() == 0) && (this.url != null && this.url.trim().length() >0)) host = this.url;
		if((path == null || path.trim().length() == 0) && (this.path != null && this.path.trim().length() >0)) path = this.path;
		
		logger.debug("update begin...");
		try {
		if (isAll ) {
			this.updateAllProject(host, path);
		} else {
			this.updateDesignedProject(path, "开始更新", false);
		}
		logger.info("update finish now return");
		return  "{\"result\":\"success\"}";
		}catch(Exception e) {
			e.printStackTrace();
			// e.getMessage()
		}
		return  "{\"result\":\"failure\"}";
	}*/
	
	@ResponseBody
	@RequestMapping(value = "/updateProject", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	public String updateProject(boolean isAll, String host, String path) {
		// List<String> result = svn.diffAllPath(path);
		host = this.decodeValue(host);
		path = this.decodeValue(path);
		if((host == null || host.trim().length() == 0) && (this.url != null && this.url.trim().length() >0)) host = this.url;
		if((path == null || path.trim().length() == 0) && (this.path != null && this.path.trim().length() >0)) path = this.path;
		
		logger.debug("update begin...");
		try {
		if (isAll ) {
			this.updateAllProject(host, path);
		} else {
			this.updateDesignedProject(path, "开始更新", false);
		}
		logger.info("update finish now return");
		return  "{\"result\":\"success\"}";
		}catch(Exception e) {
			e.printStackTrace();
			// e.getMessage()
		}
		return  "{\"result\":\"failure\"}";
	}

	/**
	 * 全部更新，覆盖本地文件
	 * 
	 * @param url
	 * @param path
	 */
	@ResponseBody
	@RequestMapping(value = "/updateAllProject", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	public String updateAllProject(String url, String path) {
		// svn.checkOut(url, path);
		try {
			List<SVNDirEntry> list = svn.listFolder(null,url);
			JSONArray array = new JSONArray();
			if(list == null || list.size() == 0) return "{\"result\":\"failure\"}";
			for(SVNDirEntry entry : list) {
				if (entry.getKind() == SVNNodeKind.DIR) continue;
				
				DownloadFileRequestModel req = new DownloadFileRequestModel();
		        req.setFilePath(entry.getURL().toString());
		        req.setStart(0);
				DownloadFileResponseModel model = FileUtil.readFileByte(req);
				JSONObject json = JSONObject.fromObject(model);
				array.add(json);
			}
			
			return array.toString();
        } catch (IOException e) {
            e.printStackTrace();
            DownloadFileResponseModel vo = new DownloadFileResponseModel();
            vo.setErrCode(DownloadErrCodeEnum.READ_FILE_EXCEPTION);
            return JSONObject.fromObject(vo).toString();
        }
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
	
	private String decodeValue(String value) {
		if(value == null || value.trim().length() == 0) return null;
		  try {
			return java.net.URLDecoder.decode(value,   "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		    
		  return null;
		  
	}
	
	
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}