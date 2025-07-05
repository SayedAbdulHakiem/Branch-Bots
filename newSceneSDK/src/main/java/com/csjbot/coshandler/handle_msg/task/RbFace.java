package com.csjbot.coshandler.handle_msg.task;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.csjbot.cosclient.utils.CosLogger;
import com.csjbot.coshandler.core.CsjRobot;
import com.csjbot.coshandler.global.NTFConstants;
import com.csjbot.coshandler.global.RSPConstants;
import com.csjbot.coshandler.log.Csjlogger;
import com.csjbot.coshandler.util.ShellUtil;

import org.json.JSONObject;

/**
 * 负责人脸识别模块消息的处理
 * Created by jingwc on 2017/8/12.
 */

public class RbFace extends RbBase {
    protected Handler mainHandler = new Handler(Looper.getMainLooper());
    private int mCount = 0;//连续 重启两次
    private long start = 0;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mCount ++ ;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                ShellUtil.execCmd("am force-stop com.csjbot.face", true, false);
                ShellUtil.execCmd("am force-stop com.csjbot.face", true, false);
                mainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ShellUtil.execCmd("am start -n com.csjbot.face/com.csjbot.face.MainActivity", true, false);
                    }
                },3000);
                //ShellUtil.execCmd("am start -n com.csjbot.face/com.csjbot.face.MainActivity", true, false);
                //ShellUtil.execCmd("am startservice -n com.csjbot.face/com.csjbot.face.FaceBaiduDetectService", true, false);
                Csjlogger.info("facePush2 = > {}","restart face app");
            }
            if (mCount < 2){
                mainHandler.removeCallbacks(runnable);
                mainHandler.postDelayed(runnable, 70 * 1000);
            }
        }
    };
    @Override
    protected void handleNTFMessage(String dataSource, String msgId) {
        switch (msgId){
            case NTFConstants.FACE_DETECT_PERSON_NEAR_NTF:// 检测到有人脸靠近
//                CosLogger.debug("RbFace-------->FACE_DETECT_PERSON_NEAR_NTF");
                String value = getSingleField(dataSource,"person");
//                Robot.sessionId = getSingleField(dataSource,"session_id");
                // person = true 检测有人脸出现
                // person = false 检测区域没有检测到人
                mCount = 0;
                mainHandler.removeCallbacks(runnable);
                mainHandler.postDelayed(runnable, 10000);
                if (System.currentTimeMillis() - start > 6 * 1000){//每隔几秒钟打印一次
                    if (!TextUtils.isEmpty(value)){
                        Csjlogger.info("facePush = > {}",value);
                    } else {
                        Csjlogger.info("facePush = > {}","value is empty");
                    }
                    start = System.currentTimeMillis();
                }
                CsjRobot.getInstance().pushFace(Boolean.valueOf(value));
                //CsjRobot.getInstance().pushPersonCheckInfo(3, true);
                break;
            case NTFConstants.FACE_DETECT_FACE_LIST_NTF:// 识别到人脸信息
                // 推送检测到的人脸识别信息
                CosLogger.info("personInfo -------->FACE_DETECT_FACE_LIST_NTF" + dataSource);
                CsjRobot.getInstance().pushFace(dataSource);
                break;
            case NTFConstants.FACE_SYNC_UNDO_REG_NTF:
                break;
            case NTFConstants.FACE_DETECT_COORDINATE_NTF:
                CsjRobot.getInstance().pushFaceCoordinate(dataSource);
                break;
            case NTFConstants.FACE_SNAPSHOT_NTF:
                try {
                    JSONObject jsonObject = new JSONObject(dataSource);
                    String decodedNeedBitmapDataStr = jsonObject.getString("needBitmapData");
                    byte[] decodedBytes = android.util.Base64.decode(decodedNeedBitmapDataStr, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    CsjRobot.getInstance().pushCamera(bitmap);
                } catch (Exception e) {
                    Csjlogger.debug("照片 获取错误" + e);
                }
//                String json = "{\"msg_id\":\"" + RSPConstants.FACE_SNAPSHOT_NTF + "\",\"error_code\":" + 0 + ",\"needBitmapData\":" + needBitmapData + "}";
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleRSPMessage(String dataSource, String msgId) {

        switch (msgId){
            case RSPConstants.FACE_SAVE_RSP:
                CsjRobot.getInstance().pushFaceSave(dataSource);
                break;
            case RSPConstants.FACE_DATABASE_RSP:
                CsjRobot.getInstance().pushFaceList(dataSource);
                CosLogger.debug("FACE_DATABASE_RSP------------------->json:"+dataSource);
                break;
            case RSPConstants.FACE_SNAPSHOT_RESULT_RSP:
                CsjRobot.getInstance().pushSnapshoto(dataSource);
                break;
            case RSPConstants.FACE_SYNC_UNDO_REG_RSP:
                break;
            default:
                break;
        }
    }
}
