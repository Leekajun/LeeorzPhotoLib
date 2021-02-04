package com.lazyee.photolib.photopicker.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.lazyee.photolib.entity.Photo;
import com.lazyee.photolib.entity.PhotoDirectory;
import com.lazyee.photolib.photopicker.event.Selectable;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH> implements Selectable {

  private static final String TAG = SelectableAdapter.class.getSimpleName();

  protected List<PhotoDirectory> photoDirectories;
  protected List<Photo> selectedPhotos;
  public int currentDirectoryIndex = 0;


  public SelectableAdapter() {
    photoDirectories = new ArrayList<>();
    selectedPhotos = new ArrayList<>();
  }


  /**
   * Indicates if the item at position position is selected
   *
   * @param photo Photo of the item to check
   * @return true if the item is selected, false otherwise
   */
  @Override public boolean isSelected(Photo photo) {
    return getSelectedPhotos().contains(photo);
  }


  /**
   * Toggle the selection status of the item at a given position
   *
   * @param photo Photo of the item to toggle the selection status for
   */
  @Override public void toggleSelection(Photo photo) {
    if (selectedPhotos.contains(photo)) {
      selectedPhotos.remove(photo);
    } else {
      selectedPhotos.add(photo);
    }
  }


  /**
   * Clear the selection status for all items
   */
  @Override public void clearSelection() {
    selectedPhotos.clear();
  }


  /**
   * Count the selected items
   *
   * @return Selected items count
   */
  @Override public int getSelectedItemCount() {
    return selectedPhotos.size();
  }


  public void setCurrentDirectoryIndex(int currentDirectoryIndex) {
    if(currentDirectoryIndex < photoDirectories.size()){
      this.currentDirectoryIndex = currentDirectoryIndex;
    }
  }


  public List<Photo> getCurrentPhotos() {
    return photoDirectories.get(currentDirectoryIndex).getPhotos();
  }


  public List<String> getCurrentPhotoPaths() {
    List<String> currentPhotoPaths = new ArrayList<>();
    for (Photo photo : getCurrentPhotos()) {
      currentPhotoPaths.add(photo.getPath());
    }
    return currentPhotoPaths;
  }


  @Override public List<Photo> getSelectedPhotos() {
    return selectedPhotos;
  }

}