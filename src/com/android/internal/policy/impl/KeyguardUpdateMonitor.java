// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.telephony.*;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.widget.LockPatternUtils;
import com.google.android.collect.Lists;
import com.mediatek.dmagent.DMAgent;
import com.mediatek.xlog.Xlog;
import java.util.*;

public class KeyguardUpdateMonitor
{
    static interface SystemStateCallback
    {

        public abstract void onSysBootup();

        public abstract void onSysShutdown();
    }

    static interface RadioStateCallback
    {

        public abstract void onRadioStateChanged(int i);
    }

    static interface SimStateCallback
    {

        public abstract void onSimStateChanged(com.android.internal.telephony.IccCard.State state);

        public abstract void onSimStateChangedGemini(com.android.internal.telephony.IccCard.State state, int i);
    }

    static interface InfoCallback
    {

        public abstract void onDownloadCalibrationDataUpdate(boolean flag);

        public abstract void onLockScreenUpdate(int i);

        public abstract void onMissedCallChanged(int i);

        public abstract void onRefreshBatteryInfo(boolean flag, boolean flag1, int i);

        public abstract void onRefreshCarrierInfo(CharSequence charsequence, CharSequence charsequence1);

        public abstract void onRingerModeChanged(int i);

        public abstract void onSIMInfoChanged(int i);

        public abstract void onSearchNetworkUpdate(int i, boolean flag);

        public abstract void onTimeChanged();

        public abstract void onUnlockKeyguard();

        public abstract void onWallpaperSetComplete();
    }

    static interface deviceInfoCallback
    {

        public abstract void onClockVisibilityChanged();

        public abstract void onDMKeyguardUpdate();

        public abstract void onDeviceProvisioned();
    }

    static interface phoneStateCallback
    {

        public abstract void onPhoneStateChanged(int i);
    }

    private static class BatteryStatus
    {

        public final int health;
        public final int level;
        public final int plugged;
        public final int status;

        public BatteryStatus(int i, int j, int k, int l)
        {
            status = i;
            level = j;
            plugged = k;
            health = l;
        }
    }

    private static class SimArgs
    {

        int simId;
        public final com.android.internal.telephony.IccCard.State simState;

        static SimArgs fromIntent(Intent intent)
        {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction()))
            {
                Object obj = intent.getStringExtra("ss");
                int i = intent.getIntExtra("simId", 0);
                if (!"ABSENT".equals(obj))
                {
                    if (!"READY".equals(obj))
                    {
                        if (!"LOCKED".equals(obj))
                        {
                            if (!"NETWORK".equals(obj))
                            {
                                if (!"NOT_READY".equals(obj))
                                    obj = com.android.internal.telephony.IccCard.State.UNKNOWN;
                                else
                                    obj = com.android.internal.telephony.IccCard.State.NOT_READY;
                            } else
                            {
                                obj = com.android.internal.telephony.IccCard.State.NETWORK_LOCKED;
                            }
                        } else
                        {
                            obj = intent.getStringExtra("reason");
                            if (!"PIN".equals(obj))
                            {
                                if (!"PUK".equals(obj))
                                    obj = com.android.internal.telephony.IccCard.State.UNKNOWN;
                                else
                                    obj = com.android.internal.telephony.IccCard.State.PUK_REQUIRED;
                            } else
                            {
                                obj = com.android.internal.telephony.IccCard.State.PIN_REQUIRED;
                            }
                        }
                    } else
                    {
                        obj = com.android.internal.telephony.IccCard.State.READY;
                    }
                } else
                if (!"PERM_DISABLED".equals(intent.getStringExtra("reason")))
                    obj = com.android.internal.telephony.IccCard.State.ABSENT;
                else
                    obj = com.android.internal.telephony.IccCard.State.PERM_DISABLED;
                return new SimArgs(((com.android.internal.telephony.IccCard.State) (obj)), i);
            } else
            {
                throw new IllegalArgumentException("only handles intent ACTION_SIM_STATE_CHANGED");
            }
        }

        public String toString()
        {
            return simState.toString();
        }

        SimArgs(com.android.internal.telephony.IccCard.State state)
        {
            simId = 0;
            simState = state;
        }

        SimArgs(com.android.internal.telephony.IccCard.State state, int i)
        {
            simId = 0;
            simState = state;
            simId = i;
        }
    }

    private class SIMStatus
    {

        private int _dialogType;
        private int _total;
        final KeyguardUpdateMonitor this$0;

        public int getSIMCardCount()
        {
            return _total;
        }

        public int getSimType()
        {
            return _dialogType;
        }

        public SIMStatus(int i, int j)
        {
            this$0 = KeyguardUpdateMonitor.this;
            super();
            _total = 0;
            _dialogType = 0;
            _dialogType = i;
            _total = j;
        }
    }


    private static final String ACTION_WALLPAPER_SET = "com.mediatek.lockscreen.action.WALLPAPER_SET";
    private static final int DEFAULTSIMREMOVED = 1;
    private static final String EXTRA_COMPLETE = "com.mediatek.lockscreen.extra.COMPLETE";
    private static boolean KEYGUARD_DM_LOCKED = false;
    static final int LOW_BATTERY_THRESHOLD = 16;
    private static final int MSG_BATTERY_UPDATE = 302;
    private static final int MSG_BOOTUP_MODE_PICK = 322;
    private static final int MSG_CARRIER_INFO_UPDATE = 303;
    private static final int MSG_CLOCK_VISIBILITY_CHANGED = 307;
    private static final int MSG_CONFIGURATION_CHANGED = 315;
    private static final int MSG_DELAY_SIM_DETECTED = 314;
    private static final int MSG_DEVICE_PROVISIONED = 308;
    private static final int MSG_DM_KEYGUARD_UPDATE = 312;
    private static final int MSG_DOWNLOAD_CALIBRATION_DATA_UPDATE = 325;
    private static final int MSG_GPRS_TYPE_SELECT = 323;
    private static final int MSG_KEYGUARD_RESET_DISMISS = 317;
    private static final int MSG_KEYGUARD_SIM_NAME_UPDATE = 319;
    private static final int MSG_KEYGUARD_UPDATE_LAYOUT = 318;
    private static final int MSG_LOCK_SCREEN_MISSED_CALL = 310;
    private static final int MSG_LOCK_SCREEN_WALLPAPER_SET = 311;
    private static final int MSG_MODEM_RESET = 320;
    private static final int MSG_PHONE_STATE_CHANGED = 306;
    private static final int MSG_PRE_3G_SWITCH = 321;
    private static final int MSG_RINGER_MODE_CHANGED = 305;
    private static final int MSG_SIMINFO_CHANGED = 316;
    private static final int MSG_SIM_DETECTED = 313;
    private static final int MSG_SIM_STATE_CHANGE = 304;
    private static final int MSG_SYSTEM_STATE = 324;
    private static final int MSG_TIME_UPDATE = 301;
    private static final int MSG_UNLOCK_KEYGUARD = 309;
    private static final int NEWSIMINSERTED = 2;
    public static final String OMADM_LAWMO_LOCK = "com.mediatek.dm.LAWMO_LOCK";
    public static final String OMADM_LAWMO_UNLOCK = "com.mediatek.dm.LAWMO_UNLOCK";
    private static final int SYSTEM_STATE_BOOTUP = 1;
    private static final int SYSTEM_STATE_SHUTDOWN = 0;
    private static final String TAG = "KeyguardUpdateMonitor";
    public static int dual_sim_setting = -1;
    protected static final boolean mIsCMCC = SystemProperties.get("ro.operator.optr").equals("OP01");
    private boolean DEBUG;
    private AlertDialog apn_mSIMCardDialog;
    private BatteryStatus mBatteryStatus;
    boolean mCalibrationData;
    private int mCardTotal;
    private boolean mClockVisible;
    private ComponentName mComponentName;
    private ContentObserver mContentObserver;
    private final Context mContext;
    private ArrayList mDeviceInfoCallbacks;
    private boolean mDevicePluggedIn;
    private boolean mDeviceProvisioned;
    private AlertDialog mDialog;
    private int mFailedAttempts;
    private AlertDialog mGPRSDialog1;
    private AlertDialog mGPRSDialog2;
    private Handler mHandler;
    private boolean mInPortrait;
    private ArrayList mInfoCallbacks;
    private boolean mKeyboardOpen;
    private boolean mKeyguardBypassEnabled;
    private LockPatternUtils mLockPatternUtils;
    private int mMissedCall;
    boolean mNetSearching;
    boolean mNetSearchingGemini;
    private int mPINFlag;
    private ArrayList mPhoneCallbacks;
    private int mPhoneState;
    private PhoneStateListener mPhoneStateListener;
    private PhoneStateListener mPhoneStateListenerGemini;
    private PackageManager mPm;
    private View mPromptView;
    private RadioStateCallback mRadioStateCallback;
    private int mRingMode;
    private AlertDialog mSIMCardDialog;
    private boolean mSIMRemoved;
    private com.android.internal.telephony.IccCard.State mSim2LastState;
    private com.android.internal.telephony.IccCard.State mSim2State;
    private com.android.internal.telephony.IccCard.State mSimLastState;
    private com.android.internal.telephony.IccCard.State mSimState;
    private ArrayList mSimStateCallbacks;
    private SIMStatus mSimStatus;
    private SystemStateCallback mSystemStateCallback;
    private CharSequence mTelephonyPlmn;
    private CharSequence mTelephonyPlmnGemini;
    private CharSequence mTelephonySpn;
    private CharSequence mTelephonySpnGemini;
    private boolean mWallpaperSetComplete;
    private boolean mWallpaperUpdate;
    private boolean shouldPopup;

    public KeyguardUpdateMonitor(Context context)
    {
        DEBUG = true;
        mSimState = com.android.internal.telephony.IccCard.State.UNKNOWN;
        mSim2State = com.android.internal.telephony.IccCard.State.UNKNOWN;
        mSimLastState = com.android.internal.telephony.IccCard.State.UNKNOWN;
        mSim2LastState = com.android.internal.telephony.IccCard.State.UNKNOWN;
        mFailedAttempts = 0;
        mDialog = null;
        mSIMCardDialog = null;
        mPromptView = null;
        mSIMRemoved = false;
        mCardTotal = 0;
        mGPRSDialog1 = null;
        mGPRSDialog2 = null;
        mMissedCall = 0;
        mWallpaperSetComplete = true;
        shouldPopup = false;
        mInfoCallbacks = Lists.newArrayList();
        mSimStateCallbacks = Lists.newArrayList();
        mPhoneCallbacks = Lists.newArrayList();
        mDeviceInfoCallbacks = Lists.newArrayList();
        mNetSearching = false;
        mNetSearchingGemini = false;
        mPINFlag = 0;
        mCalibrationData = true;
        apn_mSIMCardDialog = null;
        mContext = context;
        mHandler = new Handler() {

            final KeyguardUpdateMonitor this$0;

            public void handleMessage(Message message)
            {
                switch (message.what)
                {
                default:
                    break;

                case 301: 
                    handleTimeUpdate();
                    break;

                case 302: 
                    handleBatteryUpdate((BatteryStatus)message.obj);
                    break;

                case 303: 
                    handleCarrierInfoUpdate();
                    break;

                case 304: 
                    handleSimStateChange((SimArgs)message.obj);
                    break;

                case 305: 
                    handleRingerModeChange(message.arg1);
                    break;

                case 306: 
                    handlePhoneStateChanged((String)message.obj);
                    break;

                case 307: 
                    handleClockVisibilityChanged();
                    break;

                case 308: 
                    handleDeviceProvisioned();
                    break;

                case 309: 
                    if (DEBUG)
                        Xlog.d("KeyguardUpdateMonitor", "handleUNLOCK_KEYGUARD");
                    for (int i = 0; i < mInfoCallbacks.size(); i++)
                        ((InfoCallback)mInfoCallbacks.get(i)).onUnlockKeyguard();

                    break;

                case 310: 
                    handleMissedCall(message.arg1);
                    break;

                case 311: 
                    handleWallpaperSet();
                    break;

                case 312: 
                    handleDMKeyguardUpdate();
                    break;

                case 313: 
                    handleSIMCardChanged(message.arg1, message.arg2);
                    break;

                case 314: 
                    handleDelaySIMCardChanged();
                    break;

                case 315: 
                    updateResources();
                    break;

                case 316: 
                    handleSIMInfoChanged(message.arg1);
                    break;

                case 317: 
                    mPINFlag = 0;
                    break;

                case 318: 
                    Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("MSG_KEYGUARD_UPDATE_LAYOUT, msg.arg1=").append(message.arg1).toString());
                    handleLockScreenUpdateLayout(message.arg1);
                    break;

                case 319: 
                    Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("MSG_KEYGUARD_SIM_NAME_UPDATE, msg.arg1=").append(message.arg1).toString());
                    handleSIMNameUpdate(message.arg1);
                    break;

                case 320: 
                    Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("MSG_MODEM_RESET, msg.arg1=").append(message.arg1).toString());
                    handleRadioStateChanged(message.arg1);
                    break;

                case 321: 
                    handle3GSwitchEvent();
                    break;

                case 322: 
                    handleBootupModePick();
                    break;

                case 323: 
                    Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("msg.arg1 = ").append(message.arg1).toString());
                    Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("msg.arg2 = ").append(message.arg2).toString());
                    if (message.arg1 != 0)
                        handleGprsTypePickSim2();
                    else
                        handleGprsTypePickSim1();
                    break;

                case 324: 
                    Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("MSG_SYSTEM_STATE, msg.arg1=").append(message.arg1).toString());
                    handleSystemStateChanged(message.arg1);
                    break;

                case 325: 
                    handleDownloadCalibrationDataUpdate();
                    break;
                }
            }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
        }
