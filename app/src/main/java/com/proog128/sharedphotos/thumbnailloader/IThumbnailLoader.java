package com.proog128.sharedphotos.thumbnailloader;

public interface IThumbnailLoader {
    void loadThumbnail(IThumbnailLoaderListener listener);
    void cancel();
}
