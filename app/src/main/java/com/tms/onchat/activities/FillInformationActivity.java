package com.tms.onchat.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tms.onchat.R;
import com.tms.onchat.databinding.ActivityFillInformationBinding;
import com.tms.onchat.utilities.Constants;
import com.tms.onchat.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;

public class FillInformationActivity extends AppCompatActivity {

    private ActivityFillInformationBinding binding;

    private String userUID, userEmail, firstName, lastName, dateBirthday, sex, image;
    private Uri uriImage;

    private Dialog dialogWait;

    private PreferenceManager preferenceManager;

    private final ActivityResultLauncher<Intent> addImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if(result.getResultCode() == RESULT_OK) {
                            if(result.getData() != null) {
                                uriImage = result.getData().getData();
                                try {
                                    InputStream inputStream = getContentResolver().openInputStream(uriImage);
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    binding.imgImage.setImageBitmap(bitmap);
                                    binding.txtAddImage.setVisibility(View.INVISIBLE);

                                    image = encodeImage(bitmap);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFillInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        Intent intent = getIntent();
        if(intent == null) {
            return;
        }
        userUID = intent.getStringExtra(Constants.INTENT_USER_ID);
        userEmail = intent.getStringExtra(Constants.INTENT_USER_EMAIL);

        setListeners();
    }

    private void setListeners() {
        binding.btnConfirm.setOnClickListener(v -> {
            if(checkValidDetails()) {
                uploadInformation();
            }
        });

        binding.imgImage.setOnClickListener(v -> checkPermissionToAddImage());

        binding.edtInputBirthday.setOnClickListener(v -> showDateBirthdayDialog());

        binding.txtBack.setOnClickListener(v -> {
            onBackPressed();
            finish();
        });
    }

    /** TODO: Upload information to firestore */
    private void uploadInformation() {
        showDialogWait(getString(R.string.toast_wait_upload_info));

        /** Data to upload firestore */
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_USER_FIRST_NAME, firstName);
        user.put(Constants.KEY_USER_LAST_NAME, lastName);
        user.put(Constants.KEY_USER_BIRTHDAY, dateBirthday);
        user.put(Constants.KEY_USER_SEX, sex);
        user.put(Constants.KEY_USER_IMAGE, image);
        user.put(Constants.KEY_USER_UID, userUID);
        user.put(Constants.KEY_USER_EMAIL, userEmail);

        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    dialogWait.dismiss();

                    /** Data to upload SharedPreference */
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGN_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_FIRST_NAME, firstName);
                    preferenceManager.putString(Constants.KEY_USER_LAST_NAME, lastName);
                    preferenceManager.putString(Constants.KEY_USER_BIRTHDAY, dateBirthday);
                    preferenceManager.putString(Constants.KEY_USER_SEX, sex);
                    preferenceManager.putString(Constants.KEY_USER_IMAGE, image);
                    preferenceManager.putString(Constants.KEY_USER_UID, userUID);
                    preferenceManager.putString(Constants.KEY_USER_EMAIL, userEmail);
                    preferenceManager.putString(Constants.KEY_USER_DOCUMENT_ID, documentReference.getId());

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    dialogWait.dismiss();
                    showToast(getString(R.string.toast_upload_info_failured));
                });
    }

    /** TODO: Show dialog wait to upload info not allow to user click outsize */
    private void showDialogWait(String mes) {
        if(dialogWait == null) {
            dialogWait = new Dialog(this);
            dialogWait.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogWait.setContentView(R.layout.layout_wait);

            Window window = dialogWait.getWindow();
            if(window == null) {
                return;
            }

            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);
            dialogWait.setCancelable(false);
        }

        TextView txtWait = dialogWait.findViewById(R.id.txtWait);
        txtWait.setText(mes);

        dialogWait.show();
    }

    /** TODO: Encode image to base64 and return base64.toString */
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /** TODO: Show choose date birthday */
    private void showDateBirthdayDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener onDateSetListener = (view, year1, month1, dayOfMonth) -> {
            month1 = month1 + 1;
            String birthday = String.format("%02d/%02d/%04d", month1, dayOfMonth, year1);
            if(year1 < year - 16) {
                binding.edtInputBirthday.setText(birthday);
                binding.edtInputBirthday.setError(null);
            } else {
                showToast(getString(R.string.toast_birthday_invalid));
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                FillInformationActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                onDateSetListener, year, month, day);

        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.show();
    }

    /** TODO: Choose avatar from external storage */
    private void addImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            addImageLauncher.launch(intent);
        }
    }

    /** TODO: Check details inputed is valid to upload information */
    private boolean checkValidDetails() {
        firstName = binding.edtInputFirstName.getText().toString().trim();
        lastName = binding.edtInputLastName.getText().toString().trim();
        dateBirthday = binding.edtInputBirthday.getText().toString().trim();

        if(image.isEmpty() || image.equals("")) {
            showToast(getString(R.string.toast_image_invalid));
            return false;
        }

        if(firstName.isEmpty() || firstName.equals("")) {
            showToast(getString(R.string.toast_first_name_invalid));
            return false;
        }

        if(lastName.isEmpty() || lastName.equals("")) {
            showToast(getString(R.string.toast_last_name_invalid));
            return false;
        }

        if(dateBirthday.isEmpty() || dateBirthday.equals("")) {
            showToast(getString(R.string.toast_birthday_invalid));
            return false;
        }

        if(binding.rdMale.isChecked()) {
            sex = getString(R.string.title_male);
        } else if(binding.rdFemale.isChecked()) {
            sex = getString(R.string.title_female);
        } else {
            showToast(getString(R.string.toast_sex_invalid));
            return false;
        }

        return true;
    }

    private void showToast(String mes) {
        Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_SHORT).show();
    }

    /** TODO: Check accept or cancle permission READ_EXTERNAL_STORAGE */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constants.REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addImage();
            } else {
                showToast(getString(R.string.toast_permission_denied));
            }
        }
    }

    /** TODO: Check asked permission READ_EXTERNAL_STORAGE */
    private void checkPermissionToAddImage() {
        if(ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    FillInformationActivity.this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.REQUEST_CODE_STORAGE_PERMISSION
            );
        } else {
            addImage();
        }
    }

    /** TODO: Back to sign in or up activity and sign out */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FirebaseAuth.getInstance().signOut();
    }
}