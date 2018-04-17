package com.emg.svn.filter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.io.SVNRepository;

import com.emg.svn.conf.SvnConfig;
import com.emg.svn.factory.DemoSvn;
import com.emg.svn.inf.ISvn;
import com.emg.svn.inf.service.ISvnDbLog;

@Component
@Path("/UpgradeManage")
public class UpgradeManage {

	// SVN服务接口
	private ISvn svn;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
	@GET
	@Path("connectSVN")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean connectSVN(@QueryParam("host") String host, @QueryParam("username") String username,
			@QueryParam("password") String password) {
		boolean flag = false;
		try {
			// 初始化实例
			DemoSvn ts = new DemoSvn(username, password, host);
			// 获得操作对象
			this.svn = ts.execute(SvnConfig.log);

			// 得到版本库信息
			SVNRepository repository = svn.createSVNRepository();
			// 得到基础操作对象
			svn.createSVNClientManager();
		} catch (Exception e) {

			e.printStackTrace();
			flag = false;
		}
		return flag;

	}

	/**
	 * 判断是否需要更新
	 * 
	 * @param path
	 * @return
	 */
	@GET
	@Path("needUpgrade")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean needUpgrade(@QueryParam("path") String path) {
		// boolean flag = false;
		List<String> result = svn.diffAllPath(path);

		return (result != null);
	}

	@GET
	@Path("updateProject")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateProject(@QueryParam("uLock") boolean isAll, 
			@QueryParam("host") String host,
			@QueryParam("path") String path) {
		List<String> result = svn.diffAllPath(path);
		if(isAll) {
			this.updateAllProject(host, path);
		}else {
			this.updateDesignedProject(path, "开始更新", false);
		}
	}

	/**
	 * 全部更新，覆盖本地文件
	 * 
	 * @param url
	 * @param path
	 */
	@GET
	@Path("needUpgrade")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateAllProject(@QueryParam("url") String url, @QueryParam("path") String path) {
		svn.checkOut(url, path);
	}

	@GET
	@Path("updateDesignedProject")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateDesignedProject(@QueryParam("path") String path, @QueryParam("message") String message,
			@QueryParam("uLock") boolean uLock) {
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
