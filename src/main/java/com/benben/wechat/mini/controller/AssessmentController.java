package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.controller.exception.AssessCodeNotFoundException;
import com.benben.wechat.mini.controller.exception.AssessmentNotFoundException;
import com.benben.wechat.mini.controller.exception.UserNotFoundException;
import com.benben.wechat.mini.model.AssessCode;
import com.benben.wechat.mini.model.Assessment;
import com.benben.wechat.mini.model.User;
import com.benben.wechat.mini.repository.AssessCodeRepository;
import com.benben.wechat.mini.repository.AssessmentRepository;
import com.benben.wechat.mini.repository.UserRepository;
import com.benben.wechat.mini.service.UserUpdateLockService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/assessments")
public class AssessmentController {

    final private AssessmentRepository assessmentRepository;
    final private UserRepository userRepository;
    final private AssessCodeRepository assessCodeRepository;
    final private UserUpdateLockService userUpdateLockService;

    @Autowired
    public AssessmentController(
            AssessmentRepository assessmentRepository,
            UserRepository userRepository,
            AssessCodeRepository assessCodeRepository,
            UserUpdateLockService userUpdateLockService) {

        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.assessCodeRepository = assessCodeRepository;
        this.userUpdateLockService = userUpdateLockService;
    }

    /**
     * @param createReq
     * @return
     * @throws UserNotFoundException
     * @throws AssessCodeNotFoundException
     * @throws UserUpdateLockService.FailToAcquireUserUpdateLock
     * @throws IllegalStateException
     * @throws AssessCodeUnusableException
     */
    @PostMapping
    public AssessmentCreateResp createAssessment(
            @Valid @RequestBody AssessmentCreateReq createReq) {

        if (!userRepository.existsById(createReq.getOpenid())) {
            throw new UserNotFoundException();
        }

        if (!assessCodeRepository.existsById(createReq.getAssessCode())) {
            throw new AssessCodeNotFoundException();
        }

        return userUpdateLockService.doWithLock(createReq.getOpenid(), () -> {

            final var codeOwnerId = assessCodeRepository.findById(createReq.getAssessCode())
                    .orElseThrow(IllegalStateException::new).getOwner();

            final Callable<AssessmentCreateResp> task = () -> {

                final var assessCode =
                        assessCodeRepository.findById(createReq.getAssessCode())
                                .orElseThrow(IllegalStateException::new);
                final var assessmentOwner =
                        userRepository.findById(createReq.getOpenid())
                                .orElseThrow(IllegalStateException::new);
                final var assessCodeOwner =
                        userRepository.findById(codeOwnerId)
                                .orElseThrow(IllegalStateException::new);

                if (assessCode.getState() != AssessCode.State.FRESH) {
                    throw new AssessCodeUnusableException();
                }

                final var assessment = constructAssessment(
                        createReq.getSubject(), createReq.getOpenid(), assessCode.getCode());

                assessCode.setState(AssessCode.State.OCCUPIED);
                assessCode.setOccupiedBy(createReq.getOpenid());
                assessCode.setAssessmentId(assessment.getId());

                final var userAssessment = new User.Assessment();
                userAssessment.setId(assessment.getId());
                userAssessment.setCreateTime(assessment.getCreateTime());
                userAssessment.setSubject(assessment.getSubject());
                assessmentOwner.addAssessment(userAssessment);

                assessCodeOwner.getAssessCode(assessCode.getCode())
                        .orElseThrow(IllegalStateException::new)
                        .setState(assessCode.getState());

                userRepository.save(assessCodeOwner);
                assessCodeRepository.save(assessCode);
                assessmentRepository.save(assessment);
                userRepository.save(assessmentOwner);

                final var resp = new AssessmentCreateResp();
                resp.setId(assessment.getId());

                return resp;
            };

            if (codeOwnerId.equals(createReq.getOpenid())) {
                return task.call();
            } else {
                return userUpdateLockService.doWithLock(codeOwnerId, task);
            }
        });
    }

    /**
     * @param assessmentId
     * @return
     * @throws AssessmentNotFoundException
     */
    @GetMapping("/{assessment-id}")
    public AssessmentGetByIdResp getAssessmentById(
            @PathVariable("assessment-id") String assessmentId) {

        return assessmentRepository.findById(assessmentId)
                .map(AssessmentGetByIdResp::of)
                .orElseThrow(AssessmentNotFoundException::new);
    }

    /**
     * @param assessmentId
     * @param moduleId
     * @param updateReq
     * @return
     * @throws UserUpdateLockService.FailToAcquireUserUpdateLock
     * @throws AssessmentNotFoundException
     * @throws UserNotFoundException
     * @throws IllegalStateException
     */
    @PutMapping("/{assessment-id}/modules/{module-id}/answers")
    public ModuleAnswersUpdateResp updateModuleAnswers(
            @PathVariable("assessment-id") String assessmentId,
            @PathVariable("module-id") String moduleId,
            @Valid @RequestBody ModuleAnswersUpdateReq updateReq) {

        return userUpdateLockService.doWithLock(updateReq.getOpenid(), () -> {

            final var assessment = assessmentRepository.findById(assessmentId)
                    .orElseThrow(AssessmentNotFoundException::new);
            final var owner = userRepository.findById(updateReq.getOpenid())
                    .orElseThrow(UserNotFoundException::new);

            final var module = assessment.forceAcquireModule(moduleId);
            module.setAnswers(updateReq.getAnswers());

            owner.getAssessment(assessmentId)
                    .orElseThrow(IllegalStateException::new)
                    .addCompletedModule(moduleId);

            assessmentRepository.save(assessment);
            userRepository.save(owner);

            return new ModuleAnswersUpdateResp();
        });
    }

    @ExceptionHandler(AssessCodeUnusableException.class)
    public CommonResponse assessCodeUnusableHandler() {

        final var resp = new CommonResponse();
        resp.setStatusCode(CommonResponse.SC_ASSESS_CODE_UNUSABLE);
        resp.setStatusDetail("Assess-code unusable.");

        return resp;
    }

    private Assessment constructAssessment(
            String subject, String owner, String assessCode) {

        final var assessment = new Assessment();
        assessment.setId(new ObjectId().toString());
        assessment.setCreateTime(System.currentTimeMillis());
        assessment.setSubject(subject);
        assessment.setOwner(owner);
        assessment.setAssessCode(assessCode);

        return assessment;
    }

    @Validated
    @Data
    static class AssessmentCreateReq {
        @NotBlank
        private String openid;
        @NotBlank
        private String assessCode;
        @NotBlank
        private String subject;
    }

    @Getter
    @Setter
    static class AssessmentCreateResp extends CommonResponse {

        private String id;
    }

    @Getter
    @Setter
    static class AssessmentGetByIdResp extends CommonResponse {

        static AssessmentGetByIdResp of(Assessment assessment) {

            final var resp = new AssessmentGetByIdResp();
            resp.setId(assessment.getId());
            resp.setCreateTime(assessment.getCreateTime());
            resp.setSubject(assessment.getSubject());
            resp.setModules(assessment.getModules());

            return resp;
        }

        private String id;
        private Long createTime;
        private String subject;
        private List<Assessment.Module> modules;
    }

    @Validated
    @Data
    static class ModuleAnswersUpdateReq {

        @NotBlank
        private String openid;

        @NotNull
        private List<Assessment.Answer> answers;
    }

    @Getter
    @Setter
    static class ModuleAnswersUpdateResp extends CommonResponse {
    }

    static class AssessCodeUnusableException extends RuntimeException {
    }
}
