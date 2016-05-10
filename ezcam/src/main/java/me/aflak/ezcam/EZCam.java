package me.aflak.ezcam;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Omar on 31/12/2015.
 */
public class EZCam {
    private Context context;
    private EZCamCallback listener = null;

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder request;
    private CameraCaptureSession cameraCaptureSession;
    private String selectedCamera;

    private ImageReader jpegImageReader;
    private Surface previewSurface;

    public final static int FRONT = 1;
    public final static int BACK = 2;

    private final String CAM_DOES_NOT_EXIST = "No camera found for the specified id.";
    private final String ERROR_OPENING_CAM = "Error occurred while opening the camera.";
    private final String CAM_DISCONNECT = "Camera has been disconnected.";
    private final String NO_PERMISSION = "You don't have the required permissions.";
    private final String ERROR_CONFIG_SESSION = "Error occurred while configuring capture session.";
    private final String FAIL_CAPTURE = "Capture session failed.";
    private final String ERROR_GET_CHARACTERISTICS = "Could not get camera's characteristics.";

    private boolean stopPreviewOnPicture=true;
    private boolean isPreviewing=false;

    public EZCam(Context context) {
        this.context = context;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public Size[] selectCamera(int id) {
        String[] cameraId;
        try {
            cameraId = cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            throwError(e.getMessage());
            return null;
        }
        selectedCamera = null;

        if (cameraId.length == 1) {
            if (id == BACK)
                selectedCamera = cameraId[0];
        } else if (cameraId.length == 2) {
            if (id == BACK)
                selectedCamera = cameraId[0];
            else
                selectedCamera = cameraId[1];
        }

        if (selectedCamera == null) {
            throwError(CAM_DOES_NOT_EXIST);
            return null;
        }

        CameraCharacteristics cc;
        try {
            cc = cameraManager.getCameraCharacteristics(selectedCamera);
        } catch (CameraAccessException e) {
            throwError(e.getMessage());
            return null;
        }
        StreamConfigurationMap streamConfigs = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (streamConfigs == null) {
            throwError(ERROR_GET_CHARACTERISTICS);
            return null;
        }
        return streamConfigs.getOutputSizes(ImageFormat.JPEG);
    }

    public void startPreview(SurfaceTexture surfaceTexture, final int width, final int height)  {
        surfaceTexture.setDefaultBufferSize(width, height);
        previewSurface = new Surface(surfaceTexture);
        jpegImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
        jpegImageReader.setOnImageAvailableListener(jpegListener, null);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throwError(NO_PERMISSION);
            return;
        }

        try {
            cameraManager.openCamera(selectedCamera, openCallback, null);
        } catch (CameraAccessException e) {
            throwError(e.getMessage());
        }
    }

    public void stopPreview() {
        if(isPreviewing) {
            try {
                cameraCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                throwError(e.getMessage());
                return;
            }
            isPreviewing=false;
        }
        cameraDevice.close();
    }

    public void resumePreview() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throwError(NO_PERMISSION);
            return;
        }
        try {
            cameraManager.openCamera(selectedCamera, openCallback, null);
        } catch (CameraAccessException e) {
            throwError(e.getMessage());
        }
    }

    public void takePicture() {
        request.addTarget(jpegImageReader.getSurface());
        try {
            cameraCaptureSession.capture(request.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            throwError(e.getMessage());
        }
    }

    private CameraDevice.StateCallback openCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            EZCam.this.cameraDevice=cameraDevice;
            try {
                cameraDevice.createCaptureSession(Arrays.asList(previewSurface, jpegImageReader.getSurface()), captureSessionCallback, null);
            } catch (CameraAccessException e) {
                throwError(e.getMessage());
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            throwError(ERROR_OPENING_CAM);
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            throwError(CAM_DISCONNECT);
        }
    };

    private CameraCaptureSession.StateCallback captureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            try {
                EZCam.this.cameraCaptureSession = cameraCaptureSession;
                request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                request.addTarget(previewSurface);
                cameraCaptureSession.setRepeatingRequest(request.build(), captureCallback, null);
                isPreviewing=true;
            } catch (CameraAccessException e) {
                throwError(e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            throwError(ERROR_CONFIG_SESSION);
        }
    };

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            throwError(FAIL_CAPTURE);
        }
    };

    private ImageReader.OnImageAvailableListener jpegListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            request.removeTarget(jpegImageReader.getSurface());
            if(stopPreviewOnPicture) {
                stopPreview();
            }
            if(listener!=null)
                listener.onPicture(imageReader);
        }
    };

    public void setStopPreviewOnPicture(boolean enabled){
        this.stopPreviewOnPicture=enabled;
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

    public void throwError(String message){
        if(listener!=null){
            listener.onError(message);
        }
    }

    public void setEZCamCallback(EZCamCallback listener){
        this.listener=listener;
    }

    public void removeEZCamCallback(){
        this.listener=null;
    }

    public interface EZCamCallback{
        void onPicture(ImageReader reader);
        void onError(String message);
    }
}
