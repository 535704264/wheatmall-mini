package com.ndz.tirana.service.sys.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.ndz.tirana.common.helper.MenuHelper;
import com.ndz.tirana.common.helper.RouterHelper;
import com.ndz.tirana.dao.sys.SysMenuDao;
import com.ndz.tirana.dao.sys.SysRoleMenuDao;
import com.ndz.tirana.entity.sys.SysMenuEntity;
import com.ndz.tirana.entity.sys.SysRoleMenuEntity;
import com.ndz.tirana.service.sys.SysMenuService;
import com.ndz.tirana.vo.sys.RouterVO;
import com.ndz.tirana.vo.sys.SysMenuVO;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ndz.tirana.common.enums.BizCodeEnum;
import com.ndz.tirana.dto.sys.AssginMenuDTO;
import com.ndz.tirana.dto.sys.SaveSysMenuDTO;
import com.ndz.tirana.exception.WheatException;
import com.ndz.tirana.service.base.impl.BaseServiceImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysMenuServiceImpl extends BaseServiceImpl<SysMenuDao, SysMenuEntity> implements SysMenuService {


    @Resource
    SysRoleMenuDao sysRoleMenuDao;

    @Override
    public List<SysMenuVO> findNodes() {
        //全部权限列表
        List<SysMenuEntity> sysMenuList = this.baseDao.selectList(null);
        if (CollUtil.isEmpty(sysMenuList)) return null;
        List<SysMenuVO> sysMenuVOS = BeanUtil.copyToList(sysMenuList, SysMenuVO.class);

        //构建树形数据
        List<SysMenuVO> result = MenuHelper.buildTree(sysMenuVOS);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(SaveSysMenuDTO permission) {
        SysMenuEntity sysMenuEntity = BeanUtil.copyProperties(permission, SysMenuEntity.class);
        insert(sysMenuEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateById(SaveSysMenuDTO permission) {
        SysMenuEntity sysMenuEntity = selectById(permission.getId());
        BeanUtil.copyProperties(permission, sysMenuEntity, "id");
        updateById(sysMenuEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeById(Long id) {
        Long count = this.baseDao.selectCount(new LambdaQueryWrapper<SysMenuEntity>().eq(SysMenuEntity::getParentId, id));
//        AssertUtil.isTrue(count < 0,"菜单不存在");
        if (count < 0) {
            throw new WheatException(BizCodeEnum.NODE_ERROR);
        }
        deleteById(id);
    }

    @Override
    public List<SysMenuVO> findSysMenuByRoleId(Long roleId) {
        // 获取所有status为1的菜单列表
        List<SysMenuEntity> menuList = this.baseDao.selectList(new QueryWrapper<SysMenuEntity>().eq("status", 1));
        List<SysMenuVO> sysMenuVOS = BeanUtil.copyToList(menuList, SysMenuVO.class);
        // 根据角色id获取角色权限
        List<SysRoleMenuEntity> roleMenus = sysRoleMenuDao.selectList(new QueryWrapper<SysRoleMenuEntity>().eq("role_id",roleId));
        // 获取该角色已分配的所有菜单id
        List<Long> roleMenuIds = new ArrayList<>();
        for (SysRoleMenuEntity roleMenu : roleMenus) {
            roleMenuIds.add(roleMenu.getMenuId());
        }
        // 遍历所有菜单列表
        for (SysMenuVO sysMenu : sysMenuVOS) {
            // if it is true then the menu will be selected
            sysMenu.setSelect(roleMenuIds.contains(sysMenu.getId()));
        }
        // 将权限列表转换为权限树
        List<SysMenuVO> sysMenus = MenuHelper.buildTree(sysMenuVOS);
        return sysMenus;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void doAssign(AssginMenuDTO dto) {
        // 删除已分配的权限
        sysRoleMenuDao.delete(new QueryWrapper<SysRoleMenuEntity>().eq("role_id",dto.getRoleId()));
        // 遍历所有已选择的权限id
        for (Long menuId : dto.getMenuIdList()) {
            if(menuId != null){
                // 创建SysRoleMenu对象
                SysRoleMenuEntity sysRoleMenu = new SysRoleMenuEntity();
                sysRoleMenu.setMenuId(menuId);
                sysRoleMenu.setRoleId(dto.getRoleId());
                // 添加新权限
                sysRoleMenuDao.insert(sysRoleMenu);
            }
        }
    }

    /*
     * 获取用户菜单
     */
    @Override
    public List<RouterVO> findUserMenuList(Long userId) {
        // 超级管理员admin账号id为：1
        List<SysMenuEntity> sysMenuList = null;
        if (userId == 1) {
            sysMenuList = this.baseDao.selectList(new LambdaQueryWrapper<SysMenuEntity>()
                    .eq(SysMenuEntity::getStatus, 1)
                    .orderByAsc(SysMenuEntity::getSortValue));
        } else {
            sysMenuList = this.baseDao.listByUserId(userId);
        }
        List<SysMenuVO> sysMenuVOS = BeanUtil.copyToList(sysMenuList, SysMenuVO.class);
        // 构建树形数据
        List<SysMenuVO> sysMenuTreeList = MenuHelper.buildTree(sysMenuVOS);
        // 构建路由
        List<RouterVO> routerVoList = RouterHelper.buildRouters(sysMenuTreeList);
        return routerVoList;
    }


    /**
     * 根据用户Id获取用户按钮的操作权限
     *
     * @param userId 用户id
     * @return {@link List}<{@link String}>
     */
    @Override
    public List<String> findUserPermsList(Long userId) {
        // 超级管理员admin账号id为：1
        List<SysMenuEntity> sysMenuList = null;
        if (userId == 1) {
            // 管理员取所有
            sysMenuList = this.baseDao.selectList(new LambdaQueryWrapper<SysMenuEntity>()
                    .eq(SysMenuEntity::getStatus, 1));
        } else {
            sysMenuList = this.baseDao.listByUserId(userId);
        }
        // 创建返回的集合
        List<String> permissionList = new ArrayList<>();
        for (SysMenuEntity sysMenu : sysMenuList) {
            // 按钮权限
            if(sysMenu.getType() == 2){
                permissionList.add(sysMenu.getPerms());
            }
        }
        return permissionList;
    }
}
