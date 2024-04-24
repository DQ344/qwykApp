package com.qywk.user.pojo.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author qlh
 * @date 2024/04/18 15:18
 * @description  用户信息
 */
@Data
@TableName("user_info")
public class UserInfoDTO {

    /**
     * 用户名
     * */
    @TableId(value = "user_id", type = IdType.ASSIGN_UUID)
    private String userId;

    /**
     * 用户密码
     * */
    @TableField("password")
    private String password;

    /**
     * 用户的昵称
     * */
    @TableField("nickname")
    private String nickname;

    /**
     * 用户的微信授权码
     * */
    @TableField("open_id")
    private String openId;

    /**
     * 用户的微信会话码
     * */
    @TableField("session_id")
    private String sessionId;

    /**
     * 电话
     * */
    @TableField("tel")
    private String tel;

    /**
     * 邮箱
     * */
    @TableField("email")
    private String email;

    /**
     * 用户的头像图片名
     * */
    @TableField("profile")
    private String profile;

    /**
     * 创建时间
     * */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否激活
     * */
    @TableField(value = "enable", fill = FieldFill.INSERT)
    private String enable;

    /**
     * 用户经验值
     */
    @TableField("exp")
    private Integer exp;

    /**
     * 货币
     */
    @TableField("currency")
    private Integer currency;
}

