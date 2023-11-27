package com.app.model;

import java.util.List;

import com.app.utils.LibraryStatus;
import com.app.utils.RequestType;

public class ProcessResult<T> {
    public Identifier from;
    public List<T> result;
    public RequestType type;
    public LibraryStatus status;
}
