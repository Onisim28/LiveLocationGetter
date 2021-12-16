package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.data.model.LoggedInUser;
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
import okhttp3.ResponseBody;

public class login extends AppCompatActivity {
    private Executor executor = Executors.newFixedThreadPool(1);
    private volatile Handler msgHandler;
    private LoggedInUser loggedInUser = new LoggedInUser();
    public final static String LOGGED_IN_USER_KEY = "loggedInUser";
    private final static String USER_KEY = "user_key";
    EditText usernameEt, passwordEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        msgHandler = new login.MsgHandler(this);

        usernameEt = (EditText) findViewById(R.id.username);
        passwordEt = (EditText) findViewById(R.id.password);

        Button loginB = (Button) findViewById(R.id.login);

        Button dontHaveAccountB = (Button) findViewById(R.id.dontHaveAccount);

        dontHaveAccountB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent intent = new Intent(login.this, createAccount.class);
                startActivity(intent);*/
                finish();
            }
        });

        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validationsSucceed = checkAllFields();
                executor.execute(new Runnable() {
                    public void run() {
                        Message msg = msgHandler.obtainMessage();
                        try {
                            if (validationsSucceed) {
                                String username = usernameEt.getText().toString();
                                String password = passwordEt.getText().toString();
                                msg.arg1 = makePost(username, password) ? 1 : 0;
                                msgHandler.sendMessage(msg);
                                if (msg.arg1 == 1) {
                                    Intent intent = new Intent(login.this, HomePage.class);
                                    intent.putExtra(LOGGED_IN_USER_KEY, loggedInUser);
                                    startActivity(intent);
                                }
                            }

                        } catch (
                                IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

        if (savedInstanceState != null) {
            String userJson = savedInstanceState.getString(USER_KEY);
            Log.d("login.class", "savedInstanceState is not null  userJson = " + userJson);
            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);
            usernameEt.setText(user.getUsername());
            passwordEt.setText(user.getPassword());
        } else {
            Log.d("createAccount.class", "savedInstanceState is null");
        }
    }

    private boolean makePost(String username, String password) throws IOException {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            jsonObject.put("is_administrator", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request request = new Request.Builder()
                .url("http://192.168.1.9:8080/users/login")
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            System.out.println("Error: " + response);
            return false;
        }


        Gson gson = new Gson();

        ResponseBody rsp = response.body();

        loggedInUser = gson.fromJson(rsp.string(), LoggedInUser.class);

        if (loggedInUser.getId() <= 0)
            return false;

        return true;
    }

    private boolean checkAllFields() {

        if (usernameEt.length() == 0) {
            usernameEt.setError("Username is required!");
            return false;
        }

        if (passwordEt.length() == 0) {
            passwordEt.setError("Password is required!");
            return false;
        }

        // after all validations where checked, return true.
        return true;
    }


    private static class MsgHandler extends Handler {
        private final WeakReference<Activity> login;

        public MsgHandler(Activity activity) {
            login = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 1) {
                Toast.makeText(login.get().getApplicationContext(),
                        "Welcome back", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(login.get().getApplicationContext(),
                        "Login attempt was unsuccessfully", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Gson gson = new Gson();
        User user = new User();
        user.setUsername(usernameEt.getText().toString());
        user.setPassword(passwordEt.getText().toString());
        String userJson = gson.toJson(user);
        Log.d(TAG, "onSaveInstanceState()");
        outState.putString(USER_KEY, userJson);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String userJson = savedInstanceState.getString(USER_KEY);
        Log.d("login.class", "savedInstanceState is not null  userJson = " + userJson);
        Gson gson = new Gson();
        User user = gson.fromJson(userJson, User.class);
        usernameEt.setText(user.getUsername());
        passwordEt.setText(user.getPassword());

        System.out.println("whooooooooooooooraaaaaaaaaay!!");


    }

}

