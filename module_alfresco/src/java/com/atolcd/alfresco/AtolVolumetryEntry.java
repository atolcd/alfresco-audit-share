package com.atolcd.alfresco;

public class AtolVolumetryEntry {
	private long id = 0;
	private String siteId = "";
	private long siteSize = 0;
	private int folderCount = 0;
	private int fileCount = 0;
	private long atTime = 0;
	
	public AtolVolumetryEntry(){
	}
	
	public AtolVolumetryEntry(String siteId, long siteSize, int folderCount,int fileCount,long atTime){
		this.siteId = siteId;
		this.siteSize = siteSize;
		this.folderCount = folderCount;
		this.fileCount = fileCount;
		this.atTime = atTime;	
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	public long getSiteSize() {
		return siteSize;
	}
	public void setSiteSize(long siteSize) {
		this.siteSize = siteSize;
	}
	public int getFolderCount() {
		return folderCount;
	}
	public void setFolderCount(int folderCount) {
		this.folderCount = folderCount;
	}
	public int getFileCount() {
		return fileCount;
	}
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	public long getAtTime() {
		return atTime;
	}
	public void setAtTime(long atTime) {
		this.atTime = atTime;
	}

	
}
