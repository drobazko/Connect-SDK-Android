package com.connectsdk.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.connectsdk.core.Util;
import com.connectsdk.service.capability.WebAppLauncher;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;
import com.connectsdk.service.sessions.LaunchSession;
import com.connectsdk.service.sessions.LaunchSession.LaunchSessionType;
import com.connectsdk.service.sessions.MultiScreenWebAppSession;
import com.connectsdk.service.sessions.WebAppSession;
import com.connectsdk.service.sessions.WebAppSession.LaunchListener;
import com.samsung.multiscreen.application.Application;
import com.samsung.multiscreen.application.Application.Status;
import com.samsung.multiscreen.application.ApplicationAsyncResult;
import com.samsung.multiscreen.application.ApplicationError;
import com.samsung.multiscreen.device.Device;
import com.samsung.multiscreen.device.DeviceAsyncResult;
import com.samsung.multiscreen.device.DeviceError;
import com.samsung.multiscreen.device.DeviceFactory;

public class MultiScreenService extends DeviceService implements WebAppLauncher {
	public static final String ID = "MultiScreen";
	
	Device multiScreenDevice;

	public MultiScreenService(ServiceDescription serviceDescription,
			ServiceConfig serviceConfig) {
		super(serviceDescription, serviceConfig);

		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("DeviceName", serviceDescription.getFriendlyName());
		map.put("DialURI", serviceDescription.getApplicationURL());
		map.put("IP", serviceDescription.getIpAddress());
		map.put("ModelDescription", serviceDescription.getModelDescription());
		map.put("ModelName", serviceDescription.getModelName());
		map.put("ServiceURI", serviceDescription.getServiceURI());
		
		this.multiScreenDevice = DeviceFactory.createWithMap(map);
	}

