package org.clever.security.service;

import lombok.extern.slf4j.Slf4j;
import org.clever.common.exception.BusinessException;
import org.clever.security.dto.request.*;
import org.clever.security.dto.response.RoleBindPermissionRes;
import org.clever.security.dto.response.UserBindRoleRes;
import org.clever.security.dto.response.UserBindSysRes;
import org.clever.security.entity.Permission;
import org.clever.security.entity.Role;
import org.clever.security.mapper.RoleMapper;
import org.clever.security.mapper.UserMapper;
import org.clever.security.service.internal.RoleBindPermissionService;
import org.clever.security.service.internal.UserBindRoleService;
import org.clever.security.service.internal.UserBindSysNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-10-03 21:24 <br/>
 */
@Transactional(readOnly = true)
@Service
@Slf4j
public class ManageBySecurityService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserBindSysNameService userBindSysNameService;
    @Autowired
    private UserBindRoleService userBindRoleService;
    @Autowired
    private RoleBindPermissionService resetRoleBindPermission;
    @Autowired
    private ManageByQueryService manageByQueryService;

    @Transactional
    public List<UserBindSysRes> userBindSys(UserBindSysReq userBindSysReq) {
        // 校验用户全部存在
        for (String username : userBindSysReq.getUsernameList()) {
            int count = userMapper.existsByUserName(username);
            if (count <= 0) {
                throw new BusinessException("用户[" + username + "]不存在");
            }
        }
        // 校验系统全部存在
        List<String> allSysName = manageByQueryService.allSysName();
        for (String sysName : userBindSysReq.getSysNameList()) {
            if (!allSysName.contains(sysName)) {
                throw new BusinessException("系统[" + sysName + "]不存在");
            }
        }
        List<UserBindSysRes> result = new ArrayList<>();
        // 设置用户绑定的系统
        for (String username : userBindSysReq.getUsernameList()) {
            userBindSysNameService.resetUserBindSys(username, userBindSysReq.getSysNameList());
            List<String> sysNameList = userMapper.findSysNameByUsername(username);
            UserBindSysRes userBindSysRes = new UserBindSysRes();
            userBindSysRes.setUsername(username);
            userBindSysRes.setSysNameList(sysNameList);
            result.add(userBindSysRes);
        }
        return result;
    }

    @Transactional
    public List<UserBindRoleRes> userBindRole(UserBindRoleReq userBindSysReq) {
        // 校验用户全部存在
        for (String username : userBindSysReq.getUsernameList()) {
            int count = userMapper.existsByUserName(username);
            if (count <= 0) {
                throw new BusinessException("用户[" + username + "]不存在");
            }
        }
        List<UserBindRoleRes> result = new ArrayList<>();
        // 设置用户绑定的系统
        for (String username : userBindSysReq.getUsernameList()) {
            userBindRoleService.resetUserBindRole(username, userBindSysReq.getRoleNameList());
            List<Role> roleList = userMapper.findRoleByUsername(username);
            UserBindRoleRes userBindRoleRes = new UserBindRoleRes();
            userBindRoleRes.setUsername(username);
            userBindRoleRes.setRoleList(roleList);
            result.add(userBindRoleRes);
        }
        return result;
    }

    @Transactional
    public List<RoleBindPermissionRes> roleBindPermission(RoleBindPermissionReq roleBindPermissionReq) {
        // 校验角色全部存在
        for (String roleName : roleBindPermissionReq.getRoleNameList()) {
            int count = roleMapper.existsByRole(roleName);
            if (count <= 0) {
                throw new BusinessException("角色[" + roleName + "]不存在");
            }
        }
        List<RoleBindPermissionRes> result = new ArrayList<>();
        // 设置用户绑定的系统
        for (String roleName : roleBindPermissionReq.getRoleNameList()) {
            resetRoleBindPermission.resetRoleBindPermission(roleName, roleBindPermissionReq.getPermissionStrList());
            List<Permission> permissionList = roleMapper.findPermissionByRoleName(roleName);
            RoleBindPermissionRes roleBindPermissionRes = new RoleBindPermissionRes();
            roleBindPermissionRes.setRoleName(roleName);
            roleBindPermissionRes.setPermissionList(permissionList);
            result.add(roleBindPermissionRes);
        }
        return result;
    }

    @Transactional
    public UserBindSysRes userBindSys(UserSysReq userSysReq) {
//        // 校验系统全部存在
//        List<String> allSysName = manageByQueryService.allSysName();
//        for (String sysName : userBindSysReq.getSysNameList()) {
//            if (!allSysName.contains(sysName)) {
//                throw new BusinessException("系统[" + sysName + "]不存在");
//            }
//        }
        // TODO 为用户添加登录的系统
        return null;
    }

    @Transactional
    public UserBindSysRes userUnBindSys(UserSysReq userSysReq) {
        // TODO 为用户删除登录的系统
        return null;
    }

    @Transactional
    public UserBindRoleRes userBindRole(UserRoleReq userRoleReq) {
        int count = userMapper.existsUserRole(userRoleReq.getUsername(), userRoleReq.getRoleName());
        if (count >= 1) {
            throw new BusinessException("用户[" + userRoleReq.getUsername() + "]已经拥有角色[" + userRoleReq.getRoleName() + "]");
        }
        userMapper.addRole(userRoleReq.getUsername(), userRoleReq.getRoleName());
        UserBindRoleRes res = new UserBindRoleRes();
        res.setUsername(userRoleReq.getUsername());
        res.setRoleList(userMapper.findRoleByUsername(userRoleReq.getUsername()));
        // TODO 更新Session
        return res;
    }

    @Transactional
    public UserBindRoleRes userUnBindRole(UserRoleReq userRoleReq) {
        int count = userMapper.existsUserRole(userRoleReq.getUsername(), userRoleReq.getRoleName());
        if (count <= 0) {
            throw new BusinessException("用户[" + userRoleReq.getUsername() + "]未拥有角色[" + userRoleReq.getRoleName() + "]");
        }
        userMapper.delRole(userRoleReq.getUsername(), userRoleReq.getRoleName());
        UserBindRoleRes res = new UserBindRoleRes();
        res.setUsername(userRoleReq.getUsername());
        res.setRoleList(userMapper.findRoleByUsername(userRoleReq.getUsername()));
        // TODO 更新Session
        return res;
    }

    @Transactional
    public RoleBindPermissionRes roleBindPermission(RolePermissionReq rolePermissionReq) {
        int count = roleMapper.existsRolePermission(rolePermissionReq.getRoleName(), rolePermissionReq.getPermissionStr());
        if (count >= 1) {
            throw new BusinessException("角色[" + rolePermissionReq.getRoleName() + "]已经拥有权限[" + rolePermissionReq.getPermissionStr() + "]");
        }
        roleMapper.addPermission(rolePermissionReq.getRoleName(), rolePermissionReq.getPermissionStr());
        RoleBindPermissionRes res = new RoleBindPermissionRes();
        res.setRoleName(rolePermissionReq.getRoleName());
        res.setPermissionList(roleMapper.findPermissionByRoleName(rolePermissionReq.getRoleName()));
        // TODO 更新Session
        return res;
    }

    @Transactional
    public RoleBindPermissionRes roleUnBindPermission(RolePermissionReq rolePermissionReq) {
        int count = roleMapper.existsRolePermission(rolePermissionReq.getRoleName(), rolePermissionReq.getPermissionStr());
        if (count <= 0) {
            throw new BusinessException("角色[" + rolePermissionReq.getRoleName() + "]未拥有权限[" + rolePermissionReq.getPermissionStr() + "]");
        }
        roleMapper.delPermission(rolePermissionReq.getRoleName(), rolePermissionReq.getPermissionStr());
        RoleBindPermissionRes res = new RoleBindPermissionRes();
        res.setRoleName(rolePermissionReq.getRoleName());
        res.setPermissionList(roleMapper.findPermissionByRoleName(rolePermissionReq.getRoleName()));
        // TODO 更新Session
        return res;
    }
}
