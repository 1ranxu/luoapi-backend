package com.luoying.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.luoying.model.entity.SignIn;
import com.luoying.service.SignInService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 清空签到表任务
 */
@Component
public class EmptySignInJob {
    @Resource
    private SignInService signInService;

    /**
     * 每天晚上12点清空签到表
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void clearCheckInList() {
        // 每批删除的数据量
        int batchSize = 1000;
        // 是否还有数据需要删除
        boolean hasMoreData = true;

        while (hasMoreData) {
            // 分批查询数据
            List<SignIn> dataList = signInService.list(new QueryWrapper<SignIn>().last("LIMIT " + batchSize));

            if (dataList.isEmpty()) {// 没有数据了，退出循环
                hasMoreData = false;
            } else {// 批量删除数据
                signInService.removeByIds(dataList.stream().map(SignIn::getId).collect(Collectors.toList()));
            }
        }

    }
}
