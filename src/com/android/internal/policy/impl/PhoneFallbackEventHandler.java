// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.KeyguardManager;
import android.app.SearchManager;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.Slog;
import android.view.*;

// Referenced classes of package com.android.internal.policy.impl:
//            PhoneWindowManager

public class PhoneFallbackEventHandler
    implements FallbackEventHandler
{

    private static final boolean DEBUG;
    private static String TAG = "PhoneFallbackEventHandler";
    AudioManager mAudioManager;
    Context mContext;
    KeyguardManager mKeyguardManager;
    SearchManager mSearchManager;
    TelephonyManager mTelephonyManager;
    View mView;

    public PhoneFallbackEventHandler(Context context)
    {
        mContext = context;
    }

    public boolean dispatchKeyEvent(KeyEvent keyevent)
    {
        int i = keyevent.getAction();
        boolean flag = keyevent.getKeyCode();
        if (i != 0)
            flag = onKeyUp(flag, keyevent);
        else
            flag = onKeyDown(flag, keyevent);
        return flag;
    }

    AudioManager getAudioManager()
    {
        if (mAudioManager == null)
            mAudioManager = (AudioManager)mContext.getSystemService("audio");
        return mAudioManager;
    }

    KeyguardManager getKeyguardManager()
    {
        if (mKeyguardManager == null)
            mKeyguardManager = (KeyguardManager)mContext.getSystemService("keyguard");
        return mKeyguardManager;
    }

    SearchManager getSearchManager()
    {
        if (mSearchManager == null)
            mSearchManager = (SearchManager)mContext.getSystemService("search");
        return mSearchManager;
    }

    TelephonyManager getTelephonyManager()
    {
        if (mTelephonyManager == null)
            mTelephonyManager = (TelephonyManager)mContext.getSystemService("phone");
        return mTelephonyManager;
    }

    boolean onKeyDown(int i, KeyEvent keyevent)
    {
        boolean flag;
        Object obj;
        flag = true;
        obj = mView.getKeyDispatcherState();
        i;
        JVM INSTR lookupswitch 17: default 160
    //                   5: 221
    //                   24: 164
    //                   25: 164
    //                   27: 327
    //                   79: 187
    //                   84: 428
    //                   85: 177
    //                   86: 187
    //                   87: 187
    //                   88: 187
    //                   89: 187
    //                   90: 187
    //                   91: 187
    //                   126: 177
    //                   127: 177
    //                   130: 187
    //                   164: 164;
           goto _L1 _L2 _L3 _L3 _L4 _L5 _L6 _L7 _L5 _L5 _L5 _L5 _L5 _L5 _L7 _L7 _L5 _L3
_L1:
        flag = false;
_L9:
        return flag;
_L3:
        getAudioManager().handleKeyDown(i, 0x80000000);
        continue; /* Loop/switch isn't completed */
_L7:
        if (getTelephonyManager().getCallState() != 0)
            continue; /* Loop/switch isn't completed */
_L5:
        obj = new Intent("android.intent.action.MEDIA_BUTTON", null);
        ((Intent) (obj)).putExtra("android.intent.extra.KEY_EVENT", keyevent);
        mContext.sendOrderedBroadcast(((Intent) (obj)), null);
        continue; /* Loop/switch isn't completed */
_L2:
        if (!getKeyguardManager().inKeyguardRestrictedInputMode() && obj != null)
        {
            if (keyevent.getRepeatCount() == 0)
                ((android.view.KeyEvent.DispatcherState) (obj)).startTracking(keyevent, this);
            else
            if (keyevent.isLongPress() && ((android.view.KeyEvent.DispatcherState) (obj)).isTracking(keyevent))
            {
                ((android.view.KeyEvent.DispatcherState) (obj)).performedLongPress(keyevent);
                mView.performHapticFeedback(0);
                obj = new Intent("android.intent.action.VOICE_COMMAND");
                ((Intent) (obj)).setFlags(0x10000000);
                try
                {
                    sendCloseSystemWindows();
                    mContext.startActivity(((Intent) (obj)));
                }
                catch (ActivityNotFoundException _ex)
                {
                    startCallActivity();
                }
            }
            continue; /* Loop/switch isn't completed */
        }
        continue; /* Loop/switch isn't completed */
_L4:
        if (!getKeyguardManager().inKeyguardRestrictedInputMode() && obj != null)
        {
            if (keyevent.getRepeatCount() == 0)
                ((android.view.KeyEvent.DispatcherState) (obj)).startTracking(keyevent, this);
            else
            if (keyevent.isLongPress() && ((android.view.KeyEvent.DispatcherState) (obj)).isTracking(keyevent))
            {
                ((android.view.KeyEvent.DispatcherState) (obj)).performedLongPress(keyevent);
                mView.performHapticFeedback(0);
                sendCloseSystemWindows();
                obj = new Intent("android.intent.action.CAMERA_BUTTON", null);
                ((Intent) (obj)).putExtra("android.intent.extra.KEY_EVENT", keyevent);
                mContext.sendOrderedBroadcast(((Intent) (obj)), null);
            }
            continue; /* Loop/switch isn't completed */
        }
        continue; /* Loop/switch isn't completed */
_L6:
        Object obj1;
        if (getKeyguardManager().inKeyguardRestrictedInputMode() || obj == null)
            continue; /* Loop/switch isn't completed */
        if (keyevent.getRepeatCount() == 0)
        {
            ((android.view.KeyEvent.DispatcherState) (obj)).startTracking(keyevent, this);
            continue; /* Loop/switch isn't completed */
        }
        if (!keyevent.isLongPress() || !((android.view.KeyEvent.DispatcherState) (obj)).isTracking(keyevent))
            continue; /* Loop/switch isn't completed */
        obj1 = mContext.getResources().getConfiguration();
        if (((Configuration) (obj1)).keyboard != flag && ((Configuration) (obj1)).hardKeyboardHidden != 2)
            continue; /* Loop/switch isn't completed */
        obj1 = new Intent("android.intent.action.SEARCH_LONG_PRESS");
        ((Intent) (obj1)).setFlags(0x10000000);
        mView.performHapticFeedback(0);
        sendCloseSystemWindows();
        getSearchManager().stopSearch();
        mContext.startActivity(((Intent) (obj1)));
        ((android.view.KeyEvent.DispatcherState) (obj)).performedLongPress(keyevent);
        if (true) goto _L9; else goto _L8
_L8:
        JVM INSTR pop ;
        if (true) goto _L1; else goto _L10
_L10:
    }

    boolean onKeyUp(int i, KeyEvent keyevent)
    {
        boolean flag;
        flag = true;
        android.view.KeyEvent.DispatcherState dispatcherstate = mView.getKeyDispatcherState();
        if (dispatcherstate != null)
            dispatcherstate.handleUpEvent(keyevent);
        i;
        JVM INSTR lookupswitch 16: default 236
    //                   5: 160
    //                   24: 191
    //                   25: 191
    //                   27: 226
    //                   79: 258
    //                   85: 258
    //                   86: 258
    //                   87: 258
    //                   88: 258
    //                   89: 258
    //                   90: 258
    //                   91: 258
    //                   126: 258
    //                   127: 258
    //                   130: 258
    //                   164: 191;
           goto _L1 _L2 _L3 _L3 _L4 _L5 _L5 _L5 _L5 _L5 _L5 _L5 _L5 _L5 _L5 _L5 _L3
_L2:
        if (!getKeyguardManager().inKeyguardRestrictedInputMode())
        {
            if (keyevent.isTracking() && !keyevent.isCanceled())
                startCallActivity();
            break MISSING_BLOCK_LABEL_289;
        }
          goto _L1
_L3:
        if (!keyevent.isCanceled() && (AudioManager)mContext.getSystemService("audio") != null)
            getAudioManager().handleKeyUp(i, 0x80000000);
        break MISSING_BLOCK_LABEL_289;
_L4:
        if (!getKeyguardManager().inKeyguardRestrictedInputMode()) goto _L6; else goto _L1
_L1:
        flag = false;
        break MISSING_BLOCK_LABEL_289;
_L6:
        if (!keyevent.isTracking() || keyevent.isCanceled());
        break MISSING_BLOCK_LABEL_289;
_L5:
        Intent intent = new Intent("android.intent.action.MEDIA_BUTTON", null);
        intent.putExtra("android.intent.extra.KEY_EVENT", keyevent);
        mContext.sendOrderedBroadcast(intent, null);
        return flag;
    }

    public void preDispatchKeyEvent(KeyEvent keyevent)
    {
        getAudioManager().preDispatchKeyEvent(keyevent.getKeyCode(), 0x80000000);
    }

    void sendCloseSystemWindows()
    {
        PhoneWindowManager.sendCloseSystemWindows(mContext, null);
    }

    public void setView(View view)
    {
        mView = view;
    }

    void startCallActivity()
    {
        Intent intent;
        sendCloseSystemWindows();
        intent = new Intent("android.intent.action.CALL_BUTTON");
        intent.setFlags(0x10000000);
        mContext.startActivity(intent);
_L1:
        return;
        JVM INSTR pop ;
        Slog.w(TAG, "No activity found for android.intent.action.CALL_BUTTON.");
          goto _L1
    }

}
