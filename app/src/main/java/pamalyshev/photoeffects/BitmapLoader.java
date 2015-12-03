package pamalyshev.photoeffects;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.squareup.picasso.Picasso;

/**
 * Created by pamalyshev on 30.11.15.
 */
public class BitmapLoader extends AsyncTaskLoader<AsyncResult<Bitmap>> {
    public static final String TAG = "BitmapLoader";

    private Uri uri;
    private AsyncResult<Bitmap> result;
    private int width;
    private int height;

    public BitmapLoader(Context context, Uri uri, int width, int height) {
        super(context);
        this.uri = uri;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    protected void onStartLoading() {
        if (result != null && result.getException() == null)
            deliverResult(result);
        else
            forceLoad();
    }

    @Override
    protected void onReset() {
        result = null;
    }

    @Override
    public AsyncResult<Bitmap> loadInBackground() {
        try {
            //TODO: If Picasso won't be used for "recents"
            // (where multiple bitmaps need to be loaded) remove it, and use BitmapFactory with inSampleSize.
            Bitmap bitmap = Picasso.with(getContext()).load(uri).resize(width, height).centerInside().get();
            return new AsyncResult<>(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Caught exception ", e);
            return new AsyncResult<>(e);
        }
    }
}
