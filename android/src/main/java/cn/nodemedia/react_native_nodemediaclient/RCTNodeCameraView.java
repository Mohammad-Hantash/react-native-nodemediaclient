//
//  RCTNodeCameraView.java
//
//  Created by Mingliang Chen on 2017/11/29.
//  Copyright © 2017年 NodeMedia. All rights reserved.
//

package cn.nodemedia.react_native_nodemediaclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.ByteArrayOutputStream;

import cn.nodemedia.NodeCameraView;
import cn.nodemedia.NodePublisher;
import cn.nodemedia.NodePublisherDelegate;

public class RCTNodeCameraView extends NodeCameraView implements LifecycleEventListener {
    private NodePublisher mNodePublisher;
    private Boolean isAutoPreview = false;

    private int cameraId = -1;
    private boolean cameraFrontMirror = true;

    private int audioBitrate = 32000;
    private int audioProfile = 0;
    private int audioSamplerate = 44100;
    private static String CURRENT_FRAME_EVENT_NAME = "CURRENT_FRAME_EVENT_NAME";

    private int videoPreset = NodePublisher.VIDEO_PPRESET_4X3_480;
    private int videoFPS = 20;
    private int videoBitrate = 400000;
    private int videoProfile = NodePublisher.VIDEO_PROFILE_BASELINE;
    private boolean videoFrontMirror = false;

    private boolean denoise = false;
    private int smoothSkinLevel = 0;


    public RCTNodeCameraView(@NonNull ThemedReactContext context) {
        super(context);
        context.addLifecycleEventListener(this);

        mNodePublisher = new NodePublisher(context, RCTNodeMediaClient.getPremium());
        mNodePublisher.setNodePublisherDelegate(new NodePublisherDelegate() {
            @Override
            public void onEventCallback(NodePublisher nodePublisher, int i, String s) {
                WritableMap event = Arguments.createMap();
                event.putInt("code", i);
                event.putString("message", "s");
                ReactContext reactContext = (ReactContext) getContext();
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        getId(),
                        "topChange",
                        event);
            }
        });

    }

    public void setOutputUrl(String url) {
        mNodePublisher.setOutputUrl(url);

    }


    public void setCamera(int cameraId, boolean cameraFrontMirror) {
        mNodePublisher.setCameraPreview(this, cameraId, cameraFrontMirror);
        if(isAutoPreview) {
            startPrev();
        }
    }

    public void setAudio(int audioBitrate, int audioProfile,int audioSamplerate) {
        mNodePublisher.setAudioParam(audioBitrate, audioProfile, audioSamplerate);
    }

    public void setVideo(int videoPreset, int videoFPS, int videoBitrate, int videoProfile, boolean videoFrontMirror ) {
        mNodePublisher.setVideoParam(videoPreset, videoFPS, videoBitrate, videoProfile, videoFrontMirror);
    }

    public void setDenoise(boolean denoise) {
        this.denoise = denoise;
    }

    public void setSmoothSkinLevel(int smoothSkinLevel) {
        this.smoothSkinLevel = smoothSkinLevel;
    }

    public int startPrev() {
        return mNodePublisher.startPreview();
    }

    public int stopPrev() {
        return mNodePublisher.stopPreview();
    }

    public int start() {
        return mNodePublisher.start();
    }

    public void stop() {
        mNodePublisher.stop();
    }

    public int switchCam() {
        return mNodePublisher.switchCamera();
    }

    public void captureCurrentViewAsBase64(final int quality){

        mNodePublisher.capturePicture(new NodePublisher.CapturePictureListener() {
            @Override
            public void onCaptureCallback(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                WritableMap params = Arguments.createMap(); // add here the data you want to send
                params.putString("base64",encoded);
                ((ReactContext) getContext())
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(CURRENT_FRAME_EVENT_NAME,params);
            }
        });



    }
    public void audioPreview() {
        isAutoPreview = true;
        if(cameraId >=0) {
            startPrev();
        }

    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        mNodePublisher.stopPreview();
        mNodePublisher.stop();
    }
}
