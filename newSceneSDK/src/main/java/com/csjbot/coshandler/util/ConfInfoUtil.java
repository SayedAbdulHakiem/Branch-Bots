package com.csjbot.coshandler.util;

import android.os.Environment;
import android.text.TextUtils;

import com.csjbot.coshandler.log.Csjlogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;


/**
 * Created by xiasuhuei321 on 2017/10/25.
 * author:luo
 * e-mail:xiasuhuei321@163.com
 * <p>
 * desc:用来获取Sn的工具类
 */

public class ConfInfoUtil {
    /**
     * 存放SN文件的路径
     */
    private static final String SN_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + ".robot_info" + File.separator + ".sys.txt";

    private static final String SN_NAME = "sn";

    private static String SN = null;

    /**
     * 获取 SN ，此处仅仅用于从配置文件获取 SN ，中间有 IO 操作
     * 自行考虑是否将这个操作放在线程中
     *
     * @return null 如果没有 SN，否则是 SN 的字符串
     */
    public static String getSN() {
        return TextUtils.isEmpty(SN) ? getSn() : SN;
    }

    /**
     * 从文件读取sn
     *
     * @return sn
     */
    private static String getSn() {
        try {
            File file = new File(SN_PATH);
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
                return "";
            }
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String str;
            StringBuilder builder = new StringBuilder();
            while (!TextUtils.isEmpty(str = reader.readLine())) {
                builder.append(str);
            }
            fis.close();
            reader.close();
            String json = builder.toString().trim();
            if (TextUtils.isEmpty(json)) {
                return "";
            }
            return SN = new JSONObject(json).getString(SN_NAME);
        } catch (Exception e) {
            Csjlogger.info("文件错误" + e);
        }
        return "";
    }

    /**
     * 文件写入sn
     *
     * @param sn
     */
    public static void putSn(String sn) {
        if (TextUtils.isEmpty(sn)) {
            return;
        }
        Csjlogger.debug("  1111111111111111111  ");
        SN = sn;
        FileOutputStream writerStream = null;
        try {
            File file = new File(SN_PATH);
            if (!file.exists()) {
                file.createNewFile();
            }
            writerStream = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(writerStream, StandardCharsets.UTF_8));
            JSONObject object = new JSONObject();
            object.put(SN_NAME, sn);
            writer.write(object.toString());
            writer.close();
            FileDescriptor fd = writerStream.getFD();

            fd.sync();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
