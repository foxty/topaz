package com.topaz.controller;

public class Pagination {

	private boolean ready;
	private long recordSize;
	private int pageSize;
	private int page;
	private int maxPage;

	public Pagination(int pageSize, int page) {
		this.pageSize = pageSize;
		this.page = page <= 1 ? 1 : page;
		this.ready = false;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPage() {
		return page;
	}

	public int getMaxPage() {
		return maxPage;
	}

	public long getRecordSize() {
		return recordSize;
	}

	public int getOffset() {
		return (page - 1) * pageSize;
	}

	public boolean isReady() {
		return ready;
	}

	public boolean hasRecord() {
		return this.recordSize > 0;
	}

	public void calcPagination(long recordCount) {
		this.recordSize = recordCount;
		maxPage = (int) (recordCount / pageSize);
		maxPage = (recordCount % pageSize == 0) ? maxPage : maxPage + 1;

		ready = true;
	}

	public String toString() {
		return "[Pagination@" + this + ": ready=" + ready + ", recordSize="
				+ recordSize + ", page=" + page + ", pageSize=" + pageSize
				+ ", maxPage=" + maxPage + "]";
	}
}
