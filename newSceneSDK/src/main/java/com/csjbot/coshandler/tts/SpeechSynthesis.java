package com.csjbot.coshandler.tts;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import com.csjbot.coshandler.listener.OnSpeakListener;
import com.csjbot.coshandler.log.Csjlogger;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.util.EventHandler;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SpeechSynthesis implements ISpeechSpeak {
    private static SpeechSynthesis speechSynthesis;
    private final String TAG = "SpeechSynthesis";
    // 语音音量，默认值为 100f
    private float volume = 100f;
    // 语音语言，默认值为格鲁吉亚语 "ka-GE"
    private String language = "ka-GE";
    // 微软语音服务订阅密钥
    private final String subscriptionKey = "6yE45t5oZvr8bkNtpBkrbHiZb62hoVuz97U06sfiGNErZd2kAYyQJQQJ99BCACYeBjFXJ3w3AAAYACOGh2dS";
    // 微软语音服务订阅区域
    private final String subscriptionRegion = "eastus";
    // 语音配置对象
    private SpeechConfig speechConfig;
    // 语音合成器对象
    private SpeechSynthesizer speechSynthesizer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    public SpeechSynthesis() {
    }

    private String resumeString;
    public static SpeechSynthesis getInstance() {
        if (speechSynthesis == null) {
            speechSynthesis = new SpeechSynthesis();
        }
        return speechSynthesis;
    }

    private boolean isSpeaking = false;

    private boolean isFinish = true;

    public void speakWithSSML(String text, OnSpeakListener listener) {
        closeSpeech();
        resumeString = text;
        try {
            // 每次播放前重新初始化 SpeechConfig 和 SpeechSynthesizer
            speechConfig = SpeechConfig.fromSubscription(subscriptionKey, subscriptionRegion);
            // 设置音量（取值范围 0~100，默认 100）
        //    speechConfig.setProperty(PropertyId.SpeechServiceConnection_SynthVoice.SpeechServiceConnection_SynthOutputVolume, "80");
            /*if (CommonConstants.isGeorgian) {
                speechConfig.setSpeechSynthesisVoiceName("ka-GE-EkaNeural");
            } else {
                speechConfig.setSpeechSynthesisVoiceName("zh-CN-XiaoxiaoMultilingualNeural");
            }*/
            speechConfig.setSpeechSynthesisVoiceName("ka-GE-EkaNeural");
            speechSynthesizer = new SpeechSynthesizer(speechConfig);
        } catch (Exception e) {
            Csjlogger.error(TAG + " Failed to initialize SpeechSynthesizer: " + e);
        }

        try {
            String ssmlText =
                    "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='zh-CN'>" +
                            "<prosody volume='+20%'>May I have your name</prosody>" +
                     "</speak>";
            text = "რა არის რობოტი";

            String volumeStr = "+" + volume + "%";
            Csjlogger.info(TAG + " 微软TTS播报音量： " + volumeStr);

            // 构建 SSML
            @SuppressLint("DefaultLocale") String ssml = String.format(
                    "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='%s'>" +
                            "<voice name='ka-GE-EkaNeural'>" +
                            "<prosody volume='%s'>" +
                            "%s" +
                            "</prosody>" +
                            "</voice>" +
                            "</speak>",
                    "ka-GE", "-95%", text
            );
            speechSynthesizer.StartSpeakingSsml(ssml);
            isFinish = true;
            isSpeaking = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onSpeakBegin();
                }
            });
            speechSynthesizer.SynthesisStarted.addEventListener(new EventHandler<SpeechSynthesisEventArgs>() {
                @Override
                public void onEvent(Object sender, SpeechSynthesisEventArgs e) {
                    Csjlogger.info(TAG + " 微软TTS播报开始： " + e.getResult().getReason().toString());
                }
            });
            speechSynthesizer.Synthesizing.addEventListener(new EventHandler<SpeechSynthesisEventArgs>() {
                @Override
                public void onEvent(Object sender, SpeechSynthesisEventArgs e) {
                    Csjlogger.info(TAG + " 微软TTS播报进行中： " + e.getResult().getReason().toString());
                }
            });
            speechSynthesizer.SynthesisCanceled.addEventListener(new EventHandler<SpeechSynthesisEventArgs>() {
                @Override
                public void onEvent(Object sender, SpeechSynthesisEventArgs e) {
                    // 获取语音合成结果
                    SpeechSynthesisResult result = e.getResult();
                    // 获取取消原因
                    ResultReason reason = result.getReason();
                    Csjlogger.error(TAG + " 微软TTS播报取消: " + reason.toString());
                    // 获取错误详细信息
                    String errorDetails = result.getProperties().getProperty(PropertyId.SpeechServiceResponse_JsonErrorDetails);
                    if (errorDetails != null && !errorDetails.isEmpty()) {
                        Csjlogger.error(TAG + " 微软TTS播报取消 ErrorDetails: " + errorDetails);
                    } else {
                        Csjlogger.error(TAG + " 微软TTS播报取消 No additional error details.");
                    }
                }
            });
            speechSynthesizer.SynthesisCompleted.addEventListener(new EventHandler<SpeechSynthesisEventArgs>() {
                @Override
                public void onEvent(Object sender, SpeechSynthesisEventArgs e) {
                    if ("SynthesizingAudioCompleted".equals(e.getResult().getReason().toString())) {
                        Csjlogger.info(TAG + " 微软TTS播报完成");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinish) {
                                    listener.onCompleted(null);
                                    isSpeaking = false;
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            Csjlogger.error(TAG + " 微软TTS播报抛异常" + e.getMessage());
            e.printStackTrace();
        }
    }
    public void closeSpeech() {
        try {
            if (speechConfig != null) {
                speechConfig.close();
                speechConfig = null; // 设置为 null，避免重复关闭
            }
            if (speechSynthesizer != null) {
                speechSynthesizer.StopSpeakingAsync();
                speechSynthesizer.close();
                speechSynthesizer = null; // 设置为 null，避免重复关闭
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Csjlogger.info(TAG + " 微软TTS播报 关闭speechSynthesizer");
    }

    @Override
    public void startSpeaking(String text, OnSpeakListener listener) {
        speakWithSSML(text, listener);
    }


    @Override
    public void stopSpeaking() {
        Csjlogger.info(TAG + " 微软TTS播报停止 stopSpeaking called.");
        try {
            if (speechSynthesizer != null) {
                // 停止语音合成
                speechSynthesizer.StopSpeakingAsync();
                Csjlogger.info(TAG + " 微软TTS播报停止 StopSpeakingAsync called.");
                isFinish = false;
                isSpeaking = false;
            } else {
                Csjlogger.error(TAG + " 微软TTS播报停止 speechSynthesizer is null, cannot stop speaking.");
                closeSpeech();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pauseSpeaking() {
        // 实现暂停逻辑
    }

    @Override
    public void resumeSpeaking() {
        // 实现恢复逻辑
    }

    @Override
    public void resumeSpeaking(OnSpeakListener listener) {
        // 实现恢复逻辑
        speakWithSSML(resumeString, listener);
    }

    @Override
    public boolean isSpeaking() {
        return isSpeaking; // 实现是否正在说话的逻辑
    }

    @Override
    public boolean setSpeakerName(String name) {
        return false; // 实现设置说话人名称的逻辑
    }

    @Override
    public boolean setLanguage(int language) {
        return false; // 实现设置语言的逻辑
    }

    @Override
    public boolean setLanguage(Locale language) {
        this.language = language.toString();
        return true;
    }

    @Override
    public List<String> getSpeakerNames(String language, String country) {
        return Collections.emptyList(); // 实现获取说话人名称列表的逻辑
    }

    @Override
    public void setVolume(float volume) {
        Csjlogger.info("设置音量 volume：" + volume);
        this.volume = volume;
    }

    @Override
    public void setSpeed(int speed) {
        // 实现设置语速的逻辑
    }
}