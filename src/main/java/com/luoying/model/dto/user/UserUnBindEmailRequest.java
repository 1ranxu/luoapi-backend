package com.luoying.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 解除邮箱绑定请求
 */
@Data
public class UserUnBindEmailRequest implements Serializable {
    /**
     * 邮箱
     */
    private String emailAccount;

    /**
     * 验证码
     */
    private String captcha;

    private static final long serialVersionUID = 1L;
}
