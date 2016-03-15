/*
 * Copyright (C) 2013 Atol Conseils et Développements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atolcd.alfresco;

public class AtolVolumetryEntry {
	private long id = 0;
	private String siteId = "";
	private long siteSize = 0;
	private int folderCount = 0;
	private int fileCount = 0;
	private long atTime = 0;

	public AtolVolumetryEntry() {
	}

	public AtolVolumetryEntry(String siteId, long siteSize, int folderCount, int fileCount, long atTime) {
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