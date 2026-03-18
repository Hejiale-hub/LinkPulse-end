package com.hejiale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hejiale.domain.po.User;
import com.hejiale.domain.vo.UserLoginVO;

/**
 * <p>
 * 学员用户表 服务类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
public interface IUserService extends IService<User> {

    void register(User user);

    UserLoginVO login(User user);
}
