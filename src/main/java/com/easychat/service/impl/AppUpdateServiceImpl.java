package com.easychat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.easychat.entity.config.Appconfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.enums.AppUpdateFileTypeEnum;
import com.easychat.entity.enums.AppUpdateStatusEnum;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.exception.BusinessException;
import org.springframework.stereotype.Service;

import com.easychat.entity.enums.PageSize;
import com.easychat.entity.query.AppUpdateQuery;
import com.easychat.entity.po.AppUpdate;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.query.SimplePage;
import com.easychat.mappers.AppUpdateMapper;
import com.easychat.service.AppUpdateService;
import com.easychat.utils.StringTools;
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
		return this.appUpdateMapper.deleteById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException {
		AppUpdateFileTypeEnum fileTypeEnum = AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
		if (fileTypeEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		AppUpdateQuery updateQuery = new AppUpdateQuery();
		updateQuery.setOrderBy("version desc");
		updateQuery.setSimplePage(new SimplePage(0, 1));
		List<AppUpdate> appUpdateList = appUpdateMapper.selectList(updateQuery);

		if (!appUpdateList.isEmpty()) {
			AppUpdate latest = appUpdateList.get(0);
			Long dbVersion = Long.parseLong(latest.getVersion().replace(".", ""));
			Long currentVersion = Long.parseLong(appUpdate.getVersion().replace(".", ""));

			if (appUpdate.getId() == null && currentVersion <= dbVersion) {
				throw new BusinessException("当前版本必须大于历史版本");
			}

			if (appUpdate.getId() != null && currentVersion <= dbVersion && appUpdate.getVersion().equals(latest.getVersion())) {
				throw new BusinessException("当前版本必须大于历史版本");
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

}