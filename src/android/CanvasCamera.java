package com.keith.CanvasCameraPlugin;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by donald on 8/20/14.
 */
@SuppressLint("NewApi")
public class CanvasCamera extends CordovaPlugin implements Camera.PreviewCallback, Camera.PictureCallback, Runnable {
    private CallbackContext callback;

    // DestinationType
    public static final int DestinationTypeDataURL = 0;
    public static final int DestinationTypeFileURI = 1;

    // EncodingType
    public static final int EncodingTypeJPEG = 0;
    public static final int EncodingTypePNG = 1;

    // CameraPosition
    public static final int CameraPositionBack = 1;
    public static final int CameraPositionFront = 2;

    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    // parameter
    public static final String kQualityKey          = "quality";
    public static final String kDestinationTypeKey = "destinationType";
    public static final String kEncodingTypeKey     = "encodingType";

    public static final String kSaveToPhotoAlbumKey     = "saveToPhotoAlbum";
    public static final String kCorrectOrientationKey = "correctOrientation";

    public static final String kWidthKey        = "width";
    public static final String kHeightKey       = "height";

    //dispatch_queue_t queue;
    private boolean bIsStarted = false;
    private Camera camera = null;


    // parameters
    private String  _flashMode = Camera.Parameters.FLASH_MODE_OFF;
    private int    _devicePosition = CameraPositionBack;

    private int currentFlashMode = 0; // 0: Off, 1: On, 2: Auto
    private int	currentCameraPosition = CameraPositionBack;
    
    // options
    private int _quality = 85;
    private int _destType = DestinationTypeDataURL;
    private boolean _allowEdit = false;
    private int _encodeType = EncodingTypeJPEG;
    private boolean _saveToPhotoAlbum = false;
    private boolean _correctOrientation = true;

    private int _width = 640;
    private int _height = 480;

    private static int i = 0;

    private boolean bPhotoTake = false;

    public static final int DONE=1;  
    public static final int NEXT=2;  
    public static final int PERIOD=1;   

    private byte[] mBuffer;
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // action == "contacts" : retrieving information
    	
    	this.callback = callbackContext;
    	