;
        DM_Check_Locked();
        mLockPatternUtils = new LockPatternUtils(mContext);
        if (isGMSRunning())
        {
            Xlog.i("KeyguardUpdateMonitor", "first reboot, GMS is running");
            mLockPatternUtils.setLockScreenDisabled(true);
            mHandler.sendMessage(mHandler.obtainMessage(314));
        }
        mPhoneStateListener = new PhoneStateListener() {

            final KeyguardUpdateMonitor this$0;

            public void onServiceStateChanged(ServiceState servicestate)
            {
                if (servicestate == null) goto _L2; else goto _L1
_L1:
                int i = servicestate.getRegState();
                if (!mNetSearching || i == 2) goto _L4; else goto _L3
_L3:
                int j;
                Xlog.d("KeyguardUpdateMonitor", "PhoneStateListener, sim1 searching finished");
                mNetSearching = false;
                j = 0;
_L10:
                if (j < mInfoCallbacks.size()) goto _L5; else goto _L4
_L4:
                Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("PhoneStateListener, sim1 on service state changed before.state=").append(i).toString());
                if (i != 2) goto _L2; else goto _L6
_L6:
                mNetSearching = true;
                i = 0;
_L8:
                if (i < mInfoCallbacks.size()) goto _L7; else goto _L2
_L2:
                return;
_L7:
                ((InfoCallback)mInfoCallbacks.get(i)).onSearchNetworkUpdate(0, true);
                i++;
                if (true) goto _L8; else goto _L5
_L5:
                ((InfoCallback)mInfoCallbacks.get(j)).onSearchNetworkUpdate(0, false);
                j++;
                if (true) goto _L10; else goto _L9
_L9:
            }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
        }
;
        mPhoneStateListenerGemini = new PhoneStateListener() {

            final KeyguardUpdateMonitor this$0;

            public void onServiceStateChanged(ServiceState servicestate)
            {
                if (servicestate == null) goto _L2; else goto _L1
_L1:
                int j = servicestate.getRegState();
                if (!mNetSearchingGemini || j == 2) goto _L4; else goto _L3
_L3:
                int i;
                Xlog.d("KeyguardUpdateMonitor", "PhoneStateListener, sim2 searching finished");
                mNetSearchingGemini = false;
                i = 0;
_L10:
                if (i < mInfoCallbacks.size()) goto _L5; else goto _L4
_L4:
                Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("PhoneStateListener, sim2 on service state changed before.state=").append(j).toString());
                if (j != 2) goto _L2; else goto _L6
_L6:
                mNetSearchingGemini = true;
                i = 0;
_L8:
                if (i < mInfoCallbacks.size()) goto _L7; else goto _L2
_L2:
                return;
_L7:
                ((InfoCallback)mInfoCallbacks.get(i)).onSearchNetworkUpdate(1, true);
                i++;
                if (true) goto _L8; else goto _L5
_L5:
                ((InfoCallback)mInfoCallbacks.get(i)).onSearchNetworkUpdate(1, false);
                i++;
                if (true) goto _L10; else goto _L9
_L9:
            }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
        }
;
        ((TelephonyManager)mContext.getSystemService("phone")).listenGemini(mPhoneStateListener, 1, 0);
        ((TelephonyManager)mContext.getSystemService("phone")).listenGemini(mPhoneStateListenerGemini, 1, 1);
        mKeyguardBypassEnabled = context.getResources().getBoolean(0x111000e);
        boolean flag;
        if (android.provider.Settings.Secure.getInt(mContext.getContentResolver(), "device_provisioned", 0) == 0)
            flag = false;
        else
            flag = true;
        mDeviceProvisioned = flag;
        mDeviceProvisioned = true;
        if (!mDeviceProvisioned)
        {
            mContentObserver = new ContentObserver(mHandler) {

                final KeyguardUpdateMonitor this$0;

                public void onChange(boolean flag2)
                {
                    boolean flag3 = false;
                    super.onChange(flag2);
                    KeyguardUpdateMonitor keyguardupdatemonitor = KeyguardUpdateMonitor.this;
                    if (android.provider.Settings.Secure.getInt(mContext.getContentResolver(), "device_provisioned", 0) != 0)
                        flag3 = true;
                    keyguardupdatemonitor.mDeviceProvisioned = flag3;
                    if (mDeviceProvisioned && mContentObserver != null)
                    {
                        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
                        mContentObserver = null;
                    }
                    if (DEBUG)
                        Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("DEVICE_PROVISIONED state = ").append(mDeviceProvisioned).toString());
                }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super(handler);
            }
            }
