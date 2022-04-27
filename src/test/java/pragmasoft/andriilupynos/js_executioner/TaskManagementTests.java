package pragmasoft.andriilupynos.js_executioner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.exception.TaskNotFoundException;
import pragmasoft.andriilupynos.js_executioner.service.TaskDeleteService;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class TaskManagementTests {

    @Autowired private TaskDeleteService taskDeleteService;
    @Autowired private TaskStore taskStore;

    @Test
    public void userShouldBeAbleToDeleteTask() {
        // GIVEN
        taskStore.save(
                Task.builder()
                        .id("1")
                        .build()
        ).block();

        // WHEN
        taskDeleteService.deleteById("1").block();

        // THEN
        assertNull(taskStore.findById("1").block());
    }

    @Test
    public void ifUserDeletesNonExistentTaskExceptionShouldBeThrown() {
        // GIVEN
        taskStore.save(
                Task.builder()
                        .id("1")
                        .build()
        ).block();

        // WHEN
        StepVerifier.create(taskDeleteService.deleteById("2"))
        // THEN
                .expectError(TaskNotFoundException.class)
                .verify();
    }

}
