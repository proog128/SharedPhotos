package com.proog128.sharedphotos.filesystem;

public class PathMustBeAbsolute extends RuntimeException {
    public PathMustBeAbsolute(IPath path) {
        super("Path '" + path.toString() + "' must be absolute.");
    }
}
