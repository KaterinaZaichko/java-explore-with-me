package ru.practicum.dto.user;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class NewUserRequest {
    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    private String email;
    @NotBlank(message = "Field: name. Error: must not be blank. Value: null")
    @Size(min = 2, max = 250)
    private String name;
}
