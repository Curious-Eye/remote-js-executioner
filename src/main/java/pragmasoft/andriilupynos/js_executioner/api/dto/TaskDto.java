package pragmasoft.andriilupynos.js_executioner.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private String id;
    private String name;
    private String code;
    private String output;
    private String error;
    private TaskStatusDto status;
    private Date scheduledAt;
    private Date beginExecDate;
    private Date endExecDate;
    private Date createdDate;
}
