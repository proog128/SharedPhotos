package com.proog128.sharedphotos.thumbnailloader;

import android.content.res.Resources;

import com.proog128.sharedphotos.filesystem.IFilesystem;
import com.proog128.sharedphotos.filesystem.IPath;
import com.proog128.sharedphotos.filesystem.ThumbnailUrl;

import java.util.concurrent.Executor;

public class ThumbnailLoaderFactory {
    public static IThumbnailLoader make(IPath path, Executor executor, Resources resources, IFilesystem fs) {
        ThumbnailUrl url = path.getThumbnailUrl();

        if(url == null || url.url == null) {
            if(path.isCollection()) {
                return new AlbumThumbnailLoader(path, executor, resources, fs);
            } else {
                return new NoThumbnailLoader(path, resources);
            }
        }

        return new DirectThumbnailLoader(path, executor, url.fromExif);
    }
}