	public static JSONObject discoveryParameters() {
		JSONObject params = new JSONObject();
		
		try {
			params.put("serviceId", ID);
//			params.put("filter",  "urn:samsung.com:service:MultiScreenService:1");
			params.put("filter",  "urn:dial-multiscreen-org:service:dial:1");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return params;
	}
	
	@Override
	public WebAppLauncher getWebAppLauncher() {
		return this;
	}

	@Override
	public CapabilityPriorityLevel getWebAppLauncherCapabilityLevel() {
		return CapabilityPriorityLevel.NORMAL;
	}

	@Override
	public void launchWebApp(String webAppId, WebAppSession.LaunchListener listener) {
		launchWebApp(webAppId, null, listener);
	}

	@Override
	public void launchWebApp(String webAppId, boolean relaunchIfRunning,
			LaunchListener listener) {
		launchWebApp(webAppId, null, relaunchIfRunning, listener);
	}

	@Override
	public void launchWebApp(String webAppId, JSONObject params,
			final LaunchListener listener) {
		
		multiScreenDevice.getApplication(webAppId, new DeviceAsyncResult<Application>() {

			@Override
			public void onError(DeviceError error) {
				Log.d(Util.T, "getApplication: onError: " + error.toString());
			}

			@Override
			public void onResult(final Application application) {
				application.launch(new ApplicationAsyncResult<Boolean>() {

					@Override
					public void onError(ApplicationError error) {
						Util.postError(listener, new ServiceCommandError((int)error.getCode(), error.getMessage(), error));
					}

					@Override
					public void onResult(Boolean result) {
						if (result == true) {
							LaunchSession launchSession = LaunchSession.launchSessionForAppId(application.getRunTitle());
							launchSession.setService(MultiScreenService.this);
							launchSession.setSessionType(LaunchSessionType.WebApp);
							
							Util.postSuccess(listener, new MultiScreenWebAppSession(launchSession, MultiScreenService.this));
						}
						else {
							Util.postError(listener, new ServiceCommandError(0, "Failed to launch application", null));
						}
					}
				});
			}
		});
	}

	@Override
	public void launchWebApp(final String webAppId, final JSONObject params,
			boolean relaunchIfRunning, final LaunchListener listener) {
		if (webAppId == null) {
			Util.postError(listener, new ServiceCommandError(0, "Must pass a web App id", null));
			return;
		}
		
		if (relaunchIfRunning) {
			launchWebApp(webAppId, params, listener);
		} else {
			multiScreenDevice.getApplication(webAppId, new DeviceAsyncResult<Application>() {

				@Override
				public void onError(DeviceError error) {
					Util.postError(listener, new ServiceCommandError((int)error.getCode(), error.getMessage(), error));
				}

				@Override
				public void onResult(final Application application) {
					Application.Status status = application.getLastKnownStatus();
					if (status == Status.RUNNING) {
						final LaunchSession launchSession = LaunchSession.launchSessionForAppId(application.getRunTitle());
						launchSession.setService(MultiScreenService.this);
						launchSession.setSessionType(LaunchSessionType.WebApp);
						
						final MultiScreenWebAppSession session = new MultiScreenWebAppSession(launchSession, MultiScreenService.this);
						session.connect(new ResponseListener<Object>() {

							@Override
							public void onError(ServiceCommandError error) {
								
							}

							@Override
							public void onSuccess(Object object) {
								Util.postSuccess(listener, session);
							}
						});
					}
					else if (status == Status.STOPPED) {
						launchWebApp(webAppId, params, listener);
					}
					else if (status == Status.INSTALLABLE) {
						Util.postError(listener, new ServiceCommandError(0, "Web app is not installed yet", null));
					}
				}
			});
		}
	}
	
	@Override
	public void joinWebApp(final String webAppId, final LaunchListener listener) {
		if (webAppId == null) {
			Util.postError(listener, new ServiceCommandError(0, "Must pass a web App id", null));
			return;
		}
		
		multiScreenDevice.getApplication(webAppId, new DeviceAsyncResult<Application>() {

			@Override
			public void onError(DeviceError error) {
				Util.postError(listener, new ServiceCommandError((int)error.getCode(), error.getMessage(), error));
			}

			@Override
			public void onResult(final Application application) {
				Application.Status status = application.getLastKnownStatus();
				if (status == Status.RUNNING) {
					final LaunchSession launchSession = LaunchSession.launchSessionForAppId(application.getRunTitle());
					launchSession.setService(MultiScreenService.this);
					launchSession.setSessionType(LaunchSessionType.WebApp);
					
					final MultiScreenWebAppSession session = new MultiScreenWebAppSession(launchSession, MultiScreenService.this);
					session.connect(new ResponseListener<Object>() {

						@Override
						public void onError(ServiceCommandError error) {
							
						}

						@Override
						public void onSuccess(Object object) {
							Util.postSuccess(listener, session);
						}
					});
				}
				else if (status == Status.STOPPED) {
					launchWebApp(webAppId, null, listener);
				}
				else if (status == Status.INSTALLABLE) {
					Util.postError(listener, new ServiceCommandError(0, "Web app is not installed yet", null));
				}
			}
		});
	}
	
	@Override
	public void joinWebApp(LaunchSession webAppLaunchSession,
			LaunchListener listener) {
		joinWebApp(webAppLaunchSession.getAppId(), listener);
	}

	@Override
	public void closeWebApp(LaunchSession launchSession,
			final ResponseListener<Object> listener) {
		multiScreenDevice.getApplication(launchSession.getAppId(), new DeviceAsyncResult<Application>() {
				
				@Override
				public void onResult(Application application) {
					application.terminate(new ApplicationAsyncResult<Boolean>() {

						@Override
						public void onError(ApplicationError error) {
							Util.postError(listener, new ServiceCommandError((int)error.getCode(), error.getMessage(), error));							
						}

						@Override
						public void onResult(Boolean result) {
							if (result == true) {
	                        	Util.postSuccess(listener, result);
							}
							else {
								Util.postError(listener, new ServiceCommandError(0, "Failed to close application", null));
							}					
						}
					});
				}
				
				@Override
				public void onError(DeviceError error) {
					Log.d(Util.T, "getApplication: onError: " + error.toString());
					
				}
		});

	}
	
	public Device getMultiScreenDevice() {
		return multiScreenDevice;
	}
	
	@Override
	protected void updateCapabilities() {
		List<String> capabilities = new ArrayList<String>();
	
		capabilities.add(Launch);
		capabilities.add(Message_Send);
		capabilities.add(Message_Receive);
		capabilities.add(Message_Send_JSON);
		capabilities.add(Message_Receive_JSON);
		capabilities.add(Close);

		setCapabilities(capabilities);
	}
}