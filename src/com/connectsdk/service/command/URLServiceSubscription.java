/*
 * URLServiceSubscription
 * Connect SDK
 * 
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 19 Jan 2014
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

package com.connectsdk.service.command;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.connectsdk.service.DeviceService;
import com.connectsdk.service.capability.listeners.ResponseListener;

/**
 * Internal implementation of ServiceSubscription for URL-based commands
 */
public class URLServiceSubscription<T extends ResponseListener<?>> extends ServiceCommand<T> implements ServiceSubscription<T> {
	private List<T> listeners = new ArrayList<T>();

	public URLServiceSubscription(DeviceService service, String uri, JSONObject payload, ResponseListener<Object> listener) {
		super(service, uri, payload, listener);
	}
		
	public void send() {
		this.subscribe();
	}
	
	public void subscribe() {
		if ( !(httpMethod.equalsIgnoreCase(TYPE_GET)
				|| httpMethod.equalsIgnoreCase(TYPE_POST)) ) {
			httpMethod = "subscribe";
		}
		service.sendCommand(this);
	}
	
	public void unsubscribe() {
		service.unsubscribe(this);
	}
	
	public T addListener(T listener) {
		listeners.add(listener);
		
		return listener;
	}
	
	public void removeListener(T listener) {
		listeners.remove(listener);
	}
	
	public List<T> getListeners() {
		return listeners;
	}
}
