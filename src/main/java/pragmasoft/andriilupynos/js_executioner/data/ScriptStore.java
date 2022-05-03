package pragmasoft.andriilupynos.js_executioner.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.data.domain.Script;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptEntity;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptStatus;
import pragmasoft.andriilupynos.js_executioner.data.repository.ScriptRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScriptStore {

    @Autowired private ScriptRepository scriptRepository;

    public Mono<Script> save(Script script) {
        return scriptRepository.save(convertToScriptEntity(script))
                .map(ScriptStore::convertToScript);
    }

    public Flux<Script> saveAll(Collection<Script> script) {
        return scriptRepository.saveAll(
                        script.stream()
                                .map(ScriptStore::convertToScriptEntity)
                                .collect(Collectors.toList())
                )
                .map(ScriptStore::convertToScript);
    }

    public Mono<Void> deleteById(String id) {
        return scriptRepository.deleteById(id);
    }

    public Mono<Void> deleteAll() {
        return scriptRepository.deleteAll();
    }

    public Flux<Script> findAllByStatusIn(List<ScriptStatus> statuses) {
        return scriptRepository.findAllByStatusIn(statuses)
                .map(ScriptStore::convertToScript);
    }

    public Mono<Script> findById(String scriptId) {
        return scriptRepository.findById(scriptId)
                .map(ScriptStore::convertToScript);
    }

    public Flux<Script> findAll() {
        return scriptRepository.findAll()
                .map(ScriptStore::convertToScript);
    }

    private static Script convertToScript(ScriptEntity scriptEntity) {
        return Script.builder()
                .id(scriptEntity.getId())
                .name(scriptEntity.getName())
                .code(scriptEntity.getCode())
                .output(scriptEntity.getOutput())
                .error(scriptEntity.getError())
                .status(scriptEntity.getStatus())
                .scheduledAt(scriptEntity.getScheduledAt())
                .beginExecDate(scriptEntity.getBeginExecDate())
                .endExecDate(scriptEntity.getEndExecDate())
                .createdDate(scriptEntity.getCreatedDate())
                .build();
    }

    private static ScriptEntity convertToScriptEntity(Script script) {
        return ScriptEntity.builder()
                .id(script.getId())
                .name(script.getName())
                .code(script.getCode())
                .output(script.getOutput())
                .error(script.getError())
                .status(script.getStatus())
                .scheduledAt(script.getScheduledAt())
                .beginExecDate(script.getBeginExecDate())
                .endExecDate(script.getEndExecDate())
                .createdDate(script.getCreatedDate())
                .build();
    }

}
