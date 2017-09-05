package com.anxell.e3ak.transport;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.anxell.e3ak.SettingActivity;


/**
 * Created by Sean on 3/23/2017.
 */

public class GeneralDialog {

    public static void MessagePromptDialog(final Activity currActivity, String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(currActivity);

        dialog.setTitle(title);
        dialog.setMessage(message);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        dialog.show();
    }
    public static void restoreCompletedDialog(final Activity currActivity, String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(currActivity);

        dialog.setTitle(title);
        dialog.setMessage(message);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SettingActivity activity = (SettingActivity)currActivity;
                activity.onBackPressed();
                dialog.cancel();
            }
        });

        dialog.show();
    }
}
