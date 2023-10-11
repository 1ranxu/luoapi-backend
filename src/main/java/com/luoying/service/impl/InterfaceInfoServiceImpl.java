package com.luoying.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.common.ErrorCode;
import com.luoying.exception.BusinessException;
import com.luoying.mapper.InterfaceInfoMapper;
import com.luoying.model.entity.InterfaceInfo;
import com.luoying.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author 落樱的悔恨
 * @description 针对表【interface_info】的数据库操作Service实现
 * @createDate 2023-10-05 12:30:49
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        Long id = interfaceInfo.getId();
        String name = interfaceInfo.getName();

        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isAnyBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口名称过长");
        }
    }
}




