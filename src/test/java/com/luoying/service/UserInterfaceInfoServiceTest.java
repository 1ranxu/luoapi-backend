package com.luoying.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UserInterfaceInfoServiceTest {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Test
    void invokeCount() {
        boolean result = userInterfaceInfoService.invokeCount(3, 1);
        Assert.assertTrue(result);
    }
}