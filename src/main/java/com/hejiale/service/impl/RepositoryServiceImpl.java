package com.hejiale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.esotericsoftware.minlog.Log;
import com.hejiale.common.context.UserContext;
import com.hejiale.common.domain.vo.SessionUtilVO;
import com.hejiale.common.exception.BaseException;
import com.hejiale.common.util.AliOssUtil;
import com.hejiale.common.util.SessionUtils;
import com.hejiale.domain.po.Repository;
import com.hejiale.domain.vo.SessionVO;
import com.hejiale.mapper.RepositoryMapper;
import com.hejiale.service.IRepositoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class RepositoryServiceImpl extends ServiceImpl<RepositoryMapper, Repository> implements IRepositoryService {
    private final AliOssUtil aliOssUtil;
    private final VectorStore vectorStore;
    /*
     *保存会话历史id
     */
    @Override
    public void save(String type, String chatId, String sessionTitle) {
        // 封装到实体类，并保存到数据库
        Repository repository = new Repository();
        repository.setType(type);
        repository.setChatId(chatId);
        repository.setUserId(UserContext.getUserId());
        repository.setSessionTitle(sessionTitle);
        save(repository);
    }

    ;

    @Override
    public List<SessionVO> getChatIds() {
        return lambdaQuery()
                .eq(Repository::getUserId, UserContext.getUserId())
                .list()
                .stream()
                .map(repository -> {
                    SessionVO sessionVO = new SessionVO();
                    sessionVO.setChatId(repository.getChatId());
                    sessionVO.setType(repository.getType());
                    sessionVO.setSessionTitle(repository.getSessionTitle());
                    return sessionVO;
                })
                .toList();
    }

    @Override
    public void deleteByChatId(String chatId) {
        // 先删向量库，确保会话删除时不会残留向量数据
        try {
            vectorStore.delete(new FilterExpressionTextParser().parse("chatId == '" + chatId + "'"));
            log.info("向量库删除完成: chatId={}", chatId);
        } catch (Exception e) {
            log.error("向量库删除失败: chatId={}, error={}", chatId, e.getMessage(), e);
            throw new BaseException("删除会话向量数据失败: " + e.getMessage());
        }

        // 删除mysql数据库中的会话记录
        lambdaUpdate()
                .eq(Repository::getChatId, chatId)
                .remove();
        log.info("会话记录删除完成: chatId={}", chatId);
    }

    @Override
    public void updateSessionTitle(String chatId, String sessionTitle) {
        Repository repository = new Repository();
        repository.setSessionTitle(sessionTitle);
        lambdaUpdate()
                .eq(Repository::getChatId, chatId)
                .update(repository);
    }

    @Override
    public SessionVO createChat(String type) {
        // 生成唯一的会话ID和默认的会话标题的vo对象
        SessionUtilVO sessionUtilsVO = SessionUtils.generateChatId(UserContext.getUserId());
        // 保存会话
        save(type, sessionUtilsVO.getChatId(), sessionUtilsVO.getSessionTitle());
        SessionVO sessionVO = new SessionVO();
        sessionVO.setChatId(sessionUtilsVO.getChatId());
        sessionVO.setSessionTitle(sessionUtilsVO.getSessionTitle());
        sessionVO.setType(type);
        return sessionVO;
    }

    @Override
    public void savePdfFile(String chatId, MultipartFile file) throws IOException {
        try {
            // 校验是否是PDF文件
            String fileName = file.getOriginalFilename();
            if (fileName != null && !fileName.endsWith(".pdf")) {
                throw new IllegalArgumentException("只能上传PDF文件");
            }
//            // 上传文件到阿里云OSS
//            String fileAddress = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename() + "/" + chatId);
//            if (fileAddress == null) {
//                throw new RuntimeException("文件上传失败");
//            }
            // 保存aliyun文件地址到数据库 todo 后续可以使用mq异步处理，提升接口响应速度
            Repository repository = new Repository();
            repository.setFileName(fileName);
            boolean update = lambdaUpdate()
                    .eq(Repository::getChatId, chatId)
                    .update(repository);
            if (!update) {
                throw new RuntimeException("保存文件名称失败");
            }
            // 写入向量数据库
            log.info("开始写入向量数据库，文件名：{}", fileName);
            // 写入向量数据库时同时传入chatId，作为metadata的一部分存储，以便后续查询时可以根据chatId进行过滤
            writeToVectorStore(file.getResource(), chatId);
        } catch (Exception e) {
            throw new BaseException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String getPdfAddress(String chatId) {
        Repository repository = lambdaQuery()
                .eq(Repository::getChatId, chatId)
                .one();
        if (repository != null) {
            return repository.getFileAddress();
        }
        return "";
    }

    @Override
    public String getPdfName(String chatId) {
        Repository repository = lambdaQuery()
                .eq(Repository::getChatId, chatId)
                .one();
        if (repository != null) {
            return repository.getFileName();
        }
        throw new BaseException("没有pdf文件");
    }

    private void writeToVectorStore(Resource resource, String chatId) {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)
                        .build()
        );
        List<Document> documents = reader.read();
        documents.forEach(document -> {
            document.getMetadata().put("chatId", chatId);
        });
        vectorStore.add(documents);
        log.info("向量写入完成: chatId={}, chunkCount={}", chatId, documents.size());
        logVectorStoreHealth(chatId);
    }

    // 向量库自检方法，验证向量库中是否正确存储了数据，并且可以根据chatId进行过滤查询
    private void logVectorStoreHealth(String chatId) {
        try {
            List<Document> anyHits = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query("pdf")
                            .topK(3)
                            .build()
            );
            log.info("向量库自检(无过滤): hitCount={}", anyHits == null ? 0 : anyHits.size());

            List<Document> chatHits = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query("pdf")
                            .topK(3)
                            .filterExpression("chatId == '" + chatId + "'")
                            .build()
            );
            log.info("向量库自检(chatId过滤): chatId={}, hitCount={}", chatId, chatHits == null ? 0 : chatHits.size());

            if (chatHits != null && !chatHits.isEmpty()) {
                Document first = chatHits.get(0);
                log.info("向量库命中样本metadata: {}", first.getMetadata());
            }
        } catch (Exception e) {
            log.error("向量库自检失败: chatId={}, error={}", chatId, e.getMessage(), e);
        }
    }
}

