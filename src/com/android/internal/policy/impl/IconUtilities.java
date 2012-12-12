// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst noctor space 

package com.android.internal.policy.impl;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.DisplayMetrics;
import android.util.TypedValue;

final class IconUtilities
{

    private static final String TAG = "IconUtilities";
    private static final int sColors[];
    private final Paint mBlurPaint = new Paint();
    private final Canvas mCanvas = new Canvas();
    private int mColorIndex;
    private final DisplayMetrics mDisplayMetrics;
    private final Paint mGlowColorFocusedPaint = new Paint();
    private final Paint mGlowColorPressedPaint = new Paint();
    private int mIconHeight;
    private int mIconTextureHeight;
    private int mIconTextureWidth;
    private int mIconWidth;
    private final Rect mOldBounds = new Rect();
    private final Paint mPaint = new Paint();

    public IconUtilities(Context context)
    {
        mIconWidth = -1;
        mIconHeight = -1;
        mIconTextureWidth = -1;
        mIconTextureHeight = -1;
        mColorIndex = 0;
        Object obj = context.getResources();
        DisplayMetrics displaymetrics = ((Resources) (obj)).getDisplayMetrics();
        mDisplayMetrics = displaymetrics;
        float f = 5F * displaymetrics.density;
        int i = (int)((Resources) (obj)).getDimension(0x1050000);
        mIconHeight = i;
        mIconWidth = i;
        i = mIconWidth + (int)(2F * f);
        mIconTextureHeight = i;
        mIconTextureWidth = i;
        mBlurPaint.setMaskFilter(new BlurMaskFilter(f, android.graphics.BlurMaskFilter.Blur.NORMAL));
        f = new TypedValue();
        Paint paint = mGlowColorPressedPaint;
        if (!context.getTheme().resolveAttribute(0x101038d, f, true))
            i = -15616;
        else
            i = ((TypedValue) (f)).data;
        paint.setColor(i);
        mGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        i = mGlowColorFocusedPaint;
        if (!context.getTheme().resolveAttribute(0x101038f, f, true))
            f = -29184;
        else
            f = ((TypedValue) (f)).data;
        i.setColor(f);
        mGlowColorFocusedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        (new ColorMatrix()).setSaturation(0.2F);
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
    }

    private Bitmap createIconBitmap(Drawable drawable)
    {
        int j = mIconWidth;
        int i = mIconHeight;
        if (!(drawable instanceof PaintDrawable))
        {
            if (drawable instanceof BitmapDrawable)
            {
                BitmapDrawable bitmapdrawable = (BitmapDrawable)drawable;
                if (bitmapdrawable.getBitmap().getDensity() == 0)
                    bitmapdrawable.setTargetDensity(mDisplayMetrics);
            }
        } else
        {
            PaintDrawable paintdrawable = (PaintDrawable)drawable;
            paintdrawable.setIntrinsicWidth(j);
            paintdrawable.setIntrinsicHeight(i);
        }
        int k = drawable.getIntrinsicWidth();
        int l = drawable.getIntrinsicHeight();
        if (k > 0 && k > 0)
            if (j >= k && i >= l)
            {
                if (k < j && l < i)
                {
                    j = k;
                    i = l;
                }
            } else
            {
                float f = (float)k / (float)l;
                if (k <= l)
                {
                    if (l > k)
                        j = (int)(f * (float)i);
                } else
                {
                    i = (int)((float)j / f);
                }
            }
        int i1 = mIconTextureWidth;
        l = mIconTextureHeight;
        Bitmap bitmap = Bitmap.createBitmap(i1, l, android.graphics.Bitmap.Config.ARGB_8888);
        Canvas canvas = mCanvas;
        canvas.setBitmap(bitmap);
        i1 = (i1 - j) / 2;
        l = (l - i) / 2;
        mOldBounds.set(drawable.getBounds());
        drawable.setBounds(i1, l, i1 + j, l + i);
        drawable.draw(canvas);
        drawable.setBounds(mOldBounds);
        return bitmap;
    }

    private Bitmap createSelectedBitmap(Bitmap bitmap, boolean flag)
    {
        Bitmap bitmap2 = Bitmap.createBitmap(mIconTextureWidth, mIconTextureHeight, android.graphics.Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap2);
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        int ai[] = new int[2];
        Bitmap bitmap1 = bitmap.extractAlpha(mBlurPaint, ai);
        float f = ai[0];
        float f1 = ai[1];
        Paint paint;
        if (!flag)
            paint = mGlowColorFocusedPaint;
        else
            paint = mGlowColorPressedPaint;
        canvas.drawBitmap(bitmap1, f, f1, paint);
        bitmap1.recycle();
        canvas.drawBitmap(bitmap, 0F, 0F, mPaint);
        canvas.setBitmap(null);
        return bitmap2;
    }

    public Drawable createIconDrawable(Drawable drawable)
    {
        Bitmap bitmap = createIconBitmap(drawable);
        StateListDrawable statelistdrawable = new StateListDrawable();
        int ai[] = new int[1];
        ai[0] = 0x101009c;
        statelistdrawable.addState(ai, new BitmapDrawable(createSelectedBitmap(bitmap, false)));
        ai = new int[1];
        ai[0] = 0x10100a7;
        statelistdrawable.addState(ai, new BitmapDrawable(createSelectedBitmap(bitmap, true)));
        statelistdrawable.addState(new int[0], new BitmapDrawable(bitmap));
        statelistdrawable.setBounds(0, 0, mIconTextureWidth, mIconTextureHeight);
        return statelistdrawable;
    }

    static 
    {
        int ai[] = new int[3];
        ai[0] = 0xffff0000;
        ai[1] = 0xff00ff00;
        ai[2] = 0xff0000ff;
        sColors = ai;
    }
}
