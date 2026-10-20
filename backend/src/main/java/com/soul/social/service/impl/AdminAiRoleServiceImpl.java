package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.common.result.PageResult;
import com.soul.social.dto.req.AdminAiRoleDeleteReq;
import com.soul.social.dto.req.AdminAiRoleListReq;
import com.soul.social.dto.req.AdminAiRoleSaveReq;
import com.soul.social.dto.resp.AdminAiRoleResp;
import com.soul.social.entity.AiRole;
import com.soul.social.mapper.AiRoleMapper;
import com.soul.social.service.AdminAiRoleService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台 AI 角色管理服务实现。本期仅 CRUD，不做真实 AI 调用。
 */
@Slf4j
@Service
public class AdminAiRoleServiceImpl implements AdminAiRoleService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private AiRoleMapper aiRoleMapper;

    @Override
    public PageResult<AdminAiRoleResp> rolePage(AdminAiRoleListReq req) {
        Page<AiRole> page = new Page<>(req.safePageNum(), req.safePageSize());
        Page<AiRole> result = aiRoleMapper.selectPage(page,
                new LambdaQueryWrapper<AiRole>()
                        .eq(req.getStatus() != null, AiRole::getStatus, req.getStatus())
                        .like(StrUtil.isNotBlank(req.getKeyword()), AiRole::getName, req.getKeyword())
                        .orderByAsc(AiRole::getSort)
                        .orderByDesc(AiRole::getId));

        List<AdminAiRoleResp> items = new ArrayList<>();
        for (AiRole role : result.getRecords()) {
            AdminAiRoleResp resp = new AdminAiRoleResp();
            resp.setRoleId(role.getId());
            resp.setName(role.getName());
            resp.setAvatar(role.getAvatar());
            resp.setWelcomeText(role.getWelcomeText());
            resp.setPrompt(role.getPrompt());
            resp.setStatus(role.getStatus());
            resp.setSort(role.getSort());
            resp.setCreateTime(formatTime(role.getCreateTime()));
            items.add(resp);
        }
        return PageResult.of(result, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(AdminAiRoleSaveReq req) {
        if (req.getRoleId() != null) {
            AiRole role = aiRoleMapper.selectById(req.getRoleId());
            if (role == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            role.setName(req.getName());
            role.setAvatar(StrUtil.nullToEmpty(req.getAvatar()));
            role.setWelcomeText(StrUtil.nullToEmpty(req.getWelcomeText()));
            role.setPrompt(StrUtil.nullToEmpty(req.getPrompt()));
            if (req.getStatus() != null) {
                role.setStatus(req.getStatus());
            }
            if (req.getSort() != null) {
                role.setSort(req.getSort());
            }
            aiRoleMapper.updateById(role);
        } else {
            AiRole role = new AiRole();
            role.setName(req.getName());
            role.setAvatar(StrUtil.nullToEmpty(req.getAvatar()));
            role.setWelcomeText(StrUtil.nullToEmpty(req.getWelcomeText()));
            role.setPrompt(StrUtil.nullToEmpty(req.getPrompt()));
            role.setStatus(req.getStatus() == null ? 1 : req.getStatus());
            role.setSort(req.getSort() == null ? 0 : req.getSort());
            aiRoleMapper.insert(role);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(AdminAiRoleDeleteReq req) {
        AiRole role = aiRoleMapper.selectById(req.getRoleId());
        if (role == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        // 逻辑删除
        aiRoleMapper.deleteById(req.getRoleId());
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(DATE_TIME_FORMATTER);
    }
}
