package com.proog128.sharedphotos.thumbnailloader;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.proog128.sharedphotos.R;
import com.proog128.sharedphotos.filesystem.IPath;

public class NoThumbnailLoader implements IThumbnailLoader {
    private Drawable defaultIcon_;

    public NoThumbnailLoader(IPath path, Resources resources) {
        defaultIcon_ = resources.getDrawable(R.drawable.ic_album);
    }

    @Override
    public void loadThumbnail(IThumbnailLoaderListener listener) {
        listener.onFinished(defaultIcon_, true);
    }

    @Override
    public void cancel() {
    }
}
