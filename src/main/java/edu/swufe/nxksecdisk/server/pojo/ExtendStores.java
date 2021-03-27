package edu.swufe.nxksecdisk.server.pojo;

import java.io.File;

/**
 * @author Administrator
 */
public class ExtendStores
{
    private short index;

    private File path;

    public short getIndex()
    {
        return index;
    }

    public void setIndex(short index)
    {
        this.index = index;
    }

    public File getPath()
    {
        return path;
    }

    public void setPath(File path)
    {
        this.path = path;
    }

}
