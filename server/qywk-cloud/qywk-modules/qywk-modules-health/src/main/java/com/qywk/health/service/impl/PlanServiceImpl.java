package com.qywk.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qywk.common.core.constant.EnableConstants;
import com.qywk.common.core.customenum.CodeStateEnum;
import com.qywk.common.core.entity.ResultBody;
import com.qywk.common.core.utils.PageUtils;
import com.qywk.common.redis.customenum.RedisKeyEnum;
import com.qywk.common.redis.service.RedisService;
import com.qywk.health.mapper.PlanSysMapper;
import com.qywk.health.mapper.PlanUserMapper;
import com.qywk.health.pojo.ao.PlanAO;
import com.qywk.health.pojo.ao.PlanSysAO;
import com.qywk.health.pojo.dto.PlanSysDTO;
import com.qywk.health.pojo.dto.PlanUserDTO;
import com.qywk.health.pojo.vo.PlanSysVO;
import com.qywk.health.service.PlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author qlh
 * @date 2024/04/23 15:09
 * @description
 */
@Service
public class PlanServiceImpl implements PlanService {

    @Autowired
    PlanUserMapper planUserMapper;

    @Autowired
    PlanSysMapper planSysMapper;

    @Autowired
    RedisService redisService;

    @Override
    public ResultBody querySysSubarea(String userId, Integer page, Integer size) {
        List<PlanSysDTO> list = planSysMapper.selectList(
                new QueryWrapper<PlanSysDTO>()
                        .eq("enable", EnableConstants.ACTIVATION)
                        .groupBy("subarea")
        );
        PageUtils<PlanSysDTO> pageUtils = new PageUtils<>(list, page, size);
        List<String> subareaList = new ArrayList<>();
        for(PlanSysDTO dto : pageUtils.getRecords()){
            subareaList.add(dto.getSubarea());
        }
        return ResultBody.ok().data("total", pageUtils.getTotal()).data("records", subareaList);
    }

    @Override
    public ResultBody querySysRecommend(String userId, Integer page, Integer size, String subarea) {
        List<PlanSysDTO> list = planSysMapper.selectList(
                new QueryWrapper<PlanSysDTO>()
                .eq("enable", EnableConstants.ACTIVATION)
                .eq("subarea", subarea)
                .in("user_id", userId, "sys")
        );
        PageUtils<PlanSysDTO> pageUtils = new PageUtils<>(list, page, size);
        List<PlanSysVO> records = new ArrayList<>();
        for(PlanSysDTO dto : pageUtils.getRecords()){
            PlanSysVO vo = new PlanSysVO();
            BeanUtils.copyProperties(dto, vo);
            records.add(vo);
        }
        return ResultBody.ok().data("total", pageUtils.getTotal()).data("records", records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultBody choose(String userId, List<PlanAO> aoList) {
        try {
            for (PlanAO ao : aoList){
                PlanUserDTO dto = new PlanUserDTO();
                BeanUtils.copyProperties(ao, dto);
                dto.setUserId(userId);
                planUserMapper.insert(dto);
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultBody.error().code(CodeStateEnum.ERROR.code).message(CodeStateEnum.ERROR.message);
        }
        return ResultBody.ok().message("创建计划成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultBody customize(String userId, PlanSysAO ao) {
        try {
            PlanSysDTO dto = new PlanSysDTO();
            BeanUtils.copyProperties(ao, dto);
            dto.setUserId(userId);
            planSysMapper.insert(dto);
        }catch (Exception e){
            e.printStackTrace();
            return ResultBody.error().code(CodeStateEnum.ERROR.code).message(CodeStateEnum.ERROR.message);
        }
        return ResultBody.ok().message("创建自定义计划成功");
    }

    @Override
    public ResultBody punch(String userId, String planId) {
        PlanUserDTO dto = planUserMapper.selectById(planId);
        if (dto == null) return ResultBody.error().message("打卡错误");
        if(!dto.getUserId().equals(userId)){
            return ResultBody.error().code(CodeStateEnum.AUTH_UNAUTHORIZED.code).message(CodeStateEnum.AUTH_UNAUTHORIZED.message);
        }
        String key = RedisKeyEnum.PLAN_PUNCH.create(planId);

        int punchSize = 0;
        if(redisService.hasKey(key)){
            punchSize = redisService.getCacheObject(key);
        }

        if(punchSize >= dto.getPunchSize()){
            return ResultBody.ok().message("今天以完成该打卡计划");
        }
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 获取明天的4点时间
        LocalDateTime tomorrow4AM = LocalDateTime.of(currentTime.toLocalDate().plusDays(1), LocalTime.of(4, 0));
        // 计算当前时间到明天4点的秒差值
        Duration duration = Duration.between(currentTime, tomorrow4AM);
        // 输出秒差值
        long secondsDifference = duration.getSeconds();
        redisService.setCacheObject(key, punchSize + 1,  secondsDifference, TimeUnit.SECONDS);
        return null;
    }

    @Override
    public ResultBody queryPunchState(String userId, String subarea, Integer page, Integer size) {


        return null;
    }
}