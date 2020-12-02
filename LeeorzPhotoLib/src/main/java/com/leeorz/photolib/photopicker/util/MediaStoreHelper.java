package com.leeorz.photolib.photopicker.util;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.leeorz.photolib.R;
import com.leeorz.photolib.photopicker.entity.PhotoDirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;

/**
 * Created by donglua on 15/5/31.
 */
public class MediaStoreHelper {

  public final static int INDEX_ALL_PHOTOS = 0;


  public static void getPhotoDirs(FragmentActivity activity, PhotosResultCallback resultCallback) {
//    activity.getSupportLoaderManager().initLoader(0, null, new PhotoDirLoaderCallbacks(activity, resultCallback));
    LoaderManager.getInstance(activity).initLoader(0, null, new PhotoDirLoaderCallbacks(activity, resultCallback));
  }



  static class PhotoDirLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    Context context;
    PhotosResultCallback resultCallback;

    public PhotoDirLoaderCallbacks(Context context, PhotosResultCallback resultCallback) {
      this.context = context;
      this.resultCallback = resultCallback;
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      return new PhotoDirectoryLoader(context);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      if (data == null)  return;
      data.moveToFirst();
      List<PhotoDirectory> directories = new ArrayList<>();
      PhotoDirectory photoDirectoryAll = new PhotoDirectory();
      photoDirectoryAll.setName(context.getString(R.string.all_image));
      photoDirectoryAll.setId("ALL");

      while (data.moveToNext()) {

        int imageId  = data.getInt(data.getColumnIndexOrThrow(_ID));
        String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
        String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
        String path = data.getString(data.getColumnIndexOrThrow(DATA));

        if (!new File(path).exists()){
          continue;
        }
        if(TextUtils.isEmpty(name)){
          continue;
        }

        PhotoDirectory photoDirectory = new PhotoDirectory();
        photoDirectory.setId(bucketId);
        photoDirectory.setName(name);

        if (!directories.contains(photoDirectory)) {
          photoDirectory.setCoverPath(path);
          photoDirectory.addPhoto(imageId, path);
          photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
          directories.add(photoDirectory);
        } else {
          directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId, path);
        }

        photoDirectoryAll.addPhoto(imageId, path);
      }
      if (photoDirectoryAll.getPhotoPaths().size() > 0) {
        photoDirectoryAll.setCoverPath(photoDirectoryAll.getPhotoPaths().get(0));
      }
      directories.add(INDEX_ALL_PHOTOS, photoDirectoryAll);
      if (resultCallback != null) {
        resultCallback.onResultCallback(directories);
      }
    }

    @Override public void onLoaderReset(Loader<Cursor> loader) {

    }
  }


  public interface PhotosResultCallback {
    void onResultCallback(List<PhotoDirectory> directories);
  }

}
