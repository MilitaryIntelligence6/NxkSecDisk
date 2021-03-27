package edu.swufe.nxksecdisk.ui.callback;

import java.util.List;

import edu.swufe.nxksecdisk.server.enumeration.LogLevel;
import edu.swufe.nxksecdisk.server.enumeration.VCLevel;
import edu.swufe.nxksecdisk.ui.pojo.FileSystemPath;

public interface GetServerStatus
{
    int getPropertiesStatus();
    
    boolean getServerStatus();
    
    int getPort();
    
    String getInitProt();
    
    int getBufferSize();
    
    String getInitBufferSize();
    
    LogLevel getLogLevel();
    
    LogLevel getInitLogLevel();
    
    VCLevel getVCLevel();
    
    VCLevel getInitVCLevel();
    
    String getFileSystemPath();
    
    String getInitFileSystemPath();
    
    boolean getMustLogin();
    
    boolean isAllowChangePassword();
    
    boolean isOpenFileChain();
    
    List<FileSystemPath> getExtendStores();
    
    int getMaxExtendStoresNum();
}
