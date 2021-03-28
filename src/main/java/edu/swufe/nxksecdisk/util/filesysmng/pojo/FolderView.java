package edu.swufe.nxksecdisk.util.filesysmng.pojo;

import edu.swufe.nxksecdisk.server.model.Node;

import java.util.List;

/**
 * <h2>文件视图（文件管理器专用）</h2>
 * <p>该POJO用于封装需要显示给用户的文件夹内容，供文件管理器使用。提供：当前浏览的文件夹对象、本路径下所有文件夹和所有文件。</p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public class FolderView {

    /**
     * 当前文件夹
     */
    private Folder current;

    /**
     * 该文件夹内所有文件夹（嵌套的，以便于依次加载）
     */
    private List<Folder> folders;

    /**
     * 该文件夹内所有文件
     */
    private List<Node> files;


    public Folder getCurrent() {
        return current;
    }

    public void setCurrent(Folder current) {
        this.current = current;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<Node> getFiles() {
        return files;
    }

    public void setFiles(List<Node> files) {
        this.files = files;
    }
}
