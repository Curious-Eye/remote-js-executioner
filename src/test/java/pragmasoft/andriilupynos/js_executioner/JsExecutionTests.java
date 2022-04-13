package pragmasoft.andriilupynos.js_executioner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.data.TaskStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.TaskStatus;
import pragmasoft.andriilupynos.js_executioner.service.TaskScheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class JsExecutionTests {

    @Autowired private TaskScheduler taskScheduler;
    @Autowired private TaskStore taskStore;

    @Test
    public void userShouldBeAbleToScheduleJsExecution() {
        // GIVEN
        String code = "{}";

        // WHEN - Schedule code execution
        var res = taskScheduler.schedule(TaskScheduler.TaskScheduleModel.builder().code(code).build()).block();

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

}
