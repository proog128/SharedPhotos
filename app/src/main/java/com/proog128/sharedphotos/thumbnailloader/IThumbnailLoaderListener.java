package com.proog128.sharedphotos.thumbnailloader;

import android.graphics.drawable.Drawable;

public interface IThumbnailLoaderListener {
    public void onFinished(Drawable drawable, boolean fromCache);
}
