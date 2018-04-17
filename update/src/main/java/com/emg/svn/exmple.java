package com.emg.svn;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;

import com.emg.svn.conf.SvnConfig;
import com.emg.svn.factory.DemoSvn;
import com.emg.svn.inf.ISvn;
import com.emg.svn.inf.service.ISvnDbLog;
import com.emg.svn.model.SvnRepoPojo;


/**
 * 测试类
 * 
 * @author Allen
 * @date 2016年8月8日
 */
public class exmple {
	String account = "admin";
	String password = "admin";
	String path = "svn://127.0.0.1/autoUpgrade";
	String targetHead = "d:/测试";
	ISvn svn;

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
		
		/*repository.checkPath(targetHead + "/.svn/entries", -1);
		repository.checkPath(targetHead + "/pom.xml", -1);*/
		System.out.println(".svn/entries is exist" + checkPath(manager, targetHead +"/.svn/entries"));
		System.out.println("pom.xml is exist" + checkPath(manager, targetHead + "/pom.xml"));
		/** 测试--Start-- **/
		testGetRepo();
		//testCheckOut();
		// testAdd();
		// testDel();
		//testCleanUp();
		//testUpdate();
		testDiff();
		/** 测试 --End-- **/
		// 关闭库容器
		svn.closeRepo();
		// SVNURL url1 = getRepositoryAccess().getTargetURL(target1);

	}
	
	public void checkFile(File file) {
		// getRepositoryAccess().getURLFromPath(SvnTarget.fromFile(file), SVNRevision.UNDEFINED, null).<SVNURL>get(SvnRepositoryAccess.UrlInfo.url);
		
	}
	
	/**检查路径是否存在 
     * @param url 
     * @return 1：存在    0：不存在   -1：出错 
     */  
    public int checkPath(SVNClientManager manager, String url){  
    	try {  
        SVNRepository repository = manager.createRepository(SVNURL.parseURIEncoded(url), true);  
        
        SVNNodeKind nodeKind;  
        
            nodeKind = repository.checkPath("", -1);  
            boolean result = nodeKind == SVNNodeKind.NONE ? false : true;  
            if(result) return 1;  
        } catch (SVNException e) {  
            /*logger.error("checkPath error",e); */ 
            return -1;  
        }  
        return 0;  
    }  
  
	

	/**
	 * 获得版本路径文件信息
	 * 
	 * @author Allen
	 * @date 2016年8月12日
	 */
	private void testGetRepo() {
		print(svn.getRepoCatalog(""));
	}

	/**
	 * 检出到本地路径
	 * 
	 * @author Allen
	 * @date 2016年8月12日
	 */

	private void testCheckOut() {
		svn.checkOut(path, targetHead);
	}

	/**
	 * 添加文件到svn
	 * 
	 * @author Allen
	 * @date 2016年8月12日
	 */

	private void testAdd() {
		String[] strs = new String[] { targetHead + "/autoUpgrade/src/main/java/com/svn/conf/",
				targetHead + "/autoUpgrade/src/main/java/com/svn/",
				targetHead + "/autoUpgrade/src/main/java/com/svn/conf/ErrorVal.java" };
		svn.add(strs, "haha", false, new ISvnDbLog<String>() {
			@Override
			public boolean addLog(String name, SvnConfig dbType, long versionId, File[] files) {
				System.out.println("Add 到 DB 了");
				return true;
			}

			@Override
			public List<String> getLog(String name, Date startTime, Date endTime) {
				System.out.println("get 到 log 了");
				return null;
			}
		});
	}

	/**
	 * 删除文件到svn
	 * 
	 * @author Allen
	 * @date 2016年8月12日
	 */

	private void testDel() {
		String[] strs = new String[] { targetHead + "/autoUpgrade/src/main/java/com/svn/",
				targetHead + "/autoUpgrade/src/main/java/com/svn/conf/",
				targetHead + "/autoUpgrade/src/main/java/com/svn/",
				targetHead + "/autoUpgrade/src/main/java/com/svn/conf/ErrorVal.java" };
		svn.delete(strs, true, "haha", false, new ISvnDbLog<String>() {
			@Override
			public boolean addLog(String name, SvnConfig dbType, long versionId, File[] files) {
				System.out.println("del 到 DB 了");
				return true;
			}

			@Override
			public List<String> getLog(String name, Date startTime, Date endTime) {
				System.out.println("get 到 log 了");
				return null;
			}
		});
	}

	/**
	 * 更新文件到svn
	 * 
	 * @author Allen
	 * @date 2016年8月12日
	 */

	private void testUpdate() {
		String strs = targetHead + "/autoUpgrade/src/main/java/com/svn/conf/";
		svn.update(strs, "哈哈", false, new ISvnDbLog<String>() {
			@Override
			public boolean addLog(String name, SvnConfig dbType, long versionId, File[] files) {
				System.out.println("update 到 DB 了");
				return true;
			}

			@Override
			public List<String> getLog(String name, Date startTime, Date endTime) {
				System.out.println("get 到 log 了");
				return null;
			}
		});
	}

	/**
	 * 获取文件路径-
	 * 
	 * @param url
	 */
	public List<File> getFileList(List<File> fileList, String url) {
		if (fileList == null)
			fileList = new ArrayList<File>();
		File dirFile = new File(url);
		if (dirFile.exists()) {
			File[] files = dirFile.listFiles();
			if (files == null)
				return fileList;
			for (File fileChildDir : files) {
				// 输出文件名或者文件夹名
				System.out.print(fileChildDir.getName());
				if (fileChildDir.isDirectory()) {
					System.out.println(" :  此为目录名");
					// 通过递归的方式,可以把目录中的所有文件全部遍历出来
					getFileList(fileList, fileChildDir.getAbsolutePath());
				}
				if (fileChildDir.isFile()) {
					System.out.println(fileChildDir.getName());
					fileList.add(fileChildDir);
				}
			}
		}
		return fileList;

	}

	/**
	 * 测试库比对
	 * 
	 * @author liuniu
	 * @date 2016年8月12日
	 */

	private void testDiff() {
		// String[] strs = new String[] { targetHead + "/src/main/java/com/svn/conf" };

		// String[] strs = new String[] { targetHead + "/autoUpgrade/src/main/java/com/svn/conf/ErrorVal.java" };
		// List<String> s = svn.diffPath(new File(strs[0]));
		// List<String> s = svn.diffAllPath(targetHead + "/autoUpgrade");
		List<String> s = svn.diffAllPath(targetHead );
		if (s == null) return;
		for (String t : s)
			System.out.println(t);
	}
	
	/*public void isFileExist() {
		final DirParsedInfo wcInfo = obtainWcRoot(localAbsPath, isAdditionMode);
        final File localRelPath = wcInfo.localRelPath;
        SVNWCDbRoot wcRoot = wcInfo.wcDbDir.getWCRoot();
        return readInfo(wcRoot, localRelPath, fields);
	}
		SVNSqlJetStatement stmtInfo = null;
        SVNSqlJetStatement stmtActual = null;
        
        try {
            stmtInfo = wcRoot.getSDb().getStatement(info.hasField(NodeInfo.lock) ? SVNWCDbStatements.SELECT_NODE_INFO_WITH_LOCK : SVNWCDbStatements.SELECT_NODE_INFO);
            stmtInfo.bindf("is", wcRoot.getWcId(), localRelPath);
            boolean haveInfo = stmtInfo.next();
	}*/

	private void testCleanUp() {
		String[] strs = new String[] { targetHead + "/autoUpgrade/src/main/java" };
		svn.cleanUp(new File(strs[0]));
	}

	/**
	 * 打印当前版本库路径目录
	 */
	private void print(List<SvnRepoPojo> paramList) {
		System.out.print("commitMessage ");
		System.out.print("\t\t  date \t  ");
		System.out.print("\t   kind \t  ");
		System.out.print("\t name \t  ");
		System.out.print("\t repositoryRoot \t  ");
		System.out.print("\t revision \t  ");
		System.out.print("\t size \t  ");
		System.out.print("\t url \t  ");
		System.out.print("\t author \t  ");
		System.out.println("\t state \t  ");
		Collections.sort(paramList, new Comparator<SvnRepoPojo>() {
			@Override
			public int compare(SvnRepoPojo o1, SvnRepoPojo o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (SvnRepoPojo pojo : paramList) {
			System.out.print("\t" + pojo.getCommitMessage() + "\t");
			System.out.print("\t" + pojo.getDate().getTime() + "\t");
			System.out.print("\t" + pojo.getKind() + "\t");
			System.out.print("\t" + pojo.getName() + "\t");
			System.out.print("\t" + pojo.getRepositoryRoot() + "\t");
			System.out.print("\t" + pojo.getRevision() + "\t");
			System.out.print("\t" + pojo.getSize() + "\t");
			System.out.print("\t" + pojo.getUrl() + "\t");
			System.out.print("\t" + pojo.getAuthor() + "\t");
			System.out.print("\t" + pojo.getState() + "\t");
			System.out.print("\r\n");
		}
	}
	
	
  //get接口掉方法
    public  String callInterfaceGET(){
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet("http://localhost:8080/update/upgradeManager/connectSVN?host=" + path + "&username=" + account + "&password=" + password );
        
        String entityStr = null;
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            System.out.println("statusCode:"+statusCode);
            entityStr = EntityUtils.toString(entity);
            System.out.println("响应返回内容:"+entityStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return entityStr;
    }
    
  //get接口掉方法
    public  String callInterfacePost(){
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost("http://localhost:8080/update/upgradeManager/updateProject?host=" + path + "&isAll=true&path=d:/测试"  );
        
        String entityStr = null;
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity entity = httpResponse.getEntity();
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            System.out.println("statusCode:"+statusCode);
            entityStr = EntityUtils.toString(entity);
            System.out.println("响应返回内容:"+entityStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return entityStr;
    }
    
	public void testHttp() {
		
	}

	public static void main(String[] args) throws Exception {
		
		exmple e = new exmple();
		// System.out.println(e.getClass().getName());
		e.callInterfaceGET();
		e.callInterfacePost();
		//e.testCore();
	// new exmple().testCore();
	}
}
