package com.proog128.sharedphotos.filesystem.cache;

import com.proog128.sharedphotos.filesystem.IDevice;
import com.proog128.sharedphotos.filesystem.IListTask;
import com.proog128.sharedphotos.filesystem.IPath;

public class CachedDevice implements IDevice {
    private Cache cache_;
    private IDevice device_;

    public CachedDevice(Cache cache, IDevice device) {
        cache_ = cache;
        device_ = device;
    }

    @Override
    public IListTask list(IPath path, int maxResults) {
        return new CachedListTask(path, maxResults, device_.list(path, maxResults), cache_);
    }

    @Override
    public IPath getPath() {
        return device_.getPath();
    }
}
