package com.csjbot.asragent.aiui_soft;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;

import com.csjbot.asragent.IAlsaRawDataSender;
import com.csjbot.asragent.aiui_soft.cae.CaeOperator;
import com.csjbot.asragent.aiui_soft.cae.CsjRecorder;
import com.csjbot.asragent.aiui_soft.cae.IRecorder;
import com.csjbot.asragent.aiui_soft.cae.OnCaeOperatorlistener;
import com.csjbot.asragent.aiui_soft.listener.MsgCollentListener;
import com.csjbot.asragent.aiui_soft.listener.OnAIUISpeakListener;
import com.csjbot.asragent.aiui_soft.listener.OnAudioDataListener;
import com.csjbot.asragent.aiui_soft.listener.OnSpeakProgressListener;
import com.csjbot.asragent.aiui_soft.recorder.RecOperator;
import com.csjbot.asragent.aiui_soft.recorder.RecordListener;
import com.csjbot.asragent.aiui_soft.util.AiuiIntentConvert;
import com.csjbot.asragent.aiui_soft.util.AudioPlayer;
import com.csjbot.asragent.aiui_soft.util.ConAIUI;
import com.csjbot.asragent.aiui_soft.util.Config;
import com.csjbot.asragent.aiui_soft.util.FileUtil;
import com.csjbot.asragent.aiui_soft.util.RecordAudioUtil;
import com.csjbot.asragent.aiui_soft.util.USBCardFiner;
import com.csjbot.cosclient.utils.CosLogger;
import com.csjbot.coshandler.BuildConfig;
import com.csjbot.coshandler.aiui.aiui_soft.send_msg.http.FucUtil;
import com.csjbot.coshandler.aiui.aiui_soft.util.HotWordsReplaceBean;
import com.csjbot.coshandler.global.NTFConstants;
import com.csjbot.coshandler.global.RSPConstants;
import com.csjbot.coshandler.log.BaseLogger;
import com.csjbot.coshandler.log.Csjlogger;
import com.csjbot.coshandler.taskscheduler.TaskScheduler;
import com.csjbot.coshandler.util.JsonFormatUtil;
import com.google.gson.Gson;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIErrorCode;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.AIUISetting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.csjbot.asragent.aiui_soft.util.AiuiIntentConvert.aiuiResultNtfBean;

public class AIUIMixedManager {
    private AIUIPushSpeechRecgContentListener ppListener;
    private MsgCollentListener msgCollentListener;
    private IAlsaRawDataSender dataSender;

    // 当前页面是否是主页
    public static boolean isHomeShow = false;

    // 多麦克算法库
    private IRecorder mCaeOperator;
    private RecOperator mRecOperator;
    // AIUI
    private AIUIAgent mAIUIAgent = null;
    // AIUI工作状态
    private int mAIUIState = AIUIConstant.STATE_IDLE;
    private Context mContext;
    private static int ret = 0;
    // 录音机工作状态
    private static boolean isRecording = false;
    // 写音频线程工作中
    private static final boolean isWriting = false;
    private final List<HotWordsReplaceBean> replaceHotWordsList = new ArrayList<>();

    private AIUIType aiuiType = AIUIType.AIUI_R16;
    private boolean mIsWakeupEnable;
    private AudioPlayer audioPlayer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int ERROR_20006_MAX_COUNT = 10;

    private int errorAudioRecordCount = 0;

    private final Runnable speakTimeOutRun = new Runnable() {
        @Override
        public void run() {
            BaseLogger.error("AIUI speak time out ");
            if (onAIUISpeakListener != null) {
                onAIUISpeakListener.onSpeakBegin();
                onAIUISpeakListener.onCompleted(-1000);
//                onAIUISpeakListener = null;
            }
            isSpeaking.set(false);
        }
    };

    public final String CONFIG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + ".robot_info" + File.separator + "volume.config";

    public static AIUIMixedManager getInstance() {
        return AiUiInstance.aiuiManager;
    }
    private static class AiUiInstance {
        static AIUIMixedManager aiuiManager = new AIUIMixedManager();
    }

    public void setPushSpeechRecgContentListener(AIUIPushSpeechRecgContentListener pushDataListener) {
        ppListener = pushDataListener;
    }

    private AIUIMixedManager() {
        initSingleWords();
    }

    public void initSDK(Context context) {
        mContext = context;

//        File aiui = new File(AIUI_PATH);
//        aiuiType = aiui.exists() ? AIUIMixedManager.AIUIType.AIUI_SOFT : AIUIMixedManager.AIUIType.AIUI_R16;
        int mPcmCard = USBCardFiner.fetchCards(-1, name -> name.contains("AIUI"));

        aiuiType = mPcmCard != -1 ? AIUIMixedManager.AIUIType.AIUI_SOFT : AIUIMixedManager.AIUIType.AIUI_R16;

        BaseLogger.info("AIUI initSDK, type = {}", aiuiType);

        if (AIUIType.AIUI_SOFT.equals(aiuiType)) {
            CaeOperator.portingFile(mContext);
        }

//        CaeOperator.AUTH_SN = DeviceUtils.getDeviceId(this);

        // 初始化AIUI
        // AIUI_DATA_ONLY 只初始化 cae 和 alsa
        // AIUIType.R16 只初始化 aiui
        // AIUIType.AIUI_SOFT 全部初始化
        if (AIUIType.AIUI_DATA_ONLY.equals(aiuiType)) {
            // 初始化CAE
            initCaeEngine();
            // 初始化alsa录音
            initAlsa();
            BaseLogger.warn("aiuiType ==  AIUI_DATA_ONLY, create initCaeEngine and initAlsa");
        } else if (AIUIType.AIUI_SOFT.equals(aiuiType)) {
            createAgent();
            // 初始化CAE
            initCaeEngine();
            // 初始化alsa录音
            initAlsa();
            BaseLogger.warn("aiuiType == AIUI_SOFT, create createAgent ,initCaeEngine and initAlsa");
        } else {
            createAgent();
            initCaeEngine();
            BaseLogger.warn("aiuiType == AIUI_R16, create createAgent ,initCaeEngine and initAlsa");
        }
    }

