// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import java.util.List;

// Referenced classes of package com.android.internal.policy.impl:
//            IconUtilities

public class RecentApplicationsDialog extends Dialog
    implements android.view.View.OnClickListener
{
    class RecentTag
    {

        android.app.ActivityManager.RecentTaskInfo info;
        Intent intent;
        final RecentApplicationsDialog this$0;

        RecentTag()
        {
            this$0 = RecentApplicationsDialog.this;
            super();
        }
    }


    private static final boolean DBG_FORCE_EMPTY_LIST = false;
    private static final int MAX_RECENT_TASKS = 16;
    private static final int NUM_BUTTONS = 8;
    private static StatusBarManager sStatusBar;
    IntentFilter mBroadcastIntentFilter;
    private BroadcastReceiver mBroadcastReceiver;
    Runnable mCleanup;
    Handler mHandler;
    final TextView mIcons[] = new TextView[8];
    View mNoAppsText;

    public RecentApplicationsDialog(Context context)
    {
        super(context, 0x10302f8);
        mBroadcastIntentFilter = new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        mHandler = new Handler();
        mCleanup = new Runnable() {

            final RecentApplicationsDialog this$0;

            public void run()
            {
                TextView atextview[] = mIcons;
                int i = atextview.length;
                int j = 0;
                do
                {
                    if (j >= i)
                        return;
                    TextView textview = atextview[j];
                    textview.setCompoundDrawables(null, null, null, null);
                    textview.setTag(null);
                    j++;
                } while (true);
            }

            
            {
                this$0 = RecentApplicationsDialog.this;
                super();
            }
        }
;
        mBroadcastReceiver = new BroadcastReceiver() {

            final RecentApplicationsDialog this$0;

            public void onReceive(Context context1, Intent intent)
            {
                if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction()) && !"recentapps".equals(intent.getStringExtra("reason")))
                    dismiss();
            }

            
            {
                this$0 = RecentApplicationsDialog.this;
                super();
            }
        }
