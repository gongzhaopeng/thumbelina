package com.benben.wechat.mini.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("assesscode")
@Data
public class AssessCodeConfiguration {

    private List<Integer> amountToFee;
}
