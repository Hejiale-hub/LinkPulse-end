-- 访客按日汇总表：由定时任务从 Redis 落库（执行一次即可）
CREATE TABLE IF NOT EXISTS visitor_day_stat (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    ip VARCHAR(45) NOT NULL COMMENT '客户端 IP（IPv4/IPv6）',
    visit_date DATE NOT NULL COMMENT '统计自然日（Asia/Shanghai）',
    visit_count BIGINT NOT NULL DEFAULT 0 COMMENT '当日访问次数',
    PRIMARY KEY (id),
    UNIQUE KEY uk_visitor_ip_date (ip, visit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客按日访问次数汇总';
