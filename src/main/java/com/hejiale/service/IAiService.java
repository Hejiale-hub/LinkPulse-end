package com.hejiale.service;

import com.hejiale.domain.vo.AiMessageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IAiService {
    List<AiMessageVO> chat(String prompt, String chatId, List<MultipartFile> files);

    List<AiMessageVO> service(String prompt, String chatId);

    List<AiMessageVO> pdf(String prompt, String chatId);
}
