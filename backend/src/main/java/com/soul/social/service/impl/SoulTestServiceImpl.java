package com.soul.social.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soul.social.common.constant.CommonConstant;
import com.soul.social.common.exception.BizException;
import com.soul.social.common.exception.ResultCode;
import com.soul.social.context.UserContext;
import com.soul.social.dto.req.SoulTestSubmitReq;
import com.soul.social.dto.resp.SoulQuestionListResp;
import com.soul.social.dto.resp.SoulTestResultResp;
import com.soul.social.entity.SoulTestOption;
import com.soul.social.entity.SoulTestQuestion;
import com.soul.social.entity.SoulTestResult;
import com.soul.social.entity.User;
import com.soul.social.entity.UserSoulTestRecord;
import com.soul.social.mapper.SoulTestOptionMapper;
import com.soul.social.mapper.SoulTestQuestionMapper;
import com.soul.social.mapper.SoulTestResultMapper;
import com.soul.social.mapper.UserMapper;
import com.soul.social.mapper.UserSoulTestRecordMapper;
import com.soul.social.service.SoulTestService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 灵魂测试服务实现。
 * <p>结果计算规则：按选项 result_code 累加 score，取得分最高的 result_code。
 * <p>（API 文档未写死映射规则，采用"按 result_code 分组累加 score、取最高分"的合理方案，
 * 与种子数据 E/I/S/M 四种编码对应。）
 */
@Slf4j
@Service
public class SoulTestServiceImpl implements SoulTestService {

    @Resource
    private SoulTestQuestionMapper soulTestQuestionMapper;

    @Resource
    private SoulTestOptionMapper soulTestOptionMapper;

    @Resource
    private SoulTestResultMapper soulTestResultMapper;

