# Android Camera2 Library
Simple library that allows you to display preview and take pictures easily with callbacks!

# Install

Add the following line in your gradle dependencies:

	compile 'me.aflak.libraries:ezcam:2.0'

# See sample

	**https://github.com/omaflak/Android-Camera2-Library/blob/master/app/src/main/java/me/aflak/libraries/MainActivity.java**

# EZCam

    EZCam cam = new EZCam(Context);
    Size[] sizes = cam.selectCamera(EZCam.FRONT); // sizes contains the available preview sizes for the camera selected

# Callback

    cam.setCameraCallback(new EZCamCallback() {
        @Override
        public void onError(String message) {
            // all errors will be passed through this methods
        }

        @Override
        public void onCameraOpened() {
        	// triggered after cam.openCamera()
        }

        @Override
        public void onCameraDisconnected() {
        	// camera disconnected
        }

        @Override
        public void onPreviewReady() {
        	// triggered after cam.preparePreview()
        	cam.startPreview();
        }

        @Override
        public void onPicture(ImageReader imageReader) {
        	cam.saveImage(imageReader, "image.jpg"); // will save image to internal storage
        }
    });
	
# Open Camera & setup Preview

	cam.openCamera(); // needs android.permission.CAMERA
	cam.preparePreview(height, width, CameraDevice.TEMPLATE_PREVIEW, surface);
	
# Take picture | stop preview | close camera 

	cam.takePicture();
	
	cam.stopPreview();

	cam.close();