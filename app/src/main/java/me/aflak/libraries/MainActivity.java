package me.aflak.libraries;

import android.content.Intent;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.aflak.ezcam.EZCam;
import me.aflak.ezcam.EZCamCallback;

public class MainActivity extends AppCompatActivity implements EZCamCallback, View.OnClickListener{
    private TextureView textureView;
    private EZCam cam;
    private Size[] sizes;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
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
        cam.preparePreview(sizes[0].getHeight(),
                sizes[0].getWidth(),
                CameraDevice.TEMPLATE_PREVIEW,
                new Surface(textureView.getSurfaceTexture()));
    }

    @Override
    public void onCameraDisconnected() {
        Log.e(TAG, "Camera disconnected");
    }

    @Override
    public void onPreviewReady() {
        cam.startPreview();
        textureView.setOnClickListener(this);
    }

    @Override
    public void onPicture(ImageReader imageReader) {
        cam.stopPreview();
        try {
            String filename = "image_"+dateFormat.format(new Date())+".jpg"; // image_current_date.jpg
            cam.saveImage(imageReader, filename); // save image to internal storage i.e. new File(getFilesDir(), "image.jpg")

            Intent intent = new Intent(this, DisplayActivity.class);
            intent.putExtra("filename", filename);
            startActivity(intent);
            finish();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cam.closeCamera();
    }
}
