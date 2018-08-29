package com.minnovel.weiweiyixiaohenqingcheng.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    /**
     * 从assets目录下拷贝文件
     *
     * @param context            上下文
     * @param assetsFilePath     文件的路径名如：SBClock/0001cuteowl/cuteowl_dot.png
     * @param targetFileFullPath 目标文件路径如：/sdcard/SBClock/0001cuteowl/cuteowl_dot.png
     */
    public static void copyFileFromAssets(Context context, String assetsFilePath, String targetFileFullPath) {
        Log.d("Tag", "copyFileFromAssets ");
        InputStream assestsFileImputStream;
        try {
            assestsFileImputStream = context.getAssets().open(assetsFilePath);
            copyFile(assestsFileImputStream, targetFileFullPath);
        } catch (IOException e) {
            Log.d("Tag", "copyFileFromAssets " + "IOException-" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void copyFile(InputStream in, String targetPath) {
        try {
            File targetFile = new File(targetPath);
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = in.read(buffer)) != -1) {// 循环从输入流读取
                // buffer字节
                fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
            }
            fos.flush();// 刷新缓冲区
            in.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取小说在缓存中的路径
     *
     * @param context
     * @return
     */
    public static String getNovelPath(Context context, String name) {
        if (context == null || TextUtils.isEmpty(name)) {
            return null;
        }
        File cache = context.getExternalFilesDir(null);
        String novelDir = cache.getAbsolutePath() + File.separator + name;
        File novelDirFile = new File(novelDir);
        if (novelDirFile.exists()) {
            novelDirFile.delete();
        }
        try {
            novelDirFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return novelDirFile.getAbsolutePath();

    }


}
