package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.repository.UserRepository;
import com.benben.wechat.mini.service.UserUpdateLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    final private UserUpdateLockService userUpdateLockService;
    final private UserRepository userRepository;

    @Autowired
    public LoginController(
            UserUpdateLockService userUpdateLockService,
            UserRepository userRepository) {

        this.userUpdateLockService = userUpdateLockService;
        this.userRepository = userRepository;
    }
}
