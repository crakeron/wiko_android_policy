// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.*;
import android.security.KeyStore;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import com.android.internal.widget.*;
import java.util.List;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardScreen, KeyguardStatusViewManager, KeyguardUpdateMonitor, KeyguardScreenCallback

class PatternUnlockScreen extends LinearLayoutWithDefaultTouchRecepient
    implements KeyguardScreen
{
    private class UnlockPatternListener
        implements com.android.internal.widget.LockPatternView.OnPatternListener
    {

        final PatternUnlockScreen this$0;

        public void onPatternCellAdded(List list)
        {
            if (list.size() <= 2)
                mCallback.pokeWakelock(2000);
            else
                mCallback.pokeWakelock(7000);
        }

        public void onPatternCleared()
        {
        }

        public void onPatternDetected(List list)
        {
            if (!mLockPatternUtils.checkPattern(list))
            {
                boolean flag = false;
                if (list.size() > 2)
                    mCallback.pokeWakelock(7000);
                mLockPatternView.setDisplayMode(com.android.internal.widget.LockPatternView.DisplayMode.Wrong);
                if (list.size() >= 4)
                {
                    int i = 
// JavaClassFileOutputException: get_constant: invalid tag

        public void onPatternStart()
        {
            mLockPatternView.removeCallbacks(mCancelPatternRunnable);
        }

        private UnlockPatternListener()
        {
            this$0 = PatternUnlockScreen.this;
            super();
        }

    }

    static final class FooterMode extends Enum
    {

        private static final FooterMode $VALUES[];
        public static final FooterMode ForgotLockPattern;
        public static final FooterMode Normal;
        public static final FooterMode VerifyUnlocked;

        public static FooterMode valueOf(String s)
        {
            return (FooterMode)Enum.valueOf(com/android/internal/policy/impl/PatternUnlockScreen$FooterMode, s);
        }

        public static FooterMode[] values()
        {
            return (FooterMode[])$VALUES.clone();
        }

        static 
        {
            Normal = new FooterMode("Normal", 0);
            ForgotLockPattern = new FooterMode("ForgotLockPattern", 1);
            VerifyUnlocked = new FooterMode("VerifyUnlocked", 2);
            FooterMode afootermode[] = new FooterMode[3];
            afootermode[0] = Normal;
            afootermode[1] = ForgotLockPattern;
            afootermode[2] = VerifyUnlocked;
            $VALUES = afootermode;
        }

        private FooterMode(String s, int i)
        {
            super(s, i);
        }
    }


    private static final boolean DEBUG = false;
    private static final int MIN_PATTERN_BEFORE_POKE_WAKELOCK = 2;
    private static final int PATTERN_CLEAR_TIMEOUT_MS = 2000;
    private static final String TAG = "UnlockScreen";
    private static final int UNLOCK_PATTERN_WAKE_INTERVAL_FIRST_DOTS_MS = 2000;
    private static final int UNLOCK_PATTERN_WAKE_INTERVAL_MS = 7000;
    private KeyguardScreenCallback mCallback;
    private Runnable mCancelPatternRunnable;
    private CountDownTimer mCountdownTimer;
    private int mCreationOrientation;
    private boolean mEnableFallback;
    private int mFailedPatternAttemptsSinceLastTimeout;
    private Button mForgotPatternButton;
    private final android.view.View.OnClickListener mForgotPatternClick = new android.view.View.OnClickListener() {

        final PatternUnlockScreen this$0;

        public void onClick(View view)
        {
            mCallback.forgotPattern(true);
        }

            
            {
                this$0 = PatternUnlockScreen.this;
                super();
            }
    }
;
    private KeyguardStatusViewManager mKeyguardStatusViewManager;
    private long mLastPokeTime;
    private LockPatternUtils mLockPatternUtils;
    private LockPatternView mLockPatternView;
    private int mTotalFailedPatternAttempts;
    private KeyguardUpdateMonitor mUpdateMonitor;

    PatternUnlockScreen(Context context, Configuration configuration, LockPatternUtils lockpatternutils, KeyguardUpdateMonitor keyguardupdatemonitor, KeyguardScreenCallback keyguardscreencallback, int i)
    {
        LinearLayoutWithDefaultTouchRecepient(context);
        mFailedPatternAttemptsSinceLastTimeout = 0;
        mTotalFailedPatternAttempts = 0;
        mCountdownTimer = null;
        mLastPokeTime = -7000L;
        mCancelPatternRunnable = new Runnable() {

            final PatternUnlockScreen this$0;

            public void run()
            {
                mLockPatternView.clearPattern();
            }

            
            {
                this$0 = PatternUnlockScreen.this;
                super();
            }
        }
;
        mLockPatternUtils = lockpatternutils;
        mUpdateMonitor = keyguardupdatemonitor;
        mCallback = keyguardscreencallback;
        mTotalFailedPatternAttempts = i;
        mFailedPatternAttemptsSinceLastTimeout = i % 5;
        mCreationOrientation = configuration.orientation;
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        Object obj = context.getResources();
        boolean flag;
        if (!SystemProperties.getBoolean("lockscreen.rot_override", false) && !((Resources) (obj)).getBoolean(0x111001c))
            flag = false;
        else
            flag = true;
        if (!flag)
        {
            Log.d("UnlockScreen", "portrait mode");
            Log.i("UnlockScreen", "we will initialize the pattern portrait layout");
            layoutinflater.inflate(0x109005f, this, true);
        } else
        if (mCreationOrientation == 2)
        {
            Log.d("UnlockScreen", "landscape mode");
            Log.i("UnlockScreen", "we will initialize the pattern gemini land layout");
            layoutinflater.inflate(0x109005d, this, true);
        } else
        {
            Log.d("UnlockScreen", "portrait mode");
            Log.i("UnlockScreen", "we will initialize the pattern portrait layout");
            layoutinflater.inflate(0x109005f, this, true);
        }
        mKeyguardStatusViewManager = new KeyguardStatusViewManager(this, mUpdateMonitor, mLockPatternUtils, mCallback, true);
        mLockPatternView = (LockPatternView)findViewById(0x102029b);
        mForgotPatternButton = (Button)findViewById(0x10202b5);
        mForgotPatternButton.setText(0x1040301);
        mForgotPatternButton.setOnClickListener(mForgotPatternClick);
        setDefaultTouchRecepient(mLockPatternView);
        mLockPatternView.setSaveEnabled(false);
        mLockPatternView.setFocusable(false);
        mLockPatternView.setOnPatternListener(new UnlockPatternListener());
        flag = mLockPatternView;
        byte byte0;
        if (mLockPatternUtils.isVisiblePatternEnabled())
            byte0 = 0;
        else
            byte0 = 1;
        flag.setInStealthMode(byte0);
        mLockPatternView.setTactileFeedbackEnabled(mLockPatternUtils.isTactileFeedbackEnabled());
        if (!mUpdateMonitor.DM_IsLocked())
            byte0 = 0;
        else
            byte0 = 4;
        mLockPatternView.setVisibility(byte0);
        updateFooter(FooterMode.Normal);
        setFocusableInTouchMode(true);
    }

    private void handleAttemptLockout(long l)
    {
        mLockPatternView.clearPattern();
        mLockPatternView.setEnabled(false);
        mCountdownTimer = (new CountDownTimer(l - SystemClock.elapsedRealtime(), 1000L) {

            final PatternUnlockScreen this$0;

            public void onFinish()
            {
                mLockPatternView.setEnabled(true);
                mKeyguardStatusViewManager.setInstructionText(getContext().getString(0x10402df));
                mKeyguardStatusViewManager.updateStatusLines(true);
                mFailedPatternAttemptsSinceLastTimeout = 0;
                if (!mEnableFallback)
                    updateFooter(FooterMode.Normal);
                else
                    updateFooter(FooterMode.ForgotLockPattern);
            }

            public void onTick(long l1)
            {
                int i = (int)(l1 / 1000L);
                KeyguardStatusViewManager keyguardstatusviewmanager = mKeyguardStatusViewManager;
                Context context = getContext();
                Object aobj[] = new Object[1];
                aobj[0] = Integer.valueOf(i);
                keyguardstatusviewmanager.setInstructionText(context.getString(0x1040300, aobj));
                mKeyguardStatusViewManager.updateStatusLines(true);
            }

            
            {
                this$0 = PatternUnlockScreen.this;
                super(l, l1);
            }
        }
).start();
    }

    private void hideForgotPatternButton()
    {
        mForgotPatternButton.setVisibility(8);
    }

    private void showForgotPatternButton()
    {
        mForgotPatternButton.setVisibility(0);
    }

    private void updateFooter(FooterMode footermode)
    {
        class _cls4
        {

            static final int $SwitchMap$com$android$internal$policy$impl$PatternUnlockScreen$FooterMode[];

            static 
            {
                $SwitchMap$com$android$internal$policy$impl$PatternUnlockScreen$FooterMode = new int[FooterMode.values().length];
                try
                {
                    $SwitchMap$com$android$internal$policy$impl$PatternUnlockScreen$FooterMode[FooterMode.Normal.ordinal()] = 1;
                }
                catch (NoSuchFieldError _ex) { }
                try
                {
                    $SwitchMap$com$android$internal$policy$impl$PatternUnlockScreen$FooterMode[FooterMode.ForgotLockPattern.ordinal()] = 2;
                }
                catch (NoSuchFieldError _ex) { }
                $SwitchMap$com$android$internal$policy$impl$PatternUnlockScreen$FooterMode[FooterMode.VerifyUnlocked.ordinal()] = 3;
_L2:
                return;
                JVM INSTR pop ;
                if (true) goto _L2; else goto _L1
_L1:
            }
        }

        switch (_cls4..SwitchMap.com.android.internal.policy.impl.PatternUnlockScreen.FooterMode[footermode.ordinal()])
        {
        case 1: // '\001'
            hideForgotPatternButton();
            break;

        case 2: // '\002'
            showForgotPatternButton();
            break;

        case 3: // '\003'
            hideForgotPatternButton();
            break;
        }
    }

    public void cleanUp()
    {
        mUpdateMonitor.removeCallback(this);
        mLockPatternUtils = null;
        mUpdateMonitor = null;
        mCallback = null;
        mLockPatternView.setOnPatternListener(null);
    }

    public boolean dispatchTouchEvent(MotionEvent motionevent)
    {
        boolean flag = super.dispatchTouchEvent(motionevent);
        if (flag && SystemClock.elapsedRealtime() - mLastPokeTime > 6900L)
            mLastPokeTime = SystemClock.elapsedRealtime();
        return flag;
    }

    public boolean needsInput()
    {
        return false;
    }

    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (getResources().getConfiguration().orientation != mCreationOrientation)
            mCallback.recreateMe(getResources().getConfiguration());
    }

    protected void onConfigurationChanged(Configuration configuration)
    {
        super.onConfigurationChanged(configuration);
        if (configuration.orientation != mCreationOrientation)
            mCallback.recreateMe(configuration);
    }

    public void onKeyboardChange(boolean flag)
    {
    }

    public void onPause()
    {
        if (mCountdownTimer != null)
        {
            mCountdownTimer.cancel();
            mCountdownTimer = null;
        }
        mKeyguardStatusViewManager.onPause();
    }

    public void onResume()
    {
        mKeyguardStatusViewManager.onResume();
        mLockPatternView.enableInput();
        mLockPatternView.setEnabled(true);
        mLockPatternView.clearPattern();
        if (!mCallback.doesFallbackUnlockScreenExist())
            hideForgotPatternButton();
        else
            showForgotPatternButton();
        long l = mLockPatternUtils.getLockoutAttemptDeadline();
        if (l != 0L)
            handleAttemptLockout(l);
        if (!mCallback.isVerifyUnlockOnly())
        {
            if (!mEnableFallback || mTotalFailedPatternAttempts < 5)
                updateFooter(FooterMode.Normal);
            else
                updateFooter(FooterMode.ForgotLockPattern);
        } else
        {
            updateFooter(FooterMode.VerifyUnlocked);
        }
    }

    public void onWindowFocusChanged(boolean flag)
    {
        super.onWindowFocusChanged(flag);
        if (flag)
            onResume();
    }

    public void setEnableFallback(boolean flag)
    {
        mEnableFallback = flag;
    }








/*
    static int access$608(PatternUnlockScreen patternunlockscreen)
    {
        int i = patternunlockscreen.mTotalFailedPatternAttempts;
        patternunlockscreen.mTotalFailedPatternAttempts = i + 1;
        return i;
    }

*/



/*
    static int access$702(PatternUnlockScreen patternunlockscreen, int i)
    {
        patternunlockscreen.mFailedPatternAttemptsSinceLastTimeout = i;
        return i;
    }

*/


/*
    static int access$708(PatternUnlockScreen patternunlockscreen)
    {
        int i = patternunlockscreen.mFailedPatternAttemptsSinceLastTimeout;
        patternunlockscreen.mFailedPatternAttemptsSinceLastTimeout = i + 1;
        return i;
    }

*/


}
