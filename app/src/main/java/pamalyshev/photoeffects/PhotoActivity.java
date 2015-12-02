package pamalyshev.photoeffects;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhotoActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Bitmap>,
        View.OnClickListener, InitialLayoutHelper.InitialLayoutListener {
    public static final String TAG = "PhotoActivity";

    private static final String KEY_EFFECT = "effect";

    private enum Effect {
        GRAYSCALE,
        SEPIA;
    }

    private enum Mode implements NamedItem {
        BEFORE(R.string.mode_before),
        AFTER(R.string.mode_after),
        BEFORE_N_AFTER(R.string.mode_before_n_after);

        private int nameResId;

        Mode(int nameResId) {
            this.nameResId = nameResId;
        }

        @Override
        public int getNameResId() {
            return nameResId;
        }
    }

    @Bind(R.id.photoView)
    ImageView photoView;
    @Bind(R.id.grayscaleButton)
    RadioButton grayscaleButton;
    @Bind(R.id.sepiaButton)
    RadioButton sepiaButton;
    @Bind(R.id.buttonsContainer)
    LinearLayout buttonsContainer;

    private Uri imageUri;

    private Drawable drawable;
    private Effect currentEffect = Effect.GRAYSCALE;

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
        InitialLayoutHelper.registerListener(photoView, this);

        if (savedInstanceState != null) {
            currentEffect = Effect.values()[savedInstanceState.getInt(KEY_EFFECT)];
        }
        setEffect(currentEffect);

        configureActionBar();
    }

    private void configureActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        Spinner abSpinner = new AppCompatSpinner(this);

        EnumAdapter<Mode> adapter = new EnumAdapter<>(Mode.class, this,
                android.R.layout.simple_spinner_dropdown_item);
        abSpinner.setAdapter(adapter);

        actionBar.setCustomView(abSpinner);
    }

    @Override
    public void onInitialLayoutFinished() {
        //TODO: We need to load image with new size if screen is rotated.
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_EFFECT, currentEffect.ordinal());
    }

    @Override
    public void onClick(View v) {
        if (v == grayscaleButton)
            setEffect(Effect.GRAYSCALE);
        else if (v == sepiaButton)
            setEffect(Effect.SEPIA);
    }

    private void setEffect(Effect effect) {
        currentEffect = effect;
        switch (effect) {
            case GRAYSCALE:
                switchButtons(grayscaleButton);
                break;
            case SEPIA:
                switchButtons(sepiaButton);
                break;
        }

        if (drawable != null)
            configureDrawable(effect);
    }

    private void switchButtons(RadioButton selectedButton) {
        for (int i = 0; i < buttonsContainer.getChildCount(); ++i) {
            View view = buttonsContainer.getChildAt(i);
            if (view instanceof RadioButton)
                ((RadioButton) view).setChecked(view == selectedButton);
        }
    }

    private void configureDrawable(Effect effect) {
        ColorMatrix colorMatrix = null;
        switch (effect) {
            case GRAYSCALE:
                colorMatrix = grayscale;
                break;
            case SEPIA:
                colorMatrix = sepia;
                break;
        }
        drawable.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new BitmapLoader(this, imageUri, photoView.getWidth(), photoView.getHeight());
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
        drawable = new BitmapDrawable(getResources(), bitmap);


        drawable = new BeforeAfterDrawable(drawable);
        configureDrawable(currentEffect);
        photoView.setImageDrawable(drawable);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        photoView.setImageDrawable(null);
    }
}
