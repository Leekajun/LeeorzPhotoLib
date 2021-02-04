package com.lazyee.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.ComponentActivity;

import com.lazyee.photolib.entity.Photo;
import com.lazyee.photolib.callback.HandlePhotoCallback;
import com.lazyee.photolib.util.PhotoUtils;

import java.io.File;
import java.util.List;

public class MainActivity extends ComponentActivity implements HandlePhotoCallback {

    private PhotoUtils photoUtil;
    private ImageView ivImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化
        PhotoUtils.init(BuildConfig.APPLICATION_ID);
        photoUtil = new PhotoUtils(this);
        photoUtil.setCrop(true);
        photoUtil.setHandlePhotoCallback(this);

        ivImage = (ImageView) findViewById(R.id.ivImage);

        findViewById(R.id.btnOpenAlbum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoUtil.openAlbum(9);
            }
        });

        findViewById(R.id.btnOpenCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoUtil.openCamera();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        photoUtil.dealImage(requestCode,resultCode,data);
    }

    @Override
    public void onHandlePhotoComplete(List<Photo> photos) {
        ivImage.setImageURI(Uri.fromFile(new File(photos.get(0).getPath())));
    }
}
