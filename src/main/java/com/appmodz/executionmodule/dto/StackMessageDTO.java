package com.appmodz.executionmodule.dto;

import java.util.Date;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class StackMessageDTO {
    String type;
    String text;
    Date time;
    Object content;
}
