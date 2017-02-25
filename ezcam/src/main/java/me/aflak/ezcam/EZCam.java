package me.aflak.ezcam;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Omar on 23/02/2017.
 */

public class EZCam {
    private Context context;
    private EZCamCallback cameraCallback;

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private String currentCamera;

    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest.Builder captureRequestBuilderImageReader;
    private CameraCharacteristics cameraCharacteristics;
    private ImageReader imageReader;

    public final static int FRONT = 0;
    public final static int BACK = 1;

    public EZCam(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void setCameraCallback(EZCamCallback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    public Size[] selectCamera(int id) {
        try {
            String[] camerasAvailable = cameraManager.getCameraIdList();
            if (id == BACK) {
                currentCamera = camerasAvailable[0];
            }
            else if (id == FRONT) {
                currentCamera = camerasAvailable[1];
            }
            else{
                notifyError("Camera id was not found.");
                return null;
            }

            cameraCharacteristics = cameraManager.getCameraCharacteristics(currentCamera);
            StreamConfigurationMap scm = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (scm != null) {
                return scm.getOutputSizes(ImageFormat.JPEG);
            } else {
                notifyError("Could not get camera size preview.");
            }
        } catch (CameraAccessException e) {
            notifyError(e.getMessage());
        }
        return null;
    }

    public void openCamera() {
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

    public void preparePreview(int height, int width, int templateType, Surface ...outputSurface) {
        if(!checkCameraDevice()){
            return;
        }

        imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
        imageReader.setOnImageAvailableListener(onImageAvailable, null);

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(templateType);
            for(Surface surface : outputSurface) {
                captureRequestBuilder.addTarget(surface);
            }

            captureRequestBuilderImageReader = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            List<Surface> surfaceList = new ArrayList<>();
            surfaceList.add(imageReader.getSurface());
            Collections.addAll(surfaceList, outputSurface);

            cameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
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
            notifyError(e.getMessage());
        }
    }

    public void startPreview(){
        if(!checkCaptureSession()){
            return;
        }

        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            notifyError(e.getMessage());
        }
    }

    public void stopPreview(){
        if(!checkCaptureRequest()){
            return;
        }

        try {
            cameraCaptureSession.stopRepeating();
        } catch (CameraAccessException e) {
            notifyError(e.getMessage());
        }
    }

    public void closeCamera(){
        if(!checkCameraDevice()){
            return;
        }

        cameraDevice.close();
    }

    public void takePicture(){
        captureRequestBuilderImageReader.addTarget(imageReader.getSurface());
        captureRequestBuilderImageReader.set(CaptureRequest.JPEG_ORIENTATION, cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));
        try {
            cameraCaptureSession.capture(captureRequestBuilderImageReader.build(), null, null);
        } catch (CameraAccessException e) {
            notifyError(e.getMessage());
        }
    }

    private ImageReader.OnImageAvailableListener onImageAvailable = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            captureRequestBuilderImageReader.removeTarget(imageReader.getSurface());
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