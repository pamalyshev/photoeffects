package pamalyshev.photoeffects;

import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by pamalyshev on 01.12.15.
 */
public class InitialLayoutHelper {
    public interface InitialLayoutListener {
        void onInitialLayoutFinished();
    }

    private InitialLayoutListener listener;
    private View view;

    public static void  registerListener(View view, InitialLayoutListener listener) {
        new InitialLayoutHelper(view, listener);
    }

    private InitialLayoutHelper(View view, InitialLayoutListener listener) {
        this.listener = listener;
        this.view = view;
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener);
        }
    }

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener =
            new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT >= 16)
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            else
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            listener.onInitialLayoutFinished();
        }
    };
}
