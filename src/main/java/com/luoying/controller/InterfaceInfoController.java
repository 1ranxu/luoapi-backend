package com.luoying.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.annotation.AuthCheck;
import com.luoying.client.LuoApiClient;
import com.luoying.common.*;
import com.luoying.constant.CommonConstant;
import com.luoying.constant.UserConstant;
import com.luoying.exception.BusinessException;
import com.luoying.model.dto.interfaceinfo.*;
import com.luoying.model.entity.InterfaceInfo;
import com.luoying.model.enums.InterfaceInfoStatusEnum;
import com.luoying.model.request.LuoApiRequest;
import com.luoying.model.vo.UserVO;
import com.luoying.service.ApiService;
import com.luoying.service.InterfaceInfoService;
import com.luoying.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 接口信息管理
 *
 * @author 落樱的悔恨
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private ApiService apiService;

    // region 增删改查

    /**
     * 接口创建（仅管理员可创建）
     *
     * @param interfaceInfoAddRequest 接口创建请求
     * @param request                 http请求
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        // 判空
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 拷贝
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        if (CollectionUtils.isNotEmpty(interfaceInfoAddRequest.getRequestParams())) {// 解析请求参数
            List<RequestParamsField> requestParamsFields = interfaceInfoAddRequest.getRequestParams().stream().filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String requestParams = JSONUtil.toJsonStr(requestParamsFields);
            interfaceInfo.setRequestParams(requestParams);
        }
        if (CollectionUtils.isNotEmpty(interfaceInfoAddRequest.getResponseParams())) {// 解析响应参数
            List<ResponseParamsField> responseParamsFields = interfaceInfoAddRequest.getResponseParams().stream().filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String responseParams = JSONUtil.toJsonStr(responseParamsFields);
            interfaceInfo.setResponseParams(responseParams);
        }
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // validInterfaceInfo的第二个参数为true代表新增
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        // 获取登录用户
        UserVO loginUser = userService.getLoginUser(request);
        // 设置创建人id
        interfaceInfo.setUserId(loginUser.getId());
        // 插入数据库
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        // 返回接口id
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 接口删除（仅管理员可删除）
     *
     * @param deleteRequest 接口删除请求
     * @param request       http请求
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验请求
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断要删除的接口是否存在、
        long id = deleteRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {// 不存在就抛异常
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 删除
        boolean result = interfaceInfoService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 接口更新（仅管理员可更新）
     *
     * @param interfaceInfoUpdateRequest 接口更新请求
     * @param request                    http请求
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest, HttpServletRequest request) {
        // 校验请求
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 拷贝
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        if (CollectionUtils.isNotEmpty(interfaceInfoUpdateRequest.getRequestParams())) {// 解析请求参数
            List<RequestParamsField> requestParamsFields = interfaceInfoUpdateRequest.getRequestParams().stream()
                    .filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String requestParams = JSONUtil.toJsonStr(requestParamsFields);
            interfaceInfo.setRequestParams(requestParams);
        }
        if (CollectionUtils.isNotEmpty(interfaceInfoUpdateRequest.getResponseParams())) {// 解析响应参数
            List<ResponseParamsField> responseParamsFields = interfaceInfoUpdateRequest.getResponseParams().stream()
                    .filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String responseParams = JSONUtil.toJsonStr(responseParamsFields);
            interfaceInfo.setResponseParams(responseParams);
        }
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        // 判断接口是否存在
        long id = interfaceInfoUpdateRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 更新
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 更新接口头像url
     *
     * @param interfaceInfoUpdateAvatarRequest 接口头像url更新请求
     * @param request                          http请求
     */
    @PostMapping("/updateInterfaceInfoAvatar")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfoAvatarUrl(@RequestBody InterfaceInfoUpdateAvatarRequest interfaceInfoUpdateAvatarRequest,
                                                              HttpServletRequest request) {
        // 校验请求
        if (interfaceInfoUpdateAvatarRequest == null || interfaceInfoUpdateAvatarRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 拷贝
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateAvatarRequest, interfaceInfo);
        // 更新
        return ResultUtils.success(interfaceInfoService.updateById(interfaceInfo));
    }

    /**
     * 根据 id 获取接口
     *
     * @param id 接口Id
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        return ResultUtils.success(interfaceInfo);
    }

    /**
     * 获取接口列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest 获取接口列表请求
     */
    @GetMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        // 不为空就拷贝，增加一些查询条件；为空，就查询所有
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取接口列表
     *
     * @param interfaceInfoQueryRequest 分页获取接口列表请求
     * @param request                   http请求
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取分页参数
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 获取排序参数
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        // 获取查询参数
        String name = interfaceInfoQueryRequest.getName();
        String url = interfaceInfoQueryRequest.getUrl();
        Long reduceScore = interfaceInfoQueryRequest.getReduceScore();
        String description = interfaceInfoQueryRequest.getDescription();
        String method = interfaceInfoQueryRequest.getMethod();
        Integer status = interfaceInfoQueryRequest.getStatus();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name)
                .like(StringUtils.isNotBlank(url), "url", url)
                .eq(Objects.nonNull(reduceScore), "reduceScore", reduceScore)
                .like(StringUtils.isNotBlank(description), "description", description)
                .like(StringUtils.isNotBlank(method), "method", method)
                .like(Objects.nonNull(status), "status", status);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        // 非管理员只能看到已发布的接口
        if (!userService.isAdmin(request)) {
            List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords().stream()
                    .filter(interfaceInfo -> interfaceInfo.getStatus().equals(InterfaceInfoStatusEnum.ONLINE.getValue()))
                    .collect(Collectors.toList());
            interfaceInfoPage.setRecords(interfaceInfoList);
        }
        return ResultUtils.success(interfaceInfoPage);
    }


    /**
     * 接口发布
     *
     * @param idRequest id请求
     * @param request   http请求
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        // 参数校验
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断接口是否存在
        long id = idRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //todo 判断该接口是否可以调用，固定方法名改为根据测试地址来调用

        // 修改接口数据库中接口的状态字段为1
        oldInterfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean result = interfaceInfoService.updateById(oldInterfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 接口下线
     *
     * @param idRequest id请求
     * @param request   http请求
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        // 参数校验
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        // 判断接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 修改接口数据库中接口的状态字段为0
        oldInterfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(oldInterfaceInfo);
        return ResultUtils.success(result);
    }
    // endregion

    /**
     * 接口调用
     *
     * @param interfaceInfoInvokeRequest 接口调用请求
     * @param request                    http请求
     * @return
     */
    @PostMapping("/invoke")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request) {
        // 参数校验
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断接口是否存在
        long id = interfaceInfoInvokeRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 判断接口状态
        if (interfaceInfo.getStatus().equals(InterfaceInfoStatusEnum.OFFLINE.getValue())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口未开放");
        }
        // 构建请求参数
        Map<String, Object> params = new HashMap<>();
        List<InterfaceInfoInvokeRequest.Field> fieldList = interfaceInfoInvokeRequest.getRequestParams();
        if (fieldList != null) {
            for (InterfaceInfoInvokeRequest.Field field : fieldList) {
                params.put(field.getFieldName(), field.getValue());
            }
        }

        // 调用
        UserVO loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        try {
            LuoApiClient luoApiClient = new LuoApiClient(accessKey, secretKey);
            LuoApiRequest luoApiRequest = new LuoApiRequest();
            luoApiRequest.setMethod(interfaceInfo.getMethod());
            luoApiRequest.setPath(interfaceInfo.getUrl());
            luoApiRequest.setRequestParams(params);
            com.luoying.model.response.BaseResponse baseResponse = apiService.request(luoApiClient, luoApiRequest);
            return ResultUtils.success(baseResponse.getData());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
