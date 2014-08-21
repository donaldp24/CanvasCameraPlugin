package com.keith.CanvasCameraPlugin;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

/**
 * Created by donald on 8/20/14.
 */
public class CanvasCamera extends CordovaPlugin implements Camera.PictureCallback {
    private CallbackContext callback;

    // DestinationType
    public static final int DestinationTypeDataURL = 0;
    public static final int DestinationTypeFileURI = 1;

    // EncodingType
    public static final int EncodingTypeJPEG = 0;
    public static final int EncodingTypePNG = 1;

    // CameraPosition
    public static final int CameraPositionBack = 0;
    public static final int CameraPositionFront = 1;

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

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // action == "contacts" : retrieving information
        if ("start".equals(action)) {

            boolean bParsed = false;
            if (args.length() <= 0)
            {
                startCapture(args);
            }
            JSONObject options = args.getJSONObject(0);
            // show contact list activity
            this.callback = callbackContext;
            callback.success();
            return true;
        }
        return false;
    }

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
            callback.error("there is no camera for camera position :" + _devicePosition);
            return;
        }

        camera = Camera.open(cameraId);
        if (camera != null)
        {
            camera.takePicture(null, null, this);
            bIsStarted = true;

            // success callback
            callback.success();
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

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        // resize to 352 x 288
        Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap resized = Bitmap.createScaledBitmap(original, 352, 288, true);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, _quality, blob);
        byte[] resized352x288 = blob.toByteArray();

        String strPath = writeImageDataToFile(resized352x288);
        String javascript = "CanvasCamera.capture('" + strPath + "');";
        this.webView.sendJavascript(javascript);

        /*
        if (bPhotoTake)
        {
            bPhotoTake = false;

            // resize to width x height
            resized = Bitmap.createScaledBitmap(original, _width, _height, true);
            blob = new ByteArrayOutputStream();
            if (_encodeType == EncodingTypeJPEG)
                resized.compress(Bitmap.CompressFormat.JPEG, _quality, blob);
            else
                resized.compress(Bitmap.CompressFormat.PNG, 0, blob);

            // save image to album
            if (_saveToPhotoAlbum)
            {
                MediaStore.Images.Media.insertImage(cordova.getActivity().getContentResolver(), original, "CanvasCamera", "Taked by CanvasCamera");
            }

            if (_destType == DestinationTypeFileURI)
            {
                String strPath = writeTakedImageDataToStorage(blob.toByteArray());

                // call javascript function
                String javascript = String.format("%s%s%s", "CanvasCamera.capture('", strPath, "');");
            }
            else
            {
                byte[] retData = blob.toByteArray();
                // base64 encoded string
                String base64String = Base64.encodeToString(retData, Base64.NO_WRAP);

                // call javascript function
            }
        }
        */
    }

    private String writeImageDataToFile(byte[] data) {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File pictureFileDir = new File(sdDir, "CameraAPIDemo");
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

    private String writeTakedImageDataToStorage(byte[] data) {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File pictureFileDir = new File(sdDir, "CameraAPIDemo");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
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
        return filename;
    }

}
