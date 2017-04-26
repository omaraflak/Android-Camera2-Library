# Android Camera2 Library
Library that allows you to display preview and take pictures easily with camera2 !

# Dependencie

Add the following line in your gradle dependencies :

	compile 'me.aflak.libraries:ezcam:2.1'

Or if you use Maven :

	<dependency>
	  <groupId>me.aflak.libraries</groupId>
	  <artifactId>ezcam</artifactId>
	  <version>2.1</version>
	  <type>pom</type>
	</dependency>

# What's new in 2.1 ?

- full support of portrait mode (landscape coming)
- don't need to pass an available textureview, the lib will wait for it
- get cameras list
- some bugs fixed...

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

# EZCam

    EZCam cam = new EZCam(Context);
    cam.selectCamera(CameraCharacteristics.LENS_FACING_BACK);
    // you can get the available cameras with	:	getCamerasList()

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
