package com.proog128.sharedphotos.filesystem;

public interface IDevice {
    public IListTask list(IPath path, int maxResults);
    public IPath getPath();
}
