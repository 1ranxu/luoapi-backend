package com.luoying.provider;


import com.luoying.model.entity.InterfaceInfo;
import com.luoying.model.entity.User;

/**
 * 提供给其他项目的公共服务
 */
public interface CommonService {
    /**
     * 根据accessKey查询数据库，是否存在包含该accessKey的用户
     *
     * @param accessKey 公钥
     */
    User getInvokeUser(String accessKey);

    /**
     * 从数据库中查询接口是否存在（请求方法，请求路径）
     *
     * @param method 请求方法
     * @param url    请求路径
     */
    InterfaceInfo getInvokeInterfaceInfo(String method, String url);

    /**
     * 接口调用次数统计
     *
     * @param interfaceInfoId 接口id
     * @param userId 用户id
     */
    boolean invokeCount(long userId, long interfaceInfoId, long reduceScore);
}
