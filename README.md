# EZCam [![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.aflak.libraries/ezcam/badge.svg)](https://maven-badges.herokuapp.com/maven-central/me.aflak.libraries/ezcam)

EZCam is an Android library that simplifies the use of the Camera 2 API.

# Dependencie

Add the following line in your gradle dependencies :

	compile 'me.aflak.libraries:ezcam:X.X'

Or if you use Maven :

	<dependency>
	  <groupId>me.aflak.libraries</groupId>
	  <artifactId>ezcam</artifactId>
	  <version>X.X</version>
	  <type>pom</type>
	</dependency>

# See sample

**https://github.com/omaflak/Android-Camera2-Library/blob/master/app/src/main/java/me/aflak/libraries/MainActivity.java**

# TextureView

**Important : The TextureView must be in a FrameLayout.**

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <TextureView
            android:id="@+id/textureView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"/>
    </FrameLayout>

# Initialize

    EZCam cam = new EZCam(Context);
    cam.selectCamera(CameraCharacteristics.LENS_FACING_BACK);
    // you can get the available cameras with	:	getCamerasList()

# Callback

    cam.setCameraCallback(new EZCamCallback() {
    	@Override
        public void onCameraReady() {
        	// triggered after cam.open(...)
        	// you can set capture settings for example:
        	cam.setCaptureSetting(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE);
        	// then start the preview
        	cam.startPreview();
        }

        @Override
        public void onPicture(ImageReader imageReader) {
        	cam.saveImage(imageReader, "image.jpg"); // will save image to internal storage
        }

        @Override
        public void onError(String message) {
            // all errors will be passed through this methods
        }

        @Override
        public void onCameraDisconnected() {
        	// camera disconnected
        }
    });
	
# Open Camera

	cam.open(CameraDevice.TEMPLATE_PREVIEW, textureView); // needs android.permission.CAMERA
	
# Take picture | stop preview | close camera 

	cam.takePicture();
	
	cam.stopPreview();

	cam.close();

# TODO

- Support for landscape mode
- Apply custom filters
- Recording videos
