package com.luoying.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.common.ErrorCode;
import com.luoying.constant.RedisKey;
import com.luoying.constant.UserConstant;
import com.luoying.exception.BusinessException;
import com.luoying.mapper.UserMapper;
import com.luoying.model.dto.user.*;
import com.luoying.model.entity.User;
import com.luoying.model.vo.UserVO;
import com.luoying.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.luoying.constant.RedisKey.EMAIL_CAPTCHA_KEY;
import static com.luoying.constant.UserConstant.*;


/**
 * 用户服务实现类
 *
 * @author 落樱的悔恨
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 普通账号注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userName = userRegisterRequest.getUserName();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 1. 校验
        if (StringUtils.isAnyBlank(userName, userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() < 4 || userName.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称长度介于4~15位");
        }
        if (userAccount.length() < 4 || userAccount.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度介于4~15位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8 || userPassword.length() > 16 || checkPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度介于8~16位");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 昵称、账号、密码只能由英文字母大小写、数字组成
        String regex = "^[a-zA-Z0-9]+$";
        if (!userName.matches(regex) || !userAccount.matches(regex) || !userPassword.matches(regex) || !checkPassword.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称、账号、密码只能由英文字母大小写、数字组成");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 分配accessKey，secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userPassword + RandomUtil.randomNumbers(5));
            // 4. 插入数据
            User user = new User();
            user.setUserName(userName);
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 邮箱账号注册
     */
    @Override
    public long userEmailRegister(UserEmailRegisterRequest userEmailRegisterRequest) {
        String userName = userEmailRegisterRequest.getUserName();
        String emailAccount = userEmailRegisterRequest.getEmailAccount();
        String captcha = userEmailRegisterRequest.getCaptcha();
        // 1. 校验
        if (StringUtils.isAnyBlank(userName, emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() < 4 || userName.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称长度介于4~15位");
        }
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!emailAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }

        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(EMAIL_CAPTCHA_KEY, emailAccount));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误");
        }
        synchronized (emailAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", emailAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已经被注册了");
            }
            // 2. 分配accessKey，secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userName + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + emailAccount + RandomUtil.randomNumbers(5));
            // 4. 插入数据
            User user = new User();
            user.setUserName(userName);
            user.setUserAccount(emailAccount);
            user.setEmail(emailAccount);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 普通账号登录
     *
     * @return 脱敏后的用户信息
     */
    @Override
    public UserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度介于4~15位");
        }
        if (userPassword.length() < 8 || userPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度介于8~16位");
        }
        // 账号、密码只能由英文字母大小写、数字组成
        String regex = "^[a-zA-Z0-9]+$";
        if (!userAccount.matches(regex) || !userPassword.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号、密码只能由英文字母大小写、数字组成");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        request.getSession().setAttribute(USER_LOGIN_STATE, userVO);
        return userVO;
    }

    /**
     * 邮箱账号登录
     *
     * @return 脱敏后的用户信息
     */
    @Override
    public UserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request) {
        String emailAccount = userEmailLoginRequest.getEmailAccount();
        String captcha = userEmailLoginRequest.getCaptcha();
        // 1. 校验
        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 校验邮箱
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!emailAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        // 校验验证码
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(EMAIL_CAPTCHA_KEY, emailAccount));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码输入有误");
        }
        // 2. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", emailAccount);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该邮箱未绑定账号，请先绑定账号");
        }
        // 账号被封禁
        if (UserConstant.BAN_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已封禁");
        }
        // 3. 记录用户的登录态
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        request.getSession().setAttribute(USER_LOGIN_STATE, userVO);
        return userVO;
    }

    /**
     * 用户登出
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取当前登录用户
     */
    @Override
    public UserVO getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVO currentUser = (UserVO) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        User user = this.getById(userId);
        // 登录用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 账号被封禁
        if (UserConstant.BAN_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已封禁");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 是否为管理员
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVO userVO = (UserVO) userObj;
        return userVO != null && ADMIN_ROLE.equals(userVO.getUserRole());
    }

    /**
     * 更新凭证
     */
    @Override
    public UserVO updateVoucher(UserVO loginUser) {
        // 重新生成凭证
        String accessKey = DigestUtil.md5Hex(SALT + loginUser.getAccessKey() + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + loginUser.getSecretKey() + RandomUtil.randomNumbers(5));
        loginUser.setAccessKey(accessKey);
        loginUser.setSecretKey(secretKey);
        // 更新
        User user = BeanUtil.copyProperties(loginUser, User.class);
        boolean result = this.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return loginUser;
    }

    /**
     * 绑定邮箱
     */
    @Override
    public UserVO userBindEmail(UserBindEmailRequest userBindEmailRequest, UserVO loginUser) {
        String emailAccount = userBindEmailRequest.getEmailAccount();
        String captcha = userBindEmailRequest.getCaptcha();
        // 1. 校验
        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 校验邮箱
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!emailAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        // 校验验证码
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(EMAIL_CAPTCHA_KEY, emailAccount));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码输入有误");
        }
        // 判断用户是否重复绑定相同邮箱
        if (loginUser.getEmail() != null && emailAccount.equals(loginUser.getEmail())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号已绑定此邮箱,请使用其他的邮箱！");
        }
        // 判断该邮箱是否已经被他人绑定
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", emailAccount);
        User user = this.getOne(queryWrapper);
        if (user != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "此邮箱已被他人绑定,请使用其他的邮箱！");
        }
        // 2. 绑定邮箱
        user = new User();
        user.setId(loginUser.getId());
        user.setEmail(emailAccount);
        boolean result = this.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱绑定失败,请稍后再试！");
        }
        loginUser.setEmail(emailAccount);
        return loginUser;
    }

    /**
     * 解除邮箱绑定
     */
    @Override
    public UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, UserVO loginUser) {
        String emailAccount = userUnBindEmailRequest.getEmailAccount();
        String captcha = userUnBindEmailRequest.getCaptcha();
        // 1. 校验
        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验邮箱
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!emailAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        // 校验验证码
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(EMAIL_CAPTCHA_KEY, emailAccount));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码输入有误");
        }
        // 判断用户是否绑定该邮箱
        if (loginUser.getEmail() == null || !emailAccount.equals(loginUser.getEmail())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号未绑定此邮箱");
        }
        // 解除绑定
        User user = new User();
        user.setId(loginUser.getId());
        user.setEmail("");
        boolean bindEmailResult = this.updateById(user);
        if (!bindEmailResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱解绑失败,请稍后再试！");
        }
        loginUser.setEmail("");
        return loginUser;
    }
}




