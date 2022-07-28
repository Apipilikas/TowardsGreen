package activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.aueb.towardsgreen.Connection;
import com.aueb.towardsgreen.asynctask.ResultAsyncTask;
import com.aueb.towardsgreen.dialog.MyProgressDialog;
import com.aueb.towardsgreen.R;
import com.aueb.towardsgreen.Request;
import com.aueb.towardsgreen.dialog.ResultAlertDialog;
import com.aueb.towardsgreen.User;
import com.aueb.towardsgreen.UserDao;
import com.aueb.towardsgreen.Profile;
import com.google.gson.Gson;

import org.json.JSONException;

public class SignInActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private CheckBox rememberMe;
    private Button logInBtn;
    private Button createAccountBtn;
    private User rememberedUser;
    private User logInUser;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);
        setContentView(R.layout.activity_sign_in);

        showInputAddressDialog();
        UserDao userDao = UserDao.getInstance(this);
        rememberedUser = userDao.retrieveUser();

        email = findViewById(R.id.sign_in_email_edtxt);
        password = findViewById(R.id.sign_in_password_edtxt);
        rememberMe = findViewById(R.id.sign_in_remember_me_checkbox);
        logInBtn = findViewById(R.id.sign_in_log_in_btn);
        createAccountBtn = findViewById(R.id.sign_in_create_account_btn);

        if (!rememberedUser.getEmail().equals("null")) {
            email.setText(rememberedUser.getEmail());
            password.setText(rememberedUser.getPassword());
        }

        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logInUser = new User(email.getText().toString(),
                                     password.getText().toString());

                AuthenticationAsyncTask authenticationAsyncTask = new AuthenticationAsyncTask(SignInActivity.this,
                        gson.toJson(logInUser), "USERCON");
                authenticationAsyncTask.execute();
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, CreateProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private  void showInputAddressDialog() {
        EditText editText = new EditText(this);
        editText.setText("10.0.2.2");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        ConnectionAsyncTask connectionAsyncTask = new ConnectionAsyncTask(SignInActivity.this);
                        connectionAsyncTask.execute();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Πληκτρολογήστε την local address:")
                .setView(editText)
                .setPositiveButton("Εντάξει", dialogClickListener)
                .setNegativeButton("Ακύρωση", dialogClickListener);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = editText.getText().toString();
                Connection.getInstance().setAddress(address);
                alertDialog.dismiss();
                ConnectionAsyncTask connectionAsyncTask = new ConnectionAsyncTask(SignInActivity.this);
                connectionAsyncTask.execute();
            }
        });
    }

    private class ConnectionAsyncTask extends ResultAsyncTask {

        public ConnectionAsyncTask(Context context) {
            super(context, "Παρακαλώ περιμένετε για σύνδεση...");
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            Connection.getInstance().connect();
            return super.doInBackground(strings);
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            if (!rememberedUser.getEmail().equals("null")) {
                AuthenticationAsyncTask authenticationAsyncTask = new AuthenticationAsyncTask(SignInActivity.this,
                        gson.toJson(rememberedUser), "USERCON");
                authenticationAsyncTask.execute();
            }
        }
    }

    private class AuthenticationAsyncTask extends ResultAsyncTask {


        public AuthenticationAsyncTask(Context context, String json, String requestType) {
            super(context, "Παρακαλώ περιμένετε για αυθεντικοποίηση...", json, requestType);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (rememberMe.isChecked()) {
                    try {
                        UserDao.getInstance(SignInActivity.this).saveUser(logInUser);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Request request = new Request("GETPR", email.getText().toString());
                String json = Connection.getInstance().requestGetData(request).get(0);
                Profile profile = gson.fromJson(json, Profile.class);

                Connection.getInstance().setProfile(profile);
            }

            super.onPostExecute(result);

            String successMessage = "Η αυθεντικοποίηση έγινε επιτυχώς.";
            String failureMessage = "Το email ή ο κωδικός είναι λάθος. Ξαναπροσπαθήστε!";
            ResultAlertDialog resultAlertDialog = new ResultAlertDialog(SignInActivity.this, getLayoutInflater());
            resultAlertDialog.showResultAlertDialog(result, successMessage, failureMessage);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    resultAlertDialog.dismissAlertDialog();
                    if (result) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }, 3000);
        }
    }
}