    public void reInit() {
        BaseLogger.info("AIUI reInit , type");
        if (onAIUISpeakListener != null) {
            onAIUISpeakListener.onCompleted(0);
        }
        stopRecord();
        if (mAIUIAgent != null) {
            mAIUIAgent.destroy();
        }
        mAIUIAgent = null;

        initSDK(mContext);
    }

    /**
     * 监听到AIUI 声卡ATTACHED广播时调用
     */
    public void aiuiAttachedReInit() {
        stopRecord();
        // 初始化CAE
        initCaeEngine();
        // 初始化alsa录音
        initAlsa();
    }

    /**
     * 初始化AIUI
     */
    private void createAgent() {
        if (null == mAIUIAgent) {
            BaseLogger.info("AIUI 初始化AIUI agent");

            // AIUI SDK上报设备唯一标识，建议与CAE鉴权设备唯一标识一致，便于统计
            AIUISetting.setSystemInfo(AIUIConstant.KEY_SERIAL_NUM, CaeOperator.AUTH_SN);
            mAIUIAgent = AIUIAgent.createAgent(mContext, getAIUIParams(), mAIUIListener);
        }
        setVni();
        if (null == mAIUIAgent) {
            BaseLogger.error("AIUI初始化失败!");
        } else {
            BaseLogger.info("AIUI AIUI初始化成功!");
        }
    }

