package com.aueb.towardsgreen.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aueb.towardsgreen.R;

import activity.MainActivity;

/**
 * This class is intended to display the success and failure Alert Dialog.
 */
public class ResultAlertDialog {
    private AlertDialog alertDialog;
    private AlertDialog.Builder builderDialog;
    private View successLayoutView, failureLayoutView;
    private TextView successMsg, failureMsg;

    /**
     * Creates an Alert Dialog to display the result.
     * @param context The context of the fragment/activity
     * @param inflater The inflated layout used in fragment/activity
     */
    public ResultAlertDialog(Context context, LayoutInflater inflater) {
        builderDialog = new AlertDialog.Builder(context);

        successLayoutView = inflater.inflate(R.layout.success_dialog, null);
        failureLayoutView = inflater.inflate(R.layout.failure_dialog, null);

        successMsg = successLayoutView.findViewById(R.id.success_dialog_txt);
        failureMsg = failureLayoutView.findViewById(R.id.failure_dialog_txt);
    }

    /**
     * Sets the messages and shows the Alert Dialog.
     * @param result Defines which of the success or failure dialogs will be displayed.
     * @param successMessage The message that will be displayed in success.
     * @param failureMessage The message that will be displayed in failure.
     */
    public void showResultAlertDialog(boolean result, String successMessage, String failureMessage) {
        View layoutView = null;

        if (result) {
            successMsg.setText(successMessage);
            layoutView = successLayoutView;
        }
        else {
            failureMsg.setText(failureMessage);
            layoutView = failureLayoutView;
        }

        builderDialog.setView(layoutView);
        alertDialog = builderDialog.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * Dismisses the Alert Dialog.
     */
    public void dismissAlertDialog() {
        alertDialog.dismiss();
    }

    /**
     * Dismisses the Alert Dialog with delay.
     * @param delay The duration of the Alert Dialog.
     */
    public void dismissAlertDialog(long delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissAlertDialog();
            }
        }, delay);
    }
}