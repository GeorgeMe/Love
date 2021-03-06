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

package com.is.rest.converters.impl;

import com.is.rest.cache.CacheAwareCallback;
import com.is.rest.converters.BodyConverter;
import com.is.rest.exceptions.SerializationException;
import com.is.rest.utils.HeaderUtils;
import com.is.rest.utils.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

/**
 * A Jackson based impl of a Body Converter that ignores unknown properties and know how to serialize non KVC compliant
 * responses such as [{"prop":"my dad should have been an object"}]
 */
public class JacksonBodyConverter implements BodyConverter {

    private ObjectMapper mapper;

    public JacksonBodyConverter() {
        this(new ObjectMapper());
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public JacksonBodyConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }



    @Override
    public <T> HttpEntity toRequestBody(T object, String contentType) {
        Logger.d("JacksonBodyConverter.toRequestBody: object: " + object);
        try {
            String json = mapper.writeValueAsString(object);
            Logger.d("JacksonHttpFormValuesConverter.toRequestBody: json: " + json);
            StringEntity result = new StringEntity(json, "UTF-8");
            result.setContentType(contentType);
            Logger.d("JacksonBodyConverter.toRequestBody: result: " + result);
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new SerializationException(e);
        } catch (JsonMappingException e) {
            throw new SerializationException(e);
        } catch (JsonGenerationException e) {
            throw new SerializationException(e);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromResponseBody(Type target, String contentType, HttpEntity responseBody, CacheAwareCallback<T> callback) {
        Logger.d("JacksonBodyConverter.fromResponseBody: target: " + target + " responseBody: " + responseBody);
        try {
            T result;
            if (callback.isResponseIsCollection()) {
                result = mapper.readValue(responseBody.getContent(), mapper.getTypeFactory().constructCollectionType(callback.getCollectionType(), (Class<T>) target));
            } else {
                result = mapper.readValue(responseBody.getContent(), (Class<T>) target);
            }
            Logger.d("JacksonBodyConverter.fromResponseBody: result: " + result);
            return result;
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public boolean supportsRequestContentType(String contentType) {
        return HeaderUtils.CONTENT_TYPE_JSON.startsWith(contentType);
    }

    @Override
    public boolean supportsResponseContentType(String contentType) {
        return HeaderUtils.CONTENT_TYPE_JSON.startsWith(contentType);
    }
}
