package pragmasoft.andriilupynos.js_executioner.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.domain.exception.ScriptNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ScriptManagementTests {

    @Autowired private ScriptService scriptService;

    @Test
    void userShouldBeAbleToDeleteScript() {
        // GIVEN
        scriptService.create("print('Hi');", "1");

        // WHEN
        scriptService.delete("1");

        // THEN
        assertThrows(ScriptNotFoundException.class, () -> scriptService.get("1"));
    }

    @Test
    void ifUserDeletesNonExistentScriptExceptionShouldBeThrown() {
        // GIVEN
        scriptService.create("print('Hi');", "1");
        scriptService.delete("1");

        // WHEN - we stop non-existent script
        // THEN - Exception should be thrown
        assertThrows(ScriptNotFoundException.class, () -> scriptService.delete("1"));
    }

}
