package com.ndz.tirana.vo.sys;

import lombok.Data;

import java.util.Date;

@Data
public class SysRoleVO {
    private Long id;
    private String roleName;
    private String roleCode;
    private Date createTime;
}
