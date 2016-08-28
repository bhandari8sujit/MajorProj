package com.example.sujit.customerapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    Button buttonLogin;
    EditText editTextPhone, editTextPassword,editTextPhoneForgotPass;
    TextView linkSignup, linkForgotPassword,textView18;
    AppCompatButton buttonSendForgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        FirebaseMessaging.getInstance().subscribeToTopic("test");
  //      FirebaseInstanceId.getInstance().getToken();

        textView18=(TextView)findViewById(R.id.textView18);

        editTextPhone=(EditText)findViewById(R.id.editTextPhone);
        editTextPassword=(EditText)findViewById(R.id.editTextPassword);

        textView18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, MainActivity.class));
            }
        });

        linkForgotPassword=(TextView)findViewById(R.id.linkForgotPassword);
        linkForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });

        linkSignup=(TextView)findViewById(R.id.linkSignup);
        linkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        buttonLogin=(Button)findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }
    private void forgotPassword() {
        LayoutInflater li = LayoutInflater.from(this);
        View confirmDialog = li.inflate(R.layout.dialog_forgotpass,null);

        buttonSendForgotPass = (AppCompatButton) confirmDialog.findViewById(R.id.buttonSendForgotPass);
        editTextPhoneForgotPass = (EditText) confirmDialog.findViewById(R.id.editTextPhoneForgotPass);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //Adding our dialog box to the view of alert dialog
        alert.setView(confirmDialog);

        //Creating an alert dialog
        final AlertDialog alertDialog = alert.create();

        //Displaying the alert dialog
        alertDialog.show();

        buttonSendForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

                final String pNumber=editTextPhoneForgotPass.getText().toString().trim();
              //  RequestQueue requestQueue = Volley.newRequestQueue(Login.this);

                //
                //Creating an string request
                StringRequest stringRequest = new StringRequest(Request.Method.POST,Config.forgotpassword,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //if the server response is success
                                if (response.equalsIgnoreCase("success")) {
                                    Toast.makeText(Login.this, "Password Sent....Please Check Your Inbox", Toast.LENGTH_LONG).show();

                                } if (response.equalsIgnoreCase("failed")){
                                    Toast.makeText(Login.this, "Error!!", Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                alertDialog.dismiss();
                                Toast.makeText(Login.this, error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        //Adding the parameters otp and username
                        params.put("mobile_number", pNumber);
                        return params;
                    }
                };
                //Adding the request to the queue
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
            }
        });
    }
    private void login() {

        final String phoneNo = editTextPhone.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        SharedPreferences sharedPreference=getSharedPreferences("DeviceToken",MODE_PRIVATE);
        final String token = sharedPreference.getString("token","");

        SharedPreferences sharedPreferences = getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putString("phoneNo",phoneNo);
        spEditor.apply();

        final ProgressDialog loading = ProgressDialog.show(this, "Checking Your Credentials", "Please wait...", false, false);
      //  RequestQueue requestQueue = Volley.newRequestQueue(Login.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.customerlogin,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        //if the server response is success
                        if (response.equalsIgnoreCase("success")) {
                            Log.d("LoginResponse", response);
                            // storeUserData(phoneNo,password);
                            Toast.makeText(Login.this, response, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Login.this, MainActivity.class));

                        } else {
                            startActivity(new Intent(Login.this, Login.class));
                            Toast.makeText(Login.this, response, Toast.LENGTH_LONG).show();
                        }
                        // startActivity(new Intent(Login.this, Login.class));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Toast.makeText(Login.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding the parameters otp and username
                params.put("mobile_number", phoneNo);
                params.put("password", password);
                params.put("token",token);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }
}
