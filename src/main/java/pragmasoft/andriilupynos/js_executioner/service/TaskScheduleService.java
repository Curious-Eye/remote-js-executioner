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

    /**
     * Schedule task for future execution
     *
     * @param task - task to schedule
     * @return - Mono of scheduled task
     */
    public Mono<Task> schedule(TaskScheduleModel task) {
        return taskStore.save(
                Task.builder()
                        .code(task.getCode())
                        .status(TaskStatus.NEW)
                        .name(this.getNameOrGenerateNew(task))
                        .build()
        );
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
