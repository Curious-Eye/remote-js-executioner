package pragmasoft.andriilupynos.js_executioner.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import pragmasoft.andriilupynos.js_executioner.data.ScriptStore;
import pragmasoft.andriilupynos.js_executioner.data.domain.Script;
import pragmasoft.andriilupynos.js_executioner.data.domain.ScriptStatus;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

@Service
public class ScriptScheduleService {

    private final Logger log = LoggerFactory.getLogger(ScriptScheduleService.class);

    @Autowired private ScriptStore scriptStore;
    @Autowired private ScriptValidateService scriptValidateService;

    /**
     * Schedule a script for future execution
     *
     * @param scriptModel Model of the script to schedule
     * @return Mono with scheduled script
     */
    public Mono<Script> schedule(ScriptScheduleModel scriptModel) {
        log.debug("Trying to schedule new script: {}", scriptModel);
        var script =
                Script.builder()
                        .code(scriptModel.getCode())
                        .status(ScriptStatus.NEW)
                        .name(this.getNameOrGenerateNew(scriptModel))
                        .scheduledAt(scriptModel.getExecutionDate())
                        .createdDate(new Date())
                        .build();

        return scriptValidateService.validate(script)
                .then(scriptStore.save(script))
                .doOnNext(savedScript -> log.debug("Scheduled new script: {}", savedScript));
    }

    private String getNameOrGenerateNew(ScriptScheduleModel script) {
        if (StringUtils.hasText(script.getName()))
            return script.getName();
        return UUID.randomUUID().toString();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScriptScheduleModel {
        private String name;
        private String code;
        private Date executionDate;
    }

}
