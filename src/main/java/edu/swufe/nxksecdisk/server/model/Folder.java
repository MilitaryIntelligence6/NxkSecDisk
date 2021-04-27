package edu.swufe.nxksecdisk.server.model;


/**
 * @author Administrator
 */
public class Folder {

	private String folderId;

	private String folderName;

	private String folderCreationDate;

	private String folderCreator;

	private String folderParent;

	private int folderConstraint;

	private int folderHomework = 0;

	private String folderHomeworkStartTime = null;

	private String folderHomeworkEndTime = null;
	
	private int isUploadByTime = 1;

	public int getIsUploadByTime() {
		return isUploadByTime;
	}

	public void setIsUploadByTime(int isUploadByTime) {
		this.isUploadByTime = isUploadByTime;
	}

	public int getFolderHomework() {
		return folderHomework;
	}

	public void setFolderHomework(int folderHomework) {
		this.folderHomework = folderHomework;
	}

	public String getFolderHomeworkStartTime() {
		return folderHomeworkStartTime;
	}

	public void setFolderHomeworkStartTime(String folderHomeworkStartTime) {
		this.folderHomeworkStartTime = folderHomeworkStartTime;
	}

	public String getFolderHomeworkEndTime() {
		return folderHomeworkEndTime;
	}

	public void setFolderHomeworkEndTime(String folderHomeworkEndTime) {
		this.folderHomeworkEndTime = folderHomeworkEndTime;
	}

	public String getFolderId() {
		return this.folderId;
	}

	public void setFolderId(final String folderId) {
		this.folderId = folderId;
	}

	public String getFolderName() {
		return this.folderName;
	}

	public void setFolderName(final String folderName) {
		this.folderName = folderName;
	}

	public String getFolderCreationDate() {
		return this.folderCreationDate;
	}

	@Override
	public String toString() {
		return "Folder [folderId=" + folderId + ", folderName=" + folderName + ", folderCreationDate="
				+ folderCreationDate + ", folderCreator=" + folderCreator + ", folderParent=" + folderParent
				+ ", folderConstraint=" + folderConstraint + ", folderHomework=" + folderHomework
				+ ", folderHomeworkStartTime=" + folderHomeworkStartTime + ", folderHomeworkEndTime="
				+ folderHomeworkEndTime + "]";
	}

	public void setFolderCreationDate(final String folderCreationDate) {
		this.folderCreationDate = folderCreationDate;
	}

	public String getFolderCreator() {
		return this.folderCreator;
	}

	public void setFolderCreator(final String folderCreator) {
		this.folderCreator = folderCreator;
	}

	public String getFolderParent() {
		return this.folderParent;
	}

	public void setFolderParent(final String folderParent) {
		this.folderParent = folderParent;
	}

	public int getFolderConstraint() {
		return folderConstraint;
	}

	public void setFolderConstraint(int folderConstraint) {
		this.folderConstraint = folderConstraint;
	}
}
