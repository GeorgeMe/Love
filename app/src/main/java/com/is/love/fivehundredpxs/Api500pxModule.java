package com.is.love.fivehundredpxs;

import android.content.Context;
import com.is.rest.client.RestClient;
import com.is.rest.client.RestClientFactory;
import com.is.rest.client.RestServiceFactory;

/**
 * Created by lgvalle on 21/07/14.
 *
 * Service REST api module.
 * Builds a rest client based on api interface description
 */
public class Api500pxModule {
	private static final String END_POINT = "https://api.500px.com/v1";
	private static Api500pxService service;

	public static void init(Context ctx) {
		RestClient client = RestClientFactory.defaultClient(ctx);
		service = RestServiceFactory.getService(END_POINT, Api500pxService.class, client);
	}

	/**
	 * Hide constructor
	 */
	private Api500pxModule() {}

	/**
	 * Expose rest client
	 */
	public static Api500pxService getService() {
		return service;
	}

}
