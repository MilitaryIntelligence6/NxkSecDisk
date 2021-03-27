package edu.swufe.nxksecdisk.server.model;

/**
 * <h2>文件系统相关设置项的模型</h2>
 * <p>该模型用于描述文件系统数据库中的PROPERTIES表。</p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public class Propertie
{
    private String propertiesKey;

    private String propertiesValue;

    public String getPropertiesKey()
    {
        return propertiesKey;
    }

    public void setPropertiesKey(String propertiesKey)
    {
        this.propertiesKey = propertiesKey;
    }

    public String getPropertiesValue()
    {
        return propertiesValue;
    }

    public void setPropertiesValue(String propertiesValue)
    {
        this.propertiesValue = propertiesValue;
    }

}
