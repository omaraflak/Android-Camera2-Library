package me.aflak.libraries;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.io.IOException;

import me.aflak.ezcam.EZCam;
import me.aflak.ezcam.EZCamCallback;

public class MainActivity extends AppCompatActivity implements EZCamCallback, View.OnClickListener{
    private TextureView textureView;
    private EZCam cam;
    private Size[] sizes;

    private final String TAG = "CAM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cam = new EZCam(this);
        cam.setCameraCallback(this);

        textureView = (TextureView) findViewById(R.id.textureView);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                textureView.setOnClickListener(MainActivity.this);
                sizes = cam.selectCamera(EZCam.FRONT);
                cam.openCamera(); // needs android.permission.CAMERA
            }

            @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
            @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {return false;}
            @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
        });
    }

    @Override
    public void onClick(View v) {
        cam.takePicture();
    }

    @Override
    public void onError(String message) {
        Log.e(TAG, message);
    }

    @Override
    public void onCameraOpened() {
        Surface surface = new Surface(textureView.getSurfaceTexture());
        int height = sizes[0].getHeight();
        int width = sizes[0].getWidth();
        cam.preparePreview(height, width, CameraDevice.TEMPLATE_PREVIEW, surface);
    }

    @Override
    public void onCameraDisconnected() {
        Log.e(TAG, "Camera disconnected");
    }

    @Override
    public void onPreviewReady() {
        cam.startPreview();
    }

    @Override
    public void onPicture(ImageReader imageReader) {
        cam.stopPreview();
        try {
            cam.saveImage(imageReader, "image.jpg"); // save image to internal storage i.e. new File(getFilesDir(), "image.jpg")
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
