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
