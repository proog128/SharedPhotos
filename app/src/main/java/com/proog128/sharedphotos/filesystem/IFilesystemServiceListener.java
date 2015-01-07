package com.proog128.sharedphotos.filesystem;

public interface IFilesystemServiceListener {
    public void onInitialized(IFilesystem filesystem);
    public void onDestroyed(IFilesystem filesystem);
}
