package br.com.mvbos.indexff;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public class IndexffData implements Serializable {

	private static final long serialVersionUID = -4311004614131566884L;

	private final String fileName;
	private final String filePath;
	private long lastChange;

	private String[] storedFiles;

	public IndexffData(String fileName, String filePath, long lastChange) {
		super();
		this.fileName = fileName;
		this.filePath = filePath;
		this.lastChange = lastChange;
	}

	public String[] getStoredFiles() {
		return storedFiles;
	}

	public void setStoredFiles(String[] storedFiles) {
		this.storedFiles = storedFiles;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public long getLastChange() {
		return lastChange;
	}

	public void setLastChange(long lastChange) {
		this.lastChange = lastChange;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexffData other = (IndexffData) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IndeffData [fileName=" + fileName + ", filePath=" + filePath + ", lastChange=" + lastChange + ", storedFiles="
				+ Arrays.toString(storedFiles) + "]";
	}

	public String getFullPath() {
		File f = new File(getFilePath(), getFileName());
		final String s = f.getAbsolutePath();
		f = null;

		return s;
	}

}
