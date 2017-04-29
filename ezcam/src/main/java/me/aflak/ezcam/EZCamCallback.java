package me.aflak.ezcam;

import android.media.Image;

/**
 * Created by Omar on 23/02/2017.
 */

public interface EZCamCallback {
    void onCameraReady();
    void onPicture(Image image);
    void onError(String message);
    void onCameraDisconnected();
}
