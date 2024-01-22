package com.luoying.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.luoying.common.ErrorCode;
import com.luoying.exception.BusinessException;
import com.luoying.mapper.InterfaceInfoMapper;
import com.luoying.mapper.UserInterfaceInfoMapper;
import com.luoying.mapper.UserMapper;
import com.luoying.model.entity.InterfaceInfo;
import com.luoying.model.entity.User;
import com.luoying.model.entity.UserInterfaceInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

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
        if (StringUtils.isBlank(accessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("accessKey", accessKey);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户不存在");
        }
        return user;
    }

    @Override
    public InterfaceInfo getInvokeInterfaceInfo(String method, String url) {
        if (StringUtils.isAnyBlank(method, url)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("method", method);
        wrapper.eq("url", url);
        InterfaceInfo interfaceInfo = interfaceInfoMapper.selectOne(wrapper);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口不存在");
        }
        return interfaceInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean invokeCount(long userId, long interfaceInfoId, long reduceScore) {
        if (userId <= 0 || interfaceInfoId <= 0 || reduceScore < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserInterfaceInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("interfaceInfoId", interfaceInfoId);
        wrapper.eq("userId", userId);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoMapper.selectOne(wrapper);
        // 用户接口调用表增加调用次数
        if (userInterfaceInfo == null) {// 为空则插入新记录
            userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
            userInterfaceInfo.setTotalInvokes(1L);
            userInterfaceInfoMapper.insert(userInterfaceInfo);
        } else {// 不为空则调用次数加1
            userInterfaceInfo.setTotalInvokes(userInterfaceInfo.getTotalInvokes() + 1);
            userInterfaceInfoMapper.updateById(userInterfaceInfo);
        }
        // 接口表增加调用次数
        InterfaceInfo interfaceInfo = interfaceInfoMapper.selectById(interfaceInfoId);
        interfaceInfo.setTotalInvokes(interfaceInfo.getTotalInvokes() + 1);
        interfaceInfoMapper.updateById(interfaceInfo);
        // 用户表扣除积分
        User user = userMapper.selectById(userId);
        user.setScore(user.getScore() - 1);
        userMapper.updateById(user);

        return true;
    }
}




