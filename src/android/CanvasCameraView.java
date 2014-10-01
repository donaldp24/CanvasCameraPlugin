package com.keith.canvascameraplugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CanvasCameraView extends Activity implements SurfaceHolder.Callback
{
    private SurfaceHolder m_surfaceHolder;
    private Camera m_camera;
    private int m_previewCameraRotationDegree = 0;
    private int m_saveCameraRotationDegree = 0;


    boolean bUsecamera = true;
    boolean bPreviewRunning = false;
    boolean bFlash = false; // true: Flash ON, false: Flash Off
    boolean bRevert = true; // true: back camera, false: front camera

    private SurfaceView m_surfaceview;

    private ImageView m_imgFlash;
    private ImageView m_imgRevert;
    private ImageView m_imgCapture;
    private ImageView m_imgClose;

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

    // options
    private int _quality = 85;
    private int _destType = DestinationTypeFileURI;
    private boolean _allowEdit = false;
    private int _encodeType = EncodingTypeJPEG;
    private boolean _saveToPhotoAlbum = false;
    private boolean _correctOrientation = false;

    private int _width = 640;
    private int _height = 480;


    Handler customHandler = new Handler();

    ProgressDialog m_prgDialog;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canvascamera);

        _quality = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(CanvasCamera.QUALITY , 85);
        _destType = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(CanvasCamera.DESTTYPE , DestinationTypeFileURI);
        _allowEdit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(CanvasCamera.ALLOWEDIT, false);
        _encodeType = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(CanvasCamera.ENCODETYPE, EncodingTypeJPEG);
        _saveToPhotoAlbum = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(CanvasCamera.SAVETOPHOTOALBUM, false);
        _correctOrientation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(CanvasCamera.CORRECTORIENTATION, false);
        _width = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(CanvasCamera.WIDTH , 640);
        _height = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(CanvasCamera.HEIGHT , 480);

        bFlash = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(CanvasCamera.FLASH, false);
        bRevert = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(CanvasCamera.REVERT, true);
        
        getControlVariables();
        initializeUI();

        setCameraRotationDegree();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initializeUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();


    }

    @SuppressWarnings("deprecation")
	private void getControlVariables()
    {
        m_imgFlash = (ImageView) findViewById(R.id.imgFlash);

        m_imgRevert = (ImageView) findViewById(R.id.imgRevert);
        m_imgCapture = (ImageView) findViewById(R.id.imgCapture);
        m_imgClose = (ImageView) findViewById(R.id.imgClose);

        m_surfaceview = (SurfaceView) findViewById(R.id.surfaceView);
        m_surfaceHolder = m_surfaceview.getHolder();
        m_surfaceHolder.addCallback(this);
        m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        m_prgDialog = new ProgressDialog(this);
    }

    private void initializeUI()
    {
        if (bFlash)
            m_imgFlash.setImageResource(R.drawable.video_sprites_focus_inactive);
        else
            m_imgFlash.setImageResource(R.drawable.video_sprites_focus);

        if (bRevert)
            m_imgRevert.setImageResource(R.drawable.video_sprites_revert);
        else
            m_imgRevert.setImageResource(R.drawable.video_sprites_revert_inactive);

        m_imgFlash.setOnClickListener(flashClickListener);

        m_imgRevert.setOnClickListener(revertClickListener);
        m_imgCapture.setOnClickListener(captureClickListener);
        m_imgClose.setOnClickListener(closeClickListener);
    }

    private View.OnClickListener flashClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (m_camera == null || !bRevert)
                return;

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CanvasCameraView.this).edit();

            Parameters p = m_camera.getParameters();

            if (bFlash)
            {
                p.setFlashMode(Parameters.FLASH_MODE_OFF);
                m_imgFlash.setImageResource(R.drawable.video_sprites_focus);

                editor.putBoolean(CanvasCamera.FLASH, false);
            }
            else
            {
                p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                m_imgFlash.setImageResource(R.drawable.video_sprites_focus_inactive);

                editor.putBoolean(CanvasCamera.FLASH, true);
            }

            m_camera.setParameters(p);

            editor.commit();

            bFlash = !bFlash;
        }
    };

    private View.OnClickListener revertClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (m_camera != null)
            {
                m_camera.stopPreview();
                m_camera.release();
                m_camera = null;
            }

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CanvasCameraView.this).edit();

            if (bRevert)
            {
                m_camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

                m_imgRevert.setImageResource(R.drawable.video_sprites_revert_inactive);
                editor.putBoolean(CanvasCamera.REVERT, false);

                m_imgFlash.setImageResource(R.drawable.video_sprites_focus);
                bFlash = false;
                editor.putBoolean(CanvasCamera.FLASH, false);
            }
            else
            {
                m_camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

                m_imgRevert.setImageResource(R.drawable.video_sprites_revert);

                editor.putBoolean(CanvasCamera.REVERT, true);
            }

            bRevert = !bRevert;

            editor.commit();

            try
            {
                Camera.Parameters parameters = m_camera.getParameters();

                setCameraRotationDegree();

                m_camera.setDisplayOrientation(m_previewCameraRotationDegree);
                m_camera.setParameters(parameters);
                m_camera.setPreviewDisplay(m_surfaceHolder);
                m_camera.startPreview();
                bPreviewRunning = true;


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
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

    private View.OnClickListener captureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            m_prgDialog.setMessage("Please wait for saving...");
            m_prgDialog.setCancelable(false);
            m_prgDialog.show();

            m_camera.takePicture(null, null, new PictureCallback() {
                public void onPictureTaken(byte[] data, Camera camera) {

                    Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length);

                    //_correctOrientation
                    if (_correctOrientation)
                        original = rotate(original, m_saveCameraRotationDegree);

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
                        MediaStore.Images.Media.insertImage(getContentResolver(), original, "CanvasCamera", "Taked by CanvasCamera");
                    }

                    JSONObject returnInfo = new JSONObject();
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

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                        String date = dateFormat.format(new Date());
                        returnInfo.put("lastModifiedDate", date);
                        
                        returnInfo.put("size", String.valueOf(blob.size()));
                        returnInfo.put("type", (_encodeType == EncodingTypeJPEG ? "image/jpeg" : "image/png"));
                    }
                    catch (JSONException ex)
                    {
                        return;
                    }

                    CanvasCamera.sharedCanvasCamera.onTakePicture(returnInfo);
                    
                    m_prgDialog.dismiss();
                    
                    m_camera.startPreview();
                }
            });
        }
    };

    private View.OnClickListener closeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            finish();
        }
    };

    private void setCameraRotationDegree()
    {
        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if (bRevert)    // Back Camera
        {
            if(display.getRotation() == Surface.ROTATION_0)
            {
                m_previewCameraRotationDegree = 90;
            }
            else if(display.getRotation() == Surface.ROTATION_90)
            {
                m_previewCameraRotationDegree = 0;
            }
            else if(display.getRotation() == Surface.ROTATION_180)
            {
                m_previewCameraRotationDegree = 90;
            }
            else if(display.getRotation() == Surface.ROTATION_270)
            {
                m_previewCameraRotationDegree = 180;
            }

            m_saveCameraRotationDegree = m_previewCameraRotationDegree;
        }
        else    // Front Camera
        {
            if(display.getRotation() == Surface.ROTATION_0)
            {
                m_previewCameraRotationDegree = 90;
                m_saveCameraRotationDegree = 270;
            }
            else if(display.getRotation() == Surface.ROTATION_90)
            {
                m_previewCameraRotationDegree = 0;
                m_saveCameraRotationDegree = 0;
            }
            else if(display.getRotation() == Surface.ROTATION_180)
            {
                m_previewCameraRotationDegree = 90;
                m_saveCameraRotationDegree = 90;
            }
            else if(display.getRotation() == Surface.ROTATION_270)
            {
                m_previewCameraRotationDegree = 180;
                m_saveCameraRotationDegree = 180;
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        System.out.println("onsurfacecreated");

        if (bUsecamera) {

            if (bRevert)
            {
                m_camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                m_imgRevert.setImageResource(R.drawable.video_sprites_revert);
            }
            else
            {
                m_camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                m_imgRevert.setImageResource(R.drawable.video_sprites_revert_inactive);
            }

            try
            {
                m_camera.setPreviewDisplay(m_surfaceHolder);
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            bPreviewRunning = true;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        System.out.println("onsurface changed");

        if (bUsecamera)
        {
            if (bPreviewRunning)
            {
                Camera.Parameters parameters = m_camera.getParameters();

                if (bFlash)
                    parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                else
                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF);

                setCameraRotationDegree();
                m_camera.setDisplayOrientation(m_previewCameraRotationDegree);

                m_camera.setParameters(parameters);

                m_camera.startPreview();
            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

        if (m_camera != null && bUsecamera)
        {
            m_camera.release();
            m_camera = null;

            bPreviewRunning = false;
        }
    }
}
