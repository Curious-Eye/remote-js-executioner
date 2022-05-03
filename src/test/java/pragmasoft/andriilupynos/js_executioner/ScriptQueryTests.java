package pragmasoft.andriilupynos.js_executioner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.data.ScriptStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Script;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptStatus;
import pragmasoft.andriilupynos.js_executioner.service.ScriptQueryService;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ScriptQueryTests {

    @Autowired private ScriptStore scriptStore;
    @Autowired private ScriptQueryService scriptQueryService;

    @BeforeEach
    public void clearDB() {
        scriptStore.deleteAll().block();
    }

    @Test
    public void userShouldBeAbleToSearchScriptsByName() {
        // GIVEN
        scriptStore.saveAll(
                List.of(
                        Script.builder()
                                .id("1")
                                .name("Query data script")
                                .status(ScriptStatus.STOPPED)
                                .build(),
                        Script.builder()
                                .id("2")
                                .name("Input data script")
                                .status(ScriptStatus.NEW)
                                .build()
                )
        ).collectList().block();

        // WHEN
        var res = scriptQueryService.search(
                ScriptQueryService.ScriptSearchModel.builder().name("Input data script").build()
        ).collectList().block();

        // THEN
        assertEquals(1, res.size());
        var foundScript = res.get(0);
        assertEquals("2", foundScript.getId());
        assertEquals("Input data script", foundScript.getName());
        assertEquals(ScriptStatus.NEW, foundScript.getStatus());
    }

    @Test
    public void userShouldBeAbleToSearchScriptsByStatus() {
        // GIVEN
        scriptStore.saveAll(
                List.of(
                        Script.builder()
                                .id("1")
                                .name("Script 1")
                                .status(ScriptStatus.STOPPED)
                                .build(),
                        Script.builder()
                                .id("2")
                                .name("Script 2")
                                .status(ScriptStatus.NEW)
                                .build()
                )
        ).collectList().block();

        // WHEN
        var res = scriptQueryService.search(
                ScriptQueryService.ScriptSearchModel.builder().status(ScriptStatus.STOPPED).build()
        ).collectList().block();

        // THEN
        assertEquals(1, res.size());
        var foundScript = res.get(0);
        assertEquals("1", foundScript.getId());
        assertEquals("Script 1", foundScript.getName());
        assertEquals(ScriptStatus.STOPPED, foundScript.getStatus());
    }

    @Test
    public void userShouldBeAbleToSearchScriptsWithOrderByCreationDate() {
        // GIVEN
        scriptStore.saveAll(
                List.of(
                        Script.builder()
                                .id("1")
                                .createdDate(new Date())
                                .build(),
                        Script.builder()
                                .id("2")
                                .createdDate(Date.from(new Date().toInstant().plusMillis(100)))
                                .build(),
                        Script.builder()
                                .id("3")
                                .createdDate(Date.from(new Date().toInstant().plusMillis(200)))
                                .build()
                )
        ).collectList().block();

        // WHEN
        var res = scriptQueryService.search(
                ScriptQueryService.ScriptSearchModel.builder().newFirst(false).build()
        ).collectList().block();

        // THEN
        assertEquals(3, res.size());
        assertEquals("1", res.get(0).getId());
        assertEquals("2", res.get(1).getId());
        assertEquals("3", res.get(2).getId());

        // WHEN
        res = scriptQueryService.search(
                ScriptQueryService.ScriptSearchModel.builder().newFirst(true).build()
        ).collectList().block();

        // THEN
        assertEquals(3, res.size());
        assertEquals("3", res.get(0).getId());
        assertEquals("2", res.get(1).getId());
        assertEquals("1", res.get(2).getId());
    }

    @Test
    public void userShouldBeAbleToSearchScriptsWithComplexFilter() {
        // GIVEN
        scriptStore.saveAll(
                List.of(
                        Script.builder()
                                .id("0")
                                .name("Test name")
                                .status(ScriptStatus.EXECUTING)
                                .createdDate(new Date())
                                .build(),
                        Script.builder()
                                .id("1")
                                .name("Test name")
                                .status(ScriptStatus.NEW)
                                .createdDate(Date.from(new Date().toInstant().plusMillis(100)))
                                .build(),
                        Script.builder()
                                .id("2")
                                .name("Test name")
                                .status(ScriptStatus.NEW)
                                .createdDate(Date.from(new Date().toInstant().plusMillis(200)))
                                .build(),
                        Script.builder()
                                .id("3")
                                .name("Stopped script")
                                .status(ScriptStatus.STOPPED)
                                .createdDate(Date.from(new Date().toInstant().plusMillis(300)))
                                .build()
                )
        ).collectList().block();

        // WHEN
        var res = scriptQueryService.search(
                ScriptQueryService.ScriptSearchModel.builder()
                        .status(ScriptStatus.NEW)
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
