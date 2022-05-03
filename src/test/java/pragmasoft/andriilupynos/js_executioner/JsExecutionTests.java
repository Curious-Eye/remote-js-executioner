package pragmasoft.andriilupynos.js_executioner;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.data.ScriptStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Script;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptStatus;
import pragmasoft.andriilupynos.js_executioner.exception.InvalidJSProvidedException;
import pragmasoft.andriilupynos.js_executioner.service.ScriptExecuteService;
import pragmasoft.andriilupynos.js_executioner.service.ScriptScheduleService;
import reactor.test.StepVerifier;

import java.io.StringWriter;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JsExecutionTests {

    @Autowired
    private ScriptScheduleService scriptScheduleService;
    @Autowired
    private ScriptStore scriptStore;
    @Autowired
    private ScriptExecuteService scriptExecuteService;

    @BeforeEach
    public void clearDB() {
        scriptStore.deleteAll().block();
        scriptExecuteService.stopAll().block();
    }

    @Test
    public void userMustBeAbleToScheduleJsExecution() {
        // GIVEN
        String code = "console.log(\"Hi\")";

        // WHEN - Schedule code execution
        var res = scriptScheduleService.schedule(ScriptScheduleService.ScriptScheduleModel.builder().code(code).build()).block();

        // THEN - Script should be created
        assertNotNull(res);
        assertNotNull(res.getName());
        assertEquals(code, res.getCode());
        assertEquals(ScriptStatus.NEW, res.getStatus());

        var storedScript = scriptStore.findById(res.getId()).block();
        assertNotNull(storedScript);
        assertEquals(res.getName(), storedScript.getName());
        assertEquals(code, storedScript.getCode());
        assertEquals(ScriptStatus.NEW, storedScript.getStatus());
    }

    @Test
    public void userMustNotBeAbleToScheduleInvalidJs() {
        // GIVEN
        String code = "some_var..call()";

        // WHEN - Schedule invalid code execution
        // THEN - Script should not be created
        StepVerifier.create(
                        scriptScheduleService.schedule(ScriptScheduleService.ScriptScheduleModel.builder().code(code).build())
                )
                .verifyErrorMatches(err -> {
                    assertInstanceOf(InvalidJSProvidedException.class, err);
                    assertEquals(
                            "SyntaxError: <eval>:1:9 Expected ident but found .\n" +
                                    "some_var..call()\n" +
                                    "         ^\n",
                            err.getMessage()
                    );
                    return true;
                });

        assertEquals(0, scriptStore.findAll().collectList().block().size());
    }

    @Test
    public void userShouldBeAbleToExecuteJs() {
        // GIVEN
        String code = "print('Hi');";

        // WHEN
        scriptExecuteService.execute(
                Script.builder()
                        .id("1")
                        .name("Script 1")
                        .code(code)
                        .status(ScriptStatus.NEW)
                        .build(),
                new StringWriter()
        ).block();

        // THEN
        var storedScript = scriptStore.findById("1").block();
        assertNotNull(storedScript);

        assertEquals(ScriptStatus.COMPLETED, storedScript.getStatus());
        assertEquals("Hi\n", storedScript.getOutput());
        assertNull(storedScript.getError());
    }

    @Test
    public void ifExecutionThrowsAnErrorItShouldBeSaved() {
        // GIVEN
        String code = "print('Hi');" +
                "some_undefined_var.call()";

        // WHEN
        scriptExecuteService.execute(
                Script.builder()
                        .id("1")
                        .name("Script 1")
                        .code(code)
                        .status(ScriptStatus.NEW)
                        .build(),
                new StringWriter()
        ).block();

        // THEN
        var storedScript = scriptStore.findById("1").block();
        assertNotNull(storedScript);

        assertEquals(ScriptStatus.ERRORED, storedScript.getStatus());
        assertEquals("ReferenceError: some_undefined_var is not defined", storedScript.getError());
        assertEquals("Hi\n", storedScript.getOutput());
    }

    @Test
    public void userShouldBeAbleToStopScriptExecution() {
        // GIVEN - script with infinite loop is executing
        scriptStore.save(
                Script.builder()
                        .id("1")
                        .name("Script 1")
                        .code("while(true) { }")
                        .status(ScriptStatus.NEW)
                        .build()
        ).block();
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .until(() -> scriptStore.findById("1").block().getStatus() == ScriptStatus.EXECUTING);

        // WHEN - we stop this script
        scriptExecuteService.changeExecution(
                "1",
                ScriptExecuteService.ChangeExecutionModel.builder()
                        .action(ScriptExecuteService.ChangeExecutionAction.STOP)
                        .build()
        ).block();

        // THEN - script should be stopped
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .untilAsserted(() -> {
                    var stoppedScript = scriptStore.findById("1").block();
                    assertEquals(ScriptStatus.STOPPED, stoppedScript.getStatus());
                });
    }

}
