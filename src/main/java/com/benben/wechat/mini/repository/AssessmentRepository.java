package com.benben.wechat.mini.repository;

import com.benben.wechat.mini.model.Assessment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessmentRepository
        extends MongoRepository<Assessment, String> {
}
