package com.rjfun.cordova.plugin.nativeaudio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static android.R.attr.data;
import static com.rjfun.cordova.plugin.nativeaudio.NativeAudio.ERROR_AUDIOID_EXISTS;
import static com.rjfun.cordova.plugin.nativeaudio.NativeAudio.ERROR_NO_AUDIOID;
import static com.rjfun.cordova.plugin.nativeaudio.NativeAudio.LOOP;
import static com.transistorsoft.tslocationmanager.a.e;

public class BackgroundAudioService extends Service {

    private static final String LOGTAG = "BackgroundAudioService";
    private static HashMap<String, NativeAudioAsset> assetMap;
    private static HashMap<String, CallbackContext> completeCallbacks;

    public class BackgroundAudioServiceBinder extends Binder {
        BackgroundAudioService getService() {
            return BackgroundAudioService.this;
        }
    }

    private final IBinder basBinder = new BackgroundAudioServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // Return the BackgroundAudioServiceBinder object
        return basBinder;
    }

    public PluginResult executePreload(JSONArray data) {
        String audioID;
        try {
            audioID = data.getString(0);
            if (!assetMap.containsKey(audioID)) {
                String assetPath = data.getString(1);
                Log.d(LOGTAG, "preloadComplex - " + audioID + ": " + assetPath);

                double volume;
                if (data.length() <= 2) {
                    volume = 1.0;
                } else {
                    volume = data.getDouble(2);
                }

                int voices;
                if (data.length() <= 3) {
                    voices = 1;
                } else {
                    voices = data.getInt(3);
                }

                String fullPath = "www/".concat(assetPath);

                Context ctx = getApplicationContext();
                AssetManager am = ctx.getResources().getAssets();
                AssetFileDescriptor afd = am.openFd(fullPath);

                NativeAudioAsset asset = new NativeAudioAsset(
                        afd, voices, (float)volume);
                assetMap.put(audioID, asset);

                return new PluginResult(PluginResult.Status.OK);
            } else {
                return new PluginResult(PluginResult.Status.ERROR, ERROR_AUDIOID_EXISTS);
            }
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.toString());
        } catch (IOException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.toString());
        }
    }

    public PluginResult executePlayOrLoop(String action, JSONArray data) {
        final String audioID;
        try {
            audioID = data.getString(0);
            //Log.d( LOGTAG, "play - " + audioID );

            if (assetMap.containsKey(audioID)) {
                NativeAudioAsset asset = assetMap.get(audioID);
                if (LOOP.equals(action))
                    asset.loop();
                else
                    asset.play(new Callable<Void>() {
                        public Void call() throws Exception {
                            CallbackContext callbackContext = completeCallbacks.get(audioID);
                            if (callbackContext != null) {
                                JSONObject done = new JSONObject();
                                done.put("id", audioID);
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, done));
                            }
                            return null;
                        }
                    });
            } else {

                return new PluginResult(PluginResult.Status.ERROR, ERROR_NO_AUDIOID);
            }
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.toString());
        } catch (IOException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.toString());
        }

        return new PluginResult(PluginResult.Status.OK);
    }

    // Added
    public PluginResult executePause(JSONArray data) {
        String audioID;
        try {
            audioID = data.getString(0);
            //Log.d( LOGTAG, "stop - " + audioID );

            if (assetMap.containsKey(audioID)) {
                NativeAudioAsset asset = assetMap.get(audioID);
                asset.pause();
            } else {
                return new PluginResult(PluginResult.Status.ERROR, ERROR_NO_AUDIOID);
            }
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.toString());
        }

        return new PluginResult(PluginResult.Status.OK);
    }

    public PluginResult executeStop(JSONArray data) {
        String audioID;
        try {
            audioID = data.getString(0);
            //Log.d( LOGTAG, "stop - " + audioID );

            if (assetMap.containsKey(audioID)) {
                NativeAudioAsset asset = assetMap.get(audioID);
                asset.stop();
            } else {
                return new PluginResult(PluginResult.Status.ERROR, ERROR_NO_AUDIOID);
            }
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.toString());
        }

        return new PluginResult(PluginResult.Status.OK);
    }

    public PluginResult executeUnload(JSONArray data) {
        String audioID;
        try {
            audioID = data.getString(0);
            Log.d( LOGTAG, "unload - " + audioID );

            if (assetMap.containsKey(audioID)) {
                NativeAudioAsset asset = assetMap.get(audioID);
                asset.unload();
                assetMap.remove(audioID);
            } else {
                return new PluginResult(PluginResult.Status.ERROR, ERROR_NO_AUDIOID);
            }
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.toString());
        } catch (IOException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.toString());
        }

        return new PluginResult(PluginResult.Status.OK);
    }

    public PluginResult executeSetVolumeForComplexAsset(JSONArray data) {
        String audioID;
        float volume;
        try {
            audioID = data.getString(0);
            volume = (float) data.getDouble(1);
            Log.d( LOGTAG, "setVolume - " + audioID );

            if (assetMap.containsKey(audioID)) {
                NativeAudioAsset asset = assetMap.get(audioID);
                asset.setVolume(volume);
            } else {
                return new PluginResult(PluginResult.Status.ERROR, ERROR_NO_AUDIOID);
            }
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.toString());
        }
        return new PluginResult(PluginResult.Status.OK);
    }

    public PluginResult executeAddCompleteCallback(JSONArray data, CallbackContext callbackContext) {
        if (completeCallbacks == null) {
            completeCallbacks = new HashMap<String, CallbackContext>();
        }

        try {
            String audioID = data.getString(0);
            completeCallbacks.put(audioID, callbackContext);
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.toString());
        }

        return new PluginResult(PluginResult.Status.OK);
    }

    private void initSoundPool() {

        if (assetMap == null) {
            assetMap = new HashMap<String, NativeAudioAsset>();
        }

    }

    @Override
    public void onCreate() {
        Log.v("PLAYERSERVICE", "onCreate");

        initSoundPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("PLAYERSERVICE", "onStartCommand");

        return START_STICKY;
    }

    public void onDestroy() {
        Log.v("SIMPLESERVICE", "onDestroy");
    }

}
