package com.hmall.user.service.impl;

import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.ForbiddenException;
import com.hmall.user.domain.po.User;
import com.hmall.user.domain.vo.UserLoginVO;
import com.hmall.user.emus.UserStatus;
import com.hmall.user.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;
import static org.junit.jupiter.api.Assertions.*;


class UserServiceImplTest {

    @Test
    void login() {

    }
}