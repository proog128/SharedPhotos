package com.proog128.sharedphotos.filesystem;

public class PathHasInvalidType extends RuntimeException {
    public PathHasInvalidType(IPath other) {
        super("Path '" + other.toString() + "' has invalid type.");
    }
}
