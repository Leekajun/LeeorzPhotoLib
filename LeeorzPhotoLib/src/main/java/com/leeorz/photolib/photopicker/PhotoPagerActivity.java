package com.leeorz.photolib.photopicker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.gyf.immersionbar.ImmersionBar;
import com.leeorz.photolib.R;
import com.leeorz.photolib.photopicker.fragment.ImagePagerFragment;

import java.util.List;

/**
 * Created by donglua on 15/6/24.
 */
public class PhotoPagerActivity extends AppCompatActivity {

    private ImagePagerFragment pagerFragment;

    public final static String EXTRA_CURRENT_ITEM = "current_item";
    public final static String EXTRA_PHOTOS = "photos";

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pager);
        ImmersionBar.with(this).barColor(R.color.colorPrimary).init();

        int currentItem = getIntent().getIntExtra(EXTRA_CURRENT_ITEM, 0);
        List<String> paths = getIntent().getStringArrayListExtra(EXTRA_PHOTOS);

        pagerFragment = (ImagePagerFragment) getSupportFragmentManager().findFragmentById(R.id.photoPagerFragment);
        pagerFragment.setPhotos(paths, currentItem);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        updateActionBarTitle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionBar.setElevation(25);
        }


        pagerFragment.getViewPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                updateActionBarTitle();
            }

            @Override
            public void onPageSelected(int i) { }

            @Override
            public void onPageScrollStateChanged(int i) { }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }


    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        intent.putExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS, pagerFragment.getPaths());
        setResult(RESULT_OK, intent);
        finish();

        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.delete) {
            final int index = pagerFragment.getCurrentItem();

            final String deletedPath = pagerFragment.getPaths().get(index);

            if (pagerFragment.getPaths().size() <= 1) {

                // show confirm dialog
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm_to_delete)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                setResult(RESULT_OK);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();

            } else {
                pagerFragment.getPaths().remove(index);
                pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateActionBarTitle() {
        actionBar.setTitle(
                getString(R.string.image_index, pagerFragment.getViewPager().getCurrentItem() + 1,
                        pagerFragment.getPaths().size()));
    }
}
