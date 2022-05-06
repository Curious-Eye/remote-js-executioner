package pragmasoft.andriilupynos.js_executioner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pragmasoft.andriilupynos.js_executioner.domain.model.exception.ScriptNotFoundException;
import pragmasoft.andriilupynos.js_executioner.domain.service.ScriptService;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class ScriptManagementTests {

    @Autowired private ScriptService scriptService;

    @Test
    public void userShouldBeAbleToDeleteScript() {
        // GIVEN
        var id = scriptService.scheduleScript("while(true){}", null);

        // WHEN
        scriptService.deleteById(id);

        // THEN
        assertThrows(ScriptNotFoundException.class, () -> scriptService.getFullInfoById(id));
    }

    @Test
    public void ifUserDeletesNonExistentScriptExceptionShouldBeThrown() {
        // GIVEN
        var id = scriptService.scheduleScript("print('Hi')", null);

        // WHEN - we stop non-existent script
        // THEN - Exception should be thrown
        assertThrows(ScriptNotFoundException.class, () -> scriptService.deleteById("2"));
    }

}
