package com.example.swapnil.cleanbangalore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;

import junit.framework.TestCase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity {

    EditText et;
    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cd = new ConnectionDetector(getApplicationContext());
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        TextView et =(TextView) findViewById(R.id.MarqueeText);
        GifView pGif = (GifView) findViewById(R.id.gif_view);

        et.setSelected(true);

        pGif.setImageResource(R.drawable.intro);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }



    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();

                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void camAct(View view) {
        et = (EditText) findViewById(R.id.nameText);
        String name = et.getText().toString();

        isInternetPresent = cd.isConnectingToInternet();
        if(checkCameraHardware(this)) {
            if (name.isEmpty()) {
                et.setError("Cannot leave this blank");
            } else if (!isAlpha(name)) {
                et.setError("Only alphabets allowed");
            } else if (name.length() > 20 ) {
                et.setError("Too long");
            } else {

                if(isInternetPresent){
                    String firstWord = null;
                    if (name.contains(" ")) {
                        firstWord = name.substring(0, name.indexOf(" "));
                    } else {
                        firstWord = name;
                    }
                    Intent sendName = new Intent(this, CameraActivity.class);

                    sendName.putExtra("name", firstWord);
                    startActivityForResult(sendName, 9001);
                } else {
                    AlertDialog.Builder net = new AlertDialog.Builder(this);
                    net.setMessage("This application needs internet access to work, Connect to internet and try again");
                    net.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                public void onCancel(DialogInterface dialog) {
                                                    finish();
                                                }
                                            }
                    );
                    net.show();
                }

            }



        } else {
            Toast.makeText(getApplicationContext(), "No Camera Detected", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

    public boolean isAlpha(String name) {
        Pattern p = Pattern.compile("^[ A-z]+$");
        Matcher m = p.matcher(name);
        boolean b = m.matches();
        return b;
    }


}
