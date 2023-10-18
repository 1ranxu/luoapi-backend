package com.luoying.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.luoying.annotation.AuthCheck;
import com.luoying.common.BaseResponse;
import com.luoying.common.ErrorCode;
import com.luoying.common.ResultUtils;
import com.luoying.exception.BusinessException;
import com.luoying.mapper.InterfaceInfoMapper;
import com.luoying.mapper.UserInterfaceInfoMapper;
import com.luoying.model.entity.InterfaceInfo;
import com.luoying.model.entity.UserInterfaceInfo;
import com.luoying.model.vo.InvokeInterfaceInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口统计分析
 *
 * @author 落樱的悔恨
 */
@RestController
@RequestMapping("/interfaceAnalysis")
@Slf4j
public class AnalysisController {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InvokeInterfaceInfoVO>> getTopInvokeInterfaceInfoList(){
        // 1. 查询调用数据
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.getTopInvokeInterfaceInfoList(10);
        // 2. 查询每个接口的信息
        Map<Long, List<UserInterfaceInfo>> map = userInterfaceInfoList.stream().collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        QueryWrapper<InterfaceInfo> wrapper=new QueryWrapper<>();
        wrapper.in("id",map.keySet());
        List<InterfaceInfo> interfaceInfoList = interfaceInfoMapper.selectList(wrapper);
        // 判空
        if (CollectionUtil.isEmpty(interfaceInfoList)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 3. 组合数据
        List<InvokeInterfaceInfoVO> invokeInterfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            InvokeInterfaceInfoVO invokeInterfaceInfoVO = BeanUtil.copyProperties(interfaceInfo, InvokeInterfaceInfoVO.class);
            Long invokedNum = map.get(interfaceInfo.getId()).get(0).getInvokedNum();
            invokeInterfaceInfoVO.setTotalInvokeNum(invokedNum);
            return invokeInterfaceInfoVO;
        }).collect(Collectors.toList());

        return ResultUtils.success(invokeInterfaceInfoVOList);
    }
}
