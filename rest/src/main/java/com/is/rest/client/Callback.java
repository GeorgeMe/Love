/*
 * Copyright (C) 2013 47 Degrees, LLC
 * http://47deg.com
 * http://apps.ly
 * hello@47deg.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.is.rest.client;

import android.content.Context;
import com.is.rest.cache.CacheAwareCallback;
import com.is.rest.cache.CacheInfo;

/**
 * A callback to a request on a Rest Service
 * @param <Result>
 */
public abstract class Callback<Result> extends CacheAwareCallback<Result> {

    protected Callback(CacheInfo cacheInfo) {
        super(cacheInfo);
    }

    protected Callback(Context context, Class<Result> targetClass) {
        super(context, targetClass);
    }

    protected Callback(Class<Result> targetClass) {
        super(targetClass);
    }

    protected Callback() {
    }

    protected Callback(Context context) {
        super(context);
    }
}
