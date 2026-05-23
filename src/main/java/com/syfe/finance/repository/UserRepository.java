package com.syfe.finance.repository;

import com.syfe.finance.model.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, Long> userIdsByUsername = new ConcurrentHashMap<>();

    public synchronized User save(User user) {
        if (user.getId() == null) {
            user.setId(sequence.getAndIncrement());
        }
        usersById.put(user.getId(), user);
        userIdsByUsername.put(normalize(user.getUsername()), user.getId());
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(usersById.get(id));
    }

    public Optional<User> findByUsername(String username) {
        Long id = userIdsByUsername.get(normalize(username));
        return id == null ? Optional.empty() : findById(id);
    }

    public boolean existsByUsername(String username) {
        return userIdsByUsername.containsKey(normalize(username));
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
