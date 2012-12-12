// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.TransportControlView;
import com.mediatek.xlog.Xlog;
import java.util.ArrayList;
import java.util.Date;
import libcore.util.MutableInt;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardUpdateMonitor, AccountUnlockScreen, KeyguardScreenCallback, PasswordUnlockScreen

class KeyguardStatusViewManager
    implements android.view.View.OnClickListener
{
    static final class StatusMode extends Enum
    {

        private static final StatusMode $VALUES[];
        public static final StatusMode NetworkLocked;
        public static final StatusMode NetworkSearching;
        public static final StatusMode Normal;
        public static final StatusMode SimLocked;
        public static final StatusMode SimMissing;
        public static final StatusMode SimMissingLocked;
        public static final StatusMode SimNotReady;
        public static final StatusMode SimPermDisabled;
        public static final StatusMode SimPukLocked;
        public static final StatusMode SimUnknown;
        private final boolean mShowStatusLines;

        public static StatusMode valueOf(String s)
        {
            return (StatusMode)Enum.valueOf(com/android/internal/policy/impl/KeyguardStatusViewManager$StatusMode, s);
        }

        public static StatusMode[] values()
        {
            return (StatusMode[])$VALUES.clone();
        }

        public boolean shouldShowStatusLines()
        {
            return mShowStatusLines;
        }

        static 
        {
            Normal = new StatusMode("Normal", 0, true);
            NetworkLocked = new StatusMode("NetworkLocked", 1, true);
            SimMissing = new StatusMode("SimMissing", 2, false);
            SimNotReady = new StatusMode("SimNotReady", 3, false);
            SimMissingLocked = new StatusMode("SimMissingLocked", 4, false);
            NetworkSearching = new StatusMode("NetworkSearching", 5, true);
            SimPukLocked = new StatusMode("SimPukLocked", 6, false);
            SimLocked = new StatusMode("SimLocked", 7, true);
            SimPermDisabled = new StatusMode("SimPermDisabled", 8, false);
            SimUnknown = new StatusMode("SimUnknown", 9, false);
            StatusMode astatusmode[] = new StatusMode[10];
            astatusmode[0] = Normal;
            astatusmode[1] = NetworkLocked;
            astatusmode[2] = SimMissing;
            astatusmode[3] = SimNotReady;
            astatusmode[4] = SimMissingLocked;
            astatusmode[5] = NetworkSearching;
            astatusmode[6] = SimPukLocked;
            astatusmode[7] = SimLocked;
            astatusmode[8] = SimPermDisabled;
            astatusmode[9] = SimUnknown;
            $VALUES = astatusmode;
        }

        private StatusMode(String s, int i, boolean flag)
        {
            super(s, i);
            mShowStatusLines = flag;
        }
    }

    private class TransientTextManager
    {
        private class Data
        {

            final int icon;
            final CharSequence text;
            final TransientTextManager this$1;

            Data(CharSequence charsequence, int i)
            {
                this$1 = TransientTextManager.this;
                super();
                text = charsequence;
                icon = i;
            }
        }


        private ArrayList mMessages;
        private TextView mTextView;
        final KeyguardStatusViewManager this$0;

        void post(CharSequence charsequence, int i, long l)
        {
            if (mTextView != null)
            {
                mTextView.setText(charsequence);
                mTextView.setCompoundDrawablesWithIntrinsicBounds(i, 0, 0, 0);
                final Data data = new Data(charsequence, i);
                mContainer.postDelayed(new Runnable() {

                    final TransientTextManager this$1;
                    final Data val$data;

                    public void run()
                    {
                        mMessages.remove(data);
                        int j = -1 + mMessages.size();
                        int k;
                        if (j <= 0)
                        {
                            MutableInt mutableint = new MutableInt(0);
                            j = getAltTextMessage(mutableint);
                            k = mutableint.value;
                        } else
                        {
                            k = (Data)mMessages.get(j);
                            j = ((Data) (k)).text;
                            k = ((Data) (k)).icon;
                        }
                        mTextView.setText(j);
                        mTextView.setCompoundDrawablesWithIntrinsicBounds(k, 0, 0, 0);
                    }

                
                {
                    this$1 = TransientTextManager.this;
                    data = data1;
                    super();
                }
                }
, l);
            }
        }



        TransientTextManager(TextView textview)
        {
            this$0 = KeyguardStatusViewManager.this;
            super();
            mMessages = new ArrayList(5);
            mTextView = textview;
        }
    }


    public static final int ALARM_ICON = 0x108002e;
    private static final int BATTERY_INFO = 15;
    public static final int BATTERY_LOW_ICON = 0;
    private static final int CARRIER_HELP_TEXT = 12;
    private static final int CARRIER_TEXT = 11;
    public static final int CHARGING_ICON = 0;
    private static final boolean DEBUG = true;
    private static final int HELP_MESSAGE_TEXT = 13;
    private static final long INSTRUCTION_RESET_DELAY = 2000L;
    private static final int INSTRUCTION_TEXT = 10;
    public static final int LOCK_ICON = 0;
    private static final int OWNER_INFO = 14;
    private static final int SIM_COLOR_COUNT = 8;
    private static final int SIM_ICON_IMGS[];
    private static final int SIM_LOCK_INDEX = 3;
    private static final int SIM_NORMAL_INDEX = 0;
    private static final int SIM_NO_INDEX = 2;
    private static final int SIM_NO_SIGN_INDEX = 1;
    private static final String TAG = "KeyguardStatusView";
    private static boolean phoneAppAlive = false;
    private TextView mAlarmStatusView;
    private int mBatteryLevel;
    private TextView mCalibrationData;
    private KeyguardScreenCallback mCallback;
    private Drawable mCarrierGeminiIcon;
    private CharSequence mCarrierGeminiText;
    private TextView mCarrierGeminiView;
    private CharSequence mCarrierHelpText;
    private Drawable mCarrierIcon;
    private CharSequence mCarrierText;
    private TextView mCarrierView;
    private View mContainer;
    private TextView mDMPrompt;
    private String mDateFormatString;
    private TextView mDateView;
    private KeyguardUpdateMonitor.deviceInfoCallback mDeviceInfoCallback;
    private boolean mDownloadCalibrationData;
    private boolean mEmergencyButtonEnabledBecauseSimLocked;
    private Button mEmergencyCallButton;
    private final boolean mEmergencyCallButtonEnabledInScreen;
    private StatusMode mGeminiStatus;
    private String mHelpMessageText;
    private KeyguardUpdateMonitor.InfoCallback mInfoCallback;
    private String mInstructionText;
    private LockPatternUtils mLockPatternUtils;
    private CharSequence mOwnerInfoText;
    private TextView mOwnerInfoView;
    private KeyguardUpdateMonitor.phoneStateCallback mPhoneCallback;
    protected int mPhoneState;
    private CharSequence mPlmn;
    private boolean mPluggedIn;
    private boolean mShowingBatteryInfo;
    private boolean mShowingStatus;
    protected com.android.internal.telephony.IccCard.State mSimGeminiState;
    protected com.android.internal.telephony.IccCard.State mSimState;
    private KeyguardUpdateMonitor.SimStateCallback mSimStateCallback;
    private CharSequence mSpn;
    private StatusMode mStatus;
    private TextView mStatus1View;
    private TransientTextManager mTransientTextManager;
    private TransientTextManager mTransientTextManagerGemini;
    private TransportControlView mTransportView;
    private boolean mUnlockDisabledDueToSimState;
    private KeyguardUpdateMonitor mUpdateMonitor;

    public KeyguardStatusViewManager(View view, KeyguardUpdateMonitor keyguardupdatemonitor, LockPatternUtils lockpatternutils, KeyguardScreenCallback keyguardscreencallback, boolean flag)
    {
        mShowingBatteryInfo = false;
        mPluggedIn = false;
        mBatteryLevel = 100;
        mInfoCallback = new KeyguardUpdateMonitor.InfoCallback() {

            final KeyguardStatusViewManager this$0;

            public void onDownloadCalibrationDataUpdate(boolean flag1)
            {
                mDownloadCalibrationData = flag1;
                updateCalibrationDataText();
            }

            public void onLockScreenUpdate(int k)
            {
                boolean flag1 = isAccountMode();
                Xlog.i("KeyguardStatusView", (new StringBuilder()).append("onLockScreenUpdate, account=").append(flag1).toString());
                if (!flag1)
                    if (1 != k)
                        updateCarrierTextWithSimStatus(mSimState, 0);
                    else
                        updateCarrierTextWithSimStatus(mSimGeminiState, 1);
            }

            public void onMissedCallChanged(int k)
            {
            }

            public void onRefreshBatteryInfo(boolean flag1, boolean flag2, int k)
            {
                mShowingBatteryInfo = flag1;
                mPluggedIn = flag2;
                mBatteryLevel = k;
                MutableInt mutableint = new MutableInt(0);
                update(15, getAltTextMessage(mutableint));
            }

            public void onRefreshCarrierInfo(CharSequence charsequence, CharSequence charsequence1)
            {
                mPlmn = charsequence;
                mSpn = charsequence1;
                updateCarrierTextWithSimStatus(mSimGeminiState, 1);
                updateCarrierTextWithSimStatus(mSimState, 0);
            }

            public void onRingerModeChanged(int k)
            {
            }

            public void onSIMInfoChanged(int k)
            {
                boolean flag1 = isAccountMode();
                Xlog.i("KeyguardStatusView", (new StringBuilder()).append("onSIMInfoChanged, account=").append(flag1).toString());
                if (!flag1)
                    if (1 != k)
                        updateCarrierTextWithSimStatus(mSimState, 0);
                    else
                        updateCarrierTextWithSimStatus(mSimGeminiState, 1);
            }

            public void onSearchNetworkUpdate(int k, boolean flag1)
            {
                boolean flag2 = isAccountMode();
                Xlog.i("KeyguardStatusView", (new StringBuilder()).append("onSearchNetworkUpdate, account=").append(flag2).append(",simId = ").append(k).append(", switchOn=").append(flag1).toString());
                if (!flag2)
                    if (!flag1)
                    {
                        com.android.internal.telephony.IccCard.State state = mUpdateMonitor.getSimState(k);
                        if (1 != k)
                            mStatus = getStatusForIccState(state);
                        else
                            mGeminiStatus = getStatusForIccState(state);
                        updateCarrierTextWithSimStatus(state, k);
                    } else
                    {
                        if (k != 0)
                        {
                            mGeminiStatus = StatusMode.NetworkSearching;
                            mCarrierGeminiText = getContext().getString(0x2050086);
                        } else
                        {
                            mStatus = StatusMode.NetworkSearching;
                            mCarrierText = getContext().getString(0x2050086);
                        }
                        updateCarrierText();
                    }
            }

            public void onTimeChanged()
            {
                refreshDate();
            }

            public void onUnlockKeyguard()
            {
            }

            public void onWallpaperSetComplete()
            {
            }

            
            {
                this$0 = KeyguardStatusViewManager.this;
                super();
            }
        }
;
        mDeviceInfoCallback = new KeyguardUpdateMonitor.deviceInfoCallback() {

            final KeyguardStatusViewManager this$0;

            public void onClockVisibilityChanged()
            {
            }

            public void onDMKeyguardUpdate()
            {
                byte byte0 = 0;
                View view2 = findViewById(0x102029b);
                if (view2 != null)
                {
                    byte byte1;
                    if (!mUpdateMonitor.DM_IsLocked())
                        byte1 = 0;
                    else
                        byte1 = 8;
                    view2.setVisibility(byte1);
                }
                if (mEmergencyCallButton != null)
                {
                    Button button = mEmergencyCallButton;
                    byte byte2;
                    if (!mUpdateMonitor.DM_IsLocked())
                        byte2 = 8;
                    else
                        byte2 = 0;
                    button.setVisibility(byte2);
                }
                if (mDMPrompt != null)
                {
                    TextView textview = mDMPrompt;
                    if (!mUpdateMonitor.DM_IsLocked())
                        byte0 = 8;
                    textview.setVisibility(byte0);
                    mDMPrompt.setText(0x2050090);
                }
                if (mContainer instanceof PasswordUnlockScreen)
                    switch (mLockPatternUtils.getKeyguardStoredPasswordQuality())
                    {
                    default:
                        updatePasswordWhenDMChanged(view2);
                        break;

                    case 131072: 
                        updatePINWhenDMChanged();
                        break;
                    }
            }

            public void onDeviceProvisioned()
            {
            }

            
            {
                this$0 = KeyguardStatusViewManager.this;
                super();
            }
        }
;
        mPhoneCallback = new KeyguardUpdateMonitor.phoneStateCallback() {

            final KeyguardStatusViewManager this$0;

            public void onPhoneStateChanged(int k)
            {
                mPhoneState = k;
                updateEmergencyCallButtonState(k);
            }

            
            {
                this$0 = KeyguardStatusViewManager.this;
                super();
            }
        }
;
        mSimStateCallback = new KeyguardUpdateMonitor.SimStateCallback() {

            final KeyguardStatusViewManager this$0;

            public void onSimStateChanged(com.android.internal.telephony.IccCard.State state)
            {
                boolean flag1 = isAccountMode();
                Xlog.i("KeyguardStatusView", (new StringBuilder()).append("mSimStateCallback, account=").append(flag1).append(", simState=").append(state).toString());
                if (!flag1)
                    updateCarrierTextWithSimStatus(state, 0);
            }

            public void onSimStateChangedGemini(com.android.internal.telephony.IccCard.State state, int k)
            {
                boolean flag1 = isAccountMode();
                Xlog.i("KeyguardStatusView", (new StringBuilder()).append("mSimStateCallback, account=").append(flag1).append(", simState=").append(state).append(", simId=").append(k).toString());
                if (!flag1)
                    updateCarrierTextWithSimStatus(state, k);
            }

            
            {
                this$0 = KeyguardStatusViewManager.this;
                super();
            }
        }
;
        Xlog.v("KeyguardStatusView", "KeyguardStatusViewManager()");
        mContainer = view;
        mDateFormatString = getContext().getString(0x10400b0);
        mLockPatternUtils = lockpatternutils;
        mUpdateMonitor = keyguardupdatemonitor;
        mCallback = keyguardscreencallback;
        mCarrierView = (TextView)findViewById(0x102029a);
        mDateView = (TextView)findViewById(0x1020063);
        mStatus1View = (TextView)findViewById(0x1020299);
        mAlarmStatusView = (TextView)findViewById(0x1020298);
        mOwnerInfoView = (TextView)findViewById(0x10202b2);
        mCalibrationData = (TextView)findViewById(0x10202b3);
        mTransportView = (TransportControlView)findViewById(0x10202a0);
        mEmergencyCallButton = (Button)findViewById(0x1020281);
        mEmergencyCallButtonEnabledInScreen = flag;
        mDMPrompt = (TextView)findViewById(0x10202a4);
        if (mDMPrompt != null && mUpdateMonitor.DM_IsLocked())
        {
            mDMPrompt.setVisibility(0);
            mDMPrompt.setText(0x2050090);
        }
        mCarrierGeminiView = (TextView)findViewById(0x10202a6);
        mTransientTextManagerGemini = new TransientTextManager(mCarrierGeminiView);
        if (mTransportView != null)
            mTransportView.setVisibility(8);
        if (!phoneAppAlive)
            phoneAppAlive = mUpdateMonitor.isPhoneAppReady();
        if (mEmergencyCallButton != null)
        {
            mEmergencyCallButton.setText(0x10402e0);
            mEmergencyCallButton.setOnClickListener(this);
            mEmergencyCallButton.setFocusable(false);
        }
        mTransientTextManager = new TransientTextManager(mCarrierView);
        mUpdateMonitor.registerInfoCallback(mInfoCallback);
        mUpdateMonitor.registerSimStateCallback(mSimStateCallback);
        mUpdateMonitor.registerPhoneStateCallback(mPhoneCallback);
        mUpdateMonitor.registerDeviceInfoCallback(mDeviceInfoCallback);
        resetStatusInfo();
        refreshDate();
        updateOwnerInfo();
        View aview[] = new View[6];
        aview[0] = mCarrierView;
        aview[1] = mCarrierGeminiView;
        aview[2] = mDateView;
        aview[3] = mStatus1View;
        aview[4] = mOwnerInfoView;
        aview[5] = mAlarmStatusView;
        int j = aview.length;
        int i = 0;
        do
        {
            if (i >= j)
                return;
            View view1 = aview[i];
            if (view1 != null)
                view1.setSelected(true);
            i++;
        } while (true);
    }

    private View findViewById(int i)
    {
        return mContainer.findViewById(i);
    }

    private CharSequence getAltTextMessage(MutableInt mutableint)
    {
        Object obj = null;
        if (!mShowingBatteryInfo)
            obj = mCarrierText;
        else
        if (!mPluggedIn)
        {
            if (mBatteryLevel < 16)
            {
                obj = getContext().getString(0x10402e8);
                mutableint.value = 0;
            }
        } else
        {
            if (!mUpdateMonitor.isDeviceCharged())
            {
                Context context = getContext();
                obj = ((Object) (new Object[1]));
                obj[0] = Integer.valueOf(mBatteryLevel);
                obj = context.getString(0x10402e5, ((Object []) (obj)));
            } else
            {
                obj = getContext().getString(0x10402e6);
            }
            mutableint.value = 0;
        }
        return ((CharSequence) (obj));
    }

    private Context getContext()
    {
        return mContainer.getContext();
    }

    private CharSequence getPriorityTextMessage(MutableInt mutableint)
    {
        Object obj = null;
        if (TextUtils.isEmpty(mInstructionText))
        {
            if (!mShowingBatteryInfo)
            {
                if (!inWidgetMode() && mOwnerInfoView == null && mOwnerInfoText != null)
                    obj = mOwnerInfoText;
            } else
            if (!mPluggedIn || !mUpdateMonitor.isDeviceCharging())
            {
                if (mBatteryLevel < 16)
                {
                    obj = getContext().getString(0x10402e8);
                    mutableint.value = 0;
                }
            } else
            {
                if (!mUpdateMonitor.isDeviceCharged())
                {
                    obj = getContext();
                    Object aobj[] = new Object[1];
                    aobj[0] = Integer.valueOf(mBatteryLevel);
                    obj = ((Context) (obj)).getString(0x10402e5, aobj);
                } else
                {
                    obj = getContext().getString(0x10402e6);
                }
                mutableint.value = 0;
            }
        } else
        {
            obj = mInstructionText;
            mutableint.value = 0;
        }
        return ((CharSequence) (obj));
    }

    private int getResourceID(int i, StatusMode statusmode)
    {
        int j = -1;
        Xlog.i("KeyguardStatusView", (new StringBuilder()).append("getResourceID, colorIndex=:").append(i).append(", status=").append(statusmode).toString());
        if (i >= 0 && i < 8)
        {
            class _cls5
            {

                static final int $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[];
                static final int $SwitchMap$com$android$internal$telephony$IccCard$State[];

                static 
                {
                    $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode = new int[StatusMode.values().length];
                    try
                    {
                        $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[StatusMode.SimUnknown.ordinal()] = 1;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[StatusMode.Normal.ordinal()] = 2;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[StatusMode.NetworkLocked.ordinal()] = 3;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[StatusMode.SimNotReady.ordinal()] = 4;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[StatusMode.SimMissing.ordinal()] = 5;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[StatusMode.SimPermDisabled.ordinal()] = 6;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[StatusMode.SimMissingLocked.ordinal()] = 7;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[StatusMode.SimLocked.ordinal()] = 8;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[StatusMode.SimPukLocked.ordinal()] = 9;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$policy$impl$KeyguardStatusViewManager$StatusMode[StatusMode.NetworkSearching.ordinal()] = 10;
                    }
                    catch (NoSuchFieldError _ex) { }
                    $SwitchMap$com$android$internal$telephony$IccCard$State = new int[com.android.internal.telephony.IccCard.State.values().length];
                    try
                    {
                        $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.ABSENT.ordinal()] = 1;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.NETWORK_LOCKED.ordinal()] = 2;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.NOT_READY.ordinal()] = 3;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.PIN_REQUIRED.ordinal()] = 4;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.PUK_REQUIRED.ordinal()] = 5;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.READY.ordinal()] = 6;
                    }
                    catch (NoSuchFieldError _ex) { }
                    try
                    {
                        $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.PERM_DISABLED.ordinal()] = 7;
                    }
                    catch (NoSuchFieldError _ex) { }
                    $SwitchMap$com$android$internal$telephony$IccCard$State[com.android.internal.telephony.IccCard.State.UNKNOWN.ordinal()] = 8;
