package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.controller.exception.UserNotFoundException;
import com.benben.wechat.mini.model.User;
import com.benben.wechat.mini.repository.UserRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    final private UserRepository userRepository;

    @Autowired
    public UserController(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    /**
     * @param id
     * @return
     * @throws UserNotFoundException
     */
    @GetMapping("/{id}")
    public UserInfoResp getUserInfoByOpenid(
            @PathVariable("id") String id) {

        final var user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        return UserInfoResp.of(user);
    }

    /**
     * @param id
     * @param customProfile
     * @return
     * @throws UserNotFoundException
     */
    @PutMapping("/{id}/profile/custom")
    public CustomProfileUpdateResp updateCustomProfile(
            @PathVariable("id") String id,
            @Valid @RequestBody User.CustomProfile customProfile) {

        final var user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        user.setCustomProfile(customProfile);

        userRepository.save(user);

        return new CustomProfileUpdateResp();
    }

    @Getter
    @Setter
    static class UserInfoResp extends CommonResponse {

        static UserInfoResp of(User user) {

            final var resp = new UserInfoResp();
            resp.setId(user.getId());
            resp.setCustomProfile(user.getCustomProfile());
            resp.setAssessments(user.getAssessments());

            return resp;
        }

        private String id;
        private User.CustomProfile customProfile;
        private List<User.Assessment> assessments;
    }

    @Getter
    @Setter
    static class CustomProfileUpdateResp extends CommonResponse {
    }
}
