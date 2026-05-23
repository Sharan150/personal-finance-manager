package com.syfe.finance.repository;

import com.syfe.finance.model.SavingsGoal;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SavingsGoalRepository {
    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, SavingsGoal> goalsById = new ConcurrentHashMap<>();

    public synchronized SavingsGoal save(SavingsGoal goal) {
        if (goal.getId() == null) {
            goal.setId(sequence.getAndIncrement());
        }
        goalsById.put(goal.getId(), goal);
        return goal;
    }

    public Optional<SavingsGoal> findById(Long id) {
        return Optional.ofNullable(goalsById.get(id));
    }

    public Optional<SavingsGoal> findByUserAndId(Long userId, Long id) {
        return findById(id).filter(goal -> goal.getUserId().equals(userId));
    }

    public List<SavingsGoal> findByUser(Long userId) {
        return goalsById.values().stream()
                .filter(goal -> goal.getUserId().equals(userId))
                .sorted(Comparator.comparing(SavingsGoal::getId))
                .toList();
    }

    public void delete(Long id) {
        goalsById.remove(id);
    }
}
