package com.proog128.sharedphotos.filesystem.cache;

import com.proog128.sharedphotos.filesystem.IFilesystem;
import com.proog128.sharedphotos.filesystem.IFilesystemServiceListener;

import java.util.HashMap;

public class CachedFilesystemServiceListener implements IFilesystemServiceListener {
    private Cache cache_;
    private IFilesystemServiceListener listener_;

    private HashMap<IFilesystem, CachedFilesystem> cachedFilesystems_ = new HashMap<IFilesystem, CachedFilesystem>();

    public CachedFilesystemServiceListener(Cache cache, IFilesystemServiceListener listener) {
        cache_ = cache;
        listener_ = listener;
    }

    @Override
    public void onInitialized(IFilesystem fs) {
        CachedFilesystem cf = new CachedFilesystem(cache_, fs);
        cachedFilesystems_.put(fs, cf);
        listener_.onInitialized(cf);
    }

    @Override
    public void onDestroyed(IFilesystem fs) {
        CachedFilesystem cf = cachedFilesystems_.get(fs);
        listener_.onDestroyed(cf);
        cachedFilesystems_.remove(cf);
    }
}
