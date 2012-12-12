// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.*;
import android.util.Log;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.mediatek.xlog.Xlog;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardWindowController, KeyguardUpdateMonitor, KeyguardViewBase, KeyguardViewCallback, 
//            KeyguardViewProperties

public class KeyguardViewManager
    implements KeyguardWindowController
{
    private static class KeyguardViewHost extends FrameLayout
    {

        private final KeyguardViewCallback mCallback;

        protected void dispatchDraw(Canvas canvas)
        {
            super.dispatchDraw(canvas);
            if (!KeyguardViewManager.mScreenOn)
                mCallback.keyguardDoneDrawing();
        }

        private KeyguardViewHost(Context context, KeyguardViewCallback keyguardviewcallback)
        {
            super(context);
            mCallback = keyguardviewcallback;
        }

    }

    final class H extends Handler
    {

        public static final int SET_LOCKSCREEN_SENSOR_MODE = 1;
        final KeyguardViewManager this$0;

        public void handleMessage(Message message)
        {
            if (KeyguardViewManager.DEBUG)
                Xlog.d(KeyguardViewManager.TAG, (new StringBuilder()).append("handle msg: ").append(message.what).append(", arg1=").append(message.arg1).toString());
            switch (message.what)
            {
            default:
                break;

            case 1: // '\001'
                if (mWindowLayoutParams != null && mWindowLayoutParams.screenOrientation != message.arg1)
                {
                    if (KeyguardViewManager.DEBUG)
                        Xlog.d(KeyguardViewManager.TAG, (new StringBuilder()).append("curr_orientation=").append(mWindowLayoutParams.screenOrientation).toString());
                    mWindowLayoutParams.screenOrientation = message.arg1;
                    mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);
                }
                break;
            }
        }

        public H()
        {
            this$0 = KeyguardViewManager.this;
            super();
        }
    }

    public static interface ShowListener
    {

        public abstract void onShown(IBinder ibinder);
    }


    private static boolean DEBUG = true;
    private static String TAG = "KeyguardViewManager";
    private static boolean mScreenOn = false;
    private final KeyguardViewCallback mCallback;
    private final Context mContext;
    H mH;
    private FrameLayout mKeyguardHost;
    private KeyguardViewBase mKeyguardView;
    private final KeyguardViewProperties mKeyguardViewProperties;
    private boolean mNeedsInput;
    private int mScrnOrientationModeBeforeShutdown;
    private KeyguardUpdateMonitor.SystemStateCallback mSystemStateCallback;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private final ViewManager mViewManager;
    private android.view.WindowManager.LayoutParams mWindowLayoutParams;

    public KeyguardViewManager(Context context, ViewManager viewmanager, KeyguardViewCallback keyguardviewcallback, KeyguardViewProperties keyguardviewproperties, KeyguardUpdateMonitor keyguardupdatemonitor)
    {
        mNeedsInput = false;
        mScrnOrientationModeBeforeShutdown = 4;
        mH = new H();
        mSystemStateCallback = new KeyguardUpdateMonitor.SystemStateCallback() {

            final KeyguardViewManager this$0;

            public void onSysBootup()
            {
                if (KeyguardViewManager.DEBUG)
                    Xlog.d(KeyguardViewManager.TAG, (new StringBuilder()).append("onSysBootup called, mScrnOrientationModeBeforeShutdown = ").append(mScrnOrientationModeBeforeShutdown).toString());
                Message message = mH.obtainMessage(1);
                message.arg1 = mScrnOrientationModeBeforeShutdown;
                mH.sendMessage(message);
            }

            public void onSysShutdown()
            {
                if (KeyguardViewManager.DEBUG)
                    Xlog.d(KeyguardViewManager.TAG, "onSysShutdown called.");
                if (mWindowLayoutParams == null)
                {
                    if (KeyguardViewManager.DEBUG)
                        Xlog.d(KeyguardViewManager.TAG, "mWindowLayoutParams is null, ignore the message.");
                } else
                {
                    mScrnOrientationModeBeforeShutdown = mWindowLayoutParams.screenOrientation;
                    Message message = mH.obtainMessage(1);
                    message.arg1 = 2;
                    mH.sendMessage(message);
                }
            }

            
            {
                this$0 = KeyguardViewManager.this;
                super();
            }
        }
;
        mContext = context;
        mViewManager = viewmanager;
        mCallback = keyguardviewcallback;
        mKeyguardViewProperties = keyguardviewproperties;
        mUpdateMonitor = keyguardupdatemonitor;
        if ("tablet".equals(SystemProperties.get("ro.build.characteristics")))
            keyguardupdatemonitor.registerSystemStateCallback(mSystemStateCallback);
    }

    /**
     * @deprecated Method hide is deprecated
     */

    public void hide()
    {
        this;
        JVM INSTR monitorenter ;
        if (DEBUG)
            Xlog.d(TAG, "hide()");
        if (mKeyguardHost != null)
        {
            mKeyguardHost.setVisibility(8);
            if (mKeyguardView != null)
            {
                final KeyguardViewBase lastView = mKeyguardView;
                mKeyguardView = null;
                mKeyguardHost.post(new Runnable() {

                    final KeyguardViewManager this$0;
                    final KeyguardViewBase val$lastView;

                    public void run()
                    {
                        KeyguardViewManager keyguardviewmanager = KeyguardViewManager.this;
                        keyguardviewmanager;
                        JVM INSTR monitorenter ;
                        lastView.cleanUp();
                        mKeyguardHost.removeView(lastView);
                        return;
                    }

            
            {
                this$0 = KeyguardViewManager.this;
                lastView = keyguardviewbase;
                super();
            }
                }
);
            }
        }
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    /**
     * @deprecated Method isShowing is deprecated
     */

    public boolean isShowing()
    {
        this;
        JVM INSTR monitorenter ;
        if (mKeyguardHost == null) goto _L2; else goto _L1
_L1:
        int i = mKeyguardHost.getVisibility();
        if (i != 0) goto _L2; else goto _L3
_L3:
        i = 1;
_L5:
        this;
        JVM INSTR monitorexit ;
        return i;
_L2:
        i = 0;
        if (true) goto _L5; else goto _L4
_L4:
        Exception exception;
        exception;
        throw exception;
    }

    /**
     * @deprecated Method onScreenTurnedOff is deprecated
     */

    public void onScreenTurnedOff()
    {
        this;
        JVM INSTR monitorenter ;
        if (DEBUG)
            Xlog.d(TAG, "onScreenTurnedOff()");
        mScreenOn = false;
        if (mKeyguardView != null)
            mKeyguardView.onScreenTurnedOff();
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    /**
     * @deprecated Method onScreenTurnedOn is deprecated
     */

    public void onScreenTurnedOn(final ShowListener showListener)
    {
        this;
        JVM INSTR monitorenter ;
        if (DEBUG)
            Xlog.d(TAG, "onScreenTurnedOn()");
        mScreenOn = true;
        if (mKeyguardView == null)
            break MISSING_BLOCK_LABEL_80;
        mKeyguardView.onScreenTurnedOn();
        if (mKeyguardHost.getVisibility() != 0) goto _L2; else goto _L1
_L1:
        mKeyguardHost.post(new Runnable() {

            final KeyguardViewManager this$0;
            final ShowListener val$showListener;

            public void run()
            {
                if (mKeyguardHost.getVisibility() != 0)
                    showListener.onShown(null);
                else
                    showListener.onShown(mKeyguardHost.getWindowToken());
            }

            
            {
                this$0 = KeyguardViewManager.this;
                showListener = showlistener;
                super();
            }
        }
);
_L3:
        this;
        JVM INSTR monitorexit ;
        return;
_L2:
        showListener.onShown(null);
          goto _L3
        Exception exception;
        exception;
        throw exception;
        showListener.onShown(null);
          goto _L3
    }

    public void reLayoutScreen(boolean flag)
    {
        if (mWindowLayoutParams != null)
        {
            Log.i(TAG, (new StringBuilder()).append("reLayoutScreen, dmLock=").append(flag).toString());
            if (!flag)
            {
                android.view.WindowManager.LayoutParams layoutparams = mWindowLayoutParams;
                layoutparams.flags = 0xfffffeff & layoutparams.flags;
                layoutparams = mWindowLayoutParams;
                layoutparams.flags = 0xfffeffff & layoutparams.flags;
                layoutparams = mWindowLayoutParams;
                layoutparams.flags = 0x800 | layoutparams.flags;
            } else
            {
                android.view.WindowManager.LayoutParams layoutparams1 = mWindowLayoutParams;
                layoutparams1.flags = 0xfffff7ff & layoutparams1.flags;
                layoutparams1 = mWindowLayoutParams;
                layoutparams1.flags = 0x100 | layoutparams1.flags;
                layoutparams1 = mWindowLayoutParams;
                layoutparams1.flags = 0x10000 | layoutparams1.flags;
            }
            mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);
        }
    }

    /**
     * @deprecated Method reset is deprecated
     */

    public void reset()
    {
        this;
        JVM INSTR monitorenter ;
        mUpdateMonitor.DM_IsLocked();
        if (mKeyguardView == null)
            break MISSING_BLOCK_LABEL_27;
        mKeyguardView.reset();
_L1:
        this;
        JVM INSTR monitorexit ;
        return;
        Log.i(TAG, "Oh, Timing issue, actually, we needn't skip to here");
        mCallback.doKeyguardLocked();
          goto _L1
        Exception exception;
        exception;
        throw exception;
    }

    public void setDebugFilterStatus(boolean flag)
    {
        DEBUG = flag;
    }

    public void setNeedsInput(boolean flag)
    {
        mNeedsInput = flag;
        if (mWindowLayoutParams != null)
        {
            if (!flag)
            {
                android.view.WindowManager.LayoutParams layoutparams = mWindowLayoutParams;
                layoutparams.flags = 0x20000 | layoutparams.flags;
            } else
            {
                android.view.WindowManager.LayoutParams layoutparams1 = mWindowLayoutParams;
                layoutparams1.flags = 0xfffdffff & layoutparams1.flags;
            }
            mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);
        }
    }

    public void setScreenStatus(boolean flag)
    {
        mScreenOn = flag;
    }

    /**
     * @deprecated Method show is deprecated
     */

    public void show()
    {
        Object obj = 0;
        this;
        JVM INSTR monitorenter ;
        Object obj1;
        if (DEBUG)
            Xlog.d(TAG, (new StringBuilder()).append("show(); mKeyguardView==").append(mKeyguardView).toString());
        obj1 = mContext.getResources();
        if (SystemProperties.getBoolean("lockscreen.rot_override", false) || ((Resources) (obj1)).getBoolean(0x111001c))
            obj = 1;
        if (mKeyguardHost != null) goto _L2; else goto _L1
_L1:
        if (DEBUG)
            Xlog.d(TAG, "keyguard host is null, creating it...");
        mKeyguardHost = new KeyguardViewHost(mContext, mCallback);
        if (!mUpdateMonitor.DM_IsLocked()) goto _L4; else goto _L3
_L3:
        obj1 = 0x10000 | (0x100 | 0x10110900 & 0xfffff7ff);
_L7:
        if (!mNeedsInput)
            obj1 |= 0x20000;
        if (ActivityManager.isHighEndGfx(((WindowManager)mContext.getSystemService("window")).getDefaultDisplay()))
            obj1 |= 0x1000000;
        obj1 = new android.view.WindowManager.LayoutParams(-1, -1, 2004, ((int) (obj1)), -3);
        obj1.softInputMode = 16;
        obj1.windowAnimations = 0x10301da;
        if (ActivityManager.isHighEndGfx(((WindowManager)mContext.getSystemService("window")).getDefaultDisplay()))
        {
            obj1.flags = 0x1000000 | ((android.view.WindowManager.LayoutParams) (obj1)).flags;
            obj1.privateFlags = 2 | ((android.view.WindowManager.LayoutParams) (obj1)).privateFlags;
        }
        ((android.view.WindowManager.LayoutParams) (obj1)).setTitle("Keyguard");
        mWindowLayoutParams = ((android.view.WindowManager.LayoutParams) (obj1));
        mViewManager.addView(mKeyguardHost, ((android.view.ViewGroup.LayoutParams) (obj1)));
_L2:
        if (obj != 0) goto _L6; else goto _L5
_L5:
        if (DEBUG)
            Log.d(TAG, "Rotation sensor for lock screen Off!");
        mWindowLayoutParams.screenOrientation = 5;
_L8:
        mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);
        if (mKeyguardView == null)
        {
            if (DEBUG)
                Xlog.d(TAG, "keyguard view is null, creating it...");
            mKeyguardView = mKeyguardViewProperties.createKeyguardView(mContext, mUpdateMonitor, this);
            mKeyguardView.setId(0x1020222);
            mKeyguardView.setCallback(mCallback);
            obj = new android.widget.FrameLayout.LayoutParams(-1, -1);
            mKeyguardHost.addView(mKeyguardView, ((android.view.ViewGroup.LayoutParams) (obj)));
            if (mScreenOn)
                mKeyguardView.show();
        }
        mKeyguardHost.setSystemUiVisibility(0x600000);
        mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);
        mKeyguardHost.setVisibility(0);
        mKeyguardView.requestFocus();
        this;
        JVM INSTR monitorexit ;
        return;
