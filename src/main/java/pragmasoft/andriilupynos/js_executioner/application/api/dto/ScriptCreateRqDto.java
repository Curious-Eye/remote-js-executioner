package pragmasoft.andriilupynos.js_executioner.application.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptCreateRqDto {
    private String code;
    private String name;
}
