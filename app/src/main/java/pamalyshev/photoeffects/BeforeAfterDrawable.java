package pamalyshev.photoeffects;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.drawable.DrawableWrapper;

/**
 * Created by pamalyshev on 02.12.15.
 */
public class BeforeAfterDrawable extends DrawableWrapper {
    private final Rect mTmpRect = new Rect();
    private ColorFilter colorFilter;

    private Paint splitLinePaint;

    public BeforeAfterDrawable(Drawable dr) {
        super(dr);
    }

    @Override
    public void draw(Canvas canvas) {
        drawLeft(canvas);
        drawRight(canvas);
        if (splitLinePaint != null)
            drawSplitLine(canvas);
    }

    private void drawLeft(Canvas canvas) {
        mTmpRect.set(getBounds());
        mTmpRect.right -= mTmpRect.width() / 2;
        canvas.save();
        canvas.clipRect(mTmpRect);
        super.draw(canvas);
        canvas.restore();
    }

    private void drawRight(Canvas canvas) {
        mTmpRect.set(getBounds());
        mTmpRect.left += mTmpRect.width() / 2;
        canvas.save();
        canvas.clipRect(mTmpRect);
        Drawable wrappedDrawable = getWrappedDrawable();
        wrappedDrawable.setColorFilter(colorFilter);
        super.draw(canvas);
        wrappedDrawable.setColorFilter(null);
        canvas.restore();
    }

    private void drawSplitLine(Canvas canvas) {
        mTmpRect.set(getBounds());
        float hmiddle = mTmpRect.left + mTmpRect.width() / 2f;
        canvas.drawLine(hmiddle, mTmpRect.top, hmiddle, mTmpRect.bottom, splitLinePaint);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        colorFilter = cf;
        invalidateSelf();
    }

    public void setSplitLine(float width, int color) {
        if (splitLinePaint == null)
            splitLinePaint = new Paint();
        splitLinePaint.setStrokeWidth(width);
        splitLinePaint.setColor(color);
        invalidateSelf();
    }

    public void removeSplitLine() {
        splitLinePaint = null;
        invalidateSelf();
    }
}
