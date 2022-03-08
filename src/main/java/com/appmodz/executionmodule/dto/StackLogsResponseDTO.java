package com.appmodz.executionmodule.dto;

import java.util.List;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class StackLogsResponseDTO {
    List<StackButtonStateDTO> state;
    List<StackMessageDTO> message;
}
