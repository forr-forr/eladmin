package com.itheima.pogo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class MessageDTO {
    @NotNull
    private String sender;
    @NotNull

    private List<String> recipients;
}