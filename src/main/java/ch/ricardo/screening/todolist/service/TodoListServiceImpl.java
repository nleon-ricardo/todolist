package ch.ricardo.screening.todolist.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import ch.ricardo.screening.todolist.model.TodoItem;

@Service("todolistService")
public class TodoListServiceImpl implements TodoListService {

    private static final AtomicLong counter = new AtomicLong();

    private static List<TodoItem> items = new ArrayList<TodoItem>();

    @Override
    public List<TodoItem> retrieveAllItems() {
        return items;
    }

    @Override
    public Optional<TodoItem> findItemById(Long id) {
        return items.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    @Override
    public boolean exists(TodoItem item) {
        return findItemByContent(item.getContent()).isPresent();
    }

    @Override
    public TodoItem create(TodoItem item) {
        TodoItem createdItem = new TodoItem(counter.incrementAndGet(), item.getContent());
        items.add(createdItem);
        return createdItem;
    }

    @Override
    public boolean updateItem(Long id, TodoItem updatedItem) {
        if (updatedItem == null) {
            return false;
        } else {
            Optional<TodoItem> item = findItemById(id);
            item.ifPresent(originalItem -> {
                int index = items.indexOf(originalItem);
                // I want to keep my todoItems immutable, so i'm replacing the item by a new one instead of mutating it
                items.set(index, new TodoItem(id, updatedItem.getContent()));
            });
            return item.isPresent();
        }
    }

    @Override
    public boolean deleteItemById(Long id) {
        return items.removeIf(item -> item.getId().equals(id));
    }

    @Override
    public void deleteAllItems() {
        items.clear();
        // reset counter
        counter.set(0);
    }

    private Optional<TodoItem> findItemByContent(String content) {
        return items.stream().filter(item -> item.getContent().equals(content)).findFirst();
    }
}
