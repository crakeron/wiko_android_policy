// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.content.Context;
import android.util.Log;
import android.view.*;
import com.android.internal.policy.IPolicy;

// Referenced classes of package com.android.internal.policy.impl:
//            PhoneFallbackEventHandler, PhoneLayoutInflater, PhoneWindow, PhoneWindowManager

public class Policy
    implements IPolicy
{

    private static final String TAG = "PhonePolicy";
    private static final String preload_classes[];


    public FallbackEventHandler makeNewFallbackEventHandler(Context context)
    {
        return new PhoneFallbackEventHandler(context);
    }

    public LayoutInflater makeNewLayoutInflater(Context context)
    {
        return new PhoneLayoutInflater(context);
    }

    public Window makeNewWindow(Context context)
    {
        return new PhoneWindow(context);
    }

    public WindowManagerPolicy makeNewWindowManager()
    {
        return new PhoneWindowManager();
    }

    static 
    {
        String as[] = new String[7];
        as[0] = "com.android.internal.policy.impl.PhoneLayoutInflater";
        as[1] = "com.android.internal.policy.impl.PhoneWindow";
        as[2] = "com.android.internal.policy.impl.PhoneWindow$1";
        as[3] = "com.android.internal.policy.impl.PhoneWindow$ContextMenuCallback";
        as[4] = "com.android.internal.policy.impl.PhoneWindow$DecorView";
        as[5] = "com.android.internal.policy.impl.PhoneWindow$PanelFeatureState";
        as[6] = "com.android.internal.policy.impl.PhoneWindow$PanelFeatureState$SavedState";
        preload_classes = as;
        as = preload_classes;
        int i = as.length;
        int j = 0;
        while (j < i) 
        {
            String s = as[j];
            try
            {
                Class.forName(s);
            }
            catch (ClassNotFoundException _ex)
            {
                Log.e("PhonePolicy", (new StringBuilder()).append("Could not preload class for phone policy: ").append(s).toString());
            }
            j++;
        }
    }
}
