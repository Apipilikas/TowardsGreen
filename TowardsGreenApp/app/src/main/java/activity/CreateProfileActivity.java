package activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aueb.towardsgreen.Connection;
import com.aueb.towardsgreen.R;
import com.aueb.towardsgreen.Request;
import com.aueb.towardsgreen.Profile;
import com.aueb.towardsgreen.asynctask.ResultAsyncTask;
import com.aueb.towardsgreen.dialog.ResultAlertDialog;
import com.google.gson.Gson;

public class CreateProfileActivity extends AppCompatActivity {

    private Profile createdProfile;
    private EditText firstName,lastName,email,passwordInput,passwordConf;
    private Button btnSubmit, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);


        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        passwordConf = findViewById(R.id.confirmPas);

        btnCancel = findViewById(R.id.cancel);
        btnSubmit = findViewById(R.id.submit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                createdProfile = new Profile();
                if(TextUtils.isEmpty(firstName.getText())){
                    firstName.setError("Παρακαλώ εισάγετε ενα έγκυρο όνομα!");
                }else if(TextUtils.isEmpty(lastName.getText())){
                    lastName.setError("Παρακαλώ εισάγετε ενα έγκυρο επώνυμο!");
                }else if(TextUtils.isEmpty(email.getText())){
                    lastName.setError("Παρακαλώ εισάγετε ενα έγκυρο email!");
                }else if(TextUtils.isEmpty(passwordInput.getText())){
                    lastName.setError("Παρακαλώ εισάγετε εναν έγκυρο κωδικό!");
                }else if(TextUtils.isEmpty(lastName.getText())){
                    lastName.setError("Παρακαλώ εισάγετε εναν έγκυρο κωδικό!");
                }else if(!passwordInput.getText().toString().equals(passwordConf.getText().toString())){
                    Toast.makeText(getApplicationContext(),"Οι κωδικοί δεν ταιριάζουν",Toast.LENGTH_LONG).show();
                }else{
                createdProfile.setFirstName(firstName.getText().toString());
                createdProfile.setLastName(lastName.getText().toString());
                createdProfile.setEmail(email.getText().toString());
                createdProfile.setPassword(passwordInput.getText().toString());
                createdProfile.generateQR();
                
                CreateProfileAsyncTask createProfileAsyncTask = new CreateProfileAsyncTask(CreateProfileActivity.this,
                        new Gson().toJson(createdProfile), "INPR");
                createProfileAsyncTask.execute();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private class CreateProfileAsyncTask extends ResultAsyncTask {

        public CreateProfileAsyncTask(Context context, String json, String requestType) {
            super(context, "Παρακαλώ περιμένετε...", json, requestType);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            String successMessage = "Ο λογαριασμός δημιουργήθηκε με επιτυχία";
            String failureMessage = "Ο λογαριασμός δεν δημιουργήθηκε!";
            ResultAlertDialog resultAlertDialog = new ResultAlertDialog(CreateProfileActivity.this, getLayoutInflater());
            resultAlertDialog.showResultAlertDialog(result, successMessage, failureMessage);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    resultAlertDialog.dismissAlertDialog();
                    finish();
                }
            }, 3000);
        }
    }
}