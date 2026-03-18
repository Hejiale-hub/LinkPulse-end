package com.hejiale.controller;


import com.hejiale.domain.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 短链接访问日志表，记录每次点击详情，用于统计分析 前端控制器
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
@RestController
@RequestMapping("/link-access-log")
public class LinkAccessLogController {

}
