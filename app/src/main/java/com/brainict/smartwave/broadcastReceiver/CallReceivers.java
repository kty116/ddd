package com.brainict.smartwave.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.brainict.smartwave.common.TinyDB;
import com.brainict.smartwave.event.CallEvent;

import org.greenrobot.eventbus.EventBus;

public class CallReceivers extends BroadcastReceiver {

    public static final String TAG = CallReceivers.class.getSimpleName();
    private TinyDB mTinyDB;
    private String mLastState;

    @Override
    public void onReceive(Context context, Intent intent) {
        mTinyDB = new TinyDB(context);
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (state.equals(mLastState)) {
            return;
        } else {
            mLastState = state;
        }
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            EventBus.getDefault().post(new CallEvent());
        }
    }
}