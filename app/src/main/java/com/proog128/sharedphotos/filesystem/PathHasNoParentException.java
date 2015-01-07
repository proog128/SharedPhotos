package com.proog128.sharedphotos.filesystem;

public class PathHasNoParentException extends RuntimeException {
    public PathHasNoParentException(IPath path) {
        super("Path '" + path.toString() + "' has no parent.");
    }
}
