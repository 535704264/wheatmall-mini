/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.ndz.wheatmall.entity.base;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 异常日志
 */
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("base_log_error")
public class LogErrorEntity {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private Long id;


	/**
	 * 请求URI
	 */
	private String requestUri;
	/**
	 * 请求方式
	 */
	private String requestMethod;
	/**
	 * 请求参数
	 */
	private String requestParams;
	/**
	 * 用户代理
	 */
	private String userAgent;
	/**
	 * 操作IP
	 */
	private String ip;
	/**
	 * 异常信息
	 */
	private String errorInfo;

	/**
	 * 创建者
	 */
	@TableField(fill = FieldFill.INSERT)
	private Long  creator;
	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private Date createDate;

}