package edu.swufe.nxksecdisk.server.service.impl;

import com.google.gson.Gson;
import edu.swufe.nxksecdisk.server.enumeration.AccountAuth;
import edu.swufe.nxksecdisk.server.listener.ServerInitListener;
import edu.swufe.nxksecdisk.server.mapper.FolderMapper;
import edu.swufe.nxksecdisk.server.mapper.NodeMapper;
import edu.swufe.nxksecdisk.server.model.Folder;
import edu.swufe.nxksecdisk.server.pojo.CreateNewFolderByNameResponds;
import edu.swufe.nxksecdisk.server.service.FolderService;
import edu.swufe.nxksecdisk.server.util.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FolderServiceImpl implements FolderService {

	@Resource
	private FolderMapper folderMapper;

	@Resource
	private NodeMapper nodeMapper;

	@Resource
	private FolderUtil folderUtil;

	@Resource
	private LogUtil logUtil;

	@Resource
	private Gson gson;

	private final ConfigReader config = ConfigReader.getInstance();

	@Override
	public String newFolder(final HttpServletRequest request) {
		final String parentId = request.getParameter("parentId");
		final String folderName = request.getParameter("folderName");
		final String folderConstraint = request.getParameter("folderConstraint");
		final String account = (String) request.getSession().getAttribute("ACCOUNT");

		final String folderHomework = request.getParameter("folderHomework"); // 传1代表是作业文件夹，底下的时间不能为空
		final String folderHomeworkStartTime = request.getParameter("folderHomeworkStartTime");
		final String folderHomeworkEndTime = request.getParameter("folderHomeworkEndTime");
		if (parentId == null || folderName == null || parentId.length() <= 0 || folderName.length() <= 0) {
			return "errorParameter";
		}
		if (!TextFormatUtil.getInstance().matcherFolderName(folderName) || folderName.indexOf(".") == 0) {
			return "errorParameter";
		}
		if (folderHomework.equals("1")) {
			if (folderHomeworkStartTime == null || folderHomeworkEndTime == null) {
				return "errorParameter";
			}
		}
		final Folder parentFolder = this.folderMapper.queryById(parentId);
		if (parentFolder == null || !config.accessFolder(parentFolder, account)) {
			return "errorParameter";
		}
		if (!config.authorized(account, AccountAuth.CREATE_NEW_FOLDER, folderUtil.getAllFoldersId(parentId))) {
			return "noAuthorized";
		}
		if (folderMapper.queryByParentId(parentId).parallelStream()
				.anyMatch((e) -> e.getFolderName().equals(folderName))) {
			return "nameOccupied";
		}
		if (folderMapper.countByParentId(parentId) >= FileNodeUtil.MAXIMUM_NUM_OF_SINGLE_FOLDER) {
			return "foldersTotalOutOfLimit";
		}
		Folder f = new Folder();
		// 设置子文件夹约束等级，不允许子文件夹的约束等级比父文件夹低
		int pc = parentFolder.getFolderConstraint();
		if (folderConstraint != null) {
			try {
				int ifc = Integer.parseInt(folderConstraint);
				if (ifc != 0 && account == null) {
					return "errorParameter";
				}
				if (ifc < pc) {
					return "errorParameter";
				} else {
					f.setFolderConstraint(ifc);
				}
			} catch (Exception e) {
				return "errorParameter";
			}
		} else {
			return "errorParameter";
		}
		f.setFolderId(UUID.randomUUID().toString());
		f.setFolderName(folderName);
		f.setFolderCreationDate(ServerTimeUtil.accurateToDay());
		f.setFolderHomeworkStartTime(folderHomeworkStartTime);
		f.setFolderHomeworkEndTime(folderHomeworkEndTime);
		f.setFolderHomework(Integer.parseInt(folderHomework));
		if (account != null) {
			f.setFolderCreator(account);
		} else {
			f.setFolderCreator("学生");
		}
		f.setFolderParent(parentId);
		int i = 0;
		while (true) {
			try {
				final int r = this.folderMapper.insertNewFolder(f);
				if (r > 0) {
					if (folderUtil.isValidFolder(f)) {
						this.logUtil.writeCreateFolderEvent(request, f);
						return "createFolderSuccess";
					} else {
						return "cannotCreateFolder";
					}
				}
				break;
			} catch (Exception e) {
				f.setFolderId(UUID.randomUUID().toString());
				i++;
			}
			if (i >= 10) {
				break;
			}
		}
		return "cannotCreateFolder";
	}

	/**
	 * 删除目录的实现方法
	 *
	 * @param request
	 * @return
	 */
	@Override
	public String deleteFolder(final HttpServletRequest request) {
		final String folderId = request.getParameter("folderId");
		final String account = (String) request.getSession().getAttribute("ACCOUNT");
		// 检查删除目标的ID参数是否正确
		if (folderId == null || folderId.length() == 0 || "root".equals(folderId)) {
			return "errorParameter";
		}
		final Folder folder = this.folderMapper.queryById(folderId);
		if (folder == null) {
			return "deleteFolderSuccess";
		}
		// 检查删除者是否具备删除目标的访问许可
		if (!config.accessFolder(folder, account)) {
			return "noAuthorized";
		}
		// 检查权限
		if (!config.authorized(account, AccountAuth.DELETE_FILE_OR_FOLDER,
				folderUtil.getAllFoldersId(folder.getFolderParent()))) {
			return "noAuthorized";
		}
		// 执行迭代删除
		final List<Folder> l = this.folderUtil.getParentList(folderId);
		if (this.folderMapper.deleteById(folderId) > 0) {
			folderUtil.deleteAllChildFolder(folderId);
			this.logUtil.writeDeleteFolderEvent(request, folder, l);
			ServerInitListener.needCheck = true;
			return "deleteFolderSuccess";
		}
		return "cannotDeleteFolder";
	}

	/**
	 * 对编辑目录的实现;
	 *
	 * @param request
	 * @return
	 */
	@Override
	public String renameFolder(final HttpServletRequest request) {
		final String folderId = request.getParameter("folderId");
		final String newName = request.getParameter("newName");
		final String folderConstraint = request.getParameter("folderConstraint");
		final String account = (String) request.getSession().getAttribute("ACCOUNT");

		final String folderHomework = request.getParameter("folderHomework"); // 有值传进来代表是作业文件夹，底下的时间不能为空
		final String folderHomeworkStartTime = request.getParameter("folderHomeworkStartTime");
		final String folderHomeworkEndTime = request.getParameter("folderHomeworkEndTime");
		if (folderId == null || folderId.length() == 0 || newName == null || newName.length() == 0
				|| "root".equals(folderId)) {
			return "errorParameter";
		}
		if (folderHomework != null) {
			if (folderHomeworkStartTime == null || folderHomeworkEndTime == null) {
				return "errorParameter";
			}
		}
		if (!TextFormatUtil.getInstance().matcherFolderName(newName) || newName.indexOf(".") == 0) {
			return "errorParameter";
		}
		final Folder folder = this.folderMapper.queryById(folderId);
		if (folder == null) {
			return "errorParameter";
		}
		if (!config.accessFolder(folder, account)) {
			return "noAuthorized";
		}
		if (!config.authorized(account, AccountAuth.RENAME_FILE_OR_FOLDER,
				folderUtil.getAllFoldersId(folder.getFolderParent()))) {
			return "noAuthorized";
		}
		final Folder parentFolder = this.folderMapper.queryById(folder.getFolderParent());
		int pc = parentFolder.getFolderConstraint();
		if (folderConstraint != null) {
			try {
				int ifc = Integer.parseInt(folderConstraint);
				if (ifc > 0 && account == null) {
					return "errorParameter";
				}
				if (ifc < pc) {
					return "errorParameter";
				} else {
					Map<String, Object> map = new HashMap<>();
					map.put("newConstraint", ifc);
					map.put("folderId", folderId);
					folderMapper.updateFolderConstraintById(map);
					folderUtil.changeChildFolderConstraint(folderId, ifc);
					if (!folder.getFolderName().equals(newName)) {
						if (folderMapper.queryByParentId(parentFolder.getFolderId()).parallelStream()
								.anyMatch((e) -> e.getFolderName().equals(newName))) {
							return "nameOccupied";
						}
						Map<String, String> map2 = new HashMap<String, String>();
						map2.put("folderId", folderId);
						map2.put("newName", newName);
						if (this.folderMapper.updateFolderNameById(map2) == 0) {
							return "errorParameter";
						}
						this.logUtil.writeRenameFolderEvent(request, folder, newName, folderConstraint);
					}
					Folder f = new Folder();
					f.setFolderId(folderId);
					f.setFolderName(newName);
					if (folderHomework != null) {
						f.setFolderHomework(1);
						f.setFolderHomeworkStartTime(folderHomeworkStartTime);
						f.setFolderHomeworkEndTime(folderHomeworkEndTime);
						if (this.folderMapper.updateHomeworkFolder(f) == 0) {
							return "errorParameter";
						}
					} else {
						if (this.folderMapper.updateHomeworkFolder(f) == 0) {
							return "errorParameter";
						}

					}
					String newTime = folderHomeworkStartTime + folderHomeworkEndTime;
					this.logUtil.writeChangeTimeFolderEvent(request, f, newTime, folderConstraint);
					return "renameFolderSuccess";
				}
			} catch (Exception e) {
				return "errorParameter";
			}
		} else {
			return "errorParameter";
		}
	}

	@Override
	public String deleteFolderByName(HttpServletRequest request) {
		final String parentId = request.getParameter("parentId");
		final String folderName = request.getParameter("folderName");
		final String account = (String) request.getSession().getAttribute("ACCOUNT");
		if (parentId == null || parentId.length() == 0) {
			return "deleteError";
		}
		Folder p = folderMapper.queryById(parentId);
		if (p == null) {
			return "deleteError";
		}
		if (!config.authorized(account, AccountAuth.DELETE_FILE_OR_FOLDER, folderUtil.getAllFoldersId(parentId))
				|| !config.accessFolder(p, account)) {
			return "deleteError";
		}
		final Folder[] repeatFolders = this.folderMapper.queryByParentId(parentId).parallelStream()
				.filter((f) -> f.getFolderName().equals(folderName)).toArray(Folder[]::new);
		for (Folder rf : repeatFolders) {
			if (!config.accessFolder(rf, account)) {
				return "deleteError";
			}
			final List<Folder> l = this.folderUtil.getParentList(rf.getFolderId());
			if (this.folderMapper.deleteById(rf.getFolderId()) > 0) {
				folderUtil.deleteAllChildFolder(rf.getFolderId());
				this.logUtil.writeDeleteFolderEvent(request, rf, l);
			} else {
				return "deleteError";
			}
		}
		ServerInitListener.needCheck = true;
		return "deleteSuccess";
	}

	@Override
	public String createNewFolderByName(HttpServletRequest request) {
		final String parentId = request.getParameter("parentId");
		final String folderName = request.getParameter("folderName");
		final String folderConstraint = request.getParameter("folderConstraint");
		final String account = (String) request.getSession().getAttribute("ACCOUNT");
		CreateNewFolderByNameResponds cnfbnr = new CreateNewFolderByNameResponds();
		if (parentId == null || folderName == null || parentId.length() <= 0 || folderName.length() <= 0) {
			cnfbnr.setResult("error");
			return gson.toJson(cnfbnr);
		}
		if (folderName.equals(".") || folderName.equals("..")) {
			cnfbnr.setResult("error");
			return gson.toJson(cnfbnr);
		}
		final Folder parentFolder = this.folderMapper.queryById(parentId);
		if (parentFolder == null || !config.accessFolder(parentFolder, account)) {
			cnfbnr.setResult("error");
			return gson.toJson(cnfbnr);
		}
		if (!config.authorized(account, AccountAuth.CREATE_NEW_FOLDER, folderUtil.getAllFoldersId(parentId))) {
			cnfbnr.setResult("error");
			return gson.toJson(cnfbnr);
		}
		if (folderMapper.countByParentId(parentId) >= FileNodeUtil.MAXIMUM_NUM_OF_SINGLE_FOLDER) {
			cnfbnr.setResult("foldersTotalOutOfLimit");
			return gson.toJson(cnfbnr);
		}
		Folder f = new Folder();
		if (folderMapper.queryByParentId(parentId).parallelStream()
				.anyMatch((e) -> e.getFolderName().equals(folderName))) {
			f.setFolderName(FileNodeUtil.requireNewFolderName(folderName, folderMapper.queryByParentId(parentId)));
		} else {
			cnfbnr.setResult("error");
			return gson.toJson(cnfbnr);
		}
		// 设置子文件夹约束等级，不允许子文件夹的约束等级比父文件夹低
		int pc = parentFolder.getFolderConstraint();
		if (folderConstraint != null) {
			try {
				int ifc = Integer.parseInt(folderConstraint);
				if (ifc != 0 && account == null) {
					cnfbnr.setResult("error");
					return gson.toJson(cnfbnr);
				}
				if (ifc < pc) {
					cnfbnr.setResult("error");
					return gson.toJson(cnfbnr);
				} else {
					f.setFolderConstraint(ifc);
				}
			} catch (Exception e) {
				cnfbnr.setResult("error");
				return gson.toJson(cnfbnr);
			}
		} else {
			cnfbnr.setResult("error");
			return gson.toJson(cnfbnr);
		}
		f.setFolderId(UUID.randomUUID().toString());
		f.setFolderCreationDate(ServerTimeUtil.accurateToDay());
		if (account != null) {
			f.setFolderCreator(account);
		} else {
			f.setFolderCreator("学生");
		}
		f.setFolderParent(parentId);
		int i = 0;
		while (true) {
			try {
				final int r = this.folderMapper.insertNewFolder(f);
				if (r > 0) {
					if (folderUtil.isValidFolder(f)) {
						this.logUtil.writeCreateFolderEvent(request, f);
						cnfbnr.setResult("success");
						cnfbnr.setNewName(f.getFolderName());
						return gson.toJson(cnfbnr);
					} else {
						cnfbnr.setResult("error");
						return gson.toJson(cnfbnr);
					}
				}
				break;
			} catch (Exception e) {
				f.setFolderId(UUID.randomUUID().toString());
				i++;
			}
			if (i >= 10) {
				break;
			}
		}
		cnfbnr.setResult("error");
		return gson.toJson(cnfbnr);
	}

}
