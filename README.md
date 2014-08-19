CanvasCameraPlugin
============================

Cordova canvas camera plugin for iOS/Android, supports camera preview and taking photos.

### Plugin's Purpose
The purpose of the plugin is to capture video to preview camera on web page(canvas tag) and to take photos with user defined quality / dimension.


## Supported Platforms
- **iOS**<br>
- **Android**<br>

## Dependencies
[Cordova][cordova] will check all dependencies and install them if they are missing.


## Installation
The plugin can either be installed into the local development environment or cloud based through [PhoneGap Build][PGB].

### Adding the Plugin to your project
Through the [Command-line Interface][CLI]:
```bash
# ~~ from master ~~
cordova plugin add https://github.com/donaldp24/CanvasCameraPlugin.git && cordova prepare
```
or to use the last stable version:
```bash
# ~~ stable version ~~
cordova plugin add com.keith.cordova.plugin.canvascamera && cordova prepare
```

### Removing the Plugin from your project
Through the [Command-line Interface][CLI]:
```bash
cordova plugin rm com.keith.cordova.plugin.canvascamera
```

### PhoneGap Build
Add the following xml to your config.xml to always use the latest version of this plugin:
```xml
<gap:plugin name="com.keith.cordova.plugin.canvascamera" />
```
or to use an specific version:
```xml
<gap:plugin name="com.keith.cordova.plugin.canvascamera" version="1.0.1" />
```
More informations can be found [here][PGB_plugin].


## ChangeLog

#### Version 1.0.0 (not yet released)
- [feature:] Create plugin


## Using the plugin
The plugin creates the object ```window.plugin.CanvasCamera``` with the following methods:

### Plugin initialization
The plugin and its methods are not available before the *deviceready* event has been fired.
Have to call [initialize][initialize] with canvas object(canvas tag to preview camera).

```javascript
document.addEventListener('deviceready', function () {
    
    // have to call initialize function with canvas object
    var objCanvas = document.getElementById("canvas");
    window.plugin.CanvasCamera.initialize(objCanvas);

    // window.plugin.CanvasCamera is now available
}, false);
```

### start
start capture video as images from camera to preview camera on web page.<br>
[capture][capture] callback function will be called with image data(image file url) at each time when the plugin take an image for a frame.<br>

```javascript
window.CanvasCamera.start(options);
```

This function start video capturing session, then the plugin takes each frame as a jpeg image and gives it's url to web page calling [capture][capture] callback function with the image url. <br>
[capture][capture] callback function will draw the image to play video.


#### Example
```javascript
function onStartClicked()
{
    var options = {
        quality: 75,
        destinationType: CanvasCamera.DestinationType.DATA_URL,
        encodingType: CanvasCamera.EncodingType.JPEG,
        width: 640,
        height: 480
    };
    window.plugin.CanvasCamera.start(options);
}
```

### takePicture
take a photo.<br>

```javascript
window.plugin.takePicture(onSuccess);
```

This function takes a photo.
When taked a photo successfully, then the plugin calls onSuccess callback function with image URI or data URL according to [options][options].
If options.saveToPhotoAlbum is true, then this function saves taked photo to photo album, too.

```javascript

// if options.destinationType == CanvasCamera.DestinationType.IMAGE_URI
function onSuccess(data) {
    image.src = data; // URI
}

// else if options.destinationType == CanvasCamera.DestinationType.DATA_URL
function onSuccess(data) {
    image.src = "data:image/jpeg;base64," + data; // options.encodingType == CanvasCamera.EncodingType.JPEG
    // image.src = "data:image/png;base64," + data; // options.encodingType == CanvasCamera.EncodingType.PNG
}
```

### setFlashMode
Set flash mode for camera.<br>

```javascript
window.plugin.CanvasCamera.setFlashMode(flashMode);
```
##### flashMode
Value of flashMode can be one of the followings;
```javascript
CanvasCamera.FlashMode = 
{
    OFF : 0,
    ON : 1,
    AUTO : 2
};
```
```javascript
window.plugin.CanvasCamera.setFlashMode(CanvasCamera.FlashMode.AUTO);
```

### setCameraPosition
Change input camera to front or back camera.

```javascript
window.plugin.CanvasCamera.setCameraPosition(cameraPosition);
```

#### cameraPosition
Value of cameraPosition can be one of the followings;
```javascript
CanvasCamera.CameraPosition =
{
    BACK : 1,
    FRONT : 2
};
```
```javascript
window.plugin.CanvasCamera.setCameraPosition(CanvasCamera.CameraPosition.FRONT);
```


### capture
 callback function.
 User could override this function to draw images on a canvas tag.


### options
Optional parameters to customize the settings.
```javascript
{ quality : 75, 
  destinationType : CanvasCamera.DestinationType.DATA_URL,
  sourceType : CanvasCamera.PictureSourceType.CAMERA,
  allowEdit : true,
  encodingType: CanvasCamera.EncodingType.JPEG,
  correctOrientation: true,
  saveToPhotoAlbum: false,
  width: 640,
  height: 480
  };
```

