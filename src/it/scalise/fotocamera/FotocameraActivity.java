package it.scalise.fotocamera;


import it.scalise.util.UrlHelper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FotocameraActivity extends Activity implements SurfaceHolder.Callback,OnClickListener {
	
	private static FotocameraActivity fotocameraActivity = null;
	private SurfaceView sf = null;
	private SurfaceHolder mSurfaceHolder;
	private Camera cam = null;
	private boolean mPreviewRunning;
	private static final String TAG = "CAMERADEMO";
	private static final String  URL_ACTION = "http://gtug-torino-2012.appspot.com/uploadUrlFactory";
	private byte[] imageData;
	private EditText caption = null;
	private Button button = null;
	ProgressDialog dialog1 = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        fotocameraActivity = this;
        
        caption = (EditText)findViewById(R.id.caption);
        button = (Button)findViewById(R.id.button1);
        sf = (SurfaceView) findViewById(R.id.surfaceView1);
        sf.setOnClickListener(this);
        sf.setClickable(false);
        sf.setClickable(true);
        mSurfaceHolder = sf.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(imageData!=null && caption.getText().length()>0){
					dialog1 = ProgressDialog.show(fotocameraActivity, "", "Uploading foto. Please wait...", true);
					//cam.takePicture(null, mRawCallback, mPictureCallback);
					String urlToSendPicture = UrlHelper.getContents(URL_ACTION);
					String descrizione = caption.getText().toString();
					urlToSendPicture = urlToSendPicture.substring(0, urlToSendPicture.lastIndexOf("/"));
					Log.d(TAG, "FOTOCAMERA - urlToSendPicture: " + urlToSendPicture);
					try {
						sendImage(imageData, urlToSendPicture,descrizione);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
					Log.d(TAG, "FOTOCAMERA - onPictureTaken - wrote bytes: " + imageData.length);
					
				}else{
					AlertDialog.Builder builder = new AlertDialog.Builder(fotocameraActivity);
			    	builder.setMessage("Scattare la foto ed inserire la descrizione")
			    	.setCancelable(false)
			    	.setPositiveButton("Ok",new DialogInterface.OnClickListener(){
			    		public void onClick(DialogInterface dialog, int id){
			    			dialog.cancel();
			    		}
			    	});
			    	AlertDialog alert = builder.create();
			    	alert.show();					
				}
			}
		});
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        CameraInfo info = new CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
    
    @Override
	public void onClick(View v) {
		Toast.makeText(getApplicationContext(), "Foto acquisita", Toast.LENGTH_SHORT).show();
		cam.takePicture(null, mRawCallback, mPictureCallback);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
		if (mPreviewRunning) {
			cam.stopPreview();
		}
		Camera.Parameters p = cam.getParameters();
		p.setWhiteBalance(p.WHITE_BALANCE_AUTO);
        p.setFocusMode(p.FOCUS_MODE_AUTO);
        p.setSceneMode(p.SCENE_MODE_ACTION);
        p.setZoom(p.getMaxZoom());
		cam.setParameters(p);
		try {
			cam.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		cam.startPreview();
		mPreviewRunning = true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		cam = Camera.open();
		setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, cam);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		cam.stopPreview();
		mPreviewRunning = false;
		cam.release();		
	}
	
	Camera.PictureCallback mRawCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};
	
	Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};
	
	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				Log.d(TAG, "FOTOCAMERA - wrote bytes: " + data.length);
				
				outStream = new FileOutputStream(String.format("/mnt/sdcard/%d.jpg", System.currentTimeMillis()));
				outStream.write(data);
				outStream.close();
				imageData  = data;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "FOTOCAMERA - onPictureTaken - jpeg");
		}

	};

	private void sendImage(byte[] data, String urlToSendPicture, String descrizione) throws UnsupportedEncodingException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(urlToSendPicture+"/");
		ByteArrayBody bab = new ByteArrayBody(data,"image/jpeg","imageName");
		StringBody desc = new StringBody(descrizione);
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		reqEntity.addPart("uploadFormElement", bab);
		reqEntity.addPart("caption", desc);
		postRequest.setEntity(reqEntity);
		try {
		    HttpResponse response = httpClient.execute(postRequest);
	    	dialog1.dismiss();
		} catch (ClientProtocolException e) {
		    Log.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
		    Log.e(TAG, e.getMessage(), e);
		}
	}
}