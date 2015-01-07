package com.proog128.sharedphotos.filesystem;

public interface IFilesystem {
    public IDeviceService listDevices();
    public IDevice findDevice(IPath device);
    public IPath getRootPath();
}
