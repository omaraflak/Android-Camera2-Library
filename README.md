# Android Camera2 Library
Simple library that allows you to display preview and take pictures easily with callbacks!

# Install

Add the following line in your gradle dependencies:

	compile 'me.aflak.libraries:ezcam:2.1'

# See sample

**https://github.com/omaflak/Android-Camera2-Library/blob/master/app/src/main/java/me/aflak/libraries/MainActivity.java**

# EZCam

    EZCam cam = new EZCam(Context);
    cam.selectCamera(cam.getCamerasList().get(EZCam.BACK)); // should first verify if the list contains EZCam.BACK...

# Callback

    cam.setCameraCallback(new EZCamCallback() {
        @Override
        public void onError(String message) {
            	// all errors will be passed through this methods
        }

        @Override
        public void onCameraOpened() {
        	// triggered after cam.open()
		cam.setupPreview(CameraDevice.TEMPLATE_PREVIEW, textureView);
        }

        @Override
        public void onCameraDisconnected() {
        	// camera disconnected
        }

        @Override
        public void onPreviewReady() {
        	// triggered after cam.setupPreview(...)
        	cam.startPreview();
        }

        @Override
        public void onPicture(ImageReader imageReader) {
        	cam.saveImage(imageReader, "image.jpg"); // will save image to internal storage
        }
    });
	
# Open Camera

	cam.open(); // needs android.permission.CAMERA
	
# Take picture | stop preview | close camera 

	cam.takePicture();
	
	cam.stopPreview();

	cam.close();
