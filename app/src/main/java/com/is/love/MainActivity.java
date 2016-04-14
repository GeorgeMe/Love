package com.is.love;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.is.love.events.GalleryItemChosenEvent;
import com.is.love.events.GalleryReloadEvent;
import com.is.love.events.GalleryRequestingMoreElementsEvent;
import com.is.love.events.PhotosAvailableEvent;
import com.is.love.fivehundredpxs.Api500pxModule;
import com.is.love.fivehundredpxs.model.Feature;
import com.is.love.gallery.GalleryItemRenderer;
import com.is.love.interfaces.BeautifulPhotosPresenter;
import com.is.love.interfaces.BeautifulPhotosScreen;
import com.is.love.interfaces.PhotoModel;
import com.is.love.utils.BusHelper;
import com.is.love.utils.RendererAdapter;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class MainActivity extends BaseActivity implements BeautifulPhotosScreen {
    private static final String TAG = MainActivity.class.getSimpleName();
    /* Manage all business logic for this activity */
    private BeautifulPhotosPresenter presenter;
    /* Actionbar title */
    private String title;
    /* Actionbar menu */
    private Menu menu;

    /* Items before list end when loading more elements start */
    private static final int LOAD_OFFSET = 1;
    /* List adapter */
    private RendererAdapter<PhotoModel> adapter;
    /* Save last visible item to know if scrolling up or down */
    private int lastVisible;

    /* Views */
    @Bind(R.id.photo_list)
    ListView list;
    @Bind(R.id.progress_bar)
    ProgressBar pbLoading;

    @Override
    protected void onResume() {
        super.onResume();
        // Register on bus to let activity and presenter listen to events
        BusHelper.register(this);
        BusHelper.register(presenter);
        // Empty list? Ask for some photos!
        if (adapter.isEmpty()) {
            BusHelper.post(new GalleryRequestingMoreElementsEvent());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister every time activity is paused
        BusHelper.unregister(this);
        BusHelper.unregister(presenter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                break;
            case R.id.action_feature_highest_rated:
                presenter.switchFeature(Feature.HighestRated);
                break;
            case R.id.action_feature_popular:
                presenter.switchFeature(Feature.Popular);
                break;
            case R.id.action_share:
                // Share current photo
                presenter.share(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initPresenter() {
        // Init activity presenter with all it's dependencies
        presenter = new BeautifulPhotosPresenterImpl(this, Api500pxModule.getService(), Feature.Popular);
    }

    @Override
    protected void initLayout() {
        ButterKnife.bind(this);

        // Gallery adapter
        adapter = new RendererAdapter<PhotoModel>(LayoutInflater.from(this), new GalleryItemRenderer(), this);
        list.setAdapter(adapter);
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // nothing to do
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (view.getId() == list.getId()) {
                    final int currentFirstVisibleItem = list.getFirstVisiblePosition();
                    if (currentFirstVisibleItem > lastVisible) {
                        hideActionBar();
                    } else if (currentFirstVisibleItem < lastVisible) {
                        showActionBar();
                    }
                    lastVisible = currentFirstVisibleItem;
                    loadMore();
                }
            }
        });
    }

    @Override
    public void showError(int errorId) {
        Toast.makeText(this, getString(errorId), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateTitle(int titleId) {
        title = getString(titleId);
        getSupportActionBar().setTitle(title);
    }
    /**
     * Click on a gallery item
     *
     * @param position Position of clicked item
     */
    @OnItemClick(R.id.photo_list)
    public void onGalleryItemClick(int position) {
        Intent intent=new Intent();
        intent.putExtra("photo",new GalleryItemChosenEvent(adapter.getItem(position)));
        intent.setClass(MainActivity.this,DetailsActivity.class);
        startActivity(intent);
        //BusHelper.post(new GalleryItemChosenEvent(adapter.getItem(position)));
    }

    /**
     * Listen to gallery item selection: open panel al request presenter for photo details
     *
     * @param event Event containing selected item
     */
    @Subscribe
    public void onGalleryItemChosen(GalleryItemChosenEvent event) {
        showError(R.string.app_name);
    }
    /**
     * Get notifications when there are new photos available in the bus
     *
     * @param event Event containing new photos
     */
    @Subscribe
    public void onNewPhotosEvent(PhotosAvailableEvent event) {
        if (event != null && event.getPhotos() != null) {
            // Adapter refresh itself
            adapter.addElements(event.getPhotos());

            // Stop refreshing animation
            setLoading(false);
        }
    }

    /**
     * Listen to gallery refreshing event.
     * Event could be triggered from this class or from main activity. That's why it's better to just listen the bus
     */
    @Subscribe
    public void onGalleryRefreshingEvent(GalleryReloadEvent event) {
        adapter.clear();
        setLoading(true);
    }

    @Subscribe
    public void onGalleryRequestingMoreElementsEvent(GalleryRequestingMoreElementsEvent event) {
        setLoading(true);
    }

    /**
     * Request more items if already scrolled to the end of the list
     */
    private void loadMore() {
        // Load more items if not already loading
        if (!isLoading() && list.getLastVisiblePosition() >= adapter.getCount() - LOAD_OFFSET) {
            setLoading(true);
            BusHelper.post(new GalleryRequestingMoreElementsEvent());
        }
    }

    /**
     * @return true if loading progress bar is visible
     */
    private boolean isLoading() {
        return View.VISIBLE == pbLoading.getVisibility();
    }

    /**
     * When loading, display progress bar
     * @param refreshing True if refreshing, false otherwise
     */
    private void setLoading(boolean refreshing) {
        pbLoading.setVisibility(refreshing ? View.VISIBLE : View.GONE);
    }
}
