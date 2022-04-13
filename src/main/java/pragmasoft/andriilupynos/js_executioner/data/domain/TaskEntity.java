package pragmasoft.andriilupynos.js_executioner.data.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    private String id;
    private String name;
    private String code;
    private TaskStatus status;

}

