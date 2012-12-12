// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.*;
import android.telephony.TelephonyManager;
import android.util.EventLog;
import android.util.Log;
import android.view.WindowManagerImpl;
import com.android.internal.widget.LockPatternUtils;
import com.mediatek.xlog.Xlog;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardViewCallback, KeyguardUpdateMonitor, LockPatternKeyguardViewProperties, KeyguardViewManager, 
//            KeyguardViewProperties, PhoneWindowManager

public class KeyguardViewMediator
    implements KeyguardViewCallback, KeyguardUpdateMonitor.deviceInfoCallback, KeyguardUpdateMonitor.SimStateCallback
{

    protected static final int AWAKE_INTERVAL_DEFAULT_KEYBOARD_OPEN_MS = 10000;
    protected static final int AWAKE_INTERVAL_DEFAULT_MS = 10000;
    private static final boolean DBG_WAKE = true;
    private static final boolean DEBUG = true;
    private static final String DELAYED_KEYGUARD_ACTION = "com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD";
    private static final boolean ENABLE_INSECURE_STATUS_BAR_EXPAND = true;
    private static final int HIDE = 3;
    public static final String IPO_DISABLE = "android.intent.action.ACTION_BOOT_IPO";
    public static final String IPO_ENABLE = "android.intent.action.ACTION_SHUTDOWN_IPO";
    private static final int KEYGUARD_DISPLAY_TIMEOUT_DELAY_DEFAULT = 30000;
    private static final int KEYGUARD_DONE = 9;
    private static final int KEYGUARD_DONE_AUTHENTICATING = 11;
    private static final int KEYGUARD_DONE_DRAWING = 10;
    private static final int KEYGUARD_DONE_DRAWING_TIMEOUT_MS = 2000;
    private static final int KEYGUARD_LOCK_AFTER_DELAY_DEFAULT = 5000;
    private static final int KEYGUARD_RELAYOUT = 14;
    private static final int KEYGUARD_TIMEOUT = 13;
    private static final int MASTER_STREAM_TYPE = 2;
    private static final int NOTIFY_SCREEN_OFF = 6;
    private static final int NOTIFY_SCREEN_ON = 7;
    private static final int RESET = 4;
    private static final int SET_HIDDEN = 12;
    private static final int SHOW = 2;
    private static final String TAG = "KeyguardViewMediator";
    private static final int TIMEOUT = 1;
    private static final int VERIFY_UNLOCK = 5;
    private static final int WAKE_WHEN_READY = 8;
    private static boolean mExternallyEnabled = true;
    private static boolean mHidden = false;
    private static boolean mShowing = false;
    final float MAX_LOCK_VOLUME = 0.4F;
    final float MIN_LOCK_VOLUME = 0.05F;
    private AlarmManager mAlarmManager;
    private AudioManager mAudioManager;
    private BroadcastReceiver mBroadCastReceiver;
    private PhoneWindowManager mCallback;
    private boolean mChecked;
    private Context mContext;
    private int mDelayedShowingSequence;
    private android.view.WindowManagerPolicy.OnKeyguardExitResult mExitSecureCallback;
    private Handler mHandler;
    private boolean mKeyboardOpen;
    private KeyguardViewManager mKeyguardViewManager;
    private KeyguardViewProperties mKeyguardViewProperties;
    private LockPatternUtils mLockPatternUtils;
    private int mLockSoundId;
    private int mLockSoundStreamId;
    private SoundPool mLockSounds;
    private int mMasterStreamMaxVolume;
    private boolean mNeedToReshowWhenReenabled;
    private PowerManager mPM;
    private String mPhoneState;
    LocalPowerManager mRealPowerManager;
    private boolean mScreenOn;
    private int mShowCount;
    private android.os.PowerManager.WakeLock mShowKeyguardWakeLock;
    private boolean mShowLockIcon;
    private boolean mShowingLockIcon;
    private StatusBarManager mStatusBarManager;
    private boolean mSuppressNextLockSound;
    private boolean mSystemReady;
    private int mUnlockSoundId;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private Intent mUserPresentIntent;
    private boolean mWaitingUntilKeyguardVisible;
    private android.os.PowerManager.WakeLock mWakeAndHandOff;
    private android.os.PowerManager.WakeLock mWakeLock;
    private int mWakelockSequence;

    public KeyguardViewMediator(Context context, PhoneWindowManager phonewindowmanager, LocalPowerManager localpowermanager)
    {
        mChecked = false;
        mSuppressNextLockSound = true;
        mNeedToReshowWhenReenabled = false;
        mKeyboardOpen = false;
        mScreenOn = false;
        mPhoneState = TelephonyManager.EXTRA_STATE_IDLE;
        mWaitingUntilKeyguardVisible = false;
        mShowCount = 0;
        mBroadCastReceiver = new BroadcastReceiver() {

            final KeyguardViewMediator this$0;

            public void onReceive(Context context1, Intent intent)
            {
                Object obj1;
                int i;
                obj1 = intent.getAction();
                if (!((String) (obj1)).equals("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD"))
                    break MISSING_BLOCK_LABEL_111;
                i = intent.getIntExtra("seq", 0);
                Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("received DELAYED_KEYGUARD_ACTION with seq = ").append(i).append(", mDelayedShowingSequence = ").append(mDelayedShowingSequence).toString());
                obj1 = KeyguardViewMediator.this;
                obj1;
                JVM INSTR monitorenter ;
                if (mDelayedShowingSequence == i)
                {
                    mSuppressNextLockSound = true;
                    doKeyguardLocked();
                }
                break MISSING_BLOCK_LABEL_200;
                if (!"android.intent.action.PHONE_STATE".equals(obj1))
                    break MISSING_BLOCK_LABEL_200;
                mPhoneState = intent.getStringExtra("state");
                obj1 = KeyguardViewMediator.this;
                obj1;
                JVM INSTR monitorenter ;
                if (TelephonyManager.EXTRA_STATE_IDLE.equals(mPhoneState) && !mScreenOn && KeyguardViewMediator.mExternallyEnabled)
                {
                    Xlog.d("KeyguardViewMediator", "screen is off and call ended, let's make sure the keyguard is showing");
                    doKeyguardLocked();
                }
            }

            
            {
                this$0 = KeyguardViewMediator.this;
                super();
            }
        }
;
        mHandler = new Handler() {

            final KeyguardViewMediator this$0;

            public void handleMessage(Message message)
            {
                boolean flag1 = true;
                message.what;
                JVM INSTR tableswitch 1 14: default 76
            //                           1 77
            //                           2 91
            //                           3 101
            //                           4 111
            //                           5 121
            //                           6 131
            //                           7 141
            //                           8 158
            //                           9 172
            //                           10 197
            //                           11 207
            //                           12 218
            //                           13 243
            //                           14 275;
                   goto _L1 _L2 _L3 _L4 _L5 _L6 _L7 _L8 _L9 _L10 _L11 _L12 _L13 _L14 _L15
_L1:
                return;
_L2:
                handleTimeout(message.arg1);
                continue; /* Loop/switch isn't completed */
_L3:
                handleShow();
                continue; /* Loop/switch isn't completed */
_L4:
                handleHide();
                continue; /* Loop/switch isn't completed */
_L5:
                handleReset();
                continue; /* Loop/switch isn't completed */
_L6:
                handleVerifyUnlock();
                continue; /* Loop/switch isn't completed */
_L7:
                handleNotifyScreenOff();
                continue; /* Loop/switch isn't completed */
_L8:
                handleNotifyScreenOn((KeyguardViewManager.ShowListener)message.obj);
                continue; /* Loop/switch isn't completed */
_L9:
                handleWakeWhenReady(message.arg1);
                continue; /* Loop/switch isn't completed */
_L10:
                KeyguardViewMediator keyguardviewmediator = KeyguardViewMediator.this;
                if (message.arg1 == 0)
                    flag1 = false;
                keyguardviewmediator.handleKeyguardDone(flag1);
                continue; /* Loop/switch isn't completed */
_L11:
                handleKeyguardDoneDrawing();
                continue; /* Loop/switch isn't completed */
_L12:
                keyguardDone(flag1);
                continue; /* Loop/switch isn't completed */
_L13:
                KeyguardViewMediator keyguardviewmediator1 = KeyguardViewMediator.this;
                if (message.arg1 == 0)
                    flag1 = false;
                keyguardviewmediator1.handleSetHidden(flag1);
                continue; /* Loop/switch isn't completed */
_L14:
                KeyguardViewMediator keyguardviewmediator2 = KeyguardViewMediator.this;
                keyguardviewmediator2;
                JVM INSTR monitorenter ;
                Log.i("KeyguardViewMediator", "doKeyguardLocked, because:KEYGUARD_TIMEOUT");
                doKeyguardLocked();
                continue; /* Loop/switch isn't completed */
_L15:
                KeyguardViewMediator keyguardviewmediator3 = KeyguardViewMediator.this;
                if (message.arg1 == 0)
                    flag1 = false;
                keyguardviewmediator3.handleKeyguardReLayout(flag1);
                if (true) goto _L1; else goto _L16
_L16:
            }

            
            {
                this$0 = KeyguardViewMediator.this;
                super();
            }
        }
;
        mContext = context;
        mRealPowerManager = localpowermanager;
        mPM = (PowerManager)context.getSystemService("power");
        mWakeLock = mPM.newWakeLock(0x1000001a, "keyguard");
        mWakeLock.setReferenceCounted(false);
        mShowKeyguardWakeLock = mPM.newWakeLock(1, "show keyguard");
        mShowKeyguardWakeLock.setReferenceCounted(false);
        mWakeAndHandOff = mPM.newWakeLock(1, "keyguardWakeAndHandOff");
        mWakeAndHandOff.setReferenceCounted(false);
        Object obj = new IntentFilter();
        ((IntentFilter) (obj)).addAction("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD");
        ((IntentFilter) (obj)).addAction("android.intent.action.PHONE_STATE");
        ((IntentFilter) (obj)).addAction("android.intent.action.ACTION_SHUTDOWN");
        ((IntentFilter) (obj)).addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        ((IntentFilter) (obj)).addAction("android.intent.action.ACTION_BOOT_IPO");
        context.registerReceiver(mBroadCastReceiver, ((IntentFilter) (obj)));
        mAlarmManager = (AlarmManager)context.getSystemService("alarm");
        mCallback = phonewindowmanager;
        mUpdateMonitor = new KeyguardUpdateMonitor(context);
        mLockPatternUtils = new LockPatternUtils(mContext);
        mKeyguardViewProperties = new LockPatternKeyguardViewProperties(mLockPatternUtils, mUpdateMonitor);
        mKeyguardViewManager = new KeyguardViewManager(context, WindowManagerImpl.getDefault(), this, mKeyguardViewProperties, mUpdateMonitor);
        mUserPresentIntent = new Intent("android.intent.action.USER_PRESENT");
        mUserPresentIntent.addFlags(0x30000000);
        obj = mContext.getContentResolver();
        boolean flag;
        if (android.provider.Settings.System.getInt(((android.content.ContentResolver) (obj)), "show_status_bar_lock", 0) != 1)
            flag = false;
        else
            flag = true;
        mShowLockIcon = flag;
        mLockSounds = new SoundPool(1, 1, 0);
        String s = android.provider.Settings.System.getString(((android.content.ContentResolver) (obj)), "lock_sound");
        if (s != null)
            mLockSoundId = mLockSounds.load(s, 1);
        if (s == null || mLockSoundId == 0)
            Log.d("KeyguardViewMediator", (new StringBuilder()).append("failed to load sound from ").append(s).toString());
        obj = android.provider.Settings.System.getString(((android.content.ContentResolver) (obj)), "unlock_sound");
        if (obj != null)
            mUnlockSoundId = mLockSounds.load(((String) (obj)), 1);
        if (obj == null || mUnlockSoundId == 0)
            Log.d("KeyguardViewMediator", (new StringBuilder()).append("failed to load sound from ").append(((String) (obj))).toString());
        mUpdateMonitor.registerDeviceInfoCallback(this);
        mUpdateMonitor.registerSimStateCallback(this);
    }

    private void adjustStatusBarLocked()
    {
        if (mStatusBarManager == null)
            mStatusBarManager = (StatusBarManager)mContext.getSystemService("statusbar");
        if (mStatusBarManager != null)
        {
            if (mShowLockIcon)
                if (!mShowing || !isSecure())
                {
                    if (mShowingLockIcon)
                    {
                        mStatusBarManager.removeIcon("secure");
                        mShowingLockIcon = false;
                    }
                } else
                if (!mShowingLockIcon)
                {
                    String s = mContext.getString(0x10404cc);
                    mStatusBarManager.setIcon("secure", 0x1080529, 0, s);
                    mShowingLockIcon = true;
                }
            int i = 0;
            if (mShowing)
            {
                i = 0 | 0x1000000;
                if (isSecure())
                    i |= 0x10000;
                if (isSecure())
                    i |= 0x80000;
            }
            Log.d("KeyguardViewMediator", (new StringBuilder()).append("adjustStatusBarLocked: mShowing=").append(mShowing).append(" mHidden=").append(mHidden).append(" isSecure=").append(isSecure()).append(" --> flags=0x").append(Integer.toHexString(i)).toString());
            mStatusBarManager.disable(i);
        } else
        {
            Xlog.w("KeyguardViewMediator", "Could not get status bar manager");
        }
    }

    private void adjustUserActivityLocked()
    {
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("adjustUserActivityLocked mShowing: ").append(mShowing).append(" mHidden: ").append(mHidden).toString());
        boolean flag;
        if (mShowing && !mHidden)
            flag = false;
        else
            flag = true;
        mRealPowerManager.enableUserActivity(flag);
        if (!flag && mScreenOn)
            pokeWakelock();
    }

    private void adjustUserActivityLockedByShow()
    {
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("adjustUserActivityLocked mShowing: ").append(mShowing).append(" mHidden: ").append(mHidden).toString());
        boolean flag;
        if (mShowing && !mHidden)
            flag = false;
        else
            flag = true;
        if (1 != mShowCount)
        {
            Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("we will set the enableUserActivity enabled=").append(flag).toString());
            mRealPowerManager.enableUserActivity(flag);
        } else
        {
            Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("we firstset enableUserActivity,enabled=").append(flag).toString());
        }
        if (!flag && mScreenOn)
            pokeWakelock();
    }

    private void handleHide()
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", "handleHide");
        if (!mWakeAndHandOff.isHeld()) goto _L2; else goto _L1