_L2:
                    return;
                    JVM INSTR pop ;
                    if (true) goto _L2; else goto _L1
_L1:
                }
            }

            switch (_cls5..SwitchMap.com.android.internal.policy.impl.KeyguardStatusViewManager.StatusMode[statusmode.ordinal()])
            {
            case 6: // '\006'
            default:
                break;

            case 2: // '\002'
                j = SIM_ICON_IMGS[i + 0];
                break;

            case 3: // '\003'
            case 4: // '\004'
            case 10: // '\n'
                j = SIM_ICON_IMGS[i + 8];
                break;

            case 5: // '\005'
                if (mUpdateMonitor.getSimState() != com.android.internal.telephony.IccCard.State.UNKNOWN)
                    j = SIM_ICON_IMGS[i + 16];
                else
                    j = SIM_ICON_IMGS[i + 8];
                break;

            case 7: // '\007'
                j = SIM_ICON_IMGS[i + 16];
                break;

            case 8: // '\b'
            case 9: // '\t'
                j = SIM_ICON_IMGS[i + 24];
                break;
            }
            j = j;
        } else
        {
            Xlog.e("KeyguardStatusView", (new StringBuilder()).append("Invalid color index:").append(i).toString());
            j = j;
        }
        return j;
    }

    private CharSequence getText(int i)
    {
        CharSequence charsequence;
        if (i != 0)
            charsequence = getContext().getText(i);
        else
            charsequence = null;
        return charsequence;
    }

    private boolean inWidgetMode()
    {
        boolean flag;
        if (mTransportView == null || mTransportView.getVisibility() != 0)
            flag = false;
        else
            flag = true;
        return flag;
    }

    private boolean isAccountMode()
    {
        boolean flag;
        if (!(mContainer instanceof AccountUnlockScreen))
            flag = false;
        else
            flag = true;
        return flag;
    }

    private static CharSequence makeCarierString(CharSequence charsequence, CharSequence charsequence1)
    {
        boolean flag1;
        if (TextUtils.isEmpty(charsequence))
            flag1 = false;
        else
            flag1 = true;
        boolean flag;
        if (TextUtils.isEmpty(charsequence1))
            flag = false;
        else
            flag = true;
        if (!flag1 || !flag)
        {
            if (!flag1)
                if (!flag)
                    charsequence = "";
                else
                    charsequence = charsequence1;
        } else
        {
            charsequence = (new StringBuilder()).append(charsequence).append("|").append(charsequence1).toString();
        }
        return charsequence;
    }

    private CharSequence makeCarrierStringOnEmergencyCapable(CharSequence charsequence, CharSequence charsequence1)
    {
        if (mLockPatternUtils.isEmergencyCallCapable())
            charsequence = makeCarierString(charsequence, charsequence1);
        return charsequence;
    }

    private Drawable querySIMIcon(int i)
    {
        if (!phoneAppAlive)
            phoneAppAlive = mUpdateMonitor.isPhoneAppReady();
        Xlog.i("KeyguardStatusView", (new StringBuilder()).append("querySIMIcon , phoneAppAlive=").append(phoneAppAlive).append(", simdId=").append(i).toString());
        Drawable drawable;
        if (!phoneAppAlive)
            drawable = null;
        else
            drawable = mUpdateMonitor.getOptrDrawableBySlot(i);
        return drawable;
    }

    private void showOrHideCarrier()
    {
        boolean flag = false;
        boolean flag1 = false;
        TextView textview = (TextView)findViewById(0x10202a5);
        boolean flag2;
        if (((ConnectivityManager)getContext().getSystemService("connectivity")).isNetworkSupported(0))
            flag2 = false;
        else
            flag2 = true;
        if (!flag2)
        {
            if (mStatus == StatusMode.SimMissing || mStatus == StatusMode.SimMissingLocked)
                flag = true;
            if (mGeminiStatus == StatusMode.SimMissing || mGeminiStatus == StatusMode.SimMissingLocked)
                flag1 = true;
            Log.i("KeyguardStatusView", (new StringBuilder()).append("mSIMOneMissing=").append(flag).append(",mSIMTwoMissing=").append(flag1).toString());
            if (!flag || !flag1)
            {
                if (!flag || flag1)
                {
                    if (flag || !flag1)
                    {
                        textview.setVisibility(0);
                        textview.setText("|");
                        mCarrierView.setVisibility(0);
                        mCarrierGeminiView.setVisibility(0);
                        mCarrierView.setGravity(5);
                        mCarrierGeminiView.setGravity(3);
                    } else
                    {
                        textview.setVisibility(8);
                        mCarrierView.setVisibility(0);
                        mCarrierGeminiView.setVisibility(8);
                        mCarrierView.setGravity(17);
                    }
                } else
                {
                    textview.setVisibility(8);
                    mCarrierView.setVisibility(8);
                    mCarrierGeminiView.setVisibility(0);
                    mCarrierGeminiView.setGravity(17);
                }
            } else
            {
                textview.setVisibility(8);
                mCarrierView.setVisibility(0);
                mCarrierGeminiView.setVisibility(8);
                mCarrierView.setGravity(17);
            }
            if (mStatus == StatusMode.SimUnknown)
            {
                textview.setVisibility(8);
                mCarrierView.setVisibility(8);
                mCarrierGeminiView.setGravity(17);
            }
            if (mGeminiStatus == StatusMode.SimUnknown)
            {
                textview.setVisibility(8);
                mCarrierGeminiView.setVisibility(8);
                mCarrierView.setGravity(17);
            }
        } else
        {
            textview.setVisibility(8);
            mCarrierView.setVisibility(8);
            mCarrierGeminiView.setVisibility(8);
        }
    }

    private void update(int i, CharSequence charsequence)
    {
        if (!inWidgetMode())
        {
            updateStatusLines(mShowingStatus);
        } else
        {
            Log.v("KeyguardStatusView", "inWidgetMode() is true");
            switch (i)
            {
            case 11: // '\013'
            case 14: // '\016'
            default:
                Xlog.w("KeyguardStatusView", (new StringBuilder()).append("Not showing message id ").append(i).append(", str=").append(charsequence).toString());
                break;

            case 10: // '\n'
            case 12: // '\f'
            case 13: // '\r'
            case 15: // '\017'
                mTransientTextManager.post(charsequence, 0, 2000L);
                break;
            }
        }
    }

    private void updateAlarmInfo()
    {
        byte byte0 = 0;
        if (mAlarmStatusView != null)
        {
            Object obj = mLockPatternUtils.getNextAlarm();
            boolean flag;
            if (!mShowingStatus || TextUtils.isEmpty(((CharSequence) (obj))))
                flag = false;
            else
                flag = true;
            mAlarmStatusView.setText(((CharSequence) (obj)));
            mAlarmStatusView.setCompoundDrawablesWithIntrinsicBounds(0x108002e, 0, 0, 0);
            obj = mAlarmStatusView;
            if (!flag)
                byte0 = 8;
            ((TextView) (obj)).setVisibility(byte0);
        }
    }

    private void updateCalibrationDataText()
    {
        if (mCalibrationData != null)
            if (!mDownloadCalibrationData)
            {
                Xlog.i("KeyguardStatusView", "updateCalibrationDataText");
                mCalibrationData.setText(0x10402f5);
                mCalibrationData.setVisibility(0);
            } else
            {
                mCalibrationData.setVisibility(8);
            }
    }

    private void updateCarrierText()
    {
        if (!isAccountMode())
        {
            showOrHideCarrier();
            Xlog.i("KeyguardStatusView", (new StringBuilder()).append("updateCarrierText, mCarrierText=").append(mCarrierText).append(", mCarrierGeminiText=").append(mCarrierGeminiText).toString());
        }
        if (!inWidgetMode() && mCarrierView != null)
            mCarrierView.setText(mCarrierText);
        if (!inWidgetMode() && mCarrierGeminiView != null && !isAccountMode())
            mCarrierGeminiView.setText(mCarrierGeminiText);
    }

    private void updateCarrierTextWithSimStatus(com.android.internal.telephony.IccCard.State state, int i)
    {
        CharSequence charsequence2 = null;
        int j = 0;
        mUnlockDisabledDueToSimState = true;
        StatusMode _tmp = StatusMode.Normal;
        if ((StatusMode.NetworkSearching != mStatus || i != 0) && (StatusMode.NetworkSearching != mGeminiStatus || 1 != i))
        {
            CharSequence charsequence;
            CharSequence charsequence1;
            StatusMode statusmode;
            if (1 != i)
            {
                mStatus = getStatusForIccState(state);
                mSimState = state;
                charsequence = mUpdateMonitor.getTelephonyPlmn(0);
                charsequence1 = mUpdateMonitor.getTelephonySpn(0);
                statusmode = mStatus;
            } else
            {
                mGeminiStatus = getStatusForIccState(state);
                mSimGeminiState = state;
                charsequence = mUpdateMonitor.getTelephonyPlmn(1);
                charsequence1 = mUpdateMonitor.getTelephonySpn(1);
                statusmode = mGeminiStatus;
            }
            Xlog.d("KeyguardStatusView", (new StringBuilder()).append("updateCarrierTextWithSimStatus(), simState = ").append(state).append(", simId=").append(i).append(", plmn=").append(charsequence).append(", spn=").append(charsequence1).toString());
            switch (_cls5..SwitchMap.com.android.internal.policy.impl.KeyguardStatusViewManager.StatusMode[statusmode.ordinal()])
            {
            default:
                break;

            case 1: // '\001'
            case 2: // '\002'
                charsequence2 = makeCarierString(charsequence, charsequence1);
                break;

            case 3: // '\003'
                charsequence2 = makeCarierString(charsequence, getContext().getText(0x10402f4));
                j = 0x10402de;
                break;

            case 4: // '\004'
                charsequence2 = makeCarierString(charsequence, charsequence1);
                break;

            case 5: // '\005'
                charsequence2 = makeCarrierStringOnEmergencyCapable(getContext().getText(0x10402e9), charsequence);
                j = 0x10402ec;
                break;

            case 6: // '\006'
                charsequence2 = getContext().getText(0x10402e9);
                j = 0x10402ed;
                mEmergencyButtonEnabledBecauseSimLocked = true;
                break;

            case 7: // '\007'
                charsequence2 = makeCarrierStringOnEmergencyCapable(getContext().getText(0x10402e9), charsequence);
                j = 0x10402eb;
                mEmergencyButtonEnabledBecauseSimLocked = true;
                break;

            case 8: // '\b'
                charsequence2 = makeCarrierStringOnEmergencyCapable(getContext().getText(0x10402f8), charsequence);
                mEmergencyButtonEnabledBecauseSimLocked = true;
                break;

            case 9: // '\t'
                charsequence2 = makeCarrierStringOnEmergencyCapable(getContext().getText(0x10402f6), charsequence);
                if (!mLockPatternUtils.isPukUnlockScreenEnable())
                    mEmergencyButtonEnabledBecauseSimLocked = true;
                break;
            }
            setCarrierText(charsequence2, i);
            if (StatusMode.Normal == mGeminiStatus || StatusMode.Normal == mStatus)
                mUnlockDisabledDueToSimState = false;
            Xlog.d("KeyguardStatusView", (new StringBuilder()).append("updateCarrierTextWithSimStatus(), simState = ").append(state).append(", simId=").append(i).append(", plmn=").append(charsequence).append(", spn=").append(charsequence1).append(", mUnlockDisabledDueToSimState=").append(mUnlockDisabledDueToSimState).toString());
            setCarrierHelpText(j);
            if (mUnlockDisabledDueToSimState && mEmergencyCallButton.getVisibility() != 0 || !mUnlockDisabledDueToSimState && mEmergencyCallButton.getVisibility() == 0)
                updateEmergencyCallButtonState(mPhoneState);
        } else
        {
            Xlog.i("KeyguardStatusView", (new StringBuilder()).append("updateCarrierTextWithSimStatus, searching network now, don't interrupt it, simState=").append(state).append(", simId=").append(i).toString());
        }
    }

    private void updateEmergencyCallButton(int i, boolean flag)
    {
        TelephonyManager telephonymanager = (TelephonyManager)getContext().getSystemService("phone");
        boolean flag1;
        if (telephonymanager == null || !telephonymanager.isVoiceCapable())
            flag1 = false;
        else
            flag1 = true;
        Xlog.i("KeyguardStatusView", (new StringBuilder()).append("updateEmergencyCallButton, isVoiceCapable=").append(flag1).toString());
        if (!flag1)
            mEmergencyCallButton.setVisibility(8);
        else
        if (!mUpdateMonitor.DM_IsLocked() && !KeyguardUpdateMonitor.mIsCMCC && (!mLockPatternUtils.isEmergencyCallCapable() || !flag))
        {
            mEmergencyCallButton.setVisibility(8);
        } else
        {
            mEmergencyCallButton.setVisibility(0);
            int j;
            if (i != 2)
            {
                j = 0x10402e0;
                mEmergencyCallButton.setCompoundDrawablesWithIntrinsicBounds(0x10802c0, 0, 0, 0);
            } else
            {
                j = 0x10402e1;
                mEmergencyCallButton.setCompoundDrawablesWithIntrinsicBounds(0x1080084, 0, 0, 0);
            }
            mEmergencyCallButton.setText(j);
        }
    }

    private void updateEmergencyCallButtonState(int i)
    {
        if (mEmergencyCallButton != null)
        {
            boolean flag;
            if (!mEmergencyCallButtonEnabledInScreen && !mUnlockDisabledDueToSimState)
                flag = false;
            else
                flag = true;
            Xlog.i("KeyguardStatusView", (new StringBuilder()).append("updateEmergencyCallButtonState, phoneState=").append(i).append(", showIfCapable=").append(flag).append(", mEmergencyCallButtonEnabledInScreen=").append(mEmergencyCallButtonEnabledInScreen).append(", mUnlockDisabledDueToSimState=").append(mUnlockDisabledDueToSimState).toString());
            updateEmergencyCallButton(i, flag);
        }
    }

    private void updateOwnerInfo()
    {
        int i = 1;
        Object obj = getContext().getContentResolver();
        if (android.provider.Settings.Secure.getInt(((android.content.ContentResolver) (obj)), "lock_screen_owner_info_enabled", i) == 0)
            i = 0;
        if (i == 0)
            obj = null;
        else
            obj = android.provider.Settings.Secure.getString(((android.content.ContentResolver) (obj)), "lock_screen_owner_info");
        mOwnerInfoText = ((CharSequence) (obj));
        if (mOwnerInfoView != null)
        {
            mOwnerInfoView.setText(mOwnerInfoText);
            TextView textview = mOwnerInfoView;
            byte byte0;
            if (!TextUtils.isEmpty(mOwnerInfoText))
                byte0 = 0;
            else
                byte0 = 8;
            textview.setVisibility(byte0);
        }
    }

    private void updatePINWhenDMChanged()
    {
        byte byte0;
        if (!mUpdateMonitor.DM_IsLocked())
            byte0 = 0;
        else
            byte0 = 8;
        View view = findViewById(0x102029f);
        if (view != null)
            view.setVisibility(byte0);
    }

    private void updatePasswordWhenDMChanged(View view)
    {
        InputMethodManager inputmethodmanager = (InputMethodManager)getContext().getSystemService("input_method");
        if (!mUpdateMonitor.DM_IsLocked())
        {
            Xlog.i("KeyguardStatusView", "IME is hide, we should show it");
            mContainer.invalidate();
        } else
        if (inputmethodmanager.isActive())
        {
            Xlog.i("KeyguardStatusView", "IME is showing, we should hide it");
            inputmethodmanager.hideSoftInputFromWindow(view.getWindowToken(), 2);
        }
    }

    private void updateStatus1()
    {
        byte byte0 = 0;
        if (mStatus1View != null)
        {
            Object obj = new MutableInt(0);
            CharSequence charsequence = getPriorityTextMessage(((MutableInt) (obj)));
            mStatus1View.setText(charsequence);
            mStatus1View.setCompoundDrawablesWithIntrinsicBounds(((MutableInt) (obj)).value, 0, 0, 0);
            obj = mStatus1View;
            if (!mShowingStatus)
                byte0 = 4;
            ((TextView) (obj)).setVisibility(byte0);
        }
    }

    public void cleanUp()
    {
        Xlog.v("KeyguardStatusView", "cleanUp");
        mUpdateMonitor.removeCallback(mInfoCallback);
        mUpdateMonitor.removeCallback(mSimStateCallback);
        mUpdateMonitor.removeCallback(mPhoneCallback);
        mUpdateMonitor.removeCallback(mDeviceInfoCallback);
    }

    public StatusMode getStatusForIccState(com.android.internal.telephony.IccCard.State state)
    {
        StatusMode statusmode;
        if (state != null)
        {
            boolean flag;
            if (mUpdateMonitor.isDeviceProvisioned() || state != com.android.internal.telephony.IccCard.State.ABSENT && state != com.android.internal.telephony.IccCard.State.PERM_DISABLED)
                flag = false;
            else
                flag = true;
            if (flag)
                state = com.android.internal.telephony.IccCard.State.NETWORK_LOCKED;
            switch (_cls5..SwitchMap.com.android.internal.telephony.IccCard.State[state.ordinal()])
            {
            default:
                statusmode = StatusMode.SimMissing;
                break;

            case 1: // '\001'
                statusmode = StatusMode.SimMissing;
                break;

            case 2: // '\002'
                statusmode = StatusMode.NetworkLocked;
                break;

            case 3: // '\003'
                statusmode = StatusMode.SimNotReady;
                break;

            case 4: // '\004'
                statusmode = StatusMode.SimLocked;
                break;

            case 5: // '\005'
                statusmode = StatusMode.SimPukLocked;
                break;

            case 6: // '\006'
                statusmode = StatusMode.Normal;
                break;

            case 7: // '\007'
                statusmode = StatusMode.SimPermDisabled;
                break;

            case 8: // '\b'
                statusmode = StatusMode.SimUnknown;
                break;
            }
        } else
        {
            statusmode = StatusMode.SimUnknown;
        }
        return statusmode;
    }

    public void onClick(View view)
    {
        if (view == mEmergencyCallButton)
            mCallback.takeEmergencyCallAction();
    }

    public void onPause()
    {
        Xlog.v("KeyguardStatusView", "onPause()");
        mUpdateMonitor.removeCallback(mInfoCallback);
        mUpdateMonitor.removeCallback(mSimStateCallback);
        mUpdateMonitor.removeCallback(mPhoneCallback);
        mUpdateMonitor.removeCallback(mDeviceInfoCallback);
    }

    public void onResume()
    {
        Xlog.v("KeyguardStatusView", "onResume()");
        mUpdateMonitor.registerInfoCallback(mInfoCallback);
        mUpdateMonitor.registerSimStateCallback(mSimStateCallback);
        mUpdateMonitor.registerPhoneStateCallback(mPhoneCallback);
        mUpdateMonitor.registerDeviceInfoCallback(mDeviceInfoCallback);
        resetStatusInfo();
    }

    void refreshDate()
    {
        if (mDateView != null)
        {
            String s = DateFormat.getDateFormat(getContext()).format(new Date());
            Log.i("KeyguardStatusView", (new StringBuilder()).append("refreshDate, s=").append(s).toString());
            mDateView.setText(s);
        }
    }

    void resetStatusInfo()
    {
        mInstructionText = null;
        mShowingBatteryInfo = mUpdateMonitor.shouldShowBatteryInfo();
        mPluggedIn = mUpdateMonitor.isDevicePluggedIn();
        mBatteryLevel = mUpdateMonitor.getBatteryLevel();
        updateStatusLines(true);
    }

    public void setCarrierHelpText(int i)
    {
        mCarrierHelpText = getText(i);
        update(12, mCarrierHelpText);
    }

    void setCarrierText(CharSequence charsequence, int i)
    {
        if (1 != i)
            mCarrierText = charsequence;
        else
            mCarrierGeminiText = charsequence;
        update(11, charsequence);
    }

    public void setHelpMessage(int i, int j)
    {
        Object obj = getText(i);
        if (obj != null)
            obj = obj.toString();
        else
            obj = null;
        mHelpMessageText = ((String) (obj));
        update(13, mHelpMessageText);
    }

    void setInstructionText(String s)
    {
        mInstructionText = s;
        update(10, s);
    }

    void setOwnerInfo(CharSequence charsequence)
    {
        mOwnerInfoText = charsequence;
        update(14, charsequence);
    }

    void updateStatusLines(boolean flag)
    {
        Xlog.v("KeyguardStatusView", (new StringBuilder()).append("updateStatusLines(").append(flag).append(")").toString());
        mShowingStatus = flag;
        updateAlarmInfo();
        updateOwnerInfo();
        updateStatus1();
        updateCarrierText();
        updateCalibrationDataText();
    }

    static 
    {
        int ai[] = new int[32];
        ai[0] = 0x20200eb;
        ai[1] = 0x20200d4;
        ai[2] = 0x20200ef;
        ai[3] = 0x20200dd;
        ai[4] = 0x20200ff;
        ai[5] = 0x20200d8;
        ai[6] = 0x20200f8;
        ai[7] = 0x20200f3;
        ai[8] = 0x20200ea;
        ai[9] = 0x20200d3;
        ai[10] = 0x20200ee;
        ai[11] = 0x20200dc;
        ai[12] = 0x20200fe;
        ai[13] = 0x20200d7;
        ai[14] = 0x20200f7;
        ai[15] = 0x20200f2;
        ai[16] = 0x20200e9;
        ai[17] = 0x20200d2;
        ai[18] = 0x20200ed;
        ai[19] = 0x20200db;
        ai[20] = 0x20200fd;
        ai[21] = 0x20200d6;
        ai[22] = 0x20200f6;
        ai[23] = 0x20200f1;
        ai[24] = 0x20200e8;
        ai[25] = 0x20200d1;
        ai[26] = 0x20200ec;
        ai[27] = 0x20200da;
        ai[28] = 0x20200fc;
        ai[29] = 0x20200d5;
        ai[30] = 0x20200f5;
        ai[31] = 0x20200f0;
        SIM_ICON_IMGS = ai;
    }





