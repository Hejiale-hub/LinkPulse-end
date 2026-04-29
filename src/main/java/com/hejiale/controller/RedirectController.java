package com.hejiale.controller;

import com.hejiale.service.ILinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {
    private final ILinkService LinkService;

    /**
     * 访问短链接，重定向到原始URL
     * @param linkCode 短链接code
     * @param request 请求对象
     * @param response 响应对象
     */
    @GetMapping("/{linkCode}")
    public void redirect(@PathVariable(value = "linkCode") String linkCode, HttpServletRequest request, HttpServletResponse response) throws NotFoundException {
        // 记录业务接口响应时间
        long startTime = System.currentTimeMillis();

        // 获取原始URL
        String url = LinkService.redirect(linkCode, request, response);
        // 302 重定向
        log.info("重定向...");
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", url);

        long endTime = System.currentTimeMillis();
        log.info("接口响应时间：{}ms", endTime - startTime);
    }
}