_L1:
        Xlog.w("KeyguardViewMediator", "attempt to hide the keyguard while waking, ignored");
          goto _L3
_L2:
        if (mShowing) goto _L4; else goto _L3
        Exception exception;
        exception;
        throw exception;
_L4:
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(mPhoneState) && mScreenOn)
            playSounds(false);
        mKeyguardViewManager.hide();
        mShowing = false;
        adjustUserActivityLocked();
        adjustStatusBarLocked();
        this;
        JVM INSTR monitorexit ;
_L3:
    }

    private void handleKeyguardDone(boolean flag)
    {
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("handleKeyguardDone, wakeup=").append(flag).toString());
        handleHide();
        if (flag)
            mPM.userActivity(SystemClock.uptimeMillis(), true);
        mWakeLock.release();
        if (mContext != null && ActivityManagerNative.isSystemReady())
            mContext.sendBroadcast(mUserPresentIntent);
    }

    private void handleKeyguardDoneDrawing()
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", "handleKeyguardDoneDrawing");
        if (!mWaitingUntilKeyguardVisible) goto _L2; else goto _L1
_L1:
        Xlog.d("KeyguardViewMediator", "handleKeyguardDoneDrawing: notifying mWaitingUntilKeyguardVisible");
        mWaitingUntilKeyguardVisible = false;
        notifyAll();
        mHandler.removeMessages(10);
