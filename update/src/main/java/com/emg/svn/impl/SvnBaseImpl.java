package com.emg.svn.impl;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import com.emg.svn.conf.ErrorVal;
import com.emg.svn.conf.SvnConfig;
import com.emg.svn.impl.service.SvnServiceImpl;
import com.emg.svn.inf.ISvn;
import com.emg.svn.inf.service.ISvnDbLog;
import com.emg.svn.inf.service.ISvnService;
import com.emg.svn.model.SvnRepoPojo;
import com.emg.svn.tools.StringOutputSteam;

/**
 * 
 * {@link ISvnService}
 * 
 * @author Allen
 * @date 2016年8月8日
 *
 */
public class SvnBaseImpl extends SvnServiceImpl implements ISvn {

	public SvnBaseImpl(String account, String password, boolean logStatus, String repoPath) {
		super(account, password, logStatus, repoPath);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SvnRepoPojo> getRepoCatalog(String openPath) {
		try {
			if (repository == null)
				throw new Exception(ErrorVal.SVNRepository_is_null);
			Collection<SVNDirEntry> entries = repository.getDir(openPath, -1, null, (Collection<SVNDirEntry>) null);
			List<SvnRepoPojo> svns = new ArrayList<SvnRepoPojo>();
			Iterator<SVNDirEntry> it = entries.iterator();
			while (it.hasNext()) {
				SVNDirEntry entry = it.next();
				SvnRepoPojo svn = new SvnRepoPojo();
				svn.setCommitMessage(entry.getCommitMessage());
				svn.setDate(entry.getDate());
				svn.setKind(entry.getKind().toString());
				svn.setName(entry.getName());
				svn.setRepositoryRoot(entry.getRepositoryRoot().toString());
				svn.setRevision(entry.getRevision());
				svn.setSize(entry.getSize() / 1024);
				svn.setUrl(openPath.equals("") ? new StringBuffer("/").append(entry.getName()).toString() : new StringBuffer(openPath).append("/").append(entry.getName()).toString());
				svn.setAuthor(entry.getAuthor());
				svn.setState(svn.getKind() == "file" ? null : "closed");
				svns.add(svn);
			}
			super.log("获得版本库文件信息");
			return svns;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			super.log(e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean checkOut(String checkUrl, String savePath) {
		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);
		try {
			if (savePath == null || savePath.trim().equals(""))
				throw new Exception(ErrorVal.Path_no_having);
			else if (checkUrl == null || checkUrl.trim().equals(""))
				throw new Exception(ErrorVal.Url_no_having);
			File save = new File(savePath);
			if (!save.isDirectory())
				save.mkdir();
			super.log(updateClient.getClass().toString() + "updateClient类");
			
			updateClient.doCheckout(SVNURL.parseURIEncoded(checkUrl), save, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
			super.log("检出版本库信息");
			return true;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 此实现自动对Add后的数据进行提交
	 */
	@Override
	public <T> boolean add(String[] paths, String message, boolean uLocks, ISvnDbLog<? extends T> isvnLog) {
		try {
			File[] files = checkFilePaths(paths);
			files = sortF_S(files);
			SVNStatus status;
			List<File> targetList = new ArrayList<File>();
			List<List<File>> fileList = bindFile(files);
			for (int i = 0; i < fileList.size(); i++) {
				for (File f : fileList.get(i)) {
					if ((status = clientManager.getStatusClient().doStatus(f, true, true)) != null && status.getContentsStatus() != SVNStatusType.STATUS_UNVERSIONED
							&& status.getContentsStatus() != (SVNStatusType.STATUS_NONE))
						continue;
					else if (f.isFile()) {
						clientManager.getWCClient().doAdd(f, true, false, true, SVNDepth.fromRecurse(true), false, false, true);
						targetList.add(f);
						super.log("添加文件到提交队列");
					} else if (f.isDirectory()) {
						// SVNDepth.empty 保证不递归文件夹下文件
						clientManager.getWCClient().doAdd(f, false, false, false, SVNDepth.EMPTY, false, false, false);
						targetList.add(f);
						super.log("添加文件夹到提交队列");
					}
				}
			}
			long versionId = commit(targetList.toArray(new File[targetList.size()]), message, uLocks);
			if (versionId == -1)
				throw new Exception(ErrorVal.Commit_error);
			if (!isvnLog.addLog(this.svnAccount, SvnConfig.add, versionId, files))
				throw new Exception(ErrorVal.AddDbLog_error);
			return true;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Long commit(File[] files, String message, boolean uLocks) {
		try {
			if (files.length == 0) {
				super.log("无效的提交信息");
				return -1l;
			}
			long versionId = clientManager.getCommitClient().doCommit(files, uLocks, message, null, null, false, false, SVNDepth.INFINITY).getNewRevision();
			super.log("提交队列中预处理的操作操作  => 版本号: " + versionId);
			return versionId;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1l;
	}

	@Override
	public <T> boolean delete(String[] paths, boolean localDelete, String message, boolean uLock, ISvnDbLog<? extends T> isvnLog) {
		try {
			File[] files = checkFilePaths(paths);
			files = sortS_F(files);
			SVNStatus status = null;
			{
				List<File> targetList = new ArrayList<File>();
				List<List<File>> fileList = bindFile(files);
				for (int i = fileList.size() - 1; i >= 0; i--) {
					for (File f : fileList.get(i)) {
						if ((status = clientManager.getStatusClient().doStatus(f, true, true)) == null)
							throw new Exception(ErrorVal.File_Repo_no_having);
						else if (status.getContentsStatus() != SVNStatusType.STATUS_NORMAL)
							throw new Exception(ErrorVal.Repo_Status_error + status.getContentsStatus().toString());
						else {
							clientManager.getWCClient().doDelete(f, false, localDelete, false);
							if (f.isFile())
								super.log("添加文件到删除队列");
							else
								super.log("添加文件夹到删除队列");
							targetList.add(f);
						}
					}
				}
				long versionId = commit(targetList.toArray(new File[targetList.size()]), message, uLock);
				if (versionId == -1)
					throw new Exception(ErrorVal.Commit_error);
				if (!isvnLog.addLog(this.svnAccount, SvnConfig.delete, versionId, files))
					throw new Exception(ErrorVal.AddDbLog_error);
			}
			return true;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public <T> boolean update(String path, String message, boolean uLocks, ISvnDbLog<? super T> isvnLog) {
		try {
			File[] files = checkFilePaths(new String[] { path });
			// diffPath(files);
			long[] l = clientManager.getUpdateClient().doUpdate(files, SVNRevision.HEAD, SVNDepth.INFINITY, true, false);
			super.log("更新文件到操作队列");
			long versionId = l[0];// commit(files, message, uLocks);
			if (versionId == -1)
				throw new Exception(ErrorVal.Update_no_change);
			if (!isvnLog.addLog(this.svnAccount, SvnConfig.update, versionId, files))
				throw new Exception(ErrorVal.AddDbLog_error);
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<File> getAllFile(List<File> fileList,String path){
		if (fileList == null)
			fileList = new ArrayList<File>();
		File dirFile = new File(path);
		if(dirFile.isHidden()) return fileList;
		if (dirFile.exists()) {
			File[] files = dirFile.listFiles();
			if (files == null)
				return fileList;
			for (File fileChildDir : files) {
				// 输出文件名或者文件夹名
				// System.out.print(fileChildDir.getName());
				if (fileChildDir.isDirectory()) {
					// System.out.println(" :  此为目录名");
					// 通过递归的方式,可以把目录中的所有文件全部遍历出来
					getAllFile(fileList, fileChildDir.getAbsolutePath());
				}
				if (fileChildDir.isFile()) {
					// System.out.println(fileChildDir.getName());
					fileList.add(fileChildDir);
				}
			}
		}
		return fileList;
	}
	
	@Override
	public List<String> diffAllPath(String path){
		List<String> strList = new ArrayList<String>();
		try {
		if(path == null || path.trim().length() == 0)
			throw new Exception(ErrorVal.Path_no_having);
		List<File> files = this.getAllFile(null, path);
		for(File file : files) {
			List<String> str = this.diffPath(file);
			if( str != null)
			 strList.addAll(str);
		}
		return strList;
		}catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<String> diffPath(File file) {
		List<String> result = new ArrayList<String>();
		try {
			if (file == null || !file.exists())
				throw new Exception(ErrorVal.Path_no_having);
			// 获取SVNDiffClient
			SVNDiffClient diffClient = clientManager.getDiffClient();
			diffClient.setIgnoreExternals(false);
			StringOutputSteam os = new StringOutputSteam(new ArrayList<String>());
			// diffClient.
			diffClient.doDiff(file , SVNRevision.HEAD, SVNRevision.BASE, SVNRevision.HEAD, SVNDepth.INFINITY, true, os, result);
			super.log(file.getName() + "比对库路径");
			if(result != null) {
				for(String str : result) {
					super.log("存在的差异：" + str );
				}
			}
			return os.s;
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public boolean cleanUp(File file) {
		try {
			if (!file.exists())
				throw new Exception(ErrorVal.Path_no_having);
			else if (!file.isDirectory())
				throw new Exception(ErrorVal.File_is_not_directory);
			clientManager.getWCClient().doCleanup(file, false, false, false, true, true, true);
			super.log("清理完成");
		} catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean doLock() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unLock() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNeeadUpdate(String path) {
		try {
		if(path == null || path.trim().length() == 0)
			throw new Exception(ErrorVal.Path_no_having);
		List<File> files = this.getAllFile(null, path);
		for(File file : files) {
			List<String> str = this.diffPath(file);
			if( str != null)
			 return true;
		}
		}catch (SVNException e) {
			super.log(e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
