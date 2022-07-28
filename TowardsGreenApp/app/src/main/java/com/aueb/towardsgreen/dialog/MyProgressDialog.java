package com.aueb.towardsgreen.dialog;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * This class is intended to display the Progress Dialog.
 */
public class MyProgressDialog {
    private ProgressDialog progressDialog;
    private String message;

    /**
     * Creates a Progress Dialog.
     * @param context The context of the fragment/activity.
     */
    public MyProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
    }

    /**
     * Creates a Progress Dialog.
     * @param context The context of the fragment/activity.
     * @param message The message that we would like to be displayed.
     */
    public MyProgressDialog(Context context, String message) {
        progressDialog = new ProgressDialog(context);
        this.message = message;
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
    }

    /**
     * Shows the Progress Dialog. Message has to be set previously.
     */
    public void showProgressDialog() {
        if (message != null) {
            progressDialog.show();
        }
    }

    /**
     * Sets the message and shows the Progress Dialog.
     * @param message The message that we would like to be displayed.
     */
    public void showProgressDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    /**
     * Dismisses the Progress Dialog.
     */
    public void dismissProgressDialog() {
        progressDialog.hide();
        progressDialog.dismiss();
    }

    /**
     * Message getter.
     * @return message Gets the dialog message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Message setter.
     * @param message Sets the dialog message.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}