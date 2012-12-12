// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.AlertDialog;
import android.content.*;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.*;
import android.telephony.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.internal.app.ShutdownThread;
import java.util.ArrayList;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardUpdateMonitor

class GlobalActions
    implements android.content.DialogInterface.OnDismissListener, android.content.DialogInterface.OnClickListener
{
    private static class SilentModeAction
        implements Action, android.view.View.OnClickListener
    {

        private final int ITEM_IDS[];
        private final AudioManager mAudioManager;
        private final Handler mHandler;

        private int indexToRingerMode(int i)
        {
            return i;
        }

        private int ringerModeToIndex(int i)
        {
            return i;
        }

        public View create(Context context, View view, ViewGroup viewgroup, LayoutInflater layoutinflater)
        {
            View view1 = layoutinflater.inflate(0x1090041, viewgroup, false);
            int j = ringerModeToIndex(mAudioManager.getRingerMode());
            int i = 0;
            do
            {
                if (i >= 3)
                    return view1;
                View view2 = view1.findViewById(ITEM_IDS[i]);
                boolean flag;
                if (j != i)
                    flag = false;
                else
                    flag = true;
                view2.setSelected(flag);
                view2.setTag(Integer.valueOf(i));
                view2.setOnClickListener(this);
                i++;
            } while (true);
        }

        public boolean isEnabled()
        {
            return true;
        }

        public void onClick(View view)
        {
            if (view.getTag() instanceof Integer)
            {
                int i = ((Integer)view.getTag()).intValue();
                mAudioManager.setRingerMode(indexToRingerMode(i));
                mHandler.sendEmptyMessageDelayed(0, 300L);
            }
        }

        public void onPress()
        {
        }

        public boolean showBeforeProvisioning()
        {
            return false;
        }

        public boolean showDuringKeyguard()
        {
            return true;
        }

        void willCreate()
        {
        }

        SilentModeAction(AudioManager audiomanager, Handler handler)
        {
            int ai[] = new int[3];
            ai[0] = 0x1020265;
            ai[1] = 0x1020266;
            ai[2] = 0x1020267;
            ITEM_IDS = ai;
            mAudioManager = audiomanager;
            mHandler = handler;
        }
    }

    private static abstract class ToggleAction
        implements Action
    {
        static final class State extends Enum
        {

            private static final State $VALUES[];
            public static final State Off;
            public static final State On;
            public static final State TurningOff;
            public static final State TurningOn;
            private final boolean inTransition;

            public static State valueOf(String s)
            {
                return (State)Enum.valueOf(com/android/internal/policy/impl/GlobalActions$ToggleAction$State, s);
            }

            public static State[] values()
            {
                return (State[])$VALUES.clone();
            }

            public boolean inTransition()
            {
                return inTransition;
            }

            static 
            {
                Off = new State("Off", 0, false);
                TurningOn = new State("TurningOn", 1, true);
                TurningOff = new State("TurningOff", 2, true);
                On = new State("On", 3, false);
                State astate[] = new State[4];
                astate[0] = Off;
                astate[1] = TurningOn;
                astate[2] = TurningOff;
                astate[3] = On;
                $VALUES = astate;
            }

            private State(String s, int i, boolean flag)
            {
                Enum(s, i);
                inTransition = flag;
            }
        }


        protected int mDisabledIconResid;
        protected int mDisabledStatusMessageResId;
        protected int mEnabledIconResId;
        protected int mEnabledStatusMessageResId;
        protected int mMessageResId;
        protected State mState;

        protected void changeStateFromPress(boolean flag)
        {
            State state;
            if (!flag)
                state = State.Off;
            else
                state = State.On;
            mState = state;
        }

        public View create(Context context, View view, ViewGroup viewgroup, LayoutInflater layoutinflater)
        {
            willCreate();
            View view1 = layoutinflater.inflate(0x1090040, viewgroup, false);
            ImageView imageview = (ImageView)view1.findViewById(0x1020006);
            TextView textview1 = (TextView)view1.findViewById(0x102000b);
            TextView textview = (TextView)view1.findViewById(0x1020264);
            boolean flag = isEnabled();
            if (textview1 != null)
            {
                textview1.setText(mMessageResId);
                textview1.setEnabled(flag);
            }
            boolean flag1;
            if (mState != State.On && mState != State.TurningOn)
                flag1 = false;
            else
                flag1 = true;
            if (imageview != null)
            {
                Resources resources = context.getResources();
                int j;
                if (!flag1)
                    j = mDisabledIconResid;
                else
                    j = mEnabledIconResId;
                imageview.setImageDrawable(resources.getDrawable(j));
                imageview.setEnabled(flag);
            }
            if (textview != null)
            {
                int i;
                if (!flag1)
                    i = mDisabledStatusMessageResId;
                else
                    i = mEnabledStatusMessageResId;
                textview.setText(i);
                textview.setVisibility(0);
                textview.setEnabled(flag);
            }
            view1.setEnabled(flag);
            return view1;
        }

        public boolean isEnabled()
        {
            boolean flag;
            if (mState.inTransition())
                flag = false;
            else
                flag = true;
            return flag;
        }

        public final void onPress()
        {
            if (!mState.inTransition())
            {
                boolean flag;
                if (mState == State.On)
                    flag = false;
                else
                    flag = true;
                onToggle(flag);
                changeStateFromPress(flag);
            } else
            {
                Log.w("GlobalActions", "shouldn't be able to toggle when in transition");
            }
        }

        abstract void onToggle(boolean flag);

        public void updateState(State state)
        {
            mState = state;
        }

        void willCreate()
        {
        }

        public ToggleAction(int i, int j, int k, int l, int i1)
        {
            mState = State.Off;
            mEnabledIconResId = i;
            mDisabledIconResid = j;
            mMessageResId = k;
            mEnabledStatusMessageResId = l;
            mDisabledStatusMessageResId = i1;
        }
    }

    private static abstract class SinglePressAction
        implements Action
    {

        private final int mIconResId;
        private final int mMessageResId;

        public View create(Context context, View view, ViewGroup viewgroup, LayoutInflater layoutinflater)
        {
            View view1 = layoutinflater.inflate(0x1090040, viewgroup, false);
            ImageView imageview = (ImageView)view1.findViewById(0x1020006);
            TextView textview = (TextView)view1.findViewById(0x102000b);
            view1.findViewById(0x1020264).setVisibility(8);
            imageview.setImageDrawable(context.getResources().getDrawable(mIconResId));
            textview.setText(mMessageResId);
            return view1;
        }

        public boolean isEnabled()
        {
            return true;
        }

        public abstract void onPress();

        protected SinglePressAction(int i, int j)
        {
            mIconResId = i;
            mMessageResId = j;
        }
    }

    private static interface Action
    {

        public abstract View create(Context context, View view, ViewGroup viewgroup, LayoutInflater layoutinflater);

        public abstract boolean isEnabled();

        public abstract void onPress();

        public abstract boolean showBeforeProvisioning();

        public abstract boolean showDuringKeyguard();
    }

    private class MyAdapter extends BaseAdapter
    {

        final GlobalActions this$0;

        public boolean areAllItemsEnabled()
        {
            return false;
        }

        public int getCount()
        {
            int i = 0;
            int j = 0;
            do
            {
                if (j >= mItems.size())
                    return i;
                Action action = (Action)mItems.get(j);
                if ((!mKeyguardShowing || action.showDuringKeyguard()) && (mDeviceProvisioned || action.showBeforeProvisioning()))
                    i++;
                j++;
            } while (true);
        }

        public Action getItem(int i)
        {
            int k = 0;
            int j = 0;
            Action action;
            do
            {
                if (j >= mItems.size())
                    throw new IllegalArgumentException((new StringBuilder()).append("position ").append(i).append(" out of range of showable actions").append(", filtered count=").append(getCount()).append(", keyguardshowing=").append(mKeyguardShowing).append(", provisioned=").append(mDeviceProvisioned).toString());
                action = (Action)mItems.get(j);
                if ((!mKeyguardShowing || action.showDuringKeyguard()) && (mDeviceProvisioned || action.showBeforeProvisioning()))
                {
                    if (k == i)
                        break;
                    k++;
                }
                j++;
            } while (true);
            return action;
        }

        public volatile Object getItem(int i)
        {
            return getItem(i);
        }

        public long getItemId(int i)
        {
            return (long)i;
        }

        public View getView(int i, View view, ViewGroup viewgroup)
        {
            return getItem(i).create(mContext, view, viewgroup, LayoutInflater.from(mContext));
        }

        public boolean isEnabled(int i)
        {
            return getItem(i).isEnabled();
        }

        private MyAdapter()
        {
            this$0 = GlobalActions.this;
            BaseAdapter();
        }

    }


    private static final int DIALOG_DISMISS_DELAY = 300;
    private static final int MESSAGE_DISMISS = 0;
    private static final int MESSAGE_REFRESH = 1;
    private static final boolean SHOW_SILENT_TOGGLE = true;
    private static final String TAG = "GlobalActions";
    private static boolean mIsVisable = false;
    private MyAdapter mAdapter;
    private ToggleAction mAirplaneModeOn;
    private ToggleAction.State mAirplaneState;
    private final AudioManager mAudioManager;
    private BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private boolean mDeviceProvisioned;
    private AlertDialog mDialog;
    private Handler mHandler;
    private boolean mIsWaitingForEcmExit;
    private ArrayList mItems;
    private boolean mKeyguardShowing;
    PhoneStateListener mPhoneStateListener;
    private BroadcastReceiver mRingerModeReceiver;
    private boolean mRingerModeReceiverRegistered;
    private SilentModeAction mSilentModeAction;
    private boolean mThemeChanged;

    public GlobalActions(Context context)
    {
        mKeyguardShowing = false;
        mDeviceProvisioned = false;
        mAirplaneState = ToggleAction.State.Off;
        mIsWaitingForEcmExit = false;
        mThemeChanged = false;
        mBroadcastReceiver = new BroadcastReceiver() {

            final GlobalActions this$0;

            public void onReceive(Context context1, Intent intent)
            {
                String s = intent.getAction();
                if (!"android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(s) && !"android.intent.action.SCREEN_OFF".equals(s))
                {
                    if (!"android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(s))
                    {
                        if ("android.intent.action.SKIN_CHANGED".equals(s))
                            mThemeChanged = true;
                    } else
                    if (!intent.getBooleanExtra("PHONE_IN_ECM_STATE", false) && mIsWaitingForEcmExit)
                    {
                        mIsWaitingForEcmExit = false;
                        changeAirplaneModeSystemSetting(true);
                    }
                } else
                if (!"globalactions".equals(intent.getStringExtra("reason")))
                    mHandler.sendEmptyMessage(0);
            }

            
            {
                this$0 = GlobalActions.this;
                BroadcastReceiver();
            }
        }
;
        mPhoneStateListener = new PhoneStateListener() {

            final GlobalActions this$0;

            public void onServiceStateChanged(ServiceState servicestate)
            {
                boolean flag = false;
                if (android.provider.Settings.System.getInt(mContext.getContentResolver(), "airplane_mode_on", 0) != 0)
                    flag = true;
                Log.v("GlobalActions", (new StringBuilder()).append("Phone State = ").append(servicestate.getState()).append(" gemini = ").append(true).append(" inAirplaneMode ").append(flag).toString());
                GlobalActions globalactions = GlobalActions.this;
                ToggleAction.State state;
                if (!flag)
                    state = ToggleAction.State.Off;
                else
                    state = ToggleAction.State.On;
                globalactions.mAirplaneState = state;
                mAirplaneModeOn.updateState(mAirplaneState);
                mAdapter.notifyDataSetChanged();
            }

            
            {
                this$0 = GlobalActions.this;
                PhoneStateListener();
            }
        }
;
        mRingerModeReceiver = new BroadcastReceiver() {

            final GlobalActions this$0;

            public void onReceive(Context context1, Intent intent)
            {
                if (intent.getAction().equals("android.media.RINGER_MODE_CHANGED"))
                    mHandler.sendEmptyMessage(1);
            }

            
            {
                this$0 = GlobalActions.this;
                BroadcastReceiver();
            }
        }
;
        mRingerModeReceiverRegistered = false;
        mHandler = new Handler() {

            final GlobalActions this$0;

            public void handleMessage(Message message)
            {
                if (message.what != 0)
                {
                    if (message.what == 1)
                        mAdapter.notifyDataSetChanged();
                } else
                if (mDialog != null)
                    mDialog.dismiss();
            }

            
            {
                this$0 = GlobalActions.this;
                Handler();
            }
        }
;
        mContext = context;
        mAudioManager = (AudioManager)mContext.getSystemService("audio");
        Object obj = new IntentFilter();
        ((IntentFilter) (obj)).addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        ((IntentFilter) (obj)).addAction("android.intent.action.SCREEN_OFF");
        ((IntentFilter) (obj)).addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        context.registerReceiver(mBroadcastReceiver, ((IntentFilter) (obj)));
        obj = (TelephonyManager)context.getSystemService("phone");
        ((TelephonyManager) (obj)).listenGemini(mPhoneStateListener, 1, 0);
        ((TelephonyManager) (obj)).listenGemini(mPhoneStateListener, 1, 1);
    }

    private void changeAirplaneModeSystemSetting(boolean flag)
    {
        Object obj = mContext.getContentResolver();
        int i;
        if (!flag)
            i = 0;
        else
            i = 1;
        android.provider.Settings.System.putInt(((android.content.ContentResolver) (obj)), "airplane_mode_on", i);
        obj = new Intent("android.intent.action.AIRPLANE_MODE");
        ((Intent) (obj)).addFlags(0x20000000);
        ((Intent) (obj)).putExtra("state", flag);
        mContext.sendBroadcast(((Intent) (obj)));
        Log.v("GlobalActions", (new StringBuilder()).append("Enter the airplane mode ").append(flag).toString());
    }

    private AlertDialog createDialog()
    {
        mSilentModeAction = new SilentModeAction(mAudioManager, mHandler);
        mAirplaneModeOn = new ToggleAction(0x10802ce, 0x10802cf, 0x1040134, 0x1040135, 0x1040136) {

            final GlobalActions this$0;

            protected void changeStateFromPress(boolean flag)
            {
                if (!Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode")))
                {
                    ToggleAction.State state;
                    if (!flag)
                        state = ToggleAction.State.TurningOff;
                    else
                        state = ToggleAction.State.TurningOn;
                    mState = state;
                    mAirplaneState = mState;
                }
            }

            public boolean isEnabled()
            {
                boolean flag;
                if (KeyguardUpdateMonitor.dual_sim_setting != 0)
                {
                    flag = super.isEnabled();
                } else
                {
                    Log.e("GlobalActions", "if user unselect the dual sim mode setting on phone starting, the airplane mode can not be set.");
                    flag = false;
                }
                return flag;
            }

            void onToggle(boolean flag)
            {
                if (!Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode")))
                {
                    changeAirplaneModeSystemSetting(flag);
                } else
                {
                    mIsWaitingForEcmExit = true;
                    Intent intent = new Intent("android.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null);
                    intent.addFlags(0x10000000);
                    mContext.startActivity(intent);
                }
            }

            public boolean showBeforeProvisioning()
            {
                return false;
            }

            public boolean showDuringKeyguard()
            {
                return true;
            }

            
            {
                this$0 = GlobalActions.this;
                ToggleAction(i, j, k, l, i1);
            }
        }
;
        mItems = new ArrayList();
        mItems.add(new SinglePressAction(0x1080030, 0x104012f) {

            final GlobalActions this$0;

            public void onPress()
            {
                ShutdownThread.shutdown(mContext, true);
            }

            public boolean showBeforeProvisioning()
            {
                return true;
            }

            public boolean showDuringKeyguard()
            {
                return true;
            }

            
            {
                this$0 = GlobalActions.this;
                SinglePressAction(i, j);
            }
        }
);
        mItems.add(new SinglePressAction(0x10802d0, 0x1040130) {

            final GlobalActions this$0;

            public void onPress()
            {
                ShutdownThread.reboot(mContext, null, true);
            }

            public boolean showBeforeProvisioning()
            {
                return true;
            }

            public boolean showDuringKeyguard()
            {
                return true;
            }

            
            {
                this$0 = GlobalActions.this;
                SinglePressAction(i, j);
            }
        }
);
        mItems.add(mAirplaneModeOn);
        mItems.add(mSilentModeAction);
        mAdapter = new MyAdapter();
        Object obj = new android.app.AlertDialog.Builder(mContext);
        ((android.app.AlertDialog.Builder) (obj)).setAdapter(mAdapter, this).setInverseBackgroundForced(true);
        obj = ((android.app.AlertDialog.Builder) (obj)).create();
        ((AlertDialog) (obj)).getListView().setItemsCanFocus(true);
        ((AlertDialog) (obj)).getWindow().setType(2008);
        ((AlertDialog) (obj)).setOnDismissListener(this);
        return ((AlertDialog) (obj));
    }

    public static boolean isVisable()
    {
        return mIsVisable;
    }

    private void prepareDialog()
    {
        if (mAudioManager.getRingerMode() != 2);
        mAirplaneModeOn.updateState(mAirplaneState);
        mAdapter.notifyDataSetChanged();
        if (!mKeyguardShowing)
            mDialog.getWindow().setType(2008);
        else
            mDialog.getWindow().setType(2009);
        Log.d("GlobalActions", "prepareDialog -- registerReceiver mRingerModeReceiver");
        IntentFilter intentfilter = new IntentFilter("android.media.RINGER_MODE_CHANGED");
        mContext.registerReceiver(mRingerModeReceiver, intentfilter);
        mRingerModeReceiverRegistered = true;
    }

    public void onClick(DialogInterface dialoginterface, int i)
    {
        if (!(mAdapter.getItem(i) instanceof SilentModeAction))
            dialoginterface.dismiss();
        mAdapter.getItem(i).onPress();
    }

    public void onDismiss(DialogInterface dialoginterface)
    {
        if (true & mRingerModeReceiverRegistered)
        {
            Log.d("GlobalActions", "onDismiss -- unregisterReceiver mRingerModeReceiver");
            mContext.unregisterReceiver(mRingerModeReceiver);
            mRingerModeReceiverRegistered = false;
        }
        mIsVisable = false;
    }

    public void showDialog(boolean flag, boolean flag1)
    {
        if (!ShutdownThread.isPowerOffDialogShowing())
        {
            if (mDialog != null && mKeyguardShowing != flag)
            {
                mDialog.dismiss();
                mDialog = null;
            }
            mKeyguardShowing = flag;
            mDeviceProvisioned = flag1;
            if (mDialog == null || mThemeChanged)
            {
                mDialog = createDialog();
                mThemeChanged = false;
            }
            prepareDialog();
            mDialog.show();
            mDialog.getWindow().getDecorView().setSystemUiVisibility(0x10000);
        }
    }




/*
    static boolean access$002(GlobalActions globalactions, boolean flag)
    {
        globalactions.mIsWaitingForEcmExit = flag;
        return flag;
    }

*/








/*
    static ToggleAction.State access$302(GlobalActions globalactions, ToggleAction.State state)
    {
        globalactions.mAirplaneState = state;
        return state;
    }

*/






/*
    static boolean access$902(GlobalActions globalactions, boolean flag)
    {
        globalactions.mThemeChanged = flag;
        return flag;
    }

*/
}
