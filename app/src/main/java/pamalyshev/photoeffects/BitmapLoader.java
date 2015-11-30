package pamalyshev.photoeffects;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by pamalyshev on 30.11.15.
 */
public class BitmapLoader extends AsyncTaskLoader<Bitmap> {
    public static final String TAG = "BitmapLoader";

    private Uri uri;
    private Bitmap bitmap;
    private int width;
    private int height;

    public BitmapLoader(Context context, Uri uri, int width, int height) {
        super(context);
        this.uri = uri;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onStartLoading() {
        if (bitmap != null)
            deliverResult(bitmap);
        forceLoad();
    }

    @Override
    protected void onReset() {
        bitmap = null;
    }

    @Override
    public Bitmap loadInBackground() {
        try {
            bitmap = Picasso.with(getContext()).load(uri).resize(width, height).centerInside().get();
        } catch (IOException e) {
            Log.e(TAG, "Caught exception ", e);
        }

        return bitmap;
    }
}
