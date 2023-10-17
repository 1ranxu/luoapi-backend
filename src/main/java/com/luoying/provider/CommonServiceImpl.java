package com.luoying.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.common.ErrorCode;
import com.luoying.exception.BusinessException;
import com.luoying.mapper.InterfaceInfoMapper;
import com.luoying.mapper.UserInterfaceInfoMapper;
import com.luoying.mapper.UserMapper;
import com.luoying.model.entity.InterfaceInfo;
import com.luoying.model.entity.User;
import com.luoying.model.entity.UserInterfaceInfo;
import com.luoying.service.UserInterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@DubboService
public class CommonServiceImpl implements CommonService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Override
    public User getInvokeUser(String accessKey) {
        if (StringUtils.isBlank(accessKey)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("accessKey", accessKey);
        User user = userMapper.selectOne(wrapper);
        if (user == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户不存在");
        }
        return user;
    }

    @Override
    public InterfaceInfo getInvokeInterfaceInfo(String method, String url) {
        if (StringUtils.isAnyBlank(method,url)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("method", method);
        wrapper.eq("url", url);
        InterfaceInfo interfaceInfo = interfaceInfoMapper.selectOne(wrapper);
        if (interfaceInfo == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口不存在");
        }
        return interfaceInfo;
    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserInterfaceInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("interfaceInfoId", interfaceInfoId);
        wrapper.eq("userId", userId);
        wrapper.gt("leftNum", 0);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoMapper.selectOne(wrapper);

        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"无可用次数");
        }
        userInterfaceInfo.setLeftNum(userInterfaceInfo.getLeftNum() - 1);
        userInterfaceInfo.setInvokedNum(userInterfaceInfo.getInvokedNum() + 1);

        if (userInterfaceInfoMapper.updateById(userInterfaceInfo) <1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"无可用次数");
        }
        return true;
    }
}




