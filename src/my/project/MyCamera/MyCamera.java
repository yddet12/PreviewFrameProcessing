package my.project.MyCamera;

import java.io.IOException;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout.LayoutParams;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MyCamera extends Activity implements SurfaceHolder.Callback, PreviewCallback {
	
	private static final String TAG="CameraView";
	
    public SurfaceHolder holder;
    public SurfaceView sView;
    public Camera camera;
    public int width, height;
    public Mat mYuv, mRgba, mGray;
    
    //need the function below to load/link the opencv library. Without it, new Mat(...) will fail
    private BaseLoaderCallback  mOpenCVCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    //System.loadLibrary("native_code_interface");
                    //mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "Trying to load OpenCV library");
		//need the if statement below to load/link the opencv library. Without it, new Mat(...) will fail
	    if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallback))
	    {
	      Log.e(TAG, "Cannot connect to OpenCV Manager");
	    }
		
		Log.i(TAG, "ON Create");
		sView = new SurfaceView(this);
		holder = sView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
        
		LayoutParams params= new LayoutParams(200 , 200);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.addContentView(sView, params);
	}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {    	
		Log.i(TAG, "surface created");
	        initCamera();
	        camera.startPreview();
    }
    
	private void initCamera() {
		try {
			camera = Camera.open();
			camera.setPreviewDisplay(holder);
			camera.setPreviewCallback(this);
			Camera.Parameters currentParams = camera.getParameters();
			width = currentParams.getPreviewSize().width;
			height = currentParams.getPreviewSize().height;
			
			// setting for google glass to avoid bad quality preview
			currentParams.setPreviewFpsRange(30000, 30000);
			camera.setParameters(currentParams);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	camera.stopPreview();
    	camera.setPreviewCallback(null);
		camera.release();
		camera=null;
		holder=null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
		Log.i(TAG, "Entered onpreviewframe");
		mYuv = new Mat(height, width, CvType.CV_8UC1);
		mYuv.put(0, 0, data);
		Log.i(TAG, "passed dimensions " + mYuv.cols() + "x" + mYuv.rows());
		Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV2RGBA_NV21);
		Imgproc.cvtColor(mYuv, mGray, Imgproc.COLOR_YUV2GRAY_NV21);
    }
}
