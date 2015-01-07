package com.proog128.sharedphotos.filesystem;

public class InvalidPath extends RuntimeException {
    public InvalidPath(IPath path) {
        super("Invalid path '" + path.toString() + "'");
    }
}
