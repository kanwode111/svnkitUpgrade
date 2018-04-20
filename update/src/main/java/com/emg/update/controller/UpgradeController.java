package com.emg.update.controller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
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
import com.emg.update.dto.FileModel;
import com.emg.update.service.IUserService;
import com.emg.update.tool.DataUtil;
import com.emg.update.tool.FileUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/upgradeManager")
public class UpgradeController {

	private static final Logger logger = LoggerFactory.getLogger(UpdateController.class);

	public static final String ERROR_NO_HOST = "SVN账号不能为空";
	public static final String ERROR_NO_USER = "用户名和密码不能为空";

	public static final String ERROR_LOGIN_INFO = "登陆信息不正确,请检查用户名密码,或者SVN路径是否正确";

	public static final String ERROR_HOST_ADDRESS_CODE = "指定的SVN路径不存在";
	public static final String ERROR_HOST_ADDRESS = "指定的SVN路径不存在";

	@Resource
	private IUserService userService;

	private String username;
	private String password;
	private String url;
	private String path;

	private List<FileModel> files;

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
	@RequestMapping(value = "/connectSVN", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String connectSVN(String host, String username, String password) {
		// boolean flag = false;
		host = DataUtil.decodeValue(host);
		username = DataUtil.decodeValue(username);
		password = DataUtil.decodeValue(password);
		try {

			if (host == null || host.trim().length() == 0)
				return "{\"result\":\"failure\", \"errorInfo\":\"" + ERROR_NO_HOST + "\"}";

			setupLibrary();

			if (username == null || username.trim().length() == 0 || password == null || password.trim().length() == 0)
				return "{\"result\":\"failure\", \"errorInfo\":\"" + ERROR_NO_USER + "\"}";

			// 初始化实例
			DemoSvn ts = new DemoSvn(username, password, host);
			// 获得操作对象
			this.svn = ts.execute(SvnConfig.log);

			// 得到版本库信息
			SVNRepository repository = svn.createSVNRepository();
			if (repository == null)
				return "{\"result\":\"failure\", \"errorInfo\":\"" + ERROR_LOGIN_INFO + "\"}";
			// 得到基础操作对象
			svn.createSVNClientManager();
		} catch (Exception e) {

			e.printStackTrace();
			return "{\"result\":\"failure\", \"errorInfo\":\"" + ERROR_LOGIN_INFO + "\"}";
			// flag = false;
		}
		this.username = username;
		this.password = password;
		this.url = host;
		return "{\"result\":\"success\"}";

	}


	/**
	 * 判断是否需要更新传递JSON形式文件列表
	 * 
	 * @param path
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/needUpgrade", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String needUpgrade(String url, String files) {
		if(files == null)return "{\"result\":\"success\"}";
		files = DataUtil.decodeValue(files);
		List<FileModel> fileList = this.getFileList(files);

		// 如果为空则表示当前为空目录，直接进行下载
		if (fileList == null)
			return "{\"result\":\"success\"}";
		this.files = fileList;
		List<SVNDirEntry> list = svn.listFolder(null, url);
		boolean flag = false;
		if (list == null || list.size() == 0)
			return "{\"result\":\"failure\"}";
		for (SVNDirEntry entry : list) {
			if (entry.getKind() == SVNNodeKind.DIR)
				continue;
			for (FileModel model : fileList) {
				// logger.info(FileUtil.replaceRightToLeft(entry.getURL().getPath()));
				if (FileUtil.replaceRightToLeft(entry.getURL().getPath()).indexOf(FileUtil.replaceRightToLeft(model.getFilename())) <1)
					continue;
				/*
				 * if(entry.getAuthor() != model.author) { flag = false; break; }
				 */
				/*if (entry.getSize() != model.getFilesize() || entry.getDate() != DataUtil.stringToDate(model.getUpdatetime())
						|| entry.getRevision() != model.getVersion()) {*/
				Date date = DataUtil.stringToDate(model.getUpdatetime());
				if (entry.getSize() != model.getFilesize() || (date != null && entry.getDate().getTime() > date.getTime())) {
						
					flag = true;
					break;
				}
			}
		}
		if (flag)
			return "{\"result\":\"success\"}";
		return "{\"result\":\"failure\"}";
	}

	/**
	 * 全部更新，覆盖本地文件
	 * 
	 * @param url
	 * @param path
	 *//*
	@ResponseBody
	@RequestMapping(value = "/checkout", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String checkout(String url, String path) {
		// svn.checkOut(url, path);
		try {
			List<SVNDirEntry> list = svn.listFolder(url);
			JSONArray array = new JSONArray();
			if (list == null || list.size() == 0)
				return "{\"result\":\"failure\"}";
			for (SVNDirEntry entry : list) {
				if (entry.getKind() == SVNNodeKind.DIR)
					continue;

				DownloadFileRequestModel req = new DownloadFileRequestModel();
				req.setFilePath(entry.getURL().toString());
				req.setStart(0);
				DownloadFileResponseModel model = readFileByte(req);
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
	}*/
	
	
	/**
	 * 获取需要checkout文件列表
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getSVNFileList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String getSVNFileList(String url, String files) {
		if ((url == null || url.trim().length() == 0) && this.url != null)
			url = this.url;
		if ((url == null || url.trim().length() == 0) && this.url != null)
			url = this.url;
		if (files == null || files.trim().length() == 0)
			return this.checkoutFileList(url);
		
		// files = DataUtil.decodeValue(files);
		return this.updateFileList(url, files);
		// return result;
	}
	/**
	 * 获取需要checkout文件列表
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkoutFileList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String checkoutFileList(String url) {
		if ((url == null || url.trim().length() == 0) && this.url != null)
			url = this.url;
		if(svn == null) return "{\"result\":\"failure\"}"; 
		List<SVNDirEntry> dirList =  svn.listFolder(null, url);
		if(dirList == null) return "{\"result\":\"failure\"}"; 
		String result = "{files:[";
		for (SVNDirEntry entry : dirList) {
			
			FileModel file = new FileModel();
			file.setFilename(entry.getName());
			file.setFilesize(entry.getSize());
			file.setUpdatetime(DataUtil.dateToString(entry.getDate()));
			file.setVersion(entry.getRevision());
			file.setSvnurl(entry.getURL().toString());
			result += file.toString() + ",";
		}
		result = result.substring(0, result.length() - 1) + "]}";
		logger.info(result);
		return result;
	}

	/**
	 * 获取需要checkout文件列表
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/updateFileList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String updateFileList(String url, String files) {
		
		List<FileModel> fileList = this.getFileList(files);
		// 如果为空则表示当前为空目录，直接进行下载
		if (fileList == null)
			return this.checkoutFileList(url);
		String result = "{files:[";

		List<SVNDirEntry> list = svn.listFolder(null, url);
		if (list == null || list.size() == 0)
			return "";
		for (SVNDirEntry entry : list) {
			if (entry.getKind() == SVNNodeKind.DIR)
				continue;
			for (FileModel model : fileList) {
				if (FileUtil.replaceRightToLeft(entry.getURL().getPath()).indexOf(FileUtil.replaceRightToLeft(model.getFilename())) <1)
					continue;
				/*if (entry.getSize() != model.getFilesize() || entry.getDate() != DataUtil.stringToDate(model.getUpdatetime())
						|| entry.getRevision() != model.getVersion()) {*/
				if(model.getSvnurl() == null || model.getSvnurl().trim().length() == 0) model.setSvnurl(entry.getURL().toString());
				Date date = DataUtil.stringToDate(model.getUpdatetime());
				if (entry.getSize() != model.getFilesize() || (date != null && entry.getDate().getTime() > date.getTime())) {
				
					result += model.toString() + ",";
				}
			}
		}
		result = result.substring(0, result.length() - 1) + "]}";
		logger.info(result);
		return result;
	}
	
	/**
	 * 更新指定的文件
	 * @param file
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/downloadFile", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String downloadFile(String file) {
		
		file = DataUtil.decodeValue(file);
		if (file == null) return "{\"result\":\"failure\"}"; 
		JSONObject json = JSONObject.fromObject(file);
		if(svn == null) return "{\"result\":\"failure\"}"; 
		ByteArrayOutputStream out = (ByteArrayOutputStream)svn.getFile(json.get("svnurl").toString());
		if(out == null ) return "{\"result\":\"failure\"}"; 
		/*JSONObject result = new JSONObject();
		result.put("result", "success");*/
		String str = new String(Base64.encodeBase64(out.toByteArray()));
		// result.put("content", out.)
		logger.info("下载成功");
		// 服务器再用Base64解密–>Gzip解压
		return "{\"result\":\"success\", \"content\":\"" + str +  "\",\"filename\":\"" + json.get("filename") +"\", \"svnurl\":\"" +json.get("svnurl") + "\"}"; 
	}

	/**
	 * 根据JSON形式字符串获取所有文件列表
	 * 
	 * @param files
	 * @return
	 */
	private List<FileModel> getFileList(String files) {
		if(files == null ||files.trim().length() == 0) return null; 
		JSONObject json = JSONObject.fromObject(files);
		JSONArray array = json.getJSONArray("files");
		List<FileModel> list = new ArrayList<FileModel>();
		if (array == null)
			return null;
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			FileModel file = (FileModel) JSONObject.toBean(obj, FileModel.class);
			if (file != null)
				list.add(file);
		}
		return list;
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