        if ("startCapture".equals(action))
        {
            if (args.length() > 0)
                startCapture(args);
            
            return true;
        }
        else if ("stopCapture".equals(action))
        {
        	stopCapture();
        	
        	return true;
        }
        else if ("setCameraPosition".equals(action))
        {
        	setCameraPosition(args);
        	
        	return true;
        }
        else if ("setFlashMode".equals(action))
        {
        	setFlashMode(args);
        	
        	return true;
        }
        else if ("captureImage".equals(action))
        {
    		captureImage();
        	
        	return true;
        }
        return false;
    }

	@SuppressLint("NewApi")
	private void startCapture(JSONArray args)
    {
        // check already started
        if (this.camera != null && bIsStarted == true)
        {
            // failure callback
            callback.error("Already started");
        }

        // init parameters - default values
        _quality = 85;
        _destType = DestinationTypeFileURI;
        _encodeType = EncodingTypeJPEG;
        _width = 640;
        _height = 480;
        _saveToPhotoAlbum = false;
        _correctOrientation = true;

        bPhotoTake = false;

        // parse options
        if (args.length() > 0)
        {
            try {
                JSONObject jsonData = args.getJSONObject(0);
                getOptions(jsonData);
            } catch (Exception e) {
                Log.d("CanvasCamera", "Parsing options error : " + e.getMessage());
            }
        }

        // add support for options (fps, capture quality, capture format, etc.)
        int cameraId = getCameraPosition(_devicePosition);
        if (cameraId == -1)
        {
            callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, 
            		"There is no camera for camera position :" + _devicePosition));
            return;
        }
        
        camera = Camera.open(cameraId);
        if (camera != null)
        {
        	startPreview();
        	
        	//camera.takePicture(null, null, this);
        	
            // success callback
            callback.success();
        }
        else
        {
            callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Cannot start???"));
        }
    }

	private void startPreview()
	{
    	/*SurfaceView view = new SurfaceView(cordova.getActivity());  
        try
        {  
        	camera.setPreviewDisplay(view.getHolder());  
        } catch (IOException e)
        {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }*/
        
        
    	try
    	{  
    		SurfaceTexture surfaceTexture = new SurfaceTexture(10);
			camera.setPreviewTexture(surfaceTexture);
    	}
    	catch (IOException e)
    	{  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
    	}
    	
        /*Parameters params = camera.getParameters();
        int size = params.getPreviewSize().width * params.getPreviewSize().height;
        size = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
        mBuffer = new byte[size];
        // The buffer where the current frame will be copied
        //byte[] mFrame = new byte [size];
        camera.addCallbackBuffer(mBuffer);*/
    	
        //camera.setOneShotPreviewCallback(this);
        //camera.setPreviewCallbackWithBuffer(this);
		camera.setPreviewCallback(this);
    	
    	
    	camera.startPreview();
    	
    	bIsStarted = true;	
    	
	}
	

    private void stopCapture()
	{
		if (camera != null && bIsStarted == true)
		{
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		
		bIsStarted = false;
	}

    private void setCameraPosition(JSONArray args)
    {
    	try
    	{
    		currentCameraPosition = args.getInt(0);
		}
    	catch (JSONException e)
    	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if (camera != null)
    	{
    		camera.stopPreview();
    		camera.release();
    		camera = null;
    	}
    	
		camera = Camera.open(getCameraPosition(currentCameraPosition)); 

    	if (camera != null)
    		startPreview();

		callback.success();
    }

    private void setFlashMode(JSONArray args)
    {
    	if (camera == null)
    		return;
    	
    	try {
    		currentFlashMode = args.getInt(0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	Parameters p = camera.getParameters();
    	if (currentFlashMode == 0)
    	{
    		p.setFlashMode(Parameters.FLASH_MODE_OFF);
    	}
    	else if (currentFlashMode == 1)
    	{
    		p.setFlashMode(Parameters.FLASH_MODE_TORCH);    		
    	}
    	else if (currentFlashMode == 2)
    	{
    		p.setFlashMode(Parameters.FLASH_MODE_AUTO);
    	}
    	
    	camera.setParameters(p);
    	
		callback.success();    	
    }

    private void captureImage()
    {
        // check already started
        if (camera != null && bIsStarted == true)
        {
            // failure callback
            camera.stopPreview();
            camera.release();
        }

        camera = Camera.open(getCameraPosition(currentCameraPosition));
        if (camera != null)
        {
            try
            {
                SurfaceTexture surfaceTexture = new SurfaceTexture(10);
				camera.setPreviewTexture(surfaceTexture);

			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        	camera.startPreview();
        	
        	bPhotoTake = true;
        	
        	camera.takePicture(null, null, this);
            
        	bIsStarted = true;
        }
        else
        {
            callback.error("cannot start ");
        }        
    }
    
    /**
     * parse options parameter and set it to local variables
     *
     */
    private void getOptions(JSONObject jsonData) throws Exception
    {
        if (jsonData == null)
            return;

        // get parameters from argument.

        // quaility
        String obj = jsonData.getString(kQualityKey);
        if (obj != null)
            _quality = Integer.parseInt(obj);

        // destination type
        obj = jsonData.getString(kDestinationTypeKey);
        if (obj != null)
        {
            int destinationType = Integer.parseInt(obj);
            _destType = destinationType;
        }

        // encoding type
        obj = jsonData.getString(kEncodingTypeKey);
        if (obj != null)
        {
            int encodingType = Integer.parseInt(obj);
            _encodeType = encodingType;
        }

        // width
        obj = jsonData.getString(kWidthKey);
        if (obj != null)
        {
            _width = Integer.parseInt(obj);
        }

        // height
        obj = jsonData.getString(kHeightKey);
        if (obj != null)
        {
            _height = Integer.parseInt(obj);
        }

        // saveToPhotoAlbum
        obj = jsonData.getString(kSaveToPhotoAlbumKey);
        if (obj != null)
        {
            _saveToPhotoAlbum = Boolean.parseBoolean(obj);
        }

        // correctOrientation
        obj = jsonData.getString(kCorrectOrientationKey);
        if (obj != null)
        {
            _correctOrientation = Boolean.parseBoolean(obj);
        }
    }

    private int getCameraPosition(int position)
    {
        int cameraId = -1;
        // search for the back facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (position == CameraPositionBack)
            {
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    cameraId = i;
                    break;
                }
            }
            else
            {
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                {
                    cameraId = i;
                    break;
                }
            }
        }
        Log.d("CanvasCamera", "Camera ID : " + cameraId);
        return cameraId;
    }

    private String writeImageDataToFile(byte[] data) {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File pictureFileDir = new File(sdDir, "CameraAPIDemo");
        
        if (!pictureFileDir.exists())
        	pictureFileDir.mkdir();
        
        String photoFile = "picture" + i + ".jpg";
        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        File pictureFile = new File(filename);
        try
        {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            Log.d("CanvasCamera", "File " + filename + " not saved: " + e.getMessage());
        }
        
        i++;
        
        if (i > 10)
        {
            // delete previous image file
            String prevPhotoFile = "picture" + (i - 10) + ".jpg";
            String prevfilename = pictureFileDir.getPath() + File.separator + prevPhotoFile;
            File prevFile = new File(prevfilename);
            prevFile.delete();
        }
        return filename;
    }

    @SuppressLint("SimpleDateFormat")
	private String writeTakedImageDataToStorage(byte[] data) {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File pictureFileDir = new File(sdDir, "CameraAPIDemo");

        if (!pictureFileDir.exists())
        	pictureFileDir.mkdir();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "picture" + date + ".jpg";
        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        File pictureFile = new File(filename);
        try
        {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            Log.d("CanvasCamera", "File " + filename + " not saved: " + e.getMessage());
        }
        return filename;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
    
    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
    	Log.d("onPreviewFrame : ", "Test");
    	
    	byte[] resized352x288 = {0};
    	
    	Parameters p = camera.getParameters();    	
    	int format = p.getPreviewFormat();
    	
    	if (format == ImageFormat.NV21)
    	{    	
	        int width = p.getPreviewSize().width;
	        int height = p.getPreviewSize().height;
	
	        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
	        Rect rect = new Rect(0, 0, width, height);
	        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
	        yuvimage.compressToJpeg(rect, 80, outstr); // outstr contains image in jpeg

	        Bitmap original = BitmapFactory.decodeByteArray(outstr.toByteArray(), 0, outstr.size());
	        
	        if (currentCameraPosition == CameraPositionFront)
	        	original= rotate(original, -180);

	        Bitmap resized = Bitmap.createScaledBitmap(original, 352, 288, true);
	        
	        
	        ByteArrayOutputStream blob = new ByteArrayOutputStream();
	        resized.compress(Bitmap.CompressFormat.JPEG, _quality, blob);
	        resized352x288 = blob.toByteArray();
    	}
    	else if (format == ImageFormat.JPEG || format == ImageFormat.RGB_565)
    	{        
	        Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length);
	        Bitmap resized = Bitmap.createScaledBitmap(original, 352, 288, true);
	        ByteArrayOutputStream blob = new ByteArrayOutputStream();
	        resized.compress(Bitmap.CompressFormat.JPEG, _quality, blob);
	        resized352x288 = blob.toByteArray();
    	}
    	else
    		return;
    	
        String strPath = writeImageDataToFile(resized352x288);
        
        //System.gc();
        
        Log.d("onPreviewFrame : ", strPath);
        
        String javascript = "CanvasCamera.capture('" + strPath + "');";
        webView.sendJavascript(javascript);
        
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera)
    {
        /*_quality = 85;
        _destType = DestinationTypeFileURI;
        _encodeType = EncodingTypeJPEG;
        _width = 640;
        _height = 480;
        _saveToPhotoAlbum = true;
        _correctOrientation = true;*/
        
        if (bPhotoTake)
        {
            bPhotoTake = false;

            Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length);
            
            // Portrait Orientation(Front/Back Camera)
	        if (currentCameraPosition == CameraPositionBack)
	        	original= rotate(original, 90);
	        else
	        	original= rotate(original, 270);
            
            // resize to width x height
            Bitmap resized = Bitmap.createScaledBitmap(original, _width, _height, true);
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            if (_encodeType == EncodingTypeJPEG)
                resized.compress(Bitmap.CompressFormat.JPEG, _quality, blob);
            else
                resized.compress(Bitmap.CompressFormat.PNG, 0, blob);

            // save image to album
            if (_saveToPhotoAlbum)
            {
                MediaStore.Images.Media.insertImage(cordova.getActivity().getContentResolver(), original, "CanvasCamera", "Taked by CanvasCamera");
            }

            final JSONObject returnInfo = new JSONObject();				
			try
			{
	            if (_destType == DestinationTypeFileURI)
	            {
	                String strPath = writeTakedImageDataToStorage(blob.toByteArray());

	                returnInfo.put("imageURI", strPath);
	            }
	            else
	            {
	                byte[] retData = blob.toByteArray();
	                // base64 encoded string
	                String base64String = Base64.encodeToString(retData, Base64.NO_WRAP);

	                returnInfo.put("imageURI", base64String);
	            }
	            
	            returnInfo.put("size", String.valueOf(blob.size()));
			}
			catch (JSONException ex)
			{
				callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ex.toString()));
				return;
			}

    		cordova.getThreadPool().execute(new Runnable() {
	            public void run() {
	            	
	        		PluginResult result = new PluginResult(PluginResult.Status.OK, returnInfo);
	        		result.setKeepCallback(true);
	        		callback.sendPluginResult(result);
	            }
			});
        }
    }
}
