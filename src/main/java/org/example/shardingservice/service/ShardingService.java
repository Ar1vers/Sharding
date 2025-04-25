package org.example.shardingservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ShardingService {

    private static final Logger log = LoggerFactory.getLogger(ShardingService.class);
    private final int shardCount;

    @Autowired
    public ShardingService(@Value("${sharding.shard-count}") int shardCount) {
        this.shardCount = shardCount;
        log.info("ShardingService initialized with shardCount: {}", this.shardCount);
        if (this.shardCount <= 1) {
            log.error("shardCount is not positive! Sharding will likely fail. Check 'sharding.shard-count' property.");
        }
    }

    public String getShardIndex(UUID userId) {
        String uuidString = userId.toString();
        int sumOfChars = uuidString.chars().sum();
        int shardIndex = (sumOfChars % shardCount) + 1; // Добавляем +1 для получения от 1 до 3

        // Логирование маршрута к шару
        String shardKey = "shard" + shardIndex;
        log.info("Routing to shard {}", shardKey);

        return shardKey;
    }



    public List<String> getAllShardIndex() {
        List<String> shardIndex = new ArrayList<>();
        for (int i = 1; i <= shardCount; i++) { // От 1 до shardCount включительно
            var value = "shard" + i;
            shardIndex.add(value);
        }
        return shardIndex;
    }
}