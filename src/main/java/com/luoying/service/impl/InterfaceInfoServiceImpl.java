package com.luoying.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.common.ErrorCode;
import com.luoying.exception.BusinessException;
import com.luoying.mapper.InterfaceInfoMapper;
import com.luoying.model.entity.InterfaceInfo;
import com.luoying.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
        // 判空
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取参数
        String name = interfaceInfo.getName();
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        Long reduceScore = interfaceInfo.getReduceScore();
        String description = interfaceInfo.getDescription();
        if (add) {// 创建时，必要参数必须非空
            if (StringUtils.isAnyBlank(name,url,method)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口名称过长");
        }
        if (StringUtils.isNotBlank(url)) {
            interfaceInfo.setUrl(url.trim());
        }
        if (StringUtils.isNotBlank(method)) {
            interfaceInfo.setMethod(method.trim().toUpperCase());
        }
        if (Objects.nonNull(reduceScore) && reduceScore < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "扣减积分个数不能小于0");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口描述过长");
        }
    }
}




