// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.Editable;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.telephony.ITelephony;
import com.android.internal.widget.LockPatternUtils;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardScreen, KeyguardStatusViewManager, KeyguardUpdateMonitor, KeyguardScreenCallback

public class SimPukUnlockScreen extends LinearLayout
    implements KeyguardScreen, android.view.View.OnClickListener, android.view.View.OnFocusChangeListener
{
    private class TouchInput
        implements android.view.View.OnClickListener
    {

        private TextView mCancelButton;
        private TextView mEight;
        private TextView mFive;
        private TextView mFour;
        private TextView mNine;
        private TextView mOne;
        private TextView mSeven;
        private TextView mSix;
        private TextView mThree;
        private TextView mTwo;
        private TextView mZero;
        final SimPukUnlockScreen this$0;

        private int checkDigit(View view)
        {
            byte byte0 = -1;
            if (view != mZero)
            {
                if (view != mOne)
                {
                    if (view != mTwo)
                    {
                        if (view != mThree)
                        {
                            if (view != mFour)
                            {
                                if (view != mFive)
                                {
                                    if (view != mSix)
                                    {
                                        if (view != mSeven)
                                        {
                                            if (view != mEight)
                                            {
                                                if (view == mNine)
                                                    byte0 = 9;
                                            } else
                                            {
                                                byte0 = 8;
                                            }
                                        } else
                                        {
                                            byte0 = 7;
                                        }
                                    } else
                                    {
                                        byte0 = 6;
                                    }
                                } else
                                {
                                    byte0 = 5;
                                }
                            } else
                            {
                                byte0 = 4;
                            }
                        } else
                        {
                            byte0 = 3;
                        }
                    } else
                    {
                        byte0 = 2;
                    }
                } else
                {
                    byte0 = 1;
                }
            } else
            {
                byte0 = 0;
            }
            return byte0;
        }

        public void onClick(View view)
        {
            if (view != mCancelButton)
            {
                int i = checkDigit(view);
                if (i >= 0)
                {
                    mCallback.pokeWakelock(5000);
                    reportDigit(i);
                }
            } else
            {
                mPinText.setText("");
                mPukText.setText("");
                mCallback.goToLockScreen();
            }
        }

        private TouchInput()
        {
            this$0 = SimPukUnlockScreen.this;
            super();
            mZero = (TextView)findViewById(0x1020332);
            mOne = (TextView)findViewById(0x1020329);
            mTwo = (TextView)findViewById(0x102032a);
            mThree = (TextView)findViewById(0x102032b);
            mFour = (TextView)findViewById(0x102032c);
            mFive = (TextView)findViewById(0x102032d);
            mSix = (TextView)findViewById(0x102032e);
            mSeven = (TextView)findViewById(0x102032f);
            mEight = (TextView)findViewById(0x1020330);
            mNine = (TextView)findViewById(0x1020331);
            mCancelButton = (TextView)findViewById(0x1020257);
            mZero.setText("0");
            mOne.setText("1");
            mTwo.setText("2");
            mThree.setText("3");
            mFour.setText("4");
            mFive.setText("5");
            mSix.setText("6");
            mSeven.setText("7");
            mEight.setText("8");
            mNine.setText("9");
            mZero.setOnClickListener(this);
            mOne.setOnClickListener(this);
            mTwo.setOnClickListener(this);
            mThree.setOnClickListener(this);
            mFour.setOnClickListener(this);
            mFive.setOnClickListener(this);
            mSix.setOnClickListener(this);
            mSeven.setOnClickListener(this);
            mEight.setOnClickListener(this);
            mNine.setOnClickListener(this);
            mCancelButton.setOnClickListener(this);
        }

    }

    private abstract class CheckSimPuk extends Thread
    {

        private final String mPin;
        private final String mPuk;
        final SimPukUnlockScreen this$0;

        abstract void onSimLockChangedResponse(boolean flag);

        public void run()
        {
            final boolean result = com.android.internal.telephony.ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPuk(mPuk, mPin);
            post(new Runnable() {

                final CheckSimPuk this$1;
                final boolean val$result;

                public void run()
                {
                    onSimLockChangedResponse(result);
                }

                
                {
                    this$1 = CheckSimPuk.this;
                    result = flag;
                    super();
                }
            }
);
_L1:
            return;
            JVM INSTR pop ;
            post(new Runnable() {

                final CheckSimPuk this$1;

                public void run()
                {
                    onSimLockChangedResponse(false);
                }

                
                {
                    this$1 = CheckSimPuk.this;
                    super();
                }
            }
);
              goto _L1
        }

        protected CheckSimPuk(String s, String s1)
        {
            this$0 = SimPukUnlockScreen.this;
            super();
            mPuk = s;
            mPin = s1;
        }
    }


    private static final char DIGITS[];
    private static final int DIGIT_PRESS_WAKE_MILLIS = 5000;
    private final KeyguardScreenCallback mCallback;
    private int mCreationOrientation;
    private View mDelPinButton;
    private View mDelPukButton;
    private TextView mFocusedEntry;
    private TextView mHeaderText;
    private int mKeyboardHidden;
    private KeyguardStatusViewManager mKeyguardStatusViewManager;
    private LockPatternUtils mLockPatternUtils;
    private View mOkButton;
    private TextView mPinText;
    private TextView mPukText;
    private ProgressDialog mSimUnlockProgressDialog;
    private final KeyguardUpdateMonitor mUpdateMonitor;

    public SimPukUnlockScreen(Context context, Configuration configuration, KeyguardUpdateMonitor keyguardupdatemonitor, KeyguardScreenCallback keyguardscreencallback, LockPatternUtils lockpatternutils)
    {
        super(context);
        mSimUnlockProgressDialog = null;
        mUpdateMonitor = keyguardupdatemonitor;
        mCallback = keyguardscreencallback;
        mCreationOrientation = configuration.orientation;
        mKeyboardHidden = configuration.hardKeyboardHidden;
        mLockPatternUtils = lockpatternutils;
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        if (mKeyboardHidden != 1)
        {
            layoutinflater.inflate(0x1090055, this, true);
            new TouchInput();
        } else
        {
            layoutinflater.inflate(0x1090054, this, true);
        }
        mHeaderText = (TextView)findViewById(0x10202a7);
        mPukText = (TextView)findViewById(0x10202b0);
        mPinText = (TextView)findViewById(0x10202a9);
        mDelPukButton = findViewById(0x10202b1);
        mDelPinButton = findViewById(0x102029d);
        mOkButton = findViewById(0x1020287);
        mDelPinButton.setOnClickListener(this);
        mDelPukButton.setOnClickListener(this);
        mOkButton.setOnClickListener(this);
        mHeaderText.setText(0x10402d2);
        mHeaderText.setSelected(true);
        mKeyguardStatusViewManager = new KeyguardStatusViewManager(this, keyguardupdatemonitor, lockpatternutils, keyguardscreencallback, true);
        mPinText.setFocusableInTouchMode(true);
        mPinText.setOnFocusChangeListener(this);
        mPukText.setFocusableInTouchMode(true);
        mPukText.setOnFocusChangeListener(this);
    }

    private void checkPuk()
    {
        if (mPukText.getText().length() >= 8)
        {
            if (mPinText.getText().length() >= 4 && mPinText.getText().length() <= 8)
            {
                getSimUnlockProgressDialog().show();
                (new CheckSimPuk(mPukText.getText().toString(), mPinText.getText().toString()) {

                    final SimPukUnlockScreen this$0;

                    void onSimLockChangedResponse(final boolean success)
                    {
                        mPinText.post(new Runnable() {

                            final _cls1 this$1;
                            final boolean val$success;

                            public void run()
                            {
                                if (mSimUnlockProgressDialog != null)
                                    mSimUnlockProgressDialog.hide();
                                if (!success)
                                {
                                    mHeaderText.setText(0x10400cc);
                                    mPukText.setText("");
                                    mPinText.setText("");
                                } else
                                {
                                    mUpdateMonitor.reportSimUnlocked();
                                    mCallback.goToUnlockScreen();
                                }
                            }

                    
                    {
                        this$1 = _cls1.this;
                        success = flag;
                        super();
                    }
                        }
);
                    }

            
            {
                this$0 = SimPukUnlockScreen.this;
                super(s, s1);
            }
                }
).start();
            } else
            {
                mHeaderText.setText(0x10400ce);
                mPinText.setText("");
            }
        } else
        {
            mHeaderText.setText(0x10400cf);
            mPukText.setText("");
        }
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
        }
        return mSimUnlockProgressDialog;
    }

    private void reportDigit(int i)
    {
        mFocusedEntry.append(Integer.toString(i));
    }

    public void cleanUp()
    {
        if (mSimUnlockProgressDialog != null)
        {
            mSimUnlockProgressDialog.dismiss();
            mSimUnlockProgressDialog = null;
        }
        mUpdateMonitor.removeCallback(this);
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

    public void onClick(View view)
    {
        if (view != mDelPukButton)
        {
            if (view != mDelPinButton)
            {
                if (view == mOkButton)
                    checkPuk();
            } else
            {
                if (mFocusedEntry != mPinText)
                    mPinText.requestFocus();
                Editable editable = mPinText.getEditableText();
                int j = editable.length();
                if (j > 0)
                    editable.delete(j - 1, j);
            }
        } else
        {
            if (mFocusedEntry != mPukText)
                mPukText.requestFocus();
            Editable editable1 = mPukText.getEditableText();
            int i = editable1.length();
            if (i > 0)
                editable1.delete(i - 1, i);
        }
        mCallback.pokeWakelock(5000);
    }

    protected void onConfigurationChanged(Configuration configuration)
    {
        super.onConfigurationChanged(configuration);
        updateConfiguration();
    }

    public void onFocusChange(View view, boolean flag)
    {
        if (flag)
            mFocusedEntry = (TextView)view;
    }

    public boolean onKeyDown(int i, KeyEvent keyevent)
    {
        boolean flag = true;
        if (i != 4)
        {
            int j = keyevent.getMatch(DIGITS);
            if (j == 0)
            {
                if (i != 67)
                {
                    if (i != 66)
                        flag = false;
                    else
                        checkPuk();
                } else
                {
                    mFocusedEntry.onKeyDown(i, keyevent);
                    Editable editable = mFocusedEntry.getEditableText();
                    j = editable.length();
                    if (j > 0)
                        editable.delete(j - 1, j);
                    mCallback.pokeWakelock(5000);
                }
            } else
            {
                reportDigit(j + -48);
            }
        } else
        {
            mCallback.goToLockScreen();
        }
        return flag;
    }

    public void onPause()
    {
        mKeyguardStatusViewManager.onPause();
    }

    public void onResume()
    {
        mHeaderText.setText(0x10402d2);
        mKeyguardStatusViewManager.onResume();
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

    static 
    {
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







}
