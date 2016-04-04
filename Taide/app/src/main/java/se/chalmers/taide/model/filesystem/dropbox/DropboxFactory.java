package se.chalmers.taide.model.filesystem.dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import se.chalmers.taide.R;

/**
 * Created by Matz on 2016-03-22.
 */
public class DropboxFactory {

    private static final String APP_KEY = "o4m52bd67whe1js";
    private static final String APP_SECRET = "g63sygp21nqm5v7";

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    private static DropboxAPI<AndroidAuthSession> api;

    public static void initDropboxIntegration(Context context){
        AndroidAuthSession session = buildSession(context);
        api = new DropboxAPI<>(session);
        if(!api.getSession().isLinked()) {
            api.getSession().startOAuth2Authentication(context);
        }
    }

    public static boolean isAuthenticated(){
        return api != null && api.getSession().isLinked();
    }

    public static void authenticationDone(Context context){
        AndroidAuthSession session = api.getSession();
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();
                // Store it locally
                storeAuth(context, session);
            } catch (IllegalStateException e) {
                Toast.makeText(context, context.getResources().getString(R.string.dropbox_error_auth), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static DropboxAPI<?> getAPI(){
        return api;
    }


    private static AndroidAuthSession buildSession(Context context) {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(context, session);
        return session;
    }


    private static void loadAuth(Context context, AndroidAuthSession session) {
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    private static void storeAuth(Context context, AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        // Store the OAuth 1 access token, if there is one.  This is only necessary if
        // you're still using OAuth 1.
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
            return;
        }
    }
}
