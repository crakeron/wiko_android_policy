// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.accounts.*;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.android.internal.widget.LockPatternUtils;
import java.io.IOException;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardScreen, KeyguardStatusViewManager, KeyguardScreenCallback, KeyguardUpdateMonitor

public class AccountUnlockScreen extends RelativeLayout
    implements KeyguardScreen, android.view.View.OnClickListener, TextWatcher
{

    private static final int AWAKE_POKE_MILLIS = 30000;
    private static final String LOCK_PATTERN_CLASS = "com.android.settings.ChooseLockGeneric";
    private static final String LOCK_PATTERN_PACKAGE = "com.android.settings";
    private KeyguardScreenCallback mCallback;
    private ProgressDialog mCheckingDialog;
    private TextView mInstructions;
    private KeyguardStatusViewManager mKeyguardStatusViewManager;
    private LockPatternUtils mLockPatternUtils;
    private EditText mLogin;
    private Button mOk;
    private EditText mPassword;
    private TextView mTopHeader;
    private KeyguardUpdateMonitor mUpdateMonitor;

    public AccountUnlockScreen(Context context, Configuration configuration, KeyguardUpdateMonitor keyguardupdatemonitor, KeyguardScreenCallback keyguardscreencallback, LockPatternUtils lockpatternutils)
    {
        super(context);
        mCallback = keyguardscreencallback;
        mLockPatternUtils = lockpatternutils;
        LayoutInflater.from(context).inflate(0x109004c, this, true);
        mTopHeader = (TextView)findViewById(0x1020282);
        Object obj = mTopHeader;
        int i;
        if (!mLockPatternUtils.isPermanentlyLocked())
            i = 0x1040302;
        else
            i = 0x1040303;
        ((TextView) (obj)).setText(i);
        mInstructions = (TextView)findViewById(0x1020284);
        mLogin = (EditText)findViewById(0x1020285);
        obj = mLogin;
        InputFilter ainputfilter[] = new InputFilter[1];
        ainputfilter[0] = new android.text.LoginFilter.UsernameFilterGeneric();
        ((EditText) (obj)).setFilters(ainputfilter);
        mLogin.addTextChangedListener(this);
        mPassword = (EditText)findViewById(0x1020286);
        mPassword.addTextChangedListener(this);
        mOk = (Button)findViewById(0x1020287);
        mOk.setOnClickListener(this);
        mUpdateMonitor = keyguardupdatemonitor;
        mKeyguardStatusViewManager = new KeyguardStatusViewManager(this, keyguardupdatemonitor, lockpatternutils, keyguardscreencallback, true);
    }

    private void asyncCheckPassword()
    {
        mCallback.pokeWakelock(30000);
        Object obj = mLogin.getText().toString();
        String s = mPassword.getText().toString();
        obj = findIntendedAccount(((String) (obj)));
        if (obj != null)
        {
            getProgressDialog().show();
            Bundle bundle = new Bundle();
            bundle.putString("password", s);
            AccountManager.get(mContext).confirmCredentials(((Account) (obj)), bundle, null, new AccountManagerCallback() {

                final AccountUnlockScreen this$0;

                public void run(AccountManagerFuture accountmanagerfuture)
                {
                    mCallback.pokeWakelock(30000);
                    boolean flag = ((Bundle)accountmanagerfuture.getResult()).getBoolean("booleanResult");
                    postOnCheckPasswordResult(flag);
                    mLogin.post(new Runnable() {

                        final _cls2 this$1;

                        public void run()
                        {
                            getProgressDialog().hide();
                        }

                    
                    {
                        this$1 = _cls2.this;
                        super();
                    }
                    }
);
_L1:
                    return;
                    JVM INSTR pop ;
                    postOnCheckPasswordResult(false);
                    mLogin.post(new _cls1());
                      goto _L1
                    JVM INSTR pop ;
                    postOnCheckPasswordResult(false);
                    mLogin.post(new _cls1());
                      goto _L1
                    JVM INSTR pop ;
                    postOnCheckPasswordResult(false);
                    mLogin.post(new _cls1());
                      goto _L1
                    Exception exception;
                    exception;
                    mLogin.post(new _cls1());
                    throw exception;
                }

            
            {
                this$0 = AccountUnlockScreen.this;
                super();
            }
            }
, null);
        } else
        {
            postOnCheckPasswordResult(false);
        }
    }

    private Account findIntendedAccount(String s)
    {
        Account aaccount[] = AccountManager.get(mContext).getAccountsByType("com.google");
        Account account = null;
        int k = 0;
        int i = aaccount.length;
        int j = 0;
        do
        {
            if (j >= i)
                return account;
            Account account1 = aaccount[j];
            byte byte0 = 0;
            if (!s.equals(account1.name))
            {
                if (!s.equalsIgnoreCase(account1.name))
                {
                    if (s.indexOf('@') < 0)
                    {
                        int l = account1.name.indexOf('@');
                        if (l >= 0)
                        {
                            String s1 = account1.name.substring(0, l);
                            if (!s.equals(s1))
                            {
                                if (s.equalsIgnoreCase(s1))
                                    byte0 = 1;
                            } else
                            {
                                byte0 = 2;
                            }
                        }
                    }
                } else
                {
                    byte0 = 3;
                }
            } else
            {
                byte0 = 4;
            }
            if (byte0 <= k)
            {
                if (byte0 == k)
                    account = null;
            } else
            {
                account = account1;
                k = byte0;
            }
            j++;
        } while (true);
    }

    private Dialog getProgressDialog()
    {
        if (mCheckingDialog == null)
        {
            mCheckingDialog = new ProgressDialog(mContext);
            mCheckingDialog.setMessage(mContext.getString(0x104030a));
            mCheckingDialog.setIndeterminate(true);
            mCheckingDialog.setCancelable(false);
            mCheckingDialog.getWindow().setType(2009);
        }
        return mCheckingDialog;
    }

    private void postOnCheckPasswordResult(final boolean success)
    {
        mLogin.post(new Runnable() );
    }

    public void afterTextChanged(Editable editable)
    {
    }

    public void beforeTextChanged(CharSequence charsequence, int i, int j, int k)
    {
    }

    public void cleanUp()
    {
        if (mCheckingDialog != null)
            mCheckingDialog.hide();
        mUpdateMonitor.removeCallback(this);
        mCallback = null;
        mLockPatternUtils = null;
        mUpdateMonitor = null;
    }

    public boolean dispatchKeyEvent(KeyEvent keyevent)
    {
        boolean flag;
        if (keyevent.getAction() != 0 || keyevent.getKeyCode() != 4)
        {
            flag = dispatchKeyEvent(keyevent);
        } else
        {
            if (!mLockPatternUtils.isPermanentlyLocked())
                mCallback.forgotPattern(false);
            else
                mCallback.goToLockScreen();
            flag = true;
        }
        return flag;
    }

    public boolean needsInput()
    {
        return true;
    }

    public void onClick(View view)
    {
        mCallback.pokeWakelock();
        if (view == mOk)
            asyncCheckPassword();
    }

    public void onPause()
    {
        mKeyguardStatusViewManager.onPause();
    }

    protected boolean onRequestFocusInDescendants(int i, Rect rect)
    {
        return mLogin.requestFocus(i, rect);
    }

    public void onResume()
    {
        mLogin.setText("");
        mPassword.setText("");
        mLogin.requestFocus();
        mKeyguardStatusViewManager.onResume();
    }

    public void onTextChanged(CharSequence charsequence, int i, int j, int k)
    {
        mCallback.pokeWakelock(30000);
    }








}
