package edu.swufe.nxksecdisk.ui.callback;

import edu.swufe.nxksecdisk.server.enumeration.LogLevel;
import edu.swufe.nxksecdisk.server.enumeration.VcLevel;
import edu.swufe.nxksecdisk.ui.pojo.FileSystemPath;

import java.util.List;

/**
 * @author Administrator
 */
public interface GetServerStatus {

    int requirePropertiesStatus();

    boolean requireServerStatus();

    int requirePort();

    String requireInitPort();

    int requireBufferSize();

    String requireInitBufferSize();

    LogLevel requireLogLevel();

    LogLevel requireInitLogLevel();

    VcLevel requireVcLevel();

    VcLevel requireInitVcLevel();

    String requireFileSystemPath();

    String requireInitFileSystemPath();

    boolean requireMustLogin();

    boolean isAllowChangePassword();

    boolean isOpenFileChain();

    List<FileSystemPath> requireExtendStores();

    int requireMaxExtendStoresNum();
}
