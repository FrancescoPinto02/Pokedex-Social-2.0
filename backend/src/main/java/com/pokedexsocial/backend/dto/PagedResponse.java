package com.pokedexsocial.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic pagination response wrapper.
 *
 * @param <T> the type of items contained in the page
 */
public class PagedResponse<T> {

    /** Items in the current page. */
    private List<T> items;

    /** Current page number (0-based). */
    private int page;

    /** Number of items per page. */
    private int pageSize;

    /** Total number of items across all pages. */
    private long totalItems;

    /** Total number of pages. */
    private int totalPages;

    /** Whether this is the last page. */
    private boolean last;

    /**
     * Builds a {@code PagedResponse} from a Spring {@link Page}.
     *
     * @param page the Spring Data page
     * @return the corresponding {@code PagedResponse}
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        PagedResponse<T> resp = new PagedResponse<>();
        resp.setItems(page.getContent());
        resp.setPage(page.getNumber());
        resp.setPageSize(page.getSize());
        resp.setTotalItems(page.getTotalElements());
        resp.setTotalPages(page.getTotalPages());
        resp.setLast(page.isLast());
        return resp;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
