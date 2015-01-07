package com.proog128.sharedphotos.filesystem;

public class PathIsNotAFile extends RuntimeException {
    public PathIsNotAFile(IPath path) {
        super("Path '" + path.toString() + "' is not a file.");
    }
}
