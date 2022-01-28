package com.btp.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    RecyclerView rvImages;
    GalleryAdapter galleryAdapter;
    List<String> images;
    TextView tvGalleryNumber;

    private static final int MY_READ_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        tvGalleryNumber = findViewById(R.id.tvGalleryNumber);
        rvImages = findViewById(R.id.rvImages);

        if(ContextCompat.checkSelfPermission(GalleryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GalleryActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_PERMISSION);
        } else {
            loadImages();
        }

    }

    private void loadImages() {

        ProgressDialog progressDialog = new ProgressDialog(GalleryActivity.this);
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Handler handler = new Handler();
        final Runnable uiRunnable = () -> {
            progressDialog.dismiss();
            rvImages.setAdapter(galleryAdapter);
            tvGalleryNumber.setText("Documents (" + images.size() + ")");
            Toast.makeText(GalleryActivity.this,MainActivity.abc,Toast.LENGTH_LONG).show();
        };

        Thread thread = new Thread(() -> {
            rvImages.setHasFixedSize(true);
            rvImages.setLayoutManager(new GridLayoutManager(GalleryActivity.this,3));
            images = new ArrayList<>();
            images = ImagesGallery.listOfImages(GalleryActivity.this);
            galleryAdapter = new GalleryAdapter(GalleryActivity.this, images, path -> {

            });
            handler.post(uiRunnable);

        });

        thread.start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_READ_PERMISSION) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(GalleryActivity.this,"Permission Granted",Toast.LENGTH_SHORT).show();
                loadImages();
            } else {
                Toast.makeText(GalleryActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private String extractText(Bitmap bitmap) throws Exception{
//        TessBaseAPI tessBaseApi = new TessBaseAPI();
//        tessBaseApi.init(DATA_PATH, "eng");
//        tessBaseApi.setImage(bitmap);
//        String extractedText = tessBaseApi.getUTF8Text();
//        tessBaseApi.end();
//        return extractedText;
//    }
}