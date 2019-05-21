package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.controller.exception.UserNotFoundException;
import com.benben.wechat.mini.model.User;
import com.benben.wechat.mini.repository.UserRepository;
import com.benben.wechat.mini.service.UserUpdateLockService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    final private UserUpdateLockService userUpdateLockService;
    final private UserRepository userRepository;

    @Autowired
    public UserController(
            UserUpdateLockService userUpdateLockService,
            UserRepository userRepository) {

        this.userUpdateLockService = userUpdateLockService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{openid}")
    public UserInfoResp getUserInfoByOpenid(
            @PathVariable("openid") String openid) {

        final var user = userRepository.findById(openid)
                .orElseThrow(UserNotFoundException::new);

        return UserInfoResp.of(user);
    }

    @PutMapping("/{openid}/profile/custom")
    public CustomProfileUpdateResp updateCustomProfile(
            @PathVariable("openid") String openid,
            @Valid @RequestBody User.CustomProfile customProfile) {

        if (!userRepository.existsById(openid)) {
            throw new UserNotFoundException();
        }

        userUpdateLockService.doWithLock(openid, () -> {

            final var user = userRepository.findById(openid)
                    .map(u -> {
                        u.setCustomProfile(customProfile);
                        return u;
                    })
                    .orElseThrow();

            return userRepository.save(user);
        });

        return new CustomProfileUpdateResp();
    }

    @Getter
    @Setter
    static class UserInfoResp extends CommonResponse {

        static UserInfoResp of(User user) {

            final var resp = new UserInfoResp();
            resp.setOpenid(user.getOpenid());
            resp.setCustomProfile(user.getCustomProfile());
            resp.setAssessCodes(user.getAssessCodes());
            resp.setAssessments(user.getAssessments());

            return resp;
        }

        private String openid;
        private User.CustomProfile customProfile;
        private List<User.AssessCode> assessCodes;
        private List<User.Assessment> assessments;
    }

    @Getter
    @Setter
    static class CustomProfileUpdateResp extends CommonResponse {
    }
}
