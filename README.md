# Android Camera2 Library
Simple library that allows you to display preview and take pictures easily with callbacks!

# Install

Add the following line in your gradle dependencies:

	compile 'me.aflak.libraries:ezcam:1.0'
  
# In your Activity

    EZCam cam = new EZCam(this);
    cam.selectCamera(EZCam.FRONT); // or EZCam.BACK
    cam.setStopPreviewOnPicture(true);
  
# Callback

	cam.setEZCamCallback(new EZCam.EZCamCallback() {
	    @Override
	    public void onPicture(ImageReader reader) {
	        // picture available
	        cam.saveImage(reader, "image.jpeg"); // save to internal storage
	    }
	
	    @Override
	    public void onError(String message) {
	        // error occurred
	    }
	});
	
# Preview

	TextureView textureView = (TextureView)findViewById(R.id.textureView);
	textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
	    @Override
	    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
	        cam.startPreview(surfaceTexture, i, i1);
	    }
	});
	
# Take picture | stop/resume preview

	// take picture
	cam.takePicture();
	
	// stop preview
	cam.stopPreview();
	
	// resume preview
	cam.resumePreview();

