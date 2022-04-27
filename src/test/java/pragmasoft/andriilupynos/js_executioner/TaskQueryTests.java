package pragmasoft.andriilupynos.js_executioner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import pragmasoft.andriilupynos.js_executioner.service.TaskQueryService;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class TaskQueryTests {

    @Autowired private TaskStore taskStore;
    @Autowired private TaskQueryService taskQueryService;

    @BeforeEach
    public void clearDB() {
        taskStore.deleteAll().block();
    }

    @Test
    public void userShouldBeAbleToSearchTasksByName() {
        // GIVEN
        taskStore.saveAll(
                List.of(
                        Task.builder()
                                .id("1")
                                .name("Query data task")
                                .status(TaskStatus.STOPPED)
                                .build(),
                        Task.builder()
                                .id("2")
                                .name("Input data task")
                                .status(TaskStatus.NEW)
                                .build()
                )
        ).collectList().block();

        // WHEN
        var res = taskQueryService.search(
                TaskQueryService.TaskSearchModel.builder().name("Input data task").build()
        ).collectList().block();

        // THEN
        assertEquals(1, res.size());
        var foundTask = res.get(0);
        assertEquals("2", foundTask.getId());
        assertEquals("Input data task", foundTask.getName());
        assertEquals(TaskStatus.NEW, foundTask.getStatus());
    }

    @Test
    public void userShouldBeAbleToSearchTasksByStatus() {
        // GIVEN
        taskStore.saveAll(
                List.of(
                        Task.builder()
                                .id("1")
                                .name("Task 1")
                                .status(TaskStatus.STOPPED)
                                .build(),
                        Task.builder()
                                .id("2")
                                .name("Task 2")
                                .status(TaskStatus.NEW)
                                .build()
                )
        ).collectList().block();

        // WHEN
        var res = taskQueryService.search(
                TaskQueryService.TaskSearchModel.builder().status(TaskStatus.STOPPED).build()
        ).collectList().block();

        // THEN
        assertEquals(1, res.size());
        var foundTask = res.get(0);
        assertEquals("1", foundTask.getId());
        assertEquals("Task 1", foundTask.getName());
        assertEquals(TaskStatus.STOPPED, foundTask.getStatus());
    }

    @Test
    public void userShouldBeAbleToSearchTasksWithOrderByCreationDate() {
        // GIVEN
        taskStore.saveAll(
                List.of(
                        Task.builder()
                                .id("1")
                                .createdDate(new Date())
                                .build(),
                        Task.builder()
                                .id("2")
                                .createdDate(Date.from(new Date().toInstant().plusMillis(100)))
                                .build(),
                        Task.builder()
                                .id("3")
                                .createdDate(Date.from(new Date().toInstant().plusMillis(200)))
                                .build()
                )
        ).collectList().block();

        // WHEN
        var res = taskQueryService.search(
                TaskQueryService.TaskSearchModel.builder().newFirst(false).build()
        ).collectList().block();

        // THEN
        assertEquals(3, res.size());
        assertEquals("1", res.get(0).getId());
        assertEquals("2", res.get(1).getId());
        assertEquals("3", res.get(2).getId());

        // WHEN
        res = taskQueryService.search(
                TaskQueryService.TaskSearchModel.builder().newFirst(true).build()
        ).collectList().block();

        // THEN
        assertEquals(3, res.size());
        assertEquals("3", res.get(0).getId());
        assertEquals("2", res.get(1).getId());
        assertEquals("1", res.get(2).getId());
    }

    @Test
    public void userShouldBeAbleToSearchTasksWithComplexFilter() {
        // GIVEN
        taskStore.saveAll(
                List.of(
                        Task.builder()
                                .id("0")
                                .name("Test name")
                                .status(TaskStatus.EXECUTING)
                                .createdDate(new Date())
                                .build(),
                        Task.builder()
                                .id("1")
                                .name("Test name")
                                .status(TaskStatus.NEW)
                                .createdDate(Date.from(new Date().toInstant().plusMillis(100)))
                                .build(),
                        Task.builder()
                                .id("2")
                                .name("Test name")
                                .status(TaskStatus.NEW)
                                .createdDate(Date.from(new Date().toInstant().plusMillis(200)))
                                .build(),
                        Task.builder()
                                .id("3")
                                .name("Stopped task")
                                .status(TaskStatus.STOPPED)
                                .createdDate(Date.from(new Date().toInstant().plusMillis(300)))
                                .build()
                )
        ).collectList().block();

        // WHEN
        var res = taskQueryService.search(
                TaskQueryService.TaskSearchModel.builder()
                        .status(TaskStatus.NEW)
                        .name("Test name")
                        .newFirst(true)
                        .build()
        ).collectList().block();

        // THEN
        assertEquals(2, res.size());
        assertEquals("2", res.get(0).getId());
        assertEquals("1", res.get(1).getId());
    }

}
