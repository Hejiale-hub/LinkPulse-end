package com.hejiale.controller;


import com.hejiale.domain.dto.CreateLinkDTO;
import com.hejiale.domain.vo.LinkCodeVO;
import com.hejiale.domain.vo.Result;
import com.hejiale.service.ILinkService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 短链接核心信息表 前端控制器
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
@Api(tags = "link相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/link")
public class LinkController {
    private final ILinkService shortLinkService;
    /**
     * 创建短链接
     * @param createLinkDTO 创建短链接参数
     * @return 短链接列表
     */
    @PostMapping("/createCode")
    public Result<List<LinkCodeVO>> createShortLink(@RequestBody CreateLinkDTO createLinkDTO) {
        List<LinkCodeVO> linkCodeVOList = shortLinkService.createShortLink(createLinkDTO);
        return Result.success(linkCodeVOList);
    }
}
