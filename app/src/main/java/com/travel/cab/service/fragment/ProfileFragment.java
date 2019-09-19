package com.travel.cab.service.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.travel.cab.service.R;
import com.travel.cab.service.broadcast.InternetBroadcastReceiver;
import com.travel.cab.service.database.MyAppDatabase;
import com.travel.cab.service.database.User;
import com.travel.cab.service.interfaces.CheckPosition;
import com.travel.cab.service.modal.UserProfileDetail;
import com.travel.cab.service.ui.IntentFilterCondition;
import com.travel.cab.service.utils.preference.SharedPreference;
import com.travel.cab.service.utils.validation.CustomCheck;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
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
    private String userName, userMobile, userEmail, userAddress, userCompany;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private CircleImageView imageView;
    private Uri imageUri;
    private Context context;
    private Toolbar toolbar;
    private String selectedImagePath;
    private FragmentManager fragmentManager;
    private InternetBroadcastReceiver internetBroadcastReceiver;
    private IntentFilter intentFilter;
    private ProgressBar progressBar;
    private MyAppDatabase myAppDatabase;


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
        getActivity().setTitle("Edit Profile");
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setHasOptionsMenu(true);
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("users").child(SharedPreference.getInstance().getUserId());
        mStorageRef = FirebaseStorage.getInstance().getReference().child("profile_images").
                child(SharedPreference.getInstance().getUserId());
        etName = view.findViewById(R.id.et_name);
        etPhone = view.findViewById(R.id.et_mobile_number);
        etEmail = view.findViewById(R.id.et_email);
        etAddress = view.findViewById(R.id.et_address);
        etCompany = view.findViewById(R.id.et_company_name);
        btnSaveRecord = view.findViewById(R.id.btn_save_record);
        imageView = view.findViewById(R.id.img_user);
        progressBar = view.findViewById(R.id.show_progress);
        // toolbar = view.findViewById(R.id.toolbar);
        fragmentManager = getChildFragmentManager();
        imageView.setOnClickListener(this);
        internetBroadcastReceiver = new InternetBroadcastReceiver();

        myAppDatabase = Room.databaseBuilder(getActivity(), MyAppDatabase.class, "userdb").allowMainThreadQueries().build();

//         LinearLayout lyt_progress = view.findViewById(R.id.lyt_progress);
//        lyt_progress.setVisibility(View.VISIBLE);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        ((HomeActivity)getActivity()).setSupportActionBar(toolbar);
        btnSaveRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                getUserData();
                uploadUserImage();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        intentFilter = IntentFilterCondition.getInstance().callIntentFilter();
        getActivity().registerReceiver(internetBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(internetBroadcastReceiver);
    }

    private void uploadUserImage() {
        if (CustomCheck.getInstance().checkNormalStringCase(userName)) {
            if (CustomCheck.getInstance().checkPhoneNumber(userMobile)) {
                if (CustomCheck.getInstance().checkNormalStringCase(userEmail)) {
                    if (CustomCheck.getInstance().checkNormalStringCase(userAddress)) {
                        if (CustomCheck.getInstance().checkNormalStringCase(userCompany)) {
                            if (CustomCheck.getInstance().checkNormalStringCase(String.valueOf(imageUri))) {
                                if(InternetBroadcastReceiver.isNetworkInterfaceAvailable(getContext()))
                                {
                                    StorageReference user_profile = mStorageRef.child(userName + ".jpg");
                                    user_profile.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            user_profile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    if (uri != null) {
                                                        Toast.makeText(context, "Image Uploaded", Toast.LENGTH_SHORT).show();
                                                        uploadUserDetail(uri);
                                                    } else {
                                                        progressBar.setVisibility(View.GONE);
                                                    }

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });

                                }
                                else
                                {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(context, R.string.offline, Toast.LENGTH_SHORT).show();
                                }
                                                           } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(context, "Select Profile image", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, "Enter valid Company name", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, "Enter valid Address ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "Enter valid Email ", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Enter valid Mobile number", Toast.LENGTH_SHORT).show();
            }
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(context, "Enter a valid name", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadUserDetail(Uri uri) {
        String downloadedUrl = uri.toString();//actual this is uri
        Map<String, String> userProfileMap = new HashMap<>();
        userProfileMap.put("name", userName);
        userProfileMap.put("email", userEmail);
        userProfileMap.put("mobile", userMobile);
        userProfileMap.put("adres", userAddress);
        userProfileMap.put("cempany", userCompany);
        userProfileMap.put("image_Url", downloadedUrl);
        // store this values also in database
        User user = new User();
        user.setUserName(userName);
        user.setUserEmail(userEmail);
        user.setMobileNumber(userMobile);
        user.setUserAddress(userAddress);
        user.setUserCompany(userCompany);
        user.setUserImage(downloadedUrl);
        myAppDatabase.myDao().addUser(user);
        //store value in fireBase
        mDatabaseReference.push().setValue(userProfileMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Data Save Successfully", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onSuccess: ");
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
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
            Intent intent = CropImage.activity()
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
                if (imageUri != null) {
                    imageView.setImageURI(imageUri);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        } else {
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
/*    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
         super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.edit_profile:
                setUpVIewFragment();
                return true;
            case R.id.logout:
                mAuth.signOut();
                Intent loginIntent = new Intent(getActivity(),PhoneLogin.class);
                startActivity(loginIntent);
                clearPreference();
                getActivity().finish();
                return true;

            default:
                Toast.makeText(getContext(), "Do Right Selection", Toast.LENGTH_SHORT).show();
                return true;
        }
    }*/

    private void setUpVIewFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fl_profile_fragment, new ProfileFragment()).commit();

    }

    private void clearPreference() {
        SharedPreference.getInstance().clearPreference();
    }
}
