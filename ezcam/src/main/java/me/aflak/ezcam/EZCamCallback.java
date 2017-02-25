package me.aflak.ezcam;

import android.media.ImageReader;

/**
 * Created by Omar on 23/02/2017.
 */

public interface EZCamCallback {
    void onError(String message);
    void onCameraOpened();
    void onCameraDisconnected();
    void onPreviewReady();
    void onPicture(ImageReader imageReader);
}
