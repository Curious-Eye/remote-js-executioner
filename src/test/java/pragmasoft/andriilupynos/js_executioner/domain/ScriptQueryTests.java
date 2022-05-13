package pragmasoft.andriilupynos.js_executioner.domain;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.util.CurrentClock;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ScriptQueryTests {

    @Autowired private ScriptService scriptService;

    @BeforeEach
    public void deleteAllScripts() {
        scriptService.all().forEach(s -> scriptService.delete(s.name));
    }

    @Test
    void userShouldBeAbleToGetInfoAboutScript() {
        // GIVEN
        var code = "print('Hi');";
        scriptService.create(code, "1");

        // WHEN
        var scriptInfo = scriptService.get("1");

        // THEN
        assertNotNull(scriptInfo);
        assertEquals("1", scriptInfo.name);
        assertEquals(code, scriptInfo.script.code);
        assertEquals(ScriptInfo.Status.SCHEDULED, scriptInfo.getStatus());
    }

    @Test
    void userShouldBeAbleToGetInfoAboutScriptExecution() {
        var mockedClock = Mockito.mock(Clock.class);

        // GIVEN - running script
        var script = scriptService.create("print('Hi'); while(true) {}", "1");

        Mockito.when(mockedClock.instant()).thenReturn(Instant.parse("2022-05-11T00:00:00Z"));
        CurrentClock.set(mockedClock);
        scriptService.execute(script);
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertEquals(ScriptInfo.Status.RUNNING, scriptService.get("1").getStatus()));

        // WHEN
        var execution = scriptService.executionOf(script).orElse(null);

        // THEN
        assertNotNull(execution);
        assertEquals(ScriptInfo.Status.RUNNING, execution.getStatus());
        assertEquals(Instant.parse("2022-05-11T00:00:00Z"), execution.getStarted().orElseThrow());
    }

    @Test
    void userShouldBeAbleToSearchScriptsByStatus() {
        // GIVEN
        scriptService.execute(scriptService.create("while(true) {}", "1"));
        scriptService.execute(scriptService.create("print('hi 2');", "2"));
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .untilAsserted(() -> {
                    assertEquals(
                            ScriptInfo.Status.RUNNING,
                            scriptService.get("1").getStatus()
                    );
                    assertEquals(
                            ScriptInfo.Status.COMPLETED,
                            scriptService.get("2").getStatus()
                    );
                });

        // WHEN
        var res = scriptService.all(null, ScriptInfo.Status.RUNNING);

        // THEN
        assertEquals(1, res.size());
        var foundScript = res.stream().findFirst().orElseThrow();
        assertEquals("1", foundScript.name);
        assertEquals("while(true) {}", foundScript.script.code);
        assertEquals(ScriptInfo.Status.RUNNING, foundScript.getStatus());

        // WHEN
        res = scriptService.all(null, ScriptInfo.Status.COMPLETED);

        // THEN
        assertEquals(1, res.size());
        foundScript = res.stream().findFirst().orElseThrow();
        assertEquals("2", foundScript.name);
        assertEquals("print('hi 2');", foundScript.script.code);
        assertEquals(ScriptInfo.Status.COMPLETED, foundScript.getStatus());
    }

    @Test
    void userShouldBeAbleToSearchScriptsWithOrderByCreationDate() {
        var mockedClock = Mockito.mock(Clock.class);
        Mockito.when(mockedClock.instant())
                .thenReturn(Instant.parse("2022-05-11T00:00:00Z"), Instant.parse("2022-05-11T00:00:02Z"));
        CurrentClock.set(mockedClock);

        // GIVEN
        scriptService.create("while(true) {}", "1");
        scriptService.create("print('hi 2');", "2");

        // WHEN
        var res = scriptService.all(ScriptService.SortBy.CREATED, null).toArray(new ScriptInfo[]{});

        // THEN
        assertEquals(2, res.length);
        assertEquals("2", res[0].name);
        assertEquals("1", res[1].name);
    }

    @Test
    void userShouldBeAbleToSearchScriptsWithComplexFilter() {
        var mockedClock = Mockito.mock(Clock.class);
        Mockito.when(mockedClock.instant())
                .thenReturn(
                        Instant.parse("2022-05-11T00:00:01Z"),
                        Instant.parse("2022-05-11T00:00:02Z"),
                        Instant.parse("2022-05-11T00:00:03Z"),
                        Instant.parse("2022-05-11T00:00:04Z")
                );
        CurrentClock.set(mockedClock);

        // GIVEN
        var script1 = scriptService.create("print('hi 1');", "1");
        var script2 = scriptService.create("while(true) {}", "2");
        var script3 = scriptService.create("while(true) {}", "3");
        var script4 = scriptService.create("print('hi 4');", "4");
        scriptService.execute(script1);
        scriptService.execute(script2);
        scriptService.execute(script3);
        scriptService.execute(script4);
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertEquals(ScriptInfo.Status.COMPLETED, scriptService.get("1").getStatus());
                    assertEquals(ScriptInfo.Status.RUNNING, scriptService.get("2").getStatus());
                    assertEquals(ScriptInfo.Status.RUNNING, scriptService.get("3").getStatus());
                    assertEquals(ScriptInfo.Status.COMPLETED, scriptService.get("4").getStatus());
                });

        // WHEN
        var res =
                scriptService.all(ScriptService.SortBy.CREATED, ScriptInfo.Status.RUNNING)
                        .toArray(new ScriptInfo[]{});

        // THEN
        assertEquals(2, res.length);
        assertEquals("3", res[0].name);
        assertEquals("2", res[1].name);
    }

}
