package com.emg.update.dto;

import java.io.Serializable;
import java.util.Date;

import com.emg.update.tool.DataUtil;

/**
 * 自动生成文件模型
 * @author Administrator
 *
 */
public class FileModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6111882975904036876L;
	
	private String filename;
	private long filesize;
	private long version;
	private String updatetime;
	private String svnurl;
	
	public String getFilename() {
		return filename;
	}



	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getUpdatetime() {
		return this.updatetime;
	}



	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}



	
	@Override  
    public String toString() {  
		
        return  "{\"filename\": \"" + filename + "\", \"filesize\": \"" + filesize + "\", \"version\": \"" + version + "\",\"updatetime\": \"" + updatetime + "\",\"svnurl\": \"" + svnurl + "\",}";  
    }



	public long getFilesize() {
		return filesize;
	}



	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}



	public long getVersion() {
		return version;
	}



	public void setVersion(long version) {
		this.version = version;
	}



	public String getSvnurl() {
		return svnurl;
	}



	public void setSvnurl(String svnurl) {
		this.svnurl = svnurl;
	}  
	

}
