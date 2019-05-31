package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.controller.exception.AssessmentNotFoundException;
import com.benben.wechat.mini.controller.exception.UserNotFoundException;
import com.benben.wechat.mini.model.Assessment;
import com.benben.wechat.mini.model.User;
import com.benben.wechat.mini.repository.AssessmentRepository;
import com.benben.wechat.mini.repository.UserRepository;
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

@RestController
@RequestMapping("/assessments")
public class AssessmentController {

    final private AssessmentRepository assessmentRepository;
    final private UserRepository userRepository;

    @Autowired
    public AssessmentController(
            AssessmentRepository assessmentRepository,
            UserRepository userRepository) {

        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * @param createReq
     * @return
     * @throws UserNotFoundException
     * @throws IllegalStateException
     */
    @PostMapping
    public AssessmentCreateResp createAssessment(
            @Valid @RequestBody AssessmentCreateReq createReq) {

        final var assessmentOwner =
                userRepository.findById(createReq.getId())
                        .orElseThrow(UserNotFoundException::new);

        final var assessment = constructAssessment(
                createReq.getSubject(), createReq.getId());

        final var userAssessment = new User.Assessment();
        userAssessment.setId(assessment.getId());
        userAssessment.setCreateTime(assessment.getCreateTime());
        userAssessment.setSubject(assessment.getSubject());
        assessmentOwner.addAssessment(userAssessment);

        assessmentRepository.save(assessment);
        userRepository.save(assessmentOwner);

        final var resp = new AssessmentCreateResp();
        resp.setId(assessment.getId());

        return resp;
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
     * @throws AssessmentNotFoundException
     * @throws UserNotFoundException
     * @throws IllegalStateException
     */
    @PutMapping("/{assessment-id}/modules/{module-id}/answers")
    public ModuleAnswersUpdateResp updateModuleAnswers(
            @PathVariable("assessment-id") String assessmentId,
            @PathVariable("module-id") String moduleId,
            @Valid @RequestBody ModuleAnswersUpdateReq updateReq) {

        final var assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(AssessmentNotFoundException::new);
        final var owner = userRepository.findById(updateReq.getId())
                .orElseThrow(UserNotFoundException::new);

        final var module = assessment.forceAcquireModule(moduleId);
        module.setAnswers(updateReq.getAnswers());

        owner.getAssessment(assessmentId)
                .orElseThrow(IllegalStateException::new)
                .addCompletedModule(moduleId);

        assessmentRepository.save(assessment);
        userRepository.save(owner);

        return new ModuleAnswersUpdateResp();
    }

    private Assessment constructAssessment(
            String subject, String owner) {

        final var assessment = new Assessment();
        assessment.setId(new ObjectId().toString());
        assessment.setCreateTime(System.currentTimeMillis());
        assessment.setSubject(subject);
        assessment.setOwner(owner);

        return assessment;
    }

    @Validated
    @Data
    static class AssessmentCreateReq {
        @NotBlank
        private String id;
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
        private String id;

        @NotNull
        private List<Assessment.Answer> answers;
    }

    @Getter
    @Setter
    static class ModuleAnswersUpdateResp extends CommonResponse {
    }
}
