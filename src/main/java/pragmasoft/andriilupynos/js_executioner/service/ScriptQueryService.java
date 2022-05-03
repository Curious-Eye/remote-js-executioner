package pragmasoft.andriilupynos.js_executioner.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.data.ScriptStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Script;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptStatus;
import pragmasoft.andriilupynos.js_executioner.exception.ScriptNotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
public class ScriptQueryService {

    @Autowired private ScriptStore scriptStore;
    @Autowired private ScriptExecuteService scriptExecuteService;

    /**
     * Returns information about the script, including current script output,
     * or Mono.error(ScriptNotFoundException) if script with such id does not exist
     *
     * @param id script id
     * @return Mono with script information
     */
    public Mono<Script> getScriptWithCurrentOutputById(String id) {
        return scriptStore.findById(id)
                .map(script -> {
                    if (script.getStatus() == ScriptStatus.EXECUTING)
                        script.setOutput(scriptExecuteService.getCurrentExecutionOutput(script.getId()));
                    return script;
                })
                .switchIfEmpty(Mono.error(new ScriptNotFoundException("Script with such id does not exist")));
    }

    /**
     * Find all scripts. If status is provided then return only scripts
     * with given status. If newFirst is provided and it's true, then results
     * are returned from newest to oldest scripts, if it's false, then from
     * oldest to newest. If newFirst is not provided, then ordering is not guaranteed
     *
     * Note: ordering should be moved to DB level, but for simplicity I left it here.
     *
     * @param filter filter to apply to returned scripts, includes sorting
     * @return scripts matching given parameters
     */
    public Flux<Script> search(ScriptSearchModel filter) {
        var findFlux = scriptStore.findAll();
        if (filter.status != null)
            findFlux = findFlux.filter(script -> script.getStatus().equals(filter.status));
        if (filter.name != null)
            findFlux = findFlux.filter(script -> script.getName().equals(filter.name));
        if (filter.newFirst != null) {
            Comparator<Script> comparator;
            if (filter.newFirst)
                comparator = Comparator.comparing(Script::getCreatedDate).reversed();
            else
                comparator = Comparator.comparing(Script::getCreatedDate);
            findFlux = findFlux.sort(comparator);
        }
        return findFlux;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScriptSearchModel {
        private String name;
        private ScriptStatus status;
        private Boolean newFirst;
    }

}
