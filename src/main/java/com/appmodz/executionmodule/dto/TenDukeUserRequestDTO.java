package com.appmodz.executionmodule.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

@lombok.Getter
@lombok.Setter
public class TenDukeUserRequestDTO {
    String lastName;
    String firstName;
    String id;
    String email;
    String phoneNumber;
    TenDukeAddressDTO address;
    @SneakyThrows
    @JsonIgnoreProperties
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}