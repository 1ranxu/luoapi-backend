package com.luoying.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 * @author 落樱的悔恨
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户角色: user, admin，ban
     */
    private String userRole;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 积分余额
     */
    private Long score;

    private static final long serialVersionUID = 1L;
}