    @Resource
    private UserSoulTestRecordMapper userSoulTestRecordMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public SoulQuestionListResp questions() {
        List<SoulTestQuestion> questions = soulTestQuestionMapper.selectList(
                new LambdaQueryWrapper<SoulTestQuestion>()
                        .eq(SoulTestQuestion::getStatus, CommonConstant.STATUS_ENABLE)
                        .orderByAsc(SoulTestQuestion::getSort));
        if (questions.isEmpty()) {
            throw new BizException(ResultCode.TEST_QUESTION_EMPTY);
        }
        List<Long> questionIds = questions.stream().map(SoulTestQuestion::getId).toList();
        List<SoulTestOption> options = soulTestOptionMapper.selectList(
                new LambdaQueryWrapper<SoulTestOption>()
                        .in(SoulTestOption::getQuestionId, questionIds)
                        .orderByAsc(SoulTestOption::getSort));
        Map<Long, List<SoulTestOption>> optionsByQuestion = options.stream()
                .collect(Collectors.groupingBy(SoulTestOption::getQuestionId));

        SoulQuestionListResp resp = new SoulQuestionListResp();
        List<SoulQuestionListResp.QuestionItem> items = new ArrayList<>();
        for (SoulTestQuestion q : questions) {
            SoulQuestionListResp.QuestionItem item = new SoulQuestionListResp.QuestionItem();
            item.setQuestionId(q.getId());
            item.setTitle(q.getTitle());
            item.setDescription(q.getDescription());
            item.setSort(q.getSort());
            List<SoulQuestionListResp.OptionItem> optionItems = new ArrayList<>();
            for (SoulTestOption opt : optionsByQuestion.getOrDefault(q.getId(), List.of())) {
                SoulQuestionListResp.OptionItem oi = new SoulQuestionListResp.OptionItem();
                oi.setOptionId(opt.getId());
                oi.setContent(opt.getContent());
                oi.setSort(opt.getSort());
                // 不返回 resultCode / score（防作弊）
                optionItems.add(oi);
            }
            item.setOptions(optionItems);
            items.add(item);
        }
        resp.setQuestions(items);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoulTestResultResp submit(SoulTestSubmitReq req) {
        Long userId = UserContext.requireUserId();

        // 1. 取所有启用题目
        List<SoulTestQuestion> allQuestions = soulTestQuestionMapper.selectList(
                new LambdaQueryWrapper<SoulTestQuestion>()
                        .eq(SoulTestQuestion::getStatus, CommonConstant.STATUS_ENABLE));
        if (allQuestions.isEmpty()) {
            throw new BizException(ResultCode.TEST_QUESTION_EMPTY);
        }
        Set<Long> requiredQuestionIds = allQuestions.stream()
                .map(SoulTestQuestion::getId).collect(Collectors.toSet());

        // 2. 校验所有题必答
        Map<Long, Long> answerMap = new HashMap<>();
        for (SoulTestSubmitReq.AnswerItem answer : req.getAnswers()) {
            if (answer.getQuestionId() == null || answer.getOptionId() == null) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "答案项不完整");
            }
            answerMap.put(answer.getQuestionId(), answer.getOptionId());
        }
        if (!answerMap.keySet().containsAll(requiredQuestionIds)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "请完成所有题目后再提交");
        }

        // 3. 取选项，累加 score by resultCode
        List<Long> optionIds = new ArrayList<>(new HashSet<>(answerMap.values()));
        List<SoulTestOption> options = soulTestOptionMapper.selectBatchIds(optionIds);
        Map<Long, SoulTestOption> optionMap = options.stream()
                .collect(Collectors.toMap(SoulTestOption::getId, o -> o, (a, b) -> a));

        Map<String, Integer> scoreMap = new HashMap<>();
        for (Map.Entry<Long, Long> entry : answerMap.entrySet()) {
            SoulTestOption opt = optionMap.get(entry.getValue());
            if (opt == null) {
                throw new BizException(ResultCode.PARAM_ERROR.getCode(), "选项不存在");
            }
            String code = StrUtil.blankToDefault(opt.getResultCode(), "M");
            int score = opt.getScore() == null ? 0 : opt.getScore();
            scoreMap.merge(code, score, Integer::sum);
        }

        // 4. 取最高分 result_code（并列时按字典序取第一个，保证稳定）
        String resultCode = scoreMap.entrySet().stream()
                .max((a, b) -> {
                    int cmp = Integer.compare(a.getValue(), b.getValue());
                    if (cmp != 0) {
                        return cmp;
                    }
                    // 并列时取字典序较小者（反向比较以让较小 code 胜出）
                    return b.getKey().compareTo(a.getKey());
                })
                .map(Map.Entry::getKey)
                .orElse("M");

        // 5. 保存记录（唯一 user_id，先删后插保留最后一次）
        userSoulTestRecordMapper.delete(
                new LambdaQueryWrapper<UserSoulTestRecord>().eq(UserSoulTestRecord::getUserId, userId));
        UserSoulTestRecord record = new UserSoulTestRecord();
        record.setUserId(userId);
        record.setResultCode(resultCode);
        record.setAnswerJson(JSONUtil.toJsonStr(req.getAnswers()));
        record.setScoreJson(JSONUtil.toJsonStr(scoreMap));
        userSoulTestRecordMapper.insert(record);

        // 6. 置 test_flag=1
        User update = new User();
        update.setId(userId);
        update.setTestFlag(CommonConstant.YES);
        userMapper.updateById(update);

        log.info("用户 {} 提交灵魂测试，结果编码 {}，得分 {}", userId, resultCode, scoreMap);
        return buildResultResp(resultCode);
    }

    @Override
    public SoulTestResultResp myResult() {
        Long userId = UserContext.requireUserId();
        UserSoulTestRecord record = userSoulTestRecordMapper.selectOne(
                new LambdaQueryWrapper<UserSoulTestRecord>()
                        .eq(UserSoulTestRecord::getUserId, userId)
                        .last("LIMIT 1"));
        if (record == null || StrUtil.isBlank(record.getResultCode())) {
            throw new BizException(ResultCode.PROFILE_INCOMPLETE.getCode(), "尚未完成灵魂测试");
        }
        return buildResultResp(record.getResultCode());
    }

    private SoulTestResultResp buildResultResp(String resultCode) {
        SoulTestResult result = soulTestResultMapper.selectOne(
                new LambdaQueryWrapper<SoulTestResult>()
                        .eq(SoulTestResult::getResultCode, resultCode)
                        .last("LIMIT 1"));
        SoulTestResultResp resp = new SoulTestResultResp();
        resp.setResultCode(resultCode);
        if (result != null) {
            resp.setName(result.getName());
            resp.setIcon(result.getIcon());
            resp.setDescription(result.getDescription());
            resp.setSocialAdvice(result.getSocialAdvice());
        }
        return resp;
    }
}
