package com.ndz.tirana.dto.sys;

import lombok.Data;

import java.util.Date;

@Data
public class SaveSysRoleDTO {
    private Long id;
    private String roleName;
    private String roleCode;
    private Date createTime;
}
