// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.content.Context;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardUpdateMonitor, KeyguardWindowController, KeyguardViewBase

public interface KeyguardViewProperties
{

    public abstract KeyguardViewBase createKeyguardView(Context context, KeyguardUpdateMonitor keyguardupdatemonitor, KeyguardWindowController keyguardwindowcontroller);

    public abstract boolean isSecure();
}
