package com.is.love.events;

import com.is.love.interfaces.PhotoModel;

import java.io.Serializable;

/**
 * Created by lgvalle on 22/07/14.
 * <p/>
 * Event: Item selected in gallery
 */
public class GalleryItemChosenEvent implements Serializable{
	private PhotoModel photo;

	public GalleryItemChosenEvent(PhotoModel photo) {
		this.photo = photo;
	}

	public PhotoModel getPhoto() {
		return photo;
	}
}