_L4:
        this;
        JVM INSTR monitorexit ;
        return;
_L2:
        if (mContext != null && ActivityManagerNative.isSystemReady())
            mContext.sendBroadcast(new Intent("android.intent.action.SEND"), "com.android.internal.policy.impl.KeyguardViewMediator.DONE_DRAW");
        if (true) goto _L4; else goto _L3
_L3:
    }

    private void handleKeyguardReLayout(boolean flag)
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", "handleKeyguardReLayout");
        mKeyguardViewManager.reLayoutScreen(flag);
        return;
    }

    private void handleNotifyScreenOff()
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", "handleNotifyScreenOff");
        mKeyguardViewManager.onScreenTurnedOff();
        return;
    }

    private void handleNotifyScreenOn(KeyguardViewManager.ShowListener showlistener)
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", "handleNotifyScreenOn");
        mKeyguardViewManager.onScreenTurnedOn(showlistener);
        return;
    }

    private void handleReset()
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", "handleReset");
        mKeyguardViewManager.reset();
        adjustStatusBarLocked();
        return;
    }

    private void handleSetHidden(boolean flag)
    {
        this;
        JVM INSTR monitorenter ;
        if (mHidden != flag)
        {
            mHidden = flag;
            adjustUserActivityLocked();
            adjustStatusBarLocked();
        }
        return;
    }

    private void handleShow()
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", "handleShow");
        if (mSystemReady && !mShowing) goto _L2; else goto _L1
        Exception exception;
        exception;
        throw exception;
