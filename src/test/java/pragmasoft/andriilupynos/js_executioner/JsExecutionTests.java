package pragmasoft.andriilupynos.js_executioner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Task;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import pragmasoft.andriilupynos.js_executioner.service.TaskExecuteService;
import pragmasoft.andriilupynos.js_executioner.service.TaskScheduleService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JsExecutionTests {

    @Autowired private TaskScheduleService taskScheduleService;
    @Autowired private TaskStore taskStore;
    @Autowired private TaskExecuteService taskExecuteService;

    @BeforeEach
    public void clearDB() {
        taskStore.deleteAll().block();
    }

    @Test
    public void userMustBeAbleToScheduleJsExecution() {
        // GIVEN
        String code = "console.log(\"Hi\")";

        // WHEN - Schedule code execution
        var res = taskScheduleService.schedule(TaskScheduleService.TaskScheduleModel.builder().code(code).build()).block();

        // THEN - Task should be created
        assertNotNull(res);
        assertNotNull(res.getName());
        assertEquals(code, res.getCode());
        assertEquals(TaskStatus.NEW, res.getStatus());

        var storedTask = taskStore.findByName(res.getName()).block();
        assertNotNull(storedTask);
        assertEquals(res.getName(), storedTask.getName());
        assertEquals(code, storedTask.getCode());
        assertEquals(TaskStatus.NEW, storedTask.getStatus());
    }

    @Test
    public void userShouldBeAbleToExecuteJs() {
        // GIVEN
        String code = "print('Hi');";

        // WHEN
        taskExecuteService.execute(
                Task.builder()
                        .id("1")
                        .name("Task 1")
                        .code(code)
                        .status(TaskStatus.NEW)
                        .build()
        ).block();

        // THEN
        var storedTask = taskStore.findByName("Task 1").block();
        assertNotNull(storedTask);

        assertEquals(TaskStatus.COMPLETED, storedTask.getStatus());
        assertEquals("Hi\n", storedTask.getOutput());
        assertNull(storedTask.getError());
    }

    @Test
    public void ifJSThrowsAnErrorItShouldBeSaved() {
        // GIVEN
        String code = "print('Hi');" +
                "throw 'Some error';";

        // WHEN
        taskExecuteService.execute(
                Task.builder()
                        .id("1")
                        .name("Task 1")
                        .code(code)
                        .status(TaskStatus.NEW)
                        .build()
        ).block();

        // THEN
        var storedTask = taskStore.findByName("Task 1").block();
        assertNotNull(storedTask);

        assertEquals(TaskStatus.ERRORED, storedTask.getStatus());
        assertEquals("org.graalvm.polyglot.PolyglotException: Some error", storedTask.getError());
        assertEquals("Hi\n", storedTask.getOutput());
    }

}
