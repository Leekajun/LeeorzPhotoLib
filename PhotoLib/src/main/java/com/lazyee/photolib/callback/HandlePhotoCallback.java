package com.lazyee.photolib.callback;

import com.lazyee.photolib.entity.Photo;

import java.util.List;

/**
 * Created by 嘉俊 on 2015/10/9.
 */
public interface HandlePhotoCallback {
    void onHandlePhotoComplete(List<Photo> photos);
}