_L2:
        mShowCount = 1 + mShowCount;
        if (mShowCount == (int)Math.pow(2D, 32D))
            mShowCount = 2;
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("handleShow, count=").append(mShowCount).toString());
        mKeyguardViewManager.show();
        mShowing = true;
        adjustUserActivityLocked();
        adjustStatusBarLocked();
        try
        {
            ActivityManagerNative.getDefault().closeSystemDialogs("lock");
        }
        catch (RemoteException _ex) { }
        playSounds(true);
        mShowKeyguardWakeLock.release();
        this;
        JVM INSTR monitorexit ;
_L1:
    }

    private void handleTimeout(int i)
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("handleTimeout, seq=").append(i).append(", mWakelockSequence=").append(mWakelockSequence).toString());
        if (i == mWakelockSequence)
            mWakeLock.release();
        return;
    }

    private void handleVerifyUnlock()
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", "handleVerifyUnlock");
        mKeyguardViewManager.verifyUnlock();
        mShowing = true;
        return;
    }

    private void handleWakeWhenReady(int i)
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("handleWakeWhenReady(").append(i).append(")").toString());
        if (!mKeyguardViewManager.wakeWhenReadyTq(i))
        {
            Xlog.w("KeyguardViewMediator", "mKeyguardViewManager.wakeWhenReadyTq did not poke wake lock, so poke it ourselves");
            pokeWakelock();
        }
        mWakeAndHandOff.release();
        if (!mWakeLock.isHeld())
            Xlog.w("KeyguardViewMediator", "mWakeLock not held in mKeyguardViewManager.wakeWhenReadyTq");
        return;
    }

    private void hideLocked()
    {
        Xlog.d("KeyguardViewMediator", "hideLocked");
        Message message = mHandler.obtainMessage(3);
        mHandler.sendMessage(message);
    }

    public static boolean isKeyguardEnabled()
    {
        return mExternallyEnabled;
    }

    public static boolean isKeyguardNotShowing()
    {
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append(" mExternallyEnabled=").append(mExternallyEnabled).append(" mShowing=").append(mShowing).toString());
        boolean flag;
        if (!mExternallyEnabled || mShowing)
            flag = false;
        else
            flag = true;
        return flag;
    }

    public static boolean isKeyguardShowingAndNotHidden()
    {
        boolean flag;
        if (!mShowing || mHidden)
            flag = false;
        else
            flag = true;
        return flag;
    }

    private boolean isWakeKeyWhenKeyguardShowing(int i, boolean flag)
    {
        switch (i)
        {
        default:
            flag = true;
            break;

        case 27: // '\033'
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
            flag = false;
            break;

        case 24: // '\030'
        case 25: // '\031'
        case 164: 
            break;
        }
        return flag;
    }

    private void notifyScreenOffLocked()
    {
        Log.d("KeyguardViewMediator", "notifyScreenOffLocked");
        mHandler.sendEmptyMessage(6);
    }

    private void notifyScreenOnLocked(KeyguardViewManager.ShowListener showlistener)
    {
        Xlog.d("KeyguardViewMediator", "notifyScreenOnLocked");
        Message message = mHandler.obtainMessage(7, showlistener);
        mHandler.sendMessage(message);
    }

    private void playSounds(boolean flag)
    {
        if (!mSuppressNextLockSound)
        {
            if (android.provider.Settings.System.getInt(mContext.getContentResolver(), "lockscreen_sounds_enabled", 1) != 1)
                break MISSING_BLOCK_LABEL_163;
            int i;
            if (!flag)
                i = mUnlockSoundId;
            else
                i = mLockSoundId;
            mLockSounds.stop(mLockSoundStreamId);
            if (mAudioManager == null)
            {
                mAudioManager = (AudioManager)mContext.getSystemService("audio");
                if (mAudioManager == null)
                    break MISSING_BLOCK_LABEL_163;
                mMasterStreamMaxVolume = mAudioManager.getStreamMaxVolume(2);
            }
            if (!mAudioManager.isStreamMute(2))
            {
                float f = mAudioManager.getStreamVolume(2);
                if (f != 0)
                {
                    f = 0.05F + 0.35F * ((float)f / (float)mMasterStreamMaxVolume);
                    mLockSoundStreamId = mLockSounds.play(i, f, f, 1, 0, 1F);
                }
            }
        } else
        {
            mSuppressNextLockSound = false;
        }
    }

    private void resetStateLocked()
    {
        Xlog.d("KeyguardViewMediator", "resetStateLocked");
        Message message = mHandler.obtainMessage(4);
        mHandler.sendMessage(message);
    }

    private void showLocked()
    {
        Xlog.d("KeyguardViewMediator", "showLocked");
        mShowKeyguardWakeLock.acquire();
        Message message = mHandler.obtainMessage(2);
        if (!isAlarmBoot() || mChecked)
        {
            mHandler.sendMessage(message);
        } else
        {
            mChecked = true;
            Log.i("KeyguardViewMediator", "it's alarm boot, delay 3s to show");
            mHandler.sendMessageDelayed(message, 5000L);
        }
    }

    private void verifyUnlockLocked()
    {
        Xlog.d("KeyguardViewMediator", "verifyUnlockLocked");
        mHandler.sendEmptyMessage(5);
    }

    private void wakeWhenReadyLocked(int i)
    {
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("wakeWhenReadyLocked(").append(i).append(")").toString());
        mWakeAndHandOff.acquire();
        Message message = mHandler.obtainMessage(8, i, 0);
        mHandler.sendMessage(message);
    }

    public boolean DM_Check_Locked()
    {
        return mUpdateMonitor.DM_IsLocked();
    }

    public void doKeyguardLocked()
    {
        if (mExternallyEnabled || mUpdateMonitor.DM_IsLocked())
        {
            if (!mKeyguardViewManager.isShowing())
            {
                boolean flag;
                if (SystemProperties.getBoolean("keyguard.no_require_sim", false))
                    flag = false;
                else
                    flag = true;
                boolean flag1 = mUpdateMonitor.isDeviceProvisioned();
                com.android.internal.telephony.IccCard.State state1 = mUpdateMonitor.getSimState(0);
                com.android.internal.telephony.IccCard.State state = mUpdateMonitor.getSimState(1);
                boolean flag3;
                if (!state1.isPinLocked() && (state1 != com.android.internal.telephony.IccCard.State.PERM_DISABLED || !flag))
                    flag3 = false;
                else
                    flag3 = true;
                boolean flag2;
                if (!state.isPinLocked() && (state != com.android.internal.telephony.IccCard.State.PERM_DISABLED || !flag))
                    flag2 = false;
                else
                    flag2 = true;
                if (!flag3 && !flag2)
                    flag3 = false;
                else
                    flag3 = true;
                flag2 = mLockPatternUtils.isLockScreenDisabled();
                Log.i("KeyguardViewMediator", (new StringBuilder()).append("lockedOrMissing is ").append(flag3).append(", requireSim=").append(flag).append(", provisioned=").append(flag1).append(", keyguardisable=").append(flag2).toString());
                if (flag3 || flag1)
                {
                    if (!flag2 || flag3)
                    {
                        Log.d("KeyguardViewMediator", "doKeyguard: showing the lock screen");
                        showLocked();
                    } else
                    {
                        Log.d("KeyguardViewMediator", "doKeyguard: not showing because lockscreen is off");
                    }
                } else
                {
                    Log.d("KeyguardViewMediator", "doKeyguard: not showing because device isn't provisioned and the sim is not locked or missing");
                }
            } else
            {
                Xlog.d("KeyguardViewMediator", "doKeyguard: not showing because it is already showing");
            }
        } else
        {
            Xlog.d("KeyguardViewMediator", "doKeyguard: not showing because externally disabled");
        }
    }

    public void doKeyguardTimeout()
    {
        Log.i("KeyguardViewMediator", "doKeyguardLocked");
        mHandler.removeMessages(13);
        Message message = mHandler.obtainMessage(13);
        mHandler.sendMessage(message);
    }

    public boolean doLidChangeTq(boolean flag)
    {
        mKeyboardOpen = flag;
        boolean flag1;
        if (!mUpdateMonitor.isKeyguardBypassEnabled() || !mKeyboardOpen || mKeyguardViewProperties.isSecure() || !mKeyguardViewManager.isShowing())
        {
            flag1 = false;
        } else
        {
            Log.d("KeyguardViewMediator", "bypassing keyguard on sliding open of keyboard with non-secure keyguard");
            mHandler.sendEmptyMessage(11);
            flag1 = true;
        }
        return flag1;
    }

    public boolean isAlarmBoot()
    {
        String s = SystemProperties.get("sys.boot.reason");
        boolean flag;
        if (s == null || !s.equals("1"))
            flag = false;
        else
            flag = true;
        return flag;
    }

    public boolean isInputRestricted()
    {
        boolean flag;
        if (!mShowing && !mNeedToReshowWhenReenabled && mUpdateMonitor.isDeviceProvisioned())
            flag = false;
        else
            flag = true;
        return flag;
    }

    public boolean isSecure()
    {
        return mKeyguardViewProperties.isSecure();
    }

    public boolean isShowing()
    {
        return mShowing;
    }

    public boolean isShowingAndNotHidden()
    {
        boolean flag;
        if (!mShowing || mHidden)
            flag = false;
        else
            flag = true;
        return flag;
    }

    public void keyguardDone(boolean flag)
    {
        keyguardDone(flag, true);
    }

    public void keyguardDone(boolean flag, boolean flag1)
    {
        int i = 1;
        this;
        JVM INSTR monitorenter ;
        EventLog.writeEvent(0x11170, 2);
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("keyguardDone(").append(flag).append(")").toString());
        Message message = mHandler.obtainMessage(9);
        if (!flag1)
            i = 0;
        message.arg1 = i;
        mHandler.sendMessage(message);
        if (flag)
            mUpdateMonitor.clearFailedAttempts();
        if (mExitSecureCallback != null)
        {
            mExitSecureCallback.onKeyguardExitResult(flag);
            mExitSecureCallback = null;
            if (flag)
            {
                mExternallyEnabled = true;
                mNeedToReshowWhenReenabled = false;
            }
        }
        return;
    }

    public void keyguardDoneDrawing()
    {
        mHandler.sendEmptyMessage(10);
    }

    public void onClockVisibilityChanged()
    {
        adjustStatusBarLocked();
    }

    public void onDMKeyguardUpdate()
    {
        boolean flag = mUpdateMonitor.DM_IsLocked();
        Log.d("KeyguardViewMediator", (new StringBuilder()).append("onDMKeyguardUpdate():").append(flag).toString());
        if (flag)
            mPM.goToSleep(1L + SystemClock.uptimeMillis());
        Message message = mHandler.obtainMessage(14);
        int i;
        if (!flag)
            i = 0;
        else
            i = 1;
        message.arg1 = i;
        mHandler.sendMessage(message);
    }

    public void onDeviceProvisioned()
    {
        mContext.sendBroadcast(mUserPresentIntent);
    }

    public void onScreenTurnedOff(int i)
    {
        this;
        JVM INSTR monitorenter ;
        Object obj;
        long l;
        mScreenOn = false;
        Log.d("KeyguardViewMediator", (new StringBuilder()).append("onScreenTurnedOff(").append(i).append(")").toString());
        Exception exception;
        android.content.ContentResolver contentresolver;
        long l1;
        if (mLockPatternUtils.getPowerButtonInstantlyLocks() || !mLockPatternUtils.isSecure())
            obj = 1;
        else
            obj = 0;
        mKeyguardViewManager.setScreenStatus(mScreenOn);
        if (mExitSecureCallback != null)
        {
            Log.d("KeyguardViewMediator", "pending exit secure callback cancelled");
            mExitSecureCallback.onKeyguardExitResult(false);
            mExitSecureCallback = null;
            if (!mExternallyEnabled)
                hideLocked();
        } else
        {
            if (!mShowing)
                break MISSING_BLOCK_LABEL_139;
            notifyScreenOffLocked();
            resetStateLocked();
        }
        return;
        exception;
        throw exception;
        if (i != 3 && (i != 2 || obj != 0)) goto _L2; else goto _L1
_L1:
        contentresolver = mContext.getContentResolver();
        obj = (long)android.provider.Settings.System.getInt(contentresolver, "screen_off_timeout", 30000);
        l = android.provider.Settings.Secure.getInt(contentresolver, "lock_screen_lock_after_timeout", 5000);
        l1 = mLockPatternUtils.getDevicePolicyManager().getMaximumTimeToLock(null);
        if (l1 <= 0L)
            break MISSING_BLOCK_LABEL_357;
        obj = Math.min(l1 - Math.max(((long) (obj)), 0L), l);
_L3:
        if (obj <= 0L)
        {
            mSuppressNextLockSound = true;
            doKeyguardLocked();
        } else
        {
            obj += SystemClock.elapsedRealtime();
            l = new Intent("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD");
            l.putExtra("seq", mDelayedShowingSequence);
            l = PendingIntent.getBroadcast(mContext, 0, l, 0x10000000);
            mAlarmManager.set(2, ((long) (obj)), l);
            Log.d("KeyguardViewMediator", (new StringBuilder()).append("setting alarm to turn off keyguard, seq = ").append(mDelayedShowingSequence).toString());
        }
        break MISSING_BLOCK_LABEL_114;
_L2:
        if (i != 4)
            doKeyguardLocked();
        break MISSING_BLOCK_LABEL_114;
        obj = l;
          goto _L3
    }

    public void onScreenTurnedOn(KeyguardViewManager.ShowListener showlistener)
    {
        this;
        JVM INSTR monitorenter ;
        mScreenOn = true;
        mKeyguardViewManager.setScreenStatus(mScreenOn);
        mDelayedShowingSequence = 1 + mDelayedShowingSequence;
        Log.d("KeyguardViewMediator", (new StringBuilder()).append("onScreenTurnedOn, seq = ").append(mDelayedShowingSequence).toString());
        notifyScreenOnLocked(showlistener);
        return;
    }

    public void onSimStateChanged(com.android.internal.telephony.IccCard.State state)
    {
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("onSimStateChanged: ").append(state).toString());
        class _cls3
        {

            static final int $SwitchMap$com$android$internal$telephony$IccCard$State[];

            static 
            {
                $SwitchMap$com$android$internal$telephony$IccCard$State = new int[com.android.internal.telephony.IccCard.State.values().length];
                try
                {
                    $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.ABSENT.ordinal()] = 1;
                }
                catch (NoSuchFieldError _ex) { }
                try
                {
                    $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.PIN_REQUIRED.ordinal()] = 2;
                }
                catch (NoSuchFieldError _ex) { }
                try
                {
                    $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.PUK_REQUIRED.ordinal()] = 3;
                }
                catch (NoSuchFieldError _ex) { }
                try
                {
                    $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.PERM_DISABLED.ordinal()] = 4;
                }
                catch (NoSuchFieldError _ex) { }
                try
                {
                    $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.READY.ordinal()] = 5;
                }
                catch (NoSuchFieldError _ex) { }
                $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.NOT_READY.ordinal()] = 6;
