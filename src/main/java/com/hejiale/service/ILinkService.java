package com.hejiale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hejiale.domain.dto.CreateLinkDTO;
import com.hejiale.domain.po.Link;
import com.hejiale.domain.vo.LinkCodeVO;
import com.hejiale.domain.vo.MonitorListDetialsVO;
import com.hejiale.domain.vo.MonitorListVO;

import java.util.List;

/**
 * <p>
 * 短链接核心信息表 服务类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
public interface ILinkService extends IService<Link> {

    List<LinkCodeVO> createShortLink(CreateLinkDTO createLinkDTO);

    List<MonitorListVO<MonitorListDetialsVO>> getLinkMonitorList();
}
