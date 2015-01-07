package com.proog128.sharedphotos.filesystem.cache;

import com.proog128.sharedphotos.filesystem.IDeviceService;
import com.proog128.sharedphotos.filesystem.IDeviceServiceListener;

public class CachedDeviceService implements IDeviceService {
    private Cache cache_;
    private IDeviceService deviceService_;

    public CachedDeviceService(Cache cache, IDeviceService deviceService) {
        cache_ = cache;
        deviceService_ = deviceService;
    }

    @Override
    public void start(IDeviceServiceListener listener) {
        deviceService_.start(new CachedDeviceServiceListener(cache_, listener));
    }

    @Override
    public void stop() {
        deviceService_.stop();
    }
}
