package com.proog128.sharedphotos.filesystem;

public class PathMustBeRelative extends Throwable {
    public PathMustBeRelative(IPath path) {
        super("Path '" + path.toString() + "' must be relative.");
    }
}
