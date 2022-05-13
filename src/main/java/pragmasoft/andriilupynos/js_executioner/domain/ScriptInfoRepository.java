package pragmasoft.andriilupynos.js_executioner.domain;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ScriptInfoRepository {

    void store(ScriptInfo s) throws ScriptException;

    boolean delete(ScriptInfo s) throws ScriptException;

    ScriptInfo get(String name) throws ScriptException;

    Iterable<ScriptInfo> all(ScriptService.SortBy by) throws ScriptException;

    @SuppressWarnings("unused")
    default Stream<ScriptInfo> allAsStream(ScriptService.SortBy by, boolean parallel) throws ScriptException {
        return StreamSupport.stream(all(by).spliterator(), parallel);
    }

    @SuppressWarnings("unused")
    default Iterable<ScriptInfo> all() throws ScriptException {
        return all(ScriptService.SortBy.CREATED);
    }

}