_L2:
                return;
                JVM INSTR pop ;
                if (true) goto _L2; else goto _L1
_L1:
            }
        }

        _cls3..SwitchMap.com.android.internal.telephony.IccCard.State[state.ordinal()];
        JVM INSTR tableswitch 1 6: default 72
    //                   1 73
    //                   2 129
    //                   3 129
    //                   4 168
    //                   5 216
    //                   6 239;
           goto _L1 _L2 _L3 _L3 _L4 _L5 _L6
_L1:
        return;
_L2:
        this;
        JVM INSTR monitorenter ;
        if (mUpdateMonitor.isDeviceProvisioned()) goto _L8; else goto _L7
_L7:
        if (isShowing()) goto _L10; else goto _L9
_L9:
        Log.d("KeyguardViewMediator", "ICC_ABSENT isn't showing, we need to show the keyguard since the device isn't provisioned yet.");
        doKeyguardLocked();
_L11:
        this;
        JVM INSTR monitorexit ;
        continue; /* Loop/switch isn't completed */
        Exception exception;
        exception;
        throw exception;
_L10:
        resetStateLocked();
          goto _L11
_L8:
        adjustStatusBarLocked();
          goto _L11
_L3:
        this;
        JVM INSTR monitorenter ;
        if (isShowing()) goto _L13; else goto _L12