;
            mContext.getContentResolver().registerContentObserver(android.provider.Settings.Secure.getUriFor("device_provisioned"), false, mContentObserver);
            boolean flag1;
            if (android.provider.Settings.Secure.getInt(mContext.getContentResolver(), "device_provisioned", 0) == 0)
                flag1 = false;
            else
                flag1 = true;
            if (flag1 != mDeviceProvisioned)
            {
                mDeviceProvisioned = flag1;
                if (mDeviceProvisioned)
                    mHandler.sendMessage(mHandler.obtainMessage(308));
            }
        }
        mBatteryStatus = new BatteryStatus(1, 100, 0, 0);
        mDevicePluggedIn = true;
        mTelephonyPlmn = getDefaultPlmn();
        mTelephonyPlmnGemini = getDefaultPlmn();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.intent.action.TIME_TICK");
        intentfilter.addAction("android.intent.action.TIME_SET");
        intentfilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentfilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentfilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentfilter.addAction("android.intent.action.NEW_SIM_DETECTED");
        intentfilter.addAction("android.intent.action.DEFAULT_SIM_REMOVED");
        intentfilter.addAction("android.intent.action.SIM_INFO_UPDATE");
        intentfilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        intentfilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        intentfilter.addAction("android.intent.action.SIM_INSERTED_STATUS");
        intentfilter.addAction("android.intent.action.SIM_NAME_UPDATE");
        intentfilter.addAction("android.intent.action.RADIO_OFF");
        intentfilter.addAction(GeminiPhone.EVENT_3G_SWITCH_START_MD_RESET);
        intentfilter.addAction("android.intent.action.PHONE_STATE");
        intentfilter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED");
        intentfilter.addAction("android.media.RINGER_MODE_CHANGED");
        intentfilter.addAction("android.provider.Telephony.DUAL_SIM_MODE_SELECT");
        intentfilter.addAction("android.provider.Telephony.GPRS_CONNECTION_TYPE_SELECT");
        intentfilter.addAction("android.provider.Telephony.UNLOCK_KEYGUARD");
        intentfilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentfilter.addAction("com.android.phone.NotificationMgr.MissedCall_intent");
        intentfilter.addAction("com.mediatek.lockscreen.action.WALLPAPER_SET");
        intentfilter.addAction("android.intent.action.normal.boot");
        intentfilter.addAction("com.mediatek.dm.LAWMO_LOCK");
        intentfilter.addAction("com.mediatek.dm.LAWMO_UNLOCK");
        intentfilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentfilter.addAction("android.intent.action.ACTION_PREBOOT_IPO");
        intentfilter.addAction("android.intent.action.DOWNLOAD_CALIBRATION_DATA");
        context.registerReceiver(new BroadcastReceiver() {

            final KeyguardUpdateMonitor this$0;

            public void onReceive(Context context1, Intent intent)
            {
                String s = intent.getAction();
                if (DEBUG)
                    Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("received broadcast ").append(s).toString());
                if (!"android.intent.action.TIME_TICK".equals(s) && !"android.intent.action.TIME_SET".equals(s) && !"android.intent.action.TIMEZONE_CHANGED".equals(s))
                {
                    if (!"android.provider.Telephony.SPN_STRINGS_UPDATED".equals(s))
                    {
                        if (!"android.intent.action.BATTERY_CHANGED".equals(s))
                        {
                            if (!"android.intent.action.SIM_STATE_CHANGED".equals(s))
                            {
                                if (!"android.intent.action.ACTION_SHUTDOWN_IPO".equals(s))
                                {
                                    if (!"android.intent.action.RADIO_OFF".equals(s))
                                    {
                                        if (!GeminiPhone.EVENT_3G_SWITCH_START_MD_RESET.equals(s))
                                        {
                                            if (!"android.intent.action.SIM_INSERTED_STATUS".equals(s))
                                            {
                                                if (!"android.intent.action.SIM_NAME_UPDATE".equals(s))
                                                {
                                                    if (!"android.intent.action.CONFIGURATION_CHANGED".equals(s))
                                                    {
                                                        if (!"android.intent.action.NEW_SIM_DETECTED".equals(s))
                                                        {
                                                            if (!"android.intent.action.DEFAULT_SIM_REMOVED".equals(s))
                                                            {
                                                                if (!"android.intent.action.normal.boot".equals(s))
                                                                {
                                                                    if (!"android.intent.action.SIM_INFO_UPDATE".equals(s))
                                                                    {
                                                                        if (!"android.media.RINGER_MODE_CHANGED".equals(s))
                                                                        {
                                                                            if (!"android.intent.action.PHONE_STATE".equals(s))
                                                                            {
                                                                                if (!"android.provider.Telephony.DUAL_SIM_MODE_SELECT".equals(s))
                                                                                {
                                                                                    if (!"android.provider.Telephony.UNLOCK_KEYGUARD".equals(s))
                                                                                    {
                                                                                        if (!"android.provider.Telephony.GPRS_CONNECTION_TYPE_SELECT".equals(s))
                                                                                        {
                                                                                            if (!"android.intent.action.AIRPLANE_MODE".equals(s))
                                                                                            {
                                                                                                if (!"com.android.phone.NotificationMgr.MissedCall_intent".equals(s))
                                                                                                {
                                                                                                    if (!"com.mediatek.lockscreen.action.WALLPAPER_SET".equals(s))
                                                                                                    {
                                                                                                        if (!"com.mediatek.dm.LAWMO_LOCK".equals(s))
                                                                                                        {
                                                                                                            if (!"com.mediatek.dm.LAWMO_UNLOCK".equals(s))
                                                                                                            {
                                                                                                                if (!"android.intent.action.ACTION_SHUTDOWN".equals(s))
                                                                                                                {
                                                                                                                    if (!"android.intent.action.ACTION_PREBOOT_IPO".equals(s))
                                                                                                                    {
                                                                                                                        if ("android.intent.action.DOWNLOAD_CALIBRATION_DATA".equals(s))
                                                                                                                        {
                                                                                                                            mCalibrationData = intent.getBooleanExtra("calibrationData", true);
                                                                                                                            Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("mCalibrationData = ").append(mCalibrationData).toString());
                                                                                                                            mHandler.sendMessage(mHandler.obtainMessage(325));
                                                                                                                        }
                                                                                                                    } else
                                                                                                                    {
                                                                                                                        Message message = mHandler.obtainMessage(324);
                                                                                                                        message.arg1 = 1;
                                                                                                                        mHandler.sendMessage(message);
                                                                                                                    }
                                                                                                                } else
                                                                                                                {
                                                                                                                    Message message1 = mHandler.obtainMessage(324);
                                                                                                                    message1.arg1 = 0;
                                                                                                                    mHandler.sendMessage(message1);
                                                                                                                }
                                                                                                            } else
                                                                                                            {
                                                                                                                KeyguardUpdateMonitor.KEYGUARD_DM_LOCKED = false;
                                                                                                                Log.i("KeyguardUpdateMonitor", (new StringBuilder()).append("OMADM_LAWMO_UNLOCK received, KEYGUARD_DM_LOCKED=").append(KeyguardUpdateMonitor.KEYGUARD_DM_LOCKED).toString());
                                                                                                                mHandler.sendMessage(mHandler.obtainMessage(312));
                                                                                                            }
                                                                                                        } else
                                                                                                        {
                                                                                                            KeyguardUpdateMonitor.KEYGUARD_DM_LOCKED = true;
                                                                                                            Log.i("KeyguardUpdateMonitor", (new StringBuilder()).append("OMADM_LAWMO_LOCK received, KEYGUARD_DM_LOCKED=").append(KeyguardUpdateMonitor.KEYGUARD_DM_LOCKED).toString());
                                                                                                            mHandler.sendMessage(mHandler.obtainMessage(312));
                                                                                                        }
                                                                                                    } else
                                                                                                    {
                                                                                                        mWallpaperSetComplete = intent.getBooleanExtra("com.mediatek.lockscreen.extra.COMPLETE", false);
                                                                                                        Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("WALLPAPER_SET:").append(mWallpaperSetComplete).toString());
                                                                                                        if (mWallpaperSetComplete)
                                                                                                            mHandler.sendMessage(mHandler.obtainMessage(311, Integer.valueOf(0)));
                                                                                                    }
                                                                                                } else
                                                                                                {
                                                                                                    mMissedCall = intent.getExtras().getInt("MissedCallNumber");
                                                                                                    Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("MissedCall_intent received, mMissedCall=").append(mMissedCall).toString());
                                                                                                    mHandler.sendMessage(mHandler.obtainMessage(310, mMissedCall, 0));
                                                                                                }
                                                                                            } else
                                                                                            {
                                                                                                Xlog.d("KeyguardUpdateMonitor", "ACTION_AIRPLANE_MODE_CHANGED, received");
                                                                                                if (mDialog != null && intent.getExtras().getBoolean("state"))
                                                                                                {
                                                                                                    mDialog.dismiss();
                                                                                                    mDialog = null;
                                                                                                }
                                                                                            }
                                                                                        } else
                                                                                        {
                                                                                            Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("ACTION_GPRS_CONNECTION_TYPE_SELECT, simId =  ").append(intent.getIntExtra("simId", -1)).toString());
                                                                                            mHandler.sendMessage(mHandler.obtainMessage(323, intent.getIntExtra("simId", 0), 0));
                                                                                        }
                                                                                    } else
                                                                                    {
                                                                                        Xlog.i("KeyguardUpdateMonitor", "ACTION_UNLOCK_KEYGUARD, received");
                                                                                        mHandler.sendMessage(mHandler.obtainMessage(309));
                                                                                    }
                                                                                } else
                                                                                {
                                                                                    Xlog.i("KeyguardUpdateMonitor", "ACTION_DUAL_SIM_MODE_SELECT, received");
                                                                                    mHandler.sendMessage(mHandler.obtainMessage(322));
                                                                                }
                                                                            } else
                                                                            {
                                                                                String s1 = intent.getStringExtra("state");
                                                                                mHandler.sendMessage(mHandler.obtainMessage(306, s1));
                                                                            }
                                                                        } else
                                                                        {
                                                                            if (DEBUG)
                                                                                Xlog.i("KeyguardUpdateMonitor", "RINGER_MODE_CHANGED_ACTION received");
                                                                            mHandler.sendMessage(mHandler.obtainMessage(305, intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1), 0));
                                                                        }
                                                                    } else
                                                                    {
                                                                        int i = intent.getIntExtra("slotId", 0);
                                                                        Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("sim info update, slotId=").append(i).toString());
                                                                        mHandler.sendMessage(mHandler.obtainMessage(316, i, 0));
                                                                    }
                                                                } else
                                                                {
                                                                    Log.i("KeyguardUpdateMonitor", (new StringBuilder()).append("received normal boot, shouldPop=").append(shouldPopup).toString());
                                                                    if (mSimStatus != null && shouldPopup)
                                                                    {
                                                                        shouldPopup = false;
                                                                        mHandler.sendMessage(mHandler.obtainMessage(313, mSimStatus.getSimType(), mSimStatus.getSIMCardCount()));
                                                                    }
                                                                }
                                                            } else
                                                            {
                                                                int j = intent.getIntExtra("simCount", 0);
                                                                if (!isGMSRunning())
                                                                {
                                                                    boolean flag2 = SystemProperties.get("sys.boot.reason").equals("1");
                                                                    Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("default_sim_removed, total=").append(j).append(", bootReason=").append(flag2).toString());
                                                                    if (!flag2)
                                                                    {
                                                                        mHandler.sendMessage(mHandler.obtainMessage(313, 1, j));
                                                                    } else
                                                                    {
                                                                        KeyguardUpdateMonitor keyguardupdatemonitor = KeyguardUpdateMonitor.this;
                                                                        j = new SIMStatus(1, j);
                                                                        keyguardupdatemonitor.mSimStatus = j;
                                                                        shouldPopup = true;
                                                                    }
                                                                } else
                                                                {
                                                                    shouldPopup = true;
                                                                    KeyguardUpdateMonitor keyguardupdatemonitor1 = KeyguardUpdateMonitor.this;
                                                                    SIMStatus simstatus = new SIMStatus(1, j);
                                                                    keyguardupdatemonitor1.mSimStatus = simstatus;
                                                                }
                                                            }
                                                        } else
                                                        {
                                                            int k = intent.getIntExtra("newSIMSlot", 0);
                                                            if (!isGMSRunning())
                                                            {
                                                                boolean flag3 = SystemProperties.get("sys.boot.reason").equals("1");
                                                                Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("new_sim_detected, total=").append(k).append(", bootReason=").append(flag3).toString());
                                                                if (!flag3)
                                                                {
                                                                    mHandler.sendMessage(mHandler.obtainMessage(313, 2, k));
                                                                } else
                                                                {
                                                                    KeyguardUpdateMonitor keyguardupdatemonitor2 = KeyguardUpdateMonitor.this;
                                                                    k = new SIMStatus(2, k);
                                                                    keyguardupdatemonitor2.mSimStatus = k;
                                                                    shouldPopup = true;
                                                                }
                                                            } else
                                                            {
                                                                KeyguardUpdateMonitor keyguardupdatemonitor3 = KeyguardUpdateMonitor.this;
                                                                SIMStatus simstatus1 = new SIMStatus(2, k);
                                                                keyguardupdatemonitor3.mSimStatus = simstatus1;
                                                                shouldPopup = true;
                                                            }
                                                        }
                                                    } else
                                                    {
                                                        mHandler.sendMessage(mHandler.obtainMessage(315));
                                                    }
                                                } else
                                                {
                                                    int l = intent.getIntExtra("slotId", 0);
                                                    Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("SIM_NAME_UPDATE, slotId=").append(l).toString());
                                                    mHandler.sendMessage(mHandler.obtainMessage(319, l, 0));
                                                }
                                            } else
                                            {
                                                int i1 = intent.getIntExtra("slotId", 0);
                                                Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("SIM_INSERTED_STATUS, slotId=").append(i1).toString());
                                                mHandler.sendMessage(mHandler.obtainMessage(318, i1, 0));
                                            }
                                        } else
                                        {
                                            Xlog.i("KeyguardUpdateMonitor", "received EVENT_3G_SWITCH_START_MD_RESET message");
                                            mHandler.sendMessage(mHandler.obtainMessage(321));
                                        }
                                    } else
                                    {
                                        int j1 = intent.getIntExtra("slotId", 0);
                                        Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("received ACTION_RADIO_OFF message, slotId=").append(j1).toString());
                                        mHandler.sendMessage(mHandler.obtainMessage(320, j1, 0));
                                    }
                                } else
                                {
                                    Xlog.i("KeyguardUpdateMonitor", "received the IPO shutdown message");
                                    mHandler.sendMessage(mHandler.obtainMessage(317));
                                    Message message2 = mHandler.obtainMessage(324);
                                    message2.arg1 = 0;
                                    mHandler.sendMessage(message2);
                                }
                            } else
                            {
                                String s2 = intent.getStringExtra("ss");
                                int k1 = intent.getIntExtra("simId", 0);
                                if ("READY".equals(s2))
                                    if (k1 != 1)
                                        mPINFlag = 0x1010 & mPINFlag;
                                    else
                                        mPINFlag = 0x101 & mPINFlag;
                                Log.i("KeyguardUpdateMonitor", (new StringBuilder()).append("ACTION_SIM_STATE_CHANGED, stateExtra=").append(s2).append(",simId=").append(k1).toString());
                                if ("IMSI" != s2 && "LOADED" != s2)
                                    mHandler.sendMessage(mHandler.obtainMessage(304, SimArgs.fromIntent(intent)));
                            }
                        } else
                        {
                            int j2 = intent.getIntExtra("status", 1);
                            int i2 = intent.getIntExtra("plugged", 0);
                            int k2 = intent.getIntExtra("level", 0);
                            int l1 = intent.getIntExtra("health", 1);
                            Log.i("KeyguardUpdateMonitor", (new StringBuilder()).append("ACTION_BATTERY_CHANGED, status=").append(j2).append(",plugged=").append(i2).append(", level=").append(k2).append(", health=").append(l1).toString());
                            Object obj = mHandler;
                            BatteryStatus batterystatus = new BatteryStatus(j2, k2, i2, l1);
                            obj = ((Handler) (obj)).obtainMessage(302, batterystatus);
                            mHandler.sendMessage(((Message) (obj)));
                        }
                    } else
                    {
                        if (intent.getIntExtra("simId", 0) != 0)
                        {
                            mTelephonyPlmnGemini = getTelephonyPlmnFrom(intent);
                            mTelephonySpnGemini = getTelephonySpnFrom(intent);
                            Log.d("KeyguardUpdateMonitor", (new StringBuilder()).append("SPN_STRINGS_UPDATED_ACTION, update sim2, plmn=").append(mTelephonyPlmnGemini).append(", spn=").append(mTelephonySpnGemini).toString());
                        } else
                        {
                            mTelephonyPlmn = getTelephonyPlmnFrom(intent);
                            mTelephonySpn = getTelephonySpnFrom(intent);
                            Log.d("KeyguardUpdateMonitor", (new StringBuilder()).append("SPN_STRINGS_UPDATED_ACTION, update sim1, plmn=").append(mTelephonyPlmn).append(", spn=").append(mTelephonySpn).toString());
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(303));
                    }
                } else
                {
                    mHandler.sendMessage(mHandler.obtainMessage(301));
                }
            }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
        }
