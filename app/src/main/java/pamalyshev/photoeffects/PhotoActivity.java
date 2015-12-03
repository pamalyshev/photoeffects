package pamalyshev.photoeffects;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhotoActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<AsyncResult<Bitmap>>,
        View.OnClickListener, InitialLayoutHelper.InitialLayoutListener {
    public static final String TAG = "PhotoActivity";

    private static final String KEY_EFFECT = "effect";
    private static final String KEY_MODE = "mode";

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final String KEY_WAITING_FOR_PERM_REQUEST_RESULT = "waitingForPermRequestResult";

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

    private enum State {
        LOADING,
        LOADED_OK,
        LOADED_ERROR_GENERAL,
        LOADED_ERROR_STORAGE_PERMISSIONS;
    }

    @Bind(R.id.photoView)
    ImageView photoView;
    @Bind(R.id.activityCircle)
    ProgressBar activityCircle;
    @Bind(R.id.textView)
    TextView textView;
    @Bind(R.id.grayscaleButton)
    RadioButton grayscaleButton;
    @Bind(R.id.sepiaButton)
    RadioButton sepiaButton;
    @Bind(R.id.buttonsContainer)
    LinearLayout buttonsContainer;

    @Bind({R.id.photoView, R.id.activityCircle, R.id.textView})
    List<View> stateDependantViews;

    private Uri imageUri;

    private Bitmap bitmap;
    private Effect currentEffect = Effect.GRAYSCALE;
    private Mode currentMode = Mode.BEFORE_N_AFTER;
    private State currentState = State.LOADING;
    private boolean waitingForPermRequestResult;

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

        if (savedInstanceState != null) {
            currentEffect = Effect.values()[savedInstanceState.getInt(KEY_EFFECT)];
            currentMode = Mode.values()[savedInstanceState.getInt(KEY_MODE)];
            waitingForPermRequestResult = savedInstanceState.getBoolean(KEY_WAITING_FOR_PERM_REQUEST_RESULT);
        }

        setEffect(currentEffect);
        setMode(currentMode);
        setState(State.LOADING);

        configureActionBar();
        InitialLayoutHelper.registerListener(photoView, this);
    }

    private void configureActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            throw new NullPointerException("No action bar found");

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        Spinner abSpinner = new AppCompatSpinner(this);
        abSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Mode mode = (Mode) parent.getAdapter().getItem(position);
                setMode(mode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            } //Never happens
        });

        EnumAdapter<Mode> adapter = new EnumAdapter<>(Mode.class, this,
                android.R.layout.simple_spinner_dropdown_item);
        abSpinner.setAdapter(adapter);
        abSpinner.setSelection(currentMode.ordinal());
        actionBar.setCustomView(abSpinner);
    }

    @Override
    public void onInitialLayoutFinished() {
        //TODO: Since we use Picasso, which caches bitmaps in memory,
        // we can remove BitmapLoader and use Picasso with callbacks.
        LoaderManager loaderManager = getSupportLoaderManager();
        BitmapLoader oldLoader = (BitmapLoader) loaderManager.<AsyncResult<Bitmap>>getLoader(0);
        if (oldLoader != null
                && (oldLoader.getWidth() != photoView.getWidth()
                || oldLoader.getHeight() != photoView.getHeight()))
            loaderManager.destroyLoader(0);
        loaderManager.initLoader(0, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_EFFECT, currentEffect.ordinal());
        outState.putInt(KEY_MODE, currentMode.ordinal());
        outState.putBoolean(KEY_WAITING_FOR_PERM_REQUEST_RESULT, waitingForPermRequestResult);
    }

    private void setMode(Mode mode) {
        currentMode = mode;

        configureButtons();
        configurePhotoView();
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
        configureButtons();
        configurePhotoView();
    }

    private void configureButtons() {
        if (currentMode == Mode.BEFORE || currentState != State.LOADED_OK) {
            buttonsContainer.setVisibility(View.GONE);
        } else {
            buttonsContainer.setVisibility(View.VISIBLE);
            switch (currentEffect) {
                case GRAYSCALE:
                    switchButtons(grayscaleButton);
                    break;
                case SEPIA:
                    switchButtons(sepiaButton);
                    break;
            }
        }
    }

    private void switchButtons(RadioButton selectedButton) {
        for (int i = 0; i < buttonsContainer.getChildCount(); ++i) {
            View view = buttonsContainer.getChildAt(i);
            if (view instanceof RadioButton)
                ((RadioButton) view).setChecked(view == selectedButton);
        }
    }

    private void configurePhotoView() {
        Drawable currentDrawable = null;
        if (bitmap != null) {
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            switch (currentMode) {
                case BEFORE:
                    currentDrawable = bitmapDrawable;
                    break;
                case AFTER:
                    currentDrawable = bitmapDrawable;
                    configureDrawable(currentDrawable, currentEffect);
                    break;
                case BEFORE_N_AFTER:
                    BeforeAfterDrawable beforeAfterDrawable = new BeforeAfterDrawable(bitmapDrawable);

                    Resources res = getResources();
                    float splitLineWidth = res.getDimension(R.dimen.splitLineWidth);
                    int splitLineColor = res.getColor(R.color.splitLineColor);
                    beforeAfterDrawable.setSplitLine(splitLineWidth, splitLineColor);

                    currentDrawable = beforeAfterDrawable;
                    configureDrawable(currentDrawable, currentEffect);
                    break;
            }
        }
        photoView.setImageDrawable(currentDrawable);
    }

    private void configureDrawable(Drawable drawable, Effect effect) {
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

    private void setState(State state) {
        currentState = state;
        View visibleView = null;
        switch (state) {
            case LOADING:
                visibleView = activityCircle;
                break;
            case LOADED_OK:
                visibleView = photoView;
                break;
            case LOADED_ERROR_GENERAL:
                visibleView = textView;
                textView.setText(R.string.error_cant_load_image_general);
                break;
            case LOADED_ERROR_STORAGE_PERMISSIONS:
                visibleView = textView;
                textView.setText(R.string.error_cant_load_image_storage_permissions);
                break;
        }

        for (View view : stateDependantViews) {
            //TODO:They are INVISIBLE but not GONE just to be correctly measured.
            view.setVisibility(view == visibleView ? View.VISIBLE : View.INVISIBLE);
        }

        configureButtons();
    }

    @Override
    public Loader<AsyncResult<Bitmap>> onCreateLoader(int id, Bundle args) {
        return new BitmapLoader(this, imageUri, photoView.getWidth(), photoView.getHeight());
    }

    @Override
    public void onLoadFinished(Loader<AsyncResult<Bitmap>> loader, AsyncResult<Bitmap> result) {
        Exception exception = result.getException();
        if (exception == null) {
            this.bitmap = result.getValue();
            configurePhotoView();
            setState(State.LOADED_OK);
        } else if (exception instanceof SecurityException
                && !haveStoragePermissions()) {
            setState(State.LOADED_ERROR_STORAGE_PERMISSIONS);
            requestStoragePermissionsIfNeeded();
        } else {
            setState(State.LOADED_ERROR_GENERAL);
        }
    }

    @Override
    public void onLoaderReset(Loader<AsyncResult<Bitmap>> loader) {
        this.bitmap = null;
        configurePhotoView();
        setState(State.LOADING);
    }

    private boolean haveStoragePermissions() {
        if (Build.VERSION.SDK_INT < 16)
            return true;
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void requestStoragePermissionsIfNeeded() {
        if (!waitingForPermRequestResult) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            waitingForPermRequestResult = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            restartImageLoader();
            waitingForPermRequestResult = false;
        }
    }

    private void restartImageLoader() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }
}