    /**
     * 读取AIUI配置
     */
    private String getAIUIParams() {
        String params = "";
        if (mContext == null) {
            return "";
        }
        AssetManager assetManager = mContext.getResources().getAssets();

        String fileName = AIUIType.AIUI_SOFT.equals(aiuiType) ? "cfg/aiui_phone.cfg" : "cfg/aiui_phone_r16.cfg";

        try {
            InputStream ins = assetManager.open(fileName);
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();
            params = new String(buffer);

            if (AIUIType.AIUI_R16.equals(aiuiType)) {
                JSONObject paramsJson = new JSONObject(params);

                mIsWakeupEnable = !"off".equals(paramsJson.optJSONObject("speech").optString("wakeup_mode"));
                if (mIsWakeupEnable) {
                    FucUtil.copyAssetFolder(mContext, "ivw", "/sdcard/AIUI/ivw");
                }
                params = paramsJson.toString();
            }

            if (TextUtils.isEmpty(Config.aiuiID)) {
                String aiuiFile = FileUtil.readFromFile("/sdcard/.robot_info/aiuikey.txt");
                if (!TextUtils.isEmpty(aiuiFile)) {
                    JSONObject jsonObject = new JSONObject(aiuiFile);
                    Config.aiuiID = jsonObject.getString("appId");
                    Config.aiuiKey = jsonObject.getString("appKey");
                }
            }

            if (!TextUtils.isEmpty(Config.aiuiID) && !TextUtils.isEmpty(Config.aiuiKey)) {
                JSONObject jsonObject = new JSONObject(params);
                JSONObject login = jsonObject.getJSONObject("login");
                login.put("appid", Config.aiuiID);
                login.put("key", Config.aiuiKey);

//                jsonObject.put("login", login);
                params = jsonObject.toString();
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        BaseLogger.info("AIUI AIUI params = {}", params);
        return params;
    }

    public void setVni() {
        String path = "/sdcard/vtn/cae/resources/config/vtn.ini";
        File file = new File(path);
        if (file.exists()) {
            String vntid = TextUtils.isEmpty(Config.aiuiID) ? "6a2398a5" : Config.aiuiID;
            String vniString = "[auth]\n" +
                    "appid=%s\n" +
                    "\n" +
                    "[cae]\n" +
                    "#是否开启降噪功能, 0为不开启，其他为开启，默认为开启\n" +
                    "cae_enable = 1\n" +
                    "\n" +
                    "# 固定在某个波束降噪，和代码的setRealbeam能力一致\n" +
                    "#fix_beam = 1\n" +
                    "\n" +
                    "#beam取值说明: -2 表示不输出任何音频, -1 第四路为识别音频，无第五路, \n" +
                    "#0，1，2时，第四路为指定波束的音频，第五路为vad音频\n" +
                    "beam = 1\n" +
                    "\n" +
                    "# 采样位深度说明  2：短整型16bit 、 4：整型32bit\n" +
                    "input_audio_unit = 2\n" +
                    "\n" +
                    "#output_audio_type 输出音频类型,0 iat; 1 iat_vad\n" +
                    "output_audio_type = 1\n" +
                    "\n" +
                    "[caeEngine]\n" +
                    "td_model_type = fsmn\n" +
                    "# 窄波束VAD音频平滑处理： 1启用，0不启用。不启用抑制效果更好,但识别率会下降\n" +
                    "vad_sqrt = 0\n" +
                    "\n" +
                    "\n" +
                    "# 新版本降噪算法加载的算法资源\n" +
                    "aes_model = /sdcard/vtn/cae/resources/models/mlp_aes_1024_tv_xTxT_denoise.bin\n" +
                    "aes_vcall_model= /sdcard/vtn/cae/resources/models/mlp_aes_01_vcall_20210510.bin\n" +
                    "partition_model= /sdcard/vtn/cae/resources/models/mlp_partition_4mic_5beam_512.bin\n" +
                    "partition_model_rec= /sdcard/vtn/cae/resources/models/mlp_lstm_sp_20201016.bin\n" +
                    "select_model= /sdcard/vtn/cae/resources/models/mlp_select_6to3_1024.bin\n" +
                    "td_model= /sdcard/vtn/cae/resources/models/mlp_td_fsmn_hxxj.bin\n" +
                    "\n" +
                    "#客户配置回声收敛文件路径\n" +
                    "aec_coef_path =  /sdcard/vtn/cae/resources/config/eccof.bin\n" +
                    "agc_max_evolop = 10000\n" +
                    "agc_target_gain = 5000\n" +
                    "\n" +
                    "[ivw]\n" +
                    "#是否开启唤醒功能, 0为不开启，其他为开启，默认为不开启\n" +
                    "ivw_enable = 1\n" +
                    "\n" +
                    "#唤醒资源\n" +
                    "res_path=/sdcard/vtn/cae/resources/ivw/res.bin\n" +
                    "\n";
            String vniString2 = "[auth]\n" +
                    "appid=%s\n" +
                    "\n" +
                    "[cae]\n" +
                    "#是否开启降噪功能, 0为不开启，其他为开启，默认为开启\n" +
                    "cae_enable = 1\n" +
                    "\n" +
                    "# 固定在某个波束降噪，和代码的setRealbeam能力一致\n" +
                    "#fix_beam = 1\n" +
                    "\n" +
                    "#beam取值说明: -2 表示不输出任何音频, -1 第四路为识别音频，无第五路, \n" +
                    "#0，1，2时，第四路为指定波束的音频，第五路为vad音频\n" +
                    "beam = 1\n" +
                    "\n" +
                    "# 采样位深度说明  2：短整型16bit 、 4：整型32bit\n" +
                    "input_audio_unit = 2\n" +
                    "\n" +
                    "#output_audio_type 输出音频类型,0 iat; 1 iat_vad\n" +
                    "output_audio_type = 1\n" +
                    "\n" +
                    "[caeEngine]\n" +
                    "td_model_type = fsmn\n" +
                    "# 窄波束VAD音频平滑处理： 1启用，0不启用。不启用抑制效果更好,但识别率会下降\n" +
                    "vad_sqrt = 0\n" +
                    "\n" +
                    "\n" +
                    "# 新版本降噪算法加载的算法资源\n" +
                    "aes_model = /sdcard/vtn/cae/resources/models/mlp_aes_1024_tv_xTxT_denoise.bin\n" +
                    "aes_vcall_model= /sdcard/vtn/cae/resources/models/mlp_aes_01_vcall_20210510.bin\n" +
                    "partition_model= /sdcard/vtn/cae/resources/models/mlp_partition_4mic_5beam_512.bin\n" +
                    "partition_model_rec= /sdcard/vtn/cae/resources/models/mlp_lstm_sp_20201016.bin\n" +
                    "select_model= /sdcard/vtn/cae/resources/models/mlp_select_6to3_1024.bin\n" +
                    "td_model= /sdcard/vtn/cae/resources/models/mlp_td_fsmn_hxxj.bin\n" +
                    "\n" +
                    "#客户配置回声收敛文件路径\n" +
                    "aec_coef_path =  /sdcard/vtn/cae/resources/config/eccof.bin\n" +
                    "agc_max_evolop = 10000\n" +
                    "agc_target_gain = 5000\n" +
                    "\n" +
                    "[ivw]\n" +
                    "#是否开启唤醒功能, 0为不开启，其他为开启，默认为不开启\n" +
                    "ivw_enable = 1\n" +
                    "\n" +
                    "#唤醒资源\n" +
                    "res_path=/sdcard/vtn/cae/resources/ivw2/res.bin\n" +
                    "\n";
            if (ConAIUI.isTimo) {
                BaseLogger.debug("vniString ->{}", vniString);
                FileUtil.writeTxtToFile(path, String.format(Locale.getDefault(), vniString, vntid));
            } else {
                BaseLogger.debug("vniString2 ->{}", vniString2);
                FileUtil.writeTxtToFile(path, String.format(Locale.getDefault(), vniString2, vntid));
            }
        }
    }

    private void stopRecord() {
        if (isRecording && mRecOperator != null) {
            mRecOperator.stopRecord();
            mCaeOperator.stopSaveAudio();
            isRecording = false;
            BaseLogger.warn("AIUI 停止录音");
        }
    }

    private void startRecord() {
        if (mAIUIAgent == null) {
            createAgent();
            return;
        }
        String strTip;
        if (!isRecording && mRecOperator != null) {
            if (isWriting) {
                BaseLogger.warn("AIUI 正在写音频测试中，等结束后再开启录音测试");
                BaseLogger.warn("AIUI ---------start_alsa_record---------");
                return;
            }
            ret = mRecOperator.startRecord();
            if (0 == ret) {
                strTip = "开启录音成功！";
                isRecording = true;
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                         BaseLogger.debug("AIUI 手动唤醒");
//                        mCaeOperator.setRealBeam(0);
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        AIUIMessage resetWakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
//                        mAIUIAgent.sendMessage(resetWakeupMsg);
//                    }
//                }).start();

            } else if (111111 == ret) {
                strTip = "AIUI AlsaRecorder is null ...";
                BaseLogger.warn(strTip);
                BaseLogger.warn("AIUI ---------start_alsa_record---------");

            } else {
                strTip = "开启录音失败，请查看/dev/snd/下的设备节点是否有777权限!Android 8.0 以上需要暂时使用setenforce 0 命令关闭Selinux权限！";
                BaseLogger.error(strTip);
                destroyRecord();
            }
        } else {
            BaseLogger.warn("AIUI 已经开启，无需重复开启");
            AIUIMessage resetWakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            if (mAIUIAgent != null) {
                mAIUIAgent.sendMessage(resetWakeupMsg);
            }
        }
    }


    private void destroyRecord() {
        if (null != mRecOperator && null != mCaeOperator) {
            mRecOperator.stopRecord();
            mCaeOperator.stopSaveAudio();
        } else {
            BaseLogger.warn("AIUI distoryCaeEngine is Done!");
        }
    }


    public void releaseSpeechRecognizer() {
    }

    public void startAudioRecognize() {
        if (AIUIType.AIUI_SOFT.equals(aiuiType)) {
            startRecord();
        } else {
            if (mAIUIAgent == null) {
                BaseLogger.error("AIUI r16 startAudioRecognize() called  mAIUIAgent  isNull");
                return;
            }

            if (!mIsWakeupEnable) {
                AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
                mAIUIAgent.sendMessage(wakeupMsg);
            }

            // 打开AIUI内部录音机，开始录音。若要使用上传的个性化资源增强识别效果，则在参数中添加pers_param设置
            // 个性化资源使用方法可参见http://doc.xfyun.cn/aiui_mobile/的用户个性化章节
            // 在输入参数中设置tag，则对应结果中也将携带该tag，可用于关联输入输出
//            String params = "sample_rate=16000,data_type=audio,pers_param={\"uid\":\"\"},tag=audio-tag";
//            AIUIMessage startRecord = new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, params, null);
//
//            if (mAIUIAgent != null) {
//                mAIUIAgent.sendMessage(startRecord);
//            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
                    if (mAIUIAgent != null) {
                        mAIUIAgent.sendMessage(wakeupMsg);
                    }
                }
            }, 1000);

        }
    }

    private void stopAIUIRecognize() {
        if (mAIUIAgent != null) {
            AIUIMessage resetWakeupMsg = new AIUIMessage(AIUIConstant.CMD_RESET_WAKEUP, 0, 0, null, null);
            mAIUIAgent.sendMessage(resetWakeupMsg);
        }
    }

    public void cancelAudioRecognize() {
        stopAIUIRecognize();
    }

    public void closeSpeechService() {
        stopRecord();
        stopAiUiRecord();
        if (mAIUIAgent != null) {
            mAIUIAgent.destroy();
        }
        mAIUIAgent = null;
    }

    public void stopAiUiRecord() {
        if (mAIUIAgent == null) {
            return;
        }
        // 停止录音
        AIUIMessage stopRecord;
        if (AIUIType.AIUI_R16.equals(aiuiType)) {
            String params = "sample_rate=16000,data_type=audio";
            stopRecord = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, params, null);
        } else {
            stopRecord = new AIUIMessage(AIUIConstant.CMD_RESET_WAKEUP, 0, 0, null, null);
        }
        mAIUIAgent.sendMessage(stopRecord);
    }

    public String replaceHotWord(String text) {
        if (replaceHotWordsList.size() >= 1) {
            if (TextUtils.isEmpty(text)) return text;
            String punctuation = text.substring(text.length() - 1);
            String content = text.substring(0, text.length() - 1);
            for (int i = 0; i < replaceHotWordsList.size(); i++) {
                List<String> subList = replaceHotWordsList.get(i).getReplaceList();
                for (int j = 0; j < subList.size(); j++) {
                    String replaceWord = subList.get(j);
                    if (content.contains(replaceWord)) {
                        return content.replace(replaceWord, replaceHotWordsList.get(i).getHotWordsName()) + punctuation;
                    }
                }
            }
        }
        return text;
    }


    /**
     * AIUI 回调信息处理
     */
    private final AIUIListener mAIUIListener = new AIUIListener() {
        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
                    TaskScheduler.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            startAudioRecognize();
                        }
                    });
                    BaseLogger.warn("AIUI 已连接服务器");
                    break;

                case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                    BaseLogger.error("AIUI 与服务器断开连接");
                    break;

                case AIUIConstant.EVENT_WAKEUP:
                    BaseLogger.debug("AIUI 进入识别状态" + event.info);
                    break;
                case AIUIConstant.EVENT_RESULT:
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);
                        String sub = params.optString("sub");

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            byte[] dataBuffer = event.data.getByteArray(cnt_id);

                            if (dataBuffer == null) {
                                return;
                            }
                            // 获取该路会话的id，将其提供给支持人员，有助于问题排查
                            // 也可以从Json结果中看到
                            String sid = event.data.getString("sid");
                            String tag = event.data.getString("tag");

