package com.luoying.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.luoying.common.BaseResponse;
import com.luoying.common.ErrorCode;
import com.luoying.common.ResultUtils;
import com.luoying.exception.BusinessException;
import com.luoying.model.entity.SignIn;
import com.luoying.model.entity.User;
import com.luoying.model.vo.UserVO;
import com.luoying.service.SignInService;
import com.luoying.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/1/20 17:32
 */
@RestController
@RequestMapping("/sign")
public class SignInController {
    @Resource
    private SignInService signInService;

    @Resource
    private UserService userService;

    @PostMapping("/doSignIn")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> doSignIn(HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        // 判断是否已签到
        QueryWrapper<SignIn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        SignIn signIn = signInService.getOne(queryWrapper);
        if (signIn != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "今日已签到");
        }
        // 未签到，插入数据库
        signIn = new SignIn();
        signIn.setUserId(loginUser.getId());
        signIn.setAddScores(10L);
        boolean save = signInService.save(signIn);
        // 添加积分
        User user = userService.getById(loginUser);
        user.setScore(user.getScore() + 10);
        boolean update = userService.updateById(user);
        if (!save || !update) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "签到失败");
        }
        return ResultUtils.success(true);
    }
}
