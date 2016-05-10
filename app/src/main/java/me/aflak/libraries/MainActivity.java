package me.aflak.libraries;

import android.graphics.SurfaceTexture;
import android.media.ImageReader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;

import java.io.IOException;

import me.aflak.ezcam.EZCam;

public class MainActivity extends AppCompatActivity {
    private EZCam cam;
    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cam = new EZCam(this);
        cam.selectCamera(EZCam.FRONT);
        cam.setStopPreviewOnPicture(true);
        cam.setEZCamCallback(new EZCam.EZCamCallback() {
            @Override
            public void onPicture(ImageReader reader) {
                // picture available
                try {
                    cam.saveImage(reader, "image.jpeg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                // error occurred
            }
        });

        textureView = (TextureView)findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                cam.startPreview(surfaceTexture, i, i1);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }
        });

        // take picture
        cam.takePicture();
        // stop preview
        cam.stopPreview();
        // resume preview
        cam.resumePreview();
    }
}