/*
    static StatusMode access$1202(KeyguardStatusViewManager keyguardstatusviewmanager, StatusMode statusmode)
    {
        keyguardstatusviewmanager.mStatus = statusmode;
        return statusmode;
    }

*/


/*
    static CharSequence access$1302(KeyguardStatusViewManager keyguardstatusviewmanager, CharSequence charsequence)
    {
        keyguardstatusviewmanager.mCarrierText = charsequence;
        return charsequence;
    }

*/



/*
    static StatusMode access$1502(KeyguardStatusViewManager keyguardstatusviewmanager, StatusMode statusmode)
    {
        keyguardstatusviewmanager.mGeminiStatus = statusmode;
        return statusmode;
    }

*/


/*
    static CharSequence access$1602(KeyguardStatusViewManager keyguardstatusviewmanager, CharSequence charsequence)
    {
        keyguardstatusviewmanager.mCarrierGeminiText = charsequence;
        return charsequence;
    }

*/




/*
    static boolean access$1902(KeyguardStatusViewManager keyguardstatusviewmanager, boolean flag)
    {
        keyguardstatusviewmanager.mDownloadCalibrationData = flag;
        return flag;
    }

*/











/*
    static boolean access$402(KeyguardStatusViewManager keyguardstatusviewmanager, boolean flag)
    {
        keyguardstatusviewmanager.mShowingBatteryInfo = flag;
        return flag;
    }

*/


/*
    static boolean access$502(KeyguardStatusViewManager keyguardstatusviewmanager, boolean flag)
    {
        keyguardstatusviewmanager.mPluggedIn = flag;
        return flag;
    }

*/


/*
    static int access$602(KeyguardStatusViewManager keyguardstatusviewmanager, int i)
    {
        keyguardstatusviewmanager.mBatteryLevel = i;
        return i;
    }

*/



/*
    static CharSequence access$802(KeyguardStatusViewManager keyguardstatusviewmanager, CharSequence charsequence)
    {
        keyguardstatusviewmanager.mPlmn = charsequence;
        return charsequence;
    }

*/


/*
    static CharSequence access$902(KeyguardStatusViewManager keyguardstatusviewmanager, CharSequence charsequence)
    {
        keyguardstatusviewmanager.mSpn = charsequence;
        return charsequence;
    }

*/
}