_L12:
        Log.d("KeyguardViewMediator", "INTENT_VALUE_ICC_LOCKED and keygaurd isn't showing, we need to show the keyguard so the user can enter their sim pin");
        doKeyguardLocked();
_L14:
        this;
        JVM INSTR monitorexit ;
        continue; /* Loop/switch isn't completed */
        exception;
        throw exception;
_L13:
        resetStateLocked();
          goto _L14
_L4:
        this;
        JVM INSTR monitorenter ;
        if (isShowing()) goto _L16; else goto _L15
_L15:
        Log.d("KeyguardViewMediator", "PERM_DISABLED and keygaurd isn't showing.");
        doKeyguardLocked();
_L17:
        this;
        JVM INSTR monitorexit ;
        continue; /* Loop/switch isn't completed */
        exception;
        throw exception;
_L16:
        Log.d("KeyguardViewMediator", "PERM_DISABLED, resetStateLocked toshow permanently disabled message in lockscreen.");
        resetStateLocked();
          goto _L17
_L5:
        this;
        JVM INSTR monitorenter ;
        if (isShowing())
            resetStateLocked();
        continue; /* Loop/switch isn't completed */
_L6:
        this;
        JVM INSTR monitorenter ;
        if (isShowing() && com.android.internal.telephony.IccCard.State.PIN_REQUIRED == mUpdateMonitor.getLastSimState(0))
            resetStateLocked();
        if (true) goto _L1; else goto _L18
