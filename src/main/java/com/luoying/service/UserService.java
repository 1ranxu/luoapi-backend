package com.luoying.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.model.dto.user.*;
import com.luoying.model.entity.User;
import com.luoying.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 * @author 落樱的悔恨
 */
public interface UserService extends IService<User> {

    /**
     * 普通账号注册
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 邮箱账号注册
     */
    long userEmailRegister(UserEmailRegisterRequest userEmailRegisterRequest);

    /**
     * 普通账号登录
     *
     * @return 脱敏后的用户信息
     */
    UserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 邮箱账号登录
     *
     * @return 脱敏后的用户信息
     */
    UserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request);

    /**
     * 用户登出
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     */
    UserVO getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 更新凭证
     */
    UserVO updateVoucher(UserVO loginUser);

    /**
     * 绑定邮箱
     */
    UserVO userBindEmail(UserBindEmailRequest userBindEmailRequest, UserVO loginUser);

    /**
     * 解除邮箱绑定
     */
    UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, UserVO loginUser);
}
