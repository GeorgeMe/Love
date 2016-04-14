package com.is.love;

import android.app.Application;
import com.is.love.fivehundredpxs.Api500pxModule;
import com.is.love.utils.TypefaceUtil;

/**
 * Created by lgvalle on 21/07/14.
 */
public class BeautifulPhotosApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		// Init service module
		Api500pxModule.init(this);
		// Replace font typeface in all application
		TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/RobotoCondensed-Regular.ttf");
	}

}
