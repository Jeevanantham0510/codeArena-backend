package com.codearena.dto.request;
import com.codearena.entity.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RunRequest {

    @NotNull
    private Language language;

    @NotBlank
    private String code;

    private String customInput = "";

}