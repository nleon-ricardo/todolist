package ch.ricardo.screening.todolist.controller;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import ch.ricardo.screening.todolist.model.TodoItem;
import ch.ricardo.screening.todolist.service.TodoListService;

@RestController
@RequestMapping("/todolist/items")
public class TodoListRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoListRestController.class);

    @Autowired
    TodoListService todolistService;

    @RequestMapping(method = RequestMethod.GET)
    public Collection<TodoItem> retrieveAllItems() {
        LOGGER.info("Retrieving all items");
        return todolistService.retrieveAllItems();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<TodoItem> retrieveItem(@PathVariable("id") Long id) {
        LOGGER.info("Retrieving item id {}", id);
        Optional<TodoItem> maybeItem = todolistService.findItemById(id);
        if (maybeItem.isPresent()) {
            return new ResponseEntity<>(maybeItem.get(), HttpStatus.OK);
        } else {
            LOGGER.warn("Unable to retrieve item id {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Create an item
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> createItem(@RequestBody TodoItem item, UriComponentsBuilder ucBuilder) {
        LOGGER.info("Creating new item with content {}", item.getContent());
        if (todolistService.exists(item)) {
            LOGGER.warn("Unable to create new item, another item already has the same content: {}", item.getContent());
            return new ResponseEntity(HttpStatus.CONFLICT);
        } else {
            TodoItem createdItem = todolistService.create(item);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(ucBuilder.path("/todolist/items/{id}").buildAndExpand(createdItem.getId()).toUri());
            return new ResponseEntity<>(headers, HttpStatus.CREATED);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<TodoItem> updateItem(@PathVariable("id") Long id, @RequestBody TodoItem updateItem) {
        LOGGER.info("Updating item id {} with new content {}", id, updateItem.getContent());
        if (todolistService.updateItem(id, updateItem)) {
            return retrieveItem(id);
        } else {
            LOGGER.warn("Unable to update, item id {} was not found", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteItem(@PathVariable("id") Long id) {
        LOGGER.info("Deleting item id {}", id);
        HttpStatus responseStatus = todolistService.deleteItemById(id) ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(responseStatus);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteAllItems() {
        LOGGER.info("Deleting all items");
        todolistService.deleteAllItems();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
