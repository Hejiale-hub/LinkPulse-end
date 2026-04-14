package com.hejiale.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("repository")
@ApiModel(value="Repository对象", description="AI会话记录表")
public class Repository {
    @ApiModelProperty(value = "主键ID，自增")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "模型类型（如：chat、service、pdf）")
    @TableField("type")
    private String type;

    @ApiModelProperty(value = "链接标题（方便后台管理显示）")
    @TableField("chat_id")
    private String chatId;

    @ApiModelProperty(value = "用户id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "会话标题")
    @TableField("session_title")
    private String sessionTitle;

    @ApiModelProperty(value = "文件名称（仅pdf类型会有）")
    @TableField("file_name")
    private String fileName;

    @ApiModelProperty(value = "文件链接（仅pdf类型会有）")
    @TableField("file_address")
    private String fileAddress;
}