, intentfilter);
        mWallpaperUpdate = true;
    }

    private void DM_Check_Locked()
    {
        try
        {
            android.os.IBinder ibinder = ServiceManager.getService("DMAgent");
            if (ibinder != null)
            {
                boolean flag = com.mediatek.dmagent.DMAgent.Stub.asInterface(ibinder).isLockFlagSet();
                Log.i("KeyguardUpdateMonitor", (new StringBuilder()).append("DM_Check_Locked, the lock flag is:").append(flag).toString());
                KEYGUARD_DM_LOCKED = flag;
            } else
            {
                Log.i("KeyguardUpdateMonitor", "DM_Check_Locked, DMAgent doesn't exit");
            }
        }
        catch (Exception _ex)
        {
            Log.e("KeyguardUpdateMonitor", "get DM status failed!");
        }
    }

    private CharSequence getDefaultPlmn()
    {
        return mContext.getResources().getText(0x10402db);
    }

    private CharSequence getTelephonyPlmnFrom(Intent intent)
    {
        Object obj;
        if (!intent.getBooleanExtra("showPlmn", false))
        {
            obj = null;
        } else
        {
            obj = intent.getStringExtra("plmn");
            if (obj == null)
                obj = getDefaultPlmn();
        }
        return ((CharSequence) (obj));
    }

    private CharSequence getTelephonySpnFrom(Intent intent)
    {
        String s;
label0:
        {
            if (intent.getBooleanExtra("showSpn", false))
            {
                s = intent.getStringExtra("spn");
                if (s != null)
                    break label0;
            }
            s = null;
        }
        return s;
    }

    private void handle3GSwitchEvent()
    {
        mPINFlag = 0;
    }

    private void handleBatteryUpdate(BatteryStatus batterystatus)
    {
        int i;
        if (DEBUG)
            Log.d("KeyguardUpdateMonitor", "handleBatteryUpdate");
        i = isBatteryUpdateInteresting(mBatteryStatus, batterystatus);
        mBatteryStatus = batterystatus;
        if (!i) goto _L2; else goto _L1
_L1:
        i = 0;
_L5:
        if (i < mInfoCallbacks.size()) goto _L3; else goto _L2
_L2:
        return;
_L3:
        ((InfoCallback)mInfoCallbacks.get(i)).onRefreshBatteryInfo(shouldShowBatteryInfo(), isPluggedIn(batterystatus), batterystatus.level);
        i++;
        if (true) goto _L5; else goto _L4
_L4:
    }

    private void handleBootupModePick()
    {
    }

    private void handleCarrierInfoUpdate()
    {
        if (DEBUG)
            Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("handleCarrierInfoUpdate: plmn = ").append(mTelephonyPlmn).append(", spn = ").append(mTelephonySpn).toString());
        int i = 0;
        do
        {
            if (i >= mInfoCallbacks.size())
                return;
            ((InfoCallback)mInfoCallbacks.get(i)).onRefreshCarrierInfo(mTelephonyPlmn, mTelephonySpn);
            i++;
        } while (true);
    }

    private void handleClockVisibilityChanged()
    {
        if (DEBUG)
            Log.d("KeyguardUpdateMonitor", "handleClockVisibilityChanged()");
        int i = 0;
        do
        {
            if (i >= mDeviceInfoCallbacks.size())
                return;
            ((deviceInfoCallback)mDeviceInfoCallbacks.get(i)).onClockVisibilityChanged();
            i++;
        } while (true);
    }

    private void handleDelaySIMCardChanged()
    {
        (new Thread() {

            final KeyguardUpdateMonitor this$0;

            public void run()
            {
                while (1 == mPm.getComponentEnabledSetting(mComponentName) || mPm.getComponentEnabledSetting(mComponentName) == 0) 
                    try
                    {
                        Thread.sleep(500L);
                    }
                    catch (InterruptedException _ex) { }
                Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("handleDelaySIMCardChanged, exit, shouldPopup=").append(shouldPopup).toString());
                mLockPatternUtils.setLockScreenDisabled(false);
                if (shouldPopup)
                    mHandler.sendMessage(mHandler.obtainMessage(313, mSimStatus.getSimType(), mSimStatus.getSIMCardCount()));
            }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
        }
).start();
    }

    private void handleDownloadCalibrationDataUpdate()
    {
        Log.d("KeyguardUpdateMonitor", "handleDownloadCalibrationDataUpdate");
        int i = 0;
        do
        {
            if (i >= mInfoCallbacks.size())
                return;
            ((InfoCallback)mInfoCallbacks.get(i)).onDownloadCalibrationDataUpdate(mCalibrationData);
            i++;
        } while (true);
    }

    private void handleGprsTypePickSim1()
    {
        if (DEBUG)
            Xlog.d("KeyguardUpdateMonitor", "handleGprsTypePickSim1");
        mGPRSDialog1 = (new android.app.AlertDialog.Builder(mContext)).setTitle(0x2050037).setCancelable(false).setItems(0x2040001, new android.content.DialogInterface.OnClickListener() {

            final KeyguardUpdateMonitor this$0;

            public void onClick(DialogInterface dialoginterface, int i)
            {
                if (mGPRSDialog1 != null)
                {
                    mGPRSDialog1.dismiss();
                    mGPRSDialog1 = null;
                }
                i;
                JVM INSTR tableswitch 0 2: default 56
            //                           0 92
            //                           1 182
            //                           2 91;
                   goto _L1 _L2 _L3 _L4
_L4:
                break; /* Loop/switch isn't completed */
_L1:
                if (DEBUG)
                    Xlog.d("KeyguardUpdateMonitor", "handleGPRSTypePick, default, mode = NONE");
                android.provider.Settings.System.putInt(mContext.getContentResolver(), "gprs_connection_setting", 0);
_L9:
                return;
_L2:
                if (DEBUG)
                    Xlog.d("KeyguardUpdateMonitor", "handleGPRSTypePick, mode = SIM 1");
                ITelephony itelephony = com.android.internal.telephony.ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                if (itelephony == null) goto _L6; else goto _L5
_L5:
                itelephony.setGprsConnType(0, 1);
                itelephony.setGprsConnType(1, 0);
_L7:
                android.provider.Settings.System.putInt(mContext.getContentResolver(), "gprs_connection_setting", 1);
                continue; /* Loop/switch isn't completed */
_L6:
                try
                {
                    Xlog.d("KeyguardUpdateMonitor", "Connect to phone service error");
                    continue; /* Loop/switch isn't completed */
                }
                catch (RemoteException _ex)
                {
                    Xlog.d("KeyguardUpdateMonitor", "Connect to phone service error");
                }
                if (true) goto _L7; else goto _L3
_L3:
                if (DEBUG)
                    Xlog.d("KeyguardUpdateMonitor", "handleGPRSTypePick, mode = NONE");
                android.provider.Settings.System.putInt(mContext.getContentResolver(), "gprs_connection_setting", 0);
                if (true) goto _L9; else goto _L8
_L8:
            }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
        }
).create();
        mGPRSDialog1.getWindow().setType(2009);
        if (!mContext.getResources().getBoolean(0x1110008))
            mGPRSDialog1.getWindow().setFlags(4, 4);
        mGPRSDialog1.show();
    }

    private void handleRadioStateChanged(int i)
    {
        Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("handleRadioStateChanged, slotId=").append(i).append(", mSimState=").append(mSimState).append(", mSim2State=").append(mSim2State).toString());
        setPINDismiss(i, true, false);
        setPINDismiss(i, false, false);
        if ((i != 0 || com.android.internal.telephony.IccCard.State.PIN_REQUIRED != mSimState) && com.android.internal.telephony.IccCard.State.PUK_REQUIRED != mSimState)
        {
            if ((1 == i && com.android.internal.telephony.IccCard.State.PIN_REQUIRED == mSim2State || com.android.internal.telephony.IccCard.State.PUK_REQUIRED == mSim2State) && mRadioStateCallback != null)
                mRadioStateCallback.onRadioStateChanged(i);
        } else
        if (mRadioStateCallback != null)
            mRadioStateCallback.onRadioStateChanged(i);
    }

    private void handleSIMCardChanged(int i, int j)
    {
        switch (i)
        {
        default:
            throw new IllegalStateException((new StringBuilder()).append("Unknown SIMCard Changed:").append(i).toString());

        case 1: // '\001'
            mSIMRemoved = true;
            mCardTotal = j;
            Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("SIMCardRemoved, CardTotalNumber=").append(j).append(", mSIMRemoved=").append(mSIMRemoved).append(", mCardTotal=").append(mCardTotal).toString());
            Object obj = mContext.getResources().getString(0x2050079);
            mPromptView = LayoutInflater.from(mContext).inflate(0x207000f, null);
            ((TextView)mPromptView.findViewById(0x20c0034)).setText(((CharSequence) (obj)));
            InitView(mPromptView, ((String) (obj)));
            obj = new android.app.AlertDialog.Builder(mContext);
            ((android.app.AlertDialog.Builder) (obj)).setTitle(0x2050078);
            ((android.app.AlertDialog.Builder) (obj)).setCancelable(false);
            ((android.app.AlertDialog.Builder) (obj)).setView(mPromptView);
            ((android.app.AlertDialog.Builder) (obj)).setPositiveButton(0x2050077, new android.content.DialogInterface.OnClickListener() {

                final KeyguardUpdateMonitor this$0;

                public void onClick(DialogInterface dialoginterface, int k)
                {
                    Intent intent = new Intent("android.settings.GEMINI_MANAGEMENT");
                    intent.setFlags(0x10000000);
                    mContext.startActivity(intent);
                }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
            }
);
            ((android.app.AlertDialog.Builder) (obj)).setNegativeButton(0x2050088, null);
            mSIMCardDialog = ((android.app.AlertDialog.Builder) (obj)).create();
            mSIMCardDialog.getWindow().setType(2008);
            mSIMCardDialog.show();
            break;

        case 2: // '\002'
            mSIMRemoved = false;
            mCardTotal = j;
            Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("SIMCardInserted, CardTotalNumber=").append(j).append(", mSIMRemoved=").append(mSIMRemoved).append(", mCardTotal=").append(mCardTotal).toString());
            mPromptView = LayoutInflater.from(mContext).inflate(0x207000d, null);
            Object obj1;
            if (2 != j)
                obj1 = mContext.getResources().getString(0x2050075);
            else
                obj1 = mContext.getResources().getString(0x2050076);
            InitView(mPromptView, ((String) (obj1)));
            obj1 = new android.app.AlertDialog.Builder(mContext);
            ((android.app.AlertDialog.Builder) (obj1)).setTitle(0x2050074);
            ((android.app.AlertDialog.Builder) (obj1)).setCancelable(false);
            ((android.app.AlertDialog.Builder) (obj1)).setView(mPromptView);
            ((android.app.AlertDialog.Builder) (obj1)).setPositiveButton(0x2050077, new android.content.DialogInterface.OnClickListener() {

                final KeyguardUpdateMonitor this$0;

                public void onClick(DialogInterface dialoginterface, int k)
                {
                    Intent intent = new Intent("android.settings.GEMINI_MANAGEMENT_FRANCH");
                    intent.setFlags(0x10000000);
                    mContext.startActivity(intent);
                }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
            }
);
            ((android.app.AlertDialog.Builder) (obj1)).setNegativeButton(0x2050088, new android.content.DialogInterface.OnClickListener() {

                final KeyguardUpdateMonitor this$0;

                public void onClick(DialogInterface dialoginterface, int k)
                {
                    showApnSettingsDialog();
                }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
            }
);
            mSIMCardDialog = ((android.app.AlertDialog.Builder) (obj1)).create();
            mSIMCardDialog.getWindow().setType(2008);
            mSIMCardDialog.show();
            break;
        }
    }

    private void handleSIMInfoChanged(int i)
    {
        int j = 0;
        do
        {
            if (j >= mInfoCallbacks.size())
                return;
            ((InfoCallback)mInfoCallbacks.get(j)).onSIMInfoChanged(i);
            j++;
        } while (true);
    }

    private void handleSIMNameUpdate(int i)
    {
        int j = 0;
        do
        {
            if (j >= mInfoCallbacks.size())
            {
                updateResources();
                return;
            }
            ((InfoCallback)mInfoCallbacks.get(j)).onLockScreenUpdate(i);
            j++;
        } while (true);
    }

    private void handleSimStateChange(SimArgs simargs)
    {
        com.android.internal.telephony.IccCard.State state;
        com.android.internal.telephony.IccCard.State state1;
        state = simargs.simState;
        if (DEBUG)
            Log.d("KeyguardUpdateMonitor", (new StringBuilder()).append("handleSimStateChange: intentValue = ").append(simargs).append(" ").append("state resolved to ").append(state.toString()).toString());
        if (simargs.simId != 0)
        {
            state1 = mSim2State;
            mSim2LastState = mSim2State;
        } else
        {
            state1 = mSimState;
            mSimLastState = mSimState;
        }
        if (state == com.android.internal.telephony.IccCard.State.UNKNOWN || state == state1) goto _L2; else goto _L1
_L1:
        int i;
        if (state != com.android.internal.telephony.IccCard.State.NOT_READY);
        if (simargs.simId != 0)
        {
            mSim2State = state;
            Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("handleSimStateChange: mSim2State = ").append(mSim2State).toString());
        } else
        {
            mSimState = state;
            Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("handleSimStateChange: mSimState = ").append(mSimState).toString());
        }
        i = 0;
