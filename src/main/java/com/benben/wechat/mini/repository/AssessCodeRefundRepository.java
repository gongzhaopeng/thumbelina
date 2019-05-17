package com.benben.wechat.mini.repository;

import com.benben.wechat.mini.model.AssessCodeRefund;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessCodeRefundRepository
        extends MongoRepository<AssessCodeRefund, String> {
}
