package com.proog128.sharedphotos.filesystem;

public interface ITask<L> extends IStoppable {
    public void start(L l);
}
