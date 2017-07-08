package com.example.swapnil.cleanbangalore;

import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class UploadToServer extends Activity {

    String locality;
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private TextView txtPercentage;
    private ImageView imgPreview;
    static String filePath = null;
    private VideoView vidPreview;
    private Button btnUpload;

    long totalSize = 0;
    static String youString;
    String file;

    static int a = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_server);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        btnUpload = (Button) findViewById(R.id.btnUpload);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        vidPreview = (VideoView) findViewById(R.id.videoPreview);

        String name = getIntent().getStringExtra("name");

        Intent i = getIntent();
        filePath = i.getStringExtra("filename");

        double lat = i.getDoubleExtra("lat", 00.0000000);
        double lng = i.getDoubleExtra("lng", 00.0000000);

        DecimalFormat df = new DecimalFormat("00.0000000");
        String finalLat = df.format(lat);
        String finalLng = df.format(lng);




        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CleanBangalore");
        File one = new File(filePath);
        Log.d("CleanBangaloreErr", filePath);
        File two = new File(mediaStorageDir + File.separator + finalLat +
                "_" + finalLng + "_" + name + ".jpg");
        one.renameTo(two);
        file = two.getAbsolutePath();

        boolean isImage = i.getBooleanExtra("isImage", true);


        if (filePath != null) {
            previewMedia(isImage);
        } else {
        }

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server
                new UploadFileToServer().execute();
            }
        });


    }

    private void previewMedia(boolean isImage) {
        if (isImage) {
            imgPreview.setVisibility(View.VISIBLE);
            vidPreview.setVisibility(View.GONE);
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(file, options);

            imgPreview.setImageBitmap(bitmap);
        } else {
            imgPreview.setVisibility(View.GONE);
            vidPreview.setVisibility(View.VISIBLE);
            vidPreview.setVideoPath(filePath);
            vidPreview.start();
        }
    }

    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressBar.setVisibility(View.VISIBLE);

            progressBar.setProgress(progress[0]);

            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(file);

                entity.addPart("image", new FileBody(sourceFile));
                locality = getIntent().getStringExtra("locality");


                youString = locality;
                entity.addPart("locality", new StringBody(youString));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    responseString = "Successfully uploaded, shall be forwarded to the authorities.";
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;

                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);

            showAlert(result);

            super.onPostExecute(result);
        }
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Status:")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        Intent ok = new Intent(getApplicationContext(), MainActivity.class);
                        setResult(9001,ok);

                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        a++;

    }
}