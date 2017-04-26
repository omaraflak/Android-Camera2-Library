package me.aflak.ezcam;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Omar on 23/02/2017.
 */

public class EZCam {
    private Context context;
    private EZCamCallback cameraCallback;

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private String currentCamera;
    private Size previewSize;
    private SparseArray<String> camerasList;

    private CameraCaptureSession cameraCaptureSession;
    private CameraCharacteristics cameraCharacteristics;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest.Builder captureRequestBuilderImageReader;
    private ImageReader imageReader;

    private final int SCREEN_HEIGHT;
    private final int SCREEN_WIDTH;

    public EZCam(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        SCREEN_HEIGHT = displayMetrics.heightPixels;
        SCREEN_WIDTH = displayMetrics.widthPixels;
    }

    public void setCameraCallback(EZCamCallback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    public SparseArray<String> getCamerasList(){
        camerasList = new SparseArray<>();
        try {
            String[] camerasAvailable = cameraManager.getCameraIdList();
            CameraCharacteristics cam;
            Integer characteristic;
            for (String id : camerasAvailable){
                cam = cameraManager.getCameraCharacteristics(id);
                characteristic = cam.get(CameraCharacteristics.LENS_FACING);
                if (characteristic!=null){
                    switch (characteristic){
                        case CameraCharacteristics.LENS_FACING_FRONT:
                            camerasList.put(CameraCharacteristics.LENS_FACING_FRONT, id);
                            break;

                        case CameraCharacteristics.LENS_FACING_BACK:
                            camerasList.put(CameraCharacteristics.LENS_FACING_BACK, id);
                            break;

                        case CameraCharacteristics.LENS_FACING_EXTERNAL:
                            camerasList.put(CameraCharacteristics.LENS_FACING_EXTERNAL, id);
                            break;
                    }
                }
            }
            return camerasList;
        } catch (CameraAccessException e) {
            notifyError(e.getLocalizedMessage());
            return null;
        }
    }

    public void selectCamera(int id) {
        if(camerasList == null){
            getCamerasList();
        }

        currentCamera = camerasList.get(id, null);
        if(currentCamera == null) {
            notifyError("Camera id not found.");
            return;
        }

        try {
            cameraCharacteristics = cameraManager.getCameraCharacteristics(currentCamera);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if(map != null) {
                previewSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
                imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 1);
                imageReader.setOnImageAvailableListener(onImageAvailable, null);
            }
            else{
                notifyError("Could not get configuration map.");
            }
        } catch (CameraAccessException e) {
            notifyError(e.getLocalizedMessage());
        }
    }

