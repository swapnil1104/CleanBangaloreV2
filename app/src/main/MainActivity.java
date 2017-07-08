package projectssy.com.cleanbangalore;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity {

    private static final int ERROR_DIALOG_REQUEST = 9001;
    EditText et;

    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cd = new ConnectionDetector(getApplicationContext());

        GifView pGif = (GifView) findViewById(R.id.gif_view);
        pGif.setImageResource(R.drawable.intro);
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

        isInternetPresent = cd.isConnectingToInternet();

        // check for Internet status
        if (isInternetPresent) {
            if (checkCameraHardware(this)) {
                et = (EditText) findViewById(R.id.nameText);
                String name = et.getText().toString();

                if(name.isEmpty()) {
                    et.setError("Cannot leave this blank.");

                }else if (!isAlpha(name)) {
                    et.setError("This doesn't look legit.");
                } else {

                    String firstWord = null;
                    if(name.contains(" ")){
                        firstWord= name.substring(0, name.indexOf(" "));
                    } else {
                        firstWord = name;
                    }
                    Intent sendName = new Intent(this, CameraActivity.class);


                    sendName.putExtra("name", firstWord);
                    startActivity(sendName);
                    finish();
                }

            } else {
                Toast.makeText(getApplicationContext(), "No Camera Detected", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            AlertDialog.Builder net = new AlertDialog.Builder(this);
            net.setMessage("This application needs internet access to work, Connect to internet and try again.");
            net.setOnCancelListener(new DialogInterface.OnCancelListener() {

                                        public void onCancel(DialogInterface dialog) {
                                            finish();
                                        }


                                    }
            );
            net.show();

        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isAlpha(String name) {
        Pattern p = Pattern.compile("^[ A-z]+$");
        Matcher m = p.matcher(name);
        boolean b = m.matches();
        return b;
    }
}
