package me.aflak.ezcam;

import android.media.ImageReader;

/**
 * Created by Omar on 23/02/2017.
 */

public interface EZCamCallback {
    void onCameraReady();
    void onPicture(ImageReader imageReader);
    void onError(String message);
    void onCameraDisconnected();
}
