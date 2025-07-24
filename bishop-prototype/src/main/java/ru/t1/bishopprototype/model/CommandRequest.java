package ru.t1.bishopprototype.model;

import lombok.Data;

import java.util.Map;

@Data
public class CommandRequest {
    private CommandType commandType;
    private CommandAuthor author;
    private Map<String, Object> params;
}
