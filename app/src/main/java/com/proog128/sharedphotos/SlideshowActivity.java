package com.proog128.sharedphotos;

import android.app.LoaderManager;
import android.app.UiModeManager;
import android.content.Loader;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.widget.Toast;

import com.proog128.sharedphotos.filesystem.IFilesystem;
import com.proog128.sharedphotos.filesystem.IFilesystemService;
import com.proog128.sharedphotos.filesystem.IFilesystemServiceListener;
import com.proog128.sharedphotos.filesystem.IPath;
import com.proog128.sharedphotos.filesystem.cache.CachedFilesystemFactory;
import com.proog128.sharedphotos.filesystem.dlna.DlnaFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SlideshowActivity extends FragmentActivity implements IFilesystemServiceListener, LoaderManager.LoaderCallbacks<LoaderResult> {
    private ViewPager pager_;
    private PagerAdapter pagerAdapter_;

    private CachedFilesystemFactory fsFactory_;
    private IFilesystemService fsService_;
    private IFilesystem fs_;

    private IPath currentPath_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager(); // Must be called in onCreate to create LoaderManager.

        setContentView(R.layout.activity_slideshow);

        pager_ = (ViewPager) findViewById(R.id.pager);
        pager_.setBackgroundColor(Color.BLACK);

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            pager_.setPageTransformer(true, new FadePageTransformer());
        } else {
            pager_.setPageTransformer(true, new DepthPageTransformer());
        }

        if (savedInstanceState != null) {
            Object path = savedInstanceState.getSerializable("path");
            if (path != null) {
                currentPath_ = (IPath) path;
            }
        } else {
            IPath path = (IPath) getIntent().getSerializableExtra("path");
            if (path != null) {
                currentPath_ = (IPath) path;
            }
        }

        try {
            long httpCacheSize = 100 * 1024 * 1024; // 100 MiB
            File httpCacheDir = new File(getExternalCacheDir(), "http");
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {

        }

        DlnaFactory dlnaFactory = new DlnaFactory();
        IFilesystemService dlnaService = dlnaFactory.init(getApplicationContext(), this);
        fsFactory_ = new CachedFilesystemFactory();
        fsService_ = fsFactory_.init(dlnaService);
        fsService_.start(this);
    }

    @Override
    protected void onDestroy() {
        getLoaderManager().destroyLoader(0);
        fsService_.stop();

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);

        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        }
    }

    @Override
    public Loader<LoaderResult> onCreateLoader(int id, Bundle args) {
        IPath p = (IPath) args.getSerializable("path");

        setProgressBarIndeterminateVisibility(true);
        return new PathLoader(this, fs_, p.getParent(), true);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult> loader, LoaderResult data) {
        setProgressBarIndeterminateVisibility(false);

        if (data.error == LoaderError.Success) {
            pagerAdapter_ = new ScreenSlidePagerAdapter(getSupportFragmentManager(), data.paths);
            pager_.setAdapter(pagerAdapter_);

            int currentIndex = data.paths.indexOf(currentPath_.getFile());
            pager_.setCurrentItem(currentIndex);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), data.errorText, Toast.LENGTH_LONG);
            toast.show();

            pager_.setAdapter(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult> loader) {
        pager_.setAdapter(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("path", currentPath_);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onInitialized(IFilesystem filesystem) {
        fs_ = filesystem;

        if (currentPath_ == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Current path is empty.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        IPath target = currentPath_;
        Bundle b = new Bundle();
        b.putSerializable("path", target);
        getLoaderManager().initLoader(0, b, this);
    }

    @Override
    public void onDestroyed(IFilesystem filesystem) {
        fs_ = null;
    }

    private static class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<IPath> paths_;

        ScreenSlidePagerAdapter(FragmentManager fm, List<IPath> paths) {
            super(fm);
            paths_ = paths;
        }

        @Override
        public int getCount() {
            return paths_.size();
        }

        @Override
        public Fragment getItem(int i) {
            PictureFragment fragment = new PictureFragment();
            fragment.setUrl(paths_.get(i));
            return fragment;
        }
    }

    private static class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }


    private static class FadePageTransformer implements ViewPager.PageTransformer {
        public void transformPage(View view, float position) {
            view.setTranslationX(view.getWidth() * -position);

            if(position <= -1 || position >= 1) {
                view.setAlpha(0);
            } else if(position == 0) {
                view.setAlpha(1);
            } else {
                float alpha = (position <= 0) ? position + 1 : 1 - position;
                view.setAlpha(alpha);
            }
        }
    }
}
