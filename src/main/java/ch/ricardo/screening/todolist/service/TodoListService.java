package ch.ricardo.screening.todolist.service;

import java.util.List;
import java.util.Optional;

import ch.ricardo.screening.todolist.model.TodoItem;

/**
 * Created by tmi on 19/04/18.
 */
public interface TodoListService {

    List<TodoItem> retrieveAllItems();

    Optional<TodoItem> findItemById(Long id);

    boolean exists(TodoItem item);

    TodoItem create(TodoItem item);

    boolean updateItem(Long id, TodoItem currentItem);

    boolean deleteItemById(Long id);

    void deleteAllItems();
}
