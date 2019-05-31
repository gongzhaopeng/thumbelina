package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.model.User;
import com.benben.wechat.mini.repository.UserRepository;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/login")
public class LoginController {

    final private UserRepository userRepository;

    @Autowired
    public LoginController(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @PostMapping("/id")
    public LoginResp loginById(
            @Valid @RequestBody LoginByIdReq loginByIdReq) {

        final var id = loginByIdReq.getId();

        final var user = userRepository.findById(id).orElseGet(() -> {
            final var u = new User();
            u.setId(id);
            u.setCreateTime(System.currentTimeMillis());

            return userRepository.save(u);
        });

        return LoginResp.of(user);
    }

    @Validated
    @Data
    static class LoginByIdReq {

        @NotBlank
        private String id;
    }

    @Getter
    @Setter
    static class LoginResp {

        static LoginResp of(User user) {

            final var resp = new LoginResp();

            resp.setId(user.getId());
            resp.setCustomProfile(user.getCustomProfile());
            resp.setAssessments(user.getAssessments());

            return resp;
        }

        private String id;
        private User.CustomProfile customProfile;
        private List<User.Assessment> assessments;
    }
}
