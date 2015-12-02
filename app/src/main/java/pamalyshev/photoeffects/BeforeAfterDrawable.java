package pamalyshev.photoeffects;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.drawable.DrawableWrapper;

/**
 * Created by pamalyshev on 02.12.15.
 */
public class BeforeAfterDrawable extends DrawableWrapper {
    private final Rect mTmpRect = new Rect();
    private ColorFilter colorFilter;

    public BeforeAfterDrawable(Drawable dr) {
        super(dr);
    }

    @Override
    public void draw(Canvas canvas) {
        drawLeft(canvas);
        drawRight(canvas);
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

    @Override
    public void setColorFilter(ColorFilter cf) {
        colorFilter = cf;
        invalidateSelf();
    }
}
