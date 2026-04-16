package com.hejiale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hejiale.common.Properties.JwtProperties;
import com.hejiale.common.exception.AccountException;
import com.hejiale.common.util.JwtUtils;
import com.hejiale.domain.po.User;
import com.hejiale.domain.vo.UserLoginVO;
import com.hejiale.mapper.UserMapper;
import com.hejiale.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 学员用户表 服务实现类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private final JwtProperties jwtProperties;

    @Override
    public void register(User user) {
        // 判断用户名和密码是否为空
        if (user.getAccount() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        // 校验用户名是否已存在
        boolean exists = lambdaQuery()
                .eq(User::getAccount, user.getAccount())
                .exists();
        if (exists) {
            throw new AccountException("账号已存在");
        }
        //设置默认用户名
        user.setUsername("LinkP用户" + System.currentTimeMillis());
        // 保存用户到数据库
        boolean save = save(user);
        if (!save) {
            throw new AccountException("注册失败");
        }
    }

    @Override
    public UserLoginVO login(User user) {
        // 判断用户名和密码是否为空
        if (user.getAccount() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        // 校验用户名和密码是否正确
        User dbUser = lambdaQuery()
                .eq(User::getAccount, user.getAccount())
                .eq(User::getPassword, user.getPassword())
                .one();
        if (dbUser == null) {
            throw new AccountException("账号或密码错误");
        }
        // 登录成功，下发JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", dbUser.getId());
        String token = JwtUtils.createJWT(
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl(),
                claims);
        log.info("下发JWT：{}，用户id：{}", token, dbUser.getId());

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(dbUser.getId())
                .userName(dbUser.getUsername())
                .token(token)
                .build();
        return userLoginVO;
    }
}













