package com.travel.cab.service.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.travel.cab.service.MainActivity;
import com.travel.cab.service.R;
import com.travel.cab.service.activity.HomeActivity;
import com.travel.cab.service.utils.preference.SharedPreference;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final int PICK_IMG = 1;
    private static final String TAG = "ProfileFragment";
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private AppCompatEditText etName, etPhone, etEmail, etAddress, etCompany;
    private Button btnSaveRecord;
    private FirebaseDatabase mDatabase;
    private String userName, userMobile, userEmail, userAddress, userCompany;
    private DatabaseReference mDatabaseReference;
    private CircleImageView imageView;
    private Uri imageUri;
    private Context context;
    private Toolbar toolbar;
    private String selectedImagePath;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setHasOptionsMenu(true);
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child(SharedPreference.getInstance().getUserId());
         mStorageRef = FirebaseStorage.getInstance().getReference().child("profile_images").
                 child(SharedPreference.getInstance().getUserId());
        etName = view.findViewById(R.id.et_name);
        etPhone = view.findViewById(R.id.et_mobile_number);
        etEmail = view.findViewById(R.id.et_email);
        etAddress = view.findViewById(R.id.et_address);
        etCompany = view.findViewById(R.id.et_company_name);
        btnSaveRecord = view.findViewById(R.id.btn_save_record);
        imageView = view.findViewById(R.id.img_user);
        toolbar = view.findViewById(R.id.toolbar);
        imageView.setOnClickListener(this);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((HomeActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("Profile");
        btnSaveRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserData();
                uploadUserImage();
            }
        });
    }

    private void uploadUserImage() {
        StorageReference user_profile = mStorageRef.child(userName + ".jpg");
        user_profile.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                user_profile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(context, "Image Uploaded", Toast.LENGTH_SHORT).show();
                        uploadUserDetail(uri);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });
    }

    private void uploadUserDetail(Uri uri) {
        String downloadedUrl = uri.toString();//actual this is uri
        Map<String, Object> userProfileMap = new HashMap<>();
        userProfileMap.put("Name", userName);
        userProfileMap.put("Email", userEmail);
        userProfileMap.put("Mobile", userMobile);
        userProfileMap.put("Address", userAddress);
        userProfileMap.put("Company", userCompany);
        userProfileMap.put("Image_Url", downloadedUrl);
        mDatabaseReference.setValue(userProfileMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "onSuccess: ");
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onSuccess: " + e);
            }
        });
        Log.i(TAG, "onSuccess: uri= " + uri.toString());
    }

    private void getUserData() {
        userName = etName.getText().toString();
        userMobile = etPhone.getText().toString();
        userEmail = etEmail.getText().toString();
        userAddress = etAddress.getText().toString();
        userCompany = etCompany.getText().toString();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.img_user) {
            // start picker to get image for cropping and then use the image in cropping activity
          Intent intent=  CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .getIntent(getActivity());
            startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
           /* Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMG);*/
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                imageUri = result.getUri();
                if(imageUri !=null)
                {
                    imageView.setImageURI(imageUri);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        else
        {
            Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
        }
   /*     if (resultCode == Activity.RESULT_OK)
            if (requestCode == PICK_IMG) {
                if (data.getData() != null) {
                    imageUri = data.getData();
                    imageView.setImageURI(imageUri);
                    setImageFromGallery(imageUri);
                }
            }*/
    }

    private void setImageFromGallery(Uri imgUri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(imgUri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        selectedImagePath = cursor.getString(columnIndex);
        cursor.close();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
         super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.edit_profile:
                return true;
            case R.id.logout:
                return true;

            default:
                Toast.makeText(getContext(), "Do Right Selection", Toast.LENGTH_SHORT).show();
                return true;
        }
    }
}
