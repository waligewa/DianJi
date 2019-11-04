package com.example.motor.audio;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

/**
 * Author : yanftch
 * Date : 2018/3/25
 * Time : 22:11
 * Desc :
 */

public class FileUtils {
    /**
     * 文件转byte二进制
     *
     * @param filePath
     * @return 已经转成的byte
     * @throws Exception
     */
    public static byte[] readStream(String filePath) {
        FileInputStream fs = null;
        ByteArrayOutputStream outStream = null;
        try {
            fs = new FileInputStream(filePath);
            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while (-1 != (len = fs.read(buffer))) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            fs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return outStream.toByteArray();
    }
}
