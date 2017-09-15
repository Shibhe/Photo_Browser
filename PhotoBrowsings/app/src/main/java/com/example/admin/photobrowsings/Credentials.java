package com.example.admin.photobrowsings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.EachExceptionsHandler;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class Credentials extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView, mConfir_pwd, ed_username;
    private View mProgressView;
    private View mLoginFormView;
    private TextView tv, tv_first, tv_last;
    String firstname, lastname, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mEmailView.setEnabled(false);
        //populateAutoComplete();

        tv = (TextView) findViewById(R.id.txt_msg);
        tv_first = (TextView) findViewById(R.id.txt_first);
        tv_last = (TextView) findViewById(R.id.txt_last);
        tv_last.setVisibility(View.INVISIBLE);
        tv_first.setVisibility(View.INVISIBLE);

        mPasswordView = (EditText) findViewById(R.id.password);
        mConfir_pwd = (EditText) findViewById(R.id.pwd_confirmation);
        ed_username = (EditText) findViewById(R.id.ed_username);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    // attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {

            public void onClick(View view) {
                if(enableStrongPass()){

                    HashMap<String ,String> data = new HashMap<String, String>();
                    data.put(Config.KEY_EMAIL_ADDRESS, mEmailView.getText().toString());
                    data.put(Config.KEY_PASSWORD, mPasswordView.getText().toString());
                    data.put(Config.KEY_USERNAME, ed_username.getText().toString());


                    PostResponseAsyncTask task = new PostResponseAsyncTask(Credentials.this, data, new AsyncResponse() {
                        @Override
                        public void processFinish(String s) {

                            if(s.contains("credentials successfully created")){
                                Toast.makeText(Credentials.this, "successful", Toast.LENGTH_SHORT).show();
                                confirm();
                            }else{
                                Toast.makeText(Credentials.this, "error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    task.execute(Config.URL_CREATE_CREDENTIALS);
                    task.setEachExceptionsHandler(new EachExceptionsHandler() {
                        @Override
                        public void handleIOException(IOException e) {
                            Toast.makeText(Credentials.this, "cannot connect to server", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleMalformedURLException(MalformedURLException e) {

                            Toast.makeText(Credentials.this, "url error", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleProtocolException(ProtocolException e) {

                            Toast.makeText(Credentials.this, "protocol error", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleUnsupportedEncodingException(UnsupportedEncodingException e) {

                            Toast.makeText(Credentials.this, "encoding error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                try{


                    SendMail sm = new SendMail(Credentials.this, mEmailView.getText().toString(), "Photo Browsing Account Confirmation", "Hello, " + lastname + " " + firstname +
                            "\n\nYour account has been created successfully.\nWe hope you enjoy the application.\n\nThe Photo Browsing Team.");

                    //Executing sendmail to send email
                    sm.execute();
                }catch(Exception e){
                    Toast.makeText(Credentials.this, "Something went wrong, your email address must be invalid or a non-GMail account", Toast.LENGTH_LONG).show();
                }

            }


        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream file = openFileInput(Config.TAG_FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(file)));
            String line = null;
            while((line = reader.readLine()) != null){
                sb.append(line).append("\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String detail = sb.toString();
        String[] element = detail.split("#");
        email = element[0];
        firstname = element[1];
        lastname = element[2];

        tv_first.setText(firstname);
        tv_last.setText(lastname);
        mEmailView.setText(email);

    }
    private void confirm(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Account created successfully");
        alertDialogBuilder.setMessage("Would you like to be logged in using your current details?");

        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        String detail_log = mEmailView.getText().toString() + "#" + tv_first.getText().toString() + "#" + tv_last.getText().toString();

                        try {
                            FileOutputStream fos = openFileOutput(Config.TAG_EMAIL_TXT, Context.MODE_PRIVATE);
                            fos.write(detail_log.getBytes());
                            fos.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(getApplicationContext(),Dashboards.class));

                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

//    private void populateAutoComplete() {
//        if (!mayRequestContacts()) {
//            return;
//        }
//
//        getLoaderManager().initLoader(0, null, this);
//    }

//    private boolean mayRequestContacts() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return true;
//        }
//        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        }
//        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
//            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok, new View.OnClickListener() {
//                        @Override
//                        @TargetApi(Build.VERSION_CODES.M)
//                        public void onClick(View v) {
//                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//                        }
//                    });
//        } else {
//            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//        }
//        return false;
//    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //populateAutoComplete();
            }
        }
    }

    public boolean enableStrongPass(){
        String password1 = mPasswordView.getText().toString();
        String password2 = mConfir_pwd.getText().toString();

        if(password1 == null || password2 == null){
            tv.setText("Passwords cannot be empty");
            return false;
        }

        if(password2.isEmpty() || password2.isEmpty()){
            tv.setText("Passwords cannot be empty");
            return false;
        }

        if(password1.length() < 8){
            tv.setText("Minimum of eight characters for password");
            return false;
        }

        if(password2.length() < 8){
            tv.setText("Minimum of eight characters for password");
            return false;
        }

        if(!password1.equals(password2)){
            tv.setText("Passwords must match");
            return false;
        }

        boolean containUpper = false, containLower = false, containWhite = false, containDigit = false;
        for(int x = 0; x < password1.length(); x++){
            if(Character.isLowerCase(password1.charAt(x)))
                containLower = true;
            else if(Character.isUpperCase(password1.charAt(x)))
                containUpper = true;
            else if(Character.isDigit(password1.charAt(x)))
                containDigit = true;
            else if(Character.isWhitespace(password1.charAt(x)))
                containWhite = true;
        }

        if(containWhite){
            tv.setText("Password must not contain white spaces");
            return false;
        }

        if(containDigit && containLower && containUpper){
            if(password1.contains(firstname)){
                tv.setText("Password: Must not contain your names");
                return false;
            }

            if(password1.contains(lastname)){
                tv.setText("Password: Must not contain your surname");
                return false;
            }
        }else{
            tv.setText("Password: Must contain uppercase, lowercase, a number and no white spaces");
            return false;
        }

        return true;
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(Credentials.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