;
    }

    private void reloadButtons()
    {
        Context context = getContext();
        PackageManager packagemanager = context.getPackageManager();
        List list = ((ActivityManager)context.getSystemService("activity")).getRecentTasks(16, 2);
        ActivityInfo activityinfo = (new Intent("android.intent.action.MAIN")).addCategory("android.intent.category.HOME").resolveActivityInfo(packagemanager, 0);
        IconUtilities iconutilities = new IconUtilities(getContext());
        int i = 0;
        int k = list.size();
        int j = 0;
        do
        {
            if (j >= k || i >= 8)
            {
                View view = mNoAppsText;
                if (i != 0)
                    packagemanager = 8;
                else
                    packagemanager = 0;
                view.setVisibility(packagemanager);
                do
                {
                    if (i >= 8)
                        return;
                    mIcons[i].setVisibility(8);
                    i++;
                } while (true);
            }
            android.app.ActivityManager.RecentTaskInfo recenttaskinfo = (android.app.ActivityManager.RecentTaskInfo)list.get(j);
            Intent intent = new Intent(recenttaskinfo.baseIntent);
            if (recenttaskinfo.origActivity != null)
                intent.setComponent(recenttaskinfo.origActivity);
            if (activityinfo == null || !activityinfo.packageName.equals(intent.getComponent().getPackageName()) || !activityinfo.name.equals(intent.getComponent().getClassName()))
            {
                intent.setFlags(0x10000000 | 0xffdfffff & intent.getFlags());
                Object obj = packagemanager.resolveActivity(intent, 0);
                if (obj != null)
                {
                    ActivityInfo activityinfo1 = ((ResolveInfo) (obj)).activityInfo;
                    obj = activityinfo1.loadLabel(packagemanager).toString();
                    android.graphics.drawable.Drawable drawable = activityinfo1.loadIcon(packagemanager);
                    if (obj != null && ((String) (obj)).length() > 0 && drawable != null)
                    {
                        TextView textview = mIcons[i];
                        textview.setText(((CharSequence) (obj)));
                        textview.setCompoundDrawables(null, iconutilities.createIconDrawable(drawable), null, null);
                        obj = new RecentTag();
                        obj.info = recenttaskinfo;
                        obj.intent = intent;
                        textview.setTag(obj);
                        textview.setVisibility(0);
                        textview.setPressed(false);
                        textview.clearFocus();
                        i++;
                    }
                }
            }
            j++;
        } while (true);
    }

    private void switchTo(RecentTag recenttag)
    {
        if (recenttag.info.id < 0) goto _L2; else goto _L1
_L1:
        ((ActivityManager)getContext().getSystemService("activity")).moveTaskToFront(recenttag.info.id, 1);
_L4:
        return;
_L2:
        if (recenttag.intent != null)
        {
            recenttag.intent.addFlags(0x104000);
            try
            {
                getContext().startActivity(recenttag.intent);
            }
            catch (ActivityNotFoundException activitynotfoundexception)
            {
                Log.w("Recent", "Unable to launch recent task", activitynotfoundexception);
            }
        }
        if (true) goto _L4; else goto _L3
_L3:
    }

    public void dismissAndSwitch()
    {
        int j = mIcons.length;
        RecentTag recenttag = null;
        int i = 0;
        do
        {
label0:
            {
                if (i < j && mIcons[i].getVisibility() == 0)
                {
                    if (i != 0 && !mIcons[i].hasFocus())
                        break label0;
                    recenttag = (RecentTag)mIcons[i].getTag();
                    if (!mIcons[i].hasFocus())
                        break label0;
                }
                if (recenttag != null)
                    switchTo(recenttag);
                dismiss();
                return;
            }
            i++;
        } while (true);
    }

    public void onClick(View view)
    {
label0:
        {
            TextView atextview[] = mIcons;
            int j = atextview.length;
            int i = 0;
            TextView textview;
            do
            {
                if (i >= j)
                    break label0;
                textview = atextview[i];
                if (textview == view)
                    break;
                i++;
            } while (true);
            switchTo((RecentTag)textview.getTag());
        }
        dismiss();
    }

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        Object obj = getContext();
        if (sStatusBar == null)
            sStatusBar = (StatusBarManager)((Context) (obj)).getSystemService("statusbar");
        Window window = getWindow();
        window.requestFeature(1);
        window.setType(2008);
        window.setFlags(0x20000, 0x20000);
        window.setTitle("Recents");
        setContentView(0x1090082);
        obj = window.getAttributes();
        obj.width = -1;
        obj.height = -1;
        window.setAttributes(((android.view.WindowManager.LayoutParams) (obj)));
        window.setFlags(0, 2);
        mIcons[0] = (TextView)findViewById(0x10202e3);
        mIcons[1] = (TextView)findViewById(0x1020019);
        mIcons[2] = (TextView)findViewById(0x102001a);
        mIcons[3] = (TextView)findViewById(0x102001b);
        mIcons[4] = (TextView)findViewById(0x10202e4);
        mIcons[5] = (TextView)findViewById(0x10202e5);
        mIcons[6] = (TextView)findViewById(0x10202e6);
        mIcons[7] = (TextView)findViewById(0x10202e7);
        mNoAppsText = findViewById(0x10202e2);
        TextView atextview[] = mIcons;
        int j = atextview.length;
        int i = 0;
        do
        {
            if (i >= j)
                return;
            atextview[i].setOnClickListener(this);
            i++;
        } while (true);
    }

    public boolean onKeyDown(int i, KeyEvent keyevent)
    {
        boolean flag = true;
        if (i == 61) goto _L2; else goto _L1
_L1:
        flag = super.onKeyDown(i, keyevent);
          goto _L3
_L2:
        boolean flag1;
        int j;
        int k;
        flag1 = keyevent.isShiftPressed();
        k = mIcons.length;
        j = 0;
_L7:
        if (j < k && mIcons[j].getVisibility() == 0) goto _L5; else goto _L4
_L4:
        if (j != 0)
        {
            int l;
label0:
            {
                if (!flag1)
                    l = 0;
                else
                    l = j - 1;
                int i1 = 0;
                do
                {
                    if (i1 >= j)
                        break label0;
                    if (mIcons[i1].hasFocus())
                        break;
                    i1++;
                } while (true);
                if (!flag1)
                    l = (i1 + 1) % j;
                else
                    l = (-1 + (i1 + j)) % j;
            }
            byte byte0;
            if (!flag1)
                byte0 = 2;
            else
                byte0 = flag;
            if (mIcons[l].requestFocus(byte0))
                mIcons[l].playSoundEffect(SoundEffectConstants.getContantForFocusDirection(byte0));
        }
_L3:
        return flag;
_L5:
        j++;
        if (true) goto _L7; else goto _L6
_L6:
    }

    public void onStart()
    {
        super.onStart();
        reloadButtons();
        if (sStatusBar != null)
            sStatusBar.disable(0x10000);
        getContext().registerReceiver(mBroadcastReceiver, mBroadcastIntentFilter);
        mHandler.removeCallbacks(mCleanup);
    }

    public void onStop()
    {
        super.onStop();
        if (sStatusBar != null)
            sStatusBar.disable(0);
        getContext().unregisterReceiver(mBroadcastReceiver);
        mHandler.postDelayed(mCleanup, 100L);
    }
}
