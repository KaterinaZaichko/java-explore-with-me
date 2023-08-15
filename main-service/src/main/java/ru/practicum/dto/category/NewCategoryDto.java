package ru.practicum.dto.category;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class NewCategoryDto {
    @NotBlank(message = "Field: name. Error: must not be blank. Value: null")
    @Size(min = 1, max = 50)
    private String name;
}
