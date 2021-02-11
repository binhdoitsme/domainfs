package com.hanu.domainfs.ws.generators.models;

import java.util.Collection;

/**
 * Wrapper class to represent a page with currentPage and pageCount
 */
public final class Page<T> {
    private final int currentPage;
    private final int pageCount;
    private final Collection<T> content;

    public Page(int currentPage, int pageCount, Collection<T> content) {
        this.currentPage = currentPage;
        this.pageCount = pageCount;
        this.content = content;
    }

    public Collection<T> getContent() {
        return content;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageCount() {
        return pageCount;
    }
}
