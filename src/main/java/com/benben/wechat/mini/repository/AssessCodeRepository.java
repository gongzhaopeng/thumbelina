package com.benben.wechat.mini.repository;

import com.benben.wechat.mini.model.AssessCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessCodeRepository
        extends MongoRepository<AssessCode, String> {
}
