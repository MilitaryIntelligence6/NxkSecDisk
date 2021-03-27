package edu.swufe.nxksecdisk.server.service.impl;

import edu.swufe.nxksecdisk.server.enumeration.AccountAuth;
import edu.swufe.nxksecdisk.server.mapper.FolderMapper;
import edu.swufe.nxksecdisk.server.mapper.NodeMapper;
import edu.swufe.nxksecdisk.server.mapper.PropertiesMapper;
import edu.swufe.nxksecdisk.server.model.Folder;
import edu.swufe.nxksecdisk.server.model.Node;
import edu.swufe.nxksecdisk.server.model.Property;
import edu.swufe.nxksecdisk.server.service.FileChainService;
import edu.swufe.nxksecdisk.server.util.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author Administrator
 */
@Service
public class FileChainServiceImpl extends RangeFileStreamWriter implements FileChainService
{
    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private FolderMapper folderMapper;

    @Resource
    private FileBlockUtil fileBlockUtil;

    @Resource
    private ContentTypeMap contentTypeMap;

    @Resource
    private LogUtil logUtil;

    @Resource
    private AesCipher cipher;

    @Resource
    private PropertiesMapper propertiesMapper;

    @Resource
    private FolderUtil folderUtil;

    @Override
    public void getResourceByChainKey(HttpServletRequest request, HttpServletResponse response)
    {
        int statusCode = 403;
        if (ConfigureReader.getInstance().isOpenFileChain())
        {
            final String ckey = request.getParameter("ckey");
            // 权限凭证有效性并确认其对应的资源
            if (ckey != null)
            {
                Property keyProp = propertiesMapper.selectByKey("chain_aes_key");
                if (keyProp != null)
                {
                    try
                    {
                        String fid = cipher.decrypt(keyProp.getPropertyValue(), ckey);
                        Node f = this.nodeMapper.queryById(fid);
                        if (f != null)
                        {
                            File target = this.fileBlockUtil.getFileFromBlocks(f);
                            if (target != null && target.isFile())
                            {
                                String fileName = f.getFileName();
                                String suffix = "";
                                if (fileName.indexOf(".") >= 0)
                                {
                                    suffix = fileName.substring(fileName.lastIndexOf(".")).trim().toLowerCase();
                                }
                                String range = request.getHeader("Range");
                                int status = writeRangeFileStream(request, response, target, f.getFileName(),
                                        contentTypeMap.getContentType(suffix), ConfigureReader.getInstance().getDownloadMaxRate(null),
                                        fileBlockUtil.getETag(target), false);
                                if (status == HttpServletResponse.SC_OK
                                        || (range != null && range.startsWith("bytes=0-")))
                                {
                                    this.logUtil.writeChainEvent(request, f);
                                }
                                return;
                            }
                        }
                        statusCode = 404;
                    }
                    catch (Exception e)
                    {
                        logUtil.writeException(e);
                        statusCode = 500;
                    }
                }
                else
                {
                    statusCode = 404;
                }
            }
        }
        try
        {
            //  处理无法下载的资源
            response.sendError(statusCode);
        }
        catch (IOException e)
        {

        }
    }

    @Override
    public String getChainKeyByFid(HttpServletRequest request)
    {
        if (ConfigureReader.getInstance().isOpenFileChain())
        {
            String fid = request.getParameter("fid");
            String account = (String) request.getSession().getAttribute("ACCOUNT");
            if (fid != null)
            {
                final Node f = this.nodeMapper.queryById(fid);
                if (f != null)
                {
                    if (ConfigureReader.getInstance().authorized(account, AccountAuth.DOWNLOAD_FILES,
                            folderUtil.getAllFoldersId(f.getFileParentFolder())))
                    {
                        Folder folder = folderMapper.queryById(f.getFileParentFolder());
                        if (ConfigureReader.getInstance().accessFolder(folder, account))
                        {
                            // 将指定的fid加密为ckey并返回。
                            try
                            {
                                Property keyProp = propertiesMapper.selectByKey("chain_aes_key");
                                if (keyProp == null)
                                {// 如果没有生成过永久性AES密钥，则先生成再加密
                                    String aesKey = cipher.generateRandomKey();
                                    Property chainAESKey = new Property();
                                    chainAESKey.setPropertyKey("chain_aes_key");
                                    chainAESKey.setPropertyValue(aesKey);
                                    if (propertiesMapper.insert(chainAESKey) > 0)
                                    {
                                        return cipher.encrypt(aesKey, fid);
                                    }
                                }
                                else
                                {// 如果已经有了，则直接用其加密
                                    return cipher.encrypt(keyProp.getPropertyValue(), fid);
                                }
                            }
                            catch (Exception e)
                            {
                                logUtil.writeException(e);
                            }
                        }
                    }
                }
            }
        }
        return "ERROR";
    }

}
