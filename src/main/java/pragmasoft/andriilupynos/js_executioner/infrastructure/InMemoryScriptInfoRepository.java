package pragmasoft.andriilupynos.js_executioner.infrastructure;

import pragmasoft.andriilupynos.js_executioner.domain.ScriptException;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptInfo;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptInfoRepository;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptService.SortBy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class InMemoryScriptInfoRepository implements ScriptInfoRepository {

    private final ConcurrentMap<String, ScriptInfo> scripts = new ConcurrentHashMap<>();

    @Override
    public void store(ScriptInfo s) throws ScriptException {
        this.scripts.put(s.name, s);
    }

    @Override
    public boolean delete(ScriptInfo s) throws ScriptException {
        return this.scripts.remove(s.name) != null;
    }

    @Override
    public ScriptInfo get(String name) throws ScriptException {
        return this.scripts.get(name);
    }

    @Override
    public Iterable<ScriptInfo> all(SortBy by) throws ScriptException {
        return scripts.values()
                .stream()
                .sorted((o1, o2) -> {
                    if (SortBy.CREATED.equals(by)) {
                        return o2.created.compareTo(o1.created);
                    }
                    if (SortBy.STATUS.equals(by)) {
                        return o1.getStatus().compareTo(o2.getStatus());
                    }
                    if (SortBy.NAME.equals(by)) {
                        return o1.name.compareTo(o2.name);
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

}
