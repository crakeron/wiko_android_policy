// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.FrameLayout;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardViewCallback

public abstract class KeyguardViewBase extends FrameLayout
{

    private static final int BACKGROUND_COLOR = 0x70000000;
    private static final boolean KEYGUARD_MANAGES_VOLUME = true;
    private AudioManager mAudioManager;
    Drawable mBackgroundDrawable;
    private KeyguardViewCallback mCallback;
    private TelephonyManager mTelephonyManager;

    public KeyguardViewBase(Context context)
    {
        super(context);
        mTelephonyManager = null;
        mBackgroundDrawable = new Drawable() {

            final KeyguardViewBase this$0;

            public void draw(Canvas canvas)
            {
                canvas.drawColor(0x70000000, android.graphics.PorterDuff.Mode.SRC);
            }

            public int getOpacity()
            {
                return -3;
            }

            public void setAlpha(int i)
            {
            }

            public void setColorFilter(ColorFilter colorfilter)
            {
            }

            
            {
                this$0 = KeyguardViewBase.this;
                super();
            }
        }
;
        resetBackground();
    }

    private boolean interceptMediaKey(KeyEvent keyevent)
    {
        boolean flag;
        int i;
        int j;
        i = -1;
        flag = true;
        j = keyevent.getKeyCode();
        if (keyevent.getAction() != 0) goto _L2; else goto _L1
_L1:
        j;
        JVM INSTR lookupswitch 14: default 144
    //                   24: 219
    //                   25: 219
    //                   79: 188
    //                   85: 148
    //                   86: 188
    //                   87: 188
    //                   88: 188
    //                   89: 188
    //                   90: 188
    //                   91: 188
    //                   126: 148
    //                   127: 148
    //                   130: 188
    //                   164: 219;
           goto _L3 _L4 _L4 _L5 _L6 _L5 _L5 _L5 _L5 _L5 _L5 _L6 _L6 _L5 _L4
_L3:
        flag = false;
_L7:
        return flag;
_L6:
        if (mTelephonyManager == null)
            mTelephonyManager = (TelephonyManager)getContext().getSystemService("phone");
        if (mTelephonyManager != null && mTelephonyManager.getCallState() != 0) goto _L7; else goto _L5
_L5:
        i = new Intent("android.intent.action.MEDIA_BUTTON", null);
        i.putExtra("android.intent.extra.KEY_EVENT", keyevent);
        getContext().sendOrderedBroadcast(i, null);
          goto _L7
_L4:
        this;
        JVM INSTR monitorenter ;
        if (mAudioManager == null)
            mAudioManager = (AudioManager)getContext().getSystemService("audio");
        this;
        JVM INSTR monitorexit ;
        if (mAudioManager.isMusicActive())
        {
            AudioManager audiomanager = mAudioManager;
            if (j == 24)
                i = ((flag) ? 1 : 0);
            else
                i = i;
            audiomanager.adjustStreamVolume(3, i, 0);
        } else
        if (mAudioManager.isFmActive())
        {
            AudioManager audiomanager1 = mAudioManager;
            if (j == 24)
                i = ((flag) ? 1 : 0);
            audiomanager1.adjustStreamVolume(10, i, 0);
        }
          goto _L7
        flag;
        this;
        JVM INSTR monitorexit ;
        throw flag;
_L2:
        if (keyevent.getAction() != flag) goto _L3; else goto _L8
_L8:
        switch (j)
        {
        default:
            break;

        case 79: // 'O'
        case 85: // 'U'
        case 86: // 'V'
        case 87: // 'W'
        case 88: // 'X'
        case 89: // 'Y'
        case 90: // 'Z'
        case 91: // '['
        case 126: // '~'
        case 127: // '\177'
        case 130: 
            Intent intent = new Intent("android.intent.action.MEDIA_BUTTON", null);
            intent.putExtra("android.intent.extra.KEY_EVENT", keyevent);
            getContext().sendOrderedBroadcast(intent, null);
            break;
        }
        if (true) goto _L7; else goto _L9
_L9:
        if (true) goto _L3; else goto _L10
_L10:
    }

    private boolean shouldEventKeepScreenOnWhileKeyguardShowing(KeyEvent keyevent)
    {
        boolean flag = false;
        if (keyevent.getAction() == 0)
            switch (keyevent.getKeyCode())
            {
            default:
                flag = true;
                break;

            case 19: // '\023'
            case 20: // '\024'
            case 21: // '\025'
            case 22: // '\026'
                break;
            }
        return flag;
    }

    public abstract void cleanUp();

    public boolean dispatchKeyEvent(KeyEvent keyevent)
    {
        if (shouldEventKeepScreenOnWhileKeyguardShowing(keyevent))
            mCallback.pokeWakelock();
        boolean flag;
        if (!interceptMediaKey(keyevent))
            flag = super.dispatchKeyEvent(keyevent);
        else
            flag = true;
        return flag;
    }

    public void dispatchSystemUiVisibilityChanged(int i)
    {
        super.dispatchSystemUiVisibilityChanged(i);
        setSystemUiVisibility(0x400000);
    }

    public KeyguardViewCallback getCallback()
    {
        return mCallback;
    }

    public abstract void onScreenTurnedOff();

    public abstract void onScreenTurnedOn();

    public abstract void reset();

    public void resetBackground()
    {
        setBackgroundDrawable(mBackgroundDrawable);
    }

    void setCallback(KeyguardViewCallback keyguardviewcallback)
    {
        mCallback = keyguardviewcallback;
    }

    public abstract void show();

    public abstract void verifyUnlock();

    public abstract void wakeWhenReadyTq(int i);
}
