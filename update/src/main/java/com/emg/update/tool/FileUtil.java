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
import java.util.Arrays;

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
    
    
}
