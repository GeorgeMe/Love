package com.is.love.events;

import com.is.love.interfaces.PhotoModel;

/**
 * Created by lgvalle on 21/07/14.
 * <p/>
 * Event: new photo details available
 */
public class PhotoDetailsAvailableEvent {

	private final PhotoModel photo;

	public PhotoDetailsAvailableEvent(PhotoModel photo) {
		this.photo = photo;
	}

	public PhotoModel getPhoto() {
		return photo;
	}
}
