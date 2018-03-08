package com.osu.instaapp;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private AuthorizationService mAuthorizationService;
    private AuthState mAuthState;
    private OkHttpClient mOkHttpClient, mOkHttpClient2;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TextView mLatText;
    private TextView mLonText;
    private Location mLastLocation;
    private LocationListener mLocationListener;
    private static final int LOCATION_PERMISSION_RESULT = 17;
    private String[] taskArr = new String[20];

    String tasklist = "tobechanged";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences authPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        setContentView(R.layout.activity_main);




        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mLatText = (TextView) findViewById(R.id.lat_placeholder);
        mLonText = (TextView) findViewById(R.id.lon_placeholder);
        mLatText.setText("No Lat available.");
        mLonText.setText("No Lon available.");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    mLonText.setText(String.valueOf(location.getLongitude()));
                    mLatText.setText(String.valueOf(location.getLatitude()));
                } else {
                    mLatText.setText("No Lat available.");
                    mLonText.setText("No Lon available.");
                }
            }
        };

        mAuthorizationService = new AuthorizationService(this);

        ((Button)findViewById(R.id.login_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAuthState();

                //maybe do a callback
                List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();
                HashMap<String, String> m = new HashMap<String, String>();
                m.put("task_num", "");
                m.put("task_title", "Welcome!");
                m.put("task_status", "Select \"List Treasures\" to view your list!");
                tasks.add(m);

                final SimpleAdapter taskAdapter = new SimpleAdapter(
                        MainActivity.this,
                        tasks,
                        R.layout.email_item,
                        new String[]{"task_num", "task_title", "task_status"},
                        new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                    }
                });
            }
        });;

        ((Button)findViewById(R.id.logout_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuthState == null){
                    List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();
                    HashMap<String, String> m = new HashMap<String, String>();
                    m.put("task_num", "");
                    m.put("task_title", "You are already logged out!");
                    m.put("task_status", "Select \"Log In\" to view your list!");
                    tasks.add(m);

                    final SimpleAdapter taskAdapter = new SimpleAdapter(
                            MainActivity.this,
                            tasks,
                            R.layout.email_item,
                            new String[]{"task_num", "task_title", "task_status"},
                            new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                        }
                    });
                }
                else {
                    mAuthState = null;
                    List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();
                    HashMap<String, String> m = new HashMap<String, String>();
                    m.put("task_num", "");
                    m.put("task_title", "You have successfully logged out.");
                    m.put("task_status", "Select \"Log In\" to view your list!");
                    tasks.add(m);

                    final SimpleAdapter taskAdapter = new SimpleAdapter(
                            MainActivity.this,
                            tasks,
                            R.layout.email_item,
                            new String[]{"task_num", "task_title", "task_status"},
                            new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                        }
                    });
                }
            }
        });;

        ((Button)findViewById(R.id.get_drafts_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuthState == null){
                    List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();
                    HashMap<String, String> m = new HashMap<String, String>();
                    m.put("task_num", "");
                    m.put("task_title", "You are not Logged In!");
                    m.put("task_status", "Select \"Log In\" to view your list!");
                    tasks.add(m);

                    final SimpleAdapter taskAdapter = new SimpleAdapter(
                            MainActivity.this,
                            tasks,
                            R.layout.email_item,
                            new String[]{"task_num", "task_title", "task_status"},
                            new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                        }
                    });
                }
                else {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {

                                mOkHttpClient2 = new OkHttpClient();
                                HttpUrl reqUrl2 = HttpUrl.parse("https://www.googleapis.com/tasks/v1/users/@me/lists");
                                reqUrl2 = reqUrl2.newBuilder().build();
                                Request request2 = new Request.Builder()
                                        .url(reqUrl2)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .build();
                                mOkHttpClient2.newCall(request2).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r2 = response.body().string();
                                        try {
                                            JSONObject j = new JSONObject(r2);
                                            JSONArray items = j.getJSONArray("items");

                                            for (int i = 0; i < items.length(); i++){
                                                if (items.getJSONObject(i).getString("title").equals("osulist"))
                                                    tasklist = items.getJSONObject(i).getString("id");
                                            }
                                        } catch (JSONException el) {
                                            el.printStackTrace();
                                        }
                                    }
                                });

                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/tasks/v1/lists/" + tasklist + "/tasks");
                                reqUrl = reqUrl.newBuilder().build();
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        try {
                                            List<Map<String, String>> tasks2 = new ArrayList<Map<String, String>>();
                                            HashMap<String, String> m2 = new HashMap<String, String>();
                                            m2.put("task_num", "");
                                            m2.put("task_title", "You have not dropped any treasures.");
                                            m2.put("task_status", "Go on, drop a treasure!");
                                            tasks2.add(m2);

                                            final SimpleAdapter taskAdapter2 = new SimpleAdapter(
                                                    MainActivity.this,
                                                    tasks2,
                                                    R.layout.email_item,
                                                    new String[]{"task_num", "task_title", "task_status"},
                                                    new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter2);
                                                }
                                            });

                                            JSONObject j = new JSONObject(r);
                                            JSONArray items = j.getJSONArray("items");

                                            for (int i = 0; i < taskArr.length; i++)
                                                taskArr[i] = null;

                                            List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();

