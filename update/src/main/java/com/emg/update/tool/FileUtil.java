package com.emg.update.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.ContentType;

import org.apache.http.HttpEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.hsqldb.types.Charset;

import com.emg.update.dto.DownloadErrCodeEnum;
import com.emg.update.dto.DownloadFileRequestModel;
import com.emg.update.dto.DownloadFileResponseModel;

/**
 * 文件操作工具类
 * @author Administrator
 *
 */
public class FileUtil {
	
	/**
	 * 获取所有文件列表，不包含目录和空目录
	 * @param fileList
	 * @param path
	 * @return
	 */
	public static List<File> getAllFile(List<File> fileList,String path){
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
	
	/**
	 * 获取所有文件，以及路径
	 * @param fileList
	 * @param path
	 * @return
	 */
	public static List<File> getAllFileWithDir(List<File> fileList,String path) {
		if (fileList == null)
			fileList = new ArrayList<File>();
		File dirFile = new File(path);
		if(dirFile.isHidden()) return fileList;
		if (dirFile.exists()) {
			fileList.add(dirFile);
			File[] files = dirFile.listFiles();
			if (files == null)
				return fileList;
			for (File fileChildDir : files) {
				// 输出文件名或者文件夹名
				// System.out.print(fileChildDir.getName());
				if (fileChildDir.isDirectory()) {
					// System.out.println(" :  此为目录名");
					// 通过递归的方式,可以把目录中的所有文件全部遍历出来
					fileList.add(fileChildDir);
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
	
	
	// 单次传送最大字节数20M。
    private final static int maxsize_once;

    static
    {
        maxsize_once = 1024 * 1024 * 20;
    }
	
	/**
     * 读取文件内容，构建文件字节流返回对象。
     * @param req 请求参数
     * @return 读取文件返回值。
     * @throws IOException IO异常
     */
    public static DownloadFileResponseModel readFileByte(DownloadFileRequestModel req) throws IOException { 

    	DownloadFileResponseModel vo = new DownloadFileResponseModel();

        // 获取判断文件最近修改时间
        File fileObject = new File(req.getFilePath());
        final long fileLastModifiedTime = fileObject.lastModified();

        // 判断分批传过程中文件是否修改
        if (fileLastModifiedTime != req.getFileLastModifiedTime()
                && req.getFileLastModifiedTime() != -1) 
        {   
            vo.setErrCode(DownloadErrCodeEnum.FILE_HAS_MODIFIED_WHILE_DOWNLOAD);
            return vo;
        }

        // 读取文件字节流。
        ByteArrayOutputStream fileStream = new ByteArrayOutputStream(1024);
        FileInputStream file = new FileInputStream(req.getFilePath());

        byte[] readbuff = new byte[1024];
        while(file.read(readbuff) != -1) {
            fileStream.write(readbuff);
        }
        file.close();

        // 构建返回文件字节信息。超过20M, 一次只返回20M。
        final byte[] fileBuff = fileStream.toByteArray();
        int end = 0;
        if (fileBuff.length - req.getStart() > maxsize_once) {
            end = req.getStart() + maxsize_once;
            vo.setEof(false);
        } else {    
            end = fileBuff.length;
            vo.setEof(true);
        }

        // 拷贝[start, end)范围内的字节到返回值中。
        vo.setFileByteBuff(Arrays.copyOfRange(fileBuff, req.getStart(), end));
        vo.setStart(end);
        vo.setErrCode(DownloadErrCodeEnum.DOWN_LOAD_SUCCESS);
        vo.setFileLastModifiedTime(fileLastModifiedTime);

        fileStream.close();

        return vo;
    }
    
    /**
     * 把/替换成\
     * @param value
     * @return
     */
    public static String replaceLeftToRight(String value) {
    	if(value == null) return null;
    	return value.replaceAll("/", "\\\\");
    }
    
    /**
     * 把\替换成/
     * @param value
     * @return
     */
    public static String replaceRightToLeft(String value) {
    	if(value == null) return null;
    	return value.replaceAll("\\\\", "/");
    }
    
    
}
