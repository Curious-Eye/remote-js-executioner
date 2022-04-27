package pragmasoft.andriilupynos.js_executioner.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskChangeExecutionRqDto {

    private ChangeExecutionAction action;

    public enum ChangeExecutionAction {
        STOP
    }
}
