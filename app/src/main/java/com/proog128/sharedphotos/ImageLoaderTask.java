package com.proog128.sharedphotos;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;

import com.proog128.sharedphotos.filesystem.IPath;

import java.net.MalformedURLException;
import java.net.URL;

public class ImageLoaderTask extends AsyncTaskLoader<ImageLoader.Image> {
    private IPath path_;
    private ImageLoader.Image bitmap_;

    public ImageLoaderTask(Context context, IPath path) {
        super(context);

        path_ = path;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (bitmap_ != null) {
           deliverResult(bitmap_);
        }

        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();

        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
    }

    @Override
    public ImageLoader.Image loadInBackground() {
        try {
            return ImageLoader.load(new URL(path_.getContentUrl()));
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
