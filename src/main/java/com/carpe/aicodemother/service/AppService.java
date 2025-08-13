package com.carpe.aicodemother.service;

import com.carpe.aicodemother.model.dto.app.AppAddRequest;
import com.carpe.aicodemother.model.dto.app.AppQueryRequest;
import com.carpe.aicodemother.model.dto.app.AppUpdateRequest;
import com.carpe.aicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.carpe.aicodemother.model.entity.App;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author jaeger
 */
public interface AppService extends IService<App> {
    /**
     * 获取应用封装类
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构造应用查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


}
