package com.jiongsoft.cocit.actions;

import static com.kmjsoft.cocit.Demsy.appconfig;
import static com.kmjsoft.cocit.Demsy.bizSession;
import static com.kmjsoft.cocit.Demsy.contextDir;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.upload.FieldMeta;
import org.nutz.mvc.upload.TempFile;

import com.jiongsoft.cocit.config.IAppConfig;
import com.jiongsoft.cocit.entitydef.field.Upload;
import com.jiongsoft.cocit.lang.Dates;
import com.jiongsoft.cocit.lang.DemsyException;
import com.jiongsoft.cocit.lang.Files;
import com.jiongsoft.cocit.lang.Img;
import com.jiongsoft.cocit.lang.Lists;
import com.jiongsoft.cocit.lang.Str;
import com.jiongsoft.cocit.log.Log;
import com.jiongsoft.cocit.log.Logs;
import com.jiongsoft.cocit.modules.ckfinder.CKFinder;
import com.jiongsoft.cocit.mvc.MvcConst;
import com.jiongsoft.cocit.mvc.nutz.DemsyUploadAdaptor;
import com.kmjsoft.cocit.Demsy;
import com.kmjsoft.cocit.entity.EntityConst;
import com.kmjsoft.cocit.entity.web.IUploadInfo;
import com.kmjsoft.cocit.entityengine.manager.IBizSession;

/**
 * 
 * @author yongshan.ji
 * 
 */
@Ok("json")
public class UploadActions implements MvcConst {
	private static Log log = Logs.getLog(UploadActions.class);

	private IBizSession session() {
		return bizSession;
	}

	@At({ URL_CKFINDER })
	@Ok("void")
	public void ckfinder() {
		CKFinder.getResponse(Demsy.me().request(), Demsy.me().response());
	}

	@At({ URL_UPLOAD })
	@AdaptBy(type = DemsyUploadAdaptor.class)
	public Map upload(@Param("upload") TempFile tmpfile, String moduleId, String fieldId) {
		if (log.isDebugEnabled())
			log.debugf("上传文件...[moduleId=%s, fieldId=%s, fileName=%s]", moduleId, fieldId, tmpfile == null ? "NULL" : tmpfile.getMeta().getFileLocalName());

		Map map = new HashMap();

		int errorNumber;
		String customMsg;
		boolean success;

		// 保存文件
		try {
			if (tmpfile == null) {
				throw new DemsyException("上传的临时文件不存在! ");
			}

			IUploadInfo info = save(tmpfile, moduleId, fieldId);
			String fileUrl = "";
			if (info.getPath() != null) {
				fileUrl = info.getPath().toString();
			}
			map.put("path", fileUrl);
			map.put("name", info.getLocalName());
			map.put("contentLength", info.getContentLength());

			map.put("fileUrl", fileUrl);
			map.put("fileName", info.getLocalName());

			// try {
			// if (Images.isImage(fileUrl.substring(fileUrl.lastIndexOf(".") +
			// 1).toLowerCase())) {
			// String zoomImgPath = zoomImgPath(fileUrl, 100, 75);
			// boolean isImg = Images.zoomImage(contextDir + fileUrl, contextDir
			// + zoomImgPath, 100, 75);
			// if (isImg) {
			// map.put("fileZoomImageUrl", zoomImgPath);
			// }
			// }
			// } catch (Throwable e) {
			// log.errorf("上传文件: 压缩图片LOGO出错! %s", e);
			// }

			errorNumber = 101;
			customMsg = "上传文件: 成功! [" + info.getLocalName() + "]";
			success = true;

			log.debugf("上传文件成功. [moduleId=%s, fieldId=%s, fileUrl=%s]", moduleId, fieldId, fileUrl);

		} catch (Throwable ex) {
			errorNumber = 1;
			customMsg = "上传文件出错: " + ex.getLocalizedMessage();
			success = false;

			log.errorf("上传文件出错: [moduleId=%s, fieldId=%s, fileName=%s] %s", moduleId, fieldId, tmpfile == null ? "NULL" : tmpfile.getMeta().getFileLocalName(), ex.getLocalizedMessage());
		} finally {
			if (tmpfile != null) {
				tmpfile.getFile().delete();
			}
		}

		map.put("success", success);
		map.put("errorNumber", errorNumber);
		map.put("customMsg", customMsg);

		return map;
	}

