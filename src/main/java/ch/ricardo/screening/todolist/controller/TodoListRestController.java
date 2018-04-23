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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import ch.ricardo.screening.todolist.model.TodoItem;
import ch.ricardo.screening.todolist.service.TodoListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value="Todo list", description="CRUD operations for managing items of the Todo list")
@RequestMapping(value = "/todolist/items", produces = {"application/json"})
public class TodoListRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TodoListRestController.class);

    @Autowired
    TodoListService todolistService;

    @ApiOperation(value = "Retrieve all items of the Todo List", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All items successfully retrieved")
    })
    @RequestMapping(method = RequestMethod.GET)
    public Collection<TodoItem> retrieveAllItems() {
        LOGGER.info("Retrieving all items");
        return todolistService.retrieveAllItems();
    }

    @ApiOperation(value = "Retrieve an item of the Todo List given its id", response = TodoItem.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Item successfully retrieved"),
            @ApiResponse(code = 404, message = "Item not found")
    })
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

    @ApiOperation(value = "Create a new Todo item given its content. Provided id is ignored.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Item successfully created"),
            @ApiResponse(code = 409, message = "An existing item has the same content. Not created")
    })
    @RequestMapping(method = RequestMethod.POST, consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
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

    @ApiOperation(value = "Update the content of an existing Todo item. Id provided in the input item is ignored.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Item successfully updated"),
            @ApiResponse(code = 404, message = "Item not found")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = {"application/json"})
    public ResponseEntity<TodoItem> updateItem(@PathVariable("id") Long id, @RequestBody TodoItem updateItem) {
        LOGGER.info("Updating item id {} with new content {}", id, updateItem.getContent());
        if (todolistService.updateItem(id, updateItem)) {
            return retrieveItem(id);
        } else {
            LOGGER.warn("Unable to update, item id {} was not found", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Delete an existing Todo item given its id")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Item successfully deleted"),
            @ApiResponse(code = 404, message = "Item not found")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteItem(@PathVariable("id") Long id) {
        LOGGER.info("Deleting item id {}", id);
        HttpStatus responseStatus = todolistService.deleteItemById(id) ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(responseStatus);
    }

    @ApiOperation(value = "Delete all items of the Todo list and reset the id counter")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "All items deleted")
    })
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteAllItems() {
        LOGGER.info("Deleting all items");
        todolistService.deleteAllItems();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
