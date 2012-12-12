// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.accounts.*;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.*;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.policy.IFaceLockCallback;
import com.android.internal.policy.IFaceLockInterface;
import com.android.internal.widget.*;
import com.mediatek.xlog.Xlog;
import java.io.IOException;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardViewBase, KeyguardUpdateMonitor, KeyguardScreenCallback, KeyguardScreen, 
//            KeyguardWindowController, LockScreen, PatternUnlockScreen, SimUnlockScreen, 
//            AccountUnlockScreen, PasswordUnlockScreen, KeyguardViewCallback

public class LockPatternKeyguardView extends KeyguardViewBase
    implements android.os.Handler.Callback, KeyguardUpdateMonitor.phoneStateCallback, KeyguardUpdateMonitor.deviceInfoCallback
{
    private static class FastBitmapDrawable extends Drawable
    {

        private Bitmap mBitmap;
        private int mOpacity;

        public void draw(Canvas canvas)
        {
            canvas.drawBitmap(mBitmap, (getBounds().width() - mBitmap.getWidth()) / 2, getBounds().height() - mBitmap.getHeight(), null);
        }

        public int getIntrinsicHeight()
        {
            return mBitmap.getHeight();
        }

        public int getIntrinsicWidth()
        {
            return mBitmap.getWidth();
        }

        public int getMinimumHeight()
        {
            return mBitmap.getHeight();
        }

        public int getMinimumWidth()
        {
            return mBitmap.getWidth();
        }

        public int getOpacity()
        {
            return mOpacity;
        }

        public void setAlpha(int i)
        {
        }

        public void setColorFilter(ColorFilter colorfilter)
        {
        }

        private FastBitmapDrawable(Bitmap bitmap)
        {
            mBitmap = bitmap;
            int i;
            if (!mBitmap.hasAlpha())
                i = -1;
            else
                i = -3;
            mOpacity = i;
        }
    }

    private class AccountAnalyzer
        implements AccountManagerCallback
    {

        private int mAccountIndex;
        private final AccountManager mAccountManager;
        private final Account mAccounts[];
        final LockPatternKeyguardView this$0;

        private void next()
        {
            if (!mEnableFallback && mAccountIndex < mAccounts.length)
                mAccountManager.confirmCredentials(mAccounts[mAccountIndex], null, null, this, null);
            else
            if (mUnlockScreen != null)
            {
                if (mUnlockScreen instanceof PatternUnlockScreen)
                    ((PatternUnlockScreen)mUnlockScreen).setEnableFallback(mEnableFallback);
            } else
            {
                Xlog.w("LockPatternKeyguardView", "no unlock screen when trying to enable fallback");
            }
        }

        public void run(AccountManagerFuture accountmanagerfuture)
        {
            if (((Bundle)accountmanagerfuture.getResult()).getParcelable("intent") != null)
                mEnableFallback = true;
            mAccountIndex = 1 + mAccountIndex;
            next();
_L2:
            return;
            JVM INSTR pop ;
            mAccountIndex = 1 + mAccountIndex;
            next();
            continue; /* Loop/switch isn't completed */
            JVM INSTR pop ;
            mAccountIndex = 1 + mAccountIndex;
            next();
            continue; /* Loop/switch isn't completed */
            JVM INSTR pop ;
            mAccountIndex = 1 + mAccountIndex;
            next();
            if (true) goto _L2; else goto _L1
_L1:
            Exception exception;
            exception;
            mAccountIndex = 1 + mAccountIndex;
            next();
            throw exception;
        }

        public void start()
        {
            mEnableFallback = false;
            mAccountIndex = 0;
            next();
        }

        private AccountAnalyzer(AccountManager accountmanager)
        {
            this$0 = LockPatternKeyguardView.this;
            super();
            mAccountManager = accountmanager;
            mAccounts = accountmanager.getAccountsByType("com.google");
        }

    }

    static final class UnlockMode extends Enum
    {

        private static final UnlockMode $VALUES[];
        public static final UnlockMode Account;
        public static final UnlockMode Password;
        public static final UnlockMode Pattern;
        public static final UnlockMode Sim2Pin;
        public static final UnlockMode SimPin;
        public static final UnlockMode Unknown;

        public static UnlockMode valueOf(String s)
        {
            return (UnlockMode)Enum.valueOf(com/android/internal/policy/impl/LockPatternKeyguardView$UnlockMode, s);
        }

        public static UnlockMode[] values()
        {
            return (UnlockMode[])$VALUES.clone();
        }

        static 
        {
            Pattern = new UnlockMode("Pattern", 0);
            SimPin = new UnlockMode("SimPin", 1);
            Sim2Pin = new UnlockMode("Sim2Pin", 2);
            Account = new UnlockMode("Account", 3);
            Password = new UnlockMode("Password", 4);
            Unknown = new UnlockMode("Unknown", 5);
            UnlockMode aunlockmode[] = new UnlockMode[6];
            aunlockmode[0] = Pattern;
            aunlockmode[1] = SimPin;
            aunlockmode[2] = Sim2Pin;
            aunlockmode[3] = Account;
            aunlockmode[4] = Password;
            aunlockmode[5] = Unknown;
            $VALUES = aunlockmode;
        }

        private UnlockMode(String s, int i)
        {
            super(s, i);
        }
    }

    static final class Mode extends Enum
    {

        private static final Mode $VALUES[];
        public static final Mode LockScreen;
        public static final Mode UnlockScreen;

        public static Mode valueOf(String s)
        {
            return (Mode)Enum.valueOf(com/android/internal/policy/impl/LockPatternKeyguardView$Mode, s);
        }

        public static Mode[] values()
        {
            return (Mode[])$VALUES.clone();
        }

        static 
        {
            LockScreen = new Mode("LockScreen", 0);
            UnlockScreen = new Mode("UnlockScreen", 1);
            Mode amode[] = new Mode[2];
            amode[0] = LockScreen;
            amode[1] = UnlockScreen;
            $VALUES = amode;
        }

        private Mode(String s, int i)
        {
            super(s, i);
        }
    }


    static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";
    private static final boolean DEBUG = false;
    static final boolean DEBUG_CONFIGURATION = false;
    private static final int EMERGENCY_CALL_TIMEOUT = 10000;
    private static final int FAILED_FACE_UNLOCK_ATTEMPTS_BEFORE_BACKUP = 15;
    private static final String TAG = "LockPatternKeyguardView";
    private static final int TRANSPORT_USERACTIVITY_TIMEOUT = 10000;
    private final int BACKUP_LOCK_TIMEOUT = 5000;
    private final int FACELOCK_VIEW_AREA_EMERGENCY_DIALER_TIMEOUT = 1000;
    private final int FACELOCK_VIEW_AREA_SERVICE_TIMEOUT = 3000;
    private final int MSG_HIDE_FACELOCK_AREA_VIEW = 1;
    private final int MSG_SHOW_FACELOCK_AREA_VIEW = 0;
    private boolean mBoundToFaceLockService;
    private Configuration mConfiguration;
    private Context mContext;
    private boolean mEnableFallback;
    private View mFaceLockAreaView;
    private final IFaceLockCallback mFaceLockCallback = new com.android.internal.policy.IFaceLockCallback.Stub() ;
    private ServiceConnection mFaceLockConnection;
    private IFaceLockInterface mFaceLockService;
    private boolean mFaceLockServiceRunning;
    private final Object mFaceLockServiceRunningLock = new Object();
    private final Object mFaceLockStartupLock = new Object();
    private int mFailedFaceUnlockAttempts;
    private boolean mForgotPattern;
    private Handler mHandler;
    private boolean mHasOverlay;
    private boolean mIsVerifyUnlockOnly;
    KeyguardScreenCallback mKeyguardScreenCallback;
    private final LockPatternUtils mLockPatternUtils;
    private View mLockScreen;
    private Mode mMode;
    private Runnable mRecreateRunnable;
    private boolean mRequiresSim;
    private Parcelable mSavedState;
    private volatile boolean mScreenOn;
    private boolean mShowLockBeforeUnlock;
    private TransportControlView mTransportControlView;
    private View mUnlockScreen;
    private UnlockMode mUnlockScreenMode;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private LockScreenWidgetCallback mWidgetCallback;
    private final KeyguardWindowController mWindowController;
    private volatile boolean mWindowFocused;

    public LockPatternKeyguardView(Context context, KeyguardUpdateMonitor keyguardupdatemonitor, LockPatternUtils lockpatternutils, KeyguardWindowController keyguardwindowcontroller)
    {
        super(context);
        mScreenOn = false;
        mWindowFocused = false;
        mEnableFallback = false;
        mShowLockBeforeUnlock = false;
        mBoundToFaceLockService = false;
        mFaceLockServiceRunning = false;
        mFailedFaceUnlockAttempts = 0;
        mMode = Mode.LockScreen;
        mUnlockScreenMode = UnlockMode.Unknown;
        mForgotPattern = false;
        mIsVerifyUnlockOnly = false;
        mRecreateRunnable = new Runnable() {

            final LockPatternKeyguardView this$0;

            public void run()
            {
                updateScreen(getInitialMode(), true);
                restoreWidgetState();
            }

            
            {
                this$0 = LockPatternKeyguardView.this;
                super();
            }
        }
;
        mWidgetCallback = new LockScreenWidgetCallback() {

            final LockPatternKeyguardView this$0;

            public boolean isVisible(View view)
            {
                boolean flag;
                if (view.getVisibility() != 0)
                    flag = false;
                else
                    flag = true;
                return flag;
            }

            public void requestHide(View view)
            {
                view.setVisibility(8);
                mUpdateMonitor.reportClockVisible(true);
                resetBackground();
            }

            public void requestShow(View view)
            {
                view.setVisibility(0);
                mUpdateMonitor.reportClockVisible(false);
                if (findViewById(0x10202a2) != null)
                    resetBackground();
                else
                    setBackgroundColor(0xff000000);
            }

            public void userActivity(View view)
            {
                mKeyguardScreenCallback.pokeWakelock(10000);
            }

            
            {
                this$0 = LockPatternKeyguardView.this;
                super();
            }
        }
;
        mFaceLockConnection = new ServiceConnection() {

            final LockPatternKeyguardView this$0;

            public void onServiceConnected(ComponentName componentname, IBinder ibinder)
            {
                mFaceLockService = com.android.internal.policy.IFaceLockInterface.Stub.asInterface(ibinder);
                mFaceLockService.registerCallback(mFaceLockCallback);
                if (mFaceLockAreaView != null)
                    startFaceLock(mFaceLockAreaView.getWindowToken(), mFaceLockAreaView.getLeft(), mFaceLockAreaView.getTop(), mFaceLockAreaView.getWidth(), mFaceLockAreaView.getHeight());
_L2:
                return;
                RemoteException remoteexception;
                remoteexception;
                Log.e("LockPatternKeyguardView", (new StringBuilder()).append("Caught exception connecting to FaceLock: ").append(remoteexception.toString()).toString());
                mFaceLockService = null;
                mBoundToFaceLockService = false;
                if (true) goto _L2; else goto _L1
_L1:
            }

            public void onServiceDisconnected(ComponentName componentname)
            {
                synchronized (mFaceLockServiceRunningLock)
                {
                    mFaceLockService = null;
                    mFaceLockServiceRunning = false;
                }
                mBoundToFaceLockService = false;
                Log.w("LockPatternKeyguardView", "Unexpected disconnect from FaceLock service");
                return;
                exception;
                obj;
                JVM INSTR monitorexit ;
                throw exception;
            }

            
            {
                this$0 = LockPatternKeyguardView.this;
                super();
            }
        }
;
        mContext = context;
        mHandler = new Handler(this);
        mConfiguration = context.getResources().getConfiguration();
        mEnableFallback = false;
        mRequiresSim = TextUtils.isEmpty(SystemProperties.get("keyguard.no_require_sim"));
        mUpdateMonitor = keyguardupdatemonitor;
        mLockPatternUtils = lockpatternutils;
        mWindowController = keyguardwindowcontroller;
        mHasOverlay = false;
        mUpdateMonitor.registerDeviceInfoCallback(this);
        mUpdateMonitor.registerPhoneStateCallback(this);
        mKeyguardScreenCallback = new KeyguardScreenCallback() {

            final LockPatternKeyguardView this$0;

            public void doKeyguardLocked()
            {
            }

            public boolean doesFallbackUnlockScreenExist()
            {
                return mEnableFallback;
            }

            public void forgotPattern(boolean flag)
            {
                if (mEnableFallback)
                {
                    mForgotPattern = flag;
                    updateScreen(Mode.UnlockScreen, false);
                }
            }

            public void goToLockScreen()
            {
                mForgotPattern = false;
                if (!mIsVerifyUnlockOnly)
                {
                    updateScreen(Mode.LockScreen, false);
                } else
                {
                    mIsVerifyUnlockOnly = false;
                    getCallback().keyguardDone(false);
                }
            }

            public void goToUnlockScreen()
            {
                com.android.internal.telephony.IccCard.State state1 = mUpdateMonitor.getSimState(0);
                com.android.internal.telephony.IccCard.State state = mUpdateMonitor.getSimState(1);
                if (!stuckOnLockScreenBecauseSimMissing() && (state1 != com.android.internal.telephony.IccCard.State.PUK_REQUIRED || mLockPatternUtils.isPukUnlockScreenEnable()) && (state != com.android.internal.telephony.IccCard.State.PUK_REQUIRED || mLockPatternUtils.isPukUnlockScreenEnable()))
                    if (isSecure())
                        updateScreen(Mode.UnlockScreen, false);
                    else
                        getCallback().keyguardDone(true);
            }

            public boolean isSecure()
            {
                return LockPatternKeyguardView.this.isSecure();
            }

            public boolean isVerifyUnlockOnly()
            {
                return mIsVerifyUnlockOnly;
            }

            public void keyguardDone(boolean flag)
            {
                getCallback().keyguardDone(flag);
                mSavedState = null;
            }

            public void keyguardDoneDrawing()
            {
            }

            public void pokeWakelock()
            {
                getCallback().pokeWakelock();
            }

            public void pokeWakelock(int i)
            {
                getCallback().pokeWakelock(i);
            }

            public void recreateMe(Configuration configuration)
            {
                removeCallbacks(mRecreateRunnable);
                post(mRecreateRunnable);
            }

            public void reportFailedUnlockAttempt()
            {
                mUpdateMonitor.reportFailedAttempt();
                int i = mUpdateMonitor.getFailedAttempts();
                boolean flag;
                if (mLockPatternUtils.getKeyguardStoredPasswordQuality() != 0x10000)
                    flag = false;
                else
                    flag = true;
                int j = mLockPatternUtils.getDevicePolicyManager().getMaximumFailedPasswordsForWipe(null);
                if (j <= 0)
                    j = 0x7fffffff;
                else
                    j -= i;
                if (j >= 5)
                {
                    if (i % 5 != 0)
                        j = 0;
                    else
                        j = 1;
                    if (flag && mEnableFallback)
                        if (i != 15)
                        {
                            if (i >= 20)
                            {
                                mLockPatternUtils.setPermanentlyLocked(true);
                                updateScreen(mMode, false);
                                j = 0;
                            }
                        } else
                        {
                            showAlmostAtAccountLoginDialog();
                            j = 0;
                        }
                    if (j != 0)
                        showTimeoutDialog();
                } else
                if (j <= 0)
                {
                    Slog.i("LockPatternKeyguardView", "Too many unlock attempts; device will be wiped!");
                    showWipeDialog(i);
                } else
                {
                    showAlmostAtWipeDialog(i, j);
                }
                mLockPatternUtils.reportFailedPasswordAttempt();
            }

            public void reportSuccessfulUnlockAttempt()
            {
                mFailedFaceUnlockAttempts = 0;
                mLockPatternUtils.reportSuccessfulPasswordAttempt();
            }

            public void takeEmergencyCallAction()
            {
                mHasOverlay = true;
                if (usingFaceLock() && mFaceLockServiceRunning)
                    showFaceLockAreaWithTimeout(1000L);
                stopAndUnbindFromFaceLock();
                pokeWakelock(10000);
                if (TelephonyManager.getDefault().getCallState() != 2)
                {
                    Intent intent = new Intent("com.android.phone.EmergencyDialer.DIAL");
                    intent.setFlags(0x10800000);
                    getContext().startActivity(intent);
                } else
                {
                    mLockPatternUtils.resumeCall();
                }
            }

            
            {
                this$0 = LockPatternKeyguardView.this;
                super();
            }
        }
;
        setFocusableInTouchMode(true);
        setDescendantFocusability(0x40000);
        updateScreen(getInitialMode(), false);
        maybeEnableFallback(context);
    }

    private boolean IsSIMPINRequired()
    {
        boolean flag = false;
        com.android.internal.telephony.IccCard.State _tmp = com.android.internal.telephony.IccCard.State.NOT_READY;
        com.android.internal.telephony.IccCard.State state1 = mUpdateMonitor.getSimState(0);
        com.android.internal.telephony.IccCard.State state = mUpdateMonitor.getSimState(1);
        if ((state1 != com.android.internal.telephony.IccCard.State.PIN_REQUIRED || mUpdateMonitor.getPINDismissFlag(0, true)) && (state1 != com.android.internal.telephony.IccCard.State.PUK_REQUIRED || mUpdateMonitor.getPINDismissFlag(0, false)))
        {
            if (state == com.android.internal.telephony.IccCard.State.PIN_REQUIRED && !mUpdateMonitor.getPINDismissFlag(1, true) || state == com.android.internal.telephony.IccCard.State.PUK_REQUIRED && !mUpdateMonitor.getPINDismissFlag(1, false))
                flag = true;
        } else
        {
            flag = true;
        }
        return flag;
    }

    private void activateFaceLockIfAble()
    {
        boolean flag;
        if (mFailedFaceUnlockAttempts < 15)
            flag = false;
        else
            flag = true;
        boolean flag1;
        if (mUpdateMonitor.getFailedAttempts() < 5)
            flag1 = false;
        else
            flag1 = true;
        if (flag)
            Log.i("LockPatternKeyguardView", (new StringBuilder()).append("tooManyFaceUnlockTries: ").append(flag).toString());
        if (mUpdateMonitor.getPhoneState() != 0 || !usingFaceLock() || mHasOverlay || flag || flag1)
        {
            hideFaceLockArea();
        } else
        {
            bindToFaceLock();
            if (usingFaceLock())
                showFaceLockAreaWithTimeout(3000L);
            showFaceLockAreaWithTimeout(3000L);
            mKeyguardScreenCallback.pokeWakelock();
            if (mLockPatternUtils.usingBiometricWeak() && mLockPatternUtils.isBiometricWeakInstalled())
                showFaceLockAreaWithTimeout(3000L);
        }
    }

    private Mode getInitialMode()
    {
        Mode mode;
        if (!stuckOnLockScreenBecauseSimMissing())
        {
            if (isSecure() && !mShowLockBeforeUnlock)
                mode = Mode.UnlockScreen;
            else
                mode = Mode.LockScreen;
        } else
        {
            mode = Mode.LockScreen;
        }
        return mode;
    }

    private UnlockMode getUnlockMode()
    {
        Object obj = mUpdateMonitor.getSimState(0);
        com.android.internal.telephony.IccCard.State state = mUpdateMonitor.getSimState(1);
        UnlockMode _tmp = UnlockMode.Unknown;
        if ((obj != com.android.internal.telephony.IccCard.State.PIN_REQUIRED || mUpdateMonitor.getPINDismissFlag(0, true)) && (obj != com.android.internal.telephony.IccCard.State.PUK_REQUIRED || mUpdateMonitor.getPINDismissFlag(0, false)))
        {
            if ((state != com.android.internal.telephony.IccCard.State.PIN_REQUIRED || mUpdateMonitor.getPINDismissFlag(1, true)) && (state != com.android.internal.telephony.IccCard.State.PUK_REQUIRED || mUpdateMonitor.getPINDismissFlag(1, false)))
            {
                int i = mLockPatternUtils.getKeyguardStoredPasswordQuality();
                switch (i)
                {
                default:
                    throw new IllegalStateException((new StringBuilder()).append("Unknown unlock mode:").append(i).toString());

                case 0: // '\0'
                case 65536: 
                    if (!mForgotPattern && !mLockPatternUtils.isPermanentlyLocked())
                        i = UnlockMode.Pattern;
                    else
                        i = com.android.internal.policy.impl.UnlockMode.Account;
                    break;

                case 131072: 
                case 262144: 
                case 327680: 
                case 393216: 
                    i = UnlockMode.Password;
                    break;
                }
            } else
            {
                i = UnlockMode.Sim2Pin;
            }
        } else
        {
            i = UnlockMode.SimPin;
        }
        Xlog.i("LockPatternKeyguardView", (new StringBuilder()).append("getUnlockMode, Mode=").append(i).toString());
        return i;
    }

    private void hideFaceLockArea()
    {
        removeFaceLockAreaDisplayMessages();
        mHandler.sendEmptyMessage(1);
    }

    private void initializeFaceLockAreaView(View view)
    {
        if (!usingFaceLock())
        {
            mFaceLockAreaView = null;
        } else
        {
            mFaceLockAreaView = view.findViewById(0x10202a1);
            if (mFaceLockAreaView != null)
            {
                Xlog.i("LockPatternKeyguardView", (new StringBuilder()).append("we want to show the faceunlock view, mHasOverlay = ").append(mHasOverlay).toString());
                if (mHasOverlay)
                    hideFaceLockArea();
                else
                    showFaceLockArea();
                if (mBoundToFaceLockService)
                {
                    stopAndUnbindFromFaceLock();
                    activateFaceLockIfAble();
                }
            } else
            {
                Log.e("LockPatternKeyguardView", "Layout does not have faceLockAreaView and FaceLock is enabled");
            }
        }
    }

    private void initializeTransportControlView(View view)
    {
        mTransportControlView = (TransportControlView)view.findViewById(0x10202a0);
        if (mTransportControlView != null)
        {
            mUpdateMonitor.reportClockVisible(true);
            mTransportControlView.setVisibility(8);
            mTransportControlView.setCallback(mWidgetCallback);
        }
    }

    private boolean isSecure()
    {
        UnlockMode unlockmode = getUnlockMode();
        class _cls6
        {

            static final int $SwitchMap$com$android$internal$policy$impl$LockPatternKeyguardView$UnlockMode[];

            static 
            {
                $SwitchMap$com$android$internal$policy$impl$LockPatternKeyguardView$UnlockMode = new int[UnlockMode.values().length];
                try
                {
                    $SwitchMap$com$android$internal$policy$impl$LockPatternKeyguardView$UnlockMode[UnlockMode.Pattern.ordinal()] = 1;
                }
                catch (NoSuchFieldError _ex) { }
                try
                {
                    $SwitchMap$com$android$internal$policy$impl$LockPatternKeyguardView$UnlockMode[UnlockMode.SimPin.ordinal()] = 2;
                }
                catch (NoSuchFieldError _ex) { }
                try
                {
                    $SwitchMap$com$android$internal$policy$impl$LockPatternKeyguardView$UnlockMode[UnlockMode.Sim2Pin.ordinal()] = 3;
                }
                catch (NoSuchFieldError _ex) { }
                try
                {
                    $SwitchMap$com$android$internal$policy$impl$LockPatternKeyguardView$UnlockMode[com.android.internal.policy.impl.UnlockMode.Account.ordinal()] = 4;
                }
                catch (NoSuchFieldError _ex) { }
                $SwitchMap$com$android$internal$policy$impl$LockPatternKeyguardView$UnlockMode[UnlockMode.Password.ordinal()] = 5;
_L2:
                return;
                JVM INSTR pop ;
                if (true) goto _L2; else goto _L1
_L1:
            }
        }

        boolean flag;
        switch (_cls6..SwitchMap.com.android.internal.policy.impl.LockPatternKeyguardView.UnlockMode[unlockmode.ordinal()])
        {
        default:
            throw new IllegalStateException((new StringBuilder()).append("unknown unlock mode ").append(unlockmode).toString());

        case 1: // '\001'
            flag = mLockPatternUtils.isLockPatternEnabled();
            Log.i("LockPatternKeyguardView", (new StringBuilder()).append("isSecure, Pattern, secure=").append(flag).toString());
            break;

        case 2: // '\002'
            if ((mUpdateMonitor.getSimState(0) != com.android.internal.telephony.IccCard.State.PIN_REQUIRED || mUpdateMonitor.getPINDismissFlag(0, true)) && (mUpdateMonitor.getSimState(0) != com.android.internal.telephony.IccCard.State.PUK_REQUIRED || mUpdateMonitor.getPINDismissFlag(0, false)))
                flag = false;
            else
                flag = true;
            break;

        case 3: // '\003'
            if ((mUpdateMonitor.getSimState(1) != com.android.internal.telephony.IccCard.State.PIN_REQUIRED || mUpdateMonitor.getPINDismissFlag(1, true)) && (mUpdateMonitor.getSimState(1) != com.android.internal.telephony.IccCard.State.PUK_REQUIRED || mUpdateMonitor.getPINDismissFlag(1, false)))
                flag = false;
            else
                flag = true;
            break;

        case 4: // '\004'
            flag = true;
            break;

        case 5: // '\005'
            flag = mLockPatternUtils.isLockPasswordEnabled();
            break;
        }
        return flag;
    }

    private void maybeEnableFallback(Context context)
    {
        (new AccountAnalyzer(AccountManager.get(context))).start();
    }

    private void recreateLockScreen()
    {
        Log.i("LockPatternKeyguardView", "recreateLockScreen");
        if (mLockScreen != null)
        {
            ((KeyguardScreen)mLockScreen).onPause();
            ((KeyguardScreen)mLockScreen).cleanUp();
            removeView(mLockScreen);
        }
        mLockScreen = createLockScreen();
        mLockScreen.setVisibility(4);
        addView(mLockScreen);
    }

    private void recreateUnlockScreen(UnlockMode unlockmode)
    {
        Log.i("LockPatternKeyguardView", (new StringBuilder()).append("recreateUnlockScreen, unlockMode=").append(unlockmode).toString());
        if (mUnlockScreen != null)
        {
            ((KeyguardScreen)mUnlockScreen).onPause();
            ((KeyguardScreen)mUnlockScreen).cleanUp();
            removeView(mUnlockScreen);
        }
        mUnlockScreen = createUnlockScreenFor(unlockmode);
        mUnlockScreen.setVisibility(4);
        addView(mUnlockScreen);
    }

    private void removeFaceLockAreaDisplayMessages()
    {
        mHandler.removeMessages(0);
        mHandler.removeMessages(1);
    }

    private void restoreWidgetState()
    {
        if (mTransportControlView != null && mSavedState != null)
            mTransportControlView.onRestoreInstanceState(mSavedState);
    }

    private void saveWidgetState()
    {
        if (mTransportControlView != null)
            mSavedState = mTransportControlView.onSaveInstanceState();
    }

    private void showAlmostAtAccountLoginDialog()
    {
        Context context = mContext;
        Object aobj[] = new Object[3];
        aobj[0] = Integer.valueOf(15);
        aobj[1] = Integer.valueOf(5);
        aobj[2] = Integer.valueOf(30);
        showDialog(null, context.getString(0x10402fd, aobj));
    }

    private void showAlmostAtWipeDialog(int i, int j)
    {
        Context context = mContext;
        Object aobj[] = new Object[2];
        aobj[0] = Integer.valueOf(i);
        aobj[1] = Integer.valueOf(j);
        showDialog(null, context.getString(0x10402fe, aobj));
    }

    private void showDialog(String s, String s1)
    {
        AlertDialog alertdialog = (new android.app.AlertDialog.Builder(mContext)).setTitle(s).setMessage(s1).setNeutralButton(0x104000a, null).create();
        alertdialog.getWindow().setType(2009);
        alertdialog.show();
    }

    private void showFaceLockArea()
    {
        removeFaceLockAreaDisplayMessages();
        mHandler.sendEmptyMessage(0);
    }

    private void showFaceLockAreaWithTimeout(long l)
    {
        showFaceLockArea();
        mHandler.sendEmptyMessageDelayed(1, l);
    }

    private void showTimeoutDialog()
    {
        int i = 0x10402fa;
        if (getUnlockMode() == UnlockMode.Password)
            if (mLockPatternUtils.getKeyguardStoredPasswordQuality() != 0x20000)
                i = 0x10402fb;
            else
                i = 0x10402fc;
        Context context = mContext;
        Object aobj[] = new Object[2];
        aobj[0] = Integer.valueOf(mUpdateMonitor.getFailedAttempts());
        aobj[1] = Integer.valueOf(30);
        showDialog(null, context.getString(i, aobj));
    }

    private void showWipeDialog(int i)
    {
        Context context = mContext;
        Object aobj[] = new Object[1];
        aobj[0] = Integer.valueOf(i);
        showDialog(null, context.getString(0x10402ff, aobj));
    }

    private boolean stopFaceLockIfRunning()
    {
        boolean flag;
        if (!usingFaceLock() || !mBoundToFaceLockService)
        {
            flag = false;
        } else
        {
            stopAndUnbindFromFaceLock();
            flag = true;
        }
        return flag;
    }

    private boolean stuckOnLockScreenBecauseSimMissing()
    {
        boolean flag = true;
        if (!mRequiresSim || mUpdateMonitor.isDeviceProvisioned() || mUpdateMonitor.getSimState(0) != com.android.internal.telephony.IccCard.State.ABSENT || mUpdateMonitor.getSimState(flag) != com.android.internal.telephony.IccCard.State.ABSENT)
            flag = false;
        return flag;
    }

    private void updateScreen(Mode mode, boolean flag)
    {
        Log.v("LockPatternKeyguardView", (new StringBuilder()).append("**** UPDATE SCREEN: mode=").append(mode).append(" last mode=").append(mMode).append(", force = ").append(flag).toString());
        mMode = mode;
        if ((mode == Mode.LockScreen || mShowLockBeforeUnlock) && (flag || mLockScreen == null))
            recreateLockScreen();
        if (mode == Mode.UnlockScreen)
        {
            UnlockMode unlockmode = getUnlockMode();
            if (flag || mUnlockScreen == null || unlockmode != mUnlockScreenMode)
            {
                boolean flag1 = stopFaceLockIfRunning();
                recreateUnlockScreen(unlockmode);
                if (flag1)
                    activateFaceLockIfAble();
            }
        }
        View view1;
        if (mode != Mode.LockScreen)
            view1 = mLockScreen;
        else
            view1 = mUnlockScreen;
        View view;
        if (mode != Mode.LockScreen)
            view = mUnlockScreen;
        else
            view = mLockScreen;
        mWindowController.setNeedsInput(((KeyguardScreen)view).needsInput());
        if (mScreenOn)
        {
            if (view1 != null && view1.getVisibility() == 0)
                ((KeyguardScreen)view1).onPause();
            if (view.getVisibility() != 0)
                ((KeyguardScreen)view).onResume();
        }
        if (view1 != null)
            view1.setVisibility(8);
        view.setVisibility(0);
        requestLayout();
        if (view.requestFocus())
            return;
        else
            throw new IllegalStateException((new StringBuilder()).append("keyguard screen must be able to take focus when shown ").append(view.getClass().getCanonicalName()).toString());
    }

    private boolean usingFaceLock()
    {
        boolean flag;
        if (!mLockPatternUtils.usingBiometricWeak() || !mLockPatternUtils.isBiometricWeakInstalled())
            flag = false;
        else
            flag = true;
        return flag;
    }

    public void bindToFaceLock()
    {
        if (usingFaceLock())
            if (mBoundToFaceLockService)
            {
                Log.w("LockPatternKeyguardView", "Attempt to bind to FaceLock when already bound");
            } else
            {
                mContext.bindService(new Intent(com/android/internal/policy/IFaceLockInterface.getName()), mFaceLockConnection, 1);
                mBoundToFaceLockService = true;
            }
    }

    public void cleanUp()
    {
        if (mLockScreen != null)
        {
            ((KeyguardScreen)mLockScreen).onPause();
            ((KeyguardScreen)mLockScreen).cleanUp();
            removeView(mLockScreen);
            mLockScreen = null;
        }
        if (mUnlockScreen != null)
        {
            ((KeyguardScreen)mUnlockScreen).onPause();
            ((KeyguardScreen)mUnlockScreen).cleanUp();
            removeView(mUnlockScreen);
            mUnlockScreen = null;
        }
        mUpdateMonitor.removeCallback(this);
        if (mFaceLockService != null)
        {
            try
            {
                mFaceLockService.unregisterCallback(mFaceLockCallback);
            }
            catch (RemoteException _ex) { }
            stopFaceLock();
            mFaceLockService = null;
        }
    }

    View createLockScreen()
    {
        LockScreen lockscreen = new LockScreen(mContext, mConfiguration, mLockPatternUtils, mUpdateMonitor, mKeyguardScreenCallback);
        initializeTransportControlView(lockscreen);
        return lockscreen;
    }

    View createUnlockScreenFor(UnlockMode unlockmode)
    {
        if (unlockmode != UnlockMode.Pattern) goto _L2; else goto _L1
_L1:
        Object obj;
        obj = new PatternUnlockScreen(mContext, mConfiguration, mLockPatternUtils, mUpdateMonitor, mKeyguardScreenCallback, mUpdateMonitor.getFailedAttempts());
        ((PatternUnlockScreen) (obj)).setEnableFallback(mEnableFallback);
        obj = obj;
_L6:
        initializeTransportControlView(((View) (obj)));
        initializeFaceLockAreaView(((View) (obj)));
        mUnlockScreenMode = unlockmode;
        obj = obj;
_L4:
        return ((View) (obj));
_L2:
        if (unlockmode == UnlockMode.SimPin)
        {
            obj = new SimUnlockScreen(mContext, mConfiguration, mUpdateMonitor, mKeyguardScreenCallback, mLockPatternUtils, 0);
            continue; /* Loop/switch isn't completed */
        }
        if (unlockmode == UnlockMode.Sim2Pin)
        {
            obj = new SimUnlockScreen(mContext, mConfiguration, mUpdateMonitor, mKeyguardScreenCallback, mLockPatternUtils, 1);
            continue; /* Loop/switch isn't completed */
        }
        if (unlockmode != com.android.internal.policy.impl.UnlockMode.Account)
            break; /* Loop/switch isn't completed */
        obj = new AccountUnlockScreen(mContext, mConfiguration, mUpdateMonitor, mKeyguardScreenCallback, mLockPatternUtils);
        obj = obj;
        continue; /* Loop/switch isn't completed */
        JVM INSTR pop ;
        Log.i("LockPatternKeyguardView", "Couldn't instantiate AccountUnlockScreen (IAccountsService isn't available)");
        obj = createUnlockScreenFor(UnlockMode.Pattern);
        if (true) goto _L4; else goto _L3
_L3:
        if (unlockmode == UnlockMode.Password)
        {
            Context context = mContext;
            Configuration configuration = mConfiguration;
            obj = mLockPatternUtils;
            KeyguardUpdateMonitor keyguardupdatemonitor = mUpdateMonitor;
            KeyguardScreenCallback keyguardscreencallback = mKeyguardScreenCallback;
            obj = new PasswordUnlockScreen(context, configuration, ((LockPatternUtils) (obj)), keyguardupdatemonitor, keyguardscreencallback);
        } else
        {
            throw new IllegalArgumentException((new StringBuilder()).append("unknown unlock mode ").append(unlockmode).toString());
        }
        if (true) goto _L6; else goto _L5
_L5:
    }

    protected void dispatchDraw(Canvas canvas)
    {
        super.dispatchDraw(canvas);
    }

    protected boolean dispatchHoverEvent(MotionEvent motionevent)
    {
        AccessibilityManager accessibilitymanager = AccessibilityManager.getInstance(mContext);
        if (accessibilitymanager.isEnabled() && accessibilitymanager.isTouchExplorationEnabled())
            getCallback().pokeWakelock();
        return super.dispatchHoverEvent(motionevent);
    }

    public boolean handleMessage(Message message)
    {
        boolean flag = false;
        message.what;
        JVM INSTR tableswitch 0 1: default 28
    //                   0 40
    //                   1 58;
           goto _L1 _L2 _L3
_L1:
        Log.w("LockPatternKeyguardView", "Unhandled message");
          goto _L4
_L2:
        if (mFaceLockAreaView != null)
            mFaceLockAreaView.setVisibility(0);
          goto _L5
_L3:
        if (mFaceLockAreaView != null)
            mFaceLockAreaView.setVisibility(4);
_L5:
        flag = true;
_L4:
        return flag;
    }

    public void onClockVisibilityChanged()
    {
        int j = 0xff7fffff & getSystemUiVisibility();
        int i;
        if (!mUpdateMonitor.isClockVisible())
            i = 0;
        else
            i = 0x800000;
        setSystemUiVisibility(i | j);
    }

    protected void onConfigurationChanged(Configuration configuration)
    {
        mShowLockBeforeUnlock = getResources().getBoolean(0x111001b);
        mConfiguration = configuration;
        saveWidgetState();
        removeCallbacks(mRecreateRunnable);
        post(mRecreateRunnable);
    }

    public void onDMKeyguardUpdate()
    {
    }

    protected void onDetachedFromWindow()
    {
        removeCallbacks(mRecreateRunnable);
        stopAndUnbindFromFaceLock();
        super.onDetachedFromWindow();
    }

    public void onDeviceProvisioned()
    {
    }

    public void onPhoneStateChanged(int i)
    {
        if (i == 1)
        {
            mHasOverlay = true;
            stopAndUnbindFromFaceLock();
            hideFaceLockArea();
        }
    }

    public void onScreenTurnedOff()
    {
        boolean flag = false;
        mScreenOn = false;
        mForgotPattern = false;
        if (mUpdateMonitor.getPhoneState() != 0)
            flag = true;
        mHasOverlay = flag;
        if (mMode != Mode.LockScreen)
            ((KeyguardScreen)mUnlockScreen).onPause();
        else
            ((KeyguardScreen)mLockScreen).onPause();
        saveWidgetState();
        stopAndUnbindFromFaceLock();
    }

    public void onScreenTurnedOn()
    {
        boolean flag;
        synchronized (mFaceLockStartupLock)
        {
            mScreenOn = true;
            flag = mWindowFocused;
        }
        show();
        restoreWidgetState();
        if (flag)
            activateFaceLockIfAble();
        return;
        exception;
        obj;
        JVM INSTR monitorexit ;
        throw exception;
    }

    public void onWindowFocusChanged(boolean flag)
    {
        obj1 = 0;
        synchronized (mFaceLockStartupLock)
        {
            if (mScreenOn && !mWindowFocused)
                obj1 = flag;
            mWindowFocused = flag;
        }
        if (flag) goto _L2; else goto _L1
_L1:
        mHasOverlay = true;
        stopAndUnbindFromFaceLock();
        hideFaceLockArea();
_L4:
        return;
        obj1;
        obj;
        JVM INSTR monitorexit ;
        throw obj1;
_L2:
        if (obj1 != 0)
            activateFaceLockIfAble();
        if (true) goto _L4; else goto _L3
_L3:
    }

    public void reset()
    {
        mIsVerifyUnlockOnly = false;
        mForgotPattern = false;
        updateScreen(getInitialMode(), false);
        restoreWidgetState();
    }

    public void show()
    {
        if (mMode != Mode.LockScreen)
            ((KeyguardScreen)mUnlockScreen).onResume();
        else
            ((KeyguardScreen)mLockScreen).onResume();
        if (!usingFaceLock() || mHasOverlay)
            hideFaceLockArea();
        else
            showFaceLockArea();
    }

    public void startFaceLock(IBinder ibinder, int i, int j, int k, int l)
    {
        if (!usingFaceLock())
            break MISSING_BLOCK_LABEL_100;
        Object obj = mFaceLockServiceRunningLock;
        obj;
        JVM INSTR monitorenter ;
        boolean flag = mFaceLockServiceRunning;
        if (flag)
            break MISSING_BLOCK_LABEL_48;
        try
        {
            mFaceLockService.startUi(ibinder, i, j, k, l);
        }
        catch (RemoteException remoteexception)
        {
            Log.e("LockPatternKeyguardView", (new StringBuilder()).append("Caught exception starting FaceLock: ").append(remoteexception.toString()).toString());
            break MISSING_BLOCK_LABEL_100;
        }
        mFaceLockServiceRunning = true;
        obj;
        JVM INSTR monitorexit ;
        break MISSING_BLOCK_LABEL_100;
        Exception exception;
        exception;
        throw exception;
    }

    public void stopAndUnbindFromFaceLock()
    {
        if (usingFaceLock())
        {
            stopFaceLock();
            if (mBoundToFaceLockService)
            {
                if (mFaceLockService != null)
                    try
                    {
                        mFaceLockService.unregisterCallback(mFaceLockCallback);
                    }
                    catch (RemoteException _ex) { }
                mContext.unbindService(mFaceLockConnection);
                mBoundToFaceLockService = false;
            }
        }
    }

    public void stopFaceLock()
    {
        if (!usingFaceLock())
            break MISSING_BLOCK_LABEL_80;
        Object obj = mFaceLockServiceRunningLock;
        obj;
        JVM INSTR monitorenter ;
        boolean flag = mFaceLockServiceRunning;
        if (!flag)
            break MISSING_BLOCK_LABEL_37;
        try
        {
            mFaceLockService.stopUi();
        }
        catch (RemoteException remoteexception)
        {
            Log.e("LockPatternKeyguardView", (new StringBuilder()).append("Caught exception stopping FaceLock: ").append(remoteexception.toString()).toString());
        }
        mFaceLockServiceRunning = false;
        obj;
        JVM INSTR monitorexit ;
        break MISSING_BLOCK_LABEL_80;
        Exception exception;
        exception;
        throw exception;
    }

    public void verifyUnlock()
    {
        if (isSecure())
        {
            if (mUnlockScreenMode == UnlockMode.Pattern || mUnlockScreenMode == UnlockMode.Password)
            {
                mIsVerifyUnlockOnly = true;
                updateScreen(Mode.UnlockScreen, false);
            } else
            {
                getCallback().keyguardDone(false);
            }
        } else
        {
            getCallback().keyguardDone(true);
        }
    }

    public void wakeWhenReadyTq(int i)
    {
        if (i != 82 || !isSecure() || mMode != Mode.LockScreen || mUpdateMonitor.getSimState(0) == com.android.internal.telephony.IccCard.State.PUK_REQUIRED || mUpdateMonitor.getSimState(1) == com.android.internal.telephony.IccCard.State.PUK_REQUIRED)
        {
            getCallback().pokeWakelock();
        } else
        {
            updateScreen(Mode.UnlockScreen, false);
            getCallback().pokeWakelock();
        }
    }





/*
    static boolean access$1102(LockPatternKeyguardView lockpatternkeyguardview, boolean flag)
    {
        lockpatternkeyguardview.mHasOverlay = flag;
        return flag;
    }

*/




/*
    static boolean access$1302(LockPatternKeyguardView lockpatternkeyguardview, boolean flag)
    {
        lockpatternkeyguardview.mFaceLockServiceRunning = flag;
        return flag;
    }

*/



/*
    static Parcelable access$1502(LockPatternKeyguardView lockpatternkeyguardview, Parcelable parcelable)
    {
        lockpatternkeyguardview.mSavedState = parcelable;
        return parcelable;
    }

*/








/*
    static int access$2102(LockPatternKeyguardView lockpatternkeyguardview, int i)
    {
        lockpatternkeyguardview.mFailedFaceUnlockAttempts = i;
        return i;
    }

*/


/*
    static int access$2108(LockPatternKeyguardView lockpatternkeyguardview)
    {
        int i = lockpatternkeyguardview.mFailedFaceUnlockAttempts;
        lockpatternkeyguardview.mFailedFaceUnlockAttempts = i + 1;
        return i;
    }

*/




/*
    static IFaceLockInterface access$2402(LockPatternKeyguardView lockpatternkeyguardview, IFaceLockInterface ifacelockinterface)
    {
        lockpatternkeyguardview.mFaceLockService = ifacelockinterface;
        return ifacelockinterface;
    }

*/



/*
    static boolean access$2602(LockPatternKeyguardView lockpatternkeyguardview, boolean flag)
    {
        lockpatternkeyguardview.mBoundToFaceLockService = flag;
        return flag;
    }

*/







/*
    static boolean access$402(LockPatternKeyguardView lockpatternkeyguardview, boolean flag)
    {
        lockpatternkeyguardview.mForgotPattern = flag;
        return flag;
    }

*/



/*
    static boolean access$502(LockPatternKeyguardView lockpatternkeyguardview, boolean flag)
    {
        lockpatternkeyguardview.mIsVerifyUnlockOnly = flag;
        return flag;
    }

*/





/*
    static boolean access$802(LockPatternKeyguardView lockpatternkeyguardview, boolean flag)
    {
        lockpatternkeyguardview.mEnableFallback = flag;
        return flag;
    }

*/

}
