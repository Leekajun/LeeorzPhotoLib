package com.leeorz.photolib.utils.photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.leeorz.photolib.utils.BitmapUtil;
import com.leeorz.photolib.crop.Crop;
import com.leeorz.photolib.photopicker.PhotoPickerActivity;
import com.leeorz.photolib.photopicker.utils.PhotoPickerIntent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片工具
 * Created by 嘉俊 on 2015/10/9.
 */
public class PhotoUtil {
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
    private OnDealImageListener onDealImageListener;

    public static void init(String applicationId){
        FILE_PROVIDER = applicationId + ".fileprovider";
    }

    public PhotoUtil(Activity activity) {
        this.mActivity = activity;
    }

    public PhotoUtil setCrop(boolean isCrop) {
        this.isCrop = isCrop;
        return this;
    }

    public PhotoUtil setFreeCrop(boolean freeCrop) {
        this.isFreeCrop = freeCrop;
        this.isCrop = freeCrop;
        return this;
    }

    public PhotoUtil setMaxSize(int maxWidth,int maxHeight){
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        return this;
    }

    public PhotoUtil setCropAspect(int aspectX,int aspectY) {
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


    public void setOnDealImageListener(OnDealImageListener onDealImageListener) {
        this.onDealImageListener = onDealImageListener;
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
            imagePath = BitmapUtil.ratingImageAndSave(getRealFilePath(imagePath));
            if (isCrop) {
                beginCrop(imagePath);
            } else {
                if (onDealImageListener != null) {
                    int[] arr = getImageWidthAndHeight(imagePath);
                    onDealImageListener.onDealSingleImageComplete(getImage(getRealFilePath(imagePath), arr[0], arr[1]));
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
        ArrayList<String> photos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
        if (photos.size() == 1 && isCrop) {
            beginCrop(photos.get(0));
        } else if (photos.size() == 1) {
            if (onDealImageListener != null) {
                imagePath = photos.get(0);
                int[] arr = getImageWidthAndHeight(imagePath);
                onDealImageListener.onDealSingleImageComplete(getImage(getRealFilePath(imagePath), arr[0], arr[1]));
            }
        } else if (photos.size() != 0) {
            if (onDealImageListener != null) {
                List<Photo> gImageList = new ArrayList();
                for (int i = 0; i < photos.size(); i++) {
                    int[] arr = getImageWidthAndHeight(photos.get(i));
                    gImageList.add(getImage(photos.get(i), arr[0], arr[1]));
                }
                onDealImageListener.onDealMultiImageComplete(gImageList);
            }
        }
    }

    private void beginCrop(String path) {

        File file = new File(getRealFilePath(path));
        Uri source = null;
        Log.e("Build.VERSION.SDK_INT","Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
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
        if (resultCode == mActivity.RESULT_OK && result != null) {
            if (onDealImageListener != null) {
                Crop.getOutput(result).getPath();
//                BitmapUtil.ratingImageAndSave(getRealFilePath(outputImagePath));
                int[] arr = getImageWidthAndHeight(outputImagePath);
                onDealImageListener.onDealSingleImageComplete(getImage(outputImagePath, arr[0], arr[1]));
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(mActivity, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取图片的实体
     *
     * @param path
     * @param w
     * @param h
     * @return
     */
    private Photo getImage(String path, int w, int h) {
        Photo photo = new Photo();
        photo.setWidth(w);
        photo.setHeight(h);
        photo.setUrl(path);
        return photo;
    }

    private Uri getUri() {

//        //兼容性控制
//        File file;
//        if (Build.VERSION.SDK_INT > 8) {
////            file = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//            file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        } else {
//            file = new File(Environment.getExternalStorageDirectory(), AppConfig.IMAGE_PATH);
//        }
//
//        if (!file.exists()) { // 创建目录
//            file.mkdirs();
//        }
//
//
//        String name = UUID.randomUUID() + ".png";
//        File file1 = new File(file, name);
//        imagePath = file1.getAbsolutePath();
//        Uri uri = Uri.fromFile(file1);
//        return uri;

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
        Log.e("getImageWidthAndHeight","imagePath:" + imagePath);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, opts);

        boolean rotate = BitmapUtil.isPictureRotate(imagePath);
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
        Log.e("getRealFilePath","getRealFilePath:" + path);
        return path;
    }
}
