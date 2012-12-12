// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import com.android.internal.widget.*;
import com.android.internal.widget.multiwaveview.MultiWaveView;
import com.mediatek.hdmi.HDMINative;
import java.io.File;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardScreen, KeyguardStatusViewManager, KeyguardUpdateMonitor, KeyguardScreenCallback

class LockScreen extends LinearLayout
    implements KeyguardScreen
{
    class WaveViewMethods
        implements com.android.internal.widget.WaveView.OnTriggerListener, UnlockWidgetCommonMethods
    {

        private final WaveView mWaveView;
        final LockScreen this$0;

        public View getView()
        {
            return mWaveView;
        }

        public void onGrabbedStateChange(View view, int i)
        {
            if (i == 10)
                mCallback.pokeWakelock(30000);
        }

        public void onTrigger(View view, int i)
        {
            Log.i("LockScreen", (new StringBuilder()).append("onTrigger, whichHandle=").append(i).toString());
            if (i == 10)
            {
                Log.i("LockScreen", "onTrigger, requestUnlockScreen");
                requestUnlockScreen();
            }
        }

        public void ping()
        {
        }

        public void reset(boolean flag)
        {
            mWaveView.reset();
        }

        public void updateResources()
        {
        }

        WaveViewMethods(WaveView waveview)
        {
            this$0 = LockScreen.this;
            Object();
            mWaveView = waveview;
        }
    }

    class SlidingTabMethods
        implements com.android.internal.widget.SlidingTab.OnTriggerListener, UnlockWidgetCommonMethods
    {

        private final SlidingTab mSlidingTab;
        final LockScreen this$0;

        public View getView()
        {
            return mSlidingTab;
        }

        public void onGrabbedStateChange(View view, int i)
        {
            if (i == 2)
            {
                mSilentMode = isSilentMode();
                SlidingTab slidingtab = mSlidingTab;
                int j;
                if (!mSilentMode)
                    j = 0x104030d;
                else
                    j = 0x104030c;
                slidingtab.setRightHintText(j);
            }
            if (i != 0)
                mCallback.pokeWakelock();
        }

        public void onTrigger(View view, int i)
        {
            if (i != 1)
            {
                if (i == 2)
                {
                    toggleRingMode();
                    mCallback.pokeWakelock();
                }
            } else
            {
                mCallback.goToUnlockScreen();
            }
        }

        public void ping()
        {
        }

        public void reset(boolean flag)
        {
            mSlidingTab.reset(flag);
        }

        public void updateResources()
        {
            int i = 1;
            if (!mSilentMode || mAudioManager.getRingerMode() != i)
                i = 0;
            SlidingTab slidingtab = mSlidingTab;
            int l;
            if (!mSilentMode)
                l = 0x10802ca;
            else
            if (i == 0)
                l = 0x10802c9;
            else
                l = 0x10802cc;
            int k;
            if (!mSilentMode)
                k = 0x1080396;
            else
                k = 0x1080399;
            if (!mSilentMode)
                i = 0x1080381;
            else
                i = 0x1080382;
            int j;
            if (!mSilentMode)
                j = 0x1080394;
            else
                j = 0x1080395;
            slidingtab.setRightTabResources(l, k, i, j);
        }

        SlidingTabMethods(SlidingTab slidingtab)
        {
            this$0 = LockScreen.this;
            Object();
            mSlidingTab = slidingtab;
        }
    }

    private static interface UnlockWidgetCommonMethods
    {

        public abstract View getView();

        public abstract void ping();

        public abstract void reset(boolean flag);

        public abstract void updateResources();
    }


    private static final boolean DBG = true;
    private static final String ENABLE_MENU_KEY_FILE = "/data/local/enable_menu_key";
    private static final int ON_RESUME_PING_DELAY = 500;
    private static final int STAY_ON_WHILE_GRABBED_TIMEOUT = 30000;
    private static final String TAG = "LockScreen";
    private static final int WAIT_FOR_ANIMATION_TIMEOUT;
    private AudioManager mAudioManager;
    private KeyguardScreenCallback mCallback;
    private int mCreationOrientation;
    private boolean mEnableMenuKeyInLockScreen;
    private HDMINative mHDMI;
    private int mKeyboardHidden;
    private LockPatternUtils mLockPatternUtils;
    private final Runnable mOnResumePing = new Runnable() {

        final LockScreen this$0;

        public void run()
        {
            mUnlockWidgetMethods.ping();
        }

            
            {
                this$0 = LockScreen.this;
                Object();
            }
    }
;
    private boolean mSilentMode;
    private KeyguardStatusViewManager mStatusViewManager;
    private View mUnlockWidget;
    private UnlockWidgetCommonMethods mUnlockWidgetMethods;
    private KeyguardUpdateMonitor mUpdateMonitor;

    LockScreen(Context context, Configuration configuration, LockPatternUtils lockpatternutils, KeyguardUpdateMonitor keyguardupdatemonitor, KeyguardScreenCallback keyguardscreencallback)
    {
        super(context);
        mHDMI = new HDMINative();
        mLockPatternUtils = lockpatternutils;
        mUpdateMonitor = keyguardupdatemonitor;
        mCallback = keyguardscreencallback;
        mEnableMenuKeyInLockScreen = shouldEnableMenuKey();
        mCreationOrientation = configuration.orientation;
        mKeyboardHidden = configuration.hardKeyboardHidden;
        Object obj = LayoutInflater.from(context);
        Log.v("LockScreen", (new StringBuilder()).append("Creation orientation = ").append(mCreationOrientation).toString());
        Log.i("LockScreen", "we will initialize the LockScreen single portrait layout");
        ((LayoutInflater) (obj)).inflate(0x109005a, this, true);
        mStatusViewManager = new KeyguardStatusViewManager(this, mUpdateMonitor, mLockPatternUtils, mCallback, false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(0x60000);
        mAudioManager = (AudioManager)mContext.getSystemService("audio");
        mSilentMode = isSilentMode();
        mUnlockWidget = findViewById(0x102029b);
        if (!(mUnlockWidget instanceof SlidingTab))
        {
            if (!(mUnlockWidget instanceof WaveView))
            {
                if (!(mUnlockWidget instanceof MultiWaveView))
                    throw new IllegalStateException((new StringBuilder()).append("Unrecognized unlock widget: ").append(mUnlockWidget).toString());
                MultiWaveView multiwaveview = (MultiWaveView)mUnlockWidget;
                obj = new MultiWaveViewMethods(multiwaveview);
                multiwaveview.setOnTriggerListener(((com.android.internal.widget.multiwaveview.MultiWaveView.OnTriggerListener) (obj)));
                mUnlockWidgetMethods = ((UnlockWidgetCommonMethods) (obj));
            } else
            {
                WaveView waveview = (WaveView)mUnlockWidget;
                obj = new WaveViewMethods(waveview);
                waveview.setOnTriggerListener(((com.android.internal.widget.WaveView.OnTriggerListener) (obj)));
                mUnlockWidgetMethods = ((UnlockWidgetCommonMethods) (obj));
            }
        } else
        {
            obj = (SlidingTab)mUnlockWidget;
            ((SlidingTab) (obj)).setHoldAfterTrigger(true, false);
            ((SlidingTab) (obj)).setLeftHintText(0x104030b);
            ((SlidingTab) (obj)).setLeftTabResources(0x10802cb, 0x1080397, 0x1080378, 0x108038b);
            SlidingTabMethods slidingtabmethods = new SlidingTabMethods(((SlidingTab) (obj)));
            ((SlidingTab) (obj)).setOnTriggerListener(slidingtabmethods);
            mUnlockWidgetMethods = slidingtabmethods;
        }
        mUnlockWidgetMethods.updateResources();
        if (mUpdateMonitor.DM_IsLocked())
        {
            Log.i("LockScreen", "we should hide unlock widget");
            mUnlockWidget.setVisibility(4);
        }
        obj = (new StringBuilder()).append("*** LockScreen accel is ");
        String s;
        if (!mUnlockWidget.isHardwareAccelerated())
            s = "off";
        else
            s = "on";
        Log.v("LockScreen", ((StringBuilder) (obj)).append(s).toString());
    }

    private boolean isSilentMode()
    {
        boolean flag;
        if (mAudioManager.getRingerMode() == 2)
            flag = false;
        else
            flag = true;
        return flag;
    }

    private void requestUnlockScreen()
    {
        postDelayed(new Runnable() {

            final LockScreen this$0;

            public void run()
            {
                mCallback.goToUnlockScreen();
            }

            
            {
                this$0 = LockScreen.this;
                Object();
            }
        }
, 0L);
    }

    private boolean shouldEnableMenuKey()
    {
        boolean flag1 = getResources().getBoolean(0x111001a);
        boolean flag = ActivityManager.isRunningInTestHarness();
        boolean flag2 = (new File("/data/local/enable_menu_key")).exists();
        if (flag1 && !flag && !flag2)
            flag = false;
        else
            flag = true;
        return flag;
    }

    private void toggleRingMode()
    {
        int i = 1;
        boolean flag;
        if (mSilentMode)
            flag = false;
        else
            flag = i;
        mSilentMode = flag;
        if (!mSilentMode)
        {
            mAudioManager.setRingerMode(2);
        } else
        {
            int j;
            if (android.provider.Settings.System.getInt(mContext.getContentResolver(), "vibrate_in_silent", i) != i)
                j = 0;
            else
                j = i;
            AudioManager audiomanager = mAudioManager;
            if (j == 0)
                i = 0;
            audiomanager.setRingerMode(i);
        }
    }

    public void cleanUp()
    {
        mUpdateMonitor.removeCallback(this);
        mLockPatternUtils = null;
        mUpdateMonitor = null;
        mCallback = null;
    }

    public boolean needsInput()
    {
        return false;
    }

    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        updateConfiguration();
    }

    protected void onConfigurationChanged(Configuration configuration)
    {
        super.onConfigurationChanged(configuration);
        updateConfiguration();
    }

    public boolean onKeyDown(int i, KeyEvent keyevent)
    {
        if (i == 82 && mEnableMenuKeyInLockScreen)
            mCallback.goToUnlockScreen();
        return false;
    }

    public void onPause()
    {
        mStatusViewManager.onPause();
        mUnlockWidgetMethods.reset(false);
        mHDMI.hdmiPortraitEnable(false);
    }

    public void onPhoneStateChanged(String s)
    {
    }

    public void onResume()
    {
        mStatusViewManager.onResume();
        postDelayed(mOnResumePing, 500L);
        mHDMI.hdmiPortraitEnable(true);
    }

    public void onRingerModeChanged(int i)
    {
        boolean flag;
        if (2 == i)
            flag = false;
        else
            flag = true;
        if (flag != mSilentMode)
        {
            mSilentMode = flag;
            mUnlockWidgetMethods.updateResources();
        }
    }

    void updateConfiguration()
    {
        int i = 1;
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == mCreationOrientation)
        {
            if (configuration.hardKeyboardHidden != mKeyboardHidden)
            {
                mKeyboardHidden = configuration.hardKeyboardHidden;
                if (mKeyboardHidden != i)
                    i = 0;
                if (mUpdateMonitor.isKeyguardBypassEnabled() && i != 0)
                    mCallback.goToUnlockScreen();
            }
        } else
        {
            mCallback.recreateMe(configuration);
        }
    }



/*
    static boolean access$002(LockScreen lockscreen, boolean flag)
    {
        lockscreen.mSilentMode = flag;
        return flag;
    }

*/








}
