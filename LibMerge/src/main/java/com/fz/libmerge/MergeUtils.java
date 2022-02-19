package com.fz.libmerge;

import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Title：
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
public class MergeUtils {

    public static boolean isExists(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        return new File(filePath).exists();
    }

    public static boolean delFile(String filePath){
        if(isExists(filePath)){
            return new File(filePath).delete();
        }
        return false;
    }

    public static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean copyFile(String originalPath, String newPath) {
        if (originalPath != null && !"".equals(originalPath) && newPath != null && !"".equals(newPath)) {
            File originalFile = new File(originalPath);
            if (!originalFile.exists()) {
                return false;
            } else {
                FileInputStream inputStream = null;

                boolean var5;
                try {
                    inputStream = new FileInputStream(originalFile);
                    boolean var4 = copyFile((InputStream) inputStream, newPath);
                    return var4;
                } catch (FileNotFoundException var9) {
                    var5 = false;
                } finally {
                    close(inputStream);
                }

                return var5;
            }
        } else {
            return false;
        }
    }

    public static boolean copyFile(InputStream inputStream, String newPath) {
        if (inputStream != null && newPath != null && !"".equals(newPath)) {
            File newFile = new File(newPath);
            OutputStream out = null;
            BufferedOutputStream bout = null;

            try {
                if (!newFile.exists()) {
                    newFile.getParentFile().mkdirs();
                    newFile.createNewFile();
                }

                out = new FileOutputStream(newFile);
                bout = new BufferedOutputStream(out);
                byte[] data = new byte[1024];

                int read = 0;
                while ((read = inputStream.read(data)) != -1) {
                    bout.write(data, 0, read);
                }

                bout.flush();
                return true;
            } catch (Exception var11) {
                var11.printStackTrace();
            } finally {
                close(bout);
                close(out);
                close(inputStream);
            }
        }
        return false;
    }

}
