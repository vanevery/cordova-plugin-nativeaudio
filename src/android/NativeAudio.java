//
//
//  NativeAudio.java
//
//  Created by Sidney Bofah on 2014-06-26.
//

package com.rjfun.cordova.plugin.nativeaudio;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;

import static android.R.attr.data;

public class NativeAudio extends CordovaPlugin implements AudioManager.OnAudioFocusChangeListener {

    /* options */
    public static final String OPT_FADE_MUSIC = "fadeMusic";

	public static final String ERROR_NO_AUDIOID="A reference does not exist for the specified audio id.";
	public static final String ERROR_AUDIOID_EXISTS="A reference already exists for the specified audio id.";
	
	public static final String SET_OPTIONS="setOptions";
	public static final String PRELOAD_SIMPLE="preloadSimple";
	public static final String PRELOAD_COMPLEX="preloadComplex";
	public static final String PLAY="play";
	public static final String STOP="stop";
    public static final String PAUSE="pause"; // Added
	public static final String LOOP="loop";
	public static final String UNLOAD="unload";
    public static final String ADD_COMPLETE_LISTENER="addCompleteListener";
	public static final String SET_VOLUME_FOR_COMPLEX_ASSET="setVolumeForComplexAsset";
//	public static final String IS_READY="isReady";

	private static final String LOGTAG = "NativeAudio";
	
	Intent playbackServiceIntent;
	BackgroundAudioService baService;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder baBinder) {
			Log.v(LOGTAG,"*** onServiceConnected ***");
			baService = ((BackgroundAudioService.BackgroundAudioServiceBinder) baBinder)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			baService = null;
		}
	};

	// No-op
	public void setOptions(JSONObject options) {
	}

	private PluginResult executePreload(JSONArray data) {
			// Call on service
			return baService.executePreload(data);
	}
	
	private PluginResult executePlayOrLoop(String action, JSONArray data) {
			// Call on service
			return baService.executePlayOrLoop(action, data);
	}

    // Added
    private PluginResult executePause(JSONArray data) {
        // Call on service
		return baService.executePause(data);
    }
    
	private PluginResult executeStop(JSONArray data) {
		// Call on service
		return baService.executeStop(data);
	}

	private PluginResult executeUnload(JSONArray data) {
		// Call on service
		return baService.executeUnload(data);
	}

	private PluginResult executeSetVolumeForComplexAsset(JSONArray data) {
		// Call on service
		return baService.executeSetVolumeForComplexAsset(data);
	}

	private PluginResult executeAddCompleteCallback(JSONArray data, CallbackContext callbackContext) {
		return baService.executeAddCompleteCallback(data, callbackContext);
	}

	@Override
	protected void pluginInitialize() {
		Log.v(LOGTAG,"*** pluginInitialize ***");

		AudioManager am = (AudioManager)cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);

		int result = am.requestAudioFocus(this,
				// Use the music stream.
				AudioManager.STREAM_MUSIC,
				// Request permanent focus.
				AudioManager.AUDIOFOCUS_GAIN);

		// Instantiate service
		playbackServiceIntent = new Intent(this.cordova.getActivity(), BackgroundAudioService.class);
		this.cordova.getActivity().startService(playbackServiceIntent);
		this.cordova.getActivity().bindService(playbackServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		
		// Allow android to receive the volume events
		this.webView.setButtonPlumbedToJs(KeyEvent.KEYCODE_VOLUME_DOWN, false);
		this.webView.setButtonPlumbedToJs(KeyEvent.KEYCODE_VOLUME_UP, false);
	}

	@Override
	public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) {
		Log.d(LOGTAG, "Plugin Called: " + action);
		
		PluginResult result = null;

		try {
			if (SET_OPTIONS.equals(action)) {
                JSONObject options = data.optJSONObject(0);
                this.setOptions(options);
                callbackContext.sendPluginResult( new PluginResult(Status.OK) );

			} else if (PRELOAD_SIMPLE.equals(action)) {
				cordova.getThreadPool().execute(new Runnable() {
		            public void run() {
		            	callbackContext.sendPluginResult( executePreload(data) );
		            }
		        });				
				
			} else if (PRELOAD_COMPLEX.equals(action)) {
				cordova.getThreadPool().execute(new Runnable() {
		            public void run() {
		            	callbackContext.sendPluginResult( executePreload(data) );
		            }
		        });				

			} else if (PLAY.equals(action) || LOOP.equals(action)) {
				cordova.getThreadPool().execute(new Runnable() {
		            public void run() {
		            	callbackContext.sendPluginResult( executePlayOrLoop(action, data) );
		            }
		        });				
				
            } else if (PAUSE.equals(action)) {
				// Added
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        callbackContext.sendPluginResult( executePause(data) );
                    }
                });
                
            } else if (STOP.equals(action)) {
				cordova.getThreadPool().execute(new Runnable() {
		            public void run() {
		            	callbackContext.sendPluginResult( executeStop(data) );
		            }
		        });

            } else if (UNLOAD.equals(action)) {
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        executeStop(data);
                        callbackContext.sendPluginResult( executeUnload(data) );
                    }
                });
            } else if (ADD_COMPLETE_LISTENER.equals(action)) {
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						callbackContext.sendPluginResult( executeAddCompleteCallback(data, callbackContext) );
					}
				});
	    	} else if (SET_VOLUME_FOR_COMPLEX_ASSET.equals(action)) {
				cordova.getThreadPool().execute(new Runnable() {
			public void run() {
						callbackContext.sendPluginResult( executeSetVolumeForComplexAsset(data) );
                    }
                 });
	    	}
            else {
                result = new PluginResult(Status.OK);
            }
		} catch (Exception ex) {
			result = new PluginResult(Status.ERROR, ex.toString());
		}

		if(result != null) callbackContext.sendPluginResult( result );
		return true;
	}

    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            // Pause playback
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Resume playback
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Stop playback
        }
    }

//     @Override
//     public void onPause(boolean multitasking) {
//         super.onPause(multitasking);
// 
//         for (HashMap.Entry<String, NativeAudioAsset> entry : assetMap.entrySet()) {
//             NativeAudioAsset asset = entry.getValue();
//             boolean wasPlaying = asset.pause();
//             if (wasPlaying) {
//                 resumeList.add(asset);
//             }
//         }
//     }
// 
//     @Override
//     public void onResume(boolean multitasking) {
//         super.onResume(multitasking);
//         while (!resumeList.isEmpty()) {
//             NativeAudioAsset asset = resumeList.remove(0);
//             asset.resume();
//         }
//     }

}
