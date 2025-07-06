package com.smart24.branch_bots.shared.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.csjbot.coshandler.core.CsjRobot;

public class MoveViewModel extends ViewModel {
    private MutableLiveData<Boolean> moveMld;

    private void move(int direction) {
        CsjRobot.getInstance().getAction().move(direction);
        moveMld.setValue(Boolean.TRUE);
    }

    public void moveForward() {
        move(0);

    }

    public void moveBack() {
        move(1);
    }

    public void moveLeft() {
        move(2);
    }

    public void moveRight() {
        move(3);
    }

}
