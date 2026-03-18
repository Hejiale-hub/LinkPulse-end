package com.hejiale.controller;


import com.hejiale.domain.po.User;
import com.hejiale.domain.vo.Result;
import com.hejiale.domain.vo.UserLoginVO;
import com.hejiale.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 学员用户表 前端控制器
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    private final IUserService userService;
    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody User user) {
        UserLoginVO userLoginVO = userService.login(user);
        return Result.success(userLoginVO);
    }
}
