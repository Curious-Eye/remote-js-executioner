package pragmasoft.andriilupynos.js_executioner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.data.ScriptStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Script;
import pragmasoft.andriilupynos.js_executioner.exception.ScriptNotFoundException;
import pragmasoft.andriilupynos.js_executioner.service.ScriptDeleteService;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class ScriptManagementTests {

    @Autowired private ScriptDeleteService scriptDeleteService;
    @Autowired private ScriptStore scriptStore;

    @Test
    public void userShouldBeAbleToDeleteScript() {
        // GIVEN
        scriptStore.save(
                Script.builder()
                        .id("1")
                        .build()
        ).block();

        // WHEN
        scriptDeleteService.deleteById("1").block();

        // THEN
        assertNull(scriptStore.findById("1").block());
    }

    @Test
    public void ifUserDeletesNonExistentScriptExceptionShouldBeThrown() {
        // GIVEN
        scriptStore.save(
                Script.builder()
                        .id("1")
                        .build()
        ).block();

        // WHEN
        StepVerifier.create(scriptDeleteService.deleteById("2"))
        // THEN
                .expectError(ScriptNotFoundException.class)
                .verify();
    }

}
