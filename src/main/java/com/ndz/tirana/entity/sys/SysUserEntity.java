package com.ndz.tirana.entity.sys;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户表
 */
@Data
@TableName("sys_user")
public class SysUserEntity {
    /**
     * 会员id
     */
    @TableId(type= IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机
     */
    private String phone;

    /**
     * 头像地址
     */
    private String headUrl;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 岗位id
     */
    private Long postId;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态（1：正常 0：停用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标记（0:可用 1:已删除）
     */
    private Integer isDeleted;
}