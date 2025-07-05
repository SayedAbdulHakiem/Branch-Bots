package com.csjbot.coshandler.taskscheduler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * UI线程的Handler
 */
public class UIHandler extends Handler {

    public UIHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void dispatchMessage(Message msg) {
        try {
            super.dispatchMessage(msg);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
