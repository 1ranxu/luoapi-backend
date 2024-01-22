package com.luoying.controller;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.luoying.annotation.AuthCheck;
import com.luoying.common.*;
import com.luoying.constant.EmailConstant;
import com.luoying.constant.RedisKey;
import com.luoying.constant.UserConstant;
import com.luoying.exception.BusinessException;
import com.luoying.model.dto.user.*;
import com.luoying.model.entity.User;
import com.luoying.model.vo.UserVO;
import com.luoying.service.UserService;
import com.luoying.utils.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.luoying.constant.RedisKey.EMAIL_CAPTCHA_KEY;
import static com.luoying.constant.UserConstant.SALT;

/**
 * 用户接口
 *
 * @author 落樱的悔恨
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // region 登录相关

    /**
     * 普通账号注册
     *
     * @param userRegisterRequest 普通账号注册请求
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 判空
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 普通账号注册
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 邮箱账号注册
     *
     * @param userEmailRegisterRequest 邮箱账号注册请求
     */
    @PostMapping("/email/register")
    public BaseResponse<Long> userEmailRegister(@RequestBody UserEmailRegisterRequest userEmailRegisterRequest) {
        // 判空
        if (userEmailRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 邮箱账号注册
        long result = userService.userEmailRegister(userEmailRegisterRequest);
        // redis删除验证码缓存
        stringRedisTemplate.delete(RedisKey.getKey(EMAIL_CAPTCHA_KEY, userEmailRegisterRequest.getEmailAccount()));
        return ResultUtils.success(result);
    }

    /**
     * 获取验证码
     *
     * @param emailAccount 邮箱账号
     */
    @GetMapping("/getCaptcha")
    public BaseResponse<Boolean> getCaptcha(@RequestParam("emailAccount") String emailAccount) {
        if (StringUtils.isBlank(emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!emailAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        // 获取验证码 存入redis
        String captcha = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue()
                .set(RedisKey.getKey(EMAIL_CAPTCHA_KEY, emailAccount), captcha, 5, TimeUnit.MINUTES);
        // 发送邮件
        try {
            sendEmail(emailAccount, captcha);
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("【发送验证码失败】" + e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码获取失败");
        }
    }

    private void sendEmail(String emailAccount, String captcha) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        // 邮箱发送内容组成
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        // 邮件主题
        helper.setSubject(EmailConstant.EMAIL_SUBJECT);
        // 正文
        helper.setText(EmailUtil.buildEmailContent(EmailConstant.EMAIL_HTML_CONTENT_PATH, captcha), true);
        // 收件人
        helper.setTo(emailAccount);
        // 发件人
        helper.setFrom(EmailConstant.EMAIL_TITLE + '<' + "1574925401@qq.com" + '>');
        mailSender.send(message);
    }


    /**
     * 普通账号登录
     *
     * @param userLoginRequest 普通账号登录请求
     * @param request          http请求
     */
    @PostMapping("/login")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 判空
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 普通账号登录
        UserVO userVO = userService.userLogin(userLoginRequest, request);
        return ResultUtils.success(userVO);
    }

    /**
     * 邮箱账号登录
     *
     * @param userEmailLoginRequest 邮箱账号登录
     * @param request               http请求
     */
    @PostMapping("/email/login")
    public BaseResponse<UserVO> userEmailLogin(@RequestBody UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request) {
        // 判空
        if (userEmailLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 邮箱账号登录
        UserVO user = userService.userEmailLogin(userEmailLoginRequest, request);
        // redis删除验证码缓存
        stringRedisTemplate.delete(RedisKey.getKey(EMAIL_CAPTCHA_KEY, userEmailLoginRequest.getEmailAccount()));
        return ResultUtils.success(user);
    }

    /**
     * 用户登出
     *
     * @param request http请求
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        // 判空
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登出
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request http请求
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        return ResultUtils.success(userService.getLoginUser(request));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest 创建用户请求
     * @param request        http请求
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        // 校验
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 拷贝
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 保存
        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 删除用户请求
     * @param request       http请求
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 删除
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest 更新用户请求
     * @param request           http请求
     */
    @PostMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        UserVO loginUser = userService.getLoginUser(request);
        if (!userUpdateRequest.getId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人或管理员可以修改");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        // 加密
        if (StringUtils.isNotBlank(userUpdateRequest.getUserPassword())) {
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userUpdateRequest.getUserPassword()).getBytes());
            user.setUserPassword(encryptPassword);
        }
        // 更新
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取用户
     *
     * @param id      用户id
     * @param request http请求
     */
    @GetMapping("/get")
    public BaseResponse<UserVO> getUserById(int id, HttpServletRequest request) {
        // 校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询
        User user = userService.getById(id);
        // 拷贝
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 获取用户列表
     *
     * @param userQueryRequest 获取用户列表请求
     * @param request          http请求
     */
    @GetMapping("/list")
    public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        // 判空
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 拷贝
        User userQuery = new User();
        BeanUtils.copyProperties(userQueryRequest, userQuery);
        // 查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = userService.list(queryWrapper);
        // 脱敏
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(userVOList);
    }

    /**
     * 分页获取用户列表
     *
     * @param userQueryRequest 分页获取用户列表请求
     * @param request          http请求
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        // 判空
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取参数
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String email = userQueryRequest.getEmail();
        Long score = userQueryRequest.getScore();
        Integer gender = userQueryRequest.getGender();
        String userRole = userQueryRequest.getUserRole();
        // 条件构造器
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName)
                .eq(StringUtils.isNotBlank(userAccount), "userAccount", userAccount)
                .eq(StringUtils.isNotBlank(email), "email", email)
                .eq(Objects.nonNull(score), "score", score)
                .eq(Objects.nonNull(gender), "gender", gender)
                .eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        // 分页查询
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);
        // 脱敏
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新凭证
     *
     * @param request http请求
     */
    @PostMapping("/update/voucher")
    public BaseResponse<UserVO> updateVoucher(HttpServletRequest request) {
        // 判空
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        UserVO loginUser = userService.getLoginUser(request);
        // 更新凭证
        UserVO userVO = userService.updateVoucher(loginUser);
        return ResultUtils.success(userVO);
    }

    /**
     * 解封
     *
     * @param idRequest id请求
     */
    @PostMapping("/normal")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> normalUser(@RequestBody IdRequest idRequest) {
        // 校验
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据id查询用户
        Long id = idRequest.getId();
        User user = userService.getById(id);
        if (user == null) {// 用户不存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 用户角色设置为user
        user.setUserRole(UserConstant.DEFAULT_ROLE);
        return ResultUtils.success(userService.updateById(user));
    }

    /**
     * 封号
     *
     * @param idRequest id请求
     */
    @PostMapping("/ban")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> banUser(@RequestBody IdRequest idRequest) {
        // 校验
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据Id查询用户
        Long id = idRequest.getId();
        User user = userService.getById(id);
        if (user == null) {// 用户不存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 用户角色设置为ban
        user.setUserRole(UserConstant.BAN_ROLE);
        return ResultUtils.success(userService.updateById(user));
    }

    /**
     * 绑定邮箱
     *
     * @param userBindEmailRequest 绑定邮箱请求
     * @param request              http请求
     */
    @PostMapping("/email/bind")
    public BaseResponse<UserVO> userBindEmail(@RequestBody UserBindEmailRequest userBindEmailRequest, HttpServletRequest request) {
        // 判空
        if (userBindEmailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        UserVO loginUser = userService.getLoginUser(request);
        // 绑定邮箱
        UserVO user = userService.userBindEmail(userBindEmailRequest, loginUser);
        return ResultUtils.success(user);
    }

    /**
     * 解除邮箱绑定
     *
     * @param userUnBindEmailRequest 解除邮箱绑定请求
     * @param request                http请求
     */
    @PostMapping("/email/unbind")
    public BaseResponse<UserVO> userUnBindEmail(@RequestBody UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request) {
        // 判空
        if (userUnBindEmailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        UserVO loginUser = userService.getLoginUser(request);
        // 解除邮箱绑定
        UserVO user = userService.userUnBindEmail(userUnBindEmailRequest, loginUser);
        // 删除验证码缓存
        stringRedisTemplate.delete(RedisKey.getKey(EMAIL_CAPTCHA_KEY, userUnBindEmailRequest.getEmailAccount()));
        return ResultUtils.success(user);
    }
}
