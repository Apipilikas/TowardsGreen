package com.aueb.towardsgreen.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.aueb.towardsgreen.Connection;
import com.aueb.towardsgreen.Request;
import com.aueb.towardsgreen.dialog.MyProgressDialog;

/**
 * This class is intended to get the result of a record's action.
 */
public class ResultAsyncTask extends AsyncTask<String, String, Boolean> {
    private MyProgressDialog progressDialog;
    private String json;
    private String requestType;

    /**
     * Simple constructor that initializes the Progress Dialog
     * with a message.
     * @param context The context of the Activity/Fragment.
     * @param message The message we would like to be displayed in the Dialog.
     */
    public ResultAsyncTask(Context context, String message) {
        progressDialog = new MyProgressDialog(context, message);
    }

    /**
     * This constructor is intended to send a request immediately to the
     * server. Response is expected as well.
     */
    public ResultAsyncTask(Context context, String message, String json, String requestType) {
        this(context, message);
        this.json = json;
        this.requestType = requestType;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.showProgressDialog();
    }

    /**
     * Here we use a little trick with the variable json. If the
     * latter is empty, that means that we don't expect a response from
     * the server. Otherwise, the expected result is returned to the
     * onPostExecute function.
     * */
    @Override
    protected Boolean doInBackground(String... strings) {
        if (json != null) {
            Request request = new Request(requestType, json);
            return Connection.getInstance().requestSendData(request);
        }
        else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        progressDialog.dismissProgressDialog();
    }
}