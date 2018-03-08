package com.osu.instaapp;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

public class AuthCompleteActivity extends AppCompatActivity {

    private AuthorizationService mAuthorizationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_complete);

        mAuthorizationService = new AuthorizationService(this);
        Uri redirectUri = getIntent().getData();
        AuthorizationResponse resp = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException ex = AuthorizationException.fromIntent(getIntent());

        if (resp != null){
            final AuthState authState = new AuthState(resp, ex);
            mAuthorizationService.performTokenRequest(resp.createTokenExchangeRequest(),
                    new AuthorizationService.TokenResponseCallback(){
                        @Override
                        public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException e){
                            authState.update(tokenResponse, e);
                            SharedPreferences authPreferences = getSharedPreferences("auth", MODE_PRIVATE);
                            authPreferences.edit().putString("stateJson", authState.jsonSerializeString()).apply();
                            finish();
                        }
                    });
        }
    }
}