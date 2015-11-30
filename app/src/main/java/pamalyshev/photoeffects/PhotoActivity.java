package pamalyshev.photoeffects;

import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhotoActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Bitmap> {
    public static final String TAG = "PhotoActivity";

    @Bind(R.id.photoView)
    ImageView photoView;

    private Uri imageUri;

    private static ColorMatrix grayscale;
    private static ColorMatrix sepia;

    static {
        grayscale = new ColorMatrix();
        grayscale.setSaturation(0);

        //From http://stackoverflow.com/a/9149010/3360009
        ColorMatrix tmp = new ColorMatrix();
        tmp.setScale(1f, .95f, .82f, 1.0f);
        sepia = new ColorMatrix();
        sepia.setConcat(tmp, grayscale);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);

        imageUri = getIntent().getData();

        ViewTreeObserver viewTreeObserver = photoView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= 16)
                        photoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    else
                        photoView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    getSupportLoaderManager().initLoader(0, null, PhotoActivity.this);
                }
            });
        }

    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new BitmapLoader(this, imageUri, photoView.getWidth(), photoView.getHeight());
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);

        drawable.setColorFilter(new ColorMatrixColorFilter(sepia));
        photoView.setImageDrawable(drawable);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        photoView.setImageDrawable(null);
    }
}
