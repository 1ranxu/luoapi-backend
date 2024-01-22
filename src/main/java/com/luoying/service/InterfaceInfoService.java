package com.luoying.service;

import com.luoying.model.entity.InterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 落樱的悔恨
* @description 针对表【interface_info】的数据库操作Service
* @createDate 2023-10-05 12:30:49
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验
     *
     * @param interfaceInfo 接口信息
     * @param add 是否为创建校验
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
