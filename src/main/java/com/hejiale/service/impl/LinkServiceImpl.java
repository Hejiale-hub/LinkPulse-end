package com.hejiale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hejiale.common.exception.CreateLinkCodeException;
import com.hejiale.common.util.LinkUtils;
import com.hejiale.domain.dto.CreateLinkDTO;
import com.hejiale.domain.po.Link;
import com.hejiale.domain.vo.LinkCodeVO;
import com.hejiale.mapper.LinkMapper;
import com.hejiale.service.ILinkService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * 短链接核心信息表 服务实现类
 * </p>
 *
 * @author hejiale
 * @since 2026-03-17
 */
@Service
public class LinkServiceImpl extends ServiceImpl<LinkMapper, Link> implements ILinkService {
    @Transactional
    @Override
    public List<LinkCodeVO> createShortLink(CreateLinkDTO createLinkDTO) {
        // 属性拷贝
        Link link = new Link();
        BeanUtils.copyProperties(createLinkDTO, link);
        // 保存linkCode对象到数据库，获取自增ID
        boolean result = save(link);
        if (!result) {
            throw new CreateLinkCodeException("创建链接码失败");
        }
        // 根据ID生成linkcode
        String linkCode = LinkUtils.encode(link.getId());
        // 更新短码回数据库
        link.setLinkCode(linkCode);
        updateById(link);
        // 封装
        LinkCodeVO linkCodeVO = new LinkCodeVO();
        List<LinkCodeVO> linkCodeVOList = new ArrayList<>();
        linkCodeVO.setLinkTitle(createLinkDTO.getLinkTitle());
        linkCodeVO.setLinkCode(linkCode);
        linkCodeVOList.add(linkCodeVO);
        linkCodeVOList.add(linkCodeVO);
        linkCodeVOList.add(linkCodeVO);
        return linkCodeVOList;
    }

}