    public void open() {
        if(!checkCameraId()){
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            notifyError("You don't have the required permissions.");
            return;
        }

        try {
            cameraManager.openCamera(currentCamera, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    if(cameraCallback != null){
                        cameraCallback.onCameraOpened();
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    if(cameraCallback != null){
                        cameraCallback.onError("Camera device is no longer available for use.");
                        cameraCallback.onCameraDisconnected();
                    }
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    switch (error){
                        case CameraDevice.StateCallback.ERROR_CAMERA_DEVICE:
                            notifyError("Camera device has encountered a fatal error.");
                            break;
                        case CameraDevice.StateCallback.ERROR_CAMERA_DISABLED:
                            notifyError("Camera device could not be opened due to a device policy.");
                            break;
                        case CameraDevice.StateCallback.ERROR_CAMERA_IN_USE:
                            notifyError("Camera device is in use already.");
                            break;
                        case CameraDevice.StateCallback.ERROR_CAMERA_SERVICE:
                            notifyError("Camera service has encountered a fatal error.");
                            break;
                        case CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE:
                            notifyError("Camera device could not be opened because there are too many other open camera devices.");
                            break;
                    }
                }
            }, null);
        } catch (CameraAccessException e) {
            notifyError("Could not open camera. May be used by another application.");
        }
    }

    public void setupPreview(final int templateType, final TextureView outputSurface) {
        if(!checkCameraDevice()){
            return;
        }

        if(outputSurface.isAvailable()){
            setupPreview_(templateType, outputSurface.getSurfaceTexture());
        }
        else{
            outputSurface.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    setAspectRatioTextureView(previewSize, outputSurface);
                    setupPreview_(templateType, surface);
                }

                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {return false;}
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
            });
        }
    }

    private void setAspectRatioTextureView(Size previewSize, TextureView textureView)
    {
        int rotation = ((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
        int newWidth=previewSize.getWidth(), newHeight=previewSize.getHeight();

        textureView.setPivotX(textureView.getWidth() / 2);
        textureView.setPivotY(textureView.getHeight() / 2);

        switch (rotation) {
            case Surface.ROTATION_0: // portrait
                newWidth = SCREEN_WIDTH;
                newHeight = (SCREEN_WIDTH * previewSize.getWidth() / previewSize.getHeight());
                break;

            case Surface.ROTATION_180: // weird...
                newWidth = SCREEN_WIDTH;
                newHeight = (SCREEN_WIDTH * previewSize.getWidth() / previewSize.getHeight());
                textureView.setRotation(180);
                break;

            case Surface.ROTATION_90: // rotate to left
                if(previewSize.getHeight()-SCREEN_HEIGHT > previewSize.getWidth()-SCREEN_WIDTH) {
                    newWidth = (SCREEN_HEIGHT * previewSize.getWidth() / previewSize.getHeight());
                    newHeight = SCREEN_HEIGHT;
                }
                else{
                    newWidth = SCREEN_WIDTH;
                    newHeight = (SCREEN_WIDTH * previewSize.getHeight() / previewSize.getWidth());
                }
                textureView.setRotation(270);
                break;

            case Surface.ROTATION_270: // rotate to right
                if(previewSize.getHeight()-SCREEN_HEIGHT > previewSize.getWidth()-SCREEN_WIDTH) {
                    newWidth = (SCREEN_HEIGHT * previewSize.getWidth() / previewSize.getHeight());
                    newHeight = SCREEN_HEIGHT;
                }
                else{
                    newWidth = SCREEN_WIDTH;
                    newHeight = (SCREEN_WIDTH * previewSize.getHeight() / previewSize.getWidth());
                }
                textureView.setRotation(90);
                break;
        }

        textureView.setLayoutParams(new FrameLayout.LayoutParams(newWidth, newHeight, Gravity.CENTER));
    }

    private void setupPreview_(int templateType, SurfaceTexture surfaceTexture){
        Surface surface = new Surface(surfaceTexture);

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(templateType);
            captureRequestBuilder.addTarget(surface);

            captureRequestBuilderImageReader = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilderImageReader.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    if(cameraCallback != null){
                        cameraCallback.onPreviewReady();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    notifyError("Could not configure capture session.");
                }
            }, null);
        } catch (CameraAccessException e) {
            notifyError(e.getLocalizedMessage());
        }
    }

    public void startPreview(){
        if(!checkCaptureSession()){
            return;
        }

        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            notifyError(e.getLocalizedMessage());
        }
    }

    public void stopPreview(){
        if(!checkCaptureRequest()){
            return;
        }

        try {
            cameraCaptureSession.stopRepeating();
        } catch (CameraAccessException e) {
            notifyError(e.getLocalizedMessage());
        }
    }

    public void close(){
        if(!checkCameraDevice()){
            return;
        }

        cameraDevice.close();
    }

    public void takePicture(){
        captureRequestBuilderImageReader.set(CaptureRequest.JPEG_ORIENTATION, cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));
        try {
            cameraCaptureSession.capture(captureRequestBuilderImageReader.build(), null, null);
        } catch (CameraAccessException e) {
            notifyError(e.getLocalizedMessage());
        }
    }

    private ImageReader.OnImageAvailableListener onImageAvailable = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            if(cameraCallback != null){
                cameraCallback.onPicture(imageReader);
            }
        }
    };

    private boolean checkCameraId(){
        if(currentCamera == null){
            notifyError("selectCamera() has not been called.");
            return false;
        }
        return true;
    }

    private boolean checkCameraDevice(){
        if(cameraDevice == null){
            notifyError("openCamera() has not been called.");
            return false;
        }
        return true;
    }

    private boolean checkCaptureSession(){
        if(cameraCaptureSession == null){
            notifyError("preparePreview() has not been called.");
            return false;
        }
        return true;
    }

    private boolean checkCaptureRequest(){
        if(captureRequestBuilder == null){
            notifyError("startPreview() has not been called.");
            return false;
        }
        return true;
    }

    private void notifyError(String message) {
        if (cameraCallback != null) {
            cameraCallback.onError(message);
        }
    }

    public File saveImage(ImageReader imageReader, String filename) throws IOException {
        Image image = imageReader.acquireLatestImage();
        File file = new File(context.getFilesDir(), filename);
        if(file.exists()) {
            image.close();
            return null;
        }
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = new FileOutputStream(file);
        output.write(bytes);
        image.close();
        output.close();
        return file;
    }
}