//                                            if (false){
//                                                HashMap<String, String> m = new HashMap<String, String>();
//                                                m.put("task_num", "");
//                                                m.put("task_title", "You have not dropped any treasures.");
//                                                m.put("task_status", "Go on, drop a treasure!");
//                                                tasks.add(m);
//                                            }
//                                            else {
                                                for (int i = 0; i < items.length(); i++) {
                                                    HashMap<String, String> m = new HashMap<String, String>();
                                                    m.put("task_num", "LOCATION #" + Integer.toString(i + 1));
                                                    m.put("task_title", items.getJSONObject(i).getString("title"));
                                                    if (items.getJSONObject(i).getString("status").equals("completed"))
                                                        m.put("task_status", "Your treasure has been retreived.");
                                                    else
                                                        m.put("task_status", "Your treasure has not yet been found!");
                                                    tasks.add(m);

                                                    taskArr[i + 1] = items.getJSONObject(i).getString("id");
                                                }
                                            //}

                                            final SimpleAdapter taskAdapter = new SimpleAdapter(
                                                    MainActivity.this,
                                                    tasks,
                                                    R.layout.email_item,
                                                    new String[]{"task_num", "task_title", "task_status"},
                                                    new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                                                }
                                            });
                                        } catch (JSONException el) {
                                            el.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });;


        ((Button)findViewById(R.id.new_task_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuthState == null){
                    List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();
                    HashMap<String, String> m = new HashMap<String, String>();
                    m.put("task_num", "");
                    m.put("task_title", "You are not Logged In!");
                    m.put("task_status", "Select \"Log In\" to view your list!");
                    tasks.add(m);

                    final SimpleAdapter taskAdapter = new SimpleAdapter(
                            MainActivity.this,
                            tasks,
                            R.layout.email_item,
                            new String[]{"task_num", "task_title", "task_status"},
                            new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                        }
                    });
                }
                else {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {

                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/tasks/v1/lists/" + tasklist + "/tasks");
                                reqUrl = reqUrl.newBuilder().build();

                                String json = "{\"title\":\"" + mLatText.getText() + "," + mLonText.getText() + "\"}";

                                RequestBody body = RequestBody.create(JSON, json);
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .post(body)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        try {
                                            JSONObject j = new JSONObject(r);
                                            List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();
                                            HashMap<String, String> m = new HashMap<String, String>();
                                            m.put("task_num", "");
                                            m.put("task_title", "Your treasure has been added!");
                                            m.put("task_status", "Select \"List Treasures\" to view your list!");
                                            tasks.add(m);

                                            final SimpleAdapter taskAdapter = new SimpleAdapter(
                                                    MainActivity.this,
                                                    tasks,
                                                    R.layout.email_item,
                                                    new String[]{"task_num", "task_title", "task_status"},
                                                    new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                                                }
                                            });
                                        } catch (JSONException el) {
                                            el.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });;

        ((Button)findViewById(R.id.mark_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuthState == null){
                    List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();
                    HashMap<String, String> m = new HashMap<String, String>();
                    m.put("task_num", "");
                    m.put("task_title", "You are not Logged In!");
                    m.put("task_status", "Select \"Log In\" to view your list!");
                    tasks.add(m);

                    final SimpleAdapter taskAdapter = new SimpleAdapter(
                            MainActivity.this,
                            tasks,
                            R.layout.email_item,
                            new String[]{"task_num", "task_title", "task_status"},
                            new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                        }
                    });
                }
                else {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            EditText num = (EditText) findViewById(R.id.mark_num);
                            int numInt = Integer.parseInt(num.getText().toString());
                            if (e == null && taskArr[numInt] != null) {

                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/tasks/v1/lists/" + tasklist + "/tasks/" + taskArr[numInt]);
                                reqUrl = reqUrl.newBuilder().build();

                                String json = "{\"status\":\"completed\"}";

                                RequestBody body = RequestBody.create(JSON, json);
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .patch(body)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        try {
                                            JSONObject j = new JSONObject(r);
                                            List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();
                                            HashMap<String, String> m = new HashMap<String, String>();
                                            m.put("task_num", "");
                                            m.put("task_title", "This treasure has been marked found!");
                                            m.put("task_status", "Select \"List Treasures\" to view your list!");
                                            tasks.add(m);

                                            final SimpleAdapter taskAdapter = new SimpleAdapter(
                                                    MainActivity.this,
                                                    tasks,
                                                    R.layout.email_item,
                                                    new String[]{"task_num", "task_title", "task_status"},
                                                    new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                                                }
                                            });
                                        } catch (JSONException el) {
                                            el.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });;

        ((Button)findViewById(R.id.clear_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuthState == null){
                    List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();
                    HashMap<String, String> m = new HashMap<String, String>();
                    m.put("task_num", "");
                    m.put("task_title", "You are not Logged In!");
                    m.put("task_status", "Select \"Log In\" to view your list!");
                    tasks.add(m);

                    final SimpleAdapter taskAdapter = new SimpleAdapter(
                            MainActivity.this,
                            tasks,
                            R.layout.email_item,
                            new String[]{"task_num", "task_title", "task_status"},
                            new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                        }
                    });
                }
                else {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {

                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/tasks/v1/lists/" + tasklist + "/clear");
                                reqUrl = reqUrl.newBuilder().build();

                                RequestBody body = RequestBody.create(null, new byte[]{});
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .post(body)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        List<Map<String, String>> tasks = new ArrayList<Map<String, String>>();
                                        HashMap<String, String> m = new HashMap<String, String>();
                                        m.put("task_num", "");
                                        m.put("task_title", "Found treasures cleared!");
                                        m.put("task_status", "Select \"Get List Treasures\" to view your list!");
                                        tasks.add(m);

                                        final SimpleAdapter taskAdapter = new SimpleAdapter(
                                                MainActivity.this,
                                                tasks,
                                                R.layout.email_item,
                                                new String[]{"task_num", "task_title", "task_status"},
                                                new int[]{R.id.item_num, R.id.item_1, R.id.item_2});
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((ListView) findViewById(R.id.email_listview)).setAdapter(taskAdapter);
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });;

    }

    @Override
    protected void onStart(){

//        Button button = (Button) findViewById(R.id.accounts_btn);
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mAuthState = getOrCreateAuthState();
//            }
//        });
        mGoogleApiClient.connect();
        mAuthState = getOrCreateAuthState();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_RESULT);
            return;
        }
        updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Dialog errDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0);
        errDialog.show();
        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { //.length > 0) {
                updateLocation();
            }
        }
    }

    private void updateLocation() {
        //askedPermission = false;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_RESULT);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLonText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
    }

    AuthState getOrCreateAuthState(){
        AuthState auth = null;
        SharedPreferences authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        String stateJson = authPreference.getString("stateJson", null);
        if (stateJson != null){
            try{
                auth = AuthState.jsonDeserialize(stateJson);
            } catch (JSONException e){
                e.printStackTrace();
                return null;
            }
        }
        if (auth != null && auth.getAccessToken() != null){
            return auth;
        } else {
            updateAuthState();
            return null;
        }
    }

    void updateAuthState(){
        Uri authEndpoint = new Uri.Builder().scheme("https").authority("accounts.google.com").path("/o/oauth2/v2/auth").build();
        Uri tokenEndpoint = new Uri.Builder().scheme("https").authority("www.googleapis.com").path("/oauth2/v4/token").build();
        Uri redirect = new Uri.Builder().scheme("com.osu.instaapp").path("foo").build();

        AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(authEndpoint, tokenEndpoint, null);
        AuthorizationRequest req = new AuthorizationRequest.Builder(config, "689950229794-sppchojro206hlshqghjdk8m8k5diq0j.apps.googleusercontent.com", ResponseTypeValues.CODE, redirect)
                .setScopes("https://www.googleapis.com/auth/tasks")
                .build();

        Intent authComplete = new Intent(this, AuthCompleteActivity.class);
        mAuthorizationService.performAuthorizationRequest(req, PendingIntent.getActivity(this, req.hashCode(), authComplete, 0));
    }
}
