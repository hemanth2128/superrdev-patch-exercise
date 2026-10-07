package com.internal.tasktracker;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Normalize query input
        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + query.toLowerCase() + "%";

        // Parse + validate status filter (400 instead of 500 on bad input)
        String normalizedStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid status: " + status));
            }
        }

        // Clamp pagination bounds
        // if (page < 1) {
        //     page = 1;
        // }
        // pageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

       // Validate pagination bounds
if (page < 1) {
    return ResponseEntity.badRequest()
            .body(Map.of("error", "page must be >= 1"));
}

if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
    return ResponseEntity.badRequest()
            .body(Map.of(
                    "error",
                    "pageSize must be between 1 and " + MAX_PAGE_SIZE
            ));
}

        if (log.isDebugEnabled()) {
            log.debug("[TaskController] q=\"{}\" status={} page={} pageSize={}",
                    query, normalizedStatus, page, pageSize);
        }

        // DB-side pagination: COUNT + LIMIT/OFFSET instead of loading all rows
        long total = taskRepository.countTasks(searchTerm, normalizedStatus);
        int offset = (page - 1) * pageSize;
        List<Task> pageResults = offset < total
                ? taskRepository.searchTasksPaged(searchTerm, normalizedStatus, pageSize, offset)
                : Collections.emptyList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", pageResults);
        response.put("total", total);
        response.put("page", page);
        response.put("pageSize", pageSize);

        return ResponseEntity.ok(response);
    }
}

