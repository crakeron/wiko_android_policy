// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.*;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.android.internal.view.RootViewSurfaceTaker;
import com.android.internal.view.StandaloneActionMode;
import com.android.internal.view.menu.*;
import com.android.internal.widget.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

// Referenced classes of package com.android.internal.policy.impl:
//            PhoneWindowManager

public class PhoneWindow extends Window
    implements com.android.internal.view.menu.MenuBuilder.Callback
{
    private final class DialogMenuCallback
        implements com.android.internal.view.menu.MenuBuilder.Callback, com.android.internal.view.menu.MenuPresenter.Callback
    {

        private int mFeatureId;
        private MenuDialogHelper mSubMenuHelper;
        final PhoneWindow this$0;

        public void onCloseMenu(MenuBuilder menubuilder, boolean flag)
        {
            if (menubuilder.getRootMenu() != menubuilder)
                onCloseSubMenu(menubuilder);
            if (flag)
            {
                android.view.Window.Callback callback = getCallback();
                if (callback != null && !isDestroyed())
                    callback.onPanelClosed(mFeatureId, menubuilder);
                if (menubuilder == mContextMenu)
                    dismissContextMenu();
                if (mSubMenuHelper != null)
                {
                    mSubMenuHelper.dismiss();
                    mSubMenuHelper = null;
                }
            }
        }

        public void onCloseSubMenu(MenuBuilder menubuilder)
        {
            android.view.Window.Callback callback = getCallback();
            if (callback != null && !isDestroyed())
                callback.onPanelClosed(mFeatureId, menubuilder.getRootMenu());
        }

        public boolean onMenuItemSelected(MenuBuilder menubuilder, MenuItem menuitem)
        {
            android.view.Window.Callback callback = getCallback();
            boolean flag;
            if (callback == null || isDestroyed() || !callback.onMenuItemSelected(mFeatureId, menuitem))
                flag = false;
            else
                flag = true;
            return flag;
        }

        public void onMenuModeChange(MenuBuilder menubuilder)
        {
        }

        public boolean onOpenSubMenu(MenuBuilder menubuilder)
        {
            boolean flag;
            if (menubuilder != null)
            {
                menubuilder.setCallback(this);
                mSubMenuHelper = new MenuDialogHelper(menubuilder);
                mSubMenuHelper.show(null);
                flag = true;
            } else
            {
                flag = false;
            }
            return flag;
        }

        public DialogMenuCallback(int i)
        {
            this$0 = PhoneWindow.this;
            super();
            mFeatureId = i;
        }
    }

    static class RotationWatcher extends android.view.IRotationWatcher.Stub
    {

        private Handler mHandler;
        private boolean mIsWatching;
        private final Runnable mRotationChanged = new Runnable() {

            final RotationWatcher this$0;

            public void run()
            {
                dispatchRotationChanged();
            }

                
                {
                    this$0 = RotationWatcher.this;
                    super();
                }
        }
;
        private final ArrayList mWindows = new ArrayList();

        public void addWindow(PhoneWindow phonewindow)
        {
            ArrayList arraylist = mWindows;
            arraylist;
            JVM INSTR monitorenter ;
            boolean flag = mIsWatching;
            if (flag)
                break MISSING_BLOCK_LABEL_42;
            try
            {
                WindowManagerHolder.sWindowManager.watchRotation(this);
                mHandler = new Handler();
                mIsWatching = true;
            }
            catch (RemoteException remoteexception)
            {
                Log.e("PhoneWindow", "Couldn't start watching for device rotation", remoteexception);
            }
            mWindows.add(new WeakReference(phonewindow));
            arraylist;
            JVM INSTR monitorexit ;
            return;
            Exception exception;
            exception;
            throw exception;
        }

        void dispatchRotationChanged()
        {
            ArrayList arraylist = mWindows;
            arraylist;
            JVM INSTR monitorenter ;
            for (int i = 0; i < mWindows.size();)
            {
                PhoneWindow phonewindow = (PhoneWindow)((WeakReference)mWindows.get(i)).get();
                if (phonewindow != null)
                {
                    phonewindow.onOptionsPanelRotationChanged();
                    i++;
                } else
                {
                    mWindows.remove(i);
                }
            }

            break MISSING_BLOCK_LABEL_69;
            Exception exception;
            exception;
            throw exception;
            arraylist;
            JVM INSTR monitorexit ;
        }

        public void onRotationChanged(int i)
            throws RemoteException
        {
            mHandler.post(mRotationChanged);
        }

        public void removeWindow(PhoneWindow phonewindow)
        {
            ArrayList arraylist = mWindows;
            arraylist;
            JVM INSTR monitorenter ;
            for (int i = 0; i < mWindows.size();)
            {
                PhoneWindow phonewindow1 = (PhoneWindow)((WeakReference)mWindows.get(i)).get();
                Exception exception;
                if (phonewindow1 == null || phonewindow1 == phonewindow)
                    mWindows.remove(i);
                else
                    i++;
            }

            break MISSING_BLOCK_LABEL_74;
            exception;
            throw exception;
            arraylist;
            JVM INSTR monitorexit ;
        }

    }

    private static final class PanelFeatureState
    {
        private static class SavedState
            implements Parcelable
        {

            public static final android.os.Parcelable.Creator CREATOR = new android.os.Parcelable.Creator() {

                public SavedState createFromParcel(Parcel parcel)
                {
                    return SavedState.readFromParcel(parcel);
                }

                public volatile Object createFromParcel(Parcel parcel)
                {
                    return createFromParcel(parcel);
                }

                public SavedState[] newArray(int i)
                {
                    return new SavedState[i];
                }

                public volatile Object[] newArray(int i)
                {
                    return newArray(i);
                }

            }
;
            int featureId;
            boolean isInExpandedMode;
            boolean isOpen;
            Bundle menuState;

            private static SavedState readFromParcel(Parcel parcel)
            {
                boolean flag = true;
                SavedState savedstate = new SavedState();
                savedstate.featureId = parcel.readInt();
                boolean flag1;
                if (parcel.readInt() != flag)
                    flag1 = false;
                else
                    flag1 = flag;
                savedstate.isOpen = flag1;
                if (parcel.readInt() != flag)
                    flag = false;
                savedstate.isInExpandedMode = flag;
                if (savedstate.isOpen)
                    savedstate.menuState = parcel.readBundle();
                return savedstate;
            }

            public int describeContents()
            {
                return 0;
            }

            public void writeToParcel(Parcel parcel, int i)
            {
                int k = 1;
                parcel.writeInt(featureId);
                int j;
                if (!isOpen)
                    j = 0;
                else
                    j = k;
                parcel.writeInt(j);
                if (!isInExpandedMode)
                    k = 0;
                parcel.writeInt(k);
                if (isOpen)
                    parcel.writeBundle(menuState);
            }



            private SavedState()
            {
            }

        }


        int background;
        View createdPanelView;
        DecorView decorView;
        int featureId;
        Bundle frozenActionViewState;
        Bundle frozenMenuState;
        int fullBackground;
        int gravity;
        IconMenuPresenter iconMenuPresenter;
        boolean isCompact;
        boolean isHandled;
        boolean isInExpandedMode;
        boolean isOpen;
        boolean isPrepared;
        ListMenuPresenter listMenuPresenter;
        int listPresenterTheme;
        MenuBuilder menu;
        public boolean qwertyMode;
        boolean refreshDecorView;
        boolean refreshMenuContent;
        View shownPanelView;
        boolean wasLastExpanded;
        boolean wasLastOpen;
        int windowAnimations;
        int x;
        int y;

        void applyFrozenState()
        {
            if (menu != null && frozenMenuState != null)
            {
                menu.restorePresenterStates(frozenMenuState);
                frozenMenuState = null;
            }
        }

        public void clearMenuPresenters()
        {
            if (menu != null)
            {
                menu.removeMenuPresenter(iconMenuPresenter);
                menu.removeMenuPresenter(listMenuPresenter);
            }
            iconMenuPresenter = null;
            listMenuPresenter = null;
        }

        MenuView getIconMenuView(Context context, com.android.internal.view.menu.MenuPresenter.Callback callback)
        {
            MenuView menuview;
            if (menu != null)
            {
                if (iconMenuPresenter == null)
                {
                    iconMenuPresenter = new IconMenuPresenter(context);
                    iconMenuPresenter.setCallback(callback);
                    iconMenuPresenter.setId(0x1020229);
                    menu.addMenuPresenter(iconMenuPresenter);
                }
                menuview = iconMenuPresenter.getMenuView(decorView);
            } else
            {
                menuview = null;
            }
            return menuview;
        }

        MenuView getListMenuView(Context context, com.android.internal.view.menu.MenuPresenter.Callback callback)
        {
            MenuView menuview;
            if (menu != null)
            {
                if (!isCompact)
                    getIconMenuView(context, callback);
                if (listMenuPresenter == null)
                {
                    listMenuPresenter = new ListMenuPresenter(0x1090066, listPresenterTheme);
                    listMenuPresenter.setCallback(callback);
                    listMenuPresenter.setId(0x102022a);
                    menu.addMenuPresenter(listMenuPresenter);
                }
                if (iconMenuPresenter != null)
                    listMenuPresenter.setItemIndexOffset(iconMenuPresenter.getNumActualItemsShown());
                menuview = listMenuPresenter.getMenuView(decorView);
            } else
            {
                menuview = null;
            }
            return menuview;
        }

        public boolean hasPanelItems()
        {
            boolean flag = true;
            if (shownPanelView != null)
            {
                if (createdPanelView == null)
                    if (!isCompact && !isInExpandedMode)
                    {
                        if (((ViewGroup)shownPanelView).getChildCount() <= 0)
                            flag = false;
                    } else
                    {
                        if (listMenuPresenter.getAdapter().getCount() <= 0)
                            flag = false;
                        else
                            flag = flag;
                        flag = flag;
                    }
            } else
            {
                flag = false;
            }
            return flag;
        }

        public boolean isInListMode()
        {
            boolean flag;
            if (!isInExpandedMode && !isCompact)
                flag = false;
            else
                flag = true;
            return flag;
        }

        void onRestoreInstanceState(Parcelable parcelable)
        {
            SavedState savedstate = (SavedState)parcelable;
            featureId = savedstate.featureId;
            wasLastOpen = savedstate.isOpen;
            wasLastExpanded = savedstate.isInExpandedMode;
            frozenMenuState = savedstate.menuState;
            createdPanelView = null;
            shownPanelView = null;
            decorView = null;
        }

        Parcelable onSaveInstanceState()
        {
            SavedState savedstate = new SavedState();
            savedstate.featureId = featureId;
            savedstate.isOpen = isOpen;
            savedstate.isInExpandedMode = isInExpandedMode;
            if (menu != null)
            {
                savedstate.menuState = new Bundle();
                menu.savePresenterStates(savedstate.menuState);
            }
            return savedstate;
        }

        void setMenu(MenuBuilder menubuilder)
        {
            if (menubuilder != menu)
            {
                if (menu != null)
                {
                    menu.removeMenuPresenter(iconMenuPresenter);
                    menu.removeMenuPresenter(listMenuPresenter);
                }
                menu = menubuilder;
                if (menubuilder != null)
                {
                    if (iconMenuPresenter != null)
                        menubuilder.addMenuPresenter(iconMenuPresenter);
                    if (listMenuPresenter != null)
                        menubuilder.addMenuPresenter(listMenuPresenter);
                }
            }
        }

        void setStyle(Context context)
        {
            TypedArray typedarray = context.obtainStyledAttributes(com.android.internal.R.styleable.Theme);
            background = typedarray.getResourceId(46, 0);
            fullBackground = typedarray.getResourceId(47, 0);
            windowAnimations = typedarray.getResourceId(93, 0);
            isCompact = typedarray.getBoolean(224, false);
            listPresenterTheme = typedarray.getResourceId(226, 0x10302f4);
            typedarray.recycle();
        }

        PanelFeatureState(int i)
        {
            featureId = i;
            refreshDecorView = false;
        }
    }

    private static final class DrawableFeatureState
    {

        int alpha;
        Drawable child;
        Drawable cur;
        int curAlpha;
        Drawable def;
        final int featureId;
        Drawable local;
        int resid;
        Uri uri;

        DrawableFeatureState(int i)
        {
            alpha = 255;
            curAlpha = 255;
            featureId = i;
        }
    }

    private final class DecorView extends FrameLayout
        implements RootViewSurfaceTaker
    {
        private class ActionModeCallbackWrapper
            implements android.view.ActionMode.Callback
        {

            private android.view.ActionMode.Callback mWrapped;
            final DecorView this$1;

            public boolean onActionItemClicked(ActionMode actionmode, MenuItem menuitem)
            {
                return mWrapped.onActionItemClicked(actionmode, menuitem);
            }

            public boolean onCreateActionMode(ActionMode actionmode, Menu menu)
            {
                return mWrapped.onCreateActionMode(actionmode, menu);
            }

            public void onDestroyActionMode(ActionMode actionmode)
            {
                mWrapped.onDestroyActionMode(actionmode);
                if (mActionModePopup != null)
                {
                    removeCallbacks(mShowActionModePopup);
                    mActionModePopup.dismiss();
                } else
                if (mActionModeView != null)
                    mActionModeView.setVisibility(8);
                if (mActionModeView != null)
                    mActionModeView.removeAllViews();
                if (getCallback() != null && !isDestroyed())
                    try
                    {
                        getCallback().onActionModeFinished(mActionMode);
                    }
                    catch (AbstractMethodError _ex) { }
                mActionMode = null;
            }

            public boolean onPrepareActionMode(ActionMode actionmode, Menu menu)
            {
                return mWrapped.onPrepareActionMode(actionmode, menu);
            }

            public ActionModeCallbackWrapper(android.view.ActionMode.Callback callback)
            {
                this$1 = DecorView.this;
                super();
                mWrapped = callback;
            }
        }


        private ActionMode mActionMode;
        private PopupWindow mActionModePopup;
        private ActionBarContextView mActionModeView;
        private final Rect mBackgroundPadding = new Rect();
        private boolean mChanging;
        int mDefaultOpacity;
        private int mDownY;
        private final Rect mDrawingBounds = new Rect();
        private final int mFeatureId;
        private final Rect mFrameOffsets = new Rect();
        private final Rect mFramePadding = new Rect();
        private Drawable mMenuBackground;
        private Runnable mShowActionModePopup;
        private boolean mWatchingForMenu;
        final PhoneWindow this$0;

        private void drawableChanged()
        {
            if (!mChanging)
            {
                setPadding(mFramePadding.left + mBackgroundPadding.left, mFramePadding.top + mBackgroundPadding.top, mFramePadding.right + mBackgroundPadding.right, mFramePadding.bottom + mBackgroundPadding.bottom);
                requestLayout();
                invalidate();
                int j = -1;
                int i = getBackground();
                Drawable drawable = getForeground();
                if (i != null)
                    if (drawable != null)
                    {
                        if (mFramePadding.left > 0 || mFramePadding.top > 0 || mFramePadding.right > 0 || mFramePadding.bottom > 0)
                        {
                            j = -3;
                        } else
                        {
                            j = drawable.getOpacity();
                            i = i.getOpacity();
                            if (j != -1 && i != -1)
                            {
                                if (j != 0)
                                {
                                    if (i != 0)
                                        j = Drawable.resolveOpacity(j, i);
                                    else
                                        j = j;
                                } else
                                {
                                    j = i;
                                }
                            } else
                            {
                                j = -1;
                            }
                        }
                    } else
                    {
                        j = i.getOpacity();
                    }
                mDefaultOpacity = j;
                if (mFeatureId < 0)
                    setDefaultWindowFormat(j);
            }
        }

        private boolean isOutOfBounds(int i, int j)
        {
            boolean flag;
            if (i >= -5 && j >= -5 && i <= 5 + getWidth() && j <= 5 + getHeight())
                flag = false;
            else
                flag = true;
            return flag;
        }

        public boolean dispatchGenericMotionEvent(MotionEvent motionevent)
        {
            boolean flag = getCallback();
            if (flag == null || isDestroyed() || mFeatureId >= 0)
                flag = super.dispatchGenericMotionEvent(motionevent);
            else
                flag = flag.dispatchGenericMotionEvent(motionevent);
            return flag;
        }

        public boolean dispatchKeyEvent(KeyEvent keyevent)
        {
            boolean flag;
label0:
            {
                flag = true;
                int i = keyevent.getKeyCode();
                boolean flag1;
                if (keyevent.getAction() != 0)
                    flag1 = false;
                else
                    flag1 = flag;
                if (flag1 && keyevent.getRepeatCount() == 0 && (mPanelChordingKey > 0 && mPanelChordingKey != i && dispatchKeyShortcutEvent(keyevent) || mPreparedPanel != null && mPreparedPanel.isOpen && performPanelShortcut(mPreparedPanel, i, keyevent, 0)))
                    break label0;
                if (!isDestroyed())
                {
                    Object obj = getCallback();
                    if (obj == null || mFeatureId >= 0)
                        obj = super.dispatchKeyEvent(keyevent);
                    else
                        obj = ((android.view.Window.Callback) (obj)).dispatchKeyEvent(keyevent);
                    if (obj != 0)
                        break label0;
                }
                if (!flag1)
                    flag = onKeyUp(mFeatureId, keyevent.getKeyCode(), keyevent);
                else
                    flag = onKeyDown(mFeatureId, keyevent.getKeyCode(), keyevent);
            }
            return flag;
        }

        public boolean dispatchKeyShortcutEvent(KeyEvent keyevent)
        {
            boolean flag;
label0:
            {
                flag = true;
                if (mPreparedPanel == null || !performPanelShortcut(mPreparedPanel, keyevent.getKeyCode(), keyevent, flag))
                {
                    Object obj = getCallback();
                    if (obj == null || isDestroyed() || mFeatureId >= 0)
                        obj = super.dispatchKeyShortcutEvent(keyevent);
                    else
                        obj = ((android.view.Window.Callback) (obj)).dispatchKeyShortcutEvent(keyevent);
                    if (obj != 0)
                        break label0;
                    if (mPreparedPanel == null)
                    {
                        PanelFeatureState panelfeaturestate = getPanelState(0, flag);
                        preparePanel(panelfeaturestate, keyevent);
                        boolean flag1 = performPanelShortcut(panelfeaturestate, keyevent.getKeyCode(), keyevent, flag);
                        panelfeaturestate.isPrepared = false;
                        if (flag1)
                            break label0;
                    }
                    flag = false;
                } else
                if (mPreparedPanel != null)
                    mPreparedPanel.isHandled = flag;
            }
            return flag;
        }

        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityevent)
        {
            android.view.Window.Callback callback = getCallback();
            boolean flag;
            if (callback == null || isDestroyed() || !callback.dispatchPopulateAccessibilityEvent(accessibilityevent))
                flag = super.dispatchPopulateAccessibilityEvent(accessibilityevent);
            else
                flag = true;
            return flag;
        }

        public boolean dispatchTouchEvent(MotionEvent motionevent)
        {
            boolean flag = getCallback();
            if (flag == null || isDestroyed() || mFeatureId >= 0)
                flag = super.dispatchTouchEvent(motionevent);
            else
                flag = flag.dispatchTouchEvent(motionevent);
            return flag;
        }

        public boolean dispatchTrackballEvent(MotionEvent motionevent)
        {
            boolean flag = getCallback();
            if (flag == null || isDestroyed() || mFeatureId >= 0)
                flag = super.dispatchTrackballEvent(motionevent);
            else
                flag = flag.dispatchTrackballEvent(motionevent);
            return flag;
        }

        public void draw(Canvas canvas)
        {
            super.draw(canvas);
            if (mMenuBackground != null)
                mMenuBackground.draw(canvas);
        }

        public void finishChanging()
        {
            mChanging = false;
            drawableChanged();
        }

        protected boolean fitSystemWindows(Rect rect)
        {
            mFrameOffsets.set(rect);
            if (getForeground() != null)
                drawableChanged();
            return super.fitSystemWindows(rect);
        }

        protected void onAttachedToWindow()
        {
            super.onAttachedToWindow();
            updateWindowResizeState();
            android.view.Window.Callback callback = getCallback();
            if (callback != null && !isDestroyed() && mFeatureId < 0)
                callback.onAttachedToWindow();
            if (mFeatureId == -1)
                openPanelsAfterRestore();
        }

        public void onCloseSystemDialogs(String s)
        {
            if (mFeatureId >= 0)
                closeAllPanels();
        }

        protected void onDetachedFromWindow()
        {
            super.onDetachedFromWindow();
            Object obj = getCallback();
            if (obj != null && mFeatureId < 0)
                ((android.view.Window.Callback) (obj)).onDetachedFromWindow();
            if (mActionBar != null)
                mActionBar.dismissPopupMenus();
            if (mActionModePopup != null)
            {
                removeCallbacks(mShowActionModePopup);
                if (mActionModePopup.isShowing())
                    mActionModePopup.dismiss();
                mActionModePopup = null;
            }
            obj = getPanelState(0, false);
            if (obj != null && ((PanelFeatureState) (obj)).menu != null && mFeatureId < 0)
                ((PanelFeatureState) (obj)).menu.close();
        }

        public boolean onInterceptTouchEvent(MotionEvent motionevent)
        {
            int i = motionevent.getAction();
            if (mFeatureId < 0 || i != 0 || !isOutOfBounds((int)motionevent.getX(), (int)motionevent.getY()))
            {
                i = 0;
            } else
            {
                closePanel(mFeatureId);
                i = 1;
            }
            return i;
        }

        protected void onMeasure(int i, int j)
        {
            int i1 = getContext().getResources().getDisplayMetrics();
            boolean flag1;
            if (((DisplayMetrics) (i1)).widthPixels >= ((DisplayMetrics) (i1)).heightPixels)
                flag1 = false;
            else
                flag1 = true;
            int j1 = android.view.View.MeasureSpec.getMode(i);
            super.onMeasure(i, j);
            int l = getMeasuredWidth();
            boolean flag = false;
            int k = android.view.View.MeasureSpec.makeMeasureSpec(l, 0x40000000);
            TypedValue typedvalue;
            if (!flag1)
                typedvalue = mMinWidthMajor;
            else
                typedvalue = mMinWidthMinor;
            if (j1 == 0x80000000 && typedvalue.type != 0)
            {
                if (typedvalue.type != 5)
                {
                    if (typedvalue.type != 6)
                        i1 = 0;
                    else
                        i1 = (int)typedvalue.getFraction(((DisplayMetrics) (i1)).widthPixels, ((DisplayMetrics) (i1)).widthPixels);
                } else
                {
                    i1 = (int)typedvalue.getDimension(i1);
                }
                if (l < i1)
                {
                    k = android.view.View.MeasureSpec.makeMeasureSpec(i1, 0x40000000);
                    flag = true;
                }
            }
            if (flag)
                super.onMeasure(k, j);
        }

        public boolean onTouchEvent(MotionEvent motionevent)
        {
            return onInterceptTouchEvent(motionevent);
        }

        public void onWindowFocusChanged(boolean flag)
        {
            super.onWindowFocusChanged(flag);
            if (!flag && mPanelChordingKey != 0)
                closePanel(0);
            android.view.Window.Callback callback = getCallback();
            if (callback != null && !isDestroyed() && mFeatureId < 0)
                callback.onWindowFocusChanged(flag);
        }

        public void sendAccessibilityEvent(int i)
        {
            if (AccessibilityManager.getInstance(mContext).isEnabled())
                if (mFeatureId != 0 && mFeatureId != 6 && mFeatureId != 2 && mFeatureId != 5 || getChildCount() != 1)
                    super.sendAccessibilityEvent(i);
                else
                    getChildAt(0).sendAccessibilityEvent(i);
        }

        public void setBackgroundDrawable(Drawable drawable)
        {
            super.setBackgroundDrawable(drawable);
            if (getWindowToken() != null)
                updateWindowResizeState();
        }

        protected boolean setFrame(int i, int j, int k, int l)
        {
            boolean flag = super.setFrame(i, j, k, l);
            if (flag)
            {
                Rect rect = mDrawingBounds;
                getDrawingRect(rect);
                Object obj = getForeground();
                if (obj != null)
                {
                    Rect rect1 = mFrameOffsets;
                    rect.left = rect.left + rect1.left;
                    rect.top = rect.top + rect1.top;
                    rect.right = rect.right - rect1.right;
                    rect.bottom = rect.bottom - rect1.bottom;
                    ((Drawable) (obj)).setBounds(rect);
                    obj = mFramePadding;
                    rect.left = rect.left + (((Rect) (obj)).left - rect1.left);
                    rect.top = rect.top + (((Rect) (obj)).top - rect1.top);
                    rect.right = rect.right - (((Rect) (obj)).right - rect1.right);
                    rect.bottom = rect.bottom - (((Rect) (obj)).bottom - rect1.bottom);
                }
                Drawable drawable = getBackground();
                if (drawable != null)
                    drawable.setBounds(rect);
            }
            return flag;
        }

        public void setSurfaceFormat(int i)
        {
            setFormat(i);
        }

        public void setSurfaceKeepScreenOn(boolean flag)
        {
            if (!flag)
                clearFlags(128);
            else
                addFlags(128);
        }

        public void setSurfaceType(int i)
        {
            setType(i);
        }

        public void setWindowBackground(Drawable drawable)
        {
            if (getBackground() != drawable)
            {
                setBackgroundDrawable(drawable);
                if (drawable == null)
                    mBackgroundPadding.setEmpty();
                else
                    drawable.getPadding(mBackgroundPadding);
                drawableChanged();
            }
        }

        public void setWindowFrame(Drawable drawable)
        {
            if (getForeground() != drawable)
            {
                setForeground(drawable);
                if (drawable == null)
                    mFramePadding.setEmpty();
                else
                    drawable.getPadding(mFramePadding);
                drawableChanged();
            }
        }

        public boolean showContextMenuForChild(View view)
        {
            if (mContextMenu != null)
            {
                mContextMenu.clearAll();
            } else
            {
                mContextMenu = new ContextMenuBuilder(getContext());
                mContextMenu.setCallback(mContextMenuCallback);
            }
            MenuDialogHelper menudialoghelper = mContextMenu.show(view, view.getWindowToken());
            if (menudialoghelper != null)
                menudialoghelper.setPresenterCallback(mContextMenuCallback);
            mContextMenuHelper = menudialoghelper;
            boolean flag;
            if (menudialoghelper == null)
                flag = false;
            else
                flag = true;
            return flag;
        }

        public ActionMode startActionMode(android.view.ActionMode.Callback callback)
        {
            Object obj;
            ActionMode actionmode;
            if (mActionMode != null)
                mActionMode.finish();
            obj = new ActionModeCallbackWrapper(callback);
            actionmode = null;
            if (getCallback() == null || isDestroyed())
                break MISSING_BLOCK_LABEL_62;
            actionmode = getCallback().onWindowStartingActionMode(((android.view.ActionMode.Callback) (obj)));
            actionmode = actionmode;
_L2:
            if (actionmode != null)
            {
                mActionMode = actionmode;
            } else
            {
                if (mActionModeView == null)
                    if (isFloating())
                    {
                        mActionModeView = new ActionBarContextView(mContext);
                        mActionModePopup = new PopupWindow(mContext, null, 0x10103c3);
                        mActionModePopup.setLayoutInScreenEnabled(true);
                        mActionModePopup.setLayoutInsetDecor(true);
                        mActionModePopup.setWindowLayoutType(2);
                        mActionModePopup.setContentView(mActionModeView);
                        mActionModePopup.setWidth(-1);
                        TypedValue typedvalue = new TypedValue();
                        mContext.getTheme().resolveAttribute(0x10102eb, typedvalue, true);
                        int i = TypedValue.complexToDimensionPixelSize(typedvalue.data, mContext.getResources().getDisplayMetrics());
                        mActionModeView.setContentHeight(i);
                        mActionModePopup.setHeight(-2);
                        mShowActionModePopup = new Runnable() {

                            final DecorView this$1;

                            public void run()
                            {
                                mActionModePopup.showAtLocation(mActionModeView.getApplicationWindowToken(), 55, 0, 0);
                            }

                
                {
                    this$1 = DecorView.this;
                    super();
                }
                        }
;
                    } else
                    {
                        ViewStub viewstub = (ViewStub)findViewById(0x10202e9);
                        if (viewstub != null)
                            mActionModeView = (ActionBarContextView)viewstub.inflate();
                    }
                if (mActionModeView != null)
                {
                    mActionModeView.killMode();
                    Context context = getContext();
                    ActionBarContextView actionbarcontextview = mActionModeView;
                    boolean flag;
                    if (mActionModePopup == null)
                        flag = true;
                    else
                        flag = false;
                    obj = new StandaloneActionMode(context, actionbarcontextview, ((android.view.ActionMode.Callback) (obj)), flag);
                    if (callback.onCreateActionMode(((ActionMode) (obj)), ((ActionMode) (obj)).getMenu()))
                    {
                        ((ActionMode) (obj)).invalidate();
                        mActionModeView.initForMode(((ActionMode) (obj)));
                        mActionModeView.setVisibility(0);
                        mActionMode = ((ActionMode) (obj));
                        if (mActionModePopup != null)
                            post(mShowActionModePopup);
                        mActionModeView.sendAccessibilityEvent(32);
                    } else
                    {
                        mActionMode = null;
                    }
                }
            }
            if (mActionMode != null && getCallback() != null && !isDestroyed())
                try
                {
                    getCallback().onActionModeStarted(mActionMode);
                }
                catch (AbstractMethodError _ex) { }
            return mActionMode;
            JVM INSTR pop ;
            if (true) goto _L2; else goto _L1
_L1:
        }

        public ActionMode startActionModeForChild(View view, android.view.ActionMode.Callback callback)
        {
            return startActionMode(callback);
        }

        public void startChanging()
        {
            mChanging = true;
        }

        public boolean superDispatchGenericMotionEvent(MotionEvent motionevent)
        {
            return super.dispatchGenericMotionEvent(motionevent);
        }

        public boolean superDispatchKeyEvent(KeyEvent keyevent)
        {
            boolean flag;
label0:
            {
                int i;
label1:
                {
label2:
                    {
                        flag = true;
                        if (super.dispatchKeyEvent(keyevent))
                            break label0;
                        if (keyevent.getKeyCode() == 4)
                        {
                            i = keyevent.getAction();
                            if (mActionMode != null)
                                break label1;
                            if (mActionBar != null && mActionBar.hasExpandedActionView())
                                break label2;
                        }
                        flag = false;
                        break label0;
                    }
                    if (i == flag)
                        mActionBar.collapseActionView();
                    break label0;
                }
                if (i == flag)
                    mActionMode.finish();
            }
            return flag;
        }

        public boolean superDispatchKeyShortcutEvent(KeyEvent keyevent)
        {
            return super.dispatchKeyShortcutEvent(keyevent);
        }

        public boolean superDispatchTouchEvent(MotionEvent motionevent)
        {
            return super.dispatchTouchEvent(motionevent);
        }

        public boolean superDispatchTrackballEvent(MotionEvent motionevent)
        {
            return super.dispatchTrackballEvent(motionevent);
        }

        void updateWindowResizeState()
        {
            Drawable drawable = getBackground();
            boolean flag;
            if (drawable != null && drawable.getOpacity() == -1)
                flag = false;
            else
                flag = true;
            hackTurnOffWindowResizeAnim(flag);
        }

        public android.view.InputQueue.Callback willYouTakeTheInputQueue()
        {
            android.view.InputQueue.Callback callback;
            if (mFeatureId >= 0)
                callback = null;
            else
                callback = mTakeInputQueueCallback;
            return callback;
        }

        public android.view.SurfaceHolder.Callback2 willYouTakeTheSurface()
        {
            android.view.SurfaceHolder.Callback2 callback2;
            if (mFeatureId >= 0)
                callback2 = null;
            else
                callback2 = mTakeSurfaceCallback;
            return callback2;
        }



/*
        static ActionMode access$102(DecorView decorview, ActionMode actionmode)
        {
            decorview.mActionMode = actionmode;
            return actionmode;
        }

*/




        public DecorView(Context context, int i)
        {
            this$0 = PhoneWindow.this;
            super(context);
            mDefaultOpacity = -1;
            mFeatureId = i;
        }
    }

    private final class ActionMenuPresenterCallback
        implements com.android.internal.view.menu.MenuPresenter.Callback
    {

        final PhoneWindow this$0;

        public void onCloseMenu(MenuBuilder menubuilder, boolean flag)
        {
            checkCloseActionMenu(menubuilder);
        }

        public boolean onOpenSubMenu(MenuBuilder menubuilder)
        {
            boolean flag = getCallback();
            if (flag == null)
            {
                flag = false;
            } else
            {
                flag.onMenuOpened(8, menubuilder);
                flag = true;
            }
            return flag;
        }

        private ActionMenuPresenterCallback()
        {
            this$0 = PhoneWindow.this;
            super();
        }

    }

    private class PanelMenuPresenterCallback
        implements com.android.internal.view.menu.MenuPresenter.Callback
    {

        final PhoneWindow this$0;

        public void onCloseMenu(MenuBuilder menubuilder, boolean flag)
        {
            MenuBuilder menubuilder1 = menubuilder.getRootMenu();
            boolean flag1;
            if (menubuilder1 == menubuilder)
                flag1 = false;
            else
                flag1 = true;
            Object obj = PhoneWindow.this;
            if (flag1)
                menubuilder = menubuilder1;
            obj = ((PhoneWindow) (obj)).findMenuPanel(menubuilder);
            if (obj != null)
                if (!flag1)
                {
                    closePanel(((PanelFeatureState) (obj)), flag);
                } else
                {
                    callOnPanelClosed(((PanelFeatureState) (obj)).featureId, ((PanelFeatureState) (obj)), menubuilder1);
                    closePanel(((PanelFeatureState) (obj)), true);
                }
        }

        public boolean onOpenSubMenu(MenuBuilder menubuilder)
        {
            if (menubuilder == null && hasFeature(8))
            {
                android.view.Window.Callback callback = getCallback();
                if (callback != null && !isDestroyed())
                    callback.onMenuOpened(8, menubuilder);
            }
            return true;
        }

        private PanelMenuPresenterCallback()
        {
            this$0 = PhoneWindow.this;
            super();
        }

    }

    static class WindowManagerHolder
    {

        static final IWindowManager sWindowManager = android.view.IWindowManager.Stub.asInterface(ServiceManager.getService("window"));


    }


    private static final String ACTION_BAR_TAG = "android:ActionBar";
    private static final String FOCUSED_ID_TAG = "android:focusedViewId";
    private static final String PANELS_TAG = "android:Panels";
    private static final boolean SWEEP_OPEN_MENU = false;
    private static final String TAG = "PhoneWindow";
    private static final String VIEWS_TAG = "android:views";
    static final RotationWatcher sRotationWatcher = new RotationWatcher();
    private ActionBarView mActionBar;
    private ActionMenuPresenterCallback mActionMenuPresenterCallback;
    private boolean mAlwaysReadCloseOnTouchAttr;
    private AudioManager mAudioManager;
    private Drawable mBackgroundDrawable;
    private int mBackgroundResource;
    private ProgressBar mCircularProgressBar;
    private boolean mClosingActionMenu;
    private ViewGroup mContentParent;
    private ContextMenuBuilder mContextMenu;
    final DialogMenuCallback mContextMenuCallback = new DialogMenuCallback(6);
    private MenuDialogHelper mContextMenuHelper;
    private DecorView mDecor;
    private DrawableFeatureState mDrawables[];
    private int mFrameResource;
    private ProgressBar mHorizontalProgressBar;
    private boolean mIsFloating;
    private KeyguardManager mKeyguardManager;
    private LayoutInflater mLayoutInflater;
    private ImageView mLeftIconView;
    final TypedValue mMinWidthMajor = new TypedValue();
    final TypedValue mMinWidthMinor = new TypedValue();
    private int mPanelChordingKey;
    private PanelMenuPresenterCallback mPanelMenuPresenterCallback;
    private PanelFeatureState mPanels[];
    private PanelFeatureState mPreparedPanel;
    private ImageView mRightIconView;
    android.view.InputQueue.Callback mTakeInputQueueCallback;
    android.view.SurfaceHolder.Callback2 mTakeSurfaceCallback;
    private int mTextColor;
    private CharSequence mTitle;
    private int mTitleColor;
    private TextView mTitleView;
    private int mUiOptions;
    private int mVolumeControlStreamType;

    public PhoneWindow(Context context)
    {
        super(context);
        mBackgroundResource = 0;
        mFrameResource = 0;
        mTextColor = 0;
        mTitle = null;
        mTitleColor = 0;
        mAlwaysReadCloseOnTouchAttr = false;
        mVolumeControlStreamType = 0x80000000;
        mUiOptions = 0;
        mLayoutInflater = LayoutInflater.from(context);
    }

    private void callOnPanelClosed(int i, PanelFeatureState panelfeaturestate, Menu menu)
    {
        android.view.Window.Callback callback = getCallback();
        if (callback != null)
        {
            if (menu == null)
            {
                if (panelfeaturestate == null && i >= 0 && i < mPanels.length)
                    panelfeaturestate = mPanels[i];
                if (panelfeaturestate != null)
                    menu = panelfeaturestate.menu;
            }
            if ((panelfeaturestate == null || panelfeaturestate.isOpen) && !isDestroyed())
                callback.onPanelClosed(i, menu);
        }
    }

    private static void clearMenuViews(PanelFeatureState panelfeaturestate)
    {
        panelfeaturestate.createdPanelView = null;
        panelfeaturestate.refreshDecorView = true;
        panelfeaturestate.clearMenuPresenters();
    }

    /**
     * @deprecated Method closeContextMenu is deprecated
     */

    private void closeContextMenu()
    {
        this;
        JVM INSTR monitorenter ;
        if (mContextMenu != null)
        {
            mContextMenu.close();
            dismissContextMenu();
        }
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    /**
     * @deprecated Method dismissContextMenu is deprecated
     */

    private void dismissContextMenu()
    {
        this;
        JVM INSTR monitorenter ;
        mContextMenu = null;
        if (mContextMenuHelper != null)
        {
            mContextMenuHelper.dismiss();
            mContextMenuHelper = null;
        }
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    private ProgressBar getCircularProgressBar(boolean flag)
    {
        ProgressBar progressbar;
        if (mCircularProgressBar == null)
        {
            if (mContentParent == null && flag)
                installDecor();
            mCircularProgressBar = (ProgressBar)findViewById(0x10202ec);
            if (mCircularProgressBar != null)
                mCircularProgressBar.setVisibility(4);
            progressbar = mCircularProgressBar;
        } else
        {
            progressbar = mCircularProgressBar;
        }
        return progressbar;
    }

    private DrawableFeatureState getDrawableState(int i, boolean flag)
    {
        DrawableFeatureState drawablefeaturestate;
        if ((getFeatures() & 1 << i) != 0)
        {
            DrawableFeatureState adrawablefeaturestate[] = mDrawables;
            if (adrawablefeaturestate == null || adrawablefeaturestate.length <= i)
            {
                DrawableFeatureState adrawablefeaturestate1[] = new DrawableFeatureState[i + 1];
                if (adrawablefeaturestate != null)
                    System.arraycopy(adrawablefeaturestate, 0, adrawablefeaturestate1, 0, adrawablefeaturestate.length);
                adrawablefeaturestate = adrawablefeaturestate1;
                mDrawables = adrawablefeaturestate1;
            }
            drawablefeaturestate = adrawablefeaturestate[i];
            if (drawablefeaturestate == null)
            {
                drawablefeaturestate = new DrawableFeatureState(i);
                adrawablefeaturestate[i] = drawablefeaturestate;
            }
        } else
        {
            if (flag)
                throw new RuntimeException("The feature has not been requested");
            drawablefeaturestate = null;
        }
        return drawablefeaturestate;
    }

    private ProgressBar getHorizontalProgressBar(boolean flag)
    {
        ProgressBar progressbar;
        if (mHorizontalProgressBar == null)
        {
            if (mContentParent == null && flag)
                installDecor();
            mHorizontalProgressBar = (ProgressBar)findViewById(0x10202ed);
            if (mHorizontalProgressBar != null)
                mHorizontalProgressBar.setVisibility(4);
            progressbar = mHorizontalProgressBar;
        } else
        {
            progressbar = mHorizontalProgressBar;
        }
        return progressbar;
    }

    private KeyguardManager getKeyguardManager()
    {
        if (mKeyguardManager == null)
            mKeyguardManager = (KeyguardManager)getContext().getSystemService("keyguard");
        return mKeyguardManager;
    }

    private ImageView getLeftIconView()
    {
        ImageView imageview;
        if (mLeftIconView == null)
        {
            if (mContentParent == null)
                installDecor();
            imageview = (ImageView)findViewById(0x102021e);
            mLeftIconView = imageview;
        } else
        {
            imageview = mLeftIconView;
        }
        return imageview;
    }

    private int getOptionsPanelGravity()
    {
        int i = WindowManagerHolder.sWindowManager.getPreferredOptionsPanelGravity();
        i = i;
_L2:
        return i;
        RemoteException remoteexception;
        remoteexception;
        Log.e("PhoneWindow", "Couldn't getOptionsPanelGravity; using default", remoteexception);
        remoteexception = 81;
        if (true) goto _L2; else goto _L1
_L1:
    }

    private PanelFeatureState getPanelState(int i, boolean flag)
    {
        return getPanelState(i, flag, null);
    }

    private PanelFeatureState getPanelState(int i, boolean flag, PanelFeatureState panelfeaturestate)
    {
        PanelFeatureState panelfeaturestate1;
        if ((getFeatures() & 1 << i) != 0)
        {
            PanelFeatureState apanelfeaturestate[] = mPanels;
            if (apanelfeaturestate == null || apanelfeaturestate.length <= i)
            {
                PanelFeatureState apanelfeaturestate1[] = new PanelFeatureState[i + 1];
                if (apanelfeaturestate != null)
                    System.arraycopy(apanelfeaturestate, 0, apanelfeaturestate1, 0, apanelfeaturestate.length);
                apanelfeaturestate = apanelfeaturestate1;
                mPanels = apanelfeaturestate1;
            }
            panelfeaturestate1 = apanelfeaturestate[i];
            if (panelfeaturestate1 == null)
            {
                if (panelfeaturestate == null)
                    panelfeaturestate1 = new PanelFeatureState(i);
                else
                    panelfeaturestate1 = panelfeaturestate;
                apanelfeaturestate[i] = panelfeaturestate1;
            }
        } else
        {
            if (flag)
                throw new RuntimeException("The feature has not been requested");
            panelfeaturestate1 = null;
        }
        return panelfeaturestate1;
    }

    private ImageView getRightIconView()
    {
        ImageView imageview;
        if (mRightIconView == null)
        {
            if (mContentParent == null)
                installDecor();
            imageview = (ImageView)findViewById(0x1020220);
            mRightIconView = imageview;
        } else
        {
            imageview = mRightIconView;
        }
        return imageview;
    }

    private void hideProgressBars(ProgressBar progressbar, ProgressBar progressbar1)
    {
        int i = getLocalFeatures();
        Animation animation = AnimationUtils.loadAnimation(getContext(), 0x10a0001);
        animation.setDuration(1000L);
        if ((i & 0x20) != 0 && progressbar1.getVisibility() == 0)
        {
            progressbar1.startAnimation(animation);
            progressbar1.setVisibility(4);
        }
        if ((i & 4) != 0 && progressbar.getVisibility() == 0)
        {
            progressbar.startAnimation(animation);
            progressbar.setVisibility(4);
        }
    }

    private void installDecor()
    {
        boolean flag = true;
        if (mDecor == null)
        {
            mDecor = generateDecor();
            mDecor.setDescendantFocusability(0x40000);
            mDecor.setIsRootNamespace(flag);
        }
        if (mContentParent == null)
        {
            mContentParent = generateLayout(mDecor);
            mTitleView = (TextView)findViewById(0x1020016);
            if (mTitleView == null)
            {
                mActionBar = (ActionBarView)findViewById(0x10202ef);
                if (mActionBar != null)
                {
                    mActionBar.setWindowCallback(getCallback());
                    if (mActionBar.getTitle() == null)
                        mActionBar.setWindowTitle(mTitle);
                    int i = getLocalFeatures();
                    if ((i & 4) != 0)
                        mActionBar.initProgress();
                    if ((i & 0x20) != 0)
                        mActionBar.initIndeterminateProgress();
                    if ((1 & mUiOptions) == 0)
                        flag = false;
                    boolean flag1;
                    if (!flag)
                        flag1 = getWindowStyle().getBoolean(22, false);
                    else
                        flag1 = getContext().getResources().getBoolean(0x1110001);
                    ActionBarContainer actionbarcontainer = (ActionBarContainer)findViewById(0x10202f1);
                    if (actionbarcontainer == null)
                    {
                        if (flag1)
                            Log.e("PhoneWindow", "Requested split action bar with incompatible window decor! Ignoring request.");
                    } else
                    {
                        mActionBar.setSplitView(actionbarcontainer);
                        mActionBar.setSplitActionBar(flag1);
                        mActionBar.setSplitWhenNarrow(flag);
                        ActionBarContextView actionbarcontextview = (ActionBarContextView)findViewById(0x10202f0);
                        actionbarcontextview.setSplitView(actionbarcontainer);
                        actionbarcontextview.setSplitActionBar(flag1);
                        actionbarcontextview.setSplitWhenNarrow(flag);
                    }
                    mDecor.post(new Runnable() {

                        final PhoneWindow this$0;

                        public void run()
                        {
                            PanelFeatureState panelfeaturestate = getPanelState(0, false);
                            if (!isDestroyed() && (panelfeaturestate == null || panelfeaturestate.menu == null))
                                invalidatePanelMenu(8);
                        }

            
            {
                this$0 = PhoneWindow.this;
                super();
            }
                    }
);
                }
            } else
            if ((2 & getLocalFeatures()) == 0)
            {
                mTitleView.setText(mTitle);
            } else
            {
                View view = findViewById(0x1020221);
                if (view == null)
                    mTitleView.setVisibility(8);
                else
                    view.setVisibility(8);
                if (mContentParent instanceof FrameLayout)
                    ((FrameLayout)mContentParent).setForeground(null);
            }
        }
    }

    private boolean launchDefaultSearch()
    {
        android.view.Window.Callback callback = getCallback();
        boolean flag;
        if (callback != null && !isDestroyed())
        {
            sendCloseSystemWindows("search");
            flag = callback.onSearchRequested();
        } else
        {
            flag = false;
        }
        return flag;
    }

    private Drawable loadImageURI(Uri uri)
    {
        Drawable drawable = null;
        drawable = Drawable.createFromStream(getContext().getContentResolver().openInputStream(uri), null);
        drawable = drawable;
_L2:
        return drawable;
        JVM INSTR pop ;
        Log.w("PhoneWindow", (new StringBuilder()).append("Unable to open content: ").append(uri).toString());
        if (true) goto _L2; else goto _L1
_L1:
    }

    private void openPanel(PanelFeatureState panelfeaturestate, KeyEvent keyevent)
    {
label0:
        {
            if (panelfeaturestate.isOpen || isDestroyed())
                break label0;
            if (panelfeaturestate.featureId == 0)
            {
                Context context = getContext();
                boolean flag;
                if ((0xf & context.getResources().getConfiguration().screenLayout) != 4)
                    flag = false;
                else
                    flag = true;
                boolean flag1;
                if (context.getApplicationInfo().targetSdkVersion < 11)
                    flag1 = false;
                else
                    flag1 = true;
                if (flag && flag1)
                    break label0;
            }
            android.view.Window.Callback callback = getCallback();
            if (callback == null || callback.onMenuOpened(panelfeaturestate.featureId, panelfeaturestate.menu))
            {
                WindowManager windowmanager = getWindowManager();
                if (windowmanager == null || !preparePanel(panelfeaturestate, keyevent))
                    break label0;
                byte byte0 = -2;
                if (panelfeaturestate.decorView != null && !panelfeaturestate.refreshDecorView)
                {
                    if (panelfeaturestate.isInListMode())
                    {
                        if (panelfeaturestate.createdPanelView != null)
                        {
                            android.view.ViewGroup.LayoutParams layoutparams1 = panelfeaturestate.createdPanelView.getLayoutParams();
                            if (layoutparams1 != null && layoutparams1.width == -1)
                                byte0 = -1;
                        }
                    } else
                    {
                        byte0 = -1;
                    }
                } else
                {
                    if (panelfeaturestate.decorView != null)
                    {
                        if (panelfeaturestate.refreshDecorView && panelfeaturestate.decorView.getChildCount() > 0)
                            panelfeaturestate.decorView.removeAllViews();
                    } else
                    if (!initializePanelDecor(panelfeaturestate) || panelfeaturestate.decorView == null)
                        break label0;
                    if (!initializePanelContent(panelfeaturestate) || !panelfeaturestate.hasPanelItems())
                        break label0;
                    android.view.ViewGroup.LayoutParams layoutparams2 = panelfeaturestate.shownPanelView.getLayoutParams();
                    if (layoutparams2 == null)
                        layoutparams2 = new android.view.ViewGroup.LayoutParams(-2, -2);
                    int i;
                    if (layoutparams2.width != -1)
                    {
                        i = panelfeaturestate.background;
                    } else
                    {
                        i = panelfeaturestate.fullBackground;
                        byte0 = -1;
                    }
                    panelfeaturestate.decorView.setWindowBackground(getContext().getResources().getDrawable(i));
                    panelfeaturestate.decorView.addView(panelfeaturestate.shownPanelView, layoutparams2);
                    if (!panelfeaturestate.shownPanelView.hasFocus())
                        panelfeaturestate.shownPanelView.requestFocus();
                }
                if (panelfeaturestate.menu == null || panelfeaturestate.menu.getNonActionItems().size() > 0)
                {
                    panelfeaturestate.isOpen = true;
                    panelfeaturestate.isHandled = false;
                    android.view.WindowManager.LayoutParams layoutparams = new android.view.WindowManager.LayoutParams(byte0, -2, panelfeaturestate.x, panelfeaturestate.y, 1003, 0x821000, panelfeaturestate.decorView.mDefaultOpacity);
                    if (!panelfeaturestate.isCompact)
                    {
                        layoutparams.gravity = panelfeaturestate.gravity;
                    } else
                    {
                        layoutparams.gravity = getOptionsPanelGravity();
                        sRotationWatcher.addWindow(this);
                    }
                    layoutparams.windowAnimations = panelfeaturestate.windowAnimations;
                    windowmanager.addView(panelfeaturestate.decorView, layoutparams);
                }
            } else
            {
                closePanel(panelfeaturestate, true);
            }
        }
    }

    private void openPanelsAfterRestore()
    {
        PanelFeatureState apanelfeaturestate[] = mPanels;
        if (apanelfeaturestate == null) goto _L2; else goto _L1
_L1:
        int i = -1 + apanelfeaturestate.length;
_L5:
        if (i >= 0) goto _L3; else goto _L2
_L2:
        return;
_L3:
        PanelFeatureState panelfeaturestate = apanelfeaturestate[i];
        if (panelfeaturestate != null)
        {
            panelfeaturestate.applyFrozenState();
            if (!panelfeaturestate.isOpen && panelfeaturestate.wasLastOpen)
            {
                panelfeaturestate.isInExpandedMode = panelfeaturestate.wasLastExpanded;
                openPanel(panelfeaturestate, null);
            }
        }
        i--;
        if (true) goto _L5; else goto _L4
_L4:
    }

    private boolean performPanelShortcut(PanelFeatureState panelfeaturestate, int i, KeyEvent keyevent, int j)
    {
        boolean flag;
        if (!keyevent.isSystem() && panelfeaturestate != null)
        {
            flag = false;
            if ((panelfeaturestate.isPrepared || preparePanel(panelfeaturestate, keyevent)) && panelfeaturestate.menu != null)
                flag = panelfeaturestate.menu.performShortcut(i, keyevent, j);
            if (flag)
            {
                panelfeaturestate.isHandled = true;
                if ((j & 1) == 0 && mActionBar == null)
                    closePanel(panelfeaturestate, true);
            }
        } else
        {
            flag = false;
        }
        return flag;
    }

    private void reopenMenu(boolean flag)
    {
        if (mActionBar == null || !mActionBar.isOverflowReserved())
        {
            PanelFeatureState panelfeaturestate1 = getPanelState(0, true);
            boolean flag1;
            if (!flag)
                flag1 = panelfeaturestate1.isInExpandedMode;
            else
            if (panelfeaturestate1.isInExpandedMode)
                flag1 = false;
            else
                flag1 = true;
            panelfeaturestate1.refreshDecorView = true;
            closePanel(panelfeaturestate1, false);
            panelfeaturestate1.isInExpandedMode = flag1;
            openPanel(panelfeaturestate1, null);
        } else
        {
            android.view.Window.Callback callback = getCallback();
            if (mActionBar.isOverflowMenuShowing() && flag)
            {
                mActionBar.hideOverflowMenu();
                if (callback != null && !isDestroyed())
                    callback.onPanelClosed(8, getPanelState(0, true).menu);
            } else
            if (callback != null && !isDestroyed() && mActionBar.getVisibility() == 0)
            {
                PanelFeatureState panelfeaturestate = getPanelState(0, true);
                if (callback.onPreparePanel(0, panelfeaturestate.createdPanelView, panelfeaturestate.menu))
                {
                    callback.onMenuOpened(8, panelfeaturestate.menu);
                    mActionBar.showOverflowMenu();
                }
            }
        }
    }

    private void restorePanelState(SparseArray sparsearray)
    {
        int i = -1 + sparsearray.size();
        do
        {
            if (i < 0)
                return;
            PanelFeatureState panelfeaturestate = getPanelState(i, false);
            if (panelfeaturestate != null)
            {
                panelfeaturestate.onRestoreInstanceState((Parcelable)sparsearray.get(i));
                invalidatePanelMenu(i);
            }
            i--;
        } while (true);
    }

    private void savePanelState(SparseArray sparsearray)
    {
        PanelFeatureState apanelfeaturestate[] = mPanels;
        if (apanelfeaturestate == null) goto _L2; else goto _L1
_L1:
        int i = -1 + apanelfeaturestate.length;
_L5:
        if (i >= 0) goto _L3; else goto _L2
_L2:
        return;
_L3:
        if (apanelfeaturestate[i] != null)
            sparsearray.put(i, apanelfeaturestate[i].onSaveInstanceState());
        i--;
        if (true) goto _L5; else goto _L4
_L4:
    }

    private void showProgressBars(ProgressBar progressbar, ProgressBar progressbar1)
    {
        int i = getLocalFeatures();
        if ((i & 0x20) != 0 && progressbar1.getVisibility() == 4)
            progressbar1.setVisibility(0);
        if ((i & 4) != 0 && progressbar.getProgress() < 10000)
            progressbar.setVisibility(0);
    }

    private void updateDrawable(int i, DrawableFeatureState drawablefeaturestate, boolean flag)
    {
        if (mContentParent != null)
        {
            int j = 1 << i;
            if ((j & getFeatures()) != 0 || flag)
            {
                Drawable drawable = null;
                if (drawablefeaturestate != null)
                {
                    drawable = drawablefeaturestate.child;
                    if (drawable == null)
                        drawable = drawablefeaturestate.local;
                    if (drawable == null)
                        drawable = drawablefeaturestate.def;
                }
                if ((j & getLocalFeatures()) != 0)
                {
                    if (drawablefeaturestate != null && (drawablefeaturestate.cur != drawable || drawablefeaturestate.curAlpha != drawablefeaturestate.alpha))
                    {
                        drawablefeaturestate.cur = drawable;
                        drawablefeaturestate.curAlpha = drawablefeaturestate.alpha;
                        onDrawableChanged(i, drawable, drawablefeaturestate.alpha);
                    }
                } else
                if (getContainer() != null && (isActive() || flag))
                    getContainer().setChildDrawable(i, drawable);
            }
        }
    }

    private void updateInt(int i, int j, boolean flag)
    {
        if (mContentParent != null)
        {
            int k = 1 << i;
            if ((k & getFeatures()) != 0 || flag)
                if ((k & getLocalFeatures()) != 0)
                    onIntChanged(i, j);
                else
                if (getContainer() != null)
                    getContainer().setChildInt(i, j);
        }
    }

    private void updateProgressBars(int i)
    {
        ProgressBar progressbar1 = getCircularProgressBar(true);
        ProgressBar progressbar = getHorizontalProgressBar(true);
        int j = getLocalFeatures();
        if (i != -1)
        {
            if (i != -2)
            {
                if (i != -3)
                {
                    if (i != -4)
                    {
                        if (i < 0 || i > 10000)
                        {
                            if (20000 <= i && i <= 30000)
                            {
                                progressbar.setSecondaryProgress(i - 20000);
                                showProgressBars(progressbar, progressbar1);
                            }
                        } else
                        {
                            progressbar.setProgress(i + 0);
                            if (i >= 10000)
                                hideProgressBars(progressbar, progressbar1);
                            else
                                showProgressBars(progressbar, progressbar1);
                        }
                    } else
                    {
                        progressbar.setIndeterminate(false);
                    }
                } else
                {
                    progressbar.setIndeterminate(true);
                }
            } else
            {
                if ((j & 4) != 0)
                    progressbar.setVisibility(8);
                if ((j & 0x20) != 0)
                    progressbar1.setVisibility(8);
            }
        } else
        {
            if ((j & 4) != 0)
            {
                int k = progressbar.getProgress();
                if (!progressbar.isIndeterminate() && k >= 10000)
                    k = 4;
                else
                    k = 0;
                progressbar.setVisibility(k);
            }
            if ((j & 0x20) != 0)
                progressbar1.setVisibility(0);
        }
    }

    public void addContentView(View view, android.view.ViewGroup.LayoutParams layoutparams)
    {
        if (mContentParent == null)
            installDecor();
        mContentParent.addView(view, layoutparams);
        android.view.Window.Callback callback = getCallback();
        if (callback != null && !isDestroyed())
            callback.onContentChanged();
    }

    public void alwaysReadCloseOnTouchAttr()
    {
        mAlwaysReadCloseOnTouchAttr = true;
    }

    void checkCloseActionMenu(Menu menu)
    {
        if (!mClosingActionMenu)
        {
            mClosingActionMenu = true;
            mActionBar.dismissPopupMenus();
            android.view.Window.Callback callback = getCallback();
            if (callback != null && !isDestroyed())
                callback.onPanelClosed(8, menu);
            mClosingActionMenu = false;
        }
    }

    public final void closeAllPanels()
    {
        if (getWindowManager() == null) goto _L2; else goto _L1
_L1:
        int i;
        PanelFeatureState apanelfeaturestate[];
        int j;
        apanelfeaturestate = mPanels;
        if (apanelfeaturestate == null)
            i = 0;
        else
            i = apanelfeaturestate.length;
        j = 0;
_L6:
        if (j < i) goto _L4; else goto _L3
_L3:
        closeContextMenu();
_L2:
        return;
_L4:
        PanelFeatureState panelfeaturestate = apanelfeaturestate[j];
        if (panelfeaturestate != null)
            closePanel(panelfeaturestate, true);
        j++;
        if (true) goto _L6; else goto _L5
_L5:
    }

    public final void closePanel(int i)
    {
        if (i != 0 || mActionBar == null || !mActionBar.isOverflowReserved())
        {
            if (i != 6)
                closePanel(getPanelState(i, true), true);
            else
                closeContextMenu();
        } else
        {
            mActionBar.hideOverflowMenu();
        }
    }

    public final void closePanel(PanelFeatureState panelfeaturestate, boolean flag)
    {
        if (!flag || panelfeaturestate.featureId != 0 || mActionBar == null || !mActionBar.isOverflowMenuShowing())
        {
            WindowManager windowmanager = getWindowManager();
            if (windowmanager != null && panelfeaturestate.isOpen)
            {
                if (panelfeaturestate.decorView != null)
                {
                    windowmanager.removeView(panelfeaturestate.decorView);
                    if (panelfeaturestate.isCompact)
                        sRotationWatcher.removeWindow(this);
                }
                if (flag)
                    callOnPanelClosed(panelfeaturestate.featureId, panelfeaturestate, null);
            }
            panelfeaturestate.isPrepared = false;
            panelfeaturestate.isHandled = false;
            panelfeaturestate.isOpen = false;
            panelfeaturestate.shownPanelView = null;
            if (panelfeaturestate.isInExpandedMode)
            {
                panelfeaturestate.refreshDecorView = true;
                panelfeaturestate.isInExpandedMode = false;
            }
            if (mPreparedPanel == panelfeaturestate)
            {
                mPreparedPanel = null;
                mPanelChordingKey = 0;
            }
        } else
        {
            checkCloseActionMenu(panelfeaturestate.menu);
        }
    }

    public PanelFeatureState findMenuPanel(Menu menu)
    {
        PanelFeatureState apanelfeaturestate[] = mPanels;
        int i;
        if (apanelfeaturestate == null)
            i = 0;
        else
            i = apanelfeaturestate.length;
        int j = 0;
        PanelFeatureState panelfeaturestate;
        do
        {
            if (j >= i)
            {
                panelfeaturestate = null;
                break;
            }
            panelfeaturestate = apanelfeaturestate[j];
            if (panelfeaturestate != null && panelfeaturestate.menu == menu)
                break;
            j++;
        } while (true);
        return panelfeaturestate;
    }

    protected DecorView generateDecor()
    {
        return new DecorView(getContext(), -1);
    }

    protected ViewGroup generateLayout(DecorView decorview)
    {
        TypedArray typedarray = getWindowStyle();
        mIsFloating = typedarray.getBoolean(4, false);
        int j = 0x10100 & (-1 ^ getForcedWindowFlags());
        if (!mIsFloating)
        {
            setFlags(0x10100, j);
        } else
        {
            setLayout(-2, -2);
            setFlags(0, j);
        }
        if (!typedarray.getBoolean(3, false))
        {
            if (typedarray.getBoolean(15, false))
                requestFeature(8);
        } else
        {
            requestFeature(1);
        }
        if (typedarray.getBoolean(17, false))
            requestFeature(9);
        if (typedarray.getBoolean(16, false))
            requestFeature(10);
        if (typedarray.getBoolean(9, false))
            setFlags(1024, 0x400 & (-1 ^ getForcedWindowFlags()));
        if (typedarray.getBoolean(14, false))
            setFlags(0x100000, 0x100000 & (-1 ^ getForcedWindowFlags()));
        if (getContext().getApplicationInfo().targetSdkVersion < 11)
            j = 0;
        else
            j = 1;
        if (typedarray.getBoolean(18, j))
            setFlags(0x800000, 0x800000 & (-1 ^ getForcedWindowFlags()));
        typedarray.getValue(19, mMinWidthMajor);
        typedarray.getValue(20, mMinWidthMinor);
        Context context = getContext();
        int k = context.getApplicationInfo().targetSdkVersion;
        boolean flag;
        if (k >= 11)
            flag = false;
        else
            flag = true;
        if (k >= 14)
            k = 0;
        else
            k = 1;
        boolean flag2 = context.getResources().getBoolean(0x1110005);
        boolean flag1;
        if (hasFeature(8) && !hasFeature(1))
            flag1 = false;
        else
            flag1 = true;
        if (!flag && (!k || !flag2 || !flag1))
            clearFlags(0x8000000);
        else
            addFlags(0x8000000);
        if ((mAlwaysReadCloseOnTouchAttr || getContext().getApplicationInfo().targetSdkVersion >= 11) && typedarray.getBoolean(21, false))
            setCloseOnTouchOutsideIfNotSet(true);
        Object obj = getAttributes();
        if (!hasSoftInputMode())
            obj.softInputMode = typedarray.getInt(13, ((android.view.WindowManager.LayoutParams) (obj)).softInputMode);
        if (typedarray.getBoolean(11, mIsFloating))
        {
            if ((2 & getForcedWindowFlags()) == 0)
                obj.flags = 2 | ((android.view.WindowManager.LayoutParams) (obj)).flags;
            if (!haveDimAmount())
                obj.dimAmount = typedarray.getFloat(0, 0.5F);
        }
        if (((android.view.WindowManager.LayoutParams) (obj)).windowAnimations == 0)
            obj.windowAnimations = typedarray.getResourceId(8, 0);
        if (getContainer() == null)
        {
            if (mBackgroundDrawable == null)
            {
                if (mBackgroundResource == 0)
                    mBackgroundResource = typedarray.getResourceId(1, 0);
                if (mFrameResource == 0)
                    mFrameResource = typedarray.getResourceId(2, 0);
            }
            mTextColor = typedarray.getColor(7, 0xff000000);
        }
        int i = getLocalFeatures();
        if ((i & 0x18) == 0)
        {
            if ((i & 0x24) == 0 || (i & 0x100) != 0)
            {
                if ((i & 0x80) == 0)
                {
                    if ((i & 2) != 0)
                    {
                        if ((i & 0x400) == 0)
                            obj = 0x109008c;
                        else
                            obj = 0x109008d;
                    } else
                    if (!mIsFloating)
                    {
                        if ((i & 0x100) == 0)
                            obj = 0x109008e;
                        else
                        if ((i & 0x200) == 0)
                            obj = 0x1090088;
                        else
                            obj = 0x1090089;
                    } else
                    {
                        obj = new TypedValue();
                        getContext().getTheme().resolveAttribute(0x10103c8, ((TypedValue) (obj)), true);
                        obj = ((TypedValue) (obj)).resourceId;
                    }
                } else
                {
                    if (!mIsFloating)
                    {
                        obj = 0x109008a;
                    } else
                    {
                        obj = new TypedValue();
                        getContext().getTheme().resolveAttribute(0x10103c7, ((TypedValue) (obj)), true);
                        obj = ((TypedValue) (obj)).resourceId;
                    }
                    removeFeature(8);
                }
            } else
            {
                obj = 0x109008b;
            }
        } else
        {
            if (!mIsFloating)
            {
                obj = 0x109008f;
            } else
            {
                obj = new TypedValue();
                getContext().getTheme().resolveAttribute(0x10103c6, ((TypedValue) (obj)), true);
                obj = ((TypedValue) (obj)).resourceId;
            }
            removeFeature(8);
        }
        mDecor.startChanging();
        decorview.addView(mLayoutInflater.inflate(((int) (obj)), null), new android.view.ViewGroup.LayoutParams(-1, -1));
        obj = (ViewGroup)findViewById(0x1020002);
        if (obj != null)
        {
            if ((i & 0x20) != 0)
            {
                ProgressBar progressbar = getCircularProgressBar(false);
                if (progressbar != null)
                    progressbar.setIndeterminate(true);
            }
            if (getContainer() == null)
            {
                Drawable drawable = mBackgroundDrawable;
                if (mBackgroundResource != 0)
                    drawable = getContext().getResources().getDrawable(mBackgroundResource);
                mDecor.setWindowBackground(drawable);
                drawable = null;
                if (mFrameResource != 0)
                    drawable = getContext().getResources().getDrawable(mFrameResource);
                mDecor.setWindowFrame(drawable);
                if (mTitleColor == 0)
                    mTitleColor = mTextColor;
                if (mTitle != null)
                    setTitle(mTitle);
                setTitleColor(mTitleColor);
            }
            mDecor.finishChanging();
            return ((ViewGroup) (obj));
        } else
        {
            throw new RuntimeException("Window couldn't find content container view");
        }
    }

    AudioManager getAudioManager()
    {
        if (mAudioManager == null)
            mAudioManager = (AudioManager)getContext().getSystemService("audio");
        return mAudioManager;
    }

    public View getCurrentFocus()
    {
        View view;
        if (mDecor == null)
            view = null;
        else
            view = mDecor.findFocus();
        return view;
    }

    public final View getDecorView()
    {
        if (mDecor == null)
            installDecor();
        return mDecor;
    }

    public LayoutInflater getLayoutInflater()
    {
        return mLayoutInflater;
    }

    public int getVolumeControlStream()
    {
        return mVolumeControlStreamType;
    }

    protected boolean initializePanelContent(PanelFeatureState panelfeaturestate)
    {
        boolean flag;
        if (panelfeaturestate.createdPanelView == null)
        {
            if (panelfeaturestate.menu != null)
            {
                if (mPanelMenuPresenterCallback == null)
                    mPanelMenuPresenterCallback = new PanelMenuPresenterCallback();
                if (!panelfeaturestate.isInListMode())
                    flag = panelfeaturestate.getIconMenuView(getContext(), mPanelMenuPresenterCallback);
                else
                    flag = panelfeaturestate.getListMenuView(getContext(), mPanelMenuPresenterCallback);
                panelfeaturestate.shownPanelView = (View)flag;
                if (panelfeaturestate.shownPanelView == null)
                {
                    flag = false;
                } else
                {
                    flag = flag.getWindowAnimations();
                    if (flag)
                        panelfeaturestate.windowAnimations = ((flag) ? 1 : 0);
                    flag = true;
                }
            } else
            {
                flag = false;
            }
        } else
        {
            panelfeaturestate.shownPanelView = panelfeaturestate.createdPanelView;
            flag = true;
        }
        return flag;
    }

    protected boolean initializePanelDecor(PanelFeatureState panelfeaturestate)
    {
        panelfeaturestate.decorView = new DecorView(getContext(), panelfeaturestate.featureId);
        panelfeaturestate.gravity = 81;
        panelfeaturestate.setStyle(getContext());
        return true;
    }

    protected boolean initializePanelMenu(PanelFeatureState panelfeaturestate)
    {
        Object obj = getContext();
        if ((panelfeaturestate.featureId == 0 || panelfeaturestate.featureId == 8) && mActionBar != null)
        {
            TypedValue typedvalue = new TypedValue();
            ((Context) (obj)).getTheme().resolveAttribute(0x1010397, typedvalue, true);
            int i = typedvalue.resourceId;
            if (i != 0 && ((Context) (obj)).getThemeResId() != i)
                obj = new ContextThemeWrapper(((Context) (obj)), i);
        }
        obj = new MenuBuilder(((Context) (obj)));
        ((MenuBuilder) (obj)).setCallback(this);
        panelfeaturestate.setMenu(((MenuBuilder) (obj)));
        return true;
    }

    public void invalidatePanelMenu(int i)
    {
        PanelFeatureState panelfeaturestate = getPanelState(i, true);
        if (panelfeaturestate.menu != null)
        {
            Bundle bundle = new Bundle();
            panelfeaturestate.menu.saveActionViewStates(bundle);
            if (bundle.size() > 0)
                panelfeaturestate.frozenActionViewState = bundle;
            panelfeaturestate.menu.stopDispatchingItemsChanged();
            panelfeaturestate.menu.clear();
        }
        panelfeaturestate.refreshMenuContent = true;
        panelfeaturestate.refreshDecorView = true;
        if ((i == 8 || i == 0) && mActionBar != null)
        {
            PanelFeatureState panelfeaturestate1 = getPanelState(0, false);
            if (panelfeaturestate1 != null)
            {
                panelfeaturestate1.isPrepared = false;
                preparePanel(panelfeaturestate1, null);
                if (ViewConfiguration.get(getContext()).hasPermanentMenuKey())
                    panelfeaturestate1.isPrepared = false;
            }
        }
    }

    public boolean isFloating()
    {
        return mIsFloating;
    }

    public boolean isShortcutKey(int i, KeyEvent keyevent)
    {
        boolean flag = true;
        PanelFeatureState panelfeaturestate = getPanelState(0, flag);
        if (panelfeaturestate.menu == null || !panelfeaturestate.menu.isShortcutKey(i, keyevent))
            flag = false;
        return flag;
    }

    protected void onActive()
    {
    }

    public void onConfigurationChanged(Configuration configuration)
    {
        if (mActionBar == null)
        {
            PanelFeatureState panelfeaturestate = getPanelState(0, false);
            if (panelfeaturestate != null && panelfeaturestate.menu != null)
                if (!panelfeaturestate.isOpen)
                {
                    clearMenuViews(panelfeaturestate);
                } else
                {
                    Bundle bundle = new Bundle();
                    if (panelfeaturestate.iconMenuPresenter != null)
                        panelfeaturestate.iconMenuPresenter.saveHierarchyState(bundle);
                    if (panelfeaturestate.listMenuPresenter != null)
                        panelfeaturestate.listMenuPresenter.saveHierarchyState(bundle);
                    clearMenuViews(panelfeaturestate);
                    reopenMenu(false);
                    if (panelfeaturestate.iconMenuPresenter != null)
                        panelfeaturestate.iconMenuPresenter.restoreHierarchyState(bundle);
                    if (panelfeaturestate.listMenuPresenter != null)
                        panelfeaturestate.listMenuPresenter.restoreHierarchyState(bundle);
                }
        }
    }

    protected void onDrawableChanged(int i, Drawable drawable, int j)
    {
label0:
        {
            ImageView imageview;
            if (i != 3)
            {
                if (i != 4)
                    break label0;
                imageview = getRightIconView();
            } else
            {
                imageview = getLeftIconView();
            }
            if (drawable == null)
            {
                imageview.setVisibility(8);
            } else
            {
                drawable.setAlpha(j);
                imageview.setImageDrawable(drawable);
                imageview.setVisibility(0);
            }
        }
    }

    protected void onIntChanged(int i, int j)
    {
        if (i != 2 && i != 5)
        {
            if (i == 7)
            {
                FrameLayout framelayout = (FrameLayout)findViewById(0x1020221);
                if (framelayout != null)
                    mLayoutInflater.inflate(j, framelayout);
            }
        } else
        {
            updateProgressBars(j);
        }
    }

    protected boolean onKeyDown(int i, int j, KeyEvent keyevent)
    {
        boolean flag = false;
        android.view.KeyEvent.DispatcherState dispatcherstate;
        if (mDecor == null)
            dispatcherstate = null;
        else
            dispatcherstate = mDecor.getKeyDispatcherState();
        switch (j)
        {
        default:
            break;

        case 4: // '\004'
            if (keyevent.getRepeatCount() > 0 || i < 0)
                break;
            if (dispatcherstate != null)
                dispatcherstate.startTracking(keyevent, this);
            flag = true;
            break;

        case 24: // '\030'
        case 25: // '\031'
        case 164: 
            getAudioManager().handleKeyDown(j, mVolumeControlStreamType);
            flag = true;
            break;

        case 82: // 'R'
            if (i < 0)
                i = 0;
            onKeyDownPanel(i, keyevent);
            flag = true;
            break;
        }
        return flag;
    }

    public final boolean onKeyDownPanel(int i, KeyEvent keyevent)
    {
        int j;
label0:
        {
label1:
            {
                j = keyevent.getKeyCode();
                if (keyevent.getRepeatCount() == 0)
                {
                    mPanelChordingKey = j;
                    j = getPanelState(i, true);
                    if (!((PanelFeatureState) (j)).isOpen)
                        break label1;
                }
                j = 0;
                break label0;
            }
            j = preparePanel(j, keyevent);
        }
        return j;
    }

    protected boolean onKeyUp(int i, int j, KeyEvent keyevent)
    {
        boolean flag;
        flag = false;
        android.view.KeyEvent.DispatcherState dispatcherstate;
        if (mDecor == null)
            dispatcherstate = null;
        else
            dispatcherstate = mDecor.getKeyDispatcherState();
        if (dispatcherstate != null)
            dispatcherstate.handleUpEvent(keyevent);
        j;
        JVM INSTR lookupswitch 6: default 229
    //                   4: 96
    //                   24: 161
    //                   25: 161
    //                   82: 179
    //                   84: 197
    //                   164: 161;
           goto _L1 _L2 _L3 _L3 _L4 _L5 _L3
_L1:
        break; /* Loop/switch isn't completed */
_L2:
        if (i < 0 || !keyevent.isTracking() || keyevent.isCanceled())
            break; /* Loop/switch isn't completed */
        if (i != 0) goto _L7; else goto _L6
_L6:
        flag = getPanelState(i, false);
        if (flag != null && ((PanelFeatureState) (flag)).isInExpandedMode) goto _L8; else goto _L7
_L7:
        closePanel(i);
        flag = true;
        break; /* Loop/switch isn't completed */
_L8:
        reopenMenu(true);
        flag = true;
        break; /* Loop/switch isn't completed */
_L3:
        getAudioManager().handleKeyUp(j, mVolumeControlStreamType);
        flag = true;
        break; /* Loop/switch isn't completed */
_L4:
        if (i < 0)
            i = 0;
        onKeyUpPanel(i, keyevent);
        flag = true;
        break; /* Loop/switch isn't completed */
_L5:
        if (getKeyguardManager().inKeyguardRestrictedInputMode())
            break; /* Loop/switch isn't completed */
        if (keyevent.isTracking() && !keyevent.isCanceled())
            launchDefaultSearch();
        flag = true;
        return flag;
    }

    public final void onKeyUpPanel(int i, KeyEvent keyevent)
    {
        if (mPanelChordingKey != 0)
        {
            mPanelChordingKey = 0;
            if (!keyevent.isCanceled() && (mDecor == null || mDecor.mActionMode == null))
            {
                boolean flag1 = false;
                PanelFeatureState panelfeaturestate = getPanelState(i, true);
                if (i != 0 || mActionBar == null || !mActionBar.isOverflowReserved())
                {
                    if (!panelfeaturestate.isOpen && !panelfeaturestate.isHandled)
                    {
                        if (panelfeaturestate.isPrepared)
                        {
                            boolean flag = true;
                            if (panelfeaturestate.refreshMenuContent)
                            {
                                panelfeaturestate.isPrepared = false;
                                flag = preparePanel(panelfeaturestate, keyevent);
                            }
                            if (flag)
                            {
                                EventLog.writeEvent(50001, 0);
                                openPanel(panelfeaturestate, keyevent);
                                flag1 = true;
                            }
                        }
                    } else
                    {
                        flag1 = panelfeaturestate.isOpen;
                        closePanel(panelfeaturestate, true);
                    }
                } else
                if (mActionBar.getVisibility() == 0)
                    if (mActionBar.isOverflowMenuShowing())
                        flag1 = mActionBar.hideOverflowMenu();
                    else
                    if (!isDestroyed() && preparePanel(panelfeaturestate, keyevent))
                        flag1 = mActionBar.showOverflowMenu();
                if (flag1)
                {
                    AudioManager audiomanager = (AudioManager)getContext().getSystemService("audio");
                    if (audiomanager == null)
                        Log.w("PhoneWindow", "Couldn't get audio manager");
                    else
                        audiomanager.playSoundEffect(0);
                }
            }
        }
    }

    public boolean onMenuItemSelected(MenuBuilder menubuilder, MenuItem menuitem)
    {
        boolean flag;
label0:
        {
            android.view.Window.Callback callback;
label1:
            {
                callback = getCallback();
                if (callback != null && !isDestroyed())
                {
                    flag = findMenuPanel(menubuilder.getRootMenu());
                    if (flag != null)
                        break label1;
                }
                flag = false;
                break label0;
            }
            flag = callback.onMenuItemSelected(((PanelFeatureState) (flag)).featureId, menuitem);
        }
        return flag;
    }

    public void onMenuModeChange(MenuBuilder menubuilder)
    {
        reopenMenu(true);
    }

    void onOptionsPanelRotationChanged()
    {
        PanelFeatureState panelfeaturestate = getPanelState(0, false);
        if (panelfeaturestate != null)
        {
            android.view.WindowManager.LayoutParams layoutparams;
            if (panelfeaturestate.decorView == null)
                layoutparams = null;
            else
                layoutparams = (android.view.WindowManager.LayoutParams)panelfeaturestate.decorView.getLayoutParams();
            if (layoutparams != null)
            {
                layoutparams.gravity = getOptionsPanelGravity();
                WindowManager windowmanager = getWindowManager();
                if (windowmanager != null)
                    windowmanager.updateViewLayout(panelfeaturestate.decorView, layoutparams);
            }
        }
    }

    public final void openPanel(int i, KeyEvent keyevent)
    {
        if (i != 0 || mActionBar == null || !mActionBar.isOverflowReserved())
            openPanel(getPanelState(i, true), keyevent);
        else
        if (mActionBar.getVisibility() == 0)
            mActionBar.showOverflowMenu();
    }

    public final View peekDecorView()
    {
        return mDecor;
    }

    public boolean performContextMenuIdentifierAction(int i, int j)
    {
        boolean flag;
        if (mContextMenu == null)
            flag = false;
        else
            flag = mContextMenu.performIdentifierAction(i, j);
        return flag;
    }

    public boolean performPanelIdentifierAction(int i, int j, int k)
    {
        boolean flag = false;
        PanelFeatureState panelfeaturestate = getPanelState(i, true);
        if (preparePanel(panelfeaturestate, new KeyEvent(0, 82)) && panelfeaturestate.menu != null)
        {
            flag = panelfeaturestate.menu.performIdentifierAction(j, k);
            if (mActionBar == null)
                closePanel(panelfeaturestate, true);
        }
        return flag;
    }

    public boolean performPanelShortcut(int i, int j, KeyEvent keyevent, int k)
    {
        return performPanelShortcut(getPanelState(i, true), j, keyevent, k);
    }

    public final boolean preparePanel(PanelFeatureState panelfeaturestate, KeyEvent keyevent)
    {
        boolean flag;
label0:
        {
label1:
            {
label2:
                {
label3:
                    {
                        flag = false;
                        if (isDestroyed())
                            break label0;
                        if (panelfeaturestate.isPrepared)
                            break label1;
                        if (mPreparedPanel != null && mPreparedPanel != panelfeaturestate)
                            closePanel(mPreparedPanel, false);
                        android.view.Window.Callback callback = getCallback();
                        if (callback != null)
                            panelfeaturestate.createdPanelView = callback.onCreatePanelView(panelfeaturestate.featureId);
                        if (panelfeaturestate.createdPanelView == null)
                        {
                            if (panelfeaturestate.menu == null || panelfeaturestate.refreshMenuContent)
                            {
                                if (panelfeaturestate.menu == null && (!initializePanelMenu(panelfeaturestate) || panelfeaturestate.menu == null))
                                    break label0;
                                if (mActionBar != null)
                                {
                                    if (mActionMenuPresenterCallback == null)
                                        mActionMenuPresenterCallback = new ActionMenuPresenterCallback();
                                    mActionBar.setMenu(panelfeaturestate.menu, mActionMenuPresenterCallback);
                                }
                                panelfeaturestate.menu.stopDispatchingItemsChanged();
                                if (callback == null || !callback.onCreatePanelMenu(panelfeaturestate.featureId, panelfeaturestate.menu))
                                    break label2;
                                panelfeaturestate.refreshMenuContent = false;
                            }
                            panelfeaturestate.menu.stopDispatchingItemsChanged();
                            if (panelfeaturestate.frozenActionViewState != null)
                            {
                                panelfeaturestate.menu.restoreActionViewStates(panelfeaturestate.frozenActionViewState);
                                panelfeaturestate.frozenActionViewState = null;
                            }
                            if (!callback.onPreparePanel(panelfeaturestate.featureId, panelfeaturestate.createdPanelView, panelfeaturestate.menu))
                                break label3;
                            if (keyevent == null)
                                flag = -1;
                            else
                                flag = keyevent.getDeviceId();
                            if (KeyCharacterMap.load(flag).getKeyboardType() == 1)
                                flag = false;
                            else
                                flag = true;
                            panelfeaturestate.qwertyMode = flag;
                            panelfeaturestate.menu.setQwertyMode(panelfeaturestate.qwertyMode);
                            panelfeaturestate.menu.startDispatchingItemsChanged();
                        }
                        panelfeaturestate.isPrepared = true;
                        panelfeaturestate.isHandled = false;
                        mPreparedPanel = panelfeaturestate;
                        flag = true;
                        break label0;
                    }
                    if (mActionBar != null)
                        mActionBar.setMenu(null, mActionMenuPresenterCallback);
                    panelfeaturestate.menu.startDispatchingItemsChanged();
                    break label0;
                }
                panelfeaturestate.setMenu(null);
                if (mActionBar != null)
                    mActionBar.setMenu(null, mActionMenuPresenterCallback);
                break label0;
            }
            flag = true;
        }
        return flag;
    }

    public boolean requestFeature(int i)
    {
        if (mContentParent == null)
        {
            int j = getFeatures();
            if (j == 65 || i != 7)
            {
                if ((j & 0x80) == 0 || i == 7 || i == 10)
                {
                    if ((j & 2) == 0 || i != 8)
                    {
                        if ((j & 0x100) != 0 && i == 1)
                            removeFeature(8);
                        j = super.requestFeature(i);
                    } else
                    {
                        j = 0;
                    }
                    return j;
                } else
                {
                    throw new AndroidRuntimeException("You cannot combine custom titles with other title features");
                }
            } else
            {
                throw new AndroidRuntimeException("You cannot combine custom titles with other title features");
            }
        } else
        {
            throw new AndroidRuntimeException("requestFeature() must be called before adding content");
        }
    }

    public void restoreHierarchyState(Bundle bundle)
    {
        if (mContentParent != null)
        {
            Object obj = bundle.getSparseParcelableArray("android:views");
            if (obj != null)
                mContentParent.restoreHierarchyState(((SparseArray) (obj)));
            int i = bundle.getInt("android:focusedViewId", -1);
            if (i != -1)
            {
                obj = mContentParent.findViewById(i);
                if (obj == null)
                    Log.w("PhoneWindow", (new StringBuilder()).append("Previously focused view reported id ").append(i).append(" during save, but can't be found during restore.").toString());
                else
                    ((View) (obj)).requestFocus();
            }
            obj = bundle.getSparseParcelableArray("android:Panels");
            if (obj != null)
                restorePanelState(((SparseArray) (obj)));
            if (mActionBar != null)
            {
                SparseArray sparsearray = bundle.getSparseParcelableArray("android:ActionBar");
                mActionBar.restoreHierarchyState(sparsearray);
            }
        }
    }

    public Bundle saveHierarchyState()
    {
        Bundle bundle = new Bundle();
        if (mContentParent != null)
        {
            Object obj = new SparseArray();
            mContentParent.saveHierarchyState(((SparseArray) (obj)));
            bundle.putSparseParcelableArray("android:views", ((SparseArray) (obj)));
            obj = mContentParent.findFocus();
            if (obj != null && ((View) (obj)).getId() != -1)
                bundle.putInt("android:focusedViewId", ((View) (obj)).getId());
            obj = new SparseArray();
            savePanelState(((SparseArray) (obj)));
            if (((SparseArray) (obj)).size() > 0)
                bundle.putSparseParcelableArray("android:Panels", ((SparseArray) (obj)));
            if (mActionBar != null)
            {
                SparseArray sparsearray = new SparseArray();
                mActionBar.saveHierarchyState(sparsearray);
                bundle.putSparseParcelableArray("android:ActionBar", sparsearray);
            }
        }
        return bundle;
    }

    void sendCloseSystemWindows()
    {
        PhoneWindowManager.sendCloseSystemWindows(getContext(), null);
    }

    void sendCloseSystemWindows(String s)
    {
        PhoneWindowManager.sendCloseSystemWindows(getContext(), s);
    }

    public final void setBackgroundDrawable(Drawable drawable)
    {
        if (drawable != mBackgroundDrawable || mBackgroundResource != 0)
        {
            mBackgroundResource = 0;
            mBackgroundDrawable = drawable;
            if (mDecor != null)
                mDecor.setWindowBackground(drawable);
        }
    }

    public final void setChildDrawable(int i, Drawable drawable)
    {
        DrawableFeatureState drawablefeaturestate = getDrawableState(i, true);
        drawablefeaturestate.child = drawable;
        updateDrawable(i, drawablefeaturestate, false);
    }

    public final void setChildInt(int i, int j)
    {
        updateInt(i, j, false);
    }

    public final void setContainer(Window window)
    {
        super.setContainer(window);
    }

    public void setContentView(int i)
    {
        if (mContentParent != null)
            mContentParent.removeAllViews();
        else
            installDecor();
        mLayoutInflater.inflate(i, mContentParent);
        android.view.Window.Callback callback = getCallback();
        if (callback != null && !isDestroyed())
            callback.onContentChanged();
    }

    public void setContentView(View view)
    {
        setContentView(view, new android.view.ViewGroup.LayoutParams(-1, -1));
    }

    public void setContentView(View view, android.view.ViewGroup.LayoutParams layoutparams)
    {
        if (mContentParent != null)
            mContentParent.removeAllViews();
        else
            installDecor();
        mContentParent.addView(view, layoutparams);
        android.view.Window.Callback callback = getCallback();
        if (callback != null && !isDestroyed())
            callback.onContentChanged();
    }

    protected final void setFeatureDefaultDrawable(int i, Drawable drawable)
    {
        DrawableFeatureState drawablefeaturestate = getDrawableState(i, true);
        if (drawablefeaturestate.def != drawable)
        {
            drawablefeaturestate.def = drawable;
            updateDrawable(i, drawablefeaturestate, false);
        }
    }

    public final void setFeatureDrawable(int i, Drawable drawable)
    {
        DrawableFeatureState drawablefeaturestate = getDrawableState(i, true);
        drawablefeaturestate.resid = 0;
        drawablefeaturestate.uri = null;
        if (drawablefeaturestate.local != drawable)
        {
            drawablefeaturestate.local = drawable;
            updateDrawable(i, drawablefeaturestate, false);
        }
    }

    public void setFeatureDrawableAlpha(int i, int j)
    {
        DrawableFeatureState drawablefeaturestate = getDrawableState(i, true);
        if (drawablefeaturestate.alpha != j)
        {
            drawablefeaturestate.alpha = j;
            updateDrawable(i, drawablefeaturestate, false);
        }
    }

    public final void setFeatureDrawableResource(int i, int j)
    {
        if (j == 0)
        {
            setFeatureDrawable(i, null);
        } else
        {
            DrawableFeatureState drawablefeaturestate = getDrawableState(i, true);
            if (drawablefeaturestate.resid != j)
            {
                drawablefeaturestate.resid = j;
                drawablefeaturestate.uri = null;
                drawablefeaturestate.local = getContext().getResources().getDrawable(j);
                updateDrawable(i, drawablefeaturestate, false);
            }
        }
    }

    public final void setFeatureDrawableUri(int i, Uri uri)
    {
        if (uri == null)
        {
            setFeatureDrawable(i, null);
        } else
        {
            DrawableFeatureState drawablefeaturestate = getDrawableState(i, true);
            if (drawablefeaturestate.uri == null || !drawablefeaturestate.uri.equals(uri))
            {
                drawablefeaturestate.resid = 0;
                drawablefeaturestate.uri = uri;
                drawablefeaturestate.local = loadImageURI(uri);
                updateDrawable(i, drawablefeaturestate, false);
            }
        }
    }

    protected void setFeatureFromAttrs(int i, TypedArray typedarray, int j, int k)
    {
        Drawable drawable = typedarray.getDrawable(j);
        if (drawable != null)
        {
            requestFeature(i);
            setFeatureDefaultDrawable(i, drawable);
        }
        if ((getFeatures() & 1 << i) != 0)
        {
            int l = typedarray.getInt(k, -1);
            if (l >= 0)
                setFeatureDrawableAlpha(i, l);
        }
    }

    public final void setFeatureInt(int i, int j)
    {
        updateInt(i, j, false);
    }

    public void setTitle(CharSequence charsequence)
    {
        if (mTitleView == null)
        {
            if (mActionBar != null)
                mActionBar.setWindowTitle(charsequence);
        } else
        {
            mTitleView.setText(charsequence);
        }
        mTitle = charsequence;
    }

    public void setTitleColor(int i)
    {
        if (mTitleView != null)
            mTitleView.setTextColor(i);
        mTitleColor = i;
    }

    public void setUiOptions(int i)
    {
        mUiOptions = i;
    }

    public void setUiOptions(int i, int j)
    {
        mUiOptions = mUiOptions & ~j | i & j;
    }

    public void setVolumeControlStream(int i)
    {
        mVolumeControlStreamType = i;
    }

    public boolean superDispatchGenericMotionEvent(MotionEvent motionevent)
    {
        return mDecor.superDispatchGenericMotionEvent(motionevent);
    }

    public boolean superDispatchKeyEvent(KeyEvent keyevent)
    {
        return mDecor.superDispatchKeyEvent(keyevent);
    }

    public boolean superDispatchKeyShortcutEvent(KeyEvent keyevent)
    {
        return mDecor.superDispatchKeyShortcutEvent(keyevent);
    }

    public boolean superDispatchTouchEvent(MotionEvent motionevent)
    {
        return mDecor.superDispatchTouchEvent(motionevent);
    }

    public boolean superDispatchTrackballEvent(MotionEvent motionevent)
    {
        return mDecor.superDispatchTrackballEvent(motionevent);
    }

    public void takeInputQueue(android.view.InputQueue.Callback callback)
    {
        mTakeInputQueueCallback = callback;
    }

    public void takeKeyEvents(boolean flag)
    {
        mDecor.setFocusable(flag);
    }

    public void takeSurface(android.view.SurfaceHolder.Callback2 callback2)
    {
        mTakeSurfaceCallback = callback2;
    }

    public final void togglePanel(int i, KeyEvent keyevent)
    {
        PanelFeatureState panelfeaturestate = getPanelState(i, true);
        if (!panelfeaturestate.isOpen)
            openPanel(panelfeaturestate, keyevent);
        else
            closePanel(panelfeaturestate, true);
    }

    protected final void updateDrawable(int i, boolean flag)
    {
        DrawableFeatureState drawablefeaturestate = getDrawableState(i, false);
        if (drawablefeaturestate != null)
            updateDrawable(i, drawablefeaturestate, flag);
    }



/*
    static MenuDialogHelper access$1002(PhoneWindow phonewindow, MenuDialogHelper menudialoghelper)
    {
        phonewindow.mContextMenuHelper = menudialoghelper;
        return menudialoghelper;
    }

*/












/*
    static ContextMenuBuilder access$902(PhoneWindow phonewindow, ContextMenuBuilder contextmenubuilder)
    {
        phonewindow.mContextMenu = contextmenubuilder;
        return contextmenubuilder;
    }

*/
}
