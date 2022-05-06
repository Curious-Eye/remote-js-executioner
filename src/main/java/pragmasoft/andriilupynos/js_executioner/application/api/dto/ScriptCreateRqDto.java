package pragmasoft.andriilupynos.js_executioner.application.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptCreateRqDto {
    private String code;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date executionDate;
}
