package com.soul.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.dto.resp.AdminDashboardResp;
import com.soul.social.entity.DynamicComment;
import com.soul.social.entity.DynamicContent;
import com.soul.social.entity.ImMessage;
import com.soul.social.entity.ReportRecord;
import com.soul.social.entity.User;
import com.soul.social.mapper.DynamicCommentMapper;
import com.soul.social.mapper.DynamicContentMapper;
import com.soul.social.mapper.ImMessageMapper;
import com.soul.social.mapper.ReportRecordMapper;
import com.soul.social.mapper.UserMapper;
import com.soul.social.service.AdminDashboardService;
import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 后台数据看板服务实现。多表聚合统计今日/总量。
 */
@Slf4j
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private DynamicContentMapper dynamicContentMapper;

    @Resource
    private DynamicCommentMapper dynamicCommentMapper;

    @Resource
    private ImMessageMapper imMessageMapper;

    @Resource
    private ReportRecordMapper reportRecordMapper;

    @Override
    public AdminDashboardResp summary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        AdminDashboardResp resp = new AdminDashboardResp();

        // 用户
        resp.setUserTotal(userMapper.selectCount(null));
        resp.setUserToday(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)));

        // 动态
        resp.setDynamicTotal(dynamicContentMapper.selectCount(null));
        resp.setDynamicToday(dynamicContentMapper.selectCount(
                new LambdaQueryWrapper<DynamicContent>().ge(DynamicContent::getCreateTime, todayStart)));

        // 评论
        resp.setCommentTotal(dynamicCommentMapper.selectCount(null));
        resp.setCommentToday(dynamicCommentMapper.selectCount(
                new LambdaQueryWrapper<DynamicComment>().ge(DynamicComment::getCreateTime, todayStart)));

        // 聊天消息
        resp.setMessageTotal(imMessageMapper.selectCount(null));
        resp.setMessageToday(imMessageMapper.selectCount(
                new LambdaQueryWrapper<ImMessage>().ge(ImMessage::getCreateTime, todayStart)));

        // 举报
        resp.setReportTotal(reportRecordMapper.selectCount(null));
        resp.setReportToday(reportRecordMapper.selectCount(
                new LambdaQueryWrapper<ReportRecord>().ge(ReportRecord::getCreateTime, todayStart)));
        resp.setReportPending(reportRecordMapper.selectCount(
                new LambdaQueryWrapper<ReportRecord>()
                        .eq(ReportRecord::getStatus, CommonConstant.REPORT_STATUS_PENDING)));

        return resp;
    }
}
