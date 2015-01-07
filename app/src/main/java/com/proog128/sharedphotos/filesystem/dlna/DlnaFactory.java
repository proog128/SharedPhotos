package com.proog128.sharedphotos.filesystem.dlna;

import android.app.Activity;
import android.content.Context;

import com.proog128.sharedphotos.filesystem.IFilesystemService;

public class DlnaFactory {
    public IFilesystemService init(Context applicationContext, Activity activity) {
        return new FilesystemService(applicationContext, activity);
    }
}
