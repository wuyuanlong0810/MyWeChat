package com.mywechat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.mywechat.entity.config.Appconfig;
import com.mywechat.entity.constants.Constants;
import com.mywechat.entity.enums.AppUpdateFileTypeEnum;
import com.mywechat.entity.enums.AppUpdateStatusEnum;
import com.mywechat.entity.enums.ResponseCodeEnum;
import com.mywechat.exception.BusinessException;
import org.springframework.stereotype.Service;

import com.mywechat.entity.enums.PageSize;
import com.mywechat.entity.query.AppUpdateQuery;
import com.mywechat.entity.po.AppUpdate;
import com.mywechat.entity.vo.PaginationResultVO;
import com.mywechat.entity.query.SimplePage;
import com.mywechat.mappers.AppUpdateMapper;
import com.mywechat.service.AppUpdateService;
import com.mywechat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * app发布 业务接口实现
 */
@Service("appUpdateService")
public class AppUpdateServiceImpl implements AppUpdateService {

	@Resource
	private AppUpdateMapper<AppUpdate, AppUpdateQuery> appUpdateMapper;

	@Resource
	private Appconfig appconfig;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<AppUpdate> findListByParam(AppUpdateQuery param) {
		return this.appUpdateMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(AppUpdateQuery param) {
		return this.appUpdateMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<AppUpdate> findListByPage(AppUpdateQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<AppUpdate> list = this.findListByParam(param);
		PaginationResultVO<AppUpdate> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(AppUpdate bean) {
		return this.appUpdateMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<AppUpdate> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.appUpdateMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<AppUpdate> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.appUpdateMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(AppUpdate bean, AppUpdateQuery param) {
		StringTools.checkParam(param);
		return this.appUpdateMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(AppUpdateQuery param) {
		StringTools.checkParam(param);
		return this.appUpdateMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public AppUpdate getAppUpdateById(Integer id) {
		return this.appUpdateMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateAppUpdateById(AppUpdate bean, Integer id) {
		return this.appUpdateMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteAppUpdateById(Integer id) {
        AppUpdate dbInfo = this.getAppUpdateById(id);
        if (!AppUpdateStatusEnum.INIT.getStatus().equals(dbInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
		return this.appUpdateMapper.deleteById(id);
	}

	/**
	 * 根据Version获取对象
	 */
	@Override
	public AppUpdate getAppUpdateByVersion(String version) {
		return this.appUpdateMapper.selectByVersion(version);
	}

	/**
	 * 根据Version修改
	 */
	@Override
	public Integer updateAppUpdateByVersion(AppUpdate bean, String version) {
		return this.appUpdateMapper.updateByVersion(bean, version);
	}

	/**
	 * 根据Version删除
	 */
	@Override
	public Integer deleteAppUpdateByVersion(String version) {
		return this.appUpdateMapper.deleteByVersion(version);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException {
		AppUpdateFileTypeEnum fileTypeEnum = AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
		if (fileTypeEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		if (appUpdate.getId() != null) {
			AppUpdate dbInfo = this.getAppUpdateById(appUpdate.getId());
			if (!AppUpdateStatusEnum.INIT.getStatus().equals(dbInfo.getStatus())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}

		AppUpdateQuery updateQuery = new AppUpdateQuery();
		updateQuery.setOrderBy("id desc");
		updateQuery.setSimplePage(new SimplePage(0, 1));
		List<AppUpdate> appUpdateList = appUpdateMapper.selectList(updateQuery);

		if (!appUpdateList.isEmpty()) {
			AppUpdate latest = appUpdateList.get(0);
			Long dbVersion = Long.parseLong(latest.getVersion().replace(".", ""));
			Long currentVersion = Long.parseLong(appUpdate.getVersion().replace(".", ""));

			if (appUpdate.getId() == null && currentVersion <= dbVersion) {
				throw new BusinessException("当前版本必须大于历史版本");
			}

			if (appUpdate.getId() != null && currentVersion <= dbVersion && appUpdate.getId().equals(latest.getId())) {
				throw new BusinessException("当前版本必须大于历史版本");
			}


			AppUpdate versionDb = appUpdateMapper.selectByVersion(appUpdate.getVersion ());
			if (appUpdate.getId() != null && versionDb != null && versionDb.getId().equals(appUpdate.getId())){
				throw new BusinessException("版本号已存在");
			}


		}

		if (appUpdate.getId() == null) {
			appUpdate.setCreateTime(new Date());
			appUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
			appUpdateMapper.insert(appUpdate);
		} else {
			appUpdateMapper.updateById(appUpdate, appUpdate.getId());
		}

		if (fileTypeEnum == AppUpdateFileTypeEnum.LOCAL && file != null) {
			String baseFolder = appconfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER;
			File targetFileFolder = new File(baseFolder);
			if (!targetFileFolder.exists()) {
				targetFileFolder.mkdirs();
			}
			String filePath = targetFileFolder.getPath() + "/" + appUpdate.getId() + Constants.APP_EXE_SUFFIX;
			file.transferTo(new File(filePath));
		}
	}

	@Override
	public void postUpdate(Integer id, Integer status, String grayscaleUid) {
		AppUpdateStatusEnum statusEnum = AppUpdateStatusEnum.getByStatus(status);

		if (statusEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		if (AppUpdateStatusEnum.GRAYSCALE == statusEnum && StringTools.isEmpty(grayscaleUid)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		if (AppUpdateStatusEnum.GRAYSCALE != statusEnum) {
			grayscaleUid = "";
		}

		AppUpdate update = new AppUpdate();
		update.setStatus(status);
		update.setGrayscaleUid(grayscaleUid);

		appUpdateMapper.updateById(update, id);
	}


    @Override
    public AppUpdate getLatestUpdate(String appVersion, String uid) {
        return appUpdateMapper.selectLatestUpdate(appVersion,uid);
    }
}