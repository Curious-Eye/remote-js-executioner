package pragmasoft.andriilupynos.js_executioner.domain.model.script;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@RequiredArgsConstructor
public class ScriptFindFilter {
    private final ExecutionStatus status;
    private final Boolean newFirst;
}
