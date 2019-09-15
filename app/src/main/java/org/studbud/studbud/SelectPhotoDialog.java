package org.studbud.studbud;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.Activity.RESULT_OK;


public class SelectPhotoDialog extends DialogFragment {

    private static final int PICK_FILE_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 4321;

    public interface OnPhotoSelectedListener {
        void getImagePath(Uri imagePath);
        void getImageBitmap(Bitmap bitmap);
    }
    OnPhotoSelectedListener onPhotoSelectedListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_photo,container,false);
        TextView selectPhoto = view.findViewById(R.id.dialogChoosePhoto);
        TextView takePhoto = view.findViewById(R.id.dialogOpenCamera);

        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
            }
        });

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });
        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            Intent intent = CropImage.activity(selectedImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .getIntent(getActivity());
            startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);


            //CropImage.activity(selectedImageUri).setGuidelines(CropImageView.Guidelines.ON)
             //       .setAspectRatio(1, 1).start(getActivity());

        }
        else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");
            onPhotoSelectedListener.getImageBitmap(bitmap);
            getDialog().dismiss();


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                onPhotoSelectedListener.getImagePath(resultUri);
                getDialog().dismiss();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getActivity(),"Error", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onAttach(Context context) {
        try {
            onPhotoSelectedListener = (OnPhotoSelectedListener) getActivity();
        }catch (ClassCastException e) {

        }
        super.onAttach(context);
    }

    public static SelectPhotoDialog newInstance(String title) {
        SelectPhotoDialog frag = new SelectPhotoDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }



}
