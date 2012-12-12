// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.*;
import android.security.KeyStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.android.internal.widget.*;
import java.util.Iterator;
import java.util.List;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardScreen, KeyguardStatusViewManager, KeyguardUpdateMonitor, KeyguardScreenCallback

public class PasswordUnlockScreen extends LinearLayout
    implements KeyguardScreen, android.widget.TextView.OnEditorActionListener
{

    private static final int MINIMUM_PASSWORD_LENGTH_BEFORE_REPORT = 3;
    private static final String TAG = "PasswordUnlockScreen";
    private final KeyguardScreenCallback mCallback;
    private final int mCreationHardKeyboardHidden;
    private final int mCreationOrientation;
    private final boolean mIsAlpha;
    private final PasswordEntryKeyboardHelper mKeyboardHelper;
    private final PasswordEntryKeyboardView mKeyboardView = (PasswordEntryKeyboardView)findViewById(0x102029f);
    private final LockPatternUtils mLockPatternUtils;
    private final EditText mPasswordEntry = (EditText)findViewById(0x102029c);
    private boolean mResuming;
    private final KeyguardStatusViewManager mStatusViewManager;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private final boolean mUseSystemIME = true;

    public PasswordUnlockScreen(Context context, Configuration configuration, LockPatternUtils lockpatternutils, KeyguardUpdateMonitor keyguardupdatemonitor, KeyguardScreenCallback keyguardscreencallback)
    {
        super(context);
        mCreationHardKeyboardHidden = configuration.hardKeyboardHidden;
        mCreationOrientation = configuration.orientation;
        mUpdateMonitor = keyguardupdatemonitor;
        mCallback = keyguardscreencallback;
        mLockPatternUtils = lockpatternutils;
        Object obj = LayoutInflater.from(context);
        final InputMethodManager imm = context.getResources();
        boolean flag1;
        if (!SystemProperties.getBoolean("lockscreen.rot_override", false) && !imm.getBoolean(0x111001c))
            flag1 = false;
        else
            flag1 = true;
        if (!flag1)
        {
            Log.i("PasswordUnlockScreen", "we will initialize the password gemini portrait layout");
            ((LayoutInflater) (obj)).inflate(0x1090051, this, true);
        } else
        if (mCreationOrientation == 2)
        {
            Log.i("PasswordUnlockScreen", "we will initialize the password gemini land layout");
            ((LayoutInflater) (obj)).inflate(0x109004f, this, true);
        } else
        {
            Log.i("PasswordUnlockScreen", "we will initialize the password gemini portrait layout");
            ((LayoutInflater) (obj)).inflate(0x1090051, this, true);
        }
        mStatusViewManager = new KeyguardStatusViewManager(this, mUpdateMonitor, mLockPatternUtils, mCallback, true);
        int i = lockpatternutils.getKeyguardStoredPasswordQuality();
        if (0x40000 != i && 0x50000 != i && 0x60000 != i)
            i = 0;
        else
            i = 1;
        mIsAlpha = i;
        mPasswordEntry.setOnEditorActionListener(this);
        mKeyboardHelper = new PasswordEntryKeyboardHelper(context, mKeyboardView, this, false);
        i = mKeyboardHelper;
        if (android.provider.Settings.Secure.getInt(mContext.getContentResolver(), "lock_pattern_tactile_feedback_enabled", 0) == 0)
            flag1 = false;
        else
            flag1 = true;
        i.setEnableHaptics(flag1);
        boolean flag = false;
        if (!mIsAlpha)
        {
            mKeyboardHelper.setKeyboardMode(1);
            flag1 = mKeyboardView;
            byte byte0;
            if (mCreationHardKeyboardHidden != 1)
                byte0 = 0;
            else
                byte0 = 4;
            flag1.setVisibility(byte0);
            flag1 = findViewById(0x102029d);
            if (flag1 != null)
            {
                flag1.setVisibility(0);
                flag = true;
                flag1.setOnClickListener(new android.view.View.OnClickListener() {

                    final PasswordUnlockScreen this$0;

                    public void onClick(View view1)
                    {
                        mKeyboardHelper.handleBackspace();
                    }

            
            {
                this$0 = PasswordUnlockScreen.this;
                super();
            }
                }
);
            }
        } else
        {
            mKeyboardHelper.setKeyboardMode(0);
            mKeyboardView.setVisibility(8);
        }
        mPasswordEntry.requestFocus();
        if (!mIsAlpha)
        {
            mPasswordEntry.setKeyListener(DigitsKeyListener.getInstance());
            mPasswordEntry.setInputType(18);
        } else
        {
            mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
            mPasswordEntry.setInputType(129);
        }
        mPasswordEntry.setOnClickListener(new android.view.View.OnClickListener() {

            final PasswordUnlockScreen this$0;

            public void onClick(View view1)
            {
                mCallback.pokeWakelock();
            }

            
            {
                this$0 = PasswordUnlockScreen.this;
                super();
            }
        }
);
        mPasswordEntry.addTextChangedListener(new TextWatcher() {

            final PasswordUnlockScreen this$0;

            public void afterTextChanged(Editable editable)
            {
                if (!mResuming)
                    mCallback.pokeWakelock();
            }

            public void beforeTextChanged(CharSequence charsequence, int j, int k, int l)
            {
            }

            public void onTextChanged(CharSequence charsequence, int j, int k, int l)
            {
            }

            
            {
                this$0 = PasswordUnlockScreen.this;
                super();
            }
        }
);
        View view = findViewById(0x102029e);
        flag1 = (InputMethodManager)getContext().getSystemService("input_method");
        if (mIsAlpha && view != null && hasMultipleEnabledIMEsOrSubtypes(flag1, false))
        {
            view.setVisibility(0);
            view.setOnClickListener(new android.view.View.OnClickListener() {

                final PasswordUnlockScreen this$0;
                final InputMethodManager val$imm;

                public void onClick(View view1)
                {
                    mCallback.pokeWakelock();
                    imm.showInputMethodPicker();
                }

            
            {
                this$0 = PasswordUnlockScreen.this;
                imm = inputmethodmanager;
                super();
            }
            }
);
        }
        if (!flag)
        {
            android.view.ViewGroup.LayoutParams layoutparams = mPasswordEntry.getLayoutParams();
            if (layoutparams instanceof android.view.ViewGroup.MarginLayoutParams)
            {
                ((android.view.ViewGroup.MarginLayoutParams)layoutparams).leftMargin = 0;
                mPasswordEntry.setLayoutParams(layoutparams);
            }
        }
        if (mUpdateMonitor.DM_IsLocked())
        {
            Log.i("PasswordUnlockScreen", "passworkunlock dm mode is enable");
            findViewById(0x102029b).setVisibility(8);
            mKeyboardView.setVisibility(8);
        }
    }

    private void handleAttemptLockout(long l)
    {
        mPasswordEntry.setEnabled(false);
        mKeyboardView.setEnabled(false);
        (new CountDownTimer(l - SystemClock.elapsedRealtime(), 1000L) {

            final PasswordUnlockScreen this$0;

            public void onFinish()
            {
                mPasswordEntry.setEnabled(true);
                mKeyboardView.setEnabled(true);
                mStatusViewManager.resetStatusInfo();
            }

            public void onTick(long l1)
            {
                int i = (int)(l1 / 1000L);
                Context context = getContext();
                Object aobj[] = new Object[1];
                aobj[0] = Integer.valueOf(i);
                String s = context.getString(0x1040300, aobj);
                mStatusViewManager.setInstructionText(s);
            }

            
            {
                this$0 = PasswordUnlockScreen.this;
                super(l, l1);
            }
        }
).start();
    }

    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager inputmethodmanager, boolean flag)
    {
        int i = 0;
        boolean flag1 = true;
        Object obj = inputmethodmanager.getEnabledInputMethodList();
        int j = 0;
        obj = ((List) (obj)).iterator();
label0:
        do
        {
            if (!((Iterator) (obj)).hasNext())
            {
                if (j > flag1 || inputmethodmanager.getEnabledInputMethodSubtypeList(null, false).size() > flag1)
                    i = ((flag1) ? 1 : 0);
                flag1 = i;
                break;
            }
            Object obj1 = (InputMethodInfo)((Iterator) (obj)).next();
            if (j > flag1)
                break;
            obj1 = inputmethodmanager.getEnabledInputMethodSubtypeList(((InputMethodInfo) (obj1)), flag1);
            if (!((List) (obj1)).isEmpty())
            {
                int k = 0;
                Iterator iterator = ((List) (obj1)).iterator();
                do
                {
                    if (!iterator.hasNext())
                    {
                        if (((List) (obj1)).size() - k > 0 || flag && k > flag1)
                            j++;
                        continue label0;
                    }
                    if (((InputMethodSubtype)iterator.next()).isAuxiliary())
                        k++;
                } while (true);
            }
            j++;
        } while (true);
        return flag1;
    }

    private boolean verifyPasswordAndUnlock()
    {
        boolean flag = true;
        String s = mPasswordEntry.getText().toString();
        mPasswordEntry.setText("");
        if (!mLockPatternUtils.checkPassword(s))
        {
            if (s.length() <= 3)
            {
                if (s.length() > 0)
                    mStatusViewManager.setInstructionText(mContext.getString(0x10402e4));
            } else
            {
                mCallback.reportFailedUnlockAttempt();
                if (mUpdateMonitor.getFailedAttempts() % 5 == 0)
                    handleAttemptLockout(mLockPatternUtils.setLockoutAttemptDeadline());
                mStatusViewManager.setInstructionText(mContext.getString(0x10402e4));
            }
            flag = false;
        } else
        {
            mCallback.keyguardDone(flag);
            mCallback.reportSuccessfulUnlockAttempt();
            mStatusViewManager.setInstructionText(null);
            KeyStore.getInstance().password(s);
        }
        return flag;
    }

    public void cleanUp()
    {
        mUpdateMonitor.removeCallback(this);
    }

    public boolean needsInput()
    {
        return mIsAlpha;
    }

    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation != mCreationOrientation || configuration.hardKeyboardHidden != mCreationHardKeyboardHidden)
            mCallback.recreateMe(configuration);
    }

    protected void onConfigurationChanged(Configuration configuration)
    {
        super.onConfigurationChanged(configuration);
        if (configuration.orientation != mCreationOrientation || configuration.hardKeyboardHidden != mCreationHardKeyboardHidden)
            mCallback.recreateMe(configuration);
    }

    public boolean onEditorAction(TextView textview, int i, KeyEvent keyevent)
    {
        boolean flag;
        if (i != 0 && i != 6 && i != 5)
        {
            flag = false;
        } else
        {
            if (verifyPasswordAndUnlock())
            {
                flag = (InputMethodManager)getContext().getSystemService("input_method");
                if (flag != null && flag.isActive(textview))
                {
                    Log.i("PasswordUnlockScreen", "onEditorAction, IME is showing, we should hide it");
                    flag.hideSoftInputFromWindow(textview.getWindowToken(), 2);
                }
            }
            flag = true;
        }
        return flag;
    }

    public boolean onKeyDown(int i, KeyEvent keyevent)
    {
        mCallback.pokeWakelock();
        return false;
    }

    public void onKeyboardChange(boolean flag)
    {
        byte byte0 = 0;
        boolean flag1;
        if (!flag && !mUpdateMonitor.DM_IsLocked())
            flag1 = false;
        else
            flag1 = true;
        PasswordEntryKeyboardView passwordentrykeyboardview = mKeyboardView;
        if (flag1)
            byte0 = 4;
        passwordentrykeyboardview.setVisibility(byte0);
    }

    public void onPause()
    {
        mStatusViewManager.onPause();
    }

    protected boolean onRequestFocusInDescendants(int i, Rect rect)
    {
        return mPasswordEntry.requestFocus(i, rect);
    }

    public void onResume()
    {
        mResuming = true;
        mStatusViewManager.onResume();
        mPasswordEntry.setText("");
        mPasswordEntry.requestFocus();
        long l = mLockPatternUtils.getLockoutAttemptDeadline();
        if (l != 0L)
            handleAttemptLockout(l);
        mResuming = false;
    }






}
