package com.proog128.sharedphotos;

import com.proog128.sharedphotos.filesystem.IPath;

import java.util.List;

public final class LoaderResult {
    public IPath parent;
    public List<IPath> paths;
    public LoaderError error;
    public String errorText;

    public LoaderResult(IPath parent, List<IPath> paths, LoaderError error, String errorText) {
        this.parent = parent;
        this.paths = paths;
        this.error = error;
        this.errorText = errorText;
    }
}
