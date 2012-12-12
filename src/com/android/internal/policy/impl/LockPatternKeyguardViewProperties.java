// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardViewProperties, KeyguardUpdateMonitor, LockPatternKeyguardView, KeyguardWindowController, 
//            KeyguardViewBase

public class LockPatternKeyguardViewProperties
    implements KeyguardViewProperties
{

    private final LockPatternUtils mLockPatternUtils;
    private final KeyguardUpdateMonitor mUpdateMonitor;

    public LockPatternKeyguardViewProperties(LockPatternUtils lockpatternutils, KeyguardUpdateMonitor keyguardupdatemonitor)
    {
        mLockPatternUtils = lockpatternutils;
        mUpdateMonitor = keyguardupdatemonitor;
    }

    private boolean isSimPinSecure()
    {
        boolean flag = false;
        com.android.internal.telephony.IccCard.State state = mUpdateMonitor.getSimState(0);
        com.android.internal.telephony.IccCard.State state1 = mUpdateMonitor.getSimState(1);
        if (state == com.android.internal.telephony.IccCard.State.PIN_REQUIRED || state == com.android.internal.telephony.IccCard.State.PUK_REQUIRED || state == com.android.internal.telephony.IccCard.State.PERM_DISABLED || state1 == com.android.internal.telephony.IccCard.State.PIN_REQUIRED || state1 == com.android.internal.telephony.IccCard.State.PUK_REQUIRED || state1 == com.android.internal.telephony.IccCard.State.PERM_DISABLED || state == com.android.internal.telephony.IccCard.State.ABSENT && state1 == com.android.internal.telephony.IccCard.State.ABSENT)
            flag = true;
        return flag;
    }

    public KeyguardViewBase createKeyguardView(Context context, KeyguardUpdateMonitor keyguardupdatemonitor, KeyguardWindowController keyguardwindowcontroller)
    {
        return new LockPatternKeyguardView(context, keyguardupdatemonitor, mLockPatternUtils, keyguardwindowcontroller);
    }

    public boolean isSecure()
    {
        boolean flag;
        if (!mLockPatternUtils.isSecure() && !isSimPinSecure())
            flag = false;
        else
            flag = true;
        return flag;
    }
}
