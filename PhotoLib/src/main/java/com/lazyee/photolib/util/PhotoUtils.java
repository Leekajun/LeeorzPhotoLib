package com.lazyee.photolib.util;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.lazyee.photolib.callback.HandlePhotoCallback;
import com.lazyee.photolib.entity.Photo;
import com.lazyee.photolib.crop.Crop;
import com.lazyee.photolib.photopicker.PhotoPickerActivity;
import com.lazyee.photolib.photopicker.util.PhotoPickerIntent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片工具
 * Created by leeorz on 2015/10/9.
 */
public class PhotoUtils {
    private static String FILE_PROVIDER = "";
    public static final int CAMERA = 10001;
    public static final int ALBUM = 10002;
    private boolean isCrop = false;
    private boolean isFreeCrop = false;
    private Activity mActivity;
    private String imagePath = "";
    private String outputImagePath = "";

    private int aspectX = 1;
    private int aspectY = 1;

    private int maxWidth = 500;
    private int maxHeight = 500;
    private HandlePhotoCallback handlePhotoCallback;

    public static void init(String applicationId){
        FILE_PROVIDER = applicationId + ".fileprovider";
    }

    public PhotoUtils(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * 是否是处理图片
     * @param requestCode
     * @return
     */
    public static boolean isHandleImage(int requestCode){
        return requestCode == ALBUM
                || requestCode == CAMERA
                || requestCode == Crop.REQUEST_CROP
                || requestCode == Crop.REQUEST_PICK;
    }

    public PhotoUtils setCrop(boolean isCrop) {
        this.isCrop = isCrop;
        return this;
    }

    public PhotoUtils setFreeCrop(boolean freeCrop) {
        this.isFreeCrop = freeCrop;
        this.isCrop = freeCrop;
        return this;
    }

    public PhotoUtils setMaxSize(int maxWidth, int maxHeight){
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        return this;
    }

    public PhotoUtils setCropAspect(int aspectX, int aspectY) {
        this.aspectX = aspectX;
        this.aspectY = aspectY;
        return this;
    }

    /**
     * 打开摄像头
     */
    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 下面这句指定调用相机拍照后的照片存储的路径
        Uri fileUri = getUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, "portrait");

        List<ResolveInfo> resInfoList = mActivity.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            mActivity.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mActivity.startActivityForResult(intent, CAMERA);
    }


    public void setHandlePhotoCallback(HandlePhotoCallback onHandlePhotoListener) {
        this.handlePhotoCallback = onHandlePhotoListener;
    }

    /**
     * 打开相册
     *
     * @param requestCount 需要选择的图片数量
     */
    public void openAlbum(int requestCount) {
        PhotoPickerIntent intent = new PhotoPickerIntent(mActivity);
        intent.setPhotoCount(requestCount);
        intent.setShowCamera(false);
        mActivity.startActivityForResult(intent, ALBUM);
    }

    /**
     * 处理图片
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void dealImage(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == CAMERA) {
            imagePath = BitmapUtils.ratingImageAndSave(getRealFilePath(imagePath));
            if (isCrop) {
                beginCrop(imagePath);
            } else {
                if (handlePhotoCallback != null) {
                    handlePhotoCallback.onHandlePhotoComplete(createPhotoList(getRealFilePath(imagePath), getImageWidthAndHeight(imagePath)));
                }
            }
        } else if (requestCode == ALBUM) {
            getPickPhoto(data);
        } else if (requestCode == Crop.REQUEST_PICK && resultCode == mActivity.RESULT_OK) {
            beginCrop(data.getData().getPath());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }

    private void getPickPhoto(Intent data) {
        if (data == null) return;
        ArrayList<String> photoPathList = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
        if (photoPathList.size() == 1 && isCrop) {
            beginCrop(photoPathList.get(0));
        } else {

            if(handlePhotoCallback != null){
                List<Photo> photos = new ArrayList();
                for (int i = 0; i < photoPathList.size(); i++) {
                    photos.add(createPhoto(photoPathList.get(i), getImageWidthAndHeight(photoPathList.get(i))));
                }
                handlePhotoCallback.onHandlePhotoComplete(photos);
            }
        }
    }

    private void beginCrop(String path) {

        File file = new File(getRealFilePath(path));
        Uri source = null;
//        Log.e("Build.VERSION.SDK_INT","Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            source =  FileProvider.getUriForFile(mActivity,FILE_PROVIDER,file);
        } else {
//            24版本以下的直接获取Uri即可
            source = Uri.fromFile(file);
        }

        File outputPath = new File(mActivity.getFilesDir(), "images/");
        if(!outputPath.exists()){
            outputPath.mkdirs();
        }
        File output = new File(mActivity.getFilesDir(), "images/" + System.currentTimeMillis()+ ".jpg");
        Log.e("destination","destination:" + output.getPath());
        try {
            output.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputImagePath = output.getAbsolutePath();
        Uri destination = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            destination = FileProvider.getUriForFile(mActivity,FILE_PROVIDER,output);

        }else{
            destination = Uri.fromFile(output);
        }

        Crop crop = Crop.of(source, destination);
        crop.withMaxSize(maxWidth, maxHeight);
        if(!isFreeCrop){
            crop.withAspect(aspectX,aspectY);
        }
        crop.start(mActivity);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK && result != null) {
            if (handlePhotoCallback != null) {
                Crop.getOutput(result).getPath();
                handlePhotoCallback.onHandlePhotoComplete(createPhotoList(outputImagePath, getImageWidthAndHeight(outputImagePath)));
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(mActivity, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取图片的实体
     * @param path
     * @param size int array 0:w ,h:1
     * @return
     */
    private Photo createPhoto(String path, int[] size) {
        Photo photo = new Photo();
        photo.setWidth(size[0]);
        photo.setHeight(size[1]);
        photo.setPath(path);

        return photo;
    }

    /**
     * 获取图片的实体集合
     * @param path
     * @param size int array 0:w ,h:1
     * @return
     */
    private List<Photo> createPhotoList(String path, int[] size) {
        List<Photo> photos = new ArrayList();
        photos.add(createPhoto(path,size));
        return photos;
    }

    private Uri getUri() {
        String path = "images/default.jpg";
        File dir = new File(mActivity.getFilesDir().getAbsoluteFile() + File.separator + "images");
        if (!dir.exists()) { // 创建目录
            dir.mkdirs();
        }
        File file = new File(mActivity.getFilesDir(), path);
        outputImagePath = file.getAbsolutePath();
        Uri uri = FileProvider.getUriForFile(mActivity, FILE_PROVIDER, file);
        imagePath = path;
        return uri;
    }

    /**
     * 获取目标图片的宽高
     *
     * @return
     */
    private int[] getImageWidthAndHeight(String imagePath) {

        imagePath = getRealFilePath(imagePath);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, opts);

        boolean rotate = BitmapUtils.isPictureRotate(imagePath);
        int width = rotate ? opts.outHeight : opts.outWidth;
        int height = rotate ? opts.outWidth : opts.outHeight;

        return new int[]{width, height};
    }

    /**
     * 获取真实路径
     * @param path
     * @return
     */
    private String getRealFilePath(String path){

        File file = new File(path);
        if(!file.exists()){
            file = new File(mActivity.getFilesDir(),path);
            if(file.exists()){
                path = file.getAbsolutePath();
            }
        }
//        Log.e("getRealFilePath","getRealFilePath:" + path);
        return path;
    }
}
