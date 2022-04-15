package pragmasoft.andriilupynos.js_executioner.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

@Service
public class TaskScheduleService {

    @Autowired private TaskStore taskStore;
    @Autowired private TaskValidateService taskValidateService;

    /**
     * Schedule a task for future execution
     *
     * @param taskModel - Model of the task to schedule
     * @return - Mono with scheduled task
     */
    public Mono<Task> schedule(TaskScheduleModel taskModel) {
        var task =
                Task.builder()
                        .code(taskModel.getCode())
                        .status(TaskStatus.NEW)
                        .name(this.getNameOrGenerateNew(taskModel))
                        .scheduledAt(taskModel.getExecutionDate())
                        .build();

        return taskValidateService.validate(task)
                .then(taskStore.save(task));
    }

    private String getNameOrGenerateNew(TaskScheduleModel task) {
        if (StringUtils.hasText(task.getName()))
            return task.getName();
        return UUID.randomUUID().toString();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskScheduleModel {
        private String name;
        private String code;
        private Date executionDate;
    }

}
