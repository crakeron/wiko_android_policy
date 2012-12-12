// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.*;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.android.internal.telephony.ITelephony;
import com.android.internal.widget.LockPatternUtils;
import com.mediatek.xlog.Xlog;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardScreen, KeyguardUpdateMonitor, KeyguardScreenCallback

public class SimUnlockScreen extends LinearLayout
    implements KeyguardScreen, android.view.View.OnClickListener, KeyguardUpdateMonitor.InfoCallback, KeyguardUpdateMonitor.RadioStateCallback
{
    public static class Toast
    {
        private class TN extends android.app.ITransientNotification.Stub
        {

            final Runnable mHide = new Runnable() {

                final TN this$1;

                public void run()
                {
                    handleHide();
                }

                    
                    {
                        this$1 = TN.this;
                        super();
                    }
            }
;
            private final android.view.WindowManager.LayoutParams mParams = new android.view.WindowManager.LayoutParams();
            final Runnable mShow = new Runnable() {

                final TN this$1;

                public void run()
                {
                    handleShow();
                }

                    
                    {
                        this$1 = TN.this;
                        super();
                    }
            }
;
            WindowManagerImpl mWM;
            final Toast this$0;

            public void handleHide()
            {
                if (mView != null)
                {
                    if (mView.getParent() != null)
                        mWM.removeView(mView);
                    mView = null;
                }
            }

            public void handleShow()
            {
                mWM = WindowManagerImpl.getDefault();
                int i = mGravity;
                mParams.gravity = i;
                if ((i & 7) == 7)
                    mParams.horizontalWeight = 1F;
                if ((i & 0x70) == 112)
                    mParams.verticalWeight = 1F;
                mParams.y = mY;
                if (mView != null)
                {
                    if (mView.getParent() != null)
                        mWM.removeView(mView);
                    mWM.addView(mView, mParams);
                }
            }

            public void hide()
            {
                mHandler.post(mHide);
            }

            public void show()
            {
                mHandler.post(mShow);
            }

            TN()
            {
                this$0 = Toast.this;
                super();
                android.view.WindowManager.LayoutParams layoutparams = mParams;
                layoutparams.height = -2;
                layoutparams.width = -2;
                layoutparams.flags = 152;
                layoutparams.format = -3;
                layoutparams.windowAnimations = 0x1030004;
                layoutparams.type = 2009;
                layoutparams.setTitle("Toast");
            }
        }


        static final String TAG = "Toast";
        static final boolean localLOGV;
        final Context mContext;
        int mGravity;
        final Handler mHandler = new Handler();
        final TN mTN = new TN();
        View mView;
        int mY;
        private INotificationManager sService;

        private INotificationManager getService()
        {
            INotificationManager inotificationmanager;
            if (sService == null)
            {
                sService = android.app.INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                inotificationmanager = sService;
            } else
            {
                inotificationmanager = sService;
            }
            return inotificationmanager;
        }

        public static Toast makeText(Context context, CharSequence charsequence)
        {
            Toast toast = new Toast(context);
            View view = ((LayoutInflater)context.getSystemService("layout_inflater")).inflate(0x10900b1, null);
            ((TextView)view.findViewById(0x102000b)).setText(charsequence);
            toast.mView = view;
            return toast;
        }

        public void cancel()
        {
            mTN.hide();
        }

        public void show()
        {
            TN tn;
            String s;
            INotificationManager inotificationmanager;
            if (mView == null)
                throw new RuntimeException("setView must have been called");
            inotificationmanager = getService();
            s = mContext.getPackageName();
            tn = mTN;
            inotificationmanager.enqueueToast(s, tn, 0);
_L2:
            return;
            JVM INSTR pop ;
            if (true) goto _L2; else goto _L1
_L1:
        }

        public Toast(Context context)
        {
            mGravity = 81;
            mContext = context;
            mY = context.getResources().getDimensionPixelSize(0x1050009);
        }
    }

    private abstract class CheckSimPin extends Thread
    {

        private final String mPin;
        private final String mPuk;
        private boolean result;
        final SimUnlockScreen this$0;

        abstract void onSimLockChangedResponse(boolean flag);

        public void run()
        {
            Log.d("SimUnlockScreen", (new StringBuilder()).append("CheckSimPin,mSimId =").append(mSimId).toString());
            if (mSimId != 0) goto _L2; else goto _L1
_L1:
            Log.d("SimUnlockScreen", "CheckSimPin, check sim 1 or single");
            if (mUpdateMonitor.getSimState(0) != com.android.internal.telephony.IccCard.State.PIN_REQUIRED) goto _L4; else goto _L3
_L3:
            result = com.android.internal.telephony.ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPinGemini(mPin, 0);
_L6:
            post(new Runnable() {

                final CheckSimPin this$1;

                public void run()
                {
                    onSimLockChangedResponse(result);
                }

                
                {
                    this$1 = CheckSimPin.this;
                    super();
                }
            }
);
            break; /* Loop/switch isn't completed */
_L4:
            if (mUpdateMonitor.getSimState(0) == com.android.internal.telephony.IccCard.State.PUK_REQUIRED)
                result = com.android.internal.telephony.ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPukGemini(mPuk, mPin, 0);
            if (true) goto _L6; else goto _L5
            JVM INSTR pop ;
            post(new Runnable() {

                final CheckSimPin this$1;

                public void run()
                {
                    onSimLockChangedResponse(false);
                }

                
                {
                    this$1 = CheckSimPin.this;
                    super();
                }
            }
);
              goto _L5
_L2:
            Log.d("SimUnlockScreen", "CheckSimPin, check sim 2");
            if (mUpdateMonitor.getSimState(1) != com.android.internal.telephony.IccCard.State.PIN_REQUIRED) goto _L8; else goto _L7
_L7:
            result = com.android.internal.telephony.ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPinGemini(mPin, 1);
_L9:
            post(new Runnable() {

                final CheckSimPin this$1;

                public void run()
                {
                    onSimLockChangedResponse(result);
                }

                
                {
                    this$1 = CheckSimPin.this;
                    super();
                }
            }
);
            break; /* Loop/switch isn't completed */
_L8:
            if (mUpdateMonitor.getSimState(1) == com.android.internal.telephony.IccCard.State.PUK_REQUIRED)
                result = com.android.internal.telephony.ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPukGemini(mPuk, mPin, 1);
            if (true) goto _L9; else goto _L5
_L5:
        }


        protected CheckSimPin(String s)
        {
            this$0 = SimUnlockScreen.this;
            super();
            mPin = s;
            mPuk = null;
        }

        protected CheckSimPin(String s, int i)
        {
            this$0 = SimUnlockScreen.this;
            super();
            mPin = s;
            mPuk = null;
        }

        protected CheckSimPin(String s, String s1, int i)
        {
            this$0 = SimUnlockScreen.this;
            super();
            mPin = s1;
            mPuk = s;
        }
    }

    static final class SIMStatus extends Enum
    {

        private static final SIMStatus $VALUES[];
        public static final SIMStatus SIM1_BOTH_SIM_INSERTED;
        public static final SIMStatus SIM1_ONLY_SIM1_INSERTED;
        public static final SIMStatus SIM2_BOTH_SIM_INSERTED;
        public static final SIMStatus SIM2_ONLY_SIM1_INSERTED;

        public static SIMStatus valueOf(String s)
        {
            return (SIMStatus)Enum.valueOf(com/android/internal/policy/impl/SimUnlockScreen$SIMStatus, s);
        }

        public static SIMStatus[] values()
        {
            return (SIMStatus[])$VALUES.clone();
        }

        static 
        {
            SIM1_BOTH_SIM_INSERTED = new SIMStatus("SIM1_BOTH_SIM_INSERTED", 0);
            SIM1_ONLY_SIM1_INSERTED = new SIMStatus("SIM1_ONLY_SIM1_INSERTED", 1);
            SIM2_BOTH_SIM_INSERTED = new SIMStatus("SIM2_BOTH_SIM_INSERTED", 2);
            SIM2_ONLY_SIM1_INSERTED = new SIMStatus("SIM2_ONLY_SIM1_INSERTED", 3);
            SIMStatus asimstatus[] = new SIMStatus[4];
            asimstatus[0] = SIM1_BOTH_SIM_INSERTED;
            asimstatus[1] = SIM1_ONLY_SIM1_INSERTED;
            asimstatus[2] = SIM2_BOTH_SIM_INSERTED;
            asimstatus[3] = SIM2_ONLY_SIM1_INSERTED;
            $VALUES = asimstatus;
        }

        private SIMStatus(String s, int i)
        {
            super(s, i);
        }
    }


    private static final char DIGITS[];
    private static final int DIGIT_PRESS_WAKE_MILLIS = 5000;
    private static int DISMISS_LENGTH = 0;
    private static int DISMISS_SIZE = 0;
    private static final int GET_SIM_RETRY_EMPTY = -1;
    private static final int MAX_PIN_LENGTH = 8;
    private static final int MIN_PIN_LENGTH = 4;
    private static int SIMLOCK_TYPE_PIN = 0;
    private static int SIMLOCK_TYPE_SIMMELOCK = 0;
    private static final int STATE_ENTER_FINISH = 4;
    private static final int STATE_ENTER_NEW = 2;
    private static final int STATE_ENTER_PIN = 0;
    private static final int STATE_ENTER_PUK = 1;
    private static final int STATE_REENTER_NEW = 3;
    private static final String TAG = "SimUnlockScreen";
    static final int VERIFY_TYPE_PIN = 501;
    private View mBackSpaceButton;
    private final KeyguardScreenCallback mCallback;
    private TextView mCancelButton;
    private int mCreationOrientation;
    private Button mEmergencyCallButton;
    private int mEnteredDigits;
    private final int mEnteredPin[];
    private TextView mHeaderText;
    private int mKeyboardHidden;
    private LockPatternUtils mLockPatternUtils;
    private Button mMoreInfoBtn;
    private String mNewPinText;
    private TextView mOkButton;
    private int mPINRetryCount;
    private int mPUKRetryCount;
    private KeyguardUpdateMonitor.phoneStateCallback mPhoneCallback;
    private TextView mPinText;
    private int mPukEnterState;
    private String mPukText;
    private TextView mResultPrompt;
    private TextView mSIMCardName;
    private int mSim2PINRetryCount;
    private int mSim2PUKRetryCount;
    public com.android.internal.telephony.IccCard.State mSim2State;
    public int mSimId;
    public com.android.internal.telephony.IccCard.State mSimState;
    private ProgressDialog mSimUnlockProgressDialog;
    private TextView mTimesLeft;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private boolean sim1FirstBoot;
    private boolean sim2FirstBoot;

    public SimUnlockScreen(Context context, Configuration configuration, KeyguardUpdateMonitor keyguardupdatemonitor, KeyguardScreenCallback keyguardscreencallback, LockPatternUtils lockpatternutils, int i)
    {
        LinearLayout(context);
        mResultPrompt = null;
        mTimesLeft = null;
        mSIMCardName = null;
        mMoreInfoBtn = null;
        mCancelButton = null;
        int ai[] = new int[8];
        ai[0] = 0;
        ai[1] = 0;
        ai[2] = 0;
        ai[3] = 0;
        ai[4] = 0;
        ai[5] = 0;
        ai[6] = 0;
        ai[7] = 0;
        mEnteredPin = ai;
        mEnteredDigits = 0;
        mSimUnlockProgressDialog = null;
        sim1FirstBoot = false;
        sim2FirstBoot = false;
        mSimId = 0;
        mPhoneCallback = new KeyguardUpdateMonitor.phoneStateCallback() {

            final SimUnlockScreen this$0;

            public void onPhoneStateChanged(int j)
            {
                updateEmergencyCallButtonState(mEmergencyCallButton);
            }

            
            {
                this$0 = SimUnlockScreen.this;
                super();
            }
        }
;
        mUpdateMonitor = keyguardupdatemonitor;
        mCallback = keyguardscreencallback;
        mSimId = i;
        mCreationOrientation = configuration.orientation;
        Xlog.i("SimUnlockScreen", (new StringBuilder()).append("SimUnlockScreen Constructor, mSimId=").append(i).append(", mCreationOrientation=").append(mCreationOrientation).toString());
        mKeyboardHidden = configuration.hardKeyboardHidden;
        mLockPatternUtils = lockpatternutils;
        LayoutInflater.from(context).inflate(0x207000a, this, true);
        ((TextView)findViewById(0x20c0023)).setText("");
        mSIMCardName = (TextView)findViewById(0x20c0024);
        findViewById(0x20c001b).setBackgroundDrawable(context.getResources().getDrawable(0x2020042));
        if (findViewById(0x20c001f) != null)
            findViewById(0x20c001f).setBackgroundDrawable(context.getResources().getDrawable(0x202001f));
        findViewById(0x20c0020).setBackgroundDrawable(context.getResources().getDrawable(0x2020043));
        ((ImageButton)findViewById(0x20c0021)).setImageDrawable(context.getResources().getDrawable(0x2020090));
        mTimesLeft = (TextView)findViewById(0x20c001e);
        mPinText = (TextView)findViewById(0x20c0020);
        mPinText.setTextColor(0xff000000);
        mResultPrompt = (TextView)findViewById(0x20c001c);
        if (findViewById(0x20c0022) != null)
            new TouchInput();
        mHeaderText = (TextView)findViewById(0x20c001d);
        mPinText = (TextView)findViewById(0x20c0020);
        mPinText.setTextColor(0xff000000);
        mBackSpaceButton = findViewById(0x20c0021);
        mBackSpaceButton.setOnClickListener(this);
        mEmergencyCallButton = (Button)findViewById(0x20c001b);
        updateEmergencyCallButtonState(mEmergencyCallButton);
        mOkButton = (TextView)findViewById(0x20c004b);
        mCancelButton = (TextView)findViewById(0x20c004d);
        if (mCancelButton.getText().toString().length() > DISMISS_LENGTH)
            mCancelButton.setTextSize(DISMISS_SIZE);
        if (mPinText != null)
            mPinText.setFocusable(true);
        if (mEmergencyCallButton != null)
            mEmergencyCallButton.setFocusable(true);
        mOkButton.setFocusable(true);
        mCancelButton.setFocusable(true);
        updateSimState();
        if (mKeyboardHidden != 1)
        {
            setFocusable(true);
            setFocusableInTouchMode(true);
        } else
        {
            setFocusable(true);
            setFocusableInTouchMode(true);
            mPinText.requestFocus();
            mPinText.setFocusable(true);
        }
        mEmergencyCallButton.setOnClickListener(this);
        mOkButton.setOnClickListener(this);
        mUpdateMonitor.registerInfoCallback(this);
        mUpdateMonitor.registerRadioStateCallback(this);
        mUpdateMonitor.registerPhoneStateCallback(mPhoneCallback);
        if (SystemProperties.get("gsm.siminfo.ready", "false").equals("true"))
        {
            Xlog.i("SimUnlockScreen", "siminfo already update, we should read value from the siminfo");
            dealwithSIMInfoChanged(mSimId);
        }
    }

    private void checkPin()
    {
        mNewPinText = mPinText.getText().toString();
        mEnteredDigits = mNewPinText.length();
        if (mEnteredDigits != 0)
        {
            if (mEnteredDigits >= 4)
            {
                getSimUnlockProgressDialog().show();
                (new CheckSimPin(mNewPinText) {

                    final SimUnlockScreen this$0;

                    void onSimLockChangedResponse(boolean flag)
                    {
                        Xlog.d("SimUnlockScreen", (new StringBuilder()).append("onSimLockChangedResponse, success = ").append(flag).toString());
                        if (mSimUnlockProgressDialog != null)
                            mSimUnlockProgressDialog.hide();
                        if (!flag)
                        {
                            if (mPukEnterState != 0)
                            {
                                if (mPukEnterState == 1)
                                {
                                    mResultPrompt.setText(0x2050080);
                                    mResultPrompt.setFocusable(true);
                                    if (getRetryPukCount(0) != 0)
                                    {
                                        mHeaderText.setText(0x10402d1);
                                        mTimesLeft.setText(getRetryPin(0));
                                    } else
                                    {
                                        mHeaderText.setText(0x2050011);
                                        mTimesLeft.setText(getRetryPuk(0));
                                        mPukEnterState = 1;
                                    }
                                    mPinText.setText("");
                                    mEnteredDigits = 0;
                                }
                            } else
                            {
                                mResultPrompt.setText(0x2050080);
                                mResultPrompt.setFocusable(true);
                                if (getRetryPinCount(0) != 0)
                                {
                                    mHeaderText.setText(0x10402d1);
                                    mTimesLeft.setText(getRetryPin(0));
                                } else
                                {
                                    mHeaderText.setText(0x2050011);
                                    mTimesLeft.setText(getRetryPuk(0));
                                    mPukEnterState = 1;
                                }
                                mPinText.setText("");
                                mEnteredDigits = 0;
                            }
                        } else
                        {
                            mUpdateMonitor.reportSimUnlocked(mSimId);
                            if (!mUpdateMonitor.DM_IsLocked())
                                mCallback.goToUnlockScreen();
                            else
                                mCallback.goToLockScreen();
                        }
                        mCallback.pokeWakelock();
                    }

            
            {
                this$0 = SimUnlockScreen.this;
                super(s);
            }
                }
).start();
                mPinText.setText("");
            } else
            {
                mResultPrompt.setText(0x2050081);
                mPinText.setText("");
                mEnteredDigits = 0;
                mCallback.pokeWakelock();
            }
        } else
        {
            mResultPrompt.setText(0x2050081);
        }
    }

    private void checkPin(int i)
    {
        mNewPinText = mPinText.getText().toString();
        mEnteredDigits = mNewPinText.length();
        if (mEnteredDigits != 0)
        {
            if (mEnteredDigits >= 4)
            {
                getSimUnlockProgressDialog().show();
                (new CheckSimPin(mNewPinText, i) ).start();
                mPinText.setText("");
            } else
            {
                mResultPrompt.setText(0x2050081);
                mResultPrompt.setFocusable(true);
                mPinText.setText("");
                mEnteredDigits = 0;
                mCallback.pokeWakelock();
            }
        } else
        {
            mResultPrompt.setText(0x2050081);
        }
    }

    private void checkPuk()
    {
        updatePinEnterScreen();
        if (mPukEnterState == 4)
        {
            getSimUnlockProgressDialog().show();
            (new CheckSimPin(mPukText, mNewPinText, 0) ).start();
            mPinText.setText("");
        }
    }

    private void checkPuk(int i)
    {
        updatePinEnterScreen();
        if (mPukEnterState == 4)
        {
            getSimUnlockProgressDialog().show();
            (new CheckSimPin(mNewPinText, mSimId, i) ).start();
            mPinText.setText("");
        }
    }

    private void dealWithEnterMessage()
    {
        if (mUpdateMonitor.getSimState(0) != com.android.internal.telephony.IccCard.State.PIN_REQUIRED || mSimId != 0)
        {
            if (mUpdateMonitor.getSimState(1) != com.android.internal.telephony.IccCard.State.PIN_REQUIRED || 1 != mSimId)
            {
                if (mUpdateMonitor.getSimState(0) != com.android.internal.telephony.IccCard.State.PUK_REQUIRED || mSimId != 0)
                {
                    if (mUpdateMonitor.getSimState(1) != com.android.internal.telephony.IccCard.State.PUK_REQUIRED || 1 != mSimId)
                    {
                        Xlog.d("SimUnlockScreen", "wrong status");
                    } else
                    {
                        Xlog.d("SimUnlockScreen", (new StringBuilder()).append("onClick, check PUK, mSimId=").append(mSimId).toString());
                        checkPuk(mSimId);
                    }
                } else
                {
                    Xlog.d("SimUnlockScreen", (new StringBuilder()).append("onClick, check PUK, mSimId=").append(mSimId).toString());
                    checkPuk(mSimId);
                }
            } else
            {
                Xlog.d("SimUnlockScreen", (new StringBuilder()).append("onClick, check PIN, mSimId=").append(mSimId).toString());
                checkPin(mSimId);
            }
        } else
        {
            Xlog.d("SimUnlockScreen", (new StringBuilder()).append("onClick, check PIN, mSimId=").append(mSimId).toString());
            checkPin(mSimId);
        }
    }

    private void dealwithSIMInfoChanged(int i)
    {
        Object obj;
        String s;
        s = null;
        obj = null;
        if (mUpdateMonitor == null) goto _L2; else goto _L1
_L1:
        obj = mUpdateMonitor.getOptrDrawableBySlot(i);
        obj = obj;
_L3:
        s = mUpdateMonitor.getOptrNameBySlot(i);
        s = s;
_L2:
        if (s == null)
        {
            obj = (android.widget.LinearLayout.LayoutParams)mSIMCardName.getLayoutParams();
            obj.width = 0;
            mSIMCardName.setLayoutParams(((android.view.ViewGroup.LayoutParams) (obj)));
            obj = (TextView)findViewById(0x20c0023);
            if (1 == mSimId)
            {
                Xlog.i("SimUnlockScreen", "SIM2 is first reboot");
                sim2FirstBoot = true;
                ((TextView) (obj)).setText(0x205008c);
            } else
            {
                Xlog.i("SimUnlockScreen", "SIM1 is first reboot");
                sim1FirstBoot = true;
                ((TextView) (obj)).setText(0x205008b);
            }
            if (mMoreInfoBtn != null)
            {
                mMoreInfoBtn.setText(0x2050089);
            } else
            {
                mMoreInfoBtn = (Button)findViewById(0x20c0025);
                if (mMoreInfoBtn != null)
                {
                    mMoreInfoBtn.setVisibility(0);
                    mMoreInfoBtn.setText(0x2050089);
                    mMoreInfoBtn.setOnClickListener(new android.view.View.OnClickListener() {

                        final SimUnlockScreen this$0;

                        public void onClick(View view)
                        {
                            displaythesimcardinfo(mSimId);
                        }

            
            {
                this$0 = SimUnlockScreen.this;
                super();
            }
                    }
);
                }
            }
        } else
        if (mSimId == i)
        {
            Xlog.i("SimUnlockScreen", "dealwithSIMInfoChanged, we will refresh the SIMinfo");
            mSIMCardName.setText(s);
            if (obj != null)
                mSIMCardName.setBackgroundDrawable(((android.graphics.drawable.Drawable) (obj)));
        }
        return;
        JVM INSTR pop ;
        Xlog.w("SimUnlockScreen", (new StringBuilder()).append("getOptrDrawableBySlot exception, slotId=").append(i).toString());
          goto _L3
        JVM INSTR pop ;
        Xlog.w("SimUnlockScreen", (new StringBuilder()).append("getOptrNameBySlot exception, slotId=").append(i).toString());
          goto _L2
    }

    private void displaythesimcardinfo(int i)
    {
        if (i != 0 || !mUpdateMonitor.IsSIMInserted(1))
        {
            if (i != 0 || mUpdateMonitor.IsSIMInserted(1))
            {
                if (1 != i || !mUpdateMonitor.IsSIMInserted(0))
                    popupSIMInfoDialog(SIMStatus.SIM2_ONLY_SIM1_INSERTED);
                else
                    popupSIMInfoDialog(SIMStatus.SIM2_BOTH_SIM_INSERTED);
            } else
            {
                popupSIMInfoDialog(SIMStatus.SIM1_ONLY_SIM1_INSERTED);
            }
        } else
        {
            popupSIMInfoDialog(SIMStatus.SIM1_BOTH_SIM_INSERTED);
        }
    }

    private String getRetryPin(int i)
    {
        mPINRetryCount = getRetryPinCount(i);
        Object obj;
        switch (mPINRetryCount)
        {
        default:
            obj = (new StringBuilder()).append("(");
            Context context = mContext;
            Object aobj[] = new Object[1];
            aobj[0] = Integer.valueOf(mPINRetryCount);
            obj = ((StringBuilder) (obj)).append(context.getString(0x205001a, aobj)).append(")").toString();
            break;

        case -1: 
            obj = " ";
            break;
        }
        return ((String) (obj));
    }

    private int getRetryPinCount(int i)
    {
        int j;
        if (mSimId != 1)
            j = SystemProperties.getInt("gsm.sim.retry.pin1", -1);
        else
            j = SystemProperties.getInt("gsm.sim.retry.pin1.2", -1);
        return j;
    }

    private String getRetryPuk(int i)
    {
        mPUKRetryCount = getRetryPukCount(i);
        Object obj;
        switch (mPUKRetryCount)
        {
        default:
            obj = (new StringBuilder()).append("(");
            Context context = mContext;
            Object aobj[] = new Object[1];
            aobj[0] = Integer.valueOf(mPUKRetryCount);
            obj = ((StringBuilder) (obj)).append(context.getString(0x205001a, aobj)).append(")").toString();
            break;

        case -1: 
            obj = " ";
            break;
        }
        return ((String) (obj));
    }

    private int getRetryPukCount(int i)
    {
        int j;
        if (mSimId != 1)
            j = SystemProperties.getInt("gsm.sim.retry.puk1", -1);
        else
            j = SystemProperties.getInt("gsm.sim.retry.puk1.2", -1);
        return j;
    }

    private void getSIMCardName(int i)
    {
        String s;
        android.graphics.drawable.Drawable drawable;
        drawable = null;
        s = null;
        drawable = mUpdateMonitor.getOptrDrawableBySlot(i);
        drawable = drawable;
_L1:
        if (drawable != null)
            mSIMCardName.setBackgroundDrawable(drawable);
        s = mUpdateMonitor.getOptrNameBySlot(i);
        s = s;
_L2:
        Xlog.i("SimUnlockScreen", (new StringBuilder()).append("slotId=").append(i).append(", mSimId=").append(mSimId).append(",s=").append(s).toString());
        if (s != null)
            mSIMCardName.setText(s);
        else
        if (mSimId == 1 && sim2FirstBoot || mSimId == 0 && sim1FirstBoot)
        {
            Xlog.i("SimUnlockScreen", "getSIMCardName for the first reboot");
            TextView textview = (TextView)findViewById(0x20c0023);
            if (1 == i)
                textview.setText(0x205008c);
            else
                textview.setText(0x205008b);
        } else
        {
            Xlog.i("SimUnlockScreen", "getSIMCardName for seaching SIM card");
            mSIMCardName.setText(0x2050087);
        }
        return;
        JVM INSTR pop ;
        Xlog.w("SimUnlockScreen", (new StringBuilder()).append("getSIMCardName::getOptrDrawableBySlot exception, slotId=").append(i).toString());
          goto _L1
        JVM INSTR pop ;
        Xlog.w("SimUnlockScreen", (new StringBuilder()).append("getSIMCardName::getOptrNameBySlot exception, slotId=").append(i).toString());
          goto _L2
    }

    private Dialog getSimUnlockProgressDialog()
    {
        if (mSimUnlockProgressDialog == null)
        {
            mSimUnlockProgressDialog = new ProgressDialog(mContext);
            mSimUnlockProgressDialog.setMessage(mContext.getString(0x10402f9));
            mSimUnlockProgressDialog.setIndeterminate(true);
            mSimUnlockProgressDialog.setCancelable(false);
            mSimUnlockProgressDialog.getWindow().setType(2009);
            mContext.getResources().getBoolean(0x1110008);
        }
        return mSimUnlockProgressDialog;
    }

    private boolean isSimLockDisplay(int i, int j)
    {
        boolean flag = false;
        if (i >= 0)
        {
            Long long1 = Long.valueOf(Long.valueOf(android.provider.Settings.System.getLong(mContext.getContentResolver(), "sim_lock_state_setting", 0L)).longValue() >>> i * 2);
            if (SIMLOCK_TYPE_PIN != j)
            {
                if (SIMLOCK_TYPE_SIMMELOCK != j)
                    flag = true;
                else
                if (1L == (1L & Long.valueOf(long1.longValue() >>> 1).longValue()))
                    flag = true;
            } else
            if (1L == (1L & long1.longValue()))
                flag = true;
        }
        return flag;
    }

    private void popupSIMInfoDialog(SIMStatus simstatus)
    {
        Object obj = new ImageView(mContext);
        ((ImageView) (obj)).setScaleType(android.widget.ImageView.ScaleType.FIT_XY);
        class _cls8
        {

            static final int $SwitchMap$com$android$internal$policy$impl$SimUnlockScreen$SIMStatus[];

            static 
            {
                $SwitchMap$com$android$internal$policy$impl$SimUnlockScreen$SIMStatus = new int[SIMStatus.values().length];
                try
                {
                    $SwitchMap$com$android$internal$policy$impl$SimUnlockScreen$SIMStatus[SIMStatus.SIM1_BOTH_SIM_INSERTED.ordinal()] = 1;
                }
                catch (NoSuchFieldError _ex) { }
                try
                {
                    $SwitchMap$com$android$internal$policy$impl$SimUnlockScreen$SIMStatus[SIMStatus.SIM1_ONLY_SIM1_INSERTED.ordinal()] = 2;
                }
                catch (NoSuchFieldError _ex) { }
                try
                {
                    $SwitchMap$com$android$internal$policy$impl$SimUnlockScreen$SIMStatus[SIMStatus.SIM2_BOTH_SIM_INSERTED.ordinal()] = 3;
                }
                catch (NoSuchFieldError _ex) { }
                $SwitchMap$com$android$internal$policy$impl$SimUnlockScreen$SIMStatus[SIMStatus.SIM2_ONLY_SIM1_INSERTED.ordinal()] = 4;
_L2:
                return;
                JVM INSTR pop ;
                if (true) goto _L2; else goto _L1
_L1:
            }
        }

        switch (_cls8..SwitchMap.com.android.internal.policy.impl.SimUnlockScreen.SIMStatus[simstatus.ordinal()])
        {
        case 1: // '\001'
            ((ImageView) (obj)).setBackgroundDrawable(getResources().getDrawable(0x20200c2));
            break;

        case 2: // '\002'
            ((ImageView) (obj)).setBackgroundDrawable(getResources().getDrawable(0x20200c4));
            break;

        case 3: // '\003'
            ((ImageView) (obj)).setBackgroundDrawable(getResources().getDrawable(0x20200c7));
            break;

        case 4: // '\004'
            ((ImageView) (obj)).setBackgroundDrawable(getResources().getDrawable(0x20200c9));
            break;
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setTitle(0x205008a);
        builder.setCancelable(false);
        builder.setView(((View) (obj)));
        builder.setPositiveButton(0x104000a, null);
        obj = builder.create();
        ((AlertDialog) (obj)).getWindow().setType(2009);
        ((AlertDialog) (obj)).show();
    }

    private void reportDigit(int i)
    {
        if (mEnteredDigits == 0)
            mPinText.setText("");
        if (mEnteredDigits != 8)
        {
            Xlog.v("SimUnlockScreen", (new StringBuilder()).append("EnterDigits = ").append(Integer.toString(mEnteredDigits)).append(" input digit is ").append(Integer.toString(i)).toString());
            mPinText.append(Integer.toString(i));
            int ai[] = mEnteredPin;
            int j = mEnteredDigits;
            mEnteredDigits = j + 1;
            ai[j] = i;
        }
    }

    private void setInputInvalidAlertDialog(CharSequence charsequence, boolean flag)
    {
        Object obj = new StringBuilder(charsequence);
        if (!flag)
        {
            Toast.makeText(mContext, ((CharSequence) (obj))).show();
        } else
        {
            obj = (new android.app.AlertDialog.Builder(mContext)).setMessage(((CharSequence) (obj))).setPositiveButton(0x104000a, null).setCancelable(true).create();
            ((AlertDialog) (obj)).getWindow().setType(2009);
            ((AlertDialog) (obj)).getWindow().addFlags(2);
            ((AlertDialog) (obj)).show();
        }
    }

    private void setSimLockScreenDone(int i, int j)
    {
        if (i >= 0)
            if (!isSimLockDisplay(i, j))
            {
                Long long2 = Long.valueOf(android.provider.Settings.System.getLong(mContext.getContentResolver(), "sim_lock_state_setting", 0L));
                Long long1 = Long.valueOf(Long.valueOf(1L).longValue() << i * 2);
                Xlog.d("SimUnlockScreen", (new StringBuilder()).append("setSimLockScreenDone1 bitset = ").append(long1).toString());
                if (SIMLOCK_TYPE_SIMMELOCK == j)
                    long1 = Long.valueOf(long1.longValue() << 1);
                Xlog.d("SimUnlockScreen", (new StringBuilder()).append("setSimLockScreenDone2 bitset = ").append(long1).toString());
                long1 = Long.valueOf(long2.longValue() + long1.longValue());
                android.provider.Settings.System.putLong(mContext.getContentResolver(), "sim_lock_state_setting", long1.longValue());
            } else
            {
                Xlog.d("SimUnlockScreen", "setSimLockScreenDone the SimLock display is done");
            }
    }

    private void updatePinEnterScreen()
    {
        if (!sim2FirstBoot)
        {
            if (!sim1FirstBoot)
                ((TextView)findViewById(0x20c0023)).setText("");
            else
                ((TextView)findViewById(0x20c0023)).setText(0x205008b);
        } else
        {
            ((TextView)findViewById(0x20c0023)).setText(0x205008c);
        }
        switch (mPukEnterState)
        {
        default:
            break;

        case 1: // '\001'
            mPukText = mPinText.getText().toString();
            if (!validatePin(mPukText, true))
            {
                mResultPrompt.setText(0x2050015);
                mResultPrompt.setFocusable(true);
            } else
            {
                mPukEnterState = 2;
                mHeaderText.setText(0x2050013);
                mResultPrompt.setText("");
                mTimesLeft.setVisibility(8);
            }
            break;

        case 2: // '\002'
            mNewPinText = mPinText.getText().toString();
            if (!validatePin(mNewPinText, false))
            {
                mResultPrompt.setText(0x2050081);
                mResultPrompt.setFocusable(true);
            } else
            {
                mPukEnterState = 3;
                mHeaderText.setText(0x2050014);
                mResultPrompt.setText("");
            }
            break;

        case 3: // '\003'
            if (mNewPinText.equals(mPinText.getText().toString()))
            {
                mPukEnterState = 4;
                mResultPrompt.setText("");
            } else
            {
                mPukEnterState = 2;
                mHeaderText.setText(mContext.getText(0x2050013));
                mResultPrompt.setText(0x2050082);
                mResultPrompt.setFocusable(true);
            }
            break;
        }
        mPinText.setText("");
        mEnteredDigits = 0;
        mCallback.pokeWakelock();
    }

    private boolean validatePin(String s, boolean flag)
    {
        byte byte0;
        if (!flag)
            byte0 = 4;
        else
            byte0 = 8;
        if (s != null && s.length() >= byte0 && s.length() <= 8)
            byte0 = 1;
        else
            byte0 = 0;
        return byte0;
    }

    public void cleanUp()
    {
        if (mSimUnlockProgressDialog != null)
            mSimUnlockProgressDialog.hide();
        mUpdateMonitor.removeCallback(this);
        mUpdateMonitor.unRegisterRadioStateCallback();
        mUpdateMonitor.removeCallback(mPhoneCallback);
    }

    public boolean needsInput()
    {
        return true;
    }

    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        updateConfiguration();
    }

    public void onClick(View view)
    {
        if (view != mBackSpaceButton)
        {
            if (view != mEmergencyCallButton)
            {
                if (view == mOkButton)
                {
                    dealWithEnterMessage();
                    mCallback.pokeWakelock(10000);
                }
            } else
            {
                mCallback.takeEmergencyCallAction();
            }
        } else
        {
            Editable editable = mPinText.getEditableText();
            int i = editable.length();
            if (i > 0)
            {
                editable.delete(i - 1, i);
                mEnteredDigits = -1 + mEnteredDigits;
            }
            mCallback.pokeWakelock();
        }
    }

    protected void onConfigurationChanged(Configuration configuration)
    {
        super.onConfigurationChanged(configuration);
        updateConfiguration();
    }

    public void onDownloadCalibrationDataUpdate(boolean flag)
    {
    }

    public boolean onKeyDown(int i, KeyEvent keyevent)
    {
        boolean flag;
label0:
        {
            flag = false;
            switch (i)
            {
            default:
                char c = keyevent.getMatch(DIGITS);
                if (c != 0)
                {
                    reportDigit(c + -48);
                    break;
                }
                break label0;

            case 4: // '\004'
                break;

            case 19: // '\023'
            case 20: // '\024'
            case 21: // '\025'
            case 22: // '\026'
                mCallback.pokeWakelock();
                break label0;

            case 23: // '\027'
            case 66: // 'B'
                dealWithEnterMessage();
                break;

            case 67: // 'C'
                if (mEnteredDigits > 0)
                {
                    Editable editable = mPinText.getEditableText();
                    flag = editable.length();
                    mPinText.onKeyDown(i, keyevent);
                    if (mEnteredDigits > 0)
                    {
                        editable.delete(flag - 1, flag);
                        mEnteredDigits = -1 + mEnteredDigits;
                    }
                    mCallback.pokeWakelock();
                }
                break;
            }
            flag = true;
        }
        return flag;
    }

    public void onLockScreenUpdate(int i)
    {
        Xlog.i("SimUnlockScreen", (new StringBuilder()).append("onLockScreenUpdate name update, slotId=").append(i).append(", mSimId=").append(mSimId).toString());
        if (mSimId == i)
            getSIMCardName(i);
    }

    public void onMissedCallChanged(int i)
    {
    }

    public void onPause()
    {
    }

    public void onRadioStateChanged(int i)
    {
        Xlog.i("SimUnlockScreen", (new StringBuilder()).append("onRadioStateChanged, slotId=").append(i).append(", mSimId=").append(mSimId).toString());
        if (mSimId == i)
        {
            mCallback.pokeWakelock();
            mCallback.goToUnlockScreen();
        }
    }

    public void onRefreshBatteryInfo(boolean flag, boolean flag1, int i)
    {
    }

    public void onRefreshCarrierInfo(CharSequence charsequence, CharSequence charsequence1)
    {
    }

    public void onResume()
    {
        mCallback.pokeWakelock();
        updateSimState();
        mPinText.setText("");
        mEnteredDigits = 0;
        updateEmergencyCallButtonState(mEmergencyCallButton);
        InputMethodManager inputmethodmanager = (InputMethodManager)mContext.getSystemService("input_method");
        if (inputmethodmanager.isActive())
        {
            Log.i("SimUnlockScreen", "IME is showing, we should hide it");
            inputmethodmanager.hideSoftInputFromWindow(getWindowToken(), 2);
        }
    }

    public void onRingerModeChanged(int i)
    {
    }

    public void onSIMInfoChanged(int i)
    {
        Xlog.i("SimUnlockScreen", "onSIMInfoChanged");
        dealwithSIMInfoChanged(i);
    }

    public void onSearchNetworkUpdate(int i, boolean flag)
    {
    }

    public void onTimeChanged()
    {
    }

    public void onUnlockKeyguard()
    {
    }

    public void onWallpaperSetComplete()
    {
    }

    public void sendVerifyResult(int i, boolean flag)
    {
        Xlog.d("SimUnlockScreen", (new StringBuilder()).append("sendVerifyResult verifyType = ").append(i).append(" bRet = ").append(flag).toString());
        Intent intent = (new Intent("android.intent.action.CELLCONNSERVICE")).putExtra("start_type", "response");
        if (intent != null)
        {
            intent.putExtra("verfiy_type", i);
            intent.putExtra("verfiy_result", flag);
            mContext.startService(intent);
        } else
        {
            Xlog.e("SimUnlockScreen", "sendVerifyResult new retIntent failed");
        }
    }

    void updateConfiguration()
    {
        Xlog.d("SimUnlockScreen", "Call updateSimState in updateConfiguration(), refresh header text.");
        updateSimState();
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == mCreationOrientation)
        {
            if (configuration.hardKeyboardHidden != mKeyboardHidden)
            {
                mKeyboardHidden = configuration.hardKeyboardHidden;
                boolean flag;
                if (mKeyboardHidden != 1)
                    flag = false;
                else
                    flag = true;
                if (!mUpdateMonitor.isKeyguardBypassEnabled() || !flag)
                    mCallback.recreateMe(configuration);
                else
                if (!mUpdateMonitor.DM_IsLocked())
                {
                    mCallback.goToUnlockScreen();
                } else
                {
                    Xlog.i("keyguard", "we clicked cancel button");
                    mCallback.goToLockScreen();
                }
            }
        } else
        {
            mCallback.recreateMe(configuration);
        }
    }

    public void updateEmergencyCallButtonState(Button button)
    {
        int i = TelephonyManager.getDefault().getCallState();
        TelephonyManager telephonymanager = (TelephonyManager)getContext().getSystemService("phone");
        boolean flag;
        if (telephonymanager == null || !telephonymanager.isVoiceCapable())
            flag = false;
        else
            flag = true;
        if (!flag)
        {
            button.setVisibility(8);
        } else
        {
            if (i != 2)
            {
                i = 0x10402e0;
                button.setCompoundDrawablesWithIntrinsicBounds(0x2020093, 0, 0, 0);
            } else
            {
                i = 0x10402e1;
                button.setCompoundDrawablesWithIntrinsicBounds(0x2020093, 0, 0, 0);
            }
            button.setText(i);
        }
    }

    public void updateSimState()
    {
        Xlog.i("SimUnlockScreen", (new StringBuilder()).append("updateSimSate, simId=").append(mSimId).append(", sim1FirstBoot=").append(sim1FirstBoot).append(",sim2FirstBoot=").append(sim2FirstBoot).toString());
        if (mTimesLeft != null)
            mTimesLeft.setVisibility(0);
        if (mResultPrompt != null)
            mResultPrompt.setText("");
        if (sim1FirstBoot || sim2FirstBoot)
            if (mMoreInfoBtn == null)
            {
                mMoreInfoBtn = (Button)findViewById(0x20c0025);
                if (mMoreInfoBtn != null)
                {
                    mMoreInfoBtn.setVisibility(0);
                    mMoreInfoBtn.setText(0x2050089);
                    mMoreInfoBtn.setOnClickListener(new android.view.View.OnClickListener() {

                        final SimUnlockScreen this$0;

                        public void onClick(View view)
                        {
                            displaythesimcardinfo(mSimId);
                        }

            
            {
                this$0 = SimUnlockScreen.this;
                super();
            }
                    }
);
                }
            } else
            {
                mMoreInfoBtn.setText(0x2050089);
            }
        getSIMCardName(mSimId);
        if (1 != mSimId)
        {
            if (mSimId != 0)
            {
                Log.e("SimUnlockScreen", (new StringBuilder()).append("updateSimState, wrong simId:").append(mSimId).toString());
            } else
            {
                mSimState = mUpdateMonitor.getSimState(0);
                if (mSimState != com.android.internal.telephony.IccCard.State.PUK_REQUIRED)
                {
                    if (mSimState == com.android.internal.telephony.IccCard.State.PIN_REQUIRED)
                    {
                        Xlog.d("SimUnlockScreen", "updateSimState1, mSimState = PIN_REQUIRED");
                        mHeaderText.setText(mContext.getText(0x10402d1));
                        mTimesLeft.setText(getRetryPin(0));
                        mPukEnterState = 0;
                    }
                } else
                {
                    Xlog.d("SimUnlockScreen", "updateSimState1, mSimState = PUK_REQUIRED");
                    mHeaderText.setText(mContext.getText(0x2050011));
                    mTimesLeft.setText(getRetryPuk(0));
                    mPukEnterState = 1;
                }
                if (!sim1FirstBoot)
                    ((TextView)findViewById(0x20c0023)).setText("");
                else
                    ((TextView)findViewById(0x20c0023)).setText(0x205008b);
            }
        } else
        {
            mSim2State = mUpdateMonitor.getSimState(1);
            if (com.android.internal.telephony.IccCard.State.PUK_REQUIRED != mSim2State)
            {
                if (com.android.internal.telephony.IccCard.State.PIN_REQUIRED == mSim2State)
                {
                    Xlog.d("SimUnlockScreen", "updateSimState, mSim2State = PIN_REQUIRED");
                    mHeaderText.setText(0x10402d1);
                    mTimesLeft.setText(getRetryPin(1));
                    mPukEnterState = 0;
                }
            } else
            {
                Xlog.d("SimUnlockScreen", "updateSimState, mSim2State = PUK_REQUIRED");
                mHeaderText.setText(0x2050011);
                mTimesLeft.setText(getRetryPuk(1));
                mPukEnterState = 1;
            }
            if (!sim2FirstBoot)
                ((TextView)findViewById(0x20c0023)).setText("");
            else
                ((TextView)findViewById(0x20c0023)).setText(0x205008c);
        }
    }

    static 
    {
        DISMISS_SIZE = 14;
        DISMISS_LENGTH = 7;
        SIMLOCK_TYPE_PIN = 1;
        SIMLOCK_TYPE_SIMMELOCK = 2;
        char ac[] = new char[10];
        ac[0] = '0';
        ac[1] = '1';
        ac[2] = '2';
        ac[3] = '3';
        ac[4] = '4';
        ac[5] = '5';
        ac[6] = '6';
        ac[7] = '7';
        ac[8] = '8';
        ac[9] = '9';
        DIGITS = ac;
    }






/*
    static int access$1302(SimUnlockScreen simunlockscreen, int i)
    {
        simunlockscreen.mEnteredDigits = i;
        return i;
    }

*/



/*
    static int access$1402(SimUnlockScreen simunlockscreen, int i)
    {
        simunlockscreen.mPukEnterState = i;
        return i;
    }

*/

























}
