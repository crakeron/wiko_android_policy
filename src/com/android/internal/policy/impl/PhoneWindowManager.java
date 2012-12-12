// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.*;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.IAudioService;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.android.internal.app.ShutdownThread;
import com.android.internal.policy.PolicyManager;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.telephony.ITelephony;
import com.android.internal.view.BaseInputHandler;
import com.android.internal.widget.PointerLocationView;
import java.io.*;
import java.util.ArrayList;

// Referenced classes of package com.android.internal.policy.impl:
//            KeyguardViewMediator, ShortcutManager, GlobalActions, RecentApplicationsDialog

public class PhoneWindowManager
    implements WindowManagerPolicy
{
    class PassHeadsetKey
        implements Runnable
    {

        KeyEvent mKeyEvent;
        final PhoneWindowManager this$0;

        public void run()
        {
            if (ActivityManagerNative.isSystemReady())
            {
                Intent intent = new Intent("android.intent.action.MEDIA_BUTTON", null);
                intent.putExtra("android.intent.extra.KEY_EVENT", mKeyEvent);
                mContext.sendOrderedBroadcast(intent, null, mBroadcastDone, mHandler, -1, null, null);
            }
        }

        PassHeadsetKey(KeyEvent keyevent)
        {
            this$0 = PhoneWindowManager.this;
            super();
            mKeyEvent = keyevent;
        }
    }

    class MyOrientationListener extends WindowOrientationListener
    {

        final PhoneWindowManager this$0;

        public void onProposedRotationChanged(int i)
        {
            if (PhoneWindowManager.localLOGV)
                Log.v("WindowManager", (new StringBuilder()).append("onProposedRotationChanged, rotation=").append(i).toString());
            updateRotation(false);
        }

        MyOrientationListener(Context context)
        {
            this$0 = PhoneWindowManager.this;
            super(context);
        }
    }

    class SettingsObserver extends ContentObserver
    {

        final PhoneWindowManager this$0;

        void observe()
        {
            ContentResolver contentresolver = mContext.getContentResolver();
            contentresolver.registerContentObserver(android.provider.Settings.System.getUriFor("end_button_behavior"), false, this);
            contentresolver.registerContentObserver(android.provider.Settings.Secure.getUriFor("incall_power_button_behavior"), false, this);
            contentresolver.registerContentObserver(android.provider.Settings.System.getUriFor("accelerometer_rotation"), false, this);
            contentresolver.registerContentObserver(android.provider.Settings.System.getUriFor("user_rotation"), false, this);
            contentresolver.registerContentObserver(android.provider.Settings.System.getUriFor("screen_off_timeout"), false, this);
            contentresolver.registerContentObserver(android.provider.Settings.System.getUriFor("window_orientation_listener_log"), false, this);
            contentresolver.registerContentObserver(android.provider.Settings.System.getUriFor("pointer_location"), false, this);
            contentresolver.registerContentObserver(android.provider.Settings.Secure.getUriFor("default_input_method"), false, this);
            contentresolver.registerContentObserver(android.provider.Settings.System.getUriFor("fancy_rotation_anim"), false, this);
            updateSettings();
        }

        public void onChange(boolean flag)
        {
            updateSettings();
            updateRotation(false);
        }

        SettingsObserver(Handler handler)
        {
            this$0 = PhoneWindowManager.this;
            super(handler);
        }
    }


    static final int APPLICATION_LAYER = 2;
    static final int APPLICATION_MEDIA_OVERLAY_SUBLAYER = -1;
    static final int APPLICATION_MEDIA_SUBLAYER = -2;
    static final int APPLICATION_PANEL_SUBLAYER = 1;
    static final int APPLICATION_SUB_PANEL_SUBLAYER = 2;
    static final int BOOT_PROGRESS_LAYER = 22;
    private static final int BTN_MOUSE = 272;
    static boolean DEBUG = false;
    static boolean DEBUG_FALLBACK = false;
    static boolean DEBUG_INPUT = false;
    static boolean DEBUG_LAYOUT = false;
    static boolean DEBUG_ORIENTATION = false;
    static final int DEFAULT_ACCELEROMETER_ROTATION = 0;
    static final int DRAG_LAYER = 20;
    static final boolean ENABLE_CAR_DOCK_HOME_CAPTURE = true;
    static final boolean ENABLE_DESK_DOCK_HOME_CAPTURE = false;
    static final int HIDDEN_NAV_CONSUMER_LAYER = 24;
    static final int INPUT_METHOD_DIALOG_LAYER = 10;
    static final int INPUT_METHOD_LAYER = 9;
    public static final String IPO_DISABLE = "android.intent.action.ACTION_BOOT_IPO";
    public static final String IPO_ENABLE = "android.intent.action.ACTION_SHUTDOWN_IPO";
    static final int KEYGUARD_DIALOG_LAYER = 12;
    static final int KEYGUARD_LAYER = 11;
    static final int KEY_DISPATCH_MODE_ALL_DISABLE = 1;
    static final int KEY_DISPATCH_MODE_ALL_ENABLE = 0;
    static final int KEY_DISPATCH_MODE_HOME_DISABLE = 2;
    static final int KEY_DISPATCH_MODE_POWER_DISABLE = 3;
    private static final int LID_ABSENT = -1;
    private static final int LID_CLOSED = 0;
    private static final int LID_OPEN = 1;
    static final int LONG_PRESS_HOME_NOTHING = 0;
    static final int LONG_PRESS_HOME_RECENT_DIALOG = 1;
    static final int LONG_PRESS_HOME_RECENT_SYSTEM_UI = 2;
    static final int LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
    static final int LONG_PRESS_POWER_NOTHING = 0;
    static final int LONG_PRESS_POWER_SHUT_OFF = 2;
    static final int NAVIGATION_BAR_LAYER = 18;
    static final int PHONE_LAYER = 3;
    static final int POINTER_LAYER = 23;
    public static final String POWERKEY_DISABLE = "android.intent.action.DISABLE_POWER_KEY";
    static final boolean PRINT_ANIM = false;
    static final int PRIORITY_PHONE_LAYER = 7;
    static final int RECENT_APPS_BEHAVIOR_DISMISS_AND_SWITCH = 2;
    static final int RECENT_APPS_BEHAVIOR_EXIT_TOUCH_MODE_AND_SHOW = 1;
    static final int RECENT_APPS_BEHAVIOR_SHOW_OR_DISMISS = 0;
    private static final long SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS = 150L;
    static final int SEARCH_BAR_LAYER = 4;
    static final int SECURE_SYSTEM_OVERLAY_LAYER = 21;
    static final boolean SHOW_PROCESSES_ON_ALT_MENU = false;
    static final boolean SHOW_STARTING_ANIMATIONS = true;
    static final int STATUS_BAR_LAYER = 14;
    static final int STATUS_BAR_PANEL_LAYER = 15;
    static final int STATUS_BAR_SUB_PANEL_LAYER = 13;
    public static final String STK_USERACTIVITY = "android.intent.action.stk.USER_ACTIVITY";
    public static final String STK_USERACTIVITY_ENABLE = "android.intent.action.stk.USER_ACTIVITY.enable";
    private static final int SW_LID = 0;
    static final int SYSTEM_ALERT_LAYER = 8;
    static final int SYSTEM_DIALOG_LAYER = 5;
    public static final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    public static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    public static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    public static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    static final int SYSTEM_ERROR_LAYER = 19;
    static final int SYSTEM_OVERLAY_LAYER = 17;
    static final String TAG = "WindowManager";
    static final int TOAST_LAYER = 6;
    static final int TOP_MOST_LAYER = 25;
    static final int VOLUME_OVERLAY_LAYER = 16;
    static final int WALLPAPER_LAYER = 2;
    private static final int WINDOW_TYPES_WHERE_HOME_DOESNT_WORK[];
    static boolean localLOGV = false;
    static final Rect mTmpContentFrame = new Rect();
    static final Rect mTmpDisplayFrame = new Rect();
    static final Rect mTmpNavigationFrame = new Rect();
    static final Rect mTmpParentFrame = new Rect();
    static final Rect mTmpVisibleFrame = new Rect();
    static SparseArray sApplicationLaunchKeyCategories;
    int mAccelerometerDefault;
    int mAllowAllRotations;
    boolean mAllowLockscreenWhenOn;
    final Runnable mAllowSystemUiDelay = new Runnable() {

        final PhoneWindowManager this$0;

        public void run()
        {
        }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
    }
;
    boolean mAppLaunchTimeEnabled;
    ProgressDialog mBootMsgDialog;
    BroadcastReceiver mBroadcastDone;
    android.os.PowerManager.WakeLock mBroadcastWakeLock;
    boolean mCarDockEnablesAccelerometer;
    Intent mCarDockIntent;
    int mCarDockRotation;
    boolean mConsumeShortcutKeyUp;
    int mContentBottom;
    int mContentLeft;
    int mContentRight;
    int mContentTop;
    Context mContext;
    int mCurBottom;
    int mCurLeft;
    int mCurRight;
    int mCurTop;
    int mCurrentAppOrientation;
    boolean mDeskDockEnablesAccelerometer;
    Intent mDeskDockIntent;
    int mDeskDockRotation;
    boolean mDismissKeyguard;
    int mDockBottom;
    int mDockLayer;
    int mDockLeft;
    int mDockMode;
    BroadcastReceiver mDockReceiver;
    int mDockRight;
    int mDockTop;
    boolean mEnableShiftMenuBugReports;
    int mEndcallBehavior;
    final android.view.KeyCharacterMap.FallbackAction mFallbackAction = new android.view.KeyCharacterMap.FallbackAction();
    IApplicationToken mFocusedApp;
    android.view.WindowManagerPolicy.WindowState mFocusedWindow;
    int mForceClearedSystemUiFlags;
    boolean mForceStatusBar;
    GlobalActions mGlobalActions;
    private UEventObserver mHDMIObserver;
    Handler mHandler;
    boolean mHasNavigationBar;
    boolean mHasSoftInput;
    boolean mHdmiPlugged;
    int mHdmiRotation;
    boolean mHideLockScreen;
    android.view.WindowManagerPolicy.FakeWindow mHideNavFakeWindow;
    final InputHandler mHideNavInputHandler = new BaseInputHandler() {

        final PhoneWindowManager this$0;

        public void handleMotion(MotionEvent motionevent, android.view.InputQueue.FinishedCallback finishedcallback)
        {
            boolean flag;
            if ((2 & motionevent.getSource()) == 0 || motionevent.getAction() != 0)
                break MISSING_BLOCK_LABEL_140;
            flag = false;
            synchronized (mLock)
            {
                int i = 2 | mResettingSystemUiFlags;
                if (mResettingSystemUiFlags != i)
                {
                    mResettingSystemUiFlags = i;
                    flag = true;
                }
                i = 2 | mForceClearedSystemUiFlags;
                if (mForceClearedSystemUiFlags != i)
                {
                    mForceClearedSystemUiFlags = i;
                    flag = true;
                    mHandler.postDelayed(new Runnable() {

                        final _cls8 this$1;

                        public void run()
                        {
                            synchronized (mLock)
                            {
                                PhoneWindowManager phonewindowmanager = _fld0;
                                phonewindowmanager.mForceClearedSystemUiFlags = -3 & phonewindowmanager.mForceClearedSystemUiFlags;
                            }
                            mWindowManagerFuncs.reevaluateStatusBarVisibility();
                            return;
                            exception2;
                            obj1;
                            JVM INSTR monitorexit ;
                            throw exception2;
                        }

                    
                    {
                        this$1 = _cls8.this;
                        super();
                    }
                    }
, 1000L);
                }
            }
            if (!flag)
                break MISSING_BLOCK_LABEL_140;
            mWindowManagerFuncs.reevaluateStatusBarVisibility();
            finishedcallback.finished(false);
            return;
            exception1;
            obj;
            JVM INSTR monitorexit ;
            throw exception1;
            Exception exception;
            exception;
            finishedcallback.finished(false);
            throw exception;
        }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
    }
;
    Intent mHomeIntent;
    boolean mHomePressed;
    int mIncallPowerBehavior;
    boolean mIsStkUserActivityEnabled;
    int mKeyDispatcMode;
    final Object mKeyDispatchLock = new Object();
    BroadcastReceiver mKeyDispatchReceiver;
    long mKeyboardTapVibePattern[];
    android.view.WindowManagerPolicy.WindowState mKeyguard;
    KeyguardViewMediator mKeyguardMediator;
    int mLandscapeRotation;
    boolean mLastFocusNeedsMenu;
    int mLastSystemUiFlags;
    int mLidKeyboardAccessibility;
    int mLidNavigationAccessibility;
    int mLidOpen;
    int mLidOpenRotation;
    final Object mLock = new Object();
    int mLockScreenTimeout;
    boolean mLockScreenTimerActive;
    private int mLongPressOnHomeBehavior;
    int mLongPressOnPowerBehavior;
    long mLongPressVibePattern[];
    android.view.WindowManagerPolicy.WindowState mNavigationBar;
    int mNavigationBarHeight;
    int mNavigationBarWidth;
    MyOrientationListener mOrientationListener;
    boolean mOrientationSensorEnabled;
    boolean mPendingPowerKeyUpCanceled;
    InputChannel mPointerLocationInputChannel;
    private final InputHandler mPointerLocationInputHandler = new BaseInputHandler() {

        final PhoneWindowManager this$0;

        public void handleMotion(MotionEvent motionevent, android.view.InputQueue.FinishedCallback finishedcallback)
        {
            boolean flag = false;
            if ((2 & motionevent.getSource()) != 0)
                synchronized (mLock)
                {
                    if (mPointerLocationView != null)
                    {
                        mPointerLocationView.addPointerEvent(motionevent);
                        flag = true;
                    }
                }
            finishedcallback.finished(flag);
            return;
            exception;
            obj;
            JVM INSTR monitorexit ;
            throw exception;
            Exception exception1;
            exception1;
            finishedcallback.finished(flag);
            throw exception1;
        }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
    }
;
    int mPointerLocationMode;
    PointerLocationView mPointerLocationView;
    int mPortraitRotation;
    volatile boolean mPowerKeyHandled;
    private long mPowerKeyTime;
    private boolean mPowerKeyTriggered;
    private final Runnable mPowerLongPress = new Runnable() {

        final PhoneWindowManager this$0;

        public void run()
        {
            if (mLongPressOnPowerBehavior < 0)
                mLongPressOnPowerBehavior = mContext.getResources().getInteger(0x10e0012);
            switch (mLongPressOnPowerBehavior)
            {
            case 1: // '\001'
                mPowerKeyHandled = true;
                performHapticFeedbackLw(null, 0, false);
                sendCloseSystemWindows("globalactions");
                showGlobalActionsDialog();
                break;

            case 2: // '\002'
                mPowerKeyHandled = true;
                performHapticFeedbackLw(null, 0, false);
                sendCloseSystemWindows("globalactions");
                ShutdownThread.shutdown(mContext, false);
                break;
            }
        }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
    }
;
    LocalPowerManager mPowerManager;
    boolean mPrevAllowLockscreenWhenOn;
    boolean mPrevDismissKeyguard;
    boolean mPrevHideLockScreen;
    RecentApplicationsDialog mRecentAppsDialog;
    int mRecentAppsDialogHeldModifiers;
    int mResettingSystemUiFlags;
    int mRestrictedScreenHeight;
    int mRestrictedScreenLeft;
    int mRestrictedScreenTop;
    int mRestrictedScreenWidth;
    boolean mSafeMode;
    long mSafeModeDisabledVibePattern[];
    long mSafeModeEnabledVibePattern[];
    Runnable mScreenLockTimeout;
    int mScreenOffReason;
    boolean mScreenOnEarly;
    boolean mScreenOnFully;
    private final Runnable mScreenshotChordLongPress = new Runnable() {

        final PhoneWindowManager this$0;

        public void run()
        {
            takeScreenshot();
        }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
    }
;
    ServiceConnection mScreenshotConnection;
    final Object mScreenshotLock = new Object();
    final Runnable mScreenshotTimeout = new Runnable() {

        final PhoneWindowManager this$0;

        public void run()
        {
            Object obj = mScreenshotLock;
            obj;
            JVM INSTR monitorenter ;
            if (mScreenshotConnection != null)
            {
                mContext.unbindService(mScreenshotConnection);
                mScreenshotConnection = null;
            }
            return;
        }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
    }
;
    int mSeascapeRotation;
    int mShortcutKeyPressed;
    ShortcutManager mShortcutManager;
    android.view.WindowManagerPolicy.WindowState mStatusBar;
    boolean mStatusBarCanHide;
    boolean mStatusBarCanHideBeforeIPO;
    int mStatusBarHeight;
    final ArrayList mStatusBarPanels = new ArrayList();
    IStatusBarService mStatusBarService;
    private Object mStkLock;
    BroadcastReceiver mStkUserActivityEnReceiver;
    boolean mSystemBooted;
    boolean mSystemReady;
    android.view.WindowManagerPolicy.WindowState mTopFullscreenOpaqueWindowState;
    boolean mTopIsFullscreen;
    int mUiMode;
    int mUnrestrictedScreenHeight;
    int mUnrestrictedScreenLeft;
    int mUnrestrictedScreenTop;
    int mUnrestrictedScreenWidth;
    int mUpsideDownRotation;
    int mUserRotation;
    int mUserRotationMode;
    Vibrator mVibrator;
    long mVirtualKeyVibePattern[];
    private boolean mVolumeDownKeyConsumedByScreenshotChord;
    private long mVolumeDownKeyTime;
    private boolean mVolumeDownKeyTriggered;
    private boolean mVolumeUpKeyTriggered;
    IWindowManager mWindowManager;
    android.view.WindowManagerPolicy.WindowManagerFuncs mWindowManagerFuncs;

    public PhoneWindowManager()
    {
        mEnableShiftMenuBugReports = false;
        mStatusBar = null;
        mNavigationBar = null;
        mHasNavigationBar = false;
        mNavigationBarWidth = 0;
        mNavigationBarHeight = 0;
        mKeyguard = null;
        mLidOpen = -1;
        mUiMode = 1;
        mDockMode = 0;
        mUserRotationMode = 0;
        mUserRotation = 0;
        mAllowAllRotations = -1;
        mLongPressOnPowerBehavior = -1;
        mScreenOnEarly = false;
        mScreenOnFully = false;
        mOrientationSensorEnabled = false;
        mCurrentAppOrientation = -1;
        mAccelerometerDefault = 0;
        mHasSoftInput = false;
        mPointerLocationMode = 0;
        mPointerLocationView = null;
        mResettingSystemUiFlags = 0;
        mForceClearedSystemUiFlags = 0;
        mLastFocusNeedsMenu = false;
        mHideNavFakeWindow = null;
        mShortcutKeyPressed = -1;
        mPrevHideLockScreen = false;
        mPrevDismissKeyguard = false;
        mPrevAllowLockscreenWhenOn = false;
        mLandscapeRotation = 0;
        mSeascapeRotation = 0;
        mPortraitRotation = 0;
        mUpsideDownRotation = 0;
        mLongPressOnHomeBehavior = -1;
        mKeyDispatcMode = 0;
        mAppLaunchTimeEnabled = false;
        mIsStkUserActivityEnabled = false;
        mStkLock = new Object();
        mScreenOffReason = -1;
        mHDMIObserver = new UEventObserver() {

            final PhoneWindowManager this$0;

            public void onUEvent(android.os.UEventObserver.UEvent uevent)
            {
                setHdmiPlugged("1".equals(uevent.get("SWITCH_STATE")));
            }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
        }
;
        mScreenshotConnection = null;
        mBroadcastDone = new BroadcastReceiver() {

            final PhoneWindowManager this$0;

            public void onReceive(Context context, Intent intent)
            {
                mBroadcastWakeLock.release();
            }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
        }
;
        mDockReceiver = new BroadcastReceiver() {

            final PhoneWindowManager this$0;

            public void onReceive(Context context, Intent intent)
            {
                if ("android.intent.action.DOCK_EVENT".equals(intent.getAction()))
                    mDockMode = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                else
                    try
                    {
                        IUiModeManager iuimodemanager = android.app.IUiModeManager.Stub.asInterface(ServiceManager.getService("uimode"));
                        mUiMode = iuimodemanager.getCurrentModeType();
                    }
                    catch (RemoteException _ex) { }
                updateRotation(true);
                updateOrientationListenerLp();
            }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
        }
;
        mKeyDispatchReceiver = new BroadcastReceiver() {

            final PhoneWindowManager this$0;

            public void onReceive(Context context, Intent intent)
            {
                Object obj1;
                obj1 = intent.getAction();
                Log.v("WindowManager", "mKeyDispatchReceiver -- onReceive -- entry");
                Object obj = mKeyDispatchLock;
                obj;
                JVM INSTR monitorenter ;
                if (!((String) (obj1)).equals("android.intent.action.ACTION_SHUTDOWN_IPO")) goto _L2; else goto _L1
_L1:
                if (PhoneWindowManager.DEBUG_INPUT)
                    Log.v("WindowManager", "Receive IPO_ENABLE");
                mKeyDispatcMode = 1;
                mStatusBarCanHideBeforeIPO = mStatusBarCanHide;
                mStatusBarCanHide = true;
_L3:
                if (PhoneWindowManager.DEBUG_INPUT)
                    Log.v("WindowManager", (new StringBuilder()).append("mKeyDispatchReceiver -- onReceive -- exist").append(mKeyDispatcMode).toString());
                return;
_L2:
                if (!((String) (obj1)).equals("android.intent.action.ACTION_BOOT_IPO"))
                    break MISSING_BLOCK_LABEL_174;
                if (PhoneWindowManager.DEBUG_INPUT)
                    Log.v("WindowManager", "Receive IPO_DISABLE");
                mKeyDispatcMode = 0;
                mStatusBarCanHide = mStatusBarCanHideBeforeIPO;
                  goto _L3
                obj1;
                throw obj1;
                if (((String) (obj1)).equals("android.intent.action.DISABLE_POWER_KEY"))
                {
                    if (intent.getBooleanExtra("state", false))
                    {
                        if (PhoneWindowManager.DEBUG_INPUT)
                            Log.v("WindowManager", "Receive POWERKEY_DISABLE as true");
                        mKeyDispatcMode = 3;
                    } else
                    {
                        if (PhoneWindowManager.DEBUG_INPUT)
                            Log.v("WindowManager", "Receive POWERKEY_DISABLE as false");
                        mKeyDispatcMode = 0;
                    }
                } else
                if (PhoneWindowManager.DEBUG_INPUT)
                    Log.v("WindowManager", "Receive Fake Intent");
                  goto _L3
            }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
        }
;
        mStkUserActivityEnReceiver = new BroadcastReceiver() {

            final PhoneWindowManager this$0;

            public void onReceive(Context context, Intent intent)
            {
                String s;
                s = intent.getAction();
                Log.v("WindowManager", "mStkUserActivityEnReceiver -- onReceive -- entry");
                Object obj = mStkLock;
                obj;
                JVM INSTR monitorenter ;
                if (!s.equals("android.intent.action.stk.USER_ACTIVITY.enable"))
                    break MISSING_BLOCK_LABEL_118;
                if (PhoneWindowManager.DEBUG_INPUT)
                    Log.v("WindowManager", "Receive STK_ENABLE");
                boolean flag = intent.getBooleanExtra("state", false);
                if (flag != mIsStkUserActivityEnabled)
                    mIsStkUserActivityEnabled = flag;
_L2:
                if (PhoneWindowManager.DEBUG_INPUT)
                    Log.v("WindowManager", (new StringBuilder()).append("mStkUserActivityEnReceiver -- onReceive -- exist ").append(mIsStkUserActivityEnabled).toString());
                return;
                if (PhoneWindowManager.DEBUG_INPUT)
                    Log.e("WindowManager", "Receive Fake Intent");
                if (true) goto _L2; else goto _L1
_L1:
                Exception exception;
                exception;
                throw exception;
            }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
        }
;
        mBootMsgDialog = null;
        mScreenLockTimeout = new Runnable() {

            final PhoneWindowManager this$0;

            public void run()
            {
                this;
                JVM INSTR monitorenter ;
                if (PhoneWindowManager.localLOGV)
                    Log.v("WindowManager", "mScreenLockTimeout activating keyguard");
                mKeyguardMediator.doKeyguardTimeout();
                mLockScreenTimerActive = false;
                return;
            }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
        }
;
    }

    private void cancelPendingPowerKeyAction()
    {
        if (!mPowerKeyHandled)
            mHandler.removeCallbacks(mPowerLongPress);
        if (mPowerKeyTriggered)
            mPendingPowerKeyUpCanceled = true;
    }

    private void cancelPendingScreenshotChordAction()
    {
        mHandler.removeCallbacks(mScreenshotChordLongPress);
    }

    private int determineHiddenState(int i, int j, int k)
    {
        if (mLidOpen != -1)
            switch (i)
            {
            default:
                break;

            case 1: // '\001'
                if (mLidOpen != 1)
                    k = j;
                break;

            case 2: // '\002'
                if (mLidOpen != 1)
                    j = k;
                k = j;
                break;
            }
        return k;
    }

    static IAudioService getAudioService()
    {
        IAudioService iaudioservice = android.media.IAudioService.Stub.asInterface(ServiceManager.checkService("audio"));
        if (iaudioservice == null)
            Log.w("WindowManager", "Unable to find IAudioService interface.");
        return iaudioservice;
    }

    private boolean getFallbackAction(KeyCharacterMap keycharactermap, int i, int j, android.view.KeyCharacterMap.FallbackAction fallbackaction)
    {
        return keycharactermap.getFallbackAction(i, j, fallbackaction);
    }

    static long[] getLongIntArray(Resources resources, int i)
    {
        int ai[] = resources.getIntArray(i);
        long al[];
        if (ai != null)
        {
            al = new long[ai.length];
            for (int j = 0; j < ai.length; j++)
                al[j] = ai[j];

        } else
        {
            al = null;
        }
        return al;
    }

    static ITelephony getTelephonyService()
    {
        ITelephony itelephony = com.android.internal.telephony.ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        if (itelephony == null)
            Log.w("WindowManager", "Unable to find ITelephony interface.");
        return itelephony;
    }

    private void handleLongPressOnHome()
    {
        if (mLongPressOnHomeBehavior < 0)
        {
            mLongPressOnHomeBehavior = mContext.getResources().getInteger(0x10e001d);
            if (mLongPressOnHomeBehavior < 0 || mLongPressOnHomeBehavior > 2)
                mLongPressOnHomeBehavior = 0;
        }
        if (mLongPressOnHomeBehavior != 0)
        {
            performHapticFeedbackLw(null, 0, false);
            sendCloseSystemWindows("recentapps");
            mHomePressed = false;
        }
        if (mLongPressOnHomeBehavior != 1) goto _L2; else goto _L1
_L1:
        showOrHideRecentAppsDialog(0);
_L4:
        return;
_L2:
        if (mLongPressOnHomeBehavior == 2)
            try
            {
                mStatusBarService.toggleRecentApps();
            }
            catch (RemoteException remoteexception)
            {
                Slog.e("WindowManager", "RemoteException when showing recent apps", remoteexception);
            }
        if (true) goto _L4; else goto _L3
_L3:
    }

    private void interceptPowerKeyDown(boolean flag)
    {
        mPowerKeyHandled = flag;
        if (!flag)
            mHandler.postDelayed(mPowerLongPress, ViewConfiguration.getGlobalActionKeyTimeout());
    }

    private boolean interceptPowerKeyUp(boolean flag)
    {
        boolean flag1 = false;
        if (!mPowerKeyHandled)
        {
            mHandler.removeCallbacks(mPowerLongPress);
            if (!flag)
                flag1 = true;
        }
        return flag1;
    }

    private void interceptScreenshotChord()
    {
        if (mVolumeDownKeyTriggered && mPowerKeyTriggered && !mVolumeUpKeyTriggered)
        {
            long l = SystemClock.uptimeMillis();
            if (l <= 150L + mVolumeDownKeyTime && l <= 150L + mPowerKeyTime)
            {
                mVolumeDownKeyConsumedByScreenshotChord = true;
                cancelPendingPowerKeyAction();
                mHandler.postDelayed(mScreenshotChordLongPress, ViewConfiguration.getGlobalActionKeyTimeout());
            }
        }
    }

    private boolean isAnyPortrait(int i)
    {
        boolean flag;
        if (i != mPortraitRotation && i != mUpsideDownRotation)
            flag = false;
        else
            flag = true;
        return flag;
    }

    private boolean isLandscapeOrSeascape(int i)
    {
        boolean flag;
        if (i != mLandscapeRotation && i != mSeascapeRotation)
            flag = false;
        else
            flag = true;
        return flag;
    }

    private boolean keyguardIsShowingTq()
    {
        return mKeyguardMediator.isShowingAndNotHidden();
    }

    private int readRotation(int i)
    {
        int j = mContext.getResources().getInteger(i);
        j;
        JVM INSTR lookupswitch 4: default 56
    //                   0: 61
    //                   90: 66
    //                   180: 71
    //                   270: 76;
           goto _L1 _L2 _L3 _L4 _L5
_L1:
        j = -1;
_L7:
        return j;
_L2:
        j = 0;
        continue; /* Loop/switch isn't completed */
_L3:
        j = 1;
        continue; /* Loop/switch isn't completed */
_L4:
        j = 2;
        continue; /* Loop/switch isn't completed */
_L5:
        j = 3;
        if (true) goto _L7; else goto _L6
_L6:
        JVM INSTR pop ;
        if (true) goto _L1; else goto _L8
_L8:
    }

    static void sendCloseSystemWindows(Context context, String s)
    {
        if (!ActivityManagerNative.isSystemReady())
            break MISSING_BLOCK_LABEL_15;
        ActivityManagerNative.getDefault().closeSystemDialogs(s);
_L2:
        return;
        JVM INSTR pop ;
        if (true) goto _L2; else goto _L1
_L1:
    }

    private void takeScreenshot()
    {
        Object obj = mScreenshotLock;
        obj;
        JVM INSTR monitorenter ;
        if (mScreenshotConnection == null)
        {
            Object obj1 = new ComponentName("com.android.systemui", "com.android.systemui.screenshot.TakeScreenshotService");
            Intent intent = new Intent();
            intent.setComponent(((ComponentName) (obj1)));
            obj1 = new ServiceConnection() {

                final PhoneWindowManager this$0;

                public void onServiceConnected(ComponentName componentname, IBinder ibinder)
                {
                    Object obj2 = mScreenshotLock;
                    obj2;
                    JVM INSTR monitorenter ;
                    Message message;
                    Messenger messenger;
                    if (mScreenshotConnection != this)
                        break MISSING_BLOCK_LABEL_171;
                    messenger = new Messenger(ibinder);
                    message = Message.obtain(null, 1);
                    message.replyTo = new Messenger(new Handler(this) {

                        final _cls12 this$1;
                        final ServiceConnection val$myConn;

                        public void handleMessage(Message message1)
                        {
                            Object obj3 = mScreenshotLock;
                            obj3;
                            JVM INSTR monitorenter ;
                            if (mScreenshotConnection == myConn)
                            {
                                mContext.unbindService(mScreenshotConnection);
                                mScreenshotConnection = null;
                                mHandler.removeCallbacks(mScreenshotTimeout);
                            }
                            return;
                        }

                    
                    {
                        this$1 = _cls12.this;
                        myConn = serviceconnection;
                        super(final_looper);
                    }
                    }
);
                    message.arg2 = 0;
                    message.arg1 = 0;
                    if (mStatusBar != null && mStatusBar.isVisibleLw())
                        message.arg1 = 1;
                    if (mNavigationBar != null && mNavigationBar.isVisibleLw())
                        message.arg2 = 1;
                    Exception exception;
                    try
                    {
                        messenger.send(message);
                    }
                    catch (RemoteException _ex) { }
                    obj2;
                    JVM INSTR monitorexit ;
                    break MISSING_BLOCK_LABEL_171;
                    exception;
                    throw exception;
                }

                public void onServiceDisconnected(ComponentName componentname)
                {
                }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
            }
;
            if (mContext.bindService(intent, ((ServiceConnection) (obj1)), 1))
            {
                mScreenshotConnection = ((ServiceConnection) (obj1));
                mHandler.postDelayed(mScreenshotTimeout, 10000L);
            }
        }
        return;
    }

    private void updateKeyboardVisibility()
    {
        boolean flag = true;
        LocalPowerManager localpowermanager = mPowerManager;
        if (mLidOpen != flag)
            flag = false;
        localpowermanager.setKeyboardVisibility(flag);
    }

    private void updateLockScreenTimeout()
    {
        Runnable runnable = mScreenLockTimeout;
        runnable;
        JVM INSTR monitorenter ;
        boolean flag;
        if (mAllowLockscreenWhenOn && mScreenOnEarly && mKeyguardMediator.isSecure())
            flag = true;
        else
            flag = false;
        if (mLockScreenTimerActive != flag)
        {
            if (flag)
            {
                if (localLOGV)
                    Log.v("WindowManager", "setting lockscreen timer");
                mHandler.postDelayed(mScreenLockTimeout, mLockScreenTimeout);
            } else
            {
                if (localLOGV)
                    Log.v("WindowManager", "clearing lockscreen timer");
                mHandler.removeCallbacks(mScreenLockTimeout);
            }
            mLockScreenTimerActive = flag;
        }
        return;
    }

    private int updateSystemUiVisibilityLw()
    {
        int i;
        if (mFocusedWindow != null)
        {
            final int visibility = mFocusedWindow.getSystemUiVisibility() & (-1 ^ mResettingSystemUiFlags) & (-1 ^ mForceClearedSystemUiFlags);
            i = visibility ^ mLastSystemUiFlags;
            final boolean needsMenu = mFocusedWindow.getNeedsMenuLw(mTopFullscreenOpaqueWindowState);
            if (i != 0 || mLastFocusNeedsMenu != needsMenu || mFocusedApp != mFocusedWindow.getAppToken())
            {
                mLastSystemUiFlags = visibility;
                mLastFocusNeedsMenu = needsMenu;
                mFocusedApp = mFocusedWindow.getAppToken();
                mHandler.post(new Runnable() {

                    final PhoneWindowManager this$0;
                    final boolean val$needsMenu;
                    final int val$visibility;

                    public void run()
                    {
                        if (mStatusBarService == null)
                            mStatusBarService = com.android.internal.statusbar.IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
                        if (mStatusBarService == null)
                            break MISSING_BLOCK_LABEL_67;
                        mStatusBarService.setSystemUiVisibility(visibility);
                        mStatusBarService.topAppWindowChanged(needsMenu);
_L1:
                        return;
                        JVM INSTR pop ;
                        mStatusBarService = null;
                          goto _L1
                    }

            
            {
                this$0 = PhoneWindowManager.this;
                visibility = i;
                needsMenu = flag;
                super();
            }
                }
);
            } else
            {
                i = 0;
            }
        } else
        {
            i = 0;
        }
        return i;
    }

    public View addStartingWindow(IBinder ibinder, String s, int i, CompatibilityInfo compatibilityinfo, CharSequence charsequence, int j, int k, 
            int l)
    {
        if (s != null) goto _L2; else goto _L1
_L1:
        Object obj1 = null;
_L3:
        return ((View) (obj1));
_L2:
        int i1;
        obj1 = mContext;
        i1 = ((Context) (obj1)).getThemeResId();
        if (i == i1 && j == 0)
            break MISSING_BLOCK_LABEL_49;
        Object obj;
        RuntimeException runtimeexception;
        WindowManager windowmanager;
        Object obj2;
        StringBuilder stringbuilder;
        try
        {
            obj1 = ((Context) (obj1)).createPackageContext(s, 0);
            ((Context) (obj1)).setTheme(i);
        }
        catch (android.content.pm.PackageManager.NameNotFoundException _ex) { }
        obj2 = PolicyManager.makeNewWindow(((Context) (obj1)));
        if (((Window) (obj2)).getWindowStyle().getBoolean(12, false))
        {
            obj1 = null;
        } else
        {
            ((Window) (obj2)).setTitle(((Context) (obj1)).getResources().getText(j, charsequence));
            ((Window) (obj2)).setType(3);
            ((Window) (obj2)).setFlags(0x20000 | (8 | (l | 0x10)), 0x20000 | (8 | (l | 0x10)));
            if (!compatibilityinfo.supportsScreen())
                ((Window) (obj2)).addFlags(0x20000000);
            ((Window) (obj2)).setLayout(-1, -1);
            obj = ((Window) (obj2)).getAttributes();
            obj.token = ibinder;
            obj.packageName = s;
            obj.windowAnimations = ((Window) (obj2)).getWindowStyle().getResourceId(8, 0);
            obj.privateFlags = 1 | ((android.view.WindowManager.LayoutParams) (obj)).privateFlags;
            ((android.view.WindowManager.LayoutParams) (obj)).setTitle((new StringBuilder()).append("Starting ").append(s).toString());
            windowmanager = (WindowManager)((Context) (obj1)).getSystemService("window");
            obj1 = ((Window) (obj2)).getDecorView();
            if (!((Window) (obj2)).isFloating())
                break MISSING_BLOCK_LABEL_259;
            obj1 = null;
        }
          goto _L3
        if (!localLOGV) goto _L5; else goto _L4
_L4:
        stringbuilder = (new StringBuilder()).append("Adding starting window for ").append(s).append(" / ").append(ibinder).append(": ");
        if (((View) (obj1)).getParent() == null)
            break MISSING_BLOCK_LABEL_357;
        obj2 = obj1;
_L6:
        Log.v("WindowManager", stringbuilder.append(obj2).toString());
_L5:
        windowmanager.addView(((View) (obj1)), ((android.view.ViewGroup.LayoutParams) (obj)));
        obj = ((View) (obj1)).getParent();
        if (obj == null)
            obj1 = null;
          goto _L3
        obj2 = null;
          goto _L6
        JVM INSTR pop ;
        Log.w("WindowManager", (new StringBuilder()).append(ibinder).append(" already running, starting window not displayed").toString());
_L7:
        obj1 = null;
          goto _L3
        runtimeexception;
        Log.w("WindowManager", (new StringBuilder()).append(ibinder).append(" failed creating starting window").toString(), runtimeexception);
          goto _L7
    }

    public void adjustConfigurationLw(Configuration configuration)
    {
        readLidState();
        updateKeyboardVisibility();
        if (configuration.keyboard != 1)
            configuration.hardKeyboardHidden = determineHiddenState(mLidKeyboardAccessibility, 2, 1);
        else
            configuration.hardKeyboardHidden = 2;
        if (configuration.navigation != 1)
            configuration.navigationHidden = determineHiddenState(mLidNavigationAccessibility, 2, 1);
        else
            configuration.navigationHidden = 2;
        if (!mHasSoftInput && configuration.hardKeyboardHidden != 1)
            configuration.keyboardHidden = 2;
        else
            configuration.keyboardHidden = 1;
    }

    public int adjustSystemUiVisibilityLw(int i)
    {
        mResettingSystemUiFlags = i & mResettingSystemUiFlags;
        return i & (-1 ^ mResettingSystemUiFlags) & (-1 ^ mForceClearedSystemUiFlags);
    }

    public void adjustWindowParamsLw(android.view.WindowManager.LayoutParams layoutparams)
    {
        switch (layoutparams.type)
        {
        case 2005: 
        case 2006: 
        case 2015: 
            layoutparams.flags = 0x18 | layoutparams.flags;
            layoutparams.flags = 0xfffbffff & layoutparams.flags;
            break;
        }
    }

    public boolean allowAppAnimationsLw()
    {
        boolean flag;
        if (mKeyguard == null || !mKeyguard.isVisibleLw())
            flag = true;
        else
            flag = false;
        return flag;
    }

    public boolean allowKeyRepeat()
    {
        return mScreenOnEarly;
    }

    public void animatingWindowLw(android.view.WindowManagerPolicy.WindowState windowstate, android.view.WindowManager.LayoutParams layoutparams)
    {
        if (DEBUG_LAYOUT)
            Slog.i("WindowManager", (new StringBuilder()).append("Win ").append(windowstate).append(": isVisibleOrBehindKeyguardLw=").append(windowstate.isVisibleOrBehindKeyguardLw()).toString());
        if (mTopFullscreenOpaqueWindowState == null && windowstate.isVisibleOrBehindKeyguardLw() && !windowstate.isGoneForLayoutLw())
        {
            if ((0x800 & layoutparams.flags) != 0)
                mForceStatusBar = true;
            if (layoutparams.type < 1 || layoutparams.type > 99 || layoutparams.x != 0 || layoutparams.y != 0 || layoutparams.width != -1 || layoutparams.height != -1)
            {
                if (layoutparams.type == 2017 && layoutparams.x >= 0 && layoutparams.y >= 0)
                {
                    if (DEBUG_LAYOUT)
                        Log.v("WindowManager", (new StringBuilder()).append("Fullscreen window: ").append(windowstate).toString());
                    mTopFullscreenOpaqueWindowState = windowstate;
                    mHideLockScreen = mPrevHideLockScreen;
                    mAllowLockscreenWhenOn = mPrevAllowLockscreenWhenOn;
                    mDismissKeyguard = mPrevDismissKeyguard;
                }
            } else
            {
                if (DEBUG_LAYOUT)
                    Log.v("WindowManager", (new StringBuilder()).append("Fullscreen window: ").append(windowstate).toString());
                mTopFullscreenOpaqueWindowState = windowstate;
                if ((0x80000 & layoutparams.flags) != 0)
                {
                    if (localLOGV)
                        Log.v("WindowManager", (new StringBuilder()).append("Setting mHideLockScreen to true by win ").append(windowstate).toString());
                    if (!mKeyguardMediator.DM_Check_Locked())
                        mHideLockScreen = true;
                    else
                    if (!layoutparams.packageName.matches("com.android.phone"))
                        mHideLockScreen = false;
                    else
                        mHideLockScreen = true;
                }
                if ((0x400000 & layoutparams.flags) != 0)
                {
                    if (localLOGV)
                        Log.v("WindowManager", (new StringBuilder()).append("Setting mDismissKeyguard to true by win ").append(windowstate).toString());
                    if (!mKeyguardMediator.DM_Check_Locked())
                        mDismissKeyguard = true;
                    else
                    if (!layoutparams.packageName.matches("com.android.phone"))
                        mDismissKeyguard = false;
                    else
                        mDismissKeyguard = true;
                }
                if ((1 & layoutparams.flags) != 0)
                    mAllowLockscreenWhenOn = true;
            }
        }
    }

    public void beginAnimationLw(int i, int j)
    {
        mTopFullscreenOpaqueWindowState = null;
        mForceStatusBar = false;
        mHideLockScreen = false;
        mAllowLockscreenWhenOn = false;
        mDismissKeyguard = false;
    }

    public void beginLayoutLw(int i, int j, int k)
    {
        mUnrestrictedScreenTop = 0;
        mUnrestrictedScreenLeft = 0;
        mUnrestrictedScreenWidth = i;
        mUnrestrictedScreenHeight = j;
        mRestrictedScreenTop = 0;
        mRestrictedScreenLeft = 0;
        mRestrictedScreenWidth = i;
        mRestrictedScreenHeight = j;
        mCurLeft = 0;
        mContentLeft = 0;
        mDockLeft = 0;
        mCurTop = 0;
        mContentTop = 0;
        mDockTop = 0;
        mCurRight = i;
        mContentRight = i;
        mDockRight = i;
        mCurBottom = j;
        mContentBottom = j;
        mDockBottom = j;
        mDockLayer = 0x10000000;
        Rect rect = mTmpParentFrame;
        Rect rect2 = mTmpDisplayFrame;
        Rect rect1 = mTmpVisibleFrame;
        int i1 = mDockLeft;
        rect1.left = i1;
        rect2.left = i1;
        rect.left = i1;
        i1 = mDockTop;
        rect1.top = i1;
        rect2.top = i1;
        rect.top = i1;
        i1 = mDockRight;
        rect1.right = i1;
        rect2.right = i1;
        rect.right = i1;
        i1 = mDockBottom;
        rect1.bottom = i1;
        rect2.bottom = i1;
        rect.bottom = i1;
        boolean flag;
        if (mNavigationBar != null && !mNavigationBar.isVisibleLw() || (2 & mLastSystemUiFlags) != 0)
            flag = false;
        else
            flag = true;
        if (!flag)
        {
            if (mHideNavFakeWindow == null)
                mHideNavFakeWindow = mWindowManagerFuncs.addFakeWindow(mHandler.getLooper(), mHideNavInputHandler, "hidden nav", 2022, 0, false, false, true);
        } else
        if (mHideNavFakeWindow != null)
        {
            mHideNavFakeWindow.dismiss();
            mHideNavFakeWindow = null;
        }
        if (mStatusBar != null)
        {
            if (mNavigationBar != null)
            {
                if (i >= j)
                {
                    mTmpNavigationFrame.set(i - mNavigationBarWidth, 0, i, j);
                    if (!flag)
                    {
                        mTmpNavigationFrame.offset(mNavigationBarWidth, 0);
                    } else
                    {
                        mDockRight = mTmpNavigationFrame.left;
                        mRestrictedScreenWidth = mDockRight - mDockLeft;
                    }
                } else
                {
                    mTmpNavigationFrame.set(0, j - mNavigationBarHeight, i, j);
                    if (!flag)
                    {
                        mTmpNavigationFrame.offset(0, mNavigationBarHeight);
                    } else
                    {
                        mDockBottom = mTmpNavigationFrame.top;
                        mRestrictedScreenHeight = mDockBottom - mDockTop;
                    }
                }
                int j1 = mDockTop;
                mCurTop = j1;
                mContentTop = j1;
                j1 = mDockBottom;
                mCurBottom = j1;
                mContentBottom = j1;
                j1 = mDockLeft;
                mCurLeft = j1;
                mContentLeft = j1;
                j1 = mDockRight;
                mCurRight = j1;
                mContentRight = j1;
                mNavigationBar.computeFrameLw(mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame);
                if (DEBUG_LAYOUT)
                    Log.i("WindowManager", (new StringBuilder()).append("mNavigationBar frame: ").append(mTmpNavigationFrame).toString());
            }
            if (DEBUG_LAYOUT)
            {
                Object aobj1[] = new Object[4];
                aobj1[0] = Integer.valueOf(mDockLeft);
                aobj1[1] = Integer.valueOf(mDockTop);
                aobj1[2] = Integer.valueOf(mDockRight);
                aobj1[3] = Integer.valueOf(mDockBottom);
                Log.i("WindowManager", String.format("mDock rect: (%d,%d - %d,%d)", aobj1));
            }
            int k1 = mDockLeft;
            rect1.left = k1;
            rect2.left = k1;
            rect.left = k1;
            k1 = mDockTop;
            rect1.top = k1;
            rect2.top = k1;
            rect.top = k1;
            k1 = mDockRight;
            rect1.right = k1;
            rect2.right = k1;
            rect.right = k1;
            k1 = mDockBottom;
            rect1.bottom = k1;
            rect2.bottom = k1;
            rect.bottom = k1;
            mStatusBar.computeFrameLw(rect, rect2, rect1, rect1);
            if (mStatusBar.isVisibleLw())
            {
                int l = mStatusBar.getFrameLw();
                if (!mStatusBarCanHide)
                {
                    if (mRestrictedScreenTop != ((Rect) (l)).top)
                    {
                        if (mRestrictedScreenHeight - mRestrictedScreenTop == ((Rect) (l)).bottom)
                            mRestrictedScreenHeight = mRestrictedScreenHeight - (((Rect) (l)).bottom - ((Rect) (l)).top);
                    } else
                    {
                        mRestrictedScreenTop = ((Rect) (l)).bottom;
                        mRestrictedScreenHeight = mRestrictedScreenHeight - (((Rect) (l)).bottom - ((Rect) (l)).top);
                    }
                    l = mRestrictedScreenTop;
                    mDockTop = l;
                    mCurTop = l;
                    mContentTop = l;
                    l = mRestrictedScreenTop + mRestrictedScreenHeight;
                    mDockBottom = l;
                    mCurBottom = l;
                    mContentBottom = l;
                    if (DEBUG_LAYOUT)
                        Log.v("WindowManager", (new StringBuilder()).append("Status bar: restricted screen area: (").append(mRestrictedScreenLeft).append(",").append(mRestrictedScreenTop).append(",").append(mRestrictedScreenLeft + mRestrictedScreenWidth).append(",").append(mRestrictedScreenTop + mRestrictedScreenHeight).append(")").toString());
                } else
                {
                    if (mDockTop != ((Rect) (l)).top)
                    {
                        if (mDockBottom == ((Rect) (l)).bottom)
                            mDockBottom = ((Rect) (l)).top;
                    } else
                    {
                        mDockTop = ((Rect) (l)).bottom;
                    }
                    l = mDockTop;
                    mCurTop = l;
                    mContentTop = l;
                    l = mDockBottom;
                    mCurBottom = l;
                    mContentBottom = l;
                    l = mDockLeft;
                    mCurLeft = l;
                    mContentLeft = l;
                    l = mDockRight;
                    mCurRight = l;
                    mContentRight = l;
                    if (DEBUG_LAYOUT)
                    {
                        StringBuilder stringbuilder = (new StringBuilder()).append("Status bar: ");
                        Object aobj[] = new Object[12];
                        aobj[0] = Integer.valueOf(mDockLeft);
                        aobj[1] = Integer.valueOf(mDockTop);
                        aobj[2] = Integer.valueOf(mDockRight);
                        aobj[3] = Integer.valueOf(mDockBottom);
                        aobj[4] = Integer.valueOf(mContentLeft);
                        aobj[5] = Integer.valueOf(mContentTop);
                        aobj[6] = Integer.valueOf(mContentRight);
                        aobj[7] = Integer.valueOf(mContentBottom);
                        aobj[8] = Integer.valueOf(mCurLeft);
                        aobj[9] = Integer.valueOf(mCurTop);
                        aobj[10] = Integer.valueOf(mCurRight);
                        aobj[11] = Integer.valueOf(mCurBottom);
                        Log.v("WindowManager", stringbuilder.append(String.format("dock=[%d,%d][%d,%d] content=[%d,%d][%d,%d] cur=[%d,%d][%d,%d]", aobj)).toString());
                    }
                }
            }
        }
    }

    public boolean canBeForceHidden(android.view.WindowManagerPolicy.WindowState windowstate, android.view.WindowManager.LayoutParams layoutparams)
    {
        boolean flag;
        if (layoutparams.type == 2000 || layoutparams.type == 2013)
            flag = false;
        else
            flag = true;
        return flag;
    }

    public boolean canStatusBarHide()
    {
        return mStatusBarCanHide;
    }

    public int checkAddPermission(android.view.WindowManager.LayoutParams layoutparams)
    {
        byte byte0 = 0;
        int i = layoutparams.type;
        if (i >= 2000 && i <= 2999)
        {
            String s = null;
            switch (i)
            {
            default:
                s = "android.permission.INTERNAL_SYSTEM_WINDOW";
                break;

            case 2002: 
            case 2003: 
            case 2006: 
            case 2007: 
            case 2010: 
                s = "android.permission.SYSTEM_ALERT_WINDOW";
                break;

            case 2005: 
            case 2011: 
            case 2013: 
            case 2023: 
                break;
            }
            if (s != null && mContext.checkCallingOrSelfPermission(s) != 0)
                byte0 = -8;
        }
        return byte0;
    }

    public Animation createForceHideEnterAnimation()
    {
        return AnimationUtils.loadAnimation(mContext, 0x10a001f);
    }

    Intent createHomeDockIntent()
    {
        Intent intent1 = null;
        Intent intent;
        if (mUiMode != 3)
        {
            if (mUiMode != 2);
            intent = null;
        } else
        {
            intent = mCarDockIntent;
        }
        if (intent != null)
        {
            ActivityInfo activityinfo = intent.resolveActivityInfo(mContext.getPackageManager(), 128);
            if (activityinfo != null)
            {
                if (activityinfo.metaData == null || !activityinfo.metaData.getBoolean("android.dock_home"))
                {
                    Intent _tmp = intent;
                } else
                {
                    intent = new Intent(intent);
                    intent.setClassName(activityinfo.packageName, activityinfo.name);
                    intent1 = intent;
                }
            } else
            {
                Intent _tmp1 = intent;
            }
        } else
        {
            Intent _tmp2 = intent;
        }
        return intent1;
    }

    public boolean detectSafeMode()
    {
        char c = '\001';
        int i;
        int j;
        int k;
        int l;
        int i1;
        k = mWindowManager.getKeycodeState(82);
        l = mWindowManager.getKeycodeState(47);
        j = mWindowManager.getDPadKeycodeState(23);
        i = mWindowManager.getTrackballScancodeState(272);
        i1 = mWindowManager.getKeycodeState(25);
        if (k <= 0 && l <= 0 && j <= 0 && i <= 0 && i1 <= 0)
            c = '\0';
        mSafeMode = c;
        if (!mSafeMode)
            break MISSING_BLOCK_LABEL_220;
        c = '\u2711';
_L1:
        performHapticFeedbackLw(null, c, true);
        if (mSafeMode)
            Log.i("WindowManager", (new StringBuilder()).append("SAFE MODE ENABLED (menu=").append(k).append(" s=").append(l).append(" dpad=").append(j).append(" trackball=").append(i).append(")").toString());
        else
            Log.i("WindowManager", "SAFE MODE not enabled");
        return mSafeMode;
        JVM INSTR pop ;
        throw new RuntimeException("window manager dead");
        c = '\u2710';
          goto _L1
    }

    public void dismissKeyguardLw()
    {
        if (!mKeyguardMediator.isSecure() && mKeyguardMediator.isShowing())
            mHandler.post(new Runnable() {

                final PhoneWindowManager this$0;

                public void run()
                {
                    if (!mPowerManager.isScreenOn())
                        mKeyguardMediator.keyguardDone(false, false);
                    else
                        mKeyguardMediator.keyguardDone(false, true);
                }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
            }
);
    }

    public KeyEvent dispatchUnhandledKey(android.view.WindowManagerPolicy.WindowState windowstate, KeyEvent keyevent, int i)
    {
        KeyEvent keyevent1;
label0:
        {
label1:
            {
                if (DEBUG_FALLBACK)
                    Slog.d("WindowManager", (new StringBuilder()).append("Unhandled key: win=").append(windowstate).append(", action=").append(keyevent.getAction()).append(", flags=").append(keyevent.getFlags()).append(", keyCode=").append(keyevent.getKeyCode()).append(", scanCode=").append(keyevent.getScanCode()).append(", metaState=").append(keyevent.getMetaState()).append(", repeatCount=").append(keyevent.getRepeatCount()).append(", policyFlags=").append(i).toString());
                if ((0x400 & keyevent.getFlags()) == 0 && getFallbackAction(keyevent.getKeyCharacterMap(), keyevent.getKeyCode(), keyevent.getMetaState(), mFallbackAction))
                {
                    if (DEBUG_FALLBACK)
                        Slog.d("WindowManager", (new StringBuilder()).append("Fallback: keyCode=").append(mFallbackAction.keyCode).append(" metaState=").append(Integer.toHexString(mFallbackAction.metaState)).toString());
                    int j = 0x400 | keyevent.getFlags();
                    keyevent1 = KeyEvent.obtain(keyevent.getDownTime(), keyevent.getEventTime(), keyevent.getAction(), mFallbackAction.keyCode, keyevent.getRepeatCount(), mFallbackAction.metaState, keyevent.getDeviceId(), keyevent.getScanCode(), j, keyevent.getSource(), null);
                    if ((1 & interceptKeyBeforeQueueing(keyevent1, i, true)) != 0 && interceptKeyBeforeDispatching(windowstate, keyevent1, i) == 0L)
                        break label1;
                    keyevent1.recycle();
                }
                if (DEBUG_FALLBACK)
                    Slog.d("WindowManager", "No fallback.");
                keyevent1 = null;
                break label0;
            }
            if (DEBUG_FALLBACK)
                Slog.d("WindowManager", "Performing fallback.");
        }
        return keyevent1;
    }

    public boolean doesForceHide(android.view.WindowManagerPolicy.WindowState windowstate, android.view.WindowManager.LayoutParams layoutparams)
    {
        boolean flag;
        if (layoutparams.type != 2004)
            flag = false;
        else
            flag = true;
        return flag;
    }

    public void dump(String s, FileDescriptor filedescriptor, PrintWriter printwriter, String as[])
    {
        if (as.length <= 0)
        {
            printwriter.print(s);
            printwriter.print("mSafeMode=");
            printwriter.print(mSafeMode);
            printwriter.print(" mSystemReady=");
            printwriter.print(mSystemReady);
            printwriter.print(" mSystemBooted=");
            printwriter.println(mSystemBooted);
            printwriter.print(s);
            printwriter.print("mLidOpen=");
            printwriter.print(mLidOpen);
            printwriter.print(" mLidOpenRotation=");
            printwriter.print(mLidOpenRotation);
            printwriter.print(" mHdmiPlugged=");
            printwriter.println(mHdmiPlugged);
            if (mLastSystemUiFlags != 0 || mResettingSystemUiFlags != 0 || mForceClearedSystemUiFlags != 0)
            {
                printwriter.print(s);
                printwriter.print("mLastSystemUiFlags=0x");
                printwriter.print(Integer.toHexString(mLastSystemUiFlags));
                printwriter.print(" mResettingSystemUiFlags=0x");
                printwriter.print(Integer.toHexString(mResettingSystemUiFlags));
                printwriter.print(" mForceClearedSystemUiFlags=0x");
                printwriter.println(Integer.toHexString(mForceClearedSystemUiFlags));
            }
            if (mLastFocusNeedsMenu)
            {
                printwriter.print(s);
                printwriter.print("mLastFocusNeedsMenu=");
                printwriter.println(mLastFocusNeedsMenu);
            }
            printwriter.print(s);
            printwriter.print("mUiMode=");
            printwriter.print(mUiMode);
            printwriter.print(" mDockMode=");
            printwriter.print(mDockMode);
            printwriter.print(" mCarDockRotation=");
            printwriter.print(mCarDockRotation);
            printwriter.print(" mDeskDockRotation=");
            printwriter.println(mDeskDockRotation);
            printwriter.print(s);
            printwriter.print("mUserRotationMode=");
            printwriter.print(mUserRotationMode);
            printwriter.print(" mUserRotation=");
            printwriter.print(mUserRotation);
            printwriter.print(" mAllowAllRotations=");
            printwriter.println(mAllowAllRotations);
            printwriter.print(s);
            printwriter.print("mAccelerometerDefault=");
            printwriter.print(mAccelerometerDefault);
            printwriter.print(" mCurrentAppOrientation=");
            printwriter.println(mCurrentAppOrientation);
            printwriter.print(s);
            printwriter.print("mCarDockEnablesAccelerometer=");
            printwriter.print(mCarDockEnablesAccelerometer);
            printwriter.print(" mDeskDockEnablesAccelerometer=");
            printwriter.println(mDeskDockEnablesAccelerometer);
            printwriter.print(s);
            printwriter.print("mLidKeyboardAccessibility=");
            printwriter.print(mLidKeyboardAccessibility);
            printwriter.print(" mLidNavigationAccessibility=");
            printwriter.print(mLidNavigationAccessibility);
            printwriter.print(" mLongPressOnPowerBehavior=");
            printwriter.println(mLongPressOnPowerBehavior);
            printwriter.print(s);
            printwriter.print("mScreenOnEarly=");
            printwriter.print(mScreenOnEarly);
            printwriter.print(" mScreenOnFully=");
            printwriter.print(mScreenOnFully);
            printwriter.print(" mOrientationSensorEnabled=");
            printwriter.print(mOrientationSensorEnabled);
            printwriter.print(" mHasSoftInput=");
            printwriter.println(mHasSoftInput);
            printwriter.print(s);
            printwriter.print("mUnrestrictedScreen=(");
            printwriter.print(mUnrestrictedScreenLeft);
            printwriter.print(",");
            printwriter.print(mUnrestrictedScreenTop);
            printwriter.print(") ");
            printwriter.print(mUnrestrictedScreenWidth);
            printwriter.print("x");
            printwriter.println(mUnrestrictedScreenHeight);
            printwriter.print(s);
            printwriter.print("mRestrictedScreen=(");
            printwriter.print(mRestrictedScreenLeft);
            printwriter.print(",");
            printwriter.print(mRestrictedScreenTop);
            printwriter.print(") ");
            printwriter.print(mRestrictedScreenWidth);
            printwriter.print("x");
            printwriter.println(mRestrictedScreenHeight);
            printwriter.print(s);
            printwriter.print("mCur=(");
            printwriter.print(mCurLeft);
            printwriter.print(",");
            printwriter.print(mCurTop);
            printwriter.print(")-(");
            printwriter.print(mCurRight);
            printwriter.print(",");
            printwriter.print(mCurBottom);
            printwriter.println(")");
            printwriter.print(s);
            printwriter.print("mContent=(");
            printwriter.print(mContentLeft);
            printwriter.print(",");
            printwriter.print(mContentTop);
            printwriter.print(")-(");
            printwriter.print(mContentRight);
            printwriter.print(",");
            printwriter.print(mContentBottom);
            printwriter.println(")");
            printwriter.print(s);
            printwriter.print("mDock=(");
            printwriter.print(mDockLeft);
            printwriter.print(",");
            printwriter.print(mDockTop);
            printwriter.print(")-(");
            printwriter.print(mDockRight);
            printwriter.print(",");
            printwriter.print(mDockBottom);
            printwriter.println(")");
            printwriter.print(s);
            printwriter.print("mDockLayer=");
            printwriter.println(mDockLayer);
            printwriter.print(s);
            printwriter.print("mTopFullscreenOpaqueWindowState=");
            printwriter.println(mTopFullscreenOpaqueWindowState);
            printwriter.print(s);
            printwriter.print("mTopIsFullscreen=");
            printwriter.print(mTopIsFullscreen);
            printwriter.print(" mForceStatusBar=");
            printwriter.print(mForceStatusBar);
            printwriter.print(" mHideLockScreen=");
            printwriter.println(mHideLockScreen);
            printwriter.print(s);
            printwriter.print("mDismissKeyguard=");
            printwriter.print(mDismissKeyguard);
            printwriter.print(" mHomePressed=");
            printwriter.println(mHomePressed);
            printwriter.print(s);
            printwriter.print("mAllowLockscreenWhenOn=");
            printwriter.print(mAllowLockscreenWhenOn);
            printwriter.print(" mLockScreenTimeout=");
            printwriter.print(mLockScreenTimeout);
            printwriter.print(" mLockScreenTimerActive=");
            printwriter.println(mLockScreenTimerActive);
            printwriter.print(s);
            printwriter.print("mEndcallBehavior=");
            printwriter.print(mEndcallBehavior);
            printwriter.print(" mIncallPowerBehavior=");
            printwriter.print(mIncallPowerBehavior);
            printwriter.print(" mLongPressOnHomeBehavior=");
            printwriter.println(mLongPressOnHomeBehavior);
            printwriter.print(s);
            printwriter.print("mLandscapeRotation=");
            printwriter.print(mLandscapeRotation);
            printwriter.print(" mSeascapeRotation=");
            printwriter.println(mSeascapeRotation);
            printwriter.print(s);
            printwriter.print("mPortraitRotation=");
            printwriter.print(mPortraitRotation);
            printwriter.print(" mUpsideDownRotation=");
            printwriter.println(mUpsideDownRotation);
        } else
        if (!"-d enable 0".equals(as[0]))
        {
            if (!"-d enable 3".equals(as[0]))
            {
                if (!"-d enable 6".equals(as[0]))
                {
                    if (!"-d enable 10".equals(as[0]))
                    {
                        if (!"-d enable 16".equals(as[0]))
                        {
                            if (!"-d enable 21".equals(as[0]))
                                if (!"-d disable 0".equals(as[0]))
                                {
                                    if (!"-d disable 3".equals(as[0]))
                                    {
                                        if (!"-d disable 6".equals(as[0]))
                                        {
                                            if (!"-d disable 10".equals(as[0]))
                                            {
                                                if (!"-d disable 16".equals(as[0]))
                                                {
                                                    if (!"-d disable 21".equals(as[0]));
                                                } else
                                                {
                                                    DEBUG_FALLBACK = false;
                                                }
                                            } else
                                            {
                                                DEBUG_ORIENTATION = false;
                                                mOrientationListener.setLogEnabled(false);
                                            }
                                        } else
                                        {
                                            DEBUG_INPUT = false;
                                        }
                                    } else
                                    {
                                        DEBUG_LAYOUT = false;
                                    }
                                } else
                                {
                                    DEBUG = false;
                                    localLOGV = false;
                                }
                        } else
                        {
                            DEBUG_FALLBACK = true;
                        }
                    } else
                    {
                        DEBUG_ORIENTATION = true;
                        mOrientationListener.setLogEnabled(true);
                    }
                } else
                {
                    DEBUG_INPUT = true;
                }
            } else
            {
                DEBUG_LAYOUT = true;
            }
        } else
        {
            DEBUG = true;
            localLOGV = true;
        }
    }

    public void enableKeyguard(boolean flag)
    {
        mKeyguardMediator.setKeyguardEnabled(flag);
    }

    public void enableScreenAfterBoot()
    {
        readLidState();
        updateKeyboardVisibility();
        updateRotation(true);
    }

    public void exitKeyguardSecurely(android.view.WindowManagerPolicy.OnKeyguardExitResult onkeyguardexitresult)
    {
        mKeyguardMediator.verifyUnlock(onkeyguardexitresult);
    }

    public int finishAnimationLw()
    {
        int i = 0;
        boolean flag = false;
        mPrevHideLockScreen = mHideLockScreen;
        mPrevAllowLockscreenWhenOn = mAllowLockscreenWhenOn;
        mPrevDismissKeyguard = mDismissKeyguard;
        android.view.WindowManager.LayoutParams layoutparams;
        if (mTopFullscreenOpaqueWindowState == null)
            layoutparams = null;
        else
            layoutparams = mTopFullscreenOpaqueWindowState.getAttrs();
        if (mStatusBar != null)
        {
            if (DEBUG_LAYOUT)
                Log.i("WindowManager", (new StringBuilder()).append("force=").append(mForceStatusBar).append(" top=").append(mTopFullscreenOpaqueWindowState).toString());
            if (!mKeyguardMediator.DM_Check_Locked())
            {
                if (!mForceStatusBar)
                {
                    if (mTopFullscreenOpaqueWindowState != null)
                    {
                        if (localLOGV)
                        {
                            Log.d("WindowManager", (new StringBuilder()).append("frame: ").append(mTopFullscreenOpaqueWindowState.getFrameLw()).append(" shown frame: ").append(mTopFullscreenOpaqueWindowState.getShownFrameLw()).toString());
                            Log.d("WindowManager", (new StringBuilder()).append("attr: ").append(mTopFullscreenOpaqueWindowState.getAttrs()).append(" lp.flags=0x").append(Integer.toHexString(layoutparams.flags)).toString());
                        }
                        if ((0x400 & layoutparams.flags) == 0)
                            flag = false;
                        else
                            flag = true;
                        if (!flag)
                        {
                            if (DEBUG_LAYOUT)
                                Log.v("WindowManager", "** SHOWING status bar: top is not fullscreen");
                            if (mStatusBar.showLw(false))
                                i = false | true;
                        } else
                        if (!mStatusBarCanHide)
                        {
                            if (DEBUG_LAYOUT)
                                Log.v("WindowManager", "Preventing status bar from hiding by policy");
                        } else
                        {
                            if (DEBUG_LAYOUT)
                                Log.v("WindowManager", "** HIDING status bar");
                            if (mStatusBar.hideLw(true))
                            {
                                i = false | true;
                                mHandler.post(new Runnable() {

                                    final PhoneWindowManager this$0;

                                    public void run()
                                    {
                                        if (mStatusBarService == null)
                                            break MISSING_BLOCK_LABEL_22;
                                        mStatusBarService.collapse();
_L2:
                                        return;
                                        JVM INSTR pop ;
                                        if (true) goto _L2; else goto _L1
_L1:
                                    }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
                                }
);
                            }
                        }
                    }
                } else
                {
                    if (DEBUG_LAYOUT)
                        Log.v("WindowManager", "Showing status bar: forced");
                    if (mStatusBar.showLw(false))
                        i = false | true;
                }
            } else
            {
                if (DEBUG_LAYOUT)
                    Log.v("WindowManager", "Hiding status bar by DM_Check_Locked");
                if (mStatusBar.hideLw(true))
                    i = false | true;
            }
        }
        mTopIsFullscreen = flag;
        if (mKeyguard != null)
        {
            if (localLOGV)
                Log.v("WindowManager", (new StringBuilder()).append("finishAnimationLw::mHideKeyguard=").append(mHideLockScreen).append(" mDismissKeyguard=").append(mDismissKeyguard).append(" mKeyguardMediator.isSecure()= ").append(mKeyguardMediator.isSecure()).toString());
            if (!mDismissKeyguard || mKeyguardMediator.isSecure())
            {
                if (!mHideLockScreen)
                {
                    if (mKeyguard.showLw(false))
                        i |= 7;
                    mKeyguardMediator.setHidden(false);
                } else
                {
                    if (mKeyguard.hideLw(false))
                        i |= 7;
                    mKeyguardMediator.setHidden(true);
                }
            } else
            {
                if (mKeyguard.hideLw(false))
                    i |= 7;
                if (mKeyguardMediator.isShowing())
                    mHandler.post(new Runnable() {

                        final PhoneWindowManager this$0;

                        public void run()
                        {
                            if (!mPowerManager.isScreenOn())
                                mKeyguardMediator.keyguardDone(false, false);
                            else
                                mKeyguardMediator.keyguardDone(false, true);
                        }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
                    }
);
            }
        }
        if ((2 & updateSystemUiVisibilityLw()) != 0)
            i |= 1;
        updateLockScreenTimeout();
        return i;
    }

    public int finishLayoutLw()
    {
        return 0;
    }

    public int focusChangedLw(android.view.WindowManagerPolicy.WindowState windowstate, android.view.WindowManagerPolicy.WindowState windowstate1)
    {
        mFocusedWindow = windowstate1;
        int i;
        if ((2 & updateSystemUiVisibilityLw()) == 0)
            i = 0;
        else
            i = 1;
        return i;
    }

    public int getConfigDisplayHeight(int i, int j, int k)
    {
        int i1 = getNonDecorDisplayHeight(i, j, k);
        int l;
        if (!mStatusBarCanHide)
            l = 0;
        else
            l = mStatusBarHeight;
        return i1 - l;
    }

    public int getConfigDisplayWidth(int i, int j, int k)
    {
        return getNonDecorDisplayWidth(i, j, k);
    }

    public void getContentInsetHintLw(android.view.WindowManager.LayoutParams layoutparams, Rect rect)
    {
        if ((0x10500 & layoutparams.flags) != 0x10100)
            rect.setEmpty();
        else
            rect.set(mCurLeft, mCurTop, (mRestrictedScreenLeft + mRestrictedScreenWidth) - mCurRight, (mRestrictedScreenTop + mRestrictedScreenHeight) - mCurBottom);
    }

    public int getMaxWallpaperLayer()
    {
        return 14;
    }

    public int getNonDecorDisplayHeight(int i, int j, int k)
    {
        int l = 0;
        int i1;
        if (!mStatusBarCanHide)
            i1 = mStatusBarHeight;
        else
            i1 = 0;
        i1 = j - i1;
        if (i <= j)
            l = mNavigationBarHeight;
        return i1 - l;
    }

    public int getNonDecorDisplayWidth(int i, int j, int k)
    {
        if (i > j)
            i -= mNavigationBarWidth;
        return i;
    }

    boolean goHome()
    {
        if (SystemProperties.getInt("persist.sys.uts-test-mode", 0) != 1) goto _L2; else goto _L1
_L1:
        Log.d("WindowManager", "UTS-TEST-MODE");
_L4:
        boolean flag;
        if (ActivityManagerNative.getDefault().startActivity(null, mHomeIntent, mHomeIntent.resolveTypeIfNeeded(mContext.getContentResolver()), null, 0, null, null, 0, true, false, null, null, false) == 1)
        {
            flag = false;
            break MISSING_BLOCK_LABEL_132;
        }
        break MISSING_BLOCK_LABEL_130;
_L2:
        ActivityManagerNative.getDefault().stopAppSwitches();
        sendCloseSystemWindows();
        flag = createHomeDockIntent();
        if (flag == null) goto _L4; else goto _L3
_L3:
        flag = ActivityManagerNative.getDefault().startActivity(null, flag, flag.resolveTypeIfNeeded(mContext.getContentResolver()), null, 0, null, null, 0, true, false, null, null, false);
        if (flag != 1) goto _L4; else goto _L5
_L5:
        flag = false;
        break MISSING_BLOCK_LABEL_132;
        JVM INSTR pop ;
        flag = true;
        return flag;
    }

    void handleVolumeKey(int i, int j)
    {
        IAudioService iaudioservice = getAudioService();
        if (iaudioservice != null) goto _L2; else goto _L1
_L1:
        return;
_L2:
        int k;
        mBroadcastWakeLock.acquire();
        if (j != 24)
            break MISSING_BLOCK_LABEL_46;
        k = 1;
_L3:
        iaudioservice.adjustStreamVolume(i, k, 0);
        mBroadcastWakeLock.release();
          goto _L1
        k = -1;
          goto _L3
        Object obj;
        obj;
        Log.w("WindowManager", (new StringBuilder()).append("IAudioService.adjustStreamVolume() threw RemoteException ").append(obj).toString());
        mBroadcastWakeLock.release();
          goto _L1
        obj;
        mBroadcastWakeLock.release();
        throw obj;
    }

    public boolean hasNavigationBar()
    {
        return mHasNavigationBar;
    }

    public void hideBootMessages()
    {
        mHandler.post(new Runnable() {

            final PhoneWindowManager this$0;

            public void run()
            {
                if (mBootMsgDialog != null)
                {
                    mBootMsgDialog.dismiss();
                    mBootMsgDialog = null;
                }
            }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
        }
);
    }

    public boolean inKeyguardRestrictedKeyInputMode()
    {
        return mKeyguardMediator.isInputRestricted();
    }

    public void init(Context context, IWindowManager iwindowmanager, android.view.WindowManagerPolicy.WindowManagerFuncs windowmanagerfuncs, LocalPowerManager localpowermanager)
    {
        mContext = context;
        mWindowManager = iwindowmanager;
        mWindowManagerFuncs = windowmanagerfuncs;
        mPowerManager = localpowermanager;
        mKeyguardMediator = new KeyguardViewMediator(context, this, localpowermanager);
        mHandler = new Handler();
        mOrientationListener = new MyOrientationListener(mContext);
        Object obj;
        boolean flag;
        try
        {
            mOrientationListener.setCurrentRotation(iwindowmanager.getRotation());
        }
        catch (RemoteException _ex) { }
        (new SettingsObserver(mHandler)).observe();
        mShortcutManager = new ShortcutManager(context, mHandler);
        mShortcutManager.observe();
        mHomeIntent = new Intent("android.intent.action.MAIN", null);
        mHomeIntent.addCategory("android.intent.category.HOME");
        mHomeIntent.addFlags(0x10200000);
        mCarDockIntent = new Intent("android.intent.action.MAIN", null);
        mCarDockIntent.addCategory("android.intent.category.CAR_DOCK");
        mCarDockIntent.addFlags(0x10200000);
        mDeskDockIntent = new Intent("android.intent.action.MAIN", null);
        mDeskDockIntent.addCategory("android.intent.category.DESK_DOCK");
        mDeskDockIntent.addFlags(0x10200000);
        mBroadcastWakeLock = ((PowerManager)context.getSystemService("power")).newWakeLock(1, "PhoneWindowManager.mBroadcastWakeLock");
        mEnableShiftMenuBugReports = "1".equals(SystemProperties.get("ro.debuggable"));
        mLidOpenRotation = readRotation(0x10e000b);
        mCarDockRotation = readRotation(0x10e000d);
        mDeskDockRotation = readRotation(0x10e000c);
        mCarDockEnablesAccelerometer = mContext.getResources().getBoolean(0x1110016);
        mDeskDockEnablesAccelerometer = mContext.getResources().getBoolean(0x1110015);
        mLidKeyboardAccessibility = mContext.getResources().getInteger(0x10e0010);
        mLidNavigationAccessibility = mContext.getResources().getInteger(0x10e0011);
        obj = new IntentFilter();
        ((IntentFilter) (obj)).addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
        ((IntentFilter) (obj)).addAction(UiModeManager.ACTION_EXIT_CAR_MODE);
        ((IntentFilter) (obj)).addAction(UiModeManager.ACTION_ENTER_DESK_MODE);
        ((IntentFilter) (obj)).addAction(UiModeManager.ACTION_EXIT_DESK_MODE);
        ((IntentFilter) (obj)).addAction("android.intent.action.DOCK_EVENT");
        obj = context.registerReceiver(mDockReceiver, ((IntentFilter) (obj)));
        if (obj != null)
            mDockMode = ((Intent) (obj)).getIntExtra("android.intent.extra.DOCK_STATE", 0);
        obj = new IntentFilter();
        ((IntentFilter) (obj)).addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        ((IntentFilter) (obj)).addAction("android.intent.action.ACTION_BOOT_IPO");
        ((IntentFilter) (obj)).addAction("android.intent.action.DISABLE_POWER_KEY");
        context.registerReceiver(mKeyDispatchReceiver, ((IntentFilter) (obj)));
        obj = new IntentFilter();
        ((IntentFilter) (obj)).addAction("android.intent.action.stk.USER_ACTIVITY.enable");
        context.registerReceiver(mStkUserActivityEnReceiver, ((IntentFilter) (obj)));
        mVibrator = new Vibrator();
        mLongPressVibePattern = getLongIntArray(mContext.getResources(), 0x1070022);
        mVirtualKeyVibePattern = getLongIntArray(mContext.getResources(), 0x1070023);
        mKeyboardTapVibePattern = getLongIntArray(mContext.getResources(), 0x1070024);
        mSafeModeDisabledVibePattern = getLongIntArray(mContext.getResources(), 0x1070025);
        mSafeModeEnabledVibePattern = getLongIntArray(mContext.getResources(), 0x1070026);
        initializeHdmiState();
        if (mPowerManager.isScreenOn())
            screenTurningOn(null);
        else
            screenTurnedOff(2);
        if (1 == SystemProperties.getInt("persist.applaunchtime.enable", 0))
            flag = true;
        else
            flag = false;
        mAppLaunchTimeEnabled = flag;
    }

    void initializeHdmiState()
    {
        Object obj;
        boolean flag;
        int i;
        obj = 1;
        i = 0;
        if (!(new File("/sys/devices/virtual/switch/hdmi/state")).exists())
            break MISSING_BLOCK_LABEL_96;
        mHDMIObserver.startObserving("DEVPATH=/devices/virtual/switch/hdmi");
        flag = null;
        flag = new FileReader("/sys/class/switch/hdmi/state");
        char ac[] = new char[15];
        int j = flag.read(ac);
        if (j <= obj)
            break MISSING_BLOCK_LABEL_88;
        i = Integer.parseInt(new String(ac, 0, j + -1));
        if (i != 0)
            i = ((int) (obj));
        else
            i = 0;
        Object obj1;
        if (flag != null)
            try
            {
                flag.close();
            }
            catch (IOException _ex) { }
        if (i == 0)
            flag = ((boolean) (obj));
        else
            flag = false;
        mHdmiPlugged = flag;
        if (mHdmiPlugged)
            obj = 0;
        setHdmiPlugged(((boolean) (obj)));
        return;
        obj1;
_L6:
        Slog.w("WindowManager", (new StringBuilder()).append("Couldn't read hdmi state from /sys/class/switch/hdmi/state: ").append(obj1).toString());
        if (flag != null)
            try
            {
                flag.close();
            }
            catch (IOException _ex) { }
        break MISSING_BLOCK_LABEL_96;
        obj1;
_L4:
        Slog.w("WindowManager", (new StringBuilder()).append("Couldn't read hdmi state from /sys/class/switch/hdmi/state: ").append(obj1).toString());
        if (flag != null)
            try
            {
                flag.close();
            }
            catch (IOException _ex) { }
        break MISSING_BLOCK_LABEL_96;
        obj;
_L2:
        if (flag != null)
            try
            {
                flag.close();
            }
            catch (IOException _ex) { }
        throw obj;
        obj;
        flag = flag;
        if (true) goto _L2; else goto _L1
_L1:
        obj1;
        flag = flag;
        if (true) goto _L4; else goto _L3
_L3:
        obj1;
        flag = flag;
        if (true) goto _L6; else goto _L5
_L5:
    }

    public long interceptKeyBeforeDispatching(android.view.WindowManagerPolicy.WindowState windowstate, KeyEvent keyevent, int i)
    {
        long l;
        int j;
        boolean flag;
        boolean flag1;
        int k;
        boolean flag2;
        flag1 = keyguardOn();
        l = keyevent.getKeyCode();
        j = keyevent.getRepeatCount();
        k = keyevent.getMetaState();
        long l1 = keyevent.getFlags();
        long l2;
        if (keyevent.getAction() == 0)
            flag = true;
        else
            flag = false;
        flag2 = keyevent.isCanceled();
        if (DEBUG_INPUT)
            Log.d("WindowManager", (new StringBuilder()).append("interceptKeyTi keyCode=").append(l).append(" down=").append(flag).append(" repeatCount=").append(j).append(" keyguardOn=").append(flag1).append(" mHomePressed=").append(mHomePressed).append(" canceled=").append(flag2).toString());
        if ((l1 & 0x400) != 0) goto _L2; else goto _L1
_L1:
        if (!mVolumeDownKeyTriggered || mPowerKeyTriggered) goto _L4; else goto _L3
_L3:
        l2 = SystemClock.uptimeMillis();
        l1 = 150L + mVolumeDownKeyTime;
        if (l2 >= l1) goto _L4; else goto _L5
_L5:
        l = l1 - l2;
_L20:
        return l;
_L4:
        if (l == 25 && mVolumeDownKeyConsumedByScreenshotChord)
        {
            if (!flag)
                mVolumeDownKeyConsumedByScreenshotChord = false;
            l = -1L;
            continue; /* Loop/switch isn't completed */
        }
_L2:
        if (l != 3) goto _L7; else goto _L6
_L6:
        if (!mHomePressed || flag)
            break MISSING_BLOCK_LABEL_357;
        mHomePressed = false;
        if (flag2)
            break MISSING_BLOCK_LABEL_345;
        l = 0;
        j = getTelephonyService();
        if (j == null)
            break MISSING_BLOCK_LABEL_284;
        l = j.isRinging();
        l = l;
_L8:
        if (l != 0)
        {
            Log.i("WindowManager", "Ignoring HOME; there's a ringing incoming call.");
        } else
        {
            if (mAppLaunchTimeEnabled)
                Slog.i("WindowManager", "[AppLaunch] Home key pressed");
            launchHomeFromHotKey();
        }
_L9:
        l = -1L;
        continue; /* Loop/switch isn't completed */
        j;
        Log.w("WindowManager", "RemoteException from getPhoneInterface()", j);
          goto _L8
        Log.i("WindowManager", "Ignoring HOME; event canceled.");
          goto _L9
        if (windowstate != null)
            l = windowstate.getAttrs();
        else
            l = null;
        if (l != null)
        {
            int i1 = ((android.view.WindowManager.LayoutParams) (l)).type;
            if (i1 == 2004 || i1 == 2009)
            {
                l = 0L;
                continue; /* Loop/switch isn't completed */
            }
            k = WINDOW_TYPES_WHERE_HOME_DOESNT_WORK.length;
            int j1 = 0;
            do
            {
                if (j1 >= k)
                    break;
                if (i1 == WINDOW_TYPES_WHERE_HOME_DOESNT_WORK[j1])
                {
                    l = -1L;
                    continue; /* Loop/switch isn't completed */
                }
                j1++;
            } while (true);
            if ((0x80000000 & ((android.view.WindowManager.LayoutParams) (l)).flags) != 0)
            {
                l = 0L;
                continue; /* Loop/switch isn't completed */
            }
        }
        if (!flag) goto _L11; else goto _L10
_L10:
        if (j != 0) goto _L13; else goto _L12
_L12:
        mHomePressed = true;
_L11:
        l = -1L;
        continue; /* Loop/switch isn't completed */
_L13:
        if ((0x80 & keyevent.getFlags()) != 0 && !flag1)
            handleLongPressOnHome();
        if (true) goto _L11; else goto _L7
_L7:
        if (l == 82)
        {
            if (flag && j == 0 && mEnableShiftMenuBugReports && (k & 1) == 1)
            {
                l = new Intent("android.intent.action.BUG_REPORT");
                mContext.sendOrderedBroadcast(l, null);
                l = -1L;
                continue; /* Loop/switch isn't completed */
            }
            break MISSING_BLOCK_LABEL_682;
        }
        if (l != 84)
            break MISSING_BLOCK_LABEL_651;
        if (!flag) goto _L15; else goto _L14
_L14:
        if (j == 0)
        {
            mShortcutKeyPressed = l;
            mConsumeShortcutKeyUp = false;
        }
_L17:
        l = 0L;
        continue; /* Loop/switch isn't completed */
_L15:
        if (l != mShortcutKeyPressed) goto _L17; else goto _L16
_L16:
        mShortcutKeyPressed = -1;
        if (!mConsumeShortcutKeyUp) goto _L17; else goto _L18
_L18:
        mConsumeShortcutKeyUp = false;
        l = -1L;
        continue; /* Loop/switch isn't completed */
        if (l == 187)
        {
            if (flag && j == 0)
                showOrHideRecentAppsDialog(0);
            l = -1L;
            continue; /* Loop/switch isn't completed */
        }
        if (mShortcutKeyPressed != -1)
        {
            KeyCharacterMap keycharactermap = keyevent.getKeyCharacterMap();
            if (keycharactermap.isPrintingKey(l))
            {
                mConsumeShortcutKeyUp = true;
                if (flag && j == 0 && !flag1)
                {
                    j = mShortcutManager.getIntent(keycharactermap, l, k);
                    if (j != null)
                    {
                        j.addFlags(0x10000000);
                        try
                        {
                            mContext.startActivity(j);
                        }
                        // Misplaced declaration of an exception variable
                        catch (int j)
                        {
                            Slog.w("WindowManager", (new StringBuilder()).append("Dropping shortcut key combination because the activity to which it is registered was not found: ").append(KeyEvent.keyCodeToString(mShortcutKeyPressed)).append("+").append(KeyEvent.keyCodeToString(l)).toString(), j);
                        }
                    } else
                    {
                        Slog.i("WindowManager", (new StringBuilder()).append("Dropping unregistered shortcut key combination: ").append(KeyEvent.keyCodeToString(mShortcutKeyPressed)).append("+").append(KeyEvent.keyCodeToString(l)).toString());
                    }
                }
                l = -1L;
                continue; /* Loop/switch isn't completed */
            }
        }
        if (flag && j == 0 && (0x10000 & k) != 0)
        {
            Object obj = keyevent.getKeyCharacterMap();
            obj = mShortcutManager.getIntent(((KeyCharacterMap) (obj)), l, 0xfff8ffff & k);
            if (obj != null)
            {
                ((Intent) (obj)).addFlags(0x10000000);
                try
                {
                    mContext.startActivity(((Intent) (obj)));
                }
                // Misplaced declaration of an exception variable
                catch (int j)
                {
                    Slog.w("WindowManager", (new StringBuilder()).append("Dropping shortcut key combination because the activity to which it is registered was not found: META+").append(KeyEvent.keyCodeToString(l)).toString(), j);
                }
                l = -1L;
                continue; /* Loop/switch isn't completed */
            }
        }
        if (flag && j == 0)
        {
            String s = (String)sApplicationLaunchKeyCategories.get(l);
            if (s != null)
            {
                j = Intent.makeMainSelectorActivity("android.intent.action.MAIN", s);
                j.setFlags(0x10000000);
                try
                {
                    mContext.startActivity(j);
                }
                // Misplaced declaration of an exception variable
                catch (int j)
                {
                    Slog.w("WindowManager", (new StringBuilder()).append("Dropping application launch key because the activity to which it is registered was not found: keyCode=").append(l).append(", category=").append(s).toString(), j);
                }
                l = -1L;
                continue; /* Loop/switch isn't completed */
            }
        }
        if (flag && j == 0 && l == 61)
        {
            if (mRecentAppsDialogHeldModifiers == 0)
            {
                l = 0xffffff3e & keyevent.getModifiers();
                if (KeyEvent.metaStateHasModifiers(l, 2) || KeyEvent.metaStateHasModifiers(l, 0x10000))
                {
                    mRecentAppsDialogHeldModifiers = l;
                    showOrHideRecentAppsDialog(1);
                    l = -1L;
                    continue; /* Loop/switch isn't completed */
                }
            }
        } else
        if (!flag && mRecentAppsDialogHeldModifiers != 0 && (k & mRecentAppsDialogHeldModifiers) == 0)
        {
            mRecentAppsDialogHeldModifiers = 0;
            showOrHideRecentAppsDialog(2);
        }
        l = 0L;
        if (true) goto _L20; else goto _L19
_L19:
    }

    public int interceptKeyBeforeQueueing(KeyEvent keyevent, int i, boolean flag)
    {
        int j;
        int k;
        boolean flag1;
        ITelephony itelephony;
        RemoteException remoteexception;
        boolean flag2;
        boolean flag3;
        Object obj;
        boolean flag4;
        boolean flag5;
        if (keyevent.getAction() == 0)
            flag2 = true;
        else
            flag2 = false;
        flag3 = keyevent.isCanceled();
        k = keyevent.getKeyCode();
        obj = mKeyDispatchLock;
        obj;
        JVM INSTR monitorenter ;
        if (1 != mKeyDispatcMode) goto _L2; else goto _L1
_L1:
        j = 0;
          goto _L3
_L2:
        if (26 != k || 3 != mKeyDispatcMode) goto _L5; else goto _L4
_L4:
        j = 0;
          goto _L3
        j;
        throw j;
_L5:
        obj;
        JVM INSTR monitorexit ;
        if ((0x1000000 & i) != 0)
            j = 1;
        else
            j = 0;
        if (flag)
            flag4 = mKeyguardMediator.isShowingAndNotHidden();
        else
            flag4 = mKeyguardMediator.isShowing();
        if (mSystemBooted) goto _L7; else goto _L6
_L6:
        j = 0;
          goto _L3
_L7:
        if (DEBUG_INPUT)
            Log.d("WindowManager", (new StringBuilder()).append("interceptKeyTq keycode=").append(k).append(" screenIsOn=").append(flag).append(" keyguardActive=").append(flag4).append(" policyFlags = #").append(Integer.toHexString(i)).append(" down =").append(flag2).append(" canceled = ").append(flag3).toString());
        if (flag2 && (i & 0x100) != 0 && keyevent.getRepeatCount() == 0)
            performHapticFeedbackLw(null, 1, false);
        if (flag || j != 0)
        {
            j = 1;
        } else
        {
            j = 0;
            if ((i & 3) != 0)
                flag5 = true;
            else
                flag5 = false;
            if (flag2 && flag5)
                if (flag4)
                {
                    flag4 = mKeyguardMediator;
                    if (mDockMode != 0)
                        flag5 = true;
                    else
                        flag5 = false;
                    flag4.onWakeKeyWhenKeyguardShowingTq(k, flag5);
                } else
                {
                    j = 0 | 2;
                }
        }
        k;
        JVM INSTR lookupswitch 17: default 416
    //                   5: 419
    //                   6: 820
    //                   24: 546
    //                   25: 546
    //                   26: 947
    //                   79: 1217
    //                   85: 1188
    //                   86: 1217
    //                   87: 1217
    //                   88: 1217
    //                   89: 1217
    //                   90: 1217
    //                   91: 1217
    //                   126: 1188
    //                   127: 1188
    //                   130: 1217
    //                   164: 546;
           goto _L3 _L8 _L9 _L10 _L10 _L11 _L12 _L13 _L12 _L12 _L12 _L12 _L12 _L12 _L13 _L13 _L12 _L10
_L8:
        if (!flag2) goto _L3; else goto _L14
_L14:
        k = getTelephonyService();
        if (k == null) goto _L3; else goto _L15
_L15:
        if (!k.isRinging()) goto _L3; else goto _L16
_L16:
        Log.i("WindowManager", "interceptKeyBeforeQueueing: CALL key-down while ringing: Answer the call!");
        k.answerRingingCall();
        j &= -2;
          goto _L3
_L10:
        if (k == 25)
        {
            if (flag2)
            {
                if (flag && !mVolumeDownKeyTriggered && (0x400 & keyevent.getFlags()) == 0)
                {
                    mVolumeDownKeyTriggered = true;
                    mVolumeDownKeyTime = keyevent.getDownTime();
                    mVolumeDownKeyConsumedByScreenshotChord = false;
                    cancelPendingPowerKeyAction();
                    interceptScreenshotChord();
                }
            } else
            {
                mVolumeDownKeyTriggered = false;
                cancelPendingScreenshotChordAction();
            }
        } else
        if (k == 24)
            if (flag2)
            {
                if (flag && !mVolumeUpKeyTriggered && (0x400 & keyevent.getFlags()) == 0)
                {
                    mVolumeUpKeyTriggered = true;
                    cancelPendingPowerKeyAction();
                    cancelPendingScreenshotChordAction();
                }
            } else
            {
                mVolumeUpKeyTriggered = false;
                cancelPendingScreenshotChordAction();
            }
        if (!flag2) goto _L3; else goto _L17
_L17:
        flag2 = getTelephonyService();
        if (flag2 == null) goto _L19; else goto _L18
_L18:
        if (!flag2.isRinging()) goto _L21; else goto _L20
_L20:
        Log.i("WindowManager", "interceptKeyBeforeQueueing: VOLUME key-down while ringing: Silence ringer!");
        flag2.silenceRinger();
        j &= -2;
          goto _L3
_L21:
        if (!flag2.isOffhook() || (j & 1) != 0) goto _L19; else goto _L22
_L22:
        handleVolumeKey(0, k);
          goto _L3
        flag2;
        Log.w("WindowManager", "ITelephony threw RemoteException", flag2);
_L19:
        if (isMusicActive() && (j & 1) == 0)
            handleVolumeKey(3, k);
        else
        if (isFmActive() && (j & 1) == 0)
            handleVolumeKey(10, k);
          goto _L3
_L9:
        j &= -2;
        if (!flag2) goto _L24; else goto _L23
_L23:
        flag2 = getTelephonyService();
        k = 0;
        if (flag2 == null)
            break MISSING_BLOCK_LABEL_858;
        k = flag2.endCall();
        k = k;
_L25:
        if (!flag || k)
            k = 1;
        else
            k = 0;
        interceptPowerKeyDown(k);
          goto _L3
        flag2;
        Log.w("WindowManager", "ITelephony threw RemoteException", flag2);
          goto _L25
_L24:
        if (interceptPowerKeyUp(flag3) && ((1 & mEndcallBehavior) == 0 || !goHome()) && (2 & mEndcallBehavior) != 0)
            j = 4 | j & -3;
          goto _L3
_L11:
        j &= -2;
        if (!flag2) goto _L27; else goto _L26
_L26:
        if (flag && !mPowerKeyTriggered && (0x400 & keyevent.getFlags()) == 0)
        {
            mPowerKeyTriggered = true;
            mPowerKeyTime = keyevent.getDownTime();
            interceptScreenshotChord();
        }
        flag2 = getTelephonyService();
        flag1 = false;
        if (flag2 == null) goto _L29; else goto _L28
_L28:
        if (!flag2.isRinging()) goto _L31; else goto _L30
_L30:
        flag2.silenceRinger();
_L29:
        if (!flag || flag1 || mVolumeDownKeyTriggered || mVolumeUpKeyTriggered)
            flag1 = true;
        else
            flag1 = false;
        interceptPowerKeyDown(flag1);
          goto _L3
_L31:
        if (flag) goto _L33; else goto _L32
_L32:
        if (mScreenOffReason != 4) goto _L29; else goto _L33
_L33:
        if ((2 & mIncallPowerBehavior) == 0 || !flag2.isOffhook()) goto _L29; else goto _L34
_L34:
        flag1 = flag2.endCall();
        flag1 = flag1;
          goto _L29
        flag2;
        Log.w("WindowManager", "ITelephony threw RemoteException", flag2);
          goto _L29
_L27:
        mPowerKeyTriggered = false;
        cancelPendingScreenshotChordAction();
        if (flag3 || mPendingPowerKeyUpCanceled)
            flag1 = true;
        else
            flag1 = false;
        if (interceptPowerKeyUp(flag1))
            j = 4 | j & -3;
        mPendingPowerKeyUpCanceled = false;
          goto _L3
_L13:
        if (!flag2) goto _L12; else goto _L35
_L35:
        itelephony = getTelephonyService();
        if (itelephony == null) goto _L12; else goto _L36
_L36:
        itelephony = itelephony.isIdle();
        if (!itelephony) goto _L3; else goto _L12
_L12:
        if ((j & 1) == 0)
        {
            mBroadcastWakeLock.acquire();
            mHandler.post(new PassHeadsetKey(new KeyEvent(keyevent)));
        }
        break; /* Loop/switch isn't completed */
        remoteexception;
        Log.w("WindowManager", "ITelephony threw RemoteException", remoteexception);
        if (true) goto _L12; else goto _L3
        remoteexception;
        Log.w("WindowManager", "ITelephony threw RemoteException", remoteexception);
_L3:
        return j;
    }

    public int interceptMotionBeforeQueueingWhenScreenOff(int i)
    {
        int j = 0;
        boolean flag;
        if ((i & 3) == 0)
            flag = false;
        else
            flag = true;
        if (flag)
            if (!mKeyguardMediator.isShowing())
                j = 0 | 2;
            else
                mKeyguardMediator.onWakeMotionWhenKeyguardShowingTq();
        return j;
    }

    boolean isDeviceProvisioned()
    {
        boolean flag = false;
        if (android.provider.Settings.Secure.getInt(mContext.getContentResolver(), "device_provisioned", 0) != 0)
            flag = true;
        return flag;
    }

    boolean isFmActive()
    {
        AudioManager audiomanager = (AudioManager)mContext.getSystemService("audio");
        boolean flag;
        if (audiomanager != null)
        {
            flag = audiomanager.isFmActive();
        } else
        {
            Log.w("WindowManager", "isFmActive: couldn't get AudioManager reference");
            flag = false;
        }
        return flag;
    }

    public boolean isKeyguardLocked()
    {
        return keyguardOn();
    }

    public boolean isKeyguardSecure()
    {
        return mKeyguardMediator.isSecure();
    }

    boolean isMusicActive()
    {
        AudioManager audiomanager = (AudioManager)mContext.getSystemService("audio");
        boolean flag;
        if (audiomanager != null)
        {
            flag = audiomanager.isMusicActive();
        } else
        {
            Log.w("WindowManager", "isMusicActive: couldn't get AudioManager reference");
            flag = false;
        }
        return flag;
    }

    public boolean isScreenOnEarly()
    {
        return mScreenOnEarly;
    }

    public boolean isScreenOnFully()
    {
        return mScreenOnFully;
    }

    boolean keyguardOn()
    {
        boolean flag;
        if (!keyguardIsShowingTq() && !inKeyguardRestrictedKeyInputMode())
            flag = false;
        else
            flag = true;
        return flag;
    }

    void launchHomeFromHotKey()
    {
        if (!mKeyguardMediator.isShowingAndNotHidden())
            if (!mHideLockScreen && mKeyguardMediator.isInputRestricted())
            {
                mKeyguardMediator.verifyUnlock(new android.view.WindowManagerPolicy.OnKeyguardExitResult() {

                    final PhoneWindowManager this$0;

                    public void onKeyguardExitResult(boolean flag)
                    {
                        if (flag)
                        {
                            try
                            {
                                ActivityManagerNative.getDefault().stopAppSwitches();
                            }
                            catch (RemoteException _ex) { }
                            sendCloseSystemWindows("homekey");
                            startDockOrHome();
                        }
                    }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
                }
);
            } else
            {
                try
                {
                    ActivityManagerNative.getDefault().stopAppSwitches();
                }
                catch (RemoteException _ex) { }
                sendCloseSystemWindows("homekey");
                startDockOrHome();
            }
    }

    public void layoutWindowLw(android.view.WindowManagerPolicy.WindowState windowstate, android.view.WindowManager.LayoutParams layoutparams, android.view.WindowManagerPolicy.WindowState windowstate1)
    {
        if (windowstate != mStatusBar && windowstate != mNavigationBar)
        {
            int j = layoutparams.flags;
            int k = layoutparams.softInputMode;
            Rect rect1 = mTmpParentFrame;
            Rect rect3 = mTmpDisplayFrame;
            Rect rect = mTmpContentFrame;
            Rect rect2 = mTmpVisibleFrame;
            int j1;
            if (!mHasNavigationBar || mNavigationBar == null || !mNavigationBar.isVisibleLw())
                j1 = 0;
            else
                j1 = 1;
            if (layoutparams.type != 2011)
            {
                int l = k & 0xf0;
                if ((0x10500 & j) != 0x10100)
                {
                    if ((j & 0x100) == 0)
                    {
                        if (windowstate1 == null)
                        {
                            if (DEBUG_LAYOUT)
                                Log.v("WindowManager", (new StringBuilder()).append("layoutWindowLw(").append(layoutparams.getTitle()).append("): normal window").toString());
                            if (layoutparams.type != 2014)
                            {
                                rect1.left = mContentLeft;
                                rect1.top = mContentTop;
                                rect1.right = mContentRight;
                                rect1.bottom = mContentBottom;
                                if (l == 16)
                                {
                                    j1 = mContentLeft;
                                    rect.left = j1;
                                    rect3.left = j1;
                                    j1 = mContentTop;
                                    rect.top = j1;
                                    rect3.top = j1;
                                    j1 = mContentRight;
                                    rect.right = j1;
                                    rect3.right = j1;
                                    j1 = mContentBottom;
                                    rect.bottom = j1;
                                    rect3.bottom = j1;
                                } else
                                {
                                    j1 = mDockLeft;
                                    rect.left = j1;
                                    rect3.left = j1;
                                    j1 = mDockTop;
                                    rect.top = j1;
                                    rect3.top = j1;
                                    j1 = mDockRight;
                                    rect.right = j1;
                                    rect3.right = j1;
                                    j1 = mDockBottom;
                                    rect.bottom = j1;
                                    rect3.bottom = j1;
                                }
                                if (l == 48)
                                {
                                    rect2.set(rect);
                                } else
                                {
                                    rect2.left = mCurLeft;
                                    rect2.top = mCurTop;
                                    rect2.right = mCurRight;
                                    rect2.bottom = mCurBottom;
                                }
                            } else
                            {
                                l = mRestrictedScreenLeft;
                                rect.left = l;
                                rect3.left = l;
                                rect1.left = l;
                                l = mRestrictedScreenTop;
                                rect.top = l;
                                rect3.top = l;
                                rect1.top = l;
                                l = mRestrictedScreenLeft + mRestrictedScreenWidth;
                                rect.right = l;
                                rect3.right = l;
                                rect1.right = l;
                                l = mRestrictedScreenTop + mRestrictedScreenHeight;
                                rect.bottom = l;
                                rect3.bottom = l;
                                rect1.bottom = l;
                            }
                        } else
                        {
                            if (DEBUG_LAYOUT)
                                Log.v("WindowManager", (new StringBuilder()).append("layoutWindowLw(").append(layoutparams.getTitle()).append("): attached to ").append(windowstate1).toString());
                            setAttachedWindowFrames(windowstate, j, l, windowstate1, false, rect1, rect3, rect, rect2);
                        }
                    } else
                    {
                        if (DEBUG_LAYOUT)
                            Log.v("WindowManager", (new StringBuilder()).append("layoutWindowLw(").append(layoutparams.getTitle()).append("): IN_SCREEN").toString());
                        if (layoutparams.type != 2014 && layoutparams.type != 2017)
                        {
                            if (layoutparams.type != 2019)
                            {
                                if (layoutparams.type != 2015 && layoutparams.type != 2021 || (j & 0x400) == 0)
                                {
                                    if (layoutparams.type != 2021)
                                    {
                                        j1 = mRestrictedScreenLeft;
                                        rect.left = j1;
                                        rect3.left = j1;
                                        rect1.left = j1;
                                        j1 = mRestrictedScreenTop;
                                        rect.top = j1;
                                        rect3.top = j1;
                                        rect1.top = j1;
                                        j1 = mRestrictedScreenLeft + mRestrictedScreenWidth;
                                        rect.right = j1;
                                        rect3.right = j1;
                                        rect1.right = j1;
                                        j1 = mRestrictedScreenTop + mRestrictedScreenHeight;
                                        rect.bottom = j1;
                                        rect3.bottom = j1;
                                        rect1.bottom = j1;
                                    } else
                                    {
                                        j1 = mUnrestrictedScreenLeft;
                                        rect.left = j1;
                                        rect3.left = j1;
                                        rect1.left = j1;
                                        j1 = mUnrestrictedScreenTop;
                                        rect.top = j1;
                                        rect3.top = j1;
                                        rect1.top = j1;
                                        j1 = mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
                                        rect.right = j1;
                                        rect3.right = j1;
                                        rect1.right = j1;
                                        j1 = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
                                        rect.bottom = j1;
                                        rect3.bottom = j1;
                                        rect1.bottom = j1;
                                    }
                                } else
                                {
                                    j1 = mUnrestrictedScreenLeft;
                                    rect3.left = j1;
                                    rect1.left = j1;
                                    j1 = mUnrestrictedScreenTop;
                                    rect3.top = j1;
                                    rect1.top = j1;
                                    j1 = mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
                                    rect3.right = j1;
                                    rect1.right = j1;
                                    j1 = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
                                    rect3.bottom = j1;
                                    rect1.bottom = j1;
                                }
                            } else
                            {
                                j1 = mUnrestrictedScreenLeft;
                                rect3.left = j1;
                                rect1.left = j1;
                                j1 = mUnrestrictedScreenTop;
                                rect3.top = j1;
                                rect1.top = j1;
                                j1 = mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
                                rect3.right = j1;
                                rect1.right = j1;
                                j1 = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
                                rect3.bottom = j1;
                                rect1.bottom = j1;
                                if (DEBUG_LAYOUT)
                                {
                                    j1 = ((int) (new Object[4]));
                                    j1[0] = Integer.valueOf(rect1.left);
                                    j1[1] = Integer.valueOf(rect1.top);
                                    j1[2] = Integer.valueOf(rect1.right);
                                    j1[3] = Integer.valueOf(rect1.bottom);
                                    Log.v("WindowManager", String.format("Laying out navigation bar window: (%d,%d - %d,%d)", j1));
                                }
                            }
                        } else
                        {
                            int k1;
                            if (j1 == 0)
                                k1 = mUnrestrictedScreenLeft;
                            else
                                k1 = mDockLeft;
                            rect.left = k1;
                            rect3.left = k1;
                            rect1.left = k1;
                            k1 = mUnrestrictedScreenTop;
                            rect.top = k1;
                            rect3.top = k1;
                            rect1.top = k1;
                            if (j1 == 0)
                                k1 = mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
                            else
                                k1 = mRestrictedScreenLeft + mRestrictedScreenWidth;
                            rect.right = k1;
                            rect3.right = k1;
                            rect1.right = k1;
                            if (j1 == 0)
                                j1 = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
                            else
                                j1 = mRestrictedScreenTop + mRestrictedScreenHeight;
                            rect.bottom = j1;
                            rect3.bottom = j1;
                            rect1.bottom = j1;
                            if (DEBUG_LAYOUT)
                            {
                                j1 = ((int) (new Object[4]));
                                j1[0] = Integer.valueOf(rect1.left);
                                j1[1] = Integer.valueOf(rect1.top);
                                j1[2] = Integer.valueOf(rect1.right);
                                j1[3] = Integer.valueOf(rect1.bottom);
                                Log.v("WindowManager", String.format("Laying out IN_SCREEN status bar window: (%d,%d - %d,%d)", j1));
                            }
                        }
                        if (l == 48)
                        {
                            rect2.set(rect);
                        } else
                        {
                            rect2.left = mCurLeft;
                            rect2.top = mCurTop;
                            rect2.right = mCurRight;
                            rect2.bottom = mCurBottom;
                        }
                    }
                } else
                {
                    if (DEBUG_LAYOUT)
                        Log.v("WindowManager", (new StringBuilder()).append("layoutWindowLw(").append(layoutparams.getTitle()).append("): IN_SCREEN, INSET_DECOR, !FULLSCREEN, sim = #").append(Integer.toHexString(l)).toString());
                    if (windowstate1 == null)
                    {
                        if (layoutparams.type != 2014 && layoutparams.type != 2017)
                        {
                            j1 = mRestrictedScreenLeft;
                            rect3.left = j1;
                            rect1.left = j1;
                            j1 = mRestrictedScreenTop;
                            rect3.top = j1;
                            rect1.top = j1;
                            j1 = mRestrictedScreenLeft + mRestrictedScreenWidth;
                            rect3.right = j1;
                            rect1.right = j1;
                            j1 = mRestrictedScreenTop + mRestrictedScreenHeight;
                            rect3.bottom = j1;
                            rect1.bottom = j1;
                        } else
                        {
                            int l1;
                            if (j1 == 0)
                                l1 = mUnrestrictedScreenLeft;
                            else
                                l1 = mDockLeft;
                            rect3.left = l1;
                            rect1.left = l1;
                            l1 = mUnrestrictedScreenTop;
                            rect3.top = l1;
                            rect1.top = l1;
                            if (j1 == 0)
                                l1 = mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
                            else
                                l1 = mRestrictedScreenLeft + mRestrictedScreenWidth;
                            rect3.right = l1;
                            rect1.right = l1;
                            if (j1 == 0)
                                j1 = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
                            else
                                j1 = mRestrictedScreenTop + mRestrictedScreenHeight;
                            rect3.bottom = j1;
                            rect1.bottom = j1;
                            if (DEBUG_LAYOUT)
                            {
                                Object aobj1[] = new Object[4];
                                aobj1[0] = Integer.valueOf(rect1.left);
                                aobj1[1] = Integer.valueOf(rect1.top);
                                aobj1[2] = Integer.valueOf(rect1.right);
                                aobj1[3] = Integer.valueOf(rect1.bottom);
                                Log.v("WindowManager", String.format("Laying out status bar window: (%d,%d - %d,%d)", aobj1));
                            }
                        }
                        if (l == 16)
                        {
                            rect.left = mContentLeft;
                            rect.top = mContentTop;
                            rect.right = mContentRight;
                            rect.bottom = mContentBottom;
                        } else
                        {
                            rect.left = mDockLeft;
                            rect.top = mDockTop;
                            rect.right = mDockRight;
                            rect.bottom = mDockBottom;
                        }
                        if (l == 48)
                        {
                            rect2.set(rect);
                        } else
                        {
                            rect2.left = mCurLeft;
                            rect2.top = mCurTop;
                            rect2.right = mCurRight;
                            rect2.bottom = mCurBottom;
                        }
                        if (mStatusBarCanHide)
                        {
                            if (rect.top < mStatusBarHeight)
                                rect.top = mStatusBarHeight;
                            if (rect2.top < mStatusBarHeight)
                                rect2.top = mStatusBarHeight;
                        }
                    } else
                    {
                        setAttachedWindowFrames(windowstate, j, k, windowstate1, true, rect1, rect3, rect, rect2);
                    }
                }
            } else
            {
                int i1 = mDockLeft;
                rect2.left = i1;
                rect.left = i1;
                rect3.left = i1;
                rect1.left = i1;
                i1 = mDockTop;
                rect2.top = i1;
                rect.top = i1;
                rect3.top = i1;
                rect1.top = i1;
                i1 = mDockRight;
                rect2.right = i1;
                rect.right = i1;
                rect3.right = i1;
                rect1.right = i1;
                i1 = mDockBottom;
                rect2.bottom = i1;
                rect.bottom = i1;
                rect3.bottom = i1;
                rect1.bottom = i1;
                layoutparams.gravity = 80;
                mDockLayer = windowstate.getSurfaceLayer();
            }
            if ((j & 0x200) != 0)
            {
                rect2.top = -10000;
                rect2.left = -10000;
                rect.top = -10000;
                rect.left = -10000;
                rect3.top = -10000;
                rect3.left = -10000;
                rect2.bottom = 10000;
                rect2.right = 10000;
                rect.bottom = 10000;
                rect.right = 10000;
                rect3.bottom = 10000;
                rect3.right = 10000;
            }
            if (DEBUG_LAYOUT)
            {
                StringBuilder stringbuilder = (new StringBuilder()).append("Compute frame ").append(layoutparams.getTitle()).append(": sim=#").append(Integer.toHexString(k)).append(" attach=").append(windowstate1).append(" type=").append(layoutparams.type);
                Object aobj[] = new Object[1];
                aobj[0] = Integer.valueOf(j);
                Log.v("WindowManager", stringbuilder.append(String.format(" flags=0x%08x", aobj)).append(" pf=").append(rect1.toShortString()).append(" df=").append(rect3.toShortString()).append(" cf=").append(rect.toShortString()).append(" vf=").append(rect2.toShortString()).toString());
            }
            windowstate.computeFrameLw(rect1, rect3, rect, rect2);
            if (layoutparams.type == 2011 && !windowstate.getGivenInsetsPendingLw())
            {
                int i = windowstate.getContentFrameLw().top + windowstate.getGivenContentInsetsLw().top;
                if (mContentBottom > i)
                    mContentBottom = i;
                i = windowstate.getVisibleFrameLw().top + windowstate.getGivenVisibleInsetsLw().top;
                if (mCurBottom > i)
                    mCurBottom = i;
                if (DEBUG_LAYOUT)
                    Log.v("WindowManager", (new StringBuilder()).append("Input method: mDockBottom=").append(mDockBottom).append(" mContentBottom=").append(mContentBottom).append(" mCurBottom=").append(mCurBottom).toString());
            }
        }
    }

    public void lockNow()
    {
        mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        mHandler.removeCallbacks(mScreenLockTimeout);
        mHandler.post(mScreenLockTimeout);
    }

    boolean needSensorRunningLp()
    {
        boolean flag = true;
        if (mCurrentAppOrientation != 4 && mCurrentAppOrientation != 10 && mCurrentAppOrientation != 7 && mCurrentAppOrientation != 6 && (!mCarDockEnablesAccelerometer || mDockMode != 2) && (!mDeskDockEnablesAccelerometer || mDockMode != flag && mDockMode != 3 && mDockMode != 4) && mAccelerometerDefault == 0)
            flag = false;
        return flag;
    }

    public void notifyLidSwitchChanged(long l, boolean flag)
    {
        boolean flag1 = true;
        int i;
        if (!flag)
            i = 0;
        else
            i = ((flag1) ? 1 : 0);
        mLidOpen = i;
        updateKeyboardVisibility();
        boolean flag2 = mKeyguardMediator.doLidChangeTq(flag);
        updateRotation(flag1);
        if (!flag2)
        {
            if (!keyguardIsShowingTq())
            {
                if (!flag)
                    mPowerManager.userActivity(SystemClock.uptimeMillis(), false, 0);
                else
                    mPowerManager.userActivity(SystemClock.uptimeMillis(), false, flag1);
            } else
            if (flag)
            {
                KeyguardViewMediator keyguardviewmediator = mKeyguardMediator;
                if (mDockMode == 0)
                    flag1 = false;
                keyguardviewmediator.onWakeKeyWhenKeyguardShowingTq(26, flag1);
            }
        } else
        {
            mKeyguardMediator.pokeWakelock();
        }
    }

    public boolean performHapticFeedbackLw(android.view.WindowManagerPolicy.WindowState windowstate, int i, boolean flag)
    {
        boolean flag1;
label0:
        {
            flag1 = false;
            boolean flag2;
            if (android.provider.Settings.System.getInt(mContext.getContentResolver(), "haptic_feedback_enabled", 0) != 0)
                flag2 = false;
            else
                flag2 = true;
            if (!flag && (flag2 || mKeyguardMediator.isShowingAndNotHidden()))
                break label0;
            switch (i)
            {
            default:
                break label0;

            case 0: // '\0'
                flag1 = mLongPressVibePattern;
                break;

            case 1: // '\001'
                flag1 = mVirtualKeyVibePattern;
                break;

            case 3: // '\003'
                flag1 = mKeyboardTapVibePattern;
                break;

            case 10000: 
                flag1 = mSafeModeDisabledVibePattern;
                break;

            case 10001: 
                flag1 = mSafeModeEnabledVibePattern;
                break;
            }
            if (flag1.length != 1)
                mVibrator.vibrate(flag1, -1);
            else
                mVibrator.vibrate(flag1[0]);
            flag1 = true;
        }
        return flag1;
    }

    public int prepareAddWindowLw(android.view.WindowManagerPolicy.WindowState windowstate, android.view.WindowManager.LayoutParams layoutparams)
    {
        byte byte0;
label0:
        {
            byte0 = -7;
            switch (layoutparams.type)
            {
            default:
                break;

            case 2000: 
                mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                if (mStatusBar == null)
                {
                    mStatusBar = windowstate;
                    break;
                }
                break label0;

            case 2004: 
                if (mKeyguard == null)
                {
                    mKeyguard = windowstate;
                    break;
                }
                break label0;

            case 2014: 
                mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                mStatusBarPanels.add(windowstate);
                break;

            case 2017: 
                mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                mStatusBarPanels.add(windowstate);
                break;

            case 2019: 
                mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "PhoneWindowManager");
                mNavigationBar = windowstate;
                if (DEBUG_LAYOUT)
                    Log.i("WindowManager", (new StringBuilder()).append("NAVIGATION BAR: ").append(mNavigationBar).toString());
                break;
            }
            byte0 = 0;
        }
        return byte0;
    }

    void readLidState()
    {
        int i = mWindowManager.getSwitchState(0);
        if (i > 0)
            mLidOpen = 1;
        else
        if (i == 0)
            mLidOpen = 0;
        else
            mLidOpen = -1;
_L2:
        return;
        JVM INSTR pop ;
        if (true) goto _L2; else goto _L1
_L1:
    }

    public void removeStartingWindow(IBinder ibinder, View view)
    {
        if (localLOGV)
            Log.v("WindowManager", (new StringBuilder()).append("Removing starting window for ").append(ibinder).append(": ").append(view).toString());
        if (view != null)
            ((WindowManager)mContext.getSystemService("window")).removeView(view);
    }

    public void removeWindowLw(android.view.WindowManagerPolicy.WindowState windowstate)
    {
        if (mStatusBar != windowstate)
        {
            if (mKeyguard != windowstate)
            {
                if (mNavigationBar != windowstate)
                    mStatusBarPanels.remove(windowstate);
                else
                    mNavigationBar = null;
            } else
            {
                mKeyguard = null;
            }
        } else
        {
            mStatusBar = null;
        }
    }

    public int rotationForOrientationLw(int i, int j)
    {
        Object obj = mLock;
        obj;
        JVM INSTR monitorenter ;
        int k = mOrientationListener.getProposedRotation();
        if (k < 0)
            k = j;
        int l;
        if (mLidOpen == 1 && mLidOpenRotation >= 0)
            l = mLidOpenRotation;
        else
        if (mDockMode == 2 && (mCarDockEnablesAccelerometer || mCarDockRotation >= 0))
        {
            if (mCarDockEnablesAccelerometer)
                l = k;
            else
                l = mCarDockRotation;
        } else
        if ((mDockMode == 1 || mDockMode == 3 || mDockMode == 4) && (mDeskDockEnablesAccelerometer || mDeskDockRotation >= 0))
        {
            if (mDeskDockEnablesAccelerometer)
                l = k;
            else
                l = mDeskDockRotation;
        } else
        if (mHdmiPlugged)
            l = mHdmiRotation;
        else
        if (mAccelerometerDefault != 0 && (i == 2 || i == -1) || i == 4 || i == 10 || i == 6 || i == 7)
        {
            if (mAllowAllRotations < 0)
            {
                if (mContext.getResources().getBoolean(0x1110013))
                    l = 1;
                else
                    l = 0;
                mAllowAllRotations = l;
            }
            if (k != 2 || mAllowAllRotations == 1 || i == 10)
                l = k;
            else
                l = j;
        } else
        if (mUserRotationMode == 1)
            l = mUserRotation;
        else
            l = -1;
        if (DEBUG_ORIENTATION)
            Slog.v("WindowManager", (new StringBuilder()).append("rotationForOrientationLw(appReqQrientation = ").append(i).append(", lastOrientation = ").append(j).append(", sensorRotation = ").append(k).append(", UserRotation = ").append(mUserRotation).append(", LidOpen = ").append(mLidOpen).append(", DockMode = ").append(mDockMode).append(", DeskDockEnable = ").append(mDeskDockEnablesAccelerometer).append(", CarDockEnable = ").append(mCarDockEnablesAccelerometer).append(", HdmiPlugged = ").append(mHdmiPlugged).append(", Accelerometer = ").append(mAccelerometerDefault).append(", AllowAllRotations = ").append(mAllowAllRotations).append(")").toString());
          goto _L1
_L18:
        if (l < 0)
            l = 0;
          goto _L2
_L20:
        if (!isAnyPortrait(l)) goto _L3; else goto _L2
        k;
        throw k;
_L3:
        l = mPortraitRotation;
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L19:
        if (!isLandscapeOrSeascape(l)) goto _L5; else goto _L4
_L4:
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L5:
        l = mLandscapeRotation;
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L24:
        if (!isAnyPortrait(l)) goto _L7; else goto _L6
_L6:
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L7:
        l = mUpsideDownRotation;
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L23:
        if (!isLandscapeOrSeascape(l)) goto _L9; else goto _L8
_L8:
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L9:
        l = mSeascapeRotation;
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L21:
        if (!isLandscapeOrSeascape(l)) goto _L11; else goto _L10
_L10:
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L11:
        if (!isLandscapeOrSeascape(j)) goto _L13; else goto _L12
_L12:
        obj;
        JVM INSTR monitorexit ;
        l = j;
          goto _L2
_L13:
        l = mLandscapeRotation;
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L22:
        if (!isAnyPortrait(l)) goto _L15; else goto _L14
_L14:
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L15:
        if (!isAnyPortrait(j)) goto _L17; else goto _L16
_L16:
        obj;
        JVM INSTR monitorexit ;
        l = j;
          goto _L2
_L17:
        l = mPortraitRotation;
        obj;
        JVM INSTR monitorexit ;
          goto _L2
_L1:
        i;
        JVM INSTR tableswitch 0 9: default 211
    //                   0 468
    //                   1 436
    //                   2 211
    //                   3 211
    //                   4 211
    //                   5 211
    //                   6 543
    //                   7 584
    //                   8 518
    //                   9 493;
           goto _L18 _L19 _L20 _L18 _L18 _L18 _L18 _L21 _L22 _L23 _L24
_L2:
        return l;
        if (false)
            ;
        else
            break MISSING_BLOCK_LABEL_45;
    }

    public boolean rotationHasCompatibleMetricsLw(int i, int j)
    {
        boolean flag;
        switch (i)
        {
        case 2: // '\002'
        case 3: // '\003'
        case 4: // '\004'
        case 5: // '\005'
        default:
            flag = true;
            break;

        case 0: // '\0'
        case 6: // '\006'
        case 8: // '\b'
            flag = isLandscapeOrSeascape(j);
            break;

        case 1: // '\001'
        case 7: // '\007'
        case 9: // '\t'
            flag = isAnyPortrait(j);
            break;
        }
        return flag;
    }

    public void screenOnStartedLw()
    {
    }

    public void screenOnStoppedLw()
    {
        if (mPowerManager.isScreenOn() && !mKeyguardMediator.isShowingAndNotHidden())
        {
            long l = SystemClock.uptimeMillis();
            mPowerManager.userActivity(l, false, 0);
        }
    }

    public void screenTurnedOff(int i)
    {
        EventLog.writeEvent(0x11170, 0);
        synchronized (mLock)
        {
            mScreenOnEarly = false;
            mScreenOnFully = false;
            mScreenOffReason = i;
        }
        mKeyguardMediator.onScreenTurnedOff(i);
        synchronized (mLock)
        {
            updateOrientationListenerLp();
            updateLockScreenTimeout();
        }
        return;
        obj;
        obj1;
        JVM INSTR monitorexit ;
        throw obj;
        exception;
        obj;
        JVM INSTR monitorexit ;
        throw exception;
    }

    public void screenTurningOn(final android.view.WindowManagerPolicy.ScreenOnListener screenOnListener)
    {
        EventLog.writeEvent(0x11170, 1);
        if (screenOnListener == null) goto _L2; else goto _L1
_L1:
        mKeyguardMediator.onScreenTurnedOn(new KeyguardViewManager.ShowListener() {

            final PhoneWindowManager this$0;
            final android.view.WindowManagerPolicy.ScreenOnListener val$screenOnListener;

            public void onShown(IBinder ibinder)
            {
                if (ibinder == null) goto _L2; else goto _L1
_L1:
                mWindowManager.waitForWindowDrawn(ibinder, new android.os.IRemoteCallback.Stub() {

                    final _cls17 this$1;

                    public void sendResult(Bundle bundle)
                    {
                        Slog.i("WindowManager", "Lock screen displayed!");
                        screenOnListener.onScreenOn();
                        Object obj2 = mLock;
                        obj2;
                        JVM INSTR monitorenter ;
                        mScreenOnFully = true;
                        return;
                    }

                    
                    {
                        this$1 = _cls17.this;
                        super();
                    }
                }
);
_L4:
                return;
_L2:
                Slog.i("WindowManager", "No lock screen!");
                screenOnListener.onScreenOn();
                Object obj1 = mLock;
                obj1;
                JVM INSTR monitorenter ;
                mScreenOnFully = true;
                continue; /* Loop/switch isn't completed */
                JVM INSTR pop ;
                if (true) goto _L4; else goto _L3
_L3:
            }

            
            {
                this$0 = PhoneWindowManager.this;
                screenOnListener = screenonlistener;
                super();
            }
        }
);
_L4:
        synchronized (mLock)
        {
            mScreenOnEarly = true;
            updateOrientationListenerLp();
            updateLockScreenTimeout();
        }
        return;
_L2:
        obj = mLock;
        obj;
        JVM INSTR monitorenter ;
        mScreenOnFully = true;
        if (true) goto _L4; else goto _L3
_L3:
        exception;
        obj;
        JVM INSTR monitorexit ;
        throw exception;
    }

    public int selectAnimationLw(android.view.WindowManagerPolicy.WindowState windowstate, int i)
    {
        int j;
        if (i != 5 || !windowstate.hasAppShownWindows())
            j = 0;
        else
            j = 0x10a0011;
        return j;
    }

    void sendCloseSystemWindows()
    {
        sendCloseSystemWindows(mContext, null);
    }

    void sendCloseSystemWindows(String s)
    {
        sendCloseSystemWindows(mContext, s);
    }

    void setAttachedWindowFrames(android.view.WindowManagerPolicy.WindowState windowstate, int i, int j, android.view.WindowManagerPolicy.WindowState windowstate1, boolean flag, Rect rect, Rect rect1, 
            Rect rect2, Rect rect3)
    {
        if (windowstate.getSurfaceLayer() <= mDockLayer || windowstate1.getSurfaceLayer() >= mDockLayer)
        {
            if (j == 16)
            {
                rect2.set(windowstate1.getContentFrameLw());
                if (windowstate1.getSurfaceLayer() < mDockLayer)
                {
                    if (rect2.left < mContentLeft)
                        rect2.left = mContentLeft;
                    if (rect2.top < mContentTop)
                        rect2.top = mContentTop;
                    if (rect2.right > mContentRight)
                        rect2.right = mContentRight;
                    if (rect2.bottom > mContentBottom)
                        rect2.bottom = mContentBottom;
                }
            } else
            {
                rect2.set(windowstate1.getDisplayFrameLw());
            }
            if (flag)
                rect2 = windowstate1.getDisplayFrameLw();
            rect1.set(rect2);
            rect3.set(windowstate1.getVisibleFrameLw());
        } else
        {
            int k = mDockLeft;
            rect3.left = k;
            rect2.left = k;
            rect1.left = k;
            k = mDockTop;
            rect3.top = k;
            rect2.top = k;
            rect1.top = k;
            k = mDockRight;
            rect3.right = k;
            rect2.right = k;
            rect1.right = k;
            k = mDockBottom;
            rect3.bottom = k;
            rect2.bottom = k;
            rect1.bottom = k;
        }
        if ((i & 0x100) == 0)
            rect1 = windowstate1.getFrameLw();
        rect.set(rect1);
    }

    public void setCurrentOrientationLw(int i)
    {
        Object obj = mLock;
        obj;
        JVM INSTR monitorenter ;
        if (i != mCurrentAppOrientation)
        {
            mCurrentAppOrientation = i;
            updateOrientationListenerLp();
        }
        return;
    }

    void setHdmiPlugged(boolean flag)
    {
        if (mHdmiPlugged != flag)
        {
            mHdmiPlugged = flag;
            updateRotation(true);
            Intent intent = new Intent("android.intent.action.HDMI_PLUGGED");
            intent.addFlags(0x10000000);
            intent.putExtra("state", flag);
            mContext.sendStickyBroadcast(intent);
        }
    }

    public void setInitialDisplaySize(int i, int j)
    {
        int k = 0;
        int l;
        if (i <= j)
        {
            l = i;
            mPortraitRotation = 0;
            mUpsideDownRotation = 2;
            if (!mContext.getResources().getBoolean(0x1110014))
            {
                mLandscapeRotation = 1;
                mSeascapeRotation = 3;
            } else
            {
                mLandscapeRotation = 3;
                mSeascapeRotation = 1;
            }
        } else
        {
            l = j;
            mLandscapeRotation = 0;
            mSeascapeRotation = 2;
            if (!mContext.getResources().getBoolean(0x1110014))
            {
                mPortraitRotation = 3;
                mUpsideDownRotation = 1;
            } else
            {
                mPortraitRotation = 1;
                mUpsideDownRotation = 3;
            }
        }
        if ((l * 160) / DisplayMetrics.DENSITY_DEVICE >= 600)
            l = 0;
        else
            l = 1;
        mStatusBarCanHide = l;
        Object obj = mContext.getResources();
        int i1;
        if (!mStatusBarCanHide)
            i1 = 0x105000b;
        else
            i1 = 0x105000a;
        mStatusBarHeight = ((Resources) (obj)).getDimensionPixelSize(i1);
        mHasNavigationBar = mContext.getResources().getBoolean(0x111002e);
        obj = SystemProperties.get("qemu.hw.mainkeys");
        if (!"".equals(obj))
            if (!((String) (obj)).equals("1"))
            {
                if (((String) (obj)).equals("0"))
                    mHasNavigationBar = true;
            } else
            {
                mHasNavigationBar = false;
            }
        if (!mHasNavigationBar)
            obj = 0;
        else
            obj = mContext.getResources().getDimensionPixelSize(0x105000c);
        mNavigationBarHeight = ((int) (obj));
        if (mHasNavigationBar)
            k = mContext.getResources().getDimensionPixelSize(0x105000d);
        mNavigationBarWidth = k;
        if (!"portrait".equals(SystemProperties.get("persist.demo.hdmirotation")))
            mHdmiRotation = mLandscapeRotation;
        else
            mHdmiRotation = mPortraitRotation;
    }

    public void setRotationLw(int i)
    {
        mOrientationListener.setCurrentRotation(i);
    }

    public void setUserRotationMode(int i, int j)
    {
        ContentResolver contentresolver = mContext.getContentResolver();
        if (i != 1)
        {
            android.provider.Settings.System.putInt(contentresolver, "accelerometer_rotation", 1);
        } else
        {
            android.provider.Settings.System.putInt(contentresolver, "user_rotation", j);
            android.provider.Settings.System.putInt(contentresolver, "accelerometer_rotation", 0);
        }
    }

    public void showBootMessage(final CharSequence msg, boolean flag)
    {
        mHandler.post(new Runnable() {

            final PhoneWindowManager this$0;
            final CharSequence val$msg;

            public void run()
            {
                if (mBootMsgDialog == null)
                {
                    mBootMsgDialog = new ProgressDialog(mContext) {

                        final _cls20 this$1;

                        public boolean dispatchGenericMotionEvent(MotionEvent motionevent)
                        {
                            return true;
                        }

                        public boolean dispatchKeyEvent(KeyEvent keyevent)
                        {
                            return true;
                        }

                        public boolean dispatchKeyShortcutEvent(KeyEvent keyevent)
                        {
                            return true;
                        }

                        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityevent)
                        {
                            return true;
                        }

                        public boolean dispatchTouchEvent(MotionEvent motionevent)
                        {
                            return true;
                        }

                        public boolean dispatchTrackballEvent(MotionEvent motionevent)
                        {
                            return true;
                        }

                    
                    {
                        this$1 = _cls20.this;
                        super(context);
                    }
                    }
;
                    mBootMsgDialog.setTitle(0x10403ba);
                    mBootMsgDialog.setProgressStyle(0);
                    mBootMsgDialog.setIndeterminate(true);
                    mBootMsgDialog.getWindow().setType(2021);
                    mBootMsgDialog.getWindow().addFlags(258);
                    mBootMsgDialog.getWindow().setDimAmount(1F);
                    android.view.WindowManager.LayoutParams layoutparams = mBootMsgDialog.getWindow().getAttributes();
                    layoutparams.screenOrientation = 5;
                    mBootMsgDialog.getWindow().setAttributes(layoutparams);
                    mBootMsgDialog.setCancelable(false);
                    mBootMsgDialog.show();
                }
                mBootMsgDialog.setMessage(msg);
            }

            
            {
                this$0 = PhoneWindowManager.this;
                msg = charsequence;
                super();
            }
        }
);
    }

    void showGlobalActionsDialog()
    {
        if (mGlobalActions == null)
            mGlobalActions = new GlobalActions(mContext);
        boolean flag = mKeyguardMediator.isShowingAndNotHidden();
        mGlobalActions.showDialog(flag, isDeviceProvisioned());
        if (flag)
            mKeyguardMediator.pokeWakelock();
    }

    void showOrHideRecentAppsDialog(final int behavior)
    {
        mHandler.post(new Runnable() {

            final PhoneWindowManager this$0;
            final int val$behavior;

            public void run()
            {
                if (mRecentAppsDialog == null)
                    mRecentAppsDialog = new RecentApplicationsDialog(mContext);
                if (!mRecentAppsDialog.isShowing()) goto _L2; else goto _L1
_L1:
                behavior;
                JVM INSTR tableswitch 0 2: default 76
            //                           0 77
            //                           1 76
            //                           2 90;
                   goto _L3 _L4 _L3 _L5
_L3:
                return;
_L4:
                mRecentAppsDialog.dismiss();
                continue; /* Loop/switch isn't completed */
_L5:
                mRecentAppsDialog.dismissAndSwitch();
                continue; /* Loop/switch isn't completed */
_L2:
                switch (behavior)
                {
                case 0: // '\0'
                    mRecentAppsDialog.show();
                    // fall through

                default:
                    if (false)
                        ;
                    break;

                case 1: // '\001'
                    try
                    {
                        mWindowManager.setInTouchMode(false);
                    }
                    catch (RemoteException _ex) { }
                    mRecentAppsDialog.show();
                    break;
                }
                if (true) goto _L3; else goto _L6
_L6:
            }

            
            {
                this$0 = PhoneWindowManager.this;
                behavior = i;
                super();
            }
        }
);
    }

    void startDockOrHome()
    {
        Intent intent;
        intent = createHomeDockIntent();
        if (intent == null)
            break MISSING_BLOCK_LABEL_19;
        mContext.startActivity(intent);
_L1:
        return;
        JVM INSTR pop ;
        mContext.startActivity(mHomeIntent);
          goto _L1
    }

    public int subWindowTypeToLayerLw(int i)
    {
        byte byte0;
        switch (i)
        {
        default:
            Log.e("WindowManager", (new StringBuilder()).append("Unknown sub-window type: ").append(i).toString());
            byte0 = 0;
            break;

        case 1000: 
        case 1003: 
            byte0 = 1;
            break;

        case 1001: 
            byte0 = -2;
            break;

        case 1002: 
            byte0 = 2;
            break;

        case 1004: 
            byte0 = -1;
            break;
        }
        return byte0;
    }

    public void systemBooted()
    {
        Object obj = mLock;
        obj;
        JVM INSTR monitorenter ;
        mSystemBooted = true;
        return;
    }

    public void systemReady()
    {
        mKeyguardMediator.onSystemReady();
        SystemProperties.set("dev.bootcomplete", "1");
        Object obj = mLock;
        obj;
        JVM INSTR monitorenter ;
        updateOrientationListenerLp();
        mSystemReady = true;
        mHandler.post(new Runnable() {

            final PhoneWindowManager this$0;

            public void run()
            {
                updateSettings();
            }

            
            {
                this$0 = PhoneWindowManager.this;
                super();
            }
        }
);
        return;
    }

    void updateOrientationListenerLp()
    {
        if (mOrientationListener.canDetectOrientation())
        {
            if (localLOGV)
                Log.v("WindowManager", (new StringBuilder()).append("Screen status=").append(mScreenOnEarly).append(", current orientation=").append(mCurrentAppOrientation).append(", SensorEnabled=").append(mOrientationSensorEnabled).toString());
            boolean flag = true;
            if (mScreenOnEarly && needSensorRunningLp())
            {
                flag = false;
                if (!mOrientationSensorEnabled)
                {
                    mOrientationListener.enable();
                    if (localLOGV)
                        Log.v("WindowManager", "Enabling listeners");
                    mOrientationSensorEnabled = true;
                }
            }
            if (flag && mOrientationSensorEnabled)
            {
                mOrientationListener.disable();
                if (localLOGV)
                    Log.v("WindowManager", "Disabling listeners");
                mOrientationSensorEnabled = false;
            }
        }
    }

    void updateRotation(boolean flag)
    {
        mWindowManager.updateRotation(flag);
_L2:
        return;
        JVM INSTR pop ;
        if (true) goto _L2; else goto _L1
_L1:
    }

    public void updateSettings()
    {
        Object obj;
        PointerLocationView pointerlocationview;
        boolean flag;
        Object obj2;
        obj2 = mContext.getContentResolver();
        flag = false;
        pointerlocationview = null;
        obj = null;
        Object obj1 = mLock;
        obj1;
        JVM INSTR monitorenter ;
        int i;
        mEndcallBehavior = android.provider.Settings.System.getInt(((ContentResolver) (obj2)), "end_button_behavior", 2);
        mIncallPowerBehavior = android.provider.Settings.Secure.getInt(((ContentResolver) (obj2)), "incall_power_button_behavior", 1);
        i = android.provider.Settings.System.getInt(((ContentResolver) (obj2)), "accelerometer_rotation", 0);
        if (i != 0) goto _L2; else goto _L1
_L1:
        int j = 1;
_L10:
        MyOrientationListener myorientationlistener;
        mUserRotationMode = j;
        mUserRotation = android.provider.Settings.System.getInt(((ContentResolver) (obj2)), "user_rotation", 0);
        if (mAccelerometerDefault != i)
        {
            mAccelerometerDefault = i;
            updateOrientationListenerLp();
        }
        myorientationlistener = mOrientationListener;
        if (android.provider.Settings.System.getInt(((ContentResolver) (obj2)), "window_orientation_listener_log", 0) == 0) goto _L4; else goto _L3
_L3:
        j = 1;
_L11:
        myorientationlistener.setLogEnabled(j);
        if (!mSystemReady) goto _L6; else goto _L5
_L5:
        myorientationlistener = android.provider.Settings.System.getInt(((ContentResolver) (obj2)), "pointer_location", 0);
        if (mPointerLocationMode == myorientationlistener) goto _L6; else goto _L7
_L7:
        mPointerLocationMode = myorientationlistener;
        if (myorientationlistener == 0) goto _L9; else goto _L8
_L8:
        if (mPointerLocationView == null)
        {
            mPointerLocationView = new PointerLocationView(mContext);
            mPointerLocationView.setPrintCoords(false);
            pointerlocationview = mPointerLocationView;
        }
_L6:
        boolean flag1;
        mLockScreenTimeout = android.provider.Settings.System.getInt(((ContentResolver) (obj2)), "screen_off_timeout", 0);
        obj2 = android.provider.Settings.Secure.getString(((ContentResolver) (obj2)), "default_input_method");
        if (obj2 == null || ((String) (obj2)).length() <= 0)
            break MISSING_BLOCK_LABEL_477;
        flag1 = true;
_L12:
        if (mHasSoftInput != flag1)
        {
            mHasSoftInput = flag1;
            flag = true;
        }
        if (flag)
            updateRotation(true);
        if (pointerlocationview != null)
        {
            obj1 = new android.view.WindowManager.LayoutParams(-1, -1);
            obj1.type = 2015;
            obj1.flags = 1304;
            obj1.format = -3;
            ((android.view.WindowManager.LayoutParams) (obj1)).setTitle("PointerLocation");
            WindowManager windowmanager = (WindowManager)mContext.getSystemService("window");
            obj1.inputFeatures = 2 | ((android.view.WindowManager.LayoutParams) (obj1)).inputFeatures;
            windowmanager.addView(pointerlocationview, ((android.view.ViewGroup.LayoutParams) (obj1)));
            Exception exception;
            if (mPointerLocationInputChannel == null)
                try
                {
                    mPointerLocationInputChannel = mWindowManager.monitorInput("PointerLocationView");
                    InputQueue.registerInputChannel(mPointerLocationInputChannel, mPointerLocationInputHandler, mHandler.getLooper().getQueue());
                }
                catch (RemoteException remoteexception)
                {
                    Slog.e("WindowManager", "Could not set up input monitoring channel for PointerLocation.", remoteexception);
                }
        }
        if (obj != null)
        {
            if (mPointerLocationInputChannel != null)
            {
                InputQueue.unregisterInputChannel(mPointerLocationInputChannel);
                mPointerLocationInputChannel.dispose();
                mPointerLocationInputChannel = null;
            }
            ((WindowManager)mContext.getSystemService("window")).removeView(((View) (obj)));
        }
        return;
_L2:
        j = 0;
          goto _L10
_L4:
        j = 0;
          goto _L11
_L9:
        obj = mPointerLocationView;
        mPointerLocationView = null;
          goto _L6
        exception;
        throw exception;
        flag1 = false;
          goto _L12
    }

    public void userActivity()
    {
        synchronized (mStkLock)
        {
            if (mIsStkUserActivityEnabled)
            {
                Intent intent = new Intent("android.intent.action.stk.USER_ACTIVITY");
                mContext.sendBroadcast(intent);
            }
        }
        synchronized (mScreenLockTimeout)
        {
            if (mLockScreenTimerActive)
            {
                mHandler.removeCallbacks(mScreenLockTimeout);
                mHandler.postDelayed(mScreenLockTimeout, mLockScreenTimeout);
            }
        }
        return;
        exception;
        obj;
        JVM INSTR monitorexit ;
        throw exception;
        exception1;
        obj;
        JVM INSTR monitorexit ;
        throw exception1;
    }

    public int windowTypeToLayerLw(int i)
    {
        byte byte0 = 2;
        if (i < 1 || i > 99)
            switch (i)
            {
            default:
                Log.e("WindowManager", (new StringBuilder()).append("Unknown window type: ").append(i).toString());
                break;

            case 2000: 
                byte0 = 14;
                break;

            case 2001: 
                byte0 = 4;
                break;

            case 2002: 
                byte0 = 3;
                break;

            case 2003: 
                byte0 = 8;
                break;

            case 2004: 
                byte0 = 11;
                break;

            case 2005: 
                byte0 = 6;
                break;

            case 2006: 
                byte0 = 17;
                break;

            case 2007: 
                byte0 = 7;
                break;

            case 2008: 
                byte0 = 5;
                break;

            case 2009: 
                byte0 = 12;
                break;

            case 2010: 
                byte0 = 19;
                break;

            case 2011: 
                byte0 = 9;
                break;

            case 2012: 
                byte0 = 10;
                break;

            case 2014: 
                byte0 = 15;
                break;

            case 2015: 
                byte0 = 21;
                break;

            case 2016: 
                byte0 = 20;
                break;

            case 2017: 
                byte0 = 13;
                break;

            case 2018: 
                byte0 = 23;
                break;

            case 2019: 
                byte0 = 18;
                break;

            case 2020: 
                byte0 = 16;
                break;

            case 2021: 
                byte0 = 22;
                break;

            case 2022: 
                byte0 = 24;
                break;

            case 2023: 
                byte0 = 25;
                break;

            case 2013: 
                break;
            }
        return byte0;
    }

    static 
    {
        DEBUG = false;
        DEBUG_LAYOUT = false;
        DEBUG_FALLBACK = false;
        DEBUG_INPUT = true;
        DEBUG_ORIENTATION = false;
        sApplicationLaunchKeyCategories = new SparseArray();
        sApplicationLaunchKeyCategories.append(64, "android.intent.category.APP_BROWSER");
        sApplicationLaunchKeyCategories.append(65, "android.intent.category.APP_EMAIL");
        sApplicationLaunchKeyCategories.append(207, "android.intent.category.APP_CONTACTS");
        sApplicationLaunchKeyCategories.append(208, "android.intent.category.APP_CALENDAR");
        sApplicationLaunchKeyCategories.append(209, "android.intent.category.APP_MUSIC");
        sApplicationLaunchKeyCategories.append(210, "android.intent.category.APP_CALCULATOR");
        int ai[] = new int[2];
        ai[0] = 2003;
        ai[1] = 2010;
        WINDOW_TYPES_WHERE_HOME_DOESNT_WORK = ai;
    }


}
