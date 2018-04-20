package ch.ricardo.screening.todolist.model;

/**
 * Created by tmi on 19/04/18.
 */
public class TodoItem {

    private static final Long DEFAULT_ID_VALUE = -1L;

    private Long id = DEFAULT_ID_VALUE;
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
