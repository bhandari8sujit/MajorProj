package com.example.sujit.customerapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    Button btn_getCode;
    EditText input_name, input_number, input_password, editTextOtp;
    AppCompatButton buttonConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        input_name = (EditText) findViewById(R.id.input_name);
        input_number = (EditText) findViewById(R.id.input_number);
        input_password = (EditText) findViewById(R.id.input_password);

        btn_getCode = (Button) findViewById(R.id.btn_getCode);
        btn_getCode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getCode();
            }
        });
    }
    private void getCode() {

        final String name = input_name.getText().toString().trim();
        final String number = input_number.getText().toString().trim();
        final String password = input_password.getText().toString().trim();
        // final String TAG_RESPONSE= "ErrorMessage";
       // RequestQueue requestQueue = Volley.newRequestQueue(Register.this);

        final ProgressDialog loading = ProgressDialog.show(this, "Getting OTP", "Please wait...", false, false);
        loading.show();

         StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.getmeacode,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        Log.d("myAPP", response);

                        try {
                            if (response.equalsIgnoreCase("success")) {
                                //        pDialog.dismiss();
                                Toast.makeText(Register.this, response, Toast.LENGTH_LONG).show();
                                confirmOtp(name, number, password);
                            } else {
                                //Displaying a toast if the otp entered is wrong
                                Toast.makeText(Register.this, response, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Toast.makeText(Register.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding the parameters to the request
                params.put("mobile_number", number);
                //  params.put("name", name);
                return params;
            }
        };
        //Adding request the the queue
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }
    private void confirmOtp(final String name, final String number, final String password) {

        //Creating a LayoutInflater object for the dialog box
        LayoutInflater li = LayoutInflater.from(this);
        //Creating a view to get the dialog box
        View confirmDialog = li.inflate(R.layout.dialog_confirm, null);

        //Initizliaing confirm button fo dialog box and edit text of dialog box
        buttonConfirm = (AppCompatButton) confirmDialog.findViewById(R.id.buttonConfirm);
        editTextOtp = (EditText) confirmDialog.findViewById(R.id.editTextOtp);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //Adding our dialog box to the view of alert dialog
        alert.setView(confirmDialog);

        //Creating an alert dialog
        final AlertDialog alertDialog = alert.create();

        //Displaying the alert dialog
        alertDialog.show();

        //On the click of the confirm button from alert dialog
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hiding the alert dialog
                alertDialog.dismiss();

                //Displaying a progressbar
                //pDialog.show();

                //Getting the user entered otp from edittext
                final String otp = editTextOtp.getText().toString().trim();

               // RequestQueue requestQueue = Volley.newRequestQueue(Register.this);

                //Creating an string request
                StringRequest stringRequest = new StringRequest(Request.Method.POST,Config.registeracustomer,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //if the server response is success
                                if (response.equalsIgnoreCase("success")) {
                                    //dismissing the progressbar
                                    //pDialog.dismiss();
                                    //Starting a new activity
                                    startActivity(new Intent(Register.this, Login.class));

                                } if (response.equalsIgnoreCase("failed")){
                                    //Displaying a toast if the otp entered is wrong
                                    Toast.makeText(Register.this, "Wrong OTP Please Try Again", Toast.LENGTH_LONG).show();

                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                alertDialog.dismiss();
                                Toast.makeText(Register.this, error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        //Adding the parameters otp and username
                        params.put("code", otp);
                        params.put("full_name", name);
                        params.put("mobile_number", number);
                        params.put("password", password);
                        return params;
                    }
                };
                //Adding the request to the queue
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
            }
        });
    }
}


