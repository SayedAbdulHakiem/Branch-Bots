package com.csjbot.asragent.aiui_soft.cae;

import com.csjbot.asragent.aiui_soft.util.FileUtil;

public interface IRecorder {
    int initCAE(OnCaeOperatorlistener onCaeOperatorlistener);

    void saveAduio(byte[] bytes, FileUtil util);

    void writeAudioTest(byte[] data);

    void stopSaveAudio();

    boolean isAudioSaving();

    void startSaveAudio();
}
