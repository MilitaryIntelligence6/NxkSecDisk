package edu.swufe.nxksecdisk.server.pojo;

import edu.swufe.nxksecdisk.server.model.Node;

/**
 * <h2>一种专门针对视频格式的封装</h2>
 * <p>该封装除了具备node对象的属性外，额外增加了“needEncode”属性，其值为“Y”或“N”，用于代表是否需要进行转码。</p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public class VideoInfo extends Node {

    public VideoInfo(Node node) {
        this.setFileId(node.getFileId());
        this.setFileName(node.getFileName());
        this.setFileParentFolder(node.getFileParentFolder());
        this.setFilePath(node.getFilePath());
        this.setFileSize(node.getFileSize());
        this.setFileCreationDate(node.getFileCreationDate());
        this.setFileCreator(node.getFileCreator());
    }

    private String needEncode;

    public String getNeedEncode() {
        return needEncode;
    }

    public void setNeedEncode(String needEncode) {
        this.needEncode = needEncode;
    }

}
