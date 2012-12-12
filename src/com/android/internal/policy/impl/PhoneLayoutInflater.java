// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

public class PhoneLayoutInflater extends LayoutInflater
{

    private static final String sClassPrefixList[];

    public PhoneLayoutInflater(Context context)
    {
        super(context);
    }

    protected PhoneLayoutInflater(LayoutInflater layoutinflater, Context context)
    {
        super(layoutinflater, context);
    }

    public LayoutInflater cloneInContext(Context context)
    {
        return new PhoneLayoutInflater(this, context);
    }

    protected View onCreateView(String s, AttributeSet attributeset)
        throws ClassNotFoundException
    {
        int i;
        String as[];
        int j;
        as = sClassPrefixList;
        i = as.length;
        j = 0;
_L3:
        if (j >= i) goto _L2; else goto _L1
_L1:
        Object obj = as[j];
        obj = createView(s, ((String) (obj)), attributeset);
        obj = obj;
        if (obj == null)
            continue; /* Loop/switch isn't completed */
_L4:
        return ((View) (obj));
        JVM INSTR pop ;
        j++;
          goto _L3
_L2:
        obj = super.onCreateView(s, attributeset);
          goto _L4
    }

    static 
    {
        String as[] = new String[3];
        as[0] = "android.widget.";
        as[1] = "android.webkit.";
        as[2] = "com.mediatek.";
        sClassPrefixList = as;
    }
}
