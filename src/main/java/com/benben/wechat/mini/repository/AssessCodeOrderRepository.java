package com.benben.wechat.mini.repository;

import com.benben.wechat.mini.model.AssessCodeOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessCodeOrderRepository
        extends MongoRepository<AssessCodeOrder, String> {
}
