package pragmasoft.andriilupynos.js_executioner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.data.ScriptStore;
import pragmasoft.andriilupynos.js_executioner.exception.ScriptNotFoundException;
import reactor.core.publisher.Mono;

@Service
public class ScriptDeleteService {

    private final Logger log = LoggerFactory.getLogger(ScriptDeleteService.class);

    @Autowired private ScriptExecuteService scriptExecuteService;
    @Autowired private ScriptStore scriptStore;

    /**
     * Delete a script by id.
     * Return Mono.error(ScriptNotFoundException) if script with such id does not exist
     *
     * @param id id of a script
     * @return Mono signaling when operation is complete
     */
    public Mono<Void> deleteById(String id) {
        log.debug("User deletes script with id {}", id);
        return scriptStore.findById(id)
                .switchIfEmpty(Mono.error(new ScriptNotFoundException("Script with such id does not exist")))
                .then(
                        scriptExecuteService.changeExecution(
                                        id,
                                        ScriptExecuteService.ChangeExecutionModel.builder()
                                                .action(ScriptExecuteService.ChangeExecutionAction.STOP)
                                                .build()
                                )
                                .thenReturn(1)
                                .onErrorReturn(1)
                )
                .then(scriptStore.deleteById(id))
                .then();
    }

}
