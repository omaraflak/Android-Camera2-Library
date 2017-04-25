package me.aflak.libraries;

import android.Manifest;
import android.content.Intent;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.aflak.ezcam.EZCam;
import me.aflak.ezcam.EZCamCallback;

public class MainActivity extends AppCompatActivity implements EZCamCallback, View.OnClickListener{
    @BindView(R.id.textureView) TextureView textureView;

    private EZCam cam;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
    private final String TAG = "CAM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        cam = new EZCam(this);
        cam.setCameraCallback(this);
        cam.selectCamera(cam.getCamerasList().get(EZCam.BACK));

        Dexter.withActivity(MainActivity.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                cam.open();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Log.e(TAG, "permission denied");
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
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
        cam.setupPreview(CameraDevice.TEMPLATE_PREVIEW, textureView);
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
        cam.close();
    }
}
