package com.proog128.sharedphotos.filesystem.cache;

import com.proog128.sharedphotos.filesystem.IFilesystemService;
import com.proog128.sharedphotos.filesystem.IFilesystemServiceListener;

public class CachedFilesystemService implements IFilesystemService {
    private Cache cache_;
    private IFilesystemService fs_;

    public CachedFilesystemService(Cache cache, IFilesystemService fs) {
        cache_ = cache;
        fs_ = fs;
    }

    @Override
    public void start(IFilesystemServiceListener listener) {
        fs_.start(new CachedFilesystemServiceListener(cache_, listener));
    }

    @Override
    public void stop() {
        fs_.stop();
    }
}
