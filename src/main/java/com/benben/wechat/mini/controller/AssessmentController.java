package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.model.Assessment;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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

    @Autowired
    public AssessmentController() {

    }

    @PostMapping
    public AssessmentCreateResp createAssessment(
            @Valid @RequestBody AssessmentCreateReq createReq) {

        // TODO

        return null;
    }

    @GetMapping("/{assessment-id}")
    public AssessmentGetByIdResp getAssessmentById(
            @PathVariable("assessment-id") String assessmentId) {

        // TODO

        return null;
    }

    @PutMapping("/{assessment-id}/modules/{module-id}/answers")
    public ModuleAnswersUpdateResp updateModuleAnswers(
            @PathVariable("assessment-id") String assessmentId,
            @PathVariable("module-id") String moduleId,
            @Valid @RequestBody ModuleAnswersUpdateReq updateReq) {

        // TODO

        return null;
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

        private String id;
        private Long createTime;
        private String subject;
        private List<Assessment.Module> modules;
    }

    @Validated
    @Data
    static class ModuleAnswersUpdateReq {

        @NotNull
        private List<Assessment.Answer> answers;
    }

    @Getter
    @Setter
    static class ModuleAnswersUpdateResp extends CommonResponse {
    }
}
