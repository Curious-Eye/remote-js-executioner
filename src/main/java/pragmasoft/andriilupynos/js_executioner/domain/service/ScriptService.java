package pragmasoft.andriilupynos.js_executioner.domain.service;

import pragmasoft.andriilupynos.js_executioner.domain.model.exception.ScriptNotFoundException;
import pragmasoft.andriilupynos.js_executioner.domain.model.script.*;

import java.util.Date;
import java.util.List;

public interface ScriptService {

    /**
     * Schedules the script for future execution. The script will
     * be executed either as soon as possible after provided scheduledAt date,
     * or just as soon as possible
     *
     * @param code code to execute
     * @param scheduledAt date after which to execute
     * @return scheduled script
     */
    String scheduleScript(String code, Date scheduledAt);

    /**
     * Returns the script with given id with up-to-date output
     * or throws {@link ScriptNotFoundException} if it does not exist.
     *
     * @param id id of the script
     * @return found script
     */
    Script getFullInfoById(String id);

    /**
     * Returns short info about script that match provided filter.
     * If field in the filter is null, then that field does not influence
     * returned scripts.
     *
     * @param filter filter for scripts
     * @return short info about scripts matching filter
     */
    List<ScriptShortInfo> getShortInfoMatching(ScriptFindFilter filter);

    /**
     * Returns script's execution info
     *
     * @param id id of the script
     * @return returns script's execution info
     * @throws ScriptNotFoundException when script with such id does not exist
     */
    Execution getExecutionInfo(String id);

    /**
     * Changes execution of the script with given id.
     * If it is not being executed, then does nothing.
     * If script with such id does not exist, then throws {@link ScriptNotFoundException}
     *
     * @param id id of the script
     * @return returns stopped script
     * @throws ScriptNotFoundException when script with such id does not exist
     */
    Script changeExecution(String id, ExecutionStatus status);

    /**
     * Deletes the script with given id. If such script does
     * not exist, then throws {@link ScriptNotFoundException}.
     * If script is being executed, execution is terminated.
     *
     * @param id id of the script
     * @throws ScriptNotFoundException when script with such id does not exist
     */
    void deleteById(String id);

    /**
     * Deletes all scripts
     */
    void deleteAll();
}
