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
public class ScriptDto {
    private String id;
    private String name;
    private String code;
    private String output;
    private String error;
    private ScriptStatusDto status;
    private Date scheduledAt;
    private Date beginExecDate;
    private Date endExecDate;
    private Date createdDate;
}
