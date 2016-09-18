package com.vaporwarecorp.mirror.sidekick.manager;

import android.app.Activity;
import android.text.TextUtils;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.vaporwarecorp.mirror.sidekick.R;
import com.wuman.android.auth.AuthorizationDialogController;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;
import rx.Observable;

import java.io.IOException;

public class ArtikOAuthManager {
// ------------------------------ FIELDS ------------------------------

    private static final String AUTHORIZATION_URL = "https://accounts.artik.cloud/authorize";
    private static final String CREDENTIALS_STORE_PREF_FILE = "oauth";
    private static final String REDIRECT_URL = "mirror://artik/redirect";
    private static final String TOKEN_URL = "https://accounts.artik.cloud/token";
    private static final String USER_ID = "artik";

    private final OAuthManager manager;

// -------------------------- STATIC METHODS --------------------------

    public static ArtikOAuthManager newInstance(Activity activity) {
        // create the ClientParametersAuthentication object
        final ClientParametersAuthentication client = new ClientParametersAuthentication(
                activity.getString(R.string.artik_client_id), null);

        // create JsonFactory
        final JsonFactory jsonFactory = new JacksonFactory();

        // setup credential store
        final SharedPreferencesCredentialStore credentialStore =
                new SharedPreferencesCredentialStore(activity, CREDENTIALS_STORE_PREF_FILE, jsonFactory);

        // setup authorization flow
        AuthorizationFlow flow = new AuthorizationFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                AndroidHttp.newCompatibleTransport(),
                jsonFactory,
                new GenericUrl(TOKEN_URL),
                client,
                client.getClientId(),
                AUTHORIZATION_URL)
                .setCredentialStore(credentialStore)
                .build();

        // setup authorization UI controller
        AuthorizationDialogController controller =
                new DialogFragmentController(activity.getFragmentManager(), true) {
                    @Override
                    public String getRedirectUri() throws IOException {
                        return REDIRECT_URL;
                    }

                    @Override
                    public boolean isJavascriptEnabledForWebView() {
                        return true;
                    }

                    @Override
                    public boolean disableWebViewCache() {
                        return false;
                    }

                    @Override
                    public boolean removePreviousCookie() {
                        return false;
                    }
                };
        return new ArtikOAuthManager(flow, controller);
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private ArtikOAuthManager(AuthorizationFlow flow, AuthorizationDialogController controller) {
        Preconditions.checkNotNull(flow);
        Preconditions.checkNotNull(controller);
        this.manager = new OAuthManager(flow, controller);
    }

// -------------------------- OTHER METHODS --------------------------

    public Observable<String> authorizeImplicitly() {
        return Observable.create(subscriber -> {
            try {
                final Credential credential = manager.authorizeImplicitly(USER_ID, null, null).getResult();
                if (credential == null || TextUtils.isEmpty(credential.getAccessToken())) {
                    subscriber.onError(new NullPointerException());
                } else {
                    subscriber.onNext(credential.getAccessToken());
                }
            } catch (IOException e) {
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        });
    }

    public OAuthManager.OAuthFuture<Boolean> deleteCredential() {
        return manager.deleteCredential(USER_ID, null, null);
    }
}