_L5:
        if (i < mSimStateCallbacks.size()) goto _L3; else goto _L2
_L2:
        return;
_L3:
        ((SimStateCallback)mSimStateCallbacks.get(i)).onSimStateChangedGemini(state, simargs.simId);
        i++;
        if (true) goto _L5; else goto _L4
_L4:
    }

    private void handleSystemStateChanged(int i)
    {
        if (mSystemStateCallback != null)
            switch (i)
            {
            default:
                if (DEBUG)
                    Xlog.e("KeyguardUpdateMonitor", "received unknown system state change event");
                break;

            case 0: // '\0'
                mSystemStateCallback.onSysShutdown();
                break;

            case 1: // '\001'
                mSystemStateCallback.onSysBootup();
                break;
            }
        else
        if (DEBUG)
            Xlog.d("KeyguardUpdateMonitor", "mSystemStateCallback is null, skipped!");
    }

    private void handleTimeUpdate()
    {
        if (DEBUG)
            Xlog.d("KeyguardUpdateMonitor", "handleTimeUpdate");
        int i = 0;
        do
        {
            if (i >= mInfoCallbacks.size())
                return;
            ((InfoCallback)mInfoCallbacks.get(i)).onTimeChanged();
            i++;
        } while (true);
    }

    private boolean isBatteryLow(BatteryStatus batterystatus)
    {
        boolean flag;
        if (batterystatus.level >= 16)
            flag = false;
        else
            flag = true;
        return flag;
    }

    private boolean isBatteryUpdateInteresting(BatteryStatus batterystatus, BatteryStatus batterystatus1)
    {
        boolean flag3 = true;
        boolean flag = isPluggedIn(batterystatus1);
        boolean flag2 = isPluggedIn(batterystatus);
        boolean flag1;
        if (flag2 != flag3 || flag != flag3 || batterystatus.status == batterystatus1.status)
            flag1 = false;
        else
            flag1 = flag3;
        if (flag2 == flag && !flag1 && batterystatus.level == batterystatus1.level && (flag || !isBatteryLow(batterystatus1) || batterystatus1.level == batterystatus.level))
            flag3 = false;
        return flag3;
    }

    private static boolean isPluggedIn(BatteryStatus batterystatus)
    {
        boolean flag = true;
        if (batterystatus.plugged != flag && batterystatus.plugged != 2)
            flag = false;
        return flag;
    }

    private void showApnSettingsDialog()
    {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setTitle(0x20500ca);
        builder.setMessage(0x20500cb);
        builder.setCancelable(false);
        builder.setPositiveButton(0x2050077, new android.content.DialogInterface.OnClickListener() {

            final KeyguardUpdateMonitor this$0;

            public void onClick(DialogInterface dialoginterface, int i)
            {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.ApnSettings");
                intent.setFlags(0x10000000);
                mContext.startActivity(intent);
            }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
        }
);
        apn_mSIMCardDialog = builder.create();
        apn_mSIMCardDialog.getWindow().setType(2009);
        apn_mSIMCardDialog.show();
    }

    public boolean DM_IsLocked()
    {
        return KEYGUARD_DM_LOCKED;
    }

    public void InitView(View view, String s)
    {
        long l3 = android.provider.Settings.System.getLong(mContext.getContentResolver(), "voice_call_sim_setting", -5L);
        long l1 = android.provider.Settings.System.getLong(mContext.getContentResolver(), "sms_sim_setting", -5L);
        long l = android.provider.Settings.System.getLong(mContext.getContentResolver(), "gprs_connection_sim_setting", -5L);
        long l2 = android.provider.Settings.System.getLong(mContext.getContentResolver(), "video_call_sim_setting", -5L);
        TelephonyManager telephonymanager = (TelephonyManager)mContext.getSystemService("phone");
        boolean flag1;
        if (telephonymanager == null || !telephonymanager.isVoiceCapable())
            flag1 = false;
        else
            flag1 = true;
        boolean flag;
        if (telephonymanager == null || !telephonymanager.isSmsCapable())
            flag = false;
        else
            flag = true;
        if (DEBUG)
            Log.i("KeyguardUpdateMonitor", (new StringBuilder()).append("InitView, isVoiceCapable=").append(flag1).append(" isSmsCapable ").append(flag).toString());
        Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("voicecallSlot=").append(l3).append(",smsSlot=").append(l1).append(",dateSlot=").append(l).append(", videoSlot=").append(l2).toString());
        ((TextView)view.findViewById(0x20c0034)).setText(s);
        if (!mSIMRemoved)
        {
            TextView textview4 = (TextView)view.findViewById(0x20c0035);
            TextView textview2 = (TextView)view.findViewById(0x20c0036);
            ((TextView)view.findViewById(0x20c0037)).setText(0x205008e);
            if (1 != mCardTotal)
            {
                if (2 != mCardTotal)
                {
                    addNameForSIMDetectDialog(textview4, 0);
                    addNameForSIMDetectDialog(textview2, 1);
                } else
                {
                    textview2.setVisibility(8);
                    addNameForSIMDetectDialog(textview4, 1);
                }
            } else
            {
                textview2.setVisibility(8);
                addNameForSIMDetectDialog(textview4, 0);
            }
        }
        TextView textview5 = (TextView)view.findViewById(0x20c0038);
        TextView textview3 = (TextView)view.findViewById(0x20c003a);
        if (!flag1)
        {
            textview5.setVisibility(8);
            textview3.setVisibility(8);
        } else
        {
            textview5.setText(0x205007a);
            textview3.setText(0x205008f);
        }
        textview3 = (TextView)view.findViewById(0x20c003c);
        if (!flag)
            textview3.setVisibility(8);
        else
            textview3.setText(0x205007b);
        ((TextView)view.findViewById(0x20c003e)).setText(0x205007c);
        textview5 = (TextView)view.findViewById(0x20c0039);
        textview3 = (TextView)view.findViewById(0x20c003b);
        if (!flag1)
        {
            textview5.setVisibility(8);
            textview3.setVisibility(8);
        } else
        {
            textview5.setBackgroundDrawable(getOptrDrawableById(l3));
            String s4 = getOptrNameById(l3);
            if (s4 != null)
                textview5.setText(s4);
            else
                textview5.setText(0x2050087);
            textview3.setBackgroundDrawable(getOptrDrawableById(l2));
            String s3 = getOptrNameById(l2);
            if (s3 != null)
                textview3.setText(s3);
            else
                textview3.setText(0x2050087);
        }
        TextView textview1 = (TextView)view.findViewById(0x20c003d);
        if (!flag)
        {
            textview1.setVisibility(8);
        } else
        {
            textview1.setBackgroundDrawable(getOptrDrawableById(l1));
            String s2 = getOptrNameById(l1);
            if (s2 != null)
                textview1.setText(s2);
            else
                textview1.setText(0x2050087);
        }
        TextView textview = (TextView)view.findViewById(0x20c003f);
        textview.setBackgroundDrawable(getOptrDrawableById(l));
        String s1 = getOptrNameById(l);
        if (s1 != null)
            textview.setText(s1);
        else
            textview.setText(0x2050087);
    }

    public boolean IsSIMInserted(int i)
    {
        boolean flag = false;
        ITelephony itelephony = com.android.internal.telephony.ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        if (itelephony == null) goto _L2; else goto _L1
_L1:
        boolean flag1 = itelephony.isSimInsert(i);
        if (flag1) goto _L2; else goto _L3
_L3:
        return flag;
        JVM INSTR pop ;
        Xlog.e("KeyguardUpdateMonitor", "Get sim insert status failure!");
        continue; /* Loop/switch isn't completed */
_L2:
        flag = true;
        if (true) goto _L3; else goto _L4
_L4:
    }

    public void addNameForSIMDetectDialog(TextView textview, int i)
    {
        textview.setBackgroundDrawable(getOptrDrawableBySlot(i));
        String s = getOptrNameBySlot(i);
        if (s != null)
            textview.setText(s);
        else
            textview.setText(0x2050087);
    }

    public void clearFailedAttempts()
    {
        mFailedAttempts = 0;
    }

    public int getBatteryLevel()
    {
        return mBatteryStatus.level;
    }

    public int getFailedAttempts()
    {
        return mFailedAttempts;
    }

    public com.android.internal.telephony.IccCard.State getLastSimState(int i)
    {
        com.android.internal.telephony.IccCard.State state;
        if (i != 1)
        {
            Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("mSimLastState = ").append(mSimLastState).toString());
            state = mSimLastState;
        } else
        {
            Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("mSim2LastState = ").append(mSim2LastState).toString());
            state = mSim2LastState;
        }
        return state;
    }

    public int getMissedCall()
    {
        return mMissedCall;
    }

    public Drawable getOptrDrawableById(long l)
    {
        Drawable drawable = null;
        if (l > 0L)
        {
            Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("getOptrDrawableById, xxsimId=").append(l).toString());
            android.provider.Telephony.SIMInfo siminfo = android.provider.Telephony.SIMInfo.getSIMInfoById(mContext, (int)l);
            if (siminfo != null)
                drawable = mContext.getResources().getDrawable(siminfo.mSimBackgroundRes);
            else
                Xlog.i("KeyguardUpdateMonitor", "getOptrDrawableBySlotId, return null");
        }
        return drawable;
    }

    public Drawable getOptrDrawableBySlot(long l)
    {
        if (l < 0L)
            throw new IndexOutOfBoundsException();
        Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("getOptrDrawableBySlot, xxslot=").append(l).toString());
        Object obj = android.provider.Telephony.SIMInfo.getSIMInfoBySlot(mContext, (int)l);
        if (obj != null)
        {
            obj = mContext.getResources().getDrawable(((android.provider.Telephony.SIMInfo) (obj)).mSimBackgroundRes);
        } else
        {
            Xlog.i("KeyguardUpdateMonitor", "getOptrDrawableBySlotId, return null");
            obj = null;
        }
        return ((Drawable) (obj));
    }

    public String getOptrNameById(long l)
    {
        Object obj;
        if (l <= 0L)
        {
            if (-1L != l)
            {
                if (-2L != l)
                {
                    if (0L != l)
                        obj = mContext.getResources().getString(0x2050085);
                    else
                        obj = mContext.getResources().getString(0x205007e);
                } else
                {
                    obj = mContext.getResources().getString(0x2050084);
                }
            } else
            {
                obj = mContext.getResources().getString(0x205007d);
            }
        } else
        {
            Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("getOptrNameById, xxsimId=").append(l).toString());
            obj = android.provider.Telephony.SIMInfo.getSIMInfoById(mContext, (int)l);
            if (obj != null)
            {
                Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("info=").append(((android.provider.Telephony.SIMInfo) (obj)).mDisplayName).toString());
                obj = ((android.provider.Telephony.SIMInfo) (obj)).mDisplayName;
            } else
            {
                Xlog.i("KeyguardUpdateMonitor", "getOptrNameBySlotId, return null");
                obj = null;
            }
        }
        return ((String) (obj));
    }

    public String getOptrNameBySlot(long l)
    {
        if (l < 0L)
            throw new IndexOutOfBoundsException();
        Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("getOptrNameBySlot, xxSlot=").append(l).toString());
        Object obj = android.provider.Telephony.SIMInfo.getSIMInfoBySlot(mContext, (int)l);
        if (obj != null)
        {
            Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("info=").append(((android.provider.Telephony.SIMInfo) (obj)).mDisplayName).toString());
            obj = ((android.provider.Telephony.SIMInfo) (obj)).mDisplayName;
        } else
        {
            Xlog.i("KeyguardUpdateMonitor", "getOptrNameBySlotId, return null");
            obj = null;
        }
        return ((String) (obj));
    }

    public String getOptrNameBySlotForCTA(long l)
    {
        Object obj;
        if (l < 0L)
        {
            obj = mContext.getResources().getString(0x2050085);
        } else
        {
            Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("getOptrNameBySlot, xxSlot=").append(l).toString());
            obj = android.provider.Telephony.SIMInfo.getSIMInfoBySlot(mContext, (int)l);
            if (obj != null && ((android.provider.Telephony.SIMInfo) (obj)).mDisplayName != null)
            {
                Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("info=").append(((android.provider.Telephony.SIMInfo) (obj)).mDisplayName).toString());
                obj = ((android.provider.Telephony.SIMInfo) (obj)).mDisplayName;
            } else
            {
                Xlog.i("KeyguardUpdateMonitor", "getOptrNameBySlotId, return null");
                if (1L != l)
                    obj = (new StringBuilder()).append(mContext.getResources().getString(0x2050071)).append(" 01").toString();
                else
                    obj = (new StringBuilder()).append(mContext.getResources().getString(0x2050071)).append(" 02").toString();
            }
        }
        return ((String) (obj));
    }

    public boolean getPINDismissFlag(int i, boolean flag)
    {
        Log.i("KeyguardUpdateMonitor", (new StringBuilder()).append("getPINDismissFlag, slotId=").append(i).append(", PINFlag=").append(flag).toString());
        int j;
        if (!flag)
            j = 1 << i + 2;
        else
            j = 1 << i;
        if ((j & mPINFlag) != 0)
            j = 1;
        else
            j = 0;
        return j;
    }

    public int getPhoneState()
    {
        return mPhoneState;
    }

    public boolean getSearchingFlag(int i)
    {
        boolean flag;
        if (1 != i)
            flag = mNetSearching;
        else
            flag = mNetSearchingGemini;
        return flag;
    }

    public com.android.internal.telephony.IccCard.State getSimState()
    {
        return mSimState;
    }

    public com.android.internal.telephony.IccCard.State getSimState(int i)
    {
        com.android.internal.telephony.IccCard.State state;
        if (i != 1)
        {
            Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("mSimState = ").append(mSimState).toString());
            state = mSimState;
        } else
        {
            Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("mSim2State = ").append(mSim2State).toString());
            state = mSim2State;
        }
        return state;
    }

    public CharSequence getTelephonyPlmn()
    {
        return mTelephonyPlmn;
    }

    public CharSequence getTelephonyPlmn(int i)
    {
        CharSequence charsequence;
        if (i != 1)
            charsequence = mTelephonyPlmn;
        else
            charsequence = mTelephonyPlmnGemini;
        return charsequence;
    }

    public CharSequence getTelephonySpn()
    {
        return mTelephonySpn;
    }

    public CharSequence getTelephonySpn(int i)
    {
        CharSequence charsequence;
        if (i != 1)
            charsequence = mTelephonySpn;
        else
            charsequence = mTelephonySpnGemini;
        return charsequence;
    }

    public boolean getWallpaperStatus()
    {
        return mWallpaperSetComplete;
    }

    public boolean getWallpaperUpdate()
    {
        return mWallpaperUpdate;
    }

    public void handleDMKeyguardUpdate()
    {
        Log.d("KeyguardUpdateMonitor", (new StringBuilder()).append("handleDMKeyguardUpdate: flag = ").append(KEYGUARD_DM_LOCKED).toString());
        int i = 0;
        do
        {
            if (i >= mDeviceInfoCallbacks.size())
                return;
            ((deviceInfoCallback)mDeviceInfoCallbacks.get(i)).onDMKeyguardUpdate();
            i++;
        } while (true);
    }

    protected void handleDeviceProvisioned()
    {
        int i = 0;
        do
        {
            if (i >= mDeviceInfoCallbacks.size())
            {
                if (mContentObserver != null)
                {
                    mContext.getContentResolver().unregisterContentObserver(mContentObserver);
                    mContentObserver = null;
                }
                return;
            }
            ((deviceInfoCallback)mDeviceInfoCallbacks.get(i)).onDeviceProvisioned();
            i++;
        } while (true);
    }

    public void handleGprsTypePickSim2()
    {
        if (DEBUG)
            Xlog.d("KeyguardUpdateMonitor", "handleGprsTypePickSim2");
        mGPRSDialog2 = (new android.app.AlertDialog.Builder(mContext)).setTitle(0x2050037).setCancelable(false).setItems(0x2040002, new android.content.DialogInterface.OnClickListener() {

            final KeyguardUpdateMonitor this$0;

            public void onClick(DialogInterface dialoginterface, int i)
            {
                if (mGPRSDialog2 != null)
                {
                    mGPRSDialog2.dismiss();
                    mGPRSDialog2 = null;
                }
                i;
                JVM INSTR tableswitch 0 2: default 56
            //                           0 92
            //                           1 182
            //                           2 91;
                   goto _L1 _L2 _L3 _L4
_L4:
                break; /* Loop/switch isn't completed */
_L1:
                if (DEBUG)
                    Xlog.d("KeyguardUpdateMonitor", "handleGPRSTypePick, default, mode = NONE");
                android.provider.Settings.System.putInt(mContext.getContentResolver(), "gprs_connection_setting", 0);
_L9:
                return;
_L2:
                if (DEBUG)
                    Xlog.d("KeyguardUpdateMonitor", "handleGPRSTypePick, mode = SIM 2");
                ITelephony itelephony = com.android.internal.telephony.ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                if (itelephony == null) goto _L6; else goto _L5
_L5:
                itelephony.setGprsConnType(0, 0);
                itelephony.setGprsConnType(1, 1);
_L7:
                android.provider.Settings.System.putInt(mContext.getContentResolver(), "gprs_connection_setting", 2);
                continue; /* Loop/switch isn't completed */
_L6:
                try
                {
                    Xlog.d("KeyguardUpdateMonitor", "Connect to phone service error");
                    continue; /* Loop/switch isn't completed */
                }
                catch (RemoteException _ex)
                {
                    Xlog.d("KeyguardUpdateMonitor", "Connect to phone service error");
                }
                if (true) goto _L7; else goto _L3
_L3:
                if (DEBUG)
                    Xlog.d("KeyguardUpdateMonitor", "handleGPRSTypePick, mode = NONE");
                android.provider.Settings.System.putInt(mContext.getContentResolver(), "gprs_connection_setting", 0);
                if (true) goto _L9; else goto _L8
_L8:
            }

            
            {
                this$0 = KeyguardUpdateMonitor.this;
                super();
            }
        }
).create();
        mGPRSDialog2.getWindow().setType(2009);
        if (!mContext.getResources().getBoolean(0x1110008))
            mGPRSDialog2.getWindow().setFlags(4, 4);
        mGPRSDialog2.show();
    }

    protected void handleLockScreenUpdateLayout(int i)
    {
        int j = 0;
        do
        {
            if (j >= mInfoCallbacks.size())
                return;
            ((InfoCallback)mInfoCallbacks.get(j)).onLockScreenUpdate(i);
            j++;
        } while (true);
    }

    public void handleMissedCall(int i)
    {
        int j = 0;
        do
        {
            if (j >= mInfoCallbacks.size())
                return;
            ((InfoCallback)mInfoCallbacks.get(j)).onMissedCallChanged(i);
            j++;
        } while (true);
    }

    protected void handlePhoneStateChanged(String s)
    {
        if (DEBUG)
            Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("handlePhoneStateChanged(").append(s).append(")").toString());
        if (mGPRSDialog1 != null)
            mGPRSDialog1.dismiss();
        if (mGPRSDialog2 != null)
            mGPRSDialog2.dismiss();
        if (mSIMCardDialog != null)
            mSIMCardDialog.dismiss();
        if (!TelephonyManager.EXTRA_STATE_IDLE.equals(s))
        {
            if (!TelephonyManager.EXTRA_STATE_OFFHOOK.equals(s))
            {
                if (TelephonyManager.EXTRA_STATE_RINGING.equals(s))
                    mPhoneState = 1;
            } else
            {
                mPhoneState = 2;
            }
        } else
        {
            mPhoneState = 0;
        }
        int i = 0;
        do
        {
            if (i >= mPhoneCallbacks.size())
                return;
            ((phoneStateCallback)mPhoneCallbacks.get(i)).onPhoneStateChanged(mPhoneState);
            i++;
        } while (true);
    }

    protected void handleRingerModeChange(int i)
    {
        if (DEBUG)
            Xlog.d("KeyguardUpdateMonitor", (new StringBuilder()).append("handleRingerModeChange(").append(i).append(")").toString());
        mRingMode = i;
        int j = 0;
        do
        {
            if (j >= mInfoCallbacks.size())
                return;
            ((InfoCallback)mInfoCallbacks.get(j)).onRingerModeChanged(i);
            j++;
        } while (true);
    }

    public void handleWallpaperSet()
    {
        mWallpaperUpdate = true;
        int i = 0;
        do
        {
            if (i >= mInfoCallbacks.size())
                return;
            ((InfoCallback)mInfoCallbacks.get(i)).onWallpaperSetComplete();
            i++;
        } while (true);
    }

    public boolean isClockVisible()
    {
        return mClockVisible;
    }

    public boolean isDeviceCharged()
    {
        boolean flag;
        if (mBatteryStatus.status != 5 && mBatteryStatus.level < 100)
            flag = false;
        else
            flag = true;
        return flag;
    }

    public boolean isDeviceCharging()
    {
        boolean flag;
        if (mBatteryStatus.status == 3 || mBatteryStatus.status == 4)
            flag = false;
        else
            flag = true;
        return flag;
    }

    public boolean isDevicePluggedIn()
    {
        return isPluggedIn(mBatteryStatus);
    }

    public boolean isDeviceProvisioned()
    {
        return mDeviceProvisioned;
    }

    boolean isGMSRunning()
    {
        boolean flag = false;
        boolean flag1 = true;
        mPm = mContext.getPackageManager();
        mComponentName = new ComponentName("com.google.android.setupwizard", "com.google.android.setupwizard.SetupWizardActivity");
        try
        {
            mPm.getInstallerPackageName("com.google.android.setupwizard");
        }
        catch (IllegalArgumentException _ex)
        {
            flag1 = false;
        }
        if (flag1 && (1 == mPm.getComponentEnabledSetting(mComponentName) || mPm.getComponentEnabledSetting(mComponentName) == 0))
            flag = true;
        Xlog.i("KeyguardUpdateMonitor", (new StringBuilder()).append("isGMSRunning, isGMSExist = ").append(flag1).append(", running = ").append(flag).toString());
        return flag;
    }

    public boolean isKeyguardBypassEnabled()
    {
        return mKeyguardBypassEnabled;
    }

    public boolean isPhoneAppReady()
    {
        Object obj = (ActivityManager)mContext.getSystemService("activity");
        boolean flag = false;
        obj = ((ActivityManager) (obj)).getRunningAppProcesses();
        if (obj != null)
        {
            obj = ((List) (obj)).iterator();
            do
            {
                if (!((Iterator) (obj)).hasNext())
                    break;
                if (!((android.app.ActivityManager.RunningAppProcessInfo)((Iterator) (obj)).next()).processName.equals("com.android.phone"))
                    continue;
                flag = true;
                break;
            } while (true);
            flag = flag;
        } else
        {
            Log.i("KeyguardUpdateMonitor", "runningAppInfo == null");
            flag = false;
        }
        return flag;
    }

    public void registerDeviceInfoCallback(deviceInfoCallback deviceinfocallback)
    {
        if (mDeviceInfoCallbacks.contains(deviceinfocallback))
        {
            if (DEBUG)
                Log.e("KeyguardUpdateMonitor", "Object tried to add another Device callback");
        } else
        {
            mDeviceInfoCallbacks.add(deviceinfocallback);
            deviceinfocallback.onClockVisibilityChanged();
            deviceinfocallback.onDeviceProvisioned();
        }
    }

    public void registerInfoCallback(InfoCallback infocallback)
    {
        if (mInfoCallbacks.contains(infocallback))
        {
            if (DEBUG)
                Log.e("KeyguardUpdateMonitor", "Object tried to add another INFO callback");
        } else
        {
            mInfoCallbacks.add(infocallback);
            infocallback.onRefreshBatteryInfo(shouldShowBatteryInfo(), isPluggedIn(mBatteryStatus), mBatteryStatus.level);
            infocallback.onTimeChanged();
            infocallback.onRingerModeChanged(mRingMode);
            infocallback.onRefreshCarrierInfo(mTelephonyPlmn, mTelephonySpn);
            infocallback.onDownloadCalibrationDataUpdate(mCalibrationData);
        }
    }

    public void registerPhoneStateCallback(phoneStateCallback phonestatecallback)
    {
        if (mPhoneCallbacks.contains(phonestatecallback))
        {
            if (DEBUG)
                Log.e("KeyguardUpdateMonitor", "Object tried to add another Phone callback");
        } else
        {
            mPhoneCallbacks.add(phonestatecallback);
            phonestatecallback.onPhoneStateChanged(mPhoneState);
        }
    }

    public void registerRadioStateCallback(RadioStateCallback radiostatecallback)
    {
        mRadioStateCallback = radiostatecallback;
    }

    public void registerSimStateCallback(SimStateCallback simstatecallback)
    {
        if (mSimStateCallbacks.contains(simstatecallback))
        {
            if (DEBUG)
                Xlog.e("KeyguardUpdateMonitor", "Object tried to add another SIM callback");
        } else
        {
            mSimStateCallbacks.add(simstatecallback);
            simstatecallback.onSimStateChangedGemini(mSimState, 0);
            simstatecallback.onSimStateChangedGemini(mSim2State, 1);
        }
    }

    public void registerSystemStateCallback(SystemStateCallback systemstatecallback)
    {
        mSystemStateCallback = systemstatecallback;
    }

    public void removeCallback(Object obj)
    {
        mInfoCallbacks.remove(obj);
        mSimStateCallbacks.remove(obj);
        mPhoneCallbacks.remove(obj);
        mDeviceInfoCallbacks.remove(obj);
    }

    public void reportClockVisible(boolean flag)
    {
        mClockVisible = flag;
        mHandler.obtainMessage(307).sendToTarget();
    }

    public void reportFailedAttempt()
    {
        mFailedAttempts = 1 + mFailedAttempts;
    }

    public void reportSimUnlocked()
    {
        if (mSimState != com.android.internal.telephony.IccCard.State.NETWORK_LOCKED)
            mSimState = com.android.internal.telephony.IccCard.State.READY;
    }

    public void reportSimUnlocked(int i)
    {
        if (i != 1)
        {
            if (mSimState != com.android.internal.telephony.IccCard.State.NETWORK_LOCKED)
                mSimState = com.android.internal.telephony.IccCard.State.READY;
        } else
        if (mSim2State != com.android.internal.telephony.IccCard.State.NETWORK_LOCKED)
            mSim2State = com.android.internal.telephony.IccCard.State.READY;
    }

    public void reportWallpaperSet()
    {
        mWallpaperUpdate = false;
    }

    public void setDebugFilterStatus(boolean flag)
    {
        DEBUG = flag;
    }

    public void setPINDismiss(int i, boolean flag, boolean flag1)
    {
        if (!flag)
            i += 2;
        int j = 1 << i;
        if (!flag1)
            mPINFlag = mPINFlag & ~j;
        else
            mPINFlag = j | mPINFlag;
    }

    public boolean shouldShowBatteryInfo()
    {
        boolean flag;
        if (!isPluggedIn(mBatteryStatus) && !isBatteryLow(mBatteryStatus))
            flag = false;
        else
            flag = true;
        return flag;
    }

    public void unRegisterRadioStateCallback()
    {
        mRadioStateCallback = null;
    }

    void updateResources()
    {
        if (mSIMCardDialog != null)
            if (!mSIMCardDialog.isShowing() || !mSIMRemoved)
            {
                if (mSIMCardDialog.isShowing() && !mSIMRemoved)
                {
                    Xlog.i("KeyguardUpdateMonitor", "updateResources, new sim inserted");
                    mSIMCardDialog.setTitle(0x2050074);
                    Object obj = mSIMCardDialog.getButton(-2);
                    if (obj != null)
                        ((Button) (obj)).setText(0x2050088);
                    obj = mSIMCardDialog.getButton(-1);
                    if (obj != null)
                        ((Button) (obj)).setText(0x2050077);
                    if (3 != mCardTotal)
                        obj = mContext.getResources().getString(0x2050075);
                    else
                        obj = mContext.getResources().getString(0x2050076);
                    InitView(mPromptView, ((String) (obj)));
                }
            } else
            {
                Xlog.i("KeyguardUpdateMonitor", "updateResources, default sim removed");
                mSIMCardDialog.setTitle(0x2050078);
                Object obj1 = mSIMCardDialog.getButton(-2);
                if (obj1 != null)
                    ((Button) (obj1)).setText(0x2050088);
                obj1 = mSIMCardDialog.getButton(-1);
                if (obj1 != null)
                    ((Button) (obj1)).setText(0x2050077);
                obj1 = mContext.getResources().getString(0x2050079);
                InitView(mPromptView, ((String) (obj1)));
            }
    }

    static 
    {
        KEYGUARD_DM_LOCKED = false;
    }





