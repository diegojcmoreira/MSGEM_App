package app.num.http;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog progress;

    //UI Components
    EditText userName;
    EditText password;
    Button clickButton;

    //User Credencials
    String User;
    String Pass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        clickButton = (Button) findViewById(R.id.login_ok);

        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                User = userName.getText().toString();
                Pass = password.getText().toString();
                new PostRequest().execute();
            }
        });

    }

    public class PostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected JSONObject setUpLoginJson(String user, String pass) throws JSONException {
                /*
                 * {

                            "action": "1",  -> Code 1 for Login
                            "param": {
                                        "UserName": "{UserName}"
                                        "Password": "{Passordw}"

                            }


                   }
                * */

            JSONObject request = new JSONObject();
            JSONObject param = new JSONObject();
            JSONObject ret = new JSONObject();


            request.put("action", "1");
            param.put("UserName", user);
            param.put("Password", pass);
            request.put("param", param);
            ret.put("request", request);


            return ret;
        }

        protected String doInBackground(String... arg0) {

            try{

                URL url = new URL("http://192.168.25.14:8080/AppServer/");

                JSONObject postDataParams = setUpLoginJson(User, Pass);

                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    Log.d("reSPOSTA",conn.getResponseMessage());

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject response = new JSONObject(result);
                int status = response.getInt("status");

                switch (status) {
                    case 1:
                        Toast.makeText(getApplicationContext(), "Bem-Vindo",
                                Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "Usuario não existe",
                                Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        Toast.makeText(getApplicationContext(), "Senha Incorreta",
                                Toast.LENGTH_LONG).show();
                        break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}
