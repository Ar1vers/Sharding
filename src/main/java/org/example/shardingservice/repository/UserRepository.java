package org.example.shardingservice.repository;


import org.example.shardingservice.config.ShardRoutingDataSource;
import org.example.shardingservice.model.User;
import org.example.shardingservice.service.ShardingService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
    private final JdbcTemplate jdbcTemplate;
    private final ShardingService shardingService;
    private static final BeanPropertyRowMapper<User> USER_ROW_MAPPER = new BeanPropertyRowMapper<>(User.class);

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate, ShardingService shardingService) {
        this.jdbcTemplate = jdbcTemplate;
        this.shardingService = shardingService;
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(UUID userId) {
        String shardKey = null;
        try {
            shardKey = shardingService.getShardIndex(userId);
            ShardRoutingDataSource.setCurrentShard(shardKey);

            User user = jdbcTemplate.queryForObject(
                    "SELECT id, name, email FROM users WHERE id = ?",
                    USER_ROW_MAPPER,
                    userId
            );
            return Optional.ofNullable(user);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Ошибка поиска пользователя с ID {} в шардe {}: {}", userId, shardKey != null ? shardKey : "неизвестно", e.getMessage());
            return Optional.empty();
        } finally {
            ShardRoutingDataSource.clearCurrentShard();
        }
    }

    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        String shardKey = null;
        try {
            shardKey = shardingService.getShardIndex(user.getId());
            ShardRoutingDataSource.setCurrentShard(shardKey);

            String sql = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";
            int rowsAffected = jdbcTemplate.update(sql, user.getId(), user.getName(), user.getEmail());

            if (rowsAffected > 0) {
                log.info("Пользователь {} сохранен в шард {}", user.getId(), shardKey);
            }
            return user;

        } catch (Exception e) {
            log.error("Ошибка при сохранении пользователя {} в шард {}: {}", user.getId(), shardKey != null ? shardKey : "неизвестно", e.getMessage());
            throw new RuntimeException("Ошибка при сохранении пользователя с ID " + user.getId(), e);
        } finally {
            ShardRoutingDataSource.clearCurrentShard();
        }
    }

    public List<User> findAll() {
        List<User> allUsers = new ArrayList<>();
        List<String> allShardKeys = shardingService.getAllShardIndex();

        if (allShardKeys == null || allShardKeys.isEmpty()) {
            return allUsers;
        }

        for (String shardKey : allShardKeys) {
            try {
                ShardRoutingDataSource.setCurrentShard(shardKey);

                List<User> usersInShard = jdbcTemplate.query(
                        "SELECT id, name, email FROM users",
                        USER_ROW_MAPPER
                );
                allUsers.addAll(usersInShard);

            } catch (Exception e) {
                log.error("Не удалось получить пользователей из шарда {}: {}", shardKey, e.getMessage());
            } finally {
                ShardRoutingDataSource.clearCurrentShard();
            }
        }

        return allUsers;
    }
}