	/**
	 * 保存上传的临时文件到系统上传目录，并返回文件的相对路径
	 * 
	 * @return
	 * @throws IOException
	 * @throws Throwable
	 */
	private IUploadInfo save(TempFile model, String moduleId, String fieldId) throws DemsyException, IOException {
		/*
		 * 保存文件到磁盘
		 */
		FieldMeta meta = model.getMeta();
		File tmpfile = model.getFile();

		String ext = meta.getFileExtension();
		if (ext != null && ext.startsWith(".")) {
			ext = ext.substring(1);
		}

		String[] exts = Str.toArray(appconfig.get(IAppConfig.UPLOAD_FILTER).toLowerCase(), "|");
		List<String> extList = Lists.arrayToList(exts);

		if (!extList.contains(ext.toLowerCase())) {
			throw new DemsyException("非法文件类型! [%s]", meta.getFileLocalName());
		}

		// 计算文件存储目录
		String basePath = MvcUtil.getUploadBasePath();
		String folder = Demsy.me().param("folder", String.class, null);
		if (!Str.isEmpty(folder)) {
			if (folder.startsWith("/"))
				basePath = basePath + folder;
			else
				basePath = basePath + "/" + folder;
		} else if (Img.isImage(ext)) {
			basePath = basePath + "/images";
		} else if (ext.equals("swf") || ext.equals("flv")) {
			basePath = basePath + "/flash";
		} else {
			basePath = basePath + "/files";
		}

		if (Str.isEmpty(folder))
			basePath = basePath + "/" + Dates.formatDate(new Date(), "yyMM");

		String realFolder = contextDir + basePath;

		String localName = meta.getFileLocalName();
		localName = localName.replace("(", "_");
		localName = localName.replace(")", "_");
		localName = localName.replace(" ", "_");
		localName = localName.substring(0, localName.length() - ext.length() - 1);

		// 计算文件名
		String fileName;
		File file = null;
		int count = 0;
		while (true) {
			if (count > 0) {
				fileName = localName + "_" + count;
			} else {
				fileName = localName;
			}
			fileName = Integer.toHexString(fileName.hashCode()) + "." + ext;
			// fileName = fileName + "." + ext;
			file = new File(realFolder + "/" + fileName);
			if (file.exists()) {
				count++;
			} else {
				break;
			}
		}

		file.getParentFile().mkdirs();
		file.createNewFile();
		Files.copy(tmpfile, file);

		/*
		 * 保存文件信息到数据库
		 */
		IUploadInfo info = (IUploadInfo) Mirror.me(Demsy.entityModuleManager.getStaticType(EntityConst.BIZSYS_ADMIN_UPLOAD)).born();
		try {
			Demsy me = Demsy.me();
			if (me.getTenant() != null)
				info.setTenantId(me.getTenant().getId());
			info.setContentType(meta.getContentType());
			info.setExtName("UPLOADER");
			info.setLocalName(meta.getFileLocalName());
			info.setContentLength(file.length());
			info.setPath(new Upload(basePath + "/" + fileName));

			if (!Str.isEmpty(moduleId))
				try {
					info.setModule(Demsy.funMenuManager.getModule(Long.parseLong(moduleId)));
				} catch (Throwable e) {
				}

			if (!Str.isEmpty(fieldId)) {
				try {
					info.setBzfield(Demsy.entityModuleManager.getField(Long.parseLong(fieldId)));
				} catch (Throwable e) {
				}
			}

			log.debugf("保存上传信息到到数据库...[path=%s]", info.getPath());
			if (session() != null) {
				session().save(info);
				log.debugf("保存上传信息到到数据库: 结束. [path=%s]", info.getPath());
			} else
				log.errorf("保存上传信息到到数据库出错: [path=%s]IBizSession is null.", info.getPath());
		} catch (Throwable e) {
			log.error(e);
		}

		return info;
	}

}
