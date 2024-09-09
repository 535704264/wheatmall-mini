package com.ndz.tirana.exception;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson2.JSON;
import com.ndz.tirana.config.security.UserSessionContext;
import com.ndz.tirana.entity.base.SysErrorLogEntity;
import com.ndz.tirana.service.sys.SysErrorLogService;
import com.ndz.tirana.vo.sys.SysUserVO;
import com.ndz.tirana.common.enums.BizCodeEnum;
import com.ndz.tirana.common.bean.ApiResult;
import com.ndz.tirana.common.enums.StateEnum;
import com.ndz.tirana.utils.ApiResultUtils;
import com.ndz.tirana.utils.ExceptionUtils;
import com.ndz.tirana.utils.HttpContextUtils;
import com.ndz.tirana.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cn.hutool.core.map.MapUtil;




/**
 * 全局异常处理器
 *
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

	@Autowired
	private SysErrorLogService logErrorService;

	/**
	 * 处理自定义异常
	 */
	@ExceptionHandler(ApiException.class)
	public ApiResult<String> handleRenException(ApiException ex){
		log.error(ex.getMsg(), ex);
		return ApiResultUtils.error(ex.getCode(), ex.getMsg(), ex.getMessage());
	}

	/**
	 * 专处理麦子业务异常
	 */
	@ExceptionHandler(WheatException.class)
	public ApiResult<String> handleRenException(WheatException ex){
		log.error(ex.getMessage(), ex);
		return ApiResultUtils.error(ex.getMessage());
	}


	/**
	 * 数据库记录已存在冲突异常
	 */
	@ExceptionHandler(DuplicateKeyException.class)
	public ApiResult<String> handleDuplicateKeyException(DuplicateKeyException ex){
		return ApiResultUtils.error(BizCodeEnum.DB_RECORD_EXISTS.getMsg());
	}

	/**
	 * spring security异常
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseBody
	public ApiResult<String> error(AccessDeniedException ex) throws AccessDeniedException {
		return ApiResultUtils.error(StateEnum.NO_PERMISSION,null);
	}

	/**
	 * 处理其他未知异常
	 *
	 * @param ex
	 * @return {@link ApiResult}<{@link String}>
	 */
	@ExceptionHandler(Exception.class)
	public ApiResult<String> handleException(Exception ex){
		log.error(ex.getMessage(), ex);
		// 保存异常日志
		// TODO 配置中自定义开启/关闭落库
		saveLog(ex);
		// TODO 根据不通环境看是否要展示给前端错误msg
		return ApiResultUtils.error(ex.getMessage());
	}

	/**
	 * 保存其他未知异常日志
	 */
	private void saveLog(Exception ex){
		SysErrorLogEntity errorLog = new SysErrorLogEntity();

		//请求相关信息
		HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
		errorLog.setIp(IpUtils.getIpAddr(request));
		errorLog.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
		errorLog.setRequestUri(request.getRequestURI());
		errorLog.setRequestMethod(request.getMethod());
		errorLog.setCreateDate(new Date());
		Map<String, String> params = HttpContextUtils.getParameterMap(request);
		if(MapUtil.isNotEmpty(params)){
			errorLog.setRequestParams(JSON.toJSONString(params));
		}
		SysUserVO sysUserVO = UserSessionContext.get();
		if (sysUserVO!=null && sysUserVO.getName() != null) {
			errorLog.setCreator(sysUserVO.getName());
		}

		//异常信息
		errorLog.setErrorInfo(ExceptionUtils.getErrorStackTrace(ex));

		//保存
		logErrorService.save(errorLog);
	}
}