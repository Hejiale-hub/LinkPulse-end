package com.hejiale.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hejiale.domain.po.Link;
import com.hejiale.mapper.LinkMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

public class RedirectController {
    @Autowired
    private LinkMapper linkMapper;

    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        // 1. 查询数据库（后面我们要在这里加 Redis 缓存！）
        LambdaQueryWrapper<Link> query = new LambdaQueryWrapper<>();
        query.eq(Link::getLinkCode, shortCode).eq(Link::getIsActive, 1);
        Link link = linkMapper.selectOne(query);

        if (link != null) {
            // 2. 异步记录访问日志 (先留个坑，后面写异步逻辑)
            //todo recordLogAsync(shortCode);

            // 3. 执行 302 重定向
            response.sendRedirect(link.getOriginalUrl());
        } else {
            response.sendError(404, "链接已失效或不存在");
        }
    }
}
