package com.csjbot.coshandler.listener;

/**
 * @author 王乐涛
 * @email wangletao@qq.com
 * @date 2024/12/20
 * @description
 */
public interface OnRobotPersonCheckListener {
    /**
     * @param type      0代表深度相机；1代表激光;2代表激光；3代表人脸
     * @param isChecked
     */
    void onPersonCheck(int type, boolean isChecked);
}
