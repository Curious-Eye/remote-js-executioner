package pragmasoft.andriilupynos.js_executioner;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.domain.model.script.ExecutionStatus;
import pragmasoft.andriilupynos.js_executioner.domain.model.script.ScriptFindFilter;
import pragmasoft.andriilupynos.js_executioner.domain.service.ScriptService;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ScriptQueryTests {

    @Autowired private ScriptService scriptService;

    @BeforeEach
    public void deleteAllScripts() {
        scriptService.deleteAll();
    }

    @Test
    public void userShouldBeAbleToGetInfoAboutScript() {
        // GIVEN
        var code = "print('Hi');";
        var id = scriptService.scheduleScript(code, null);
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .untilAsserted(() -> {
                    assertEquals(ExecutionStatus.COMPLETED,
                            scriptService.getFullInfoById(id).getExecutionInfo().getStatus());
                });

        // WHEN
        var script = scriptService.getFullInfoById(id);

        // THEN
        assertNotNull(script);
        assertEquals(id, script.getId());
        assertEquals(code, script.getCode());
        assertNotNull(script.getCreatedDate());
        var execution = script.getExecutionInfo();
        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertNull(execution.getInterruptionMsg());
        assertEquals("", execution.getError());
        assertEquals("Hi\n", execution.getOutput());
        assertNotNull(execution.getBeginExecDate());
        assertNotNull(execution.getEndExecDate());
    }

    @Test
    public void userShouldBeAbleToGetInfoAboutScriptExecution() {
        // GIVEN
        var code = "print('Hi'); while(true) {}";
        var id = scriptService.scheduleScript(code, null);
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .untilAsserted(() -> {
                    var execution = scriptService.getFullInfoById(id).getExecutionInfo();
                    assertEquals(ExecutionStatus.EXECUTING, execution.getStatus());
                    assertEquals("Hi\n", execution.getOutput());
                });

        // WHEN
        var execution = scriptService.getExecutionInfo(id);

        // THEN
        assertEquals(ExecutionStatus.EXECUTING, execution.getStatus());
        assertNull(execution.getInterruptionMsg());
        assertEquals("", execution.getError());
        assertEquals("Hi\n", execution.getOutput());
        assertNotNull(execution.getBeginExecDate());
        assertNull(execution.getEndExecDate());
    }

    @Test
    public void userShouldBeAbleToSearchScriptsByStatus() {
        // GIVEN
        var script1Id = scriptService.scheduleScript("while(true) {}", null);
        scriptService.scheduleScript("print('hi 2');", null);
        scriptService.changeExecution(script1Id, ExecutionStatus.STOPPED);
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .untilAsserted(() -> {
                    assertEquals(ExecutionStatus.STOPPED,
                            scriptService.getFullInfoById(script1Id).getExecutionInfo().getStatus());
                });

        // WHEN
        var res = scriptService.getShortInfoMatching(new ScriptFindFilter(ExecutionStatus.STOPPED, null));

        // THEN
        assertEquals(1, res.size());
        var foundScript = res.get(0);
        assertEquals(script1Id, foundScript.getId());
        assertEquals("while(true) {}", foundScript.getCode());
        assertNull(foundScript.getScheduledAt());
        assertNotNull(foundScript.getCreatedDate());
        assertEquals(ExecutionStatus.STOPPED, foundScript.getStatus());
    }

    @Test
    public void userShouldBeAbleToSearchScriptsWithOrderByCreationDate() throws InterruptedException {
        // GIVEN
        var script1Id = scriptService.scheduleScript("print('hi 1');", null);
        Thread.sleep(1);
        var script2Id = scriptService.scheduleScript("print('hi 2');", null);
        Thread.sleep(1);
        var script3Id = scriptService.scheduleScript("print('hi 3');", null);

        // WHEN
        var res = scriptService.getShortInfoMatching(new ScriptFindFilter(null, false));

        // THEN
        assertEquals(3, res.size());
        assertEquals(script1Id, res.get(0).getId());
        assertEquals(script2Id, res.get(1).getId());
        assertEquals(script3Id, res.get(2).getId());

        // WHEN
        res = scriptService.getShortInfoMatching(new ScriptFindFilter(null, true));

        // THEN
        assertEquals(3, res.size());
        assertEquals(script3Id, res.get(0).getId());
        assertEquals(script2Id, res.get(1).getId());
        assertEquals(script1Id, res.get(2).getId());
    }

    @Test
    public void userShouldBeAbleToSearchScriptsWithComplexFilter() throws InterruptedException {
        // GIVEN
        var script1Id = scriptService.scheduleScript("print('hi 1');", null);
        Thread.sleep(1);
        var script2Id = scriptService.scheduleScript("while(true) {}", null);
        Thread.sleep(1);
        var script3Id = scriptService.scheduleScript("while(true) {}", null);
        Thread.sleep(1);
        var script4Id = scriptService.scheduleScript("print('hi 4');", null);
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertEquals(ExecutionStatus.COMPLETED,
                            scriptService.getFullInfoById(script1Id).getExecutionInfo().getStatus());
                    assertEquals(ExecutionStatus.COMPLETED,
                            scriptService.getFullInfoById(script4Id).getExecutionInfo().getStatus());

                    assertEquals(ExecutionStatus.EXECUTING,
                            scriptService.getFullInfoById(script2Id).getExecutionInfo().getStatus());
                    assertEquals(ExecutionStatus.EXECUTING,
                            scriptService.getFullInfoById(script3Id).getExecutionInfo().getStatus());
                });

        // WHEN
        var res =
                scriptService.getShortInfoMatching(new ScriptFindFilter(ExecutionStatus.EXECUTING, false));

        // THEN
        assertEquals(2, res.size());
        assertEquals(script2Id, res.get(0).getId());
        assertEquals(script3Id, res.get(1).getId());
    }

}
