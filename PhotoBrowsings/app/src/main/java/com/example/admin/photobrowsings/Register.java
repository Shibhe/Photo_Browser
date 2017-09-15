package com.example.admin.photobrowsings;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.EachExceptionsHandler;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/**
 * A login screen that offers login via email/password.
 */
public class Register extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;



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
    private View mProgressView;
    private View mLoginFormView;
    private ImageView image_gallery, image_camera, image;
    private EditText ed_firstname, ed_lastname, ed_id, ed_dob, ed_gender;
    protected String year = "", month = "", day = "", gender = "";
    CameraPhotos cameraPhotos;
    GalleryPhoto galleryPhoto;
    final int CAMERA_REQUEST = 13323;
    final int GALLERY_REQUEST = 32324;
    String selectedPhoto;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.

        image_gallery = (ImageView) findViewById(R.id.image_gallery);
        image_camera = (ImageView) findViewById(R.id.image_camera);
        image = (ImageView) findViewById(R.id.image);

        cameraPhotos = new CameraPhotos(this);
        galleryPhoto = new GalleryPhoto(this);




        image_camera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(cameraPhotos.takePhotoIntent(), CAMERA_REQUEST);
                    cameraPhotos.addToGallery();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "something went wrong while taking the picture", Toast.LENGTH_SHORT).show();

                }
            }
        });

        image_gallery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
            }
        });


        ed_firstname  = (EditText) findViewById(R.id.ed_firstname);
        ed_lastname = (EditText) findViewById(R.id.e_lastname);
        ed_id = (EditText) findViewById(R.id.ed_id);
        ed_dob = (EditText) findViewById(R.id.ed_dob);
        ed_gender = (EditText) findViewById(R.id.ed_gender);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        //populateAutoComplete();
        mayRequestContacts();

        ed_gender.setFocusable(false);
        ed_dob.setFocusable(false);
        ed_dob.setEnabled(false);
        ed_gender.setEnabled(false);

        ed_id.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keys = ed_id.getText().toString();

                if(!validateID(keys)){
                    ed_dob.setText("");
                    ed_gender.setText("");
                    return;
                }


                if(keys.length() > 1){
                    int year = Integer.parseInt(keys.substring(0, 2));

                    if(year > 16){
                        Register.this.year = "19" + year;
                    }else{
                        Register.this.year = "200" + year;
                    }

                    displayDOB();
                }

                if(keys.length() > 3){
                    int mon = Integer.parseInt(keys.substring(2, 4));

                    if(mon > 12 || mon <= 0){
                        Toast.makeText(Register.this, "Invalid month of birth provided", Toast.LENGTH_LONG).show();
                        Register.this.month = "";
                    }else{
                        filterMonth(keys);
                    }
                    displayDOB();
                }

                if(keys.length() > 5){
                    int day = Integer.parseInt(keys.substring(4, 6));

                    if(day > 31 || day <= 0){
                        Register.this.day = "";
                        Toast.makeText(Register.this, "Invalid day of birth provided", Toast.LENGTH_LONG).show();
                    }else{
                        Register.this.day = String.valueOf(day);
                    }

                    displayDOB();
                }

                if(keys.length() > 6){
                    int gender = Integer.parseInt(keys.substring(6, 7));

                    if(gender >= 5)
                        Register.this.gender = "Male";
                    else
                        Register.this.gender = "Female";

                    displayGender();
                }
            }

            private void displayDOB(){
                String disp = Register.this.day + " " + Register.this.month + " " + Register.this.year;
                ed_dob.setText(disp);
            }

            private void displayGender(){
                ed_gender.setText(Register.this.gender);
            }

            public boolean validateID(String keys){
                boolean valid = true;

                for(int x = 0; x < keys.length(); x++){
                    if(!Character.isDigit(keys.charAt(x))){
                        valid = false;
                        break;
                    }
                }

                return valid;
            }

            private void filterMonth(String keys){
                int month = Integer.parseInt(keys.substring(2, 4));

                if(month == 1)
                    Register.this.month = "January";
                else if(month == 2)
                    Register.this.month = "February";
                else if(month == 3)
                    Register.this.month = "March";
                else if(month == 4)
                    Register.this.month = "April";
                else if(month == 5)
                    Register.this.month = "May";
                else if(month == 6)
                    Register.this.month = "June";
                else if(month == 7)
                    Register.this.month = "July";
                else if(month == 8)
                    Register.this.month = "August";
                else if(month == 9)
                    Register.this.month = "September";
                else if(month == 10)
                    Register.this.month = "October";
                else if(month == 11)
                    Register.this.month = "November";
                else if(month == 12)
                    Register.this.month = "December";
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });


        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {

            public void onClick(View view) {
                if(attemptLogin()){

                }

                else {
                    try {
                        Bitmap bitmap = ImageLoader.init().from(selectedPhoto).requestSize(512, 512).getBitmap();
                        String encodemage = ImageBase64.encode(bitmap);
                        Log.e("", encodemage);

                        HashMap<String ,String> data = new HashMap<String, String>();
                        data.put(Config.KEY_EMAIL_ADDRESS, mEmailView.getText().toString());
                        data.put(Config.KEY_FIRSTNAME, ed_firstname.getText().toString());
                        data.put(Config.KEY_LASTNAME, ed_lastname.getText().toString());
                        data.put(Config.KEY_USER_ID, ed_id.getText().toString());
                        data.put(Config.KEY_DOB, ed_dob.getText().toString());
                        data.put(Config.KEY_GENDER, gender);
                        data.put(Config.KEY_IMAGE, encodemage);


                        PostResponseAsyncTask task = new PostResponseAsyncTask(Register.this, data, new AsyncResponse() {
                            @Override
                            public void processFinish(String s) {

                                if(s.contains("insert successful")){
                                    Toast.makeText(Register.this, " registration successful", Toast.LENGTH_SHORT).show();

                                    String detail = mEmailView.getText().toString() + "#" + ed_firstname.getText().toString() + "#" + ed_lastname.getText().toString() + "#" + ed_id.getText().toString() + "#";

                                    try {
                                        FileOutputStream fos = openFileOutput(Config.TAG_FILENAME, Context.MODE_PRIVATE);
                                        fos.write(detail.getBytes());
                                        fos.close();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    startActivity(new Intent(Register.this, Credentials.class));
                                }else{
                                    Toast.makeText(Register.this, "error while registering", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        task.execute(Config.URL_CREATE_ACCOUNT);
                        task.setEachExceptionsHandler(new EachExceptionsHandler() {
                            @Override
                            public void handleIOException(IOException e) {
                                Toast.makeText(Register.this, "cannot connect to server", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void handleMalformedURLException(MalformedURLException e) {

                                Toast.makeText(Register.this, "url error", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void handleProtocolException(ProtocolException e) {

                                Toast.makeText(Register.this, "protocol error", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void handleUnsupportedEncodingException(UnsupportedEncodingException e) {

                                Toast.makeText(Register.this, "encoding error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (FileNotFoundException e) {
                        Toast.makeText(Register.this, "something went wrong while encoding photo", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == CAMERA_REQUEST){
                String phto = cameraPhotos.getPhotoPath();
                selectedPhoto = phto;
                try {
                    Bitmap bitmap = ImageLoader.init().from(phto).requestSize(512, 512).getBitmap();
                    image.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else if(requestCode ==  GALLERY_REQUEST){
                Uri uri = data.getData();
                galleryPhoto.setPhotoUri(uri);
                String phto = galleryPhoto.getPath();
                selectedPhoto = phto;
                try {
                    Bitmap bitmap = ImageLoader.init().from(phto).requestSize(512, 512).getBitmap();
                    image.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "something went wrong while loading the picture", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

//    private void populateAutoComplete() {
//        if (!mayRequestContacts()) {
//            return;
//        }
//
//        getLoaderManager().initLoader(0, null, this);
//    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //populateAutoComplete();
                Toast.makeText(Register.this, "access granted", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private boolean attemptLogin() {

        if (mAuthTask != null) {

            return true;
        }

        // Reset errors.
        mEmailView.setError(null);
        ed_lastname.setError(null);
        ed_firstname.setError(null);
        ed_id.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String firstname = ed_firstname.getText().toString();
        String lastname = ed_lastname.getText().toString();
        String id = ed_id.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mPasswordView;
//            cancel = true;
//
//        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;

        }else if (TextUtils.isEmpty(firstname)) {
            ed_firstname.setError("enter firstname");
            focusView = ed_firstname;
            cancel = true;
        } else if (TextUtils.isEmpty(lastname)) {
            ed_lastname.setError("enter lastname");
            focusView = ed_lastname;
            cancel = true;
        }
        else if (TextUtils.isEmpty(id)) {
            ed_id.setError("enter correct id number");
            focusView = ed_id;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);

        }
        return cancel;
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
                new ArrayAdapter<>(Register.this,
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
            //showProgress(false);

            if (success) {
                finish();
            } else {
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
//                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //showProgress(false);
        }
    }
}

