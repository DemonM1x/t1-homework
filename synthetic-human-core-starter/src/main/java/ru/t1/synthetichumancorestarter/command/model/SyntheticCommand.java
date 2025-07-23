package ru.t1.synthetichumancorestarter.command.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SyntheticCommand {
    @Size(max = 1000)
    @NotBlank
    private String description;

    private CommandPriority priority;

    @Size(max = 100)
    @NotBlank
    private String author;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])T([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(\\.\\d{1,9})?([+-][01]\\d:[0-5]\\d|Z)?$")
    private String time;

}