_L4:
        obj1 = 0x800 | 0xfffeffff & (0x10110900 & 0xfffffeff);
          goto _L7
_L6:
        if (DEBUG)
            Log.d(TAG, "Rotation sensor for lock screen On!");
        mWindowLayoutParams.screenOrientation = 4;
          goto _L8
        obj;
        throw obj;
          goto _L7
    }

    /**
     * @deprecated Method verifyUnlock is deprecated
     */

    public void verifyUnlock()
    {
        this;
        JVM INSTR monitorenter ;
        if (DEBUG)
            Xlog.d(TAG, "verifyUnlock()");
        show();
        mKeyguardView.verifyUnlock();
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    public boolean wakeWhenReadyTq(int i)
    {
        Xlog.d(TAG, (new StringBuilder()).append("wakeWhenReady(").append(i).append(")").toString());
        boolean flag;
        if (mKeyguardView == null)
        {
            Xlog.w(TAG, "mKeyguardView is null in wakeWhenReadyTq");
            flag = false;
        } else
        {
            mKeyguardView.wakeWhenReadyTq(i);
            flag = true;
        }
        return flag;
    }









/*
    static int access$502(KeyguardViewManager keyguardviewmanager, int i)
    {
        keyguardviewmanager.mScrnOrientationModeBeforeShutdown = i;
        return i;
    }

*/

}
