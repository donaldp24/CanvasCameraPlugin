//
//  CanvasCamera.js
//  PhoneGap iOS Cordova Plugin to capture Camera streaming into a HTML5 Canvas or an IMG tag.
//
//  Created by Diego Araos <d@wehack.it> on 12/29/12.
//
//  MIT License

cordova.define("cordova/plugin/CanvasCamera", function(require, exports, module)
{
   var exec = require('cordova/exec');
   var CanvasCamera = function(){};
   
   CanvasCamera._orientation = 'landscape';
   CanvasCamera._obj = null;
   CanvasCamera._context = null;
   CanvasCamera._camImage = null;

   CanvasCamera._x = 0;
   CanvasCamera._y = 0;
   CanvasCamera._width = 0;
   CanvasCamera._height = 0;
    
  CanvasCamera.prototype.initialize = function(obj) {
      this._obj = obj;
       this._context = obj.getContext("2d");
       this._camImage = new Image();
       this._camImage.onload = function() {
               context.clearRect(0, 0, CanvasCamera._width, CanvasCamera._height);
               if (window.orientation == 90
                   || window.orientation == -90)
               {
                   CanvasCamera._context.save();
                   // rotate 90
                   CanvasCamera._context.translate(CanvasCamera._width/2, CanvasCamera._height/2);
                   CanvasCamera._context.rotate((90 - window.orientation) *Math.PI/180);
                   CanvasCamera._context.drawImage(camImage, 0, 0, 352, 288, -CanvasCamera._width/2, -CanvasCamera._height/2, CanvasCamera._width, CanvasCamera._height);
                   //
                   CanvasCamera._context.restore();
               }
               else
               {
                   CanvasCamera._context.save();
                   // rotate 90
                   CanvasCamera._context.translate(CanvasCamera._width/2, CanvasCamera._height/2);
                   CanvasCamera._context.rotate((90 - window.orientation) *Math.PI/180);
                   CanvasCamera._context.drawImage(camImage, 0, 0, 352, 288, -CanvasCamera._height/2, -CanvasCamera._width/2, CanvasCamera._height, CanvasCamera._width);
                   //
                   CanvasCamera._context.restore();
               }
        };
      
      // register orientation change event
      window.addEventListener('orientationchange', this.doOrientationChange);
      this.doOrientationChange();
      
  };
               
               
  CanvasCamera.prototype.start = function(options) {
    cordova.exec(false, false, "CanvasCamera", "startCapture", [options]);
  };
               
               
               
  CanvasCamera.prototype.capture = function(data) {
               camImage.src = data;
    };
               
    CanvasCamera.prototype.setFlashMode = function(flashMode) {
        cordova.exec(function(){}, function(){}, "CanvasCamera", "setFlashMode", [flashMode]);
               };
               
    CanvasCamera.prototype.setCameraPosition = function(cameraPosition) {
        cordova.exec(function(){}, function(){}, "CanvasCamera", "setCameraPosition", [cameraPosition]);
    };
               
    CanvasCamera.prototype.doOrientationChange = function() {
        switch(window.orientation)
        {
            case -90:
            case 90:
                CanvasCamera._orientation = 'landscape';
                break;
            default:
                CanvasCamera._orientation = 'portrait';
                break;
        }
        
        var windowWidth = window.innerWidth;
        var windowHeight = window.innerHeight;
        var pixelRatio = window.devicePixelRatio || 1; /// get pixel ratio of device
        
        CanvasCamera._obj.width = windowWidth;// * pixelRatio;   /// resolution of canvas
        CanvasCamera._obj.height = windowHeight;// * pixelRatio;
        
        CanvasCamera._obj.style.width = windowWidth + 'px';   /// CSS size of canvas
        CanvasCamera._obj.style.height = windowHeight + 'px';
        
        CanvasCamera._x = 0;
        CanvasCamera._y = 0;
        CanvasCamera._width = windowWidth;
        CanvasCamera._height = windowHeight;
        
    };
               
    CanvasCamera.prototype.takePicture = function(onsuccess) {
        cordova.exec(onsuccess, function(){}, "CanvasCamera", "captureImage", []);
    };
};
               
var CanvasCamera = cordova.require("cordova/plugin/CanvasCamera");

var DestinationType = {
    DATA_URL : 0,
    FILE_URI : 1
};

var PictureSourceType = {
    PHOTOLIBRARY : 0,
    CAMERA : 1,
    SAVEDPHOTOALBUM : 2
};
               
var EncodingType = {
    JPEG : 0,
    PNG : 1
};

var CameraPosition = {
    BACK : 0,
    FRONT : 1
};

var CameraPosition = {
    BACK : 1,
    FRONT : 2
};

var FlashMode = {
    OFF : 0,
    ON : 1,
    AUTO : 2
};

CanvasCamera.DestinationType = DestinationType;
CanvasCamera.PictureSourceType = PictureSourceType;
CanvasCamera.EncodingType = EncodingType;
CanvasCamera.CameraPosition = CameraPosition;
CanvasCamera.FlashMode = FlashMode;

module.exports = CanvasCamera;
