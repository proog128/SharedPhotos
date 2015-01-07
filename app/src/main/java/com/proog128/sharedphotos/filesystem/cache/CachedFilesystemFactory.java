package com.proog128.sharedphotos.filesystem.cache;

import com.proog128.sharedphotos.filesystem.IFilesystemService;
import com.proog128.sharedphotos.filesystem.IPath;

public class CachedFilesystemFactory {
    private static Cache cache_ = new Cache();

    public IFilesystemService init(IFilesystemService fs) {
        return new CachedFilesystemService(cache_, fs);
    }

    public void makeDirty(IPath path) {
        cache_.remove(path);
    }
}
