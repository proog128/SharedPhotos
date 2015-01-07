package com.proog128.sharedphotos.filesystem;

public interface IDeviceServiceListener {
    public void onDeviceAdded(IDevice device);
    public void onDeviceRemoved(IDevice device);
}
