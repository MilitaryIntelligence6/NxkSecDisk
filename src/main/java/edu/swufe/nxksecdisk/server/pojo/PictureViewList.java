package edu.swufe.nxksecdisk.server.pojo;

import java.util.List;

/**
 * @author Administrator
 */
public class PictureViewList {

    private List<PictureInfo> pictureViewList;

    private int index;

    public List<PictureInfo> getPictureViewList() {
        return this.pictureViewList;
    }

    public void setPictureViewList(final List<PictureInfo> pictureViewList) {
        this.pictureViewList = pictureViewList;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }
}
