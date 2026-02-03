package com.maxzdosreis.products_api.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"id", "username", "fullname", "email", "enabled"})
public class UserResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("fullname")
    private String fullname;

    @JsonProperty("email")
    private String email;

    @JsonProperty("enabled")
    private Boolean enabled;
}
