package com.app.model;

import java.util.List;

public class PagedResponse <T> {
    public List<T> components;
    public int page;
    public int pageSize;
    public int pageCount;
    public int totalResultCount;
    public int totalCount;
}
