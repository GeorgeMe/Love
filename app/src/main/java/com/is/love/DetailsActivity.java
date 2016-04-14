package com.is.love;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.is.love.events.GalleryItemChosenEvent;
import com.is.love.events.PhotoDetailsAvailableEvent;
import com.is.love.interfaces.PhotoModel;
import com.is.love.utils.BusHelper;
import com.is.love.utils.PicassoHelper;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailsActivity extends BaseActivity {
    private static final String TAG = DetailsActivity.class.getSimpleName();

    /* Animations */
    private static final SpringConfig SPRING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(10, 10);
    private Spring mSpring;


    @Bind(R.id.photo_thumbnail)
    ImageView photoThumbnail;
    @Bind(R.id.photo)
    ImageView photo;
    @Bind(R.id.photo_title)
    TextView photoTitle;
    @Bind(R.id.photo_favorites_icon)
    ImageView photoFavoritesIcon;
    @Bind(R.id.photo_favorites)
    TextView photoFavorites;
    @Bind(R.id.photo_favorites_container)
    LinearLayout photoFavoritesContainer;
    @Bind(R.id.info_container)
    RelativeLayout infoContainer;
    @Bind(R.id.photo_details_content)
    RelativeLayout photoDetailsContent;

    View decorView;

    /* Actionbar menu */
    private Menu menu;

    private PhotoModel photoModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        mSpring = SpringSystem.create().createSpring().setSpringConfig(SPRING_CONFIG).addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                // Just tell the UI to update based on the springs current state.
                animate();
            }
        });
        GalleryItemChosenEvent event=(GalleryItemChosenEvent)getIntent().getSerializableExtra("photo");
        if (event != null && event.getPhoto() != null) {
            photoModel=event.getPhoto();
            // Clear previous photo
            photoThumbnail.setImageBitmap(null);
            photo.setImageBitmap(null);
            // Load new photo
            bindImages(event.getPhoto());
            bindTexts(event.getPhoto());
            // Also, set photo title on actionbar
            getSupportActionBar().setTitle(event.getPhoto().getAuthorName());;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_details;
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initLayout() {
        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    uiRestore();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        BusHelper.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHelper.unregister(this);
        decorView.setOnSystemUiVisibilityChangeListener(null);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                break;
            case R.id.action_share:
                // Share current photo
                share(DetailsActivity.this,photoModel);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @OnClick(R.id.photo)
    public void onClick() {
        if (mSpring.getCurrentValue() == 0) {
            uiHide();
        } else {
            uiRestore();
        }
    }
    /**
     * New details for photo available. Rebind texts to add new info
     */
    @Subscribe
    public void onPhotoDetailsAvailableEvent(PhotoDetailsAvailableEvent event) {
        if (event != null && event.getPhoto() != null) {
            bindTexts(event.getPhoto());
        }
    }
    /**
     * Load photo info from photoModel.
     * First load thumbnail as background, and then load large photo in foreground
     *
     * @param photoModel Object containing photo info
     */
    private void bindImages(final PhotoModel photoModel) {
        // Start by loading thumbnail photo for background image (this should be instant) Then load current large photo
        PicassoHelper.loadWithBlur(this, photoModel.getSmallUrl(), photoThumbnail, new Callback() {
            @Override
            public void onError() {
                PicassoHelper.load(DetailsActivity.this, photoModel.getLargeUrl(), photo);
            }

            @Override
            public void onSuccess() {
                PicassoHelper.load(DetailsActivity.this, photoModel.getLargeUrl(), photo);
            }
        });
    }

    private void bindTexts(PhotoModel photoModel) {
        // Photo Author is always present
        photoTitle.setText(photoModel.getTitle());

        // Photo Favorites is an extra data, could not be available
        if (photoModel.getFavorites() == null) {
            photoFavoritesContainer.setVisibility(View.INVISIBLE);

        } else {
            photoFavoritesContainer.setVisibility(View.VISIBLE);
            photoFavorites.setText(String.valueOf(photoModel.getFavorites()));
        }
    }


    /**
     * Execute spring animations
     */
    private void animate() {
        // Map the spring to info bar position so that its hidden off screen and bounces in on ui restore.
        float position = (float) SpringUtil.mapValueFromRangeToRange(mSpring.getCurrentValue(), 0, 1, 0, infoContainer.getHeight());
        infoContainer.setTranslationY(position);
    }

    /**
     * Hide action bar and info container
     */
    private void uiHide() {
        // Animate UI away
        mSpring.setEndValue(1);
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        // If KITKAT, request immersion
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        decorView.setSystemUiVisibility(flags);

    }

    /**
     * Restore UI original visibility
     */
    private void uiRestore() {
        // Animate back UI
        mSpring.setEndValue(0);
        // Clear all flags
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    /**
     * Launch intent to share current photo
     */
    public void share(final Context ctx,final PhotoModel photo) {

        // Picasso already has cached this image, so extract cached bitmap from its cache
        Picasso.with(ctx).load(photo.getLargeUrl()).into(new Target() {
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // Get bitmap uri from filesystem and create intent with it.
                shareBitmap(ctx, bitmap, photo.getTitle());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
				 /* nothing to do */
            }
        });

    }
    /**
     * Get bitmap uri from filesystem and create intent with it.
     * @param bitmap Image bitmap
     * @param title Image title
     */
    private void shareBitmap(Context ctx, Bitmap bitmap, String title) {
        // TODO: do this in a new separate thread if needed
        String path = MediaStore.Images.Media.insertImage(ctx.getContentResolver(), bitmap, title, null);
        if (path == null) {
            showError(R.string.share_error);
        } else {
            Uri uri = Uri.parse(path);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setType("image/png");
            ctx.startActivity(intent);
        }
    }

    public void showError(int errorId) {
        Toast.makeText(this, getString(errorId), Toast.LENGTH_SHORT).show();
    }

}
