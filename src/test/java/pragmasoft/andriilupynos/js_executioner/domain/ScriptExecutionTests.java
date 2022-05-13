package pragmasoft.andriilupynos.js_executioner.domain;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.domain.exception.InvalidScriptProvidedException;
import pragmasoft.andriilupynos.js_executioner.util.CurrentClock;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ScriptExecutionTests {

    @Autowired private ScriptService scriptService;
    @Autowired private ScriptInfoRepository scriptInfoRepository;

    @Test
    void userMustBeAbleToCreateScript() {
        // GIVEN
        String code = "console.log(\"Hi\")";
        String name = "Hi printer";

        // WHEN - Schedule code execution
        var scriptInfo = scriptService.create(code, name);

        // THEN - Script should be created
        assertEquals("Hi printer", scriptInfo.name);
        assertNotNull(scriptInfo.created);
        assertEquals(ScriptInfo.Status.SCHEDULED, scriptInfo.getStatus());
        assertEquals("", scriptInfo.getOut());
        assertEquals("", scriptInfo.getErr());
        assertNotNull(scriptInfo.script);

        var storedScript = scriptService.get(name);
        assertNotNull(storedScript);
        assertNotNull(storedScript.created);
        assertEquals(ScriptInfo.Status.SCHEDULED, storedScript.getStatus());
        assertEquals("", storedScript.getOut());
        assertEquals("", storedScript.getErr());
    }

    @Test
    void userMustNotBeAbleToCreateInvalidScript() {
        // GIVEN
        String code = "some_var..call()";

        // WHEN - Schedule invalid code execution
        // THEN - Script should not be created
        var ex = assertThrows(
                InvalidScriptProvidedException.class,
                () -> scriptService.create(code)
        );
        assertEquals(
                "Invalid script provided. Details:\n" +
                        "SyntaxError: Unnamed:1:9 Expected ident but found .\n" +
                        "some_var..call()\n" +
                        "         ^\n",
                ex.getMessage()
        );
    }

    @Test
    void userMustBeAbleToExecuteScript() throws ExecutionException, InterruptedException {
        // GIVEN
        String name = "Hi printer";
        String code = "print('Hi');";
        var scriptInfo = scriptService.create(code, name);

        var clock = Mockito.mock(Clock.class);
        var started = Instant.parse("2022-05-11T00:00:00Z");
        var finished = started.plus(Duration.ofMillis(2));
        Mockito.when(clock.instant()).thenReturn(started, finished);
        var old = CurrentClock.get();
        CurrentClock.set(clock);

        // WHEN
        ScriptExecution execution = scriptService.execute(scriptInfo);
        execution.get();

        // THEN
        assertEquals(started, execution.started);
        assertEquals(finished, execution.finished);
        //noinspection OptionalGetWithoutIsPresent
        assertEquals(2, execution.getDuration().get().toMillis());
        assertEquals(ScriptInfo.Status.COMPLETED, execution.getStatus());
        assertEquals("Hi\n", execution.scriptInfo.getOut());
        assertEquals("", execution.scriptInfo.getErr());

        CurrentClock.set(old);
    }

    @Test
    void allExecutionErrorsShouldBeSaved() throws ExecutionException, InterruptedException {
        // GIVEN
        String code = "print('Hi');" +
                "console.error('Std err');" +
                "function throwsErr() { some_undefined_var.call() }" +
                "throwsErr();";
        var scriptInfo = scriptService.create(code);

        // WHEN
        ScriptExecution execution = scriptService.execute(scriptInfo);
        execution.get();

        // THEN
        var storedScript = scriptInfoRepository.get(scriptInfo.name);
        assertEquals(ScriptInfo.Status.ERROR, storedScript.getStatus());
        assertEquals(
                "Std err\n" +
                "ReferenceError: some_undefined_var is not defined\n" +
                "Stack trace:\n" +
                "<js>.throwsErr(Unnamed:1)\n" +
                "<js>.:program(Unnamed:1)\n",
                storedScript.getErr()
        );
        assertEquals("Hi\n", storedScript.getOut());
    }

    @Test
    void userShouldBeAbleToStopScriptExecution() {
        // GIVEN
        String code = "while(true) {}";
        var scriptInfo = scriptService.create(code);
        ScriptExecution execution = scriptService.execute(scriptInfo);
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .until(() -> execution.getStatus() == ScriptInfo.Status.RUNNING);

        // WHEN
        execution.cancel(true);

        // THEN - script should be stopped
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertEquals(ScriptInfo.Status.ERROR, execution.getStatus()));
        var storedScriptInfo = scriptInfoRepository.get(scriptInfo.name);
        assertEquals("", storedScriptInfo.getOut());
        assertEquals(
                "Thread was interrupted.\n" +
                "Stack trace:\n" +
                "<js>.:program(Unnamed:1)\n",
                storedScriptInfo.getErr()
        );
    }

}
