package com.example.swapnil.cleanbangalore;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private Camera mCamera;
    private CameraPreview mPreview;
    View progress;
    static String fileName = null;
    static String TAG = "CleanBangalore";
    static String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        progress = findViewById(R.id.progress);
        mCamera = getCameraInstance();
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        mCamera.setDisplayOrientation(90);
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> list = params.getSupportedPictureSizes();
        int cameraWidth = list.get(1).width;
        int cameraHeight = list.get(1).height;
        params.setPictureSize(cameraWidth, cameraHeight);
        params.setRotation(90);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);

        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);

        {
            name = getIntent().getStringExtra("name");
            TextView tv = (TextView) findViewById(R.id.textView6);
            tv.setText(name);
        }

    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
        }
        return c;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(1);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");

                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }


    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CleanBangalore");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

          fileName = mediaStorageDir.getPath() + File.separator +
                "IMG_temp"  + ".jpg";
        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_temp"  + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    public void takePicture(View view) {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);

        ClickPicture take = new ClickPicture();
        take.execute();


    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void closeApp(View view) {
        finish();

    }


    private class ClickPicture extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void result) {
            Intent openMapActivity = new Intent(getApplicationContext(), MapsActivity.class);
            openMapActivity.putExtra("filename", fileName);
            openMapActivity.putExtra("name", name);
            Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();

            startActivity(openMapActivity);
            finish();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mCamera.takePicture(null, null, mPicture);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
