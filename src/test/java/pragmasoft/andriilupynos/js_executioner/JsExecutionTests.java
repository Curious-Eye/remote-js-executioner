package pragmasoft.andriilupynos.js_executioner;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.IllegalArgumentException;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.InvalidJSProvidedException;
import pragmasoft.andriilupynos.js_executioner.domain.model.script.ExecutionStatus;
import pragmasoft.andriilupynos.js_executioner.domain.service.ScriptService;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JsExecutionTests {

    @Autowired private ScriptService scriptService;

    @Test
    void userMustBeAbleToScheduleJsExecution() {
        // GIVEN
        String code = "console.log(\"Hi\")";

        // WHEN - Schedule code execution
        var id = scriptService.scheduleScript(code, new Date(Instant.now().plusSeconds(60).toEpochMilli()));

        // THEN - Script should be created and scheduled
        assertNotNull(id);

        var storedScript = scriptService.getFullInfoById(id);
        assertNotNull(storedScript);
        assertEquals(id, storedScript.getId());
        assertEquals(code, storedScript.getCode());
        assertEquals(ExecutionStatus.SCHEDULED, storedScript.getExecutionInfo().getStatus());
    }

    @Test
    void userMustNotBeAbleToScheduleInvalidJs() {
        // GIVEN
        String code = "some_var..call()";

        // WHEN - Schedule invalid code execution
        // THEN - Script should not be created
        var ex = assertThrows(
                InvalidJSProvidedException.class,
                () -> scriptService.scheduleScript(code, null)
        );
        assertEquals(
                "SyntaxError: Unnamed:1:9 Expected ident but found .\n" +
                        "some_var..call()\n" +
                        "         ^\n",
                ex.getMessage()
        );
    }

    @Test
    void userShouldBeAbleToExecuteJs() {
        // GIVEN
        String code = "print('Hi');";

        // WHEN
        var id = scriptService.scheduleScript(code, null);
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .untilAsserted(() -> {
                    var script = scriptService.getFullInfoById(id);
                    assertEquals(ExecutionStatus.COMPLETED, script.getExecutionInfo().getStatus());
                });

        // THEN
        var storedScript = scriptService.getFullInfoById(id);
        assertEquals(ExecutionStatus.COMPLETED, storedScript.getExecutionInfo().getStatus());
        assertEquals("Hi\n", storedScript.getExecutionInfo().getOutput());
        assertEquals("", storedScript.getExecutionInfo().getError());
    }

    @Test
    void allExecutionErrorsShouldBeSaved() {
        // GIVEN
        String code = "print('Hi');" +
                "console.error('Std err');" +
                "some_undefined_var.call();";

        // WHEN
        var id = scriptService.scheduleScript(code, null);
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .untilAsserted(() -> {
                    var script = scriptService.getFullInfoById(id);
                    assertEquals(ExecutionStatus.ERRORED, script.getExecutionInfo().getStatus());
                });

        // THEN
        var storedScript = scriptService.getFullInfoById(id);
        assertEquals(ExecutionStatus.ERRORED, storedScript.getExecutionInfo().getStatus());
        assertEquals("Std err\n", storedScript.getExecutionInfo().getError());
        assertEquals("ReferenceError: some_undefined_var is not defined",
                storedScript.getExecutionInfo().getInterruptionMsg());
        assertEquals("Hi\n", storedScript.getExecutionInfo().getOutput());
    }

    @Test
    void userShouldBeAbleToStopScriptExecution() {
        // GIVEN - script with infinite loop is executing
        var id = scriptService.scheduleScript("while(true) { }", null);
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .until(() -> scriptService.getFullInfoById(id).getExecutionInfo().getStatus() == ExecutionStatus.EXECUTING);

        // WHEN - we stop this script
        scriptService.changeExecution(id, ExecutionStatus.STOPPED);
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .until(() -> scriptService.getFullInfoById(id).getExecutionInfo().getStatus() == ExecutionStatus.STOPPED);

        // THEN - script should be stopped
        var script = scriptService.getFullInfoById(id);
        assertEquals(ExecutionStatus.STOPPED, script.getExecutionInfo().getStatus());
    }

    @Test
    void userShouldNotBeAbleToChangeExecutionOtherThanStoppingIt() {
        // GIVEN - script with infinite loop is executing
        var id = scriptService.scheduleScript("while(true) { }", null);
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .until(() -> scriptService.getFullInfoById(id).getExecutionInfo().getStatus() == ExecutionStatus.EXECUTING);

        // WHEN - we try to do something other than stopping the script
        assertThrows(IllegalArgumentException.class, () -> scriptService.changeExecution(id, ExecutionStatus.COMPLETED));
        // THEN - script's execution should not be modified
        var script = scriptService.getFullInfoById(id);
        assertEquals(ExecutionStatus.EXECUTING, script.getExecutionInfo().getStatus());
    }

}
