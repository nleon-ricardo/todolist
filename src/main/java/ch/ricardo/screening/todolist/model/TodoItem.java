package ch.ricardo.screening.todolist.model;

import io.swagger.annotations.ApiModelProperty;

public class TodoItem {

    private static final Long DEFAULT_ID_VALUE = -1L;

    @ApiModelProperty(notes = "Auto-generated identifier", required = false)
    private Long id = DEFAULT_ID_VALUE;

    @ApiModelProperty(notes = "Content of the Todo item")
    private String content;

    public TodoItem() {}

    public TodoItem(String content) {
        this.content = content;
    }

    public TodoItem(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