/*
    static int access$1002(KeyguardUpdateMonitor keyguardupdatemonitor, int i)
    {
        keyguardupdatemonitor.mPINFlag = i;
        return i;
    }

*/










/*
    static boolean access$1802(KeyguardUpdateMonitor keyguardupdatemonitor, boolean flag)
    {
        keyguardupdatemonitor.mDeviceProvisioned = flag;
        return flag;
    }

*/





/*
    static ContentObserver access$2002(KeyguardUpdateMonitor keyguardupdatemonitor, ContentObserver contentobserver)
    {
        keyguardupdatemonitor.mContentObserver = contentobserver;
        return contentobserver;
    }

*/




/*
    static CharSequence access$2202(KeyguardUpdateMonitor keyguardupdatemonitor, CharSequence charsequence)
    {
        keyguardupdatemonitor.mTelephonyPlmn = charsequence;
        return charsequence;
    }

*/




/*
    static CharSequence access$2402(KeyguardUpdateMonitor keyguardupdatemonitor, CharSequence charsequence)
    {
        keyguardupdatemonitor.mTelephonySpn = charsequence;
        return charsequence;
    }

*/




/*
    static CharSequence access$2602(KeyguardUpdateMonitor keyguardupdatemonitor, CharSequence charsequence)
    {
        keyguardupdatemonitor.mTelephonyPlmnGemini = charsequence;
        return charsequence;
    }

*/



