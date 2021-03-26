package kohgylw.kiftd.server.service.impl;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import kohgylw.kiftd.server.enumeration.AccountAuth;
import kohgylw.kiftd.server.mapper.FolderMapper;
import kohgylw.kiftd.server.mapper.NodeMapper;
import kohgylw.kiftd.server.mapper.PropertiesMapper;
import kohgylw.kiftd.server.model.Folder;
import kohgylw.kiftd.server.model.Node;
import kohgylw.kiftd.server.model.Propertie;
import kohgylw.kiftd.server.service.FileChainService;
import kohgylw.kiftd.server.util.AesCipher;
import kohgylw.kiftd.server.util.ConfigureReader;
import kohgylw.kiftd.server.util.ContentTypeMap;
import kohgylw.kiftd.server.util.FileBlockUtil;
import kohgylw.kiftd.server.util.FolderUtil;
import kohgylw.kiftd.server.util.LogUtil;
import kohgylw.kiftd.server.util.RangeFileStreamWriter;

@Service
public class FileChainServiceImpl extends RangeFileStreamWriter implements FileChainService {

	@Resource
	private NodeMapper nm;
	@Resource
	private FolderMapper flm;
	@Resource
	private FileBlockUtil fbu;
	@Resource
	private ContentTypeMap ctm;
	@Resource
	private LogUtil lu;
	@Resource
	private AesCipher cipher;
	@Resource
	private PropertiesMapper pm;
	@Resource
	private FolderUtil fu;

	@Override
	public void getResourceByChainKey(HttpServletRequest request, HttpServletResponse response) {
		int statusCode = 403;
		if (ConfigureReader.getInstance().isOpenFileChain()) {
			final String ckey = request.getParameter("ckey");
			// 权限凭证有效性并确认其对应的资源
			if (ckey != null) {
				Propertie keyProp = pm.selectByKey("chain_aes_key");
				if (keyProp != null) {
					try {
						String fid = cipher.decrypt(keyProp.getPropertiesValue(), ckey);
						Node f = this.nm.queryById(fid);
						if (f != null) {
							File target = this.fbu.getFileFromBlocks(f);
							if (target != null && target.isFile()) {
								String fileName = f.getFileName();
								String suffix = "";
								if (fileName.indexOf(".") >= 0) {
									suffix = fileName.substring(fileName.lastIndexOf(".")).trim().toLowerCase();
								}
								String range = request.getHeader("Range");
								int status = writeRangeFileStream(request, response, target, f.getFileName(),
										ctm.getContentType(suffix), ConfigureReader.getInstance().getDownloadMaxRate(null),
										fbu.getETag(target), false);
								if (status == HttpServletResponse.SC_OK
										|| (range != null && range.startsWith("bytes=0-"))) {
									this.lu.writeChainEvent(request, f);
								}
								return;
							}
						}
						statusCode = 404;
					} catch (Exception e) {
						lu.writeException(e);
						statusCode = 500;
					}
				} else {
					statusCode = 404;
				}
			}
		}
		try {
			//  处理无法下载的资源
			response.sendError(statusCode);
		} catch (IOException e) {

		}
	}

	@Override
	public String getChainKeyByFid(HttpServletRequest request) {
		if (ConfigureReader.getInstance().isOpenFileChain()) {
			String fid = request.getParameter("fid");
			String account = (String) request.getSession().getAttribute("ACCOUNT");
			if (fid != null) {
				final Node f = this.nm.queryById(fid);
				if (f != null) {
					if (ConfigureReader.getInstance().authorized(account, AccountAuth.DOWNLOAD_FILES,
							fu.getAllFoldersId(f.getFileParentFolder()))) {
						Folder folder = flm.queryById(f.getFileParentFolder());
						if (ConfigureReader.getInstance().accessFolder(folder, account)) {
							// 将指定的fid加密为ckey并返回。
							try {
								Propertie keyProp = pm.selectByKey("chain_aes_key");
								if (keyProp == null) {// 如果没有生成过永久性AES密钥，则先生成再加密
									String aesKey = cipher.generateRandomKey();
									Propertie chainAESKey = new Propertie();
									chainAESKey.setPropertiesKey("chain_aes_key");
									chainAESKey.setPropertiesValue(aesKey);
									if (pm.insert(chainAESKey) > 0) {
										return cipher.encrypt(aesKey, fid);
									}
								} else {// 如果已经有了，则直接用其加密
									return cipher.encrypt(keyProp.getPropertiesValue(), fid);
								}
							} catch (Exception e) {
								lu.writeException(e);
							}
						}
					}
				}
			}
		}
		return "ERROR";
	}

}