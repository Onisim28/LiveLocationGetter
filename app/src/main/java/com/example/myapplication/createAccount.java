package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.data.model.Person;
import com.example.myapplication.data.model.User;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class createAccount extends AppCompatActivity {
    private final Executor executor = Executors.newFixedThreadPool(1);
    private final static String USER_KEY = "user_key";
    private volatile Handler msgHandler;

    EditText usernameEt, emailAddressEt, passwordEt, confirmPasswordEt;
    Button createAccountB, alreadyHaveAccountB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        msgHandler = new MsgHandler(this);

        usernameEt = (EditText) findViewById(R.id.username);
        emailAddressEt = (EditText) findViewById(R.id.emailAddress);
        passwordEt = (EditText) findViewById(R.id.password);
        confirmPasswordEt = (EditText) findViewById(R.id.confirmPassword);

        createAccountB = (Button) findViewById(R.id.createAccount);
        alreadyHaveAccountB = (Button) findViewById(R.id.alreadyHaveAccount);

        createAccountB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validationsSucceed = checkAllFields();
                executor.execute(new Runnable() {
                    public void run() {
                        Message msg = msgHandler.obtainMessage();
                        try {

                            if (validationsSucceed) {
                                String username = usernameEt.getText().toString();
                                String emailAddress = emailAddressEt.getText().toString();
                                String password = passwordEt.getText().toString();
                                String confirmPassword = confirmPasswordEt.getText().toString();
                                msg.arg1 = makePost(username, emailAddress, password, confirmPassword) ? 1 : 0;
                                msgHandler.sendMessage(msg);
                            }
                        } catch (
                                IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        alreadyHaveAccountB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(createAccount.this, login.class);
                    startActivity(intent);

            }
        });


        if (savedInstanceState != null) {
            String userJson = savedInstanceState.getString(USER_KEY);
            Log.d("createAccount.class", "savedInstanceState is not null  userJson = " + userJson);
            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);
            usernameEt.setText(user.getUsername());
            emailAddressEt.setText(user.getEmail());
            passwordEt.setText(user.getPassword());
            confirmPasswordEt.setText(user.getConfirmPassword());
        } else {
            Log.d("createAccount.class", "savedInstanceState is null");
        }
    }

    private boolean makePost(String username, String email, String password, String passwordConfirm) throws IOException {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("email", email);
            jsonObject.put("password", password);
            jsonObject.put("passwordConfirm", passwordConfirm);
            jsonObject.put("is_administrator", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        /*.url("http://192.168.1.9:8080/users/add")*/
        Request request = new Request.Builder()
                .url("http://192.168.1.9:8080/users/add")
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            System.out.println("Error: " + response);
            return false;
        }

        System.out.println(response.body().string());
        return true;
    }

    private boolean checkAllFields() {

        if (usernameEt.length() == 0) {
            usernameEt.setError("Username is required!");
            return false;
        } else if (usernameEt.length() < 5) {
            usernameEt.setError("Username must have a length of minimum 5 characters!");
            return false;
        }

        if (emailAddressEt.length() == 0) {
            emailAddressEt.setError("Email is required");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailAddressEt.getText()).matches()) {
            emailAddressEt.setError("The following regex must be respected: " + Patterns.EMAIL_ADDRESS.toString());
            return false;
        }

        if (passwordEt.length() == 0) {
            passwordEt.setError("Password is required!");
            return false;
        } else if (passwordEt.length() < 5) {
            passwordEt.setError("Password must have a length of minimum 5 characters!");
            return false;
        }

        if (confirmPasswordEt.length() == 0) {
            confirmPasswordEt.setError("Confirm password is required!");
            return false;
        } else if (!passwordEt.getText().toString().equals(confirmPasswordEt.getText().toString())) {

            confirmPasswordEt.setError("Passwords do not match!");
            return false;
        }
        /*return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());*/


        // after all validations where checked, return true.
        return true;
    }

    private static class MsgHandler extends Handler {
        private final WeakReference<Activity> createAccount;

        public MsgHandler(Activity activity) {
            createAccount = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 1) {
                Toast.makeText(createAccount.get().getApplicationContext(),
                        "Account created successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(createAccount.get().getApplicationContext(),
                        "Account could not be created!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Gson gson = new Gson();
        User user = new User();
        user.setUsername(usernameEt.getText().toString());
        user.setEmail(emailAddressEt.getText().toString());
        user.setPassword(passwordEt.getText().toString());
        user.setConfirmPassword(confirmPasswordEt.getText().toString());
        String userJson = gson.toJson(user);
        Log.d(TAG, "onSaveInstanceState()");
        outState.putString(USER_KEY, userJson);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("TAG", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("TAG", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("TAG", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("TAG", "onDestroy");
    }
}

