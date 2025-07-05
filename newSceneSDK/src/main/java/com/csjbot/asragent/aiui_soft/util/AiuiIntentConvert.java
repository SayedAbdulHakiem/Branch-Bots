package com.csjbot.asragent.aiui_soft.util;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2024/11/22
 * @description
 */

import android.text.TextUtils;

import com.csjbot.asragent.aiui_soft.listener.MsgCollentListener;
import com.csjbot.coshandler.global.NTFConstants;
import com.csjbot.coshandler.log.BaseLogger;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AiuiIntentConvert {
    private static final List<String> allowServiceId = new ArrayList<>();

    static {
//        allowServiceId.add("");
//        allowServiceId.add("AIUI.audioBook");//有声书媒资类
//        allowServiceId.add("AIUI.Bible");//圣经媒资类
//        allowServiceId.add("AIUI.chCultivation");//儿童兴趣培养媒资类
//        allowServiceId.add("AIUI.chDevelopment");//儿童学堂媒资类
//        allowServiceId.add("AIUI.chLiterature");//国学
//        allowServiceId.add("AIUI.chSong");//儿歌媒资类
//        allowServiceId.add("AIUI.meditationTime");//冥想时刻媒资类
//        allowServiceId.add("AIUI.ocularGym");//眼保健操媒资类
//        allowServiceId.add("AIUI.sleepWell");//好好睡觉媒资类
//        allowServiceId.add("AIUI.smarter");//越听越聪明媒资类
//        allowServiceId.add("AIUI.whiteNoise");//白噪音媒资类
//        allowServiceId.add("animalCries");//动物叫声媒资类
//        allowServiceId.add("crossTalk");//相声小品媒资类
//        allowServiceId.add("drama");//戏曲媒资类
//        allowServiceId.add("englishEveryday");//英语每日一句媒资类
//        allowServiceId.add("health");//健康讲座媒资类
//        allowServiceId.add("history");//历史媒资类
//        allowServiceId.add("KLLI3.studyPinYin");//我想学拼音媒资类
//        allowServiceId.add("LEIQIAO.funnyPassage");//搞笑段子媒资类
//        allowServiceId.add("LEIQIAO.openClass");//公开课媒资类
//        allowServiceId.add("LEIQIAO.speech");//名人演讲媒资类
//        allowServiceId.add("musicX");//音乐媒资类
//        allowServiceId.add("musicX_dialect");//音乐方言版媒资类
//        allowServiceId.add("news");//新闻媒资类
//        allowServiceId.add("novel");//有声小说媒资类
//        allowServiceId.add("radio");//广播电台媒资类
//        allowServiceId.add("story");//故事媒资类
//        allowServiceId.add("storyTelling");//评书媒资类
//        allowServiceId.add("KLLI3.FamilyNames");//百家姓播报类
//        allowServiceId.add("baike");//百科播报类
//        allowServiceId.add("AIUI.2bd672cefd");//猜数字播报类
//        allowServiceId.add("lottery");//彩票播报类
//        allowServiceId.add("cookbook");//菜谱播报类
//        allowServiceId.add("AIUI.e09af9377o");//成语接龙播报类
//        allowServiceId.add("wordsDictionary");//词典播报类
//        allowServiceId.add("AIUI.unitConversion");//单位换算播报类
//        allowServiceId.add("translation");//翻译播报类
//        allowServiceId.add("college");//高校查询播报类
//        allowServiceId.add("AIUI.collegeScore");//高校分数线播报类
//        allowServiceId.add("KLLI3.powerScaler");//功率换算播报类
//        allowServiceId.add("stock");//股票播报类
//        allowServiceId.add("LEIQIAO.cityOfPro");//国内城市查询播报类
//        allowServiceId.add("AIUI.forex");//汇率播报类
//        allowServiceId.add("AIUI.f65cf38453");//会说话的小鹦鹉播报类
//        allowServiceId.add("AIUI.calc");//计算器播报类
//        allowServiceId.add("LEIQIAO.relationShip");//家族关系神器播报类
//        allowServiceId.add("holiday");//假期安排播报类
//        allowServiceId.add("AIUI.cd756aff0p");//剪刀石头布播报类
//        allowServiceId.add("petrolPrice");//今日油价播报类
//        allowServiceId.add("KLLI3.numberScaler");//进制转换播报类
//        allowServiceId.add("AIUI.famousQuotes");//经典名句播报类
//        allowServiceId.add("LEIQIAO.timesTable");//九九乘法表播报类
//        allowServiceId.add("AIUI.85beebdd4t");//口算挑战播报类
//        allowServiceId.add("garbageClassify");//垃圾分类播报类
//        allowServiceId.add("LEIQIAO.historyToday");//历史上的今天播报类
//        allowServiceId.add("AIUI.b1ed7474c9");//谜语 播报类
//        allowServiceId.add("AIUI.20aafd8b1r");//脑筋急转弯播报类
//        allowServiceId.add("AIUI.ac140b7894");//抛硬币播报类
//        allowServiceId.add("carNumber");//汽车尾号限行播报类
//        allowServiceId.add("chineseZodiac");//生肖运势播报类
//        allowServiceId.add("ZUOMX.queryCapital");//省会查询播报类
//        allowServiceId.add("poetry");//诗词播报类
//        allowServiceId.add("AIUI.179c5b26by");//诗词挑战播报类
//
//        allowServiceId.add("AIUI.WorldCup");//世界杯播报类
//        allowServiceId.add("KLLI3.captialInfo");//首都查询播报类
//        allowServiceId.add("LEIQIAO.BMI");//体重指数查询播报类
//
//        allowServiceId.add("AIUI.114c02b04p");//跳数字播报类
//        allowServiceId.add("calendar");//万年历播报类
//        allowServiceId.add("joke");//笑话播报类
//        allowServiceId.add("AIUI.virusSearch");//新冠疫情查询播报类
//        allowServiceId.add("constellation");//星座播报类
//        allowServiceId.add("dream");//周公解梦播报类
//        allowServiceId.add("EGO.healthKnowledge");//健康知识播报类
//        allowServiceId.add("EGO.foodsCalorie");//食物热量播报类
//        allowServiceId.add("botattrQA");//食物热量播报类

        allowServiceId.add("weather");//天气播报类
        allowServiceId.add("weather_dialect");//天气方言版播报类
        allowServiceId.add("datetimePro");//时间日期查询播报类
    }

    private static final String satisfyString = "{\"msg_id\":\"SPEECH_ISR_LAST_RESULT_NTF\",\"result\":{\"data\":{\"actionList\":[]," +
            "\"answer\":\"%s\",\"graphic\":\"{\\\"type\\\":\\\"1\\\",\\\"answer\\\":\\\"%s\\\"}\"," +
            "\"say\":\"%s\",\"type\":\"satisfy\"},\"error_code\":0,\"text\":\"%s\"}}";


    private static final String mediaString = " {\"msg_id\":\"SPEECH_ISR_LAST_RESULT_NTF\",\"result\":{\"data\":{\"actionList\":[]," +
            "\"answer\":\"%s\",\"graphic\":\"{\\\"type\\\":\\\"3\\\",\\\"answer\\\":\\\"%s\\\",\\\"" +
            "audioFile\\\":[{\\\"url\\\":\\\"%s\\\"," +
            "\\\"name\\\":\\\"叶炫清 - 九张机 (伴奏).mp3\\\"}]}\",\"say\":\"%s\",\"type\":\"satisfy\"},\"error_code\":0,\"text\":\"%s\"}}";

    private static boolean isAllowedService(String service) {
        return allowServiceId.contains(service);
    }

    public static final AiuiResultNtfBean aiuiResultNtfBean = new AiuiResultNtfBean();


    public static class AiuiResultNtfBean {
        /**
         * msgId
         */
        @SerializedName("msg_id")
        private String msgId = NTFConstants.SPEECH_LAST_AIUI_RESULT_NTF;
        /**
         * result
         */
        @SerializedName("result")
        private String result;
        /**
         * errorCode
         */
        @SerializedName("error_code")
        private Integer errorCode = 0;

        public String getMsgId() {
            return msgId;
        }

        public void setMsgId(String msgId) {
            this.msgId = msgId;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public Integer getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(Integer errorCode) {
            this.errorCode = errorCode;
        }
    }


    /*
     * if(rc == 0){
     *     if(isAllowedService){
     *          if(service_pkg = broadcast){
     *              处理成 broadcast，发送
     *          } else (service_pkg = media){
     *              处理成 media，发送
     *          } else {
     *              返回错误
     *          }
     *     } else {
     *          返回错误
     *     }
     * } else {
     *     返回错误
     * }
     */
    public static void convertResult(String resultStr, String asr, MsgCollentListener msgCollentListener) {
        try {
            JSONObject rootObj = new JSONObject(resultStr);
            /**
             * 0（成功）
             * 1（输入异常）
             * 2（系统内部异常）
             * 3（业务操作失败，没搜索到结果或信源异常）
             * 4（说法未命中技能）
             */
            String rc = rootObj.optString("rc");
            if (TextUtils.equals(rc, "0")) {
                String service = rootObj.optString("service");
                if (!isAllowedService(service)) {
                    BaseLogger.warn("service [{}] not allowed", service);
                    aiuiResultNtfBean.setResult("");
                    msgCollentListener.collentMessage(new Gson().toJson(aiuiResultNtfBean));
                    return;
                }
                String service_pkg = rootObj.optString("service_pkg");
                String answerText = rootObj.getJSONObject("answer").optString("text");
                if (!TextUtils.isEmpty(service_pkg)) {
                    if (TextUtils.equals(service_pkg, "broadcast")) {
                        String out = String.format(Locale.getDefault(), satisfyString, answerText, answerText, answerText, asr);
                        aiuiResultNtfBean.setResult(out);
                        msgCollentListener.collentMessage(new Gson().toJson(aiuiResultNtfBean));
                    } else if (TextUtils.equals(service_pkg, "media")) {
                        JSONObject data = rootObj.getJSONObject("data");
                        JSONArray resultArray = data.getJSONArray("result");
                        if (resultArray.length() > 0) {
                            JSONObject result0 = resultArray.getJSONObject(0);
                            String uni_url = result0.getString("uni_url");
                            answerText = result0.getString("name");
                            String out = String.format(Locale.getDefault(), mediaString, answerText, answerText, uni_url, answerText, asr);
                            aiuiResultNtfBean.setResult(out);
                        } else {
                            String out = String.format(Locale.getDefault(), satisfyString, answerText, answerText, answerText, asr);
                            aiuiResultNtfBean.setResult(out);
                        }
                        msgCollentListener.collentMessage(new Gson().toJson(aiuiResultNtfBean));
                    } else {
                        aiuiResultNtfBean.setResult("");
                        msgCollentListener.collentMessage(new Gson().toJson(aiuiResultNtfBean));
                    }
                } else {
                    String out = String.format(Locale.getDefault(), satisfyString, answerText, answerText, answerText, asr);
                    aiuiResultNtfBean.setResult(out);
                    msgCollentListener.collentMessage(new Gson().toJson(aiuiResultNtfBean));
                    BaseLogger.debug("SPEECH_ISR_LAST_RESULT_NTF " + new Gson().toJson(aiuiResultNtfBean));
                }
            } else {
                // 除了 0 ，其他的都是错误或者没有语义，要走闲聊
                aiuiResultNtfBean.setResult("");
                msgCollentListener.collentMessage(new Gson().toJson(aiuiResultNtfBean));
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

    }
}
