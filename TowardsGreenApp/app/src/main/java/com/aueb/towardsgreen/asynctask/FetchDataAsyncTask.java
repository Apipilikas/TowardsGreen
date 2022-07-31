package com.aueb.towardsgreen.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.aueb.towardsgreen.Connection;
import com.aueb.towardsgreen.Request;
import com.aueb.towardsgreen.dialog.MyProgressDialog;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * This class is intended to fetch the data from the Server.
 */
public class FetchDataAsyncTask<T> extends AsyncTask<String, String, ArrayList<T>> {
    private MyProgressDialog progressDialog;
    private Class<T> classType;
    private String json;
    private String requestType;

    /**
     * Simple constructor that initializes the Progress Dialog
     * with a message.
     * @param context The context of the Activity/Fragment.
     * @param message The message we would like to be displayed in the Dialog.
     */
    public FetchDataAsyncTask(Context context, String message) {
        progressDialog = new MyProgressDialog(context, message);
    }

    /**
     * This constructor is intended to send a request immediately to the
     * server. Response is expected as well.
     * @param context The context of the Activity/Fragment.
     * @param message The message we would like to be displayed in the Dialog.
     * @param json The content of the request.
     * @param requestType The type of the request.
     */
    public FetchDataAsyncTask(Context context, String message, String json,
                              String requestType, Class<T> classType) {
        this(context, message);
        this.json = json;
        this.requestType = requestType;
        this.classType = classType;
    }

    /**
     * This constructor is intended to send a request immediately to the
     * server. Response is expected as well.
     * @param json The content of the request.
     * @param requestType The type of the request.
     */
    public FetchDataAsyncTask(String json, String requestType, Class<T> classType) {
        this.json = json;
        this.requestType = requestType;
        progressDialog = null;
        this.classType = classType;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressDialog != null) {
            progressDialog.showProgressDialog();
        }
    }

    @Override
    protected ArrayList<T> doInBackground(String... strings) {
        Request request = new Request(requestType, json);
        ArrayList<String> jsons = Connection.getInstance().requestGetData(request);
        return convertJsonToEvents(jsons);
    }

    @Override
    protected void onPostExecute(ArrayList<T> arrayList) {
        super.onPostExecute(arrayList);
        if (progressDialog != null) {
            progressDialog.dismissProgressDialog();
        }
    }

    private ArrayList<T> convertJsonToEvents(ArrayList<String> jsons) {
        Gson gson = new Gson();
        ArrayList<T> data = new ArrayList<>();
        for (String json : jsons) {
            data.add(gson.fromJson(json, classType));
        }
        return data;
    }
}