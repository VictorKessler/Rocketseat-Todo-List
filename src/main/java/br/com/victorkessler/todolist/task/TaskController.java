package br.com.victorkessler.todolist.task;

import br.com.victorkessler.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @PostMapping("/create")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        final var userId = request.getAttribute("userId");
        taskModel.setUserId((UUID) userId);

        final var currentDate = Date.from(Instant.now());

        if (currentDate.after(taskModel.getStartAt()) || currentDate.after(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Starting/Ending date must be greater than current date");
        }

        if (taskModel.getStartAt().after(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ending date must be greater than starting date");
        }

        taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskModel);
    }

    @GetMapping("/get-tasks")
    public List<TaskModel> getTasks(HttpServletRequest request) {
        final var userId = request.getAttribute("userId");

        return taskRepository.findByUserId((UUID) userId);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity updateTask(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
        final var task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Task not found");
        }

        final var userId = request.getAttribute("userId");

        if (!task.getUserId().equals(userId)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User does not have permission");
        }

        Utils.copyNonNullProperties(taskModel, task);

        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.save(task));
    }
}
