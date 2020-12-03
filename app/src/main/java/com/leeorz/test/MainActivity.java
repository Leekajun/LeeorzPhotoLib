package com.leeorz.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.leeorz.lib.base.BaseActivity;
import com.leeorz.photolib.util.photo.OnDealImageListener;
import com.leeorz.photolib.util.photo.Photo;
import com.leeorz.photolib.util.photo.PhotoUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MainActivity extends BaseActivity implements OnDealImageListener {

    private PhotoUtils photoUtil;
    private ImageView ivImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化
        PhotoUtils.init(BuildConfig.APPLICATION_ID);
        photoUtil = new PhotoUtils(getActivity());
        photoUtil.setCrop(true);
        photoUtil.setOnDealImageListener(this);

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
        System.out.println("requestCode:" + requestCode + ";resultCode:" + resultCode + ";data:" + data);
        photoUtil.dealImage(requestCode,resultCode,data);
    }

    @Override
    public void onDealSingleImageComplete(Photo photo) {

        Log.e("----->",photo.getUrl());
        Picasso.with(getActivity())
                .load("file://" + photo.getUrl())
                .into(ivImage);
    }

    @Override
    public void onDealMultiImageComplete(List<Photo> list) {
    }
}
