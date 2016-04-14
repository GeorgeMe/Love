package com.is.love.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.is.love.R;
import com.is.love.interfaces.PhotoModel;
import com.is.love.utils.PicassoHelper;
import com.is.love.utils.Renderer;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by lgvalle on 21/07/14.
 * <p/>
 * Concrete renderer for photomodel object.
 * <p/>
 * This class binds a concrete view with a concrete object.
 */
public class GalleryItemRenderer extends Renderer<PhotoModel> {
	private static final SpringConfig SPRING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(10, 10);
	private final Spring mSpring = SpringSystem.create().createSpring().setSpringConfig(SPRING_CONFIG).addListener(new SimpleSpringListener() {
		@Override
		public void onSpringUpdate(Spring spring) {
			// Just tell the UI to update based on the springs current state.
			animate();
		}
	});
	/* Views */
	@Bind(R.id.photo)
	ImageView ivPhoto;
	@Bind(R.id.photo_title)
	TextView tvPhotoTitle;

	public GalleryItemRenderer() {
		super();
	}

	private GalleryItemRenderer(PhotoModel content, LayoutInflater inflater, ViewGroup parent) {
		super(content);
		this.rootView = inflater.inflate(R.layout.row_photo, parent, false);
		this.rootView.setTag(this);
		ButterKnife.bind(this, rootView);
	}

	/**
	 * Generate a view for this renderer content.
	 *
	 * @param ctx Context
	 * @return A view representing current content
	 */
	@Override
	public View render(Context ctx) {
		// Load photo
		PicassoHelper.load(ctx, getContent().getSmallUrl(), ivPhoto);
		// Set photo title
		tvPhotoTitle.setText(getContent().getTitle());
		return rootView;
	}

	/**
	 * Creates a concrete implementation of a renderer
	 *
	 * @param content Model object for which concrete renderer is providing a view
	 */
	@Override
	protected <T> Renderer create(T content, LayoutInflater inflater, ViewGroup parent) {
		return new GalleryItemRenderer((PhotoModel) content, inflater, parent);
	}

	/**
	 * Execute spring animations
	 */
	private void animate() {
		// Map the spring to the photo and the photo title positions so that they are hidden off the row and bounces in on render or recycle row.
		float positionTitle = (float) SpringUtil.mapValueFromRangeToRange(mSpring.getCurrentValue(), 0, 1, ivPhoto.getHeight(), 0);
		float positionPhoto = (float) SpringUtil.mapValueFromRangeToRange(mSpring.getCurrentValue(), 0, 1, ivPhoto.getHeight()/2, 0);
		tvPhotoTitle.setTranslationY(positionTitle);
		ivPhoto.setTranslationY(positionPhoto);
	}

	@Override
	protected void onRecycle(PhotoModel content) {
		super.onRecycle(content);
		mSpring.setCurrentValue(0);
		mSpring.setEndValue(1);
	}
}
