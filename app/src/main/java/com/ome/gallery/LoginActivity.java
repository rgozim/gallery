package com.ome.gallery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.ome.gallery.data.models.CSRFToken;
import com.ome.gallery.data.models.LoginModel;
import com.ome.gallery.web.CookieStore;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * A login screen that offers login via email/password to OMERO
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Change this to your OMERO.web URL
     */
    private static final String HOST_URL = "http://127.0.0.1:4060";

    private static final String TOKEN_URL = HOST_URL + "/api/v0/token/";

    private static final String LOGIN_URL = HOST_URL + "/api/v0/login/";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    // Http Client
    private OkHttpClient mHttp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mHttp = new OkHttpClient.Builder()
                .cookieJar(new CookieStore(getApplicationContext()))
                .addNetworkInterceptor(new HttpLoggingInterceptor())
                .build();

        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(view -> attemptLogin());

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            // Get a token object
            mAuthTask = new UserLoginTask(this, email, password);
            mAuthTask.execute();
        }
    }

    private Call getToken() {
        Request request = new Request.Builder()
                .get()
                .url(TOKEN_URL)
                .build();

        return mHttp.newCall(request);
    }

    private Call postLogin(String csrf) {
        if (TextUtils.isEmpty(csrf)) {
            throw new IllegalArgumentException("Missing CSRF token");
        }

        RequestBody formBody = new FormBody.Builder()
                .add("server", "1")
                .add("username", "root")
                .add("password", "omero")
                .add("csrfmiddlewaretoken", csrf)
                .build();

        Request request = new Request.Builder()
                .post(formBody)
                .url(LOGIN_URL)
                .build();

        return mHttp.newCall(request);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public static class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String TAG = UserLoginTask.class.getName();
        private final OkHttpClient mHttp;
        private final String mEmail;
        private final String mPassword;

        private final Moshi moshi = new Moshi.Builder().build();
        private final WeakReference<LoginActivity> mActivity;

        UserLoginTask(LoginActivity activity, String email, String password) {
            mActivity = new WeakReference<>(activity);
            mHttp = activity.mHttp;
            mEmail = email;
            mPassword = password;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Cors policy check
                Response response = getToken().execute();
                if (!response.isSuccessful()) {
                    throw new IOException(response.message());
                }

                CSRFToken token = moshi.adapter(CSRFToken.class)
                        .fromJson(response.body().source());

                // Call login service
                response = postLogin(token.data).execute();
                if (!response.isSuccessful()) {
                    throw new IOException(response.message());
                }

                LoginModel loginModel = moshi.adapter(LoginModel.class)
                        .fromJson(response.body().source());

                // Maybe store login model somewhere
                return true;
            } catch (IOException | NullPointerException e) {
                Log.e(TAG, "", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            LoginActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }

            if (success) {
                activity.finish();
            } else {
                activity.mPasswordView.setError(activity.getString(R.string.error_incorrect_password));
                activity.mPasswordView.requestFocus();
            }

            activity.showProgress(false);
            activity.mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            LoginActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }

            activity.showProgress(false);
            activity.mAuthTask = null;
        }

        private Call getToken() {
            Request request = new Request.Builder()
                    .get()
                    .url(TOKEN_URL)
                    .build();

            return mHttp.newCall(request);
        }

        private Call postLogin(String csrf) {
            if (TextUtils.isEmpty(csrf)) {
                throw new IllegalArgumentException("Missing CSRF token");
            }

            RequestBody formBody = new FormBody.Builder()
                    .add("server", "1")
                    .add("username", mEmail)
                    .add("password", mPassword)
                    .add("csrfmiddlewaretoken", csrf)
                    .build();

            Request request = new Request.Builder()
                    .post(formBody)
                    .url(LOGIN_URL)
                    .build();

            return mHttp.newCall(request);
        }
    }
}