//                            showTip("tag=" + tag);
//                            BaseLogger.debug("sub ->{}", sub);
                            if ("nlp".equals(sub) || "iat".equals(sub) || "asr".equals(sub)) {
                                String cntStr = new String(dataBuffer, StandardCharsets.UTF_8);

                                // 获取从数据发送完到获取结果的耗时，单位：ms
                                // 也可以通过键名"bos_rslt"获取从开始发送数据到获取结果的耗时
                                long eosRsltTime = event.data.getLong("eos_rslt", -1);
//                                mTimeSpentText.setText(eosRsltTime + "ms");

                                if (TextUtils.isEmpty(cntStr)) {
                                    return;
                                }

                                JSONObject cntJson = new JSONObject(cntStr);

                                if ("nlp".equals(sub)) {
                                    // 解析得到语义结果
                                    String resultStr = cntJson.optString("intent");
//                                    Log.i( TAG, resultStr );
                                    BaseLogger.debug("AIUI SPEECH_LAST_AIUI_RESULT_NTF = {}", resultStr);
                                    if (TextUtils.isEmpty(resultStr)) {
                                        aiuiResultNtfBean.setResult("");
                                        msgCollentListener.collentMessage(new Gson().toJson(aiuiResultNtfBean));
                                    } else {
                                        AiuiIntentConvert.convertResult(resultStr, lastPushString, msgCollentListener);
                                    }
                                } else if ("iat".equals(sub)) {
                                    // 听写pgs结果更新
                                    CosLogger.debug("AIUI iat = {}", cntJson);
                                    aiuiResultNtfBean.setResult("");
                                    msgCollentListener.collentMessage(new Gson().toJson(aiuiResultNtfBean));
                                    updateIATPGS(cntJson);
                                }
//                                mNlpText.append( "\n" );
                            } else if ("tts".equals(sub)) {
                                boolean isCancel = "1".equals(content.getString("cancel"));  //合成过程中是否被取消
                                if (isCancel) {
                                    BaseLogger.warn("TTS canceled");
                                }
//                                mTtsBufferProgress = event.data.getInt("percent");
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        CosLogger.debug("AIUI iat = {}", e.getLocalizedMessage());
//                        mNlpText.append( "\n" );
//                        mNlpText.append( e.getLocalizedMessage());
                    }
                    break;

                case AIUIConstant.EVENT_ERROR:
                    BaseLogger.error("AIUI 错误: " + event.arg1 + "\n" + event.info);
                    // 如果连续的5次有这 ERROR_AUDIO_RECORD， 就上报
                    // 其他的错误直接上报
                    int arg1 = event.arg1;
                    if (arg1 == AIUIErrorCode.ERROR_AUDIO_RECORD) {
//                        errorAudioRecordCount++;
                        // 累积5次就上报，如果有识别结果，就清零
//                        if (errorAudioRecordCount == ERROR_20006_MAX_COUNT) {
                        if (msgCollentListener != null) {
                            msgCollentListener.collentMessage(JsonFormatUtil.SplicingSimpleJson(NTFConstants.SPEECH_ISR_ERROR_NTF,
                                    "desc", event.info, event.arg1));
                        }
//                            errorAudioRecordCount = 0;
//                        }
                    } else {
                        if (msgCollentListener != null) {
                            msgCollentListener.collentMessage(JsonFormatUtil.SplicingSimpleJson(NTFConstants.SPEECH_ISR_ERROR_NTF,
                                    "desc", event.info, event.arg1));
                        }
                    }
                    if (arg1 == AIUIErrorCode.MSP_ERROR_NO_RESPONSE_DATA) {
                        BaseLogger.debug("AIUI  播放出错 code=10120网络异常 播放完成, uuid = {}", uuid);
                        if (onAIUISpeakListener != null) {
                            onAIUISpeakListener.onCompleted(arg1);
                        }
                        isSpeaking.set(false);
                    }
                    if (arg1 == AIUIErrorCode.ERROR_PLAY_MEDIA) {
                        BaseLogger.debug("AIUI  播放出错 code=20011 播放错误 播放焦点抢占失败，或者是声卡录音限制了AudioTrack 调用等, uuid = {}", uuid);
                        if (onAIUISpeakListener != null) {
                            onAIUISpeakListener.onCompleted(arg1);
                        }
                        isSpeaking.set(false);
                    }
                    break;
                case AIUIConstant.EVENT_VAD:
                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        BaseLogger.debug("AIUI 找到vad_bos");
                    } else if (AIUIConstant.VAD_BOS_TIMEOUT == event.arg1) {
                        BaseLogger.debug("AIUI 前端点超时");
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        if (!TextUtils.isEmpty(lastPushString)) {
                            handler.postDelayed(postNotLastButVadEosString, 10000);
                        } else {
                            BaseLogger.debug("AIUI 找到vad_eos");
                        }
                    } else {
//                        CosLogger.debug("AIUI event_vad" + event.arg2);
                    }
                    break;
                case AIUIConstant.EVENT_SLEEP:
                    if (msgCollentListener != null) {
                        msgCollentListener.collentMessage(JsonFormatUtil.SplicingSimpleJson(RSPConstants.SPEECH_ISR_STOP_RSP));
                    }
                    BaseLogger.debug("AIUI 设备进入休眠 " + (event.arg1 == 0 ? "自动休眠" : "发送CMD_RESET_WAKEUP"));
                    break;
                case AIUIConstant.EVENT_START_RECORD:
                    BaseLogger.debug("AIUI 已开始录音");
                    break;

                case AIUIConstant.EVENT_STOP_RECORD:
                    BaseLogger.debug("AIUI 已停止录音");
                    break;

                case AIUIConstant.EVENT_STATE:    // 状态事件
                    mAIUIState = event.arg1;
                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        BaseLogger.debug("AIUI event state is STATE_IDLE, 闲置状态，AIUI未开启");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        BaseLogger.debug("AIUI event state is STATE_READY , AIUI已就绪，等待唤醒");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        BaseLogger.debug("AIUI event state is STATE_WORKING, AIUI工作中，可进行交互");
                    }
                    break;

                case AIUIConstant.EVENT_TTS: {
                    dealWithTTSEvent(event.arg1);
                }
                break;
                default:
                    break;
            }
        }
    };

    private void dealWithTTSEvent(int arg1) {
        switch (arg1) {
            case AIUIConstant.TTS_SPEAK_BEGIN:
//                CosLogger.debug("AIUI 开始播放");
//                isSpeaking = true;
//                if (onAIUISpeakListener != null) {
//                    onAIUISpeakListener.onSpeakBegin();
//                }
                BaseLogger.debug("AIUI 开始播放");
                isSpeaking.set(true);
                handler.removeCallbacks(reSpeakRun);
                handler.removeCallbacks(speakTimeOutRun);
                break;
            case AIUIConstant.TTS_SPEAK_PROGRESS:
//                            CosLogger.debug("AIUI 缓冲进度为" + "mTtsBufferProgress" +
//                                    ", 播放进度为" + event.data.getInt("percent"));     // 播放进度
                break;
            case AIUIConstant.TTS_SPEAK_PAUSED:
                BaseLogger.debug("AIUI 暂停播放");
                if (!userPause) {
                    if (onAIUISpeakListener != null) {
                        onAIUISpeakListener.onCompleted(-1);
                    }
                    isSpeaking.set(false);
                    BaseLogger.warn("AIUI 暂停播放, 非用户暂停，直接设置完成播放。");
                    userPause = false;
                }
                break;
            case AIUIConstant.TTS_SPEAK_RESUMED:
                BaseLogger.debug("AIUI 恢复播放");
                if (onAIUISpeakListener != null) {
                    onAIUISpeakListener.onSpeakBegin();
                }
                isSpeaking.set(true);
                break;
            case AIUIConstant.TTS_SPEAK_COMPLETED:
                isSpeaking.set(false);
                if (onAIUISpeakListener != null) {
                    onAIUISpeakListener.onCompleted(0);
                }
                userPause = false;
                BaseLogger.debug("AIUI 播放完成, uuid = {}", uuid);
                break;
            default:
                break;
        }

    }

    // 处理听写PGS的队列
    private final String[] mIATPGSStack = new String[100];
    private String lastPushString = "";

    private void updateIATPGS(JSONObject cntJson) throws JSONException {
        JSONObject text = cntJson.optJSONObject("text");
        // 解析拼接此次听写结果
        StringBuilder iatText = new StringBuilder();
        JSONArray words = text.optJSONArray("ws");
        boolean lastResult = text.optBoolean("ls");
        for (int index = 0; index < words.length(); index++) {
            JSONArray charWord = words.optJSONObject(index).optJSONArray("cw");
            for (int cIndex = 0; cIndex < charWord.length(); cIndex++) {
                iatText.append(charWord.optJSONObject(cIndex).opt("w"));
            }
        }


        String voiceIAT = "";
        String pgsMode = text.optString("pgs");
        //非PGS模式结果
        if (TextUtils.isEmpty(pgsMode)) {
            BaseLogger.error("AIUI pgsMode is  null ”");
        } else {
            int serialNumber = text.optInt("sn");
            mIATPGSStack[serialNumber] = iatText.toString();
            //pgs结果两种模式rpl和apd模式（替换和追加模式）
            if ("rpl".equals(pgsMode)) {
                //根据replace指定的range，清空stack中对应位置值
                JSONArray replaceRange = text.optJSONArray("rg");
                int start = replaceRange.getInt(0);
                int end = replaceRange.getInt(1);

                for (int index = start; index <= end; index++) {
                    mIATPGSStack[index] = null;
                }
            }

            StringBuilder PGSResult = new StringBuilder();
            //汇总stack经过操作后的剩余的有效结果信息
            for (int index = 0; index < mIATPGSStack.length; index++) {
                if (TextUtils.isEmpty(mIATPGSStack[index])) continue;

//                if (!TextUtils.isEmpty(PGSResult.toString())) PGSResult.append("\n");
                PGSResult.append(mIATPGSStack[index]);
                //如果是最后一条听写结果，则清空stack便于下次使用
                if (lastResult) {
                    mIATPGSStack[index] = null;
                }
            }
            voiceIAT = PGSResult.toString();

            if (ppListener != null && !TextUtils.isEmpty(voiceIAT)) {
                if (lastResult) {
                    BaseLogger.info("AIUI voiceIAT = [{}], lastResult = {}", voiceIAT, lastResult);
                    lastPushString = replaceHotWord(voiceIAT);
                    if (ignoreSingleWordHit(lastPushString, officiallyWordList)) {
                        ppListener.pushSpeechRecgData(lastPushString, true);
                        lastPushString = "";
                        handler.removeCallbacks(postNotLastButVadEosString);
                        if (TextUtils.isEmpty(voiceIAT)) {
                            if (msgCollentListener != null) {
                                msgCollentListener.collentMessage(JsonFormatUtil.SplicingSimpleJson(NTFConstants.SPEECH_ISR_ERROR_NTF,
                                        "desc", "内容为空", -3));
                            }
                        }
                    } else {
                        lastPushString = "";
                    }
                } else {
                    if (voiceIAT.length() > 2) {
                        lastPushString = replaceHotWord(voiceIAT);
                        try {
                            BaseLogger.debug("AIUI lastPushString = {}", lastPushString);
                            ppListener.pushSpeechRecgData(lastPushString, false);
                        } catch (Exception e) {
                            BaseLogger.debug("AIUI lastPushString  error  = {}", e);
                        }
                    } else {
                        BaseLogger.debug("AIUI voiceIAT = {}, voiceIAT.length() = {}", voiceIAT, voiceIAT.length());
                    }
                }

                // 有结果的化，这个count就清零
                errorAudioRecordCount = 0;
            } else {
                if (ppListener == null) {
                    BaseLogger.debug("AIUI ppListener  is null, sendBroadcast");
                    Intent intent = new Intent("com.csjbot.asrtest");
                    if (lastResult) {
                        BaseLogger.info("AIUI voiceIAT = [{}], lastResult = {}", voiceIAT, lastResult);
                        lastPushString = replaceHotWord(voiceIAT);
                        if (ignoreSingleWordHit(lastPushString, officiallyWordList)) {
//                            ppListener.pushSpeechRecgData(lastPushString, true);
                            intent.putExtra("asr", lastPushString);
                            intent.putExtra("isLast", true);
                            mContext.sendBroadcast(intent);
                            lastPushString = "";
                            handler.removeCallbacks(postNotLastButVadEosString);
                            if (TextUtils.isEmpty(voiceIAT)) {

                                if (msgCollentListener != null) {
                                    msgCollentListener.collentMessage(JsonFormatUtil.SplicingSimpleJson(NTFConstants.SPEECH_ISR_ERROR_NTF,
                                            "desc", "内容为空", -3));
                                }
                            }
                        } else {
                            lastPushString = "";
                        }
                    } else {
                        if (voiceIAT.length() > 2) {
                            lastPushString = replaceHotWord(voiceIAT);
//                            ppListener.pushSpeechRecgData(lastPushString, false);
                            intent.putExtra("asr", lastPushString);
                            intent.putExtra("isLast", false);
                            mContext.sendBroadcast(intent);
                        } else {
                            BaseLogger.debug("AIUI voiceIAT = {}, voiceIAT.length() = {}", voiceIAT, voiceIAT.length());
                        }
                    }

                    // 有结果的化，这个count就清零
                    errorAudioRecordCount = 0;

                } else {
                    BaseLogger.debug("AIUI voiceIAT is null");
                }
            }
        }
    }


    //单字识别中只保留如下字
    private final String[] enableWords = {
            "好", "是", "不", "否", "在", "要", "去", "行", "否", "有", "无", "请"
    };

    private final String[] punctuatio = {
            "。", "，", ",", ".", "!", "！"
    };

    private final List<String> officiallyWordList = new ArrayList<>();
    private final List<String> punctuatioWordList = new ArrayList<>();

    private void initSingleWords() {
        officiallyWordList.clear();
        punctuatioWordList.clear();

        officiallyWordList.addAll(Arrays.asList(enableWords));
        punctuatioWordList.addAll(Arrays.asList(punctuatio));
    }

    //是否命中单字
    private boolean ignoreSingleWordHit(String targetWord, List<String> compareList) {
        if (targetWord.length() == 2 && (targetWord.endsWith("。")
                || targetWord.endsWith("！")
                || targetWord.endsWith("？"))) {
            targetWord = targetWord.substring(0, targetWord.length() - 1);
            for (int i = 0; i < compareList.size(); i++) {
                if (TextUtils.equals(targetWord, compareList.get(i))) {
                    return true;
                }
            }
            return false;
        } else if (targetWord.length() == 1) {
            for (int i = 0; i < punctuatioWordList.size(); i++) {
                if (TextUtils.equals(targetWord, punctuatioWordList.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    private final Runnable postNotLastButVadEosString = new Runnable() {
        @Override
        public void run() {
            ppListener.pushSpeechRecgData(lastPushString, true);
            BaseLogger.warn("AIUI 找到vad_eos，但是有未结束的，直接结束");
            lastPushString = "";
        }
    };

    public String getRecgStatus() {
        boolean open = mAIUIAgent != null && (isRecording && mRecOperator != null);
        return open ? "语音识别开启(is opened)" : "语音识别关闭(is closed)";
    }

    public void replaceHotWords(List<HotWordsReplaceBean> list) {
//         BaseLogger.info("aiui 热词替换= " + list);
        replaceHotWordsList.clear();
        replaceHotWordsList.addAll(list);
    }

    public void setMsgCollentListener(MsgCollentListener listener) {
        this.msgCollentListener = listener;
    }

    private void initTTSParams() {
        params = new StringBuffer();
        // 发音人，发音人列表：https://aiui-doc.xf-yun.com/project-1/doc-93/
        params.append("vcn=x4_lingxiaoying_em_v2");
        // 语速，取值范围[0,100]
        params.append(",speed=40");
        // 音调，取值范围[0,100]
        params.append(",pitch=50");
        // 音量，取值范围[0,100]
        params.append(",volume=").append(ttsVolume);
    }

    private OnAIUISpeakListener onAIUISpeakListener;
    private byte[] ttsData;
    private final AtomicBoolean isSpeaking = new AtomicBoolean(false);
    private String uuid;
    private StringBuffer params = new StringBuffer();
    private int ttsVolume = 100;
    private int ttsSpeed = 40;
    private boolean userPause = false;
    private String speakerName = "x4_lingxiaoying_em_v2";
    private String tempText;
    private OnSpeakProgressListener onSpeakProgressListener;

    public void setVolume(float volume) {
        ttsVolume = (int) (volume);
        params = new StringBuffer();
        // 发音人，发音人列表：https://aiui-doc.xf-yun.com/project-1/doc-93/
        params.append("vcn=").append(speakerName);
//        params.append("vcn=x2_xiaojuan");
        // 语速，取值范围[0,100]
        params.append(",speed=").append(ttsSpeed);
        // 音调，取值范围[0,100]
//        params.append(",pitch=50");
        // 音量，取值范围[0,100]
        Csjlogger.debug("setVolume -> "+ttsVolume);
        params.append(",volume=").append(ttsVolume);

    }

    private final Runnable reSpeakRun = new Runnable() {
        @Override
        public void run() {
            if (!TextUtils.isEmpty(tempText) && !isSpeaking.get()) {
                initTTSParams();
                // 转为二进制数据
                ttsData = tempText.getBytes(StandardCharsets.UTF_8);
                AIUIMessage startTTS = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0, params.toString(), ttsData);
                if (mAIUIAgent != null) {
                    mAIUIAgent.sendMessage(startTTS);
                    if (onAIUISpeakListener != null) {
                        BaseLogger.debug("AIUI  tempText = text ->{}", tempText);
                        onAIUISpeakListener.onSpeakBegin();
                    }
                }
            }
        }
    };

    public void startSpeak(String text, OnAIUISpeakListener listener) {
        if (mAIUIAgent == null) {
            createAgent();
            return;
        }
        initTTSParams();
        // 转为二进制数据
        ttsData = text.getBytes(StandardCharsets.UTF_8);
        uuid = UUID.randomUUID().toString();
        if (listener != null) {
            onAIUISpeakListener = listener;
        }
        tempText = text;
        BaseLogger.info("startSpeak = {}. uuid = {}", text, uuid);
        handler.removeCallbacks(reSpeakRun);
        handler.postDelayed(reSpeakRun, 2000);
        handler.removeCallbacks(speakTimeOutRun);
        handler.postDelayed(speakTimeOutRun, 1000 * 5);

        AIUIMessage startTTS = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0, params.toString(), ttsData);
        mAIUIAgent.sendMessage(startTTS);

        if (onAIUISpeakListener != null) {
            onAIUISpeakListener.onSpeakBegin();
        }
    }

    public void setOnSpeakProgressListener(OnSpeakProgressListener listener) {
        onSpeakProgressListener = listener;
    }

    public void stopSpeaking() {
        if (mAIUIAgent == null) {
            createAgent();
            return;
        }
        isSpeaking.set(false);
        BaseLogger.debug("AIUI stopSpeaking");
        AIUIMessage cancelTTS = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.CANCEL, 0, params.toString(), ttsData);
        mAIUIAgent.sendMessage(cancelTTS);
    }

    public void pauseSpeaking() {
        if (mAIUIAgent == null) {
            createAgent();
            return;
        }
        isSpeaking.set(false);
        BaseLogger.debug("AIUI pauseSpeaking");

        handler.removeCallbacks(speakTimeOutRun);
        userPause = true;
        AIUIMessage pauseTTS = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.PAUSE, 0, params.toString(), ttsData);
        mAIUIAgent.sendMessage(pauseTTS);
    }

    public void resumeSpeaking(OnAIUISpeakListener listener) {
        if (mAIUIAgent == null) {
            createAgent();
            return;
        }
        if (listener != null) {
            onAIUISpeakListener = listener;
        }
        BaseLogger.info("resumeSpeaking = {}. uuid = {}", new String(ttsData), uuid);
//        isSpeaking.set(true);
        AIUIMessage resumeTTS = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.RESUME, 0, params.toString(), ttsData);
        mAIUIAgent.sendMessage(resumeTTS);
    }

    public boolean isSpeaking() {
        return isSpeaking.get();
    }

    public boolean startSaveAudio() {
        if (AIUIType.AIUI_SOFT.equals(aiuiType)) {
            if (mCaeOperator != null) {
                if (mCaeOperator.isAudioSaving()) {
                    return false;
                }
                mCaeOperator.startSaveAudio();
                return true;
            }
        }

        return false;
    }

    public boolean stopSaveAudio() {
        if (AIUIType.AIUI_SOFT.equals(aiuiType)) {
            if (mCaeOperator != null) {
                if (mCaeOperator.isAudioSaving()) {
                    mCaeOperator.stopSaveAudio();
                    return true;
                }
            }
        }

        return false;
    }


    /**************************   AIUI 软核    **************************/

    /**
     * Alsa录音回调消息处理
     * 如果声卡采集音频直接满足CAE格式要求，就需要做音频格式转换
     * CAE音频格式要求：例线性4麦  需要 6通道 16k 16bit|32bit
     */
    private final RecordListener onRecordListener = new RecordListener() {
        @Override
        public void onPcmData(byte[] bytes) {
            // 保存原始录音数据
            mCaeOperator.saveAduio(bytes, CaeOperator.mAlsaRawFileUtil);
            // 录音数据转换：usb声卡 线性2mic
//            byte[] data = RecordAudioUtil.adapeter2Mic(bytes);
            // 录音数据转换：usb声卡 线性4mic
//            byte[] data = RecordAudioUtil.adapeter4Mic(bytes);
            // 录音数据转换：usb声卡 线性/环形6mic
//            byte[] data = RecordAudioUtil.adapeter6Mic(bytes);
            // 录音数据转换：usb声卡 线性/环形4mic
            byte[] data = RecordAudioUtil.adapeter4Mic2(bytes);
            // 保存转换后录音数据
            mCaeOperator.saveAduio(data, CaeOperator.mAlsaRecFileUtil);
            // 写入CAE引擎
            mCaeOperator.writeAudioTest(data);
        }
    };


    /**
     * 初始化CAE
     */
    private void initCaeEngine() {

        if (AIUIType.AIUI_SOFT.equals(aiuiType)) {
            mCaeOperator = new CaeOperator();
        } else {
            mCaeOperator = new CsjRecorder();
        }
//        mCaeOperator = new CaeOperator();
        ret = mCaeOperator.initCAE(onCaeOperatorlistener);

        if (AIUIType.AIUI_SOFT.equals(aiuiType)) {
            if (ret == 0) {
                initAlsa();
                BaseLogger.info("AIUI CAE初始化成功!");
            } else {
                BaseLogger.error("CAE初始化失败,错误信息为：" + ret);
            }
        }
    }

    /**
     * 初始化ALSA
     */
    private void initAlsa() {
        mRecOperator = new RecOperator();
        mRecOperator.initRec(mContext, onRecordListener);
    }

    OnAudioDataListener onAudioDataListener;

    public void setOnAudioDataListener(OnAudioDataListener listener) {
        onAudioDataListener = listener;

    }

    /**
     * CAE 回调消息处理
     */
    private final OnCaeOperatorlistener onCaeOperatorlistener = new OnCaeOperatorlistener() {
        @Override
        public void onAudio(byte[] audioData, int dataLen) {
            // CAE降噪后音频写入AIUI SDK进行语音交互
            String params = "data_type=audio,sample_rate=16000";
            AIUIMessage msg = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, audioData);
            if (mAIUIAgent != null) {
                mAIUIAgent.sendMessage(msg);
            }
             // 测试用
            if (audioPlayer != null) {
                audioPlayer.play(audioData);
            }
            if (onAudioDataListener != null) {
                onAudioDataListener.onAudioData(audioData);
            }
            if (dataSender != null) {
                try {
                    // 给外部应用使用
                    dataSender.sendAlsaData(audioData);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if (BuildConfig.DEBUG) {
//                BaseLogger.debug("AIUI onAudio ==> dataLen = {}", dataLen);
            }
        }

        @Override
        public void onWakeup(int angle, int beam) {
            BaseLogger.debug("AIUI ======[wakeup]====== 唤醒成功, angle:" + angle + " beam:" + "AIUI listener = " + (msgCollentListener != null));
            String ntf = String.format(Locale.getDefault(), "{\"msg_id\":\"%s\",\"wakeType\":0,\"angle\":%d,\"error_code\":0}",
                    NTFConstants.SPEECH_ISR_WAKEUP_NTF, 0);
//             BaseLogger.debug("AIUI listener = {}", msgCollentListener);
            if (msgCollentListener != null) {
                msgCollentListener.collentMessage(ntf);
            }

//            setText("唤醒成功,angle:" + a + " beam:" + b);
//            setText("---------WAKEUP_CAE---------");
            // CAE SDK触发唤醒后给AIUI SDK发送手动唤醒事件：让AIUI SDK置于工作状态
            AIUIMessage resetWakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            if (mAIUIAgent != null) {
                mAIUIAgent.sendMessage(resetWakeupMsg);
            }
        }
    };

    /**
     * 保存音频
     * 在aiui.cfg配置中 将 save_datalog  取值设置为1
     * 在AIUI初始化后调用保存音频事件，会在设备sdcard/AIUI/audio 目录下记录音频信息
     */
    public void CMD_START_SAVE() {
        mAIUIAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_START_SAVE, 0, 0, "data_type=raw_audio", null));
    }

    /**
     * 取消保存
     */
    public void CMD_STOP_SAVE() {
        mAIUIAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_STOP_SAVE, 0, 0, "data_type=raw_audio", null));
    }

    public boolean startAudioPlayBack() {
        if (audioPlayer == null) {
            audioPlayer = new AudioPlayer();
            return true;
        }

        return false;
    }

    public boolean stopAudioPlayBack() {
        if (audioPlayer != null) {
            audioPlayer.stop();
        }
        audioPlayer = null;
        return true;
    }

    public void setSpeakerName(String name) {
        speakerName = name;
        params = new StringBuffer();
        // 发音人，发音人列表：https://aiui-doc.xf-yun.com/project-1/doc-93/
        params.append("vcn=").append(speakerName);
//        params.append("vcn=x2_xiaojuan");
        // 语速，取值范围[0,100]
        params.append(",speed=").append(ttsSpeed);
        // 音调，取值范围[0,100]
//        params.append(",pitch=50");
        // 音量，取值范围[0,100]
        params.append(",volume=").append(ttsVolume);
    }

    public void setSpeed(int speed) {
        ttsSpeed = speed;
        params = new StringBuffer();
        // 发音人，发音人列表：https://aiui-doc.xf-yun.com/project-1/doc-93/
        params.append("vcn=").append(speakerName);
//        params.append("vcn=x2_xiaojuan");
        // 语速，取值范围[0,100]
        params.append(",speed=").append(ttsSpeed);
        // 音调，取值范围[0,100]
//        params.append(",pitch=50");
        // 音量，取值范围[0,100]
        params.append(",volume=").append(ttsVolume);
    }


//    public boolean startSendCaeData(){
//    }

    public enum AIUIType {
        AIUI_DATA_ONLY,
        AIUI_SOFT,
        AIUI_R16
    }

    public int getSpeakVolume() {
        File file = new File(CONFIG_PATH);
        if (file.exists()) {
            String s = FileUtil.readFromFile(CONFIG_PATH);
            try {
                JSONObject jsonObject = new JSONObject(s);
                return jsonObject.getInt("speakVolume") * 10;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 100;
    }


    public void setDataSender(IAlsaRawDataSender sender) {
        dataSender = sender;
    }

}