/*
    static CharSequence access$2702(KeyguardUpdateMonitor keyguardupdatemonitor, CharSequence charsequence)
    {
        keyguardupdatemonitor.mTelephonySpnGemini = charsequence;
        return charsequence;
    }

*/



/*
    static SIMStatus access$2802(KeyguardUpdateMonitor keyguardupdatemonitor, SIMStatus simstatus)
    {
        keyguardupdatemonitor.mSimStatus = simstatus;
        return simstatus;
    }

*/



/*
    static boolean access$2902(KeyguardUpdateMonitor keyguardupdatemonitor, boolean flag)
    {
        keyguardupdatemonitor.shouldPopup = flag;
        return flag;
    }

*/




/*
    static AlertDialog access$3002(KeyguardUpdateMonitor keyguardupdatemonitor, AlertDialog alertdialog)
    {
        keyguardupdatemonitor.mDialog = alertdialog;
        return alertdialog;
    }

*/



/*
    static int access$3102(KeyguardUpdateMonitor keyguardupdatemonitor, int i)
    {
        keyguardupdatemonitor.mMissedCall = i;
        return i;
    }

*/



/*
    static boolean access$3202(KeyguardUpdateMonitor keyguardupdatemonitor, boolean flag)
    {
        keyguardupdatemonitor.mWallpaperSetComplete = flag;
        return flag;
    }

*/



/*
    static boolean access$3302(boolean flag)
    {
        KEYGUARD_DM_LOCKED = flag;
        return flag;
    }

*/







/*
    static AlertDialog access$3802(KeyguardUpdateMonitor keyguardupdatemonitor, AlertDialog alertdialog)
    {
        keyguardupdatemonitor.mGPRSDialog1 = alertdialog;
        return alertdialog;
    }

*/



/*
    static AlertDialog access$3902(KeyguardUpdateMonitor keyguardupdatemonitor, AlertDialog alertdialog)
    {
        keyguardupdatemonitor.mGPRSDialog2 = alertdialog;
        return alertdialog;
    }

*/






}
