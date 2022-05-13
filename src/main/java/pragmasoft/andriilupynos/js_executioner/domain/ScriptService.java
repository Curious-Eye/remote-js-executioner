package pragmasoft.andriilupynos.js_executioner.domain;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ScriptService {

    ScriptInfo create(String code, String name) throws ScriptException;

    default ScriptInfo create(String code) throws ScriptException {
        return create(code, UUID.randomUUID().toString());
    }

    ScriptExecution execute(ScriptInfo scriptInfo) throws ScriptException;

    void stopExecution(String name);

    Optional<ScriptExecution> executionOf(ScriptInfo scriptInfo) throws ScriptException;

    ScriptInfo get(String name);

    /**
     * If parameter is null, then it does not affect returned data
     */
    Collection<ScriptInfo> all(SortBy by, ScriptInfo.Status status) throws ScriptException;

    default Iterable<ScriptInfo> all() throws ScriptException {
        return all(SortBy.CREATED, null);
    }

    void delete(String name);

    enum SortBy {
        NAME, STATUS, CREATED
    }

}
