package com.syfe.finance.controller;

import com.syfe.finance.dto.AuthDtos;
import com.syfe.finance.dto.GoalDtos;
import com.syfe.finance.security.CurrentUserService;
import com.syfe.finance.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final CurrentUserService currentUserService;
    private final GoalService goalService;

    public GoalController(CurrentUserService currentUserService, GoalService goalService) {
        this.currentUserService = currentUserService;
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<GoalDtos.GoalResponse> create(Authentication authentication,
                                                       @Valid @RequestBody GoalDtos.CreateGoalRequest request) {
        Long userId = currentUserService.requireUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(userId, request));
    }

    @GetMapping
    public GoalDtos.GoalsResponse getAll(Authentication authentication) {
        Long userId = currentUserService.requireUserId(authentication);
        return goalService.getAll(userId);
    }

    @GetMapping("/{id}")
    public GoalDtos.GoalResponse get(Authentication authentication, @PathVariable Long id) {
        Long userId = currentUserService.requireUserId(authentication);
        return goalService.get(userId, id);
    }

    @PutMapping("/{id}")
    public GoalDtos.GoalResponse update(Authentication authentication,
                                       @PathVariable Long id,
                                       @Valid @RequestBody GoalDtos.UpdateGoalRequest request) {
        Long userId = currentUserService.requireUserId(authentication);
        return goalService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public AuthDtos.MessageResponse delete(Authentication authentication, @PathVariable Long id) {
        Long userId = currentUserService.requireUserId(authentication);
        goalService.delete(userId, id);
        return new AuthDtos.MessageResponse("Goal deleted successfully");
    }
}