_L18:
    }

    public void onSimStateChangedGemini(com.android.internal.telephony.IccCard.State state, int i)
    {
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("onSimStateChangedGemini: ").append(state).append(",simId = ").append(i).toString());
        switch (_cls3..SwitchMap.com.android.internal.telephony.IccCard.State[state.ordinal()])
        {
        case 4: // '\004'
        default:
            break;

        case 1: // '\001'
            if (mUpdateMonitor.isDeviceProvisioned())
                break;
            if (isShowing())
            {
                resetStateLocked();
            } else
            {
                Xlog.d("KeyguardViewMediator", "INTENT_VALUE_ICC_ABSENT and keygaurd isn't showing, we need to show the keyguard since the device isn't provisioned yet.");
                doKeyguardLocked();
            }
            break;

        case 2: // '\002'
        case 3: // '\003'
            if (com.android.internal.telephony.IccCard.State.PIN_REQUIRED == state && mUpdateMonitor.getPINDismissFlag(i, true))
                Xlog.i("KeyguardViewMediator", "We have dismissed PIN, so, we exit from PINPUK_REQUIRED");
            if (com.android.internal.telephony.IccCard.State.PUK_REQUIRED == state && mUpdateMonitor.getPINDismissFlag(i, false))
                Xlog.i("KeyguardViewMediator", "We have dismissed PUK, so, we exit from PINPUK_REQUIRED");
            if (isShowing())
            {
                resetStateLocked();
            } else
            {
                Xlog.d("KeyguardViewMediator", "INTENT_VALUE_ICC_LOCKED and keygaurd isn't showing, we need to show the keyguard so the user can enter their sim pin");
                doKeyguardLocked();
            }
            break;

        case 5: // '\005'
            if (isShowing())
                resetStateLocked();
            break;

        case 6: // '\006'
            if (isShowing() && com.android.internal.telephony.IccCard.State.PIN_REQUIRED == mUpdateMonitor.getLastSimState(i))
                resetStateLocked();
            break;
        }
    }

    public void onSystemReady()
    {
        this;
        JVM INSTR monitorenter ;
        Log.d("KeyguardViewMediator", "onSystemReady");
        mSystemReady = true;
        doKeyguardLocked();
        return;
    }

    public boolean onWakeKeyWhenKeyguardShowingTq(int i, boolean flag)
    {
        Log.d("KeyguardViewMediator", (new StringBuilder()).append("onWakeKeyWhenKeyguardShowing(").append(i).append(")").toString());
        boolean flag1;
        if (!isWakeKeyWhenKeyguardShowing(i, flag))
        {
            flag1 = false;
        } else
        {
            wakeWhenReadyLocked(i);
            flag1 = true;
        }
        return flag1;
    }

    public boolean onWakeMotionWhenKeyguardShowingTq()
    {
        Log.d("KeyguardViewMediator", "onWakeMotionWhenKeyguardShowing()");
        wakeWhenReadyLocked(0);
        return true;
    }

    public void pokeWakelock()
    {
        if (!mKeyboardOpen);
        pokeWakelock(10000);
    }

    public void pokeWakelock(int i)
    {
        this;
        JVM INSTR monitorenter ;
        Xlog.d("KeyguardViewMediator", (new StringBuilder()).append("pokeWakelock(").append(i).append(")").toString());
        mWakeLock.acquire();
        mHandler.removeMessages(1);
        mWakelockSequence = 1 + mWakelockSequence;
        Message message = mHandler.obtainMessage(1, mWakelockSequence, 0);
        mHandler.sendMessageDelayed(message, i);
        return;
    }

    public void setDebugFilterStatus(boolean flag)
    {
    }

    public void setHidden(boolean flag)
    {
        mHandler.removeMessages(12);
        Handler handler = mHandler;
        int i;
        if (!flag)
            i = 0;
        else
            i = 1;
        Message message = handler.obtainMessage(12, i, 0);
        mHandler.sendMessage(message);
    }

    public void setKeyguardEnabled(boolean flag)
    {
        this;
        JVM INSTR monitorenter ;
        Log.d("KeyguardViewMediator", (new StringBuilder()).append("setKeyguardEnabled(").append(flag).append(")").toString());
        mExternallyEnabled = flag;
        if (flag || !mShowing) goto _L2; else goto _L1
_L1:
        if (mExitSecureCallback == null) goto _L4; else goto _L3
_L3:
        Log.d("KeyguardViewMediator", "in process of verifyUnlock request, ignoring");
          goto _L5
_L4:
        Log.d("KeyguardViewMediator", "remembering to reshow, hiding keyguard, disabling status bar expansion");
        mNeedToReshowWhenReenabled = true;
        hideLocked();
_L7:
        this;
        JVM INSTR monitorexit ;
        break; /* Loop/switch isn't completed */
        Exception exception;
        exception;
        throw exception;
_L2:
        if (!flag)
            continue; /* Loop/switch isn't completed */
        if (!mNeedToReshowWhenReenabled)
            continue; /* Loop/switch isn't completed */
        Log.d("KeyguardViewMediator", "previously hidden, reshowing, reenabling status bar expansion");
        mNeedToReshowWhenReenabled = false;
        if (mExitSecureCallback != null)
        {
            Log.d("KeyguardViewMediator", "onKeyguardExitResult(false), resetting");
            mExitSecureCallback.onKeyguardExitResult(false);
            mExitSecureCallback = null;
            resetStateLocked();
            continue; /* Loop/switch isn't completed */
        }
        showLocked();
        mWaitingUntilKeyguardVisible = true;
        mHandler.sendEmptyMessageDelayed(10, 2000L);
        Log.d("KeyguardViewMediator", "waiting until mWaitingUntilKeyguardVisible is false");
_L6:
        boolean flag1 = mWaitingUntilKeyguardVisible;
        if (!flag1)
            break MISSING_BLOCK_LABEL_217;
        wait();
          goto _L6
        JVM INSTR pop ;
        Thread.currentThread().interrupt();
          goto _L6
        Log.d("KeyguardViewMediator", "done waiting for mWaitingUntilKeyguardVisible");
        if (true) goto _L7; else goto _L5
_L5:
    }

    public void verifyUnlock(android.view.WindowManagerPolicy.OnKeyguardExitResult onkeyguardexitresult)
    {
        this;
        JVM INSTR monitorenter ;
        Log.d("KeyguardViewMediator", "verifyUnlock");
        if (!mUpdateMonitor.isDeviceProvisioned())
        {
            Log.d("KeyguardViewMediator", "ignoring because device isn't provisioned");
            onkeyguardexitresult.onKeyguardExitResult(false);
        } else
        {
            if (!mExternallyEnabled)
                break MISSING_BLOCK_LABEL_70;
            Log.w("KeyguardViewMediator", "verifyUnlock called when not externally disabled");
            onkeyguardexitresult.onKeyguardExitResult(false);
        }
_L1:
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
        if (mExitSecureCallback != null)
        {
            onkeyguardexitresult.onKeyguardExitResult(false);
        } else
        {
            mExitSecureCallback = onkeyguardexitresult;
            verifyUnlockLocked();
        }
          goto _L1
    }





/*
    static boolean access$102(KeyguardViewMediator keyguardviewmediator, boolean flag)
    {
        keyguardviewmediator.mSuppressNextLockSound = flag;
        return flag;
    }

*/









/*
    static String access$202(KeyguardViewMediator keyguardviewmediator, String s)
    {
        keyguardviewmediator.mPhoneState = s;
        return s;
    }

*/







}
