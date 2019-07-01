package com.mysweetyphone.phone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    private boolean RegOrLogin = false;     //Reg == true, Login == false
    TextView Nick;
    TextView Pass;
    TextView ErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_login);

        Nick = findViewById(R.id.NickLOGIN);
        Pass = findViewById(R.id.PasswordLOGIN);
        ErrorText = findViewById(R.id.ErrorLOGIN);
    }

    public void onModeChanged(View view){
        Button LoginButton = findViewById(R.id.LoginLOGIN);
        switch (view.getId()){
            case R.id.RegRatioLOGIN:
                LoginButton.setText(R.string.RegLOGIN);
                Nick.setEnabled(true);
                Pass.setEnabled(true);
                LoginButton.setOnClickListener(this::onLoginClick);
                break;
            case R.id.LoginRatioLOGIN:
                LoginButton.setText(R.string.log_inLOGIN);
                Nick.setEnabled(true);
                Pass.setEnabled(true);
                LoginButton.setOnClickListener(this::onLoginClick);
                break;
            case R.id.OfflineRatioLOGIN:
                LoginButton.setText(R.string.offline);
                Nick.setEnabled(false);
                Pass.setEnabled(false);
                LoginButton.setOnClickListener(this::Offline);
                break;
        }
    }

    public void onLoginClick(View view){
        try {
            RegOrLogin = ((RadioButton) findViewById(R.id.RegRatioLOGIN)).isChecked();

            if (!Pattern.matches("\\w+", Nick.getText().toString())) {
                ErrorText.setText(R.string.invalid_name);
                ErrorText.setVisibility(View.VISIBLE);
                return;
            }

            AsyncHttpClient client = new AsyncHttpClient();
            client.get("http://mysweetyphone.herokuapp.com/?Type=" + (RegOrLogin ? "Reg" : "Login") + "&Login=" + URLEncoder.encode(Nick.getText().toString(), "UTF-8") + "&Pass=" + URLEncoder.encode(Pass.getText().toString(), "UTF-8"), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
                    try {
                        switch (responseBody.getInt("code")) {
                            case 3:
                                ErrorText.setText(R.string.FillNameAndPassLOGIN);
                                ErrorText.setVisibility(View.VISIBLE);
                                break;
                            case 2:
                                ErrorText.setText(R.string.Exception);
                                ErrorText.setVisibility(View.VISIBLE);
                                break;
                            case 1:
                                ErrorText.setText(RegOrLogin ? R.string.ErrorRegingLOGIN : R.string.ErrorLoggingInLOGIN);
                                ErrorText.setVisibility(View.VISIBLE);
                                break;
                            case 0:
                                ErrorText.setVisibility(View.INVISIBLE);
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                editor.putInt("id", responseBody.getInt("id"));

                                TextView Nick = findViewById(R.id.NickLOGIN);
                                editor.putString("login", Nick.getText().toString());
                                editor.commit();
                                Intent intent = new Intent(getApplicationContext(), RegDevice.class);
                                intent.putExtras(getIntent());
                                intent.setAction(getIntent().getAction());
                                startActivity(intent);
                                finish();
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    public void Offline(View v){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("id");
        editor.remove("name");
        editor.remove("login");
        editor.apply();
        Intent intent = new Intent(getApplicationContext(), RegDevice.class);
        intent.putExtras(getIntent());
        intent.setAction(getIntent().getAction());
        startActivity(intent);
        finish();
    }
}
