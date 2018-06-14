package com.anxell.e3ak;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class LandingActivity extends BaseActivity {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_title);
                builder.setMessage(R.string.permission_message_location);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
            else if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_title);
                builder.setMessage(R.string.permission_message_storage);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_EXTERNAL_STORAGE);
                    }
                });
                builder.show();
            }
            else
            {

//                check_GPS();
                Timer timer = new Timer();
                TimerTask tast = new TimerTask() {
                    @Override
                    public void run() {
                        openSearchPage();
                    }
                };
                timer.schedule(tast, 1000);
            }

        }
        else{
            Timer timer = new Timer();
            TimerTask tast = new TimerTask() {
                @Override
                public void run() {
                    openSearchPage();
                }
            };
            timer.schedule(tast, 1000);
        }
    }

    private void openSearchPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LandingActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransitionRightToLeft();
                finish();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.d("test", "coarse location permission granted");
//                    Timer timer = new Timer();
//                    TimerTask tast = new TimerTask() {
//                        @Override
//                        public void run() {
//                            openSearchPage();
//                        }
//                    };
//                    timer.schedule(tast, 1000);
                    if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(R.string.permission_title);
                        builder.setMessage(R.string.permission_message_storage);
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_EXTERNAL_STORAGE);
                            }
                        });
                        builder.show();
                    }
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.permission_title);
                    builder.setMessage(R.string.permission_message_location);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            int id= android.os.Process.myPid();
                            android.os.Process.killProcess(id);
                        }
                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timer timer = new Timer();
                    TimerTask tast = new TimerTask() {
                        @Override
                        public void run() {
                            openSearchPage();
                        }
                    };
                    timer.schedule(tast, 1000);
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.permission_title);
                    builder.setMessage(R.string.permission_message_storage);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            int id= android.os.Process.myPid();
                            android.os.Process.killProcess(id);
                        }
                    });
                    builder.show();
                }
                return;
            }

        }




    }




//    public void check_GPS()
//    {
//        final LocationManager locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            // show open gps message
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setCancelable(false);
////            builder.setTitle(R.string.gps_warning);
////            builder.setMessage(R.string.no_gps);
//            builder.setMessage(R.string.need_gps);
//            builder.setNegativeButton(getString(R.string.cancel), new
//                    android.content.DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            int id= android.os.Process.myPid();
//                            android.os.Process.killProcess(id);
//                        }
//                    });
//            builder.setPositiveButton(getString(R.string.ok), new
//                    android.content.DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // jump to setting
//                            if (!locManager.isProviderEnabled((LocationManager.GPS_PROVIDER)) && !locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
//                            {
//                                Intent enableGPSIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                                startActivity(enableGPSIntent);
//                                check_GPS();
//                            }
//                            else
//                            {
//                                Timer timer = new Timer();
//                                TimerTask tast = new TimerTask() {
//                                    @Override
//                                    public void run() {
//                                        openSearchPage();
//                                    }
//                                };
//                                timer.schedule(tast, 1000);
//                            }
//                        }
//                    });
//            builder.show();
//        }
//        else
//        {
//            Timer timer = new Timer();
//            TimerTask tast = new TimerTask() {
//                @Override
//                public void run() {
//                    openSearchPage();
//                }
//            };
//            timer.schedule(tast, 1000);
//        }
//    }




}
