package com.csjbot.asragent.aiui_soft.cae;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.csjbot.asragent.aiui_soft.util.FileUtil;
import com.csjbot.coshandler.log.BaseLogger;

public class CsjRecorder implements IRecorder {
    private OnCaeOperatorlistener mOnCaeOperatorlistener;

    private final Recorder mRecorder = new Recorder();


    @Override
    public int initCAE(OnCaeOperatorlistener onCaeOperatorlistener) {
        mOnCaeOperatorlistener = onCaeOperatorlistener;
        BaseLogger.debug("AIUI csj initCAE");
        try {
            startRecord();
        } catch (Exception e) {
            BaseLogger.debug("AIUI csj initCAE  error ");
        }

        return 0;
    }

    @Override
    public void saveAduio(byte[] bytes, FileUtil util) {

    }

    @Override
    public void writeAudioTest(byte[] data) {

    }

    @Override
    public void stopSaveAudio() {
        stopRecord();
    }

    @Override
    public boolean isAudioSaving() {
        return false;
    }

    @Override
    public void startSaveAudio() {

    }

    public int startRecord() {
        if (mRecorder != null)
            mRecorder.start();
        return 0;
    }

    public void stopRecord() {
        BaseLogger.debug("AIUI csj Recorder  ->mRecorder.stop ");
        if (mRecorder != null)
            mRecorder.stop();
    }

//    private class Recorder implements Runnable {
//        /* AudioRecord */
//        private AudioRecord mAudioRecord = null;
//        // frequency
//        protected int mFrequency = 16000;
//        // buffer size
//        protected int mRecordBufferSize = (1 << 11);
//        // channel
//        private int mChannelConfiguration = AudioFormat.CHANNEL_IN_MONO;
//        // bits
//        private int mAudioEncodingBits = AudioFormat.ENCODING_PCM_16BIT;
//
//        /* mIsComplete */
//        boolean mIsComplete = true;
//
//        /* Thread */
//        private Thread mRecoderThread = null;
//
//        void start() {
//            mRecoderThread = new Thread(this);
//            mRecoderThread.start();
//            BaseLogger.debug("mRecoderThread start");
//        }
//
//        void stop() {
//            BaseLogger.debug("AIUI csj Recorder  ->mRecorder.stop   in ");
//            mIsComplete = true;
//            if (mRecoderThread != null && mRecoderThread.isAlive()) {
//                try {
//                    BaseLogger.debug("AIUI csj Recorder  ->mRecoderThread.join");
//                    mRecoderThread.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        @Override
//        public void run() {
//            /* AudioRecord */
//            if (mAudioRecord != null) {
//                mAudioRecord.release();
//                mAudioRecord = null;
//            }
//            try {
//                mRecordBufferSize = AudioRecord.getMinBufferSize(mFrequency,
//                        mChannelConfiguration, mAudioEncodingBits);
//                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                        mFrequency, mChannelConfiguration, mAudioEncodingBits,
//                        mRecordBufferSize);
//            } catch (Exception e) {
//                BaseLogger.error("AIUI csj 录音机初始化失败：{}", e.getLocalizedMessage());
//                e.printStackTrace();
//                return;
//            }
//            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
//                BaseLogger.error("AIUI csj 录音机不可用");
//                return;
//            }
//            try {
//                mAudioRecord.startRecording();
//            } catch (IllegalStateException e) {
//                BaseLogger.error("AIUI csj 启动录音异常：{}", e.getLocalizedMessage());
//                return;
//            }
//
//            /* append data */
//            mIsComplete = false;
//            while (true) {
//                byte[] pcmBuffer = new byte[mRecordBufferSize];
//                int pcmBufferSize = mAudioRecord.read(pcmBuffer, 0,
//                        mRecordBufferSize);
//                if (pcmBufferSize == AudioRecord.ERROR_INVALID_OPERATION) {
//                    return;
//                } else if (pcmBufferSize == AudioRecord.ERROR_BAD_VALUE) {
//                    return;
//                } else if (pcmBufferSize != mRecordBufferSize) {
//                    return;
//                }
//
//                if (mOnCaeOperatorlistener != null) {
//                    mOnCaeOperatorlistener.onAudio(pcmBuffer, pcmBufferSize);
//                }
//            }
//        }
//    }

    private class Recorder implements Runnable {
        /* AudioRecord */
        private AudioRecord mAudioRecord = null;
        // frequency
        protected int mFrequency = 16000;
        // buffer size
        protected int mRecordBufferSize = (1 << 11);
        // channel
        private final int mChannelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        // bits
        private final int mAudioEncodingBits = AudioFormat.ENCODING_PCM_16BIT;
        /* Thread */
        private Thread mRecoderThread = null;
        /* mIsComplete */
        private volatile boolean mIsComplete = false; // 使用volatile确保可见性


        void start() {
            mIsComplete = false; // 开始录音时将标志位设置为false
            mRecoderThread = new Thread(this);
            mRecoderThread.start();
            BaseLogger.debug("mRecoderThread start");
        }

        void stop() {
            BaseLogger.debug("AIUI csj Recorder  ->mRecorder.stop   in ");
            mIsComplete = true; // 设置标志位为true，通知线程停止
            if (mRecoderThread != null && mRecoderThread.isAlive()) {
                try {
                    BaseLogger.debug("AIUI csj Recorder  ->mRecoderThread.join");
                    mRecoderThread.join(); // 等待线程结束
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            /* AudioRecord */
            if (mAudioRecord != null) {
                mAudioRecord.release();
                mAudioRecord = null;
            }
            try {
                mRecordBufferSize = AudioRecord.getMinBufferSize(mFrequency,
                        mChannelConfiguration, mAudioEncodingBits);
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        mFrequency, mChannelConfiguration, mAudioEncodingBits,
                        mRecordBufferSize);
            } catch (Exception e) {
                BaseLogger.error("AIUI csj 录音机初始化失败：{}", e.getLocalizedMessage());
                e.printStackTrace();
                return;
            }
            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                BaseLogger.error("AIUI csj 录音机不可用");
                return;
            }

            try {
                mAudioRecord.startRecording();
            } catch (IllegalStateException e) {
                BaseLogger.error("AIUI csj 启动录音异常：{}", e.getLocalizedMessage());
                return;
            }


            while (!mIsComplete) { // 检查标志位
                byte[] pcmBuffer = new byte[mRecordBufferSize];
                int pcmBufferSize = mAudioRecord.read(pcmBuffer, 0, mRecordBufferSize);
                if (pcmBufferSize == AudioRecord.ERROR_INVALID_OPERATION ||
                        pcmBufferSize == AudioRecord.ERROR_BAD_VALUE ||
                        pcmBufferSize != mRecordBufferSize) {
                    break; // 读取错误时退出循环
                }

                if (mOnCaeOperatorlistener != null) {
                    mOnCaeOperatorlistener.onAudio(pcmBuffer, pcmBufferSize);
                }
            }

            // 结束录音
            if (mAudioRecord != null) {
                mAudioRecord.stop(); // 停止录音
                mAudioRecord.release(); // 释放资源
                mAudioRecord = null;
            }
        }
    }

}