package pamalyshev.photoeffects;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhotoActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Bitmap>,
        View.OnClickListener {
    public static final String TAG = "PhotoActivity";

    private static int EFFECT_GRAYSCALE = 0;
    private static int EFFECT_SEPIA = 1;

    @Bind(R.id.photoView)
    ImageView photoView;
    @Bind(R.id.grayscaleButton)
    RadioButton grayscaleButton;
    @Bind(R.id.sepiaButton)
    RadioButton sepiaButton;
    @Bind(R.id.buttonsContainer)
    LinearLayout buttonsContainer;

    private Uri imageUri;

    private BitmapDrawable drawable;

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
        getWindow().setBackgroundDrawable(null);

        sepiaButton.setOnClickListener(this);
        grayscaleButton.setOnClickListener(this);

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
    public void onClick(View v) {
        if (v == grayscaleButton)
            setEffect(EFFECT_GRAYSCALE);
        else if (v == sepiaButton)
            setEffect(EFFECT_SEPIA);
    }

    private void setEffect(int effectId) {
        ColorMatrix colorMatrix = null;

        if (effectId == EFFECT_GRAYSCALE) {
            switchButtons(grayscaleButton);
            colorMatrix = grayscale;
        } else if (effectId == EFFECT_SEPIA) {
            switchButtons(sepiaButton);
            colorMatrix = sepia;
        }

        drawable.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }

    private void switchButtons(RadioButton selectedButton) {
        for (int i = 0; i < buttonsContainer.getChildCount(); ++i) {
            View view = buttonsContainer.getChildAt(i);
            if (view instanceof RadioButton)
                ((RadioButton) view).setChecked(view == selectedButton);
        }
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new BitmapLoader(this, imageUri, photoView.getWidth(), photoView.getHeight());
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
        drawable = new BitmapDrawable(getResources(), bitmap);

        drawable.setColorFilter(new ColorMatrixColorFilter(sepia));
        photoView.setImageDrawable(drawable);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        photoView.setImageDrawable(null);
    }
}