- quality: Quality of saved image. Range is [0, 100]. (Number)
- destinationType: Choose the format of the return value. Defined in Camera.DestinationType (Number)
```javascript
    CanvasCamera.DestinationType = {
        DATA_URL : 0,                // Return image as base64 encoded string
        FILE_URI : 1                 // Return image file URI
    };
```
- sourceType: Set the source of the picture. Defined in Camera.PictureSourceType (Number)
```javascript
CanvasCamera.PictureSourceType = {
    PHOTOLIBRARY : 0,
    CAMERA : 1,
    SAVEDPHOTOALBUM : 2
};
```
- allowEdit: Allow simple editing of image before selection. (Boolean)
- encodingType: Choose the encoding of the returned image file. Defined in Camera.EncodingType (Number)
```javascript
    CanvasCamera.EncodingType = {
        JPEG : 0,               // Return JPEG encoded image
        PNG : 1                 // Return PNG encoded image
    };
```
- width: Width in pixels to scale image. Could be used with targetHeight. Aspect ratio is keeped. (Number)
- height: Height in pixels to scale image. Could be used with targetWidth. Aspect ratio is keeped. (Number)
- correctOrientation: Rotate the image to correct for the orientation of the device during capture. (Boolean)
- saveToPhotoAlbum: Save the image to the photo album on the device after capture. (Boolean)


## Full Example
```html
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="format-detection" content="telephone=no" />
        <!-- WARNING: for iOS 7, remove the width=device-width and height=device-height attributes. See https://issues.apache.org/jira/browse/CB-4323 -->
        <meta name="viewport" content="user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width, height=device-height, target-densitydpi=device-dpi" />
        <link rel="stylesheet" type="text/css" href="css/index.css" />
        <meta name="msapplication-tap-highlight" content="no" />
        <title>Hello World</title>
    </head>
    <body>
        <div class="app">
            <h1>Apache Cordova</h1>
            <div id="deviceready" class="blink">
                <p class="event listening">Connecting to Device</p>
                <p class="event received">Device is Ready</p>
            </div>
            
            <h2> Camera Position </h2>
            <input type="radio" name="deviceposition" id="deviceposition_back" value="Back" onclick="onChangeDevicePosition();"/>
            <label for="deviceposition_back">Back</label>
            <br/>
            <input type="radio" name="deviceposition" id="deviceposition_front" value="Front" onclick="onChangeDevicePosition();"/>
            <label for="deviceposition_front">Front</label>
            
            
            <h2> Flash Mode </h2>
            <input type="radio" name="flashmode" id="flashmode_off" value="Off" onclick="onChangeFlashMode();"/>
            <label for="flashmode_off">Off</label>
            <br/>
            <input type="radio" name="flashmode" id="flashmode_on" value="On" onclick="onChangeFlashMode();"/>
            <label for="flashmode_on">On</label>
            <br/>
            <input type="radio" name="flashmode" id="flashmode_auto" value="Auto" onclick="onChangeFlashMode();"/>
            <label for="flashmode_auto">Auto</label>
            <br/>
            
            <input type="button" value="Take a picture" onclick="onTakePicture();" />


        </div>
        
	<!— camera preview canvas —>
        <canvas id="camera" width="352" height="288" style="border:2px"></canvas>
        
        <script type="text/javascript" src="cordova.js"></script>
        <script type="text/javascript" src="js/index.js"></script>
        <script type="text/javascript">
            app.initialize();
        </script>
        
        <script>
            document.addEventListener("deviceready", function() {
                                          canvasMain = document.getElementById("camera");
                                          CanvasCamera.initialize(canvasMain);
                                          // define options
                                          var opt = {
                                              quality: 75,
                                              destinationType: CanvasCamera.DestinationType.DATA_URL,
                                              encodingType: CanvasCamera.EncodingType.JPEG,
                                              saveToPhotoAlbum:true,
                                              correctOrientation:true,
                                              width:640,
                                              height:480
                                          };
                                          CanvasCamera.start(opt);
                                      });
      
            function onChangeDevicePosition() {

                var newDevicePosition = CanvasCamera.CameraPosition.BACK;
                if (document.getElementById("deviceposition_back").checked)
                {
                    newDevicePosition = CanvasCamera.CameraPosition.BACK;
                }
                else if (document.getElementById("deviceposition_front").checked)
                {
                    newDevicePosition = CanvasCamera.CameraPosition.FRONT;
                }
                //
                CanvasCamera.setCameraPosition(newDevicePosition);
            }
            
            function onChangeFlashMode() {
                
                var newFlashMode = CanvasCamera.FlashMode.OFF;
                if (document.getElementById("flashmode_off").checked)
                {
                    newFlashMode = CanvasCamera.FlashMode.OFF;
                }
                else if (document.getElementById("flashmode_on").checked)
                {
                    newFlashMode = CanvasCamera.FlashMode.ON;
                }
                else if (document.getElementById("flashmode_auto").checked)
                {
                    newFlashMode = CanvasCamera.FlashMode.AUTO;
                }
                
                CanvasCamera.setFlashMode(newFlashMode);
            }
            
            function onTakePicture() {
                CanvasCamera.takePicture(onTakeSuccess);
            }
            
            function onTakeSuccess(data) {
                //
            }
        </script>
    </body>
</html>
```

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request


## License

This software is released under the [Apache 2.0 License][apache2_license].

© 2013-2014 Snaphappi, Inc. All rights reserved

[ctassetspickercontroller]: https://github.com/chiunam/CTAssetsPickerController
[cordova-plugin-local-notifications]: https://github.com/katzer/cordova-plugin-local-notifications
[cordova]: https://cordova.apache.org
[PGB_plugin]: https://build.phonegap.com/plugins/413
[onsuccess]: #onSuccess
[oncancel]: #onCancel
[options]: #options
[getById]: #getById
[ongetbyid]: #onGetById
[CLI]: http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface
[PGB]: http://docs.build.phonegap.com/en_US/3.3.0/index.html
[apache2_license]: http://opensource.org/licenses/Apache-2.0
