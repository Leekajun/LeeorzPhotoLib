package com.lazyee.photolib.photopicker.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.lazyee.photolib.R;
import com.lazyee.photolib.photopicker.PhotoPickerActivity;
import com.lazyee.photolib.photopicker.adapter.PhotoGridAdapter;
import com.lazyee.photolib.photopicker.adapter.PopupDirectoryListAdapter;
import com.lazyee.photolib.entity.Photo;
import com.lazyee.photolib.entity.PhotoDirectory;
import com.lazyee.photolib.photopicker.event.OnPhotoClickListener;
import com.lazyee.photolib.photopicker.util.ImageCaptureManager;
import com.lazyee.photolib.photopicker.util.MediaStoreHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class PhotoPickerFragment extends Fragment {

  private ImageCaptureManager captureManager;
  private PhotoGridAdapter photoGridAdapter;
  private boolean isSelectSingle = false;//默认可以选择多张
  private PopupDirectoryListAdapter listAdapter;
  private List<PhotoDirectory> directories;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    directories = new ArrayList<>();

    captureManager = new ImageCaptureManager(getActivity());

    MediaStoreHelper.getPhotoDirs(getActivity(),
            new MediaStoreHelper.PhotosResultCallback() {
              @Override
              public void onResultCallback(List<PhotoDirectory> directories) {
                PhotoPickerFragment.this.directories.clear();
                PhotoPickerFragment.this.directories.addAll(directories);
                photoGridAdapter.notifyDataSetChanged();
                listAdapter.notifyDataSetChanged();
              }
            });
  }


  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    setRetainInstance(true);

    final View rootView = inflater.inflate(R.layout.fragment_photo_picker, container, false);


    photoGridAdapter = new PhotoGridAdapter(getActivity(), directories);
    photoGridAdapter.setSelectSingle(this.isSelectSingle);
    listAdapter  = new PopupDirectoryListAdapter(getActivity(), directories);


    RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_photos);
    StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL);
    layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(photoGridAdapter);
    recyclerView.setItemAnimator(new DefaultItemAnimator());

    final TextView tvDir = rootView.findViewById(R.id.tvDir);


    final ListPopupWindow listPopupWindow = new ListPopupWindow(getActivity());
    listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
    listPopupWindow.setAnchorView(tvDir);
    listPopupWindow.setAdapter(listAdapter);
    listPopupWindow.setModal(true);
    listPopupWindow.setDropDownGravity(Gravity.BOTTOM);
    listPopupWindow.setAnimationStyle(R.style.Animation_AppCompat_DropDownUp);

    listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listPopupWindow.dismiss();

        PhotoDirectory directory = directories.get(position);

        tvDir.setText(directory.getName());

        photoGridAdapter.setCurrentDirectoryIndex(position);
        photoGridAdapter.notifyDataSetChanged();
      }
    });

    photoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {
      @Override public void onClick(View v, int position, boolean showCamera) {
        final int index = showCamera ? position - 1 : position;

        List<String> photos = photoGridAdapter.getCurrentPhotoPaths();

        int [] screenLocation = new int[2];
        v.getLocationOnScreen(screenLocation);
        ImagePagerFragment imagePagerFragment =
            ImagePagerFragment.newInstance(photos, index, screenLocation,
                v.getWidth(), v.getHeight());

        ((PhotoPickerActivity) getActivity()).addImagePagerFragment(imagePagerFragment);
      }
    });

    photoGridAdapter.setOnCameraClickListener(new OnClickListener() {
      @Override public void onClick(View view) {
        try {
          Intent intent = captureManager.dispatchTakePictureIntent();
          startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    rootView.findViewById(R.id.llAllImage).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {

        if (listPopupWindow.isShowing()) {
          listPopupWindow.dismiss();
        } else if (!getActivity().isFinishing()) {
          listPopupWindow.setHeight(Math.round(rootView.getHeight() * 0.8f));
          listPopupWindow.show();
        }

      }
    });

    return rootView;
  }


  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
      captureManager.galleryAddPic();
      if (directories.size() > 0) {
        String path = captureManager.getCurrentPhotoPath();
        PhotoDirectory directory = directories.get(MediaStoreHelper.INDEX_ALL_PHOTOS);
        directory.getPhotos().add(MediaStoreHelper.INDEX_ALL_PHOTOS, new Photo(path.hashCode(), path));
        directory.setCoverPath(path);
        photoGridAdapter.notifyDataSetChanged();
      }
    }
  }


  public PhotoGridAdapter getPhotoGridAdapter() {
    return photoGridAdapter;
  }

  /**
   * 是否选择单张图片
   * @param isSelectSingle
   */
  public void setSelectSingle(boolean isSelectSingle){
    this.isSelectSingle = isSelectSingle;

    if(photoGridAdapter != null){
      photoGridAdapter.setSelectSingle(this.isSelectSingle);
    }
  }


  @Override public void onSaveInstanceState(Bundle outState) {
    captureManager.onSaveInstanceState(outState);
    super.onSaveInstanceState(outState);
  }


  @Override public void onViewStateRestored(Bundle savedInstanceState) {
    captureManager.onRestoreInstanceState(savedInstanceState);
    super.onViewStateRestored(savedInstanceState);
  }

  public ArrayList<String> getSelectedPhotoPaths() {
    return photoGridAdapter.getSelectedPhotoPaths();
  }

}
