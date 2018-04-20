package ch.ricardo.screening.todolist.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import ch.ricardo.screening.todolist.TodoListApplication;
import ch.ricardo.screening.todolist.model.TodoItem;
import ch.ricardo.screening.todolist.service.TodoListService;

/**
 * Created by tmi on 19/04/18.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TodoListApplication.class)
@WebAppConfiguration
public class TodoListRestControllerTest {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TodoListService todoListService;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setUp() throws Exception {
        todoListService.deleteAllItems();

        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        // Init TodoList with 2 items
        todoListService.create(new TodoItem("item1"));
        todoListService.create(new TodoItem("item2"));
    }

    @After
    public void tearDown() throws Exception {
        todoListService.deleteAllItems();
    }

    @Test
    public void retrieveAllItems() throws Exception {
        mockMvc.perform(get( "/todolist/items/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].content", is("item1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].content", is("item2")));
    }

    @Test
    public void retrieveAllItemsFromEmptyList() throws Exception {
        todoListService.deleteAllItems();
        mockMvc.perform(get( "/todolist/items/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void retrieveItem() throws Exception {
        mockMvc.perform(get("/todolist/items/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.content", is("item1")));
    }

    @Test
    public void retrieveNonExistingItem() throws Exception {
        mockMvc.perform(get("/todolist/items/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createItem() throws Exception {
        String itemJson = json(new TodoItem("new todo item"));

        this.mockMvc.perform(post("/todolist/items")
                .contentType(contentType)
                .content(itemJson))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/todolist/items/3")));
    }

    @Test
    public void createExistingItem() throws Exception {
        String itemJson = json(new TodoItem("item1"));

        this.mockMvc.perform(post("/todolist/items")
                .contentType(contentType)
                .content(itemJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateItem() throws Exception {
        String itemJson = json(new TodoItem("item1 updated"));

        this.mockMvc.perform(put("/todolist/items/1")
                .contentType(contentType)
                .content(itemJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/todolist/items/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.content", is("item1 updated")));
    }

    @Test
    public void updateNonExistingItem() throws Exception {
        String itemJson = json(new TodoItem("item updated"));

        this.mockMvc.perform(put("/todolist/items/3")
                .contentType(contentType)
                .content(itemJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteItem() throws Exception {
        this.mockMvc.perform(get("/todolist/items/1"))
                .andExpect(status().isOk());

        this.mockMvc.perform(delete("/todolist/items/1"))
                .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/todolist/items/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteNonExistingItem() throws Exception {
        this.mockMvc.perform(delete("/todolist/items/3")
                .contentType(contentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteAllItems() throws Exception {
        this.mockMvc.perform(get("/todolist/items/1"))
                .andExpect(status().isOk());
        this.mockMvc.perform(get("/todolist/items/2"))
                .andExpect(status().isOk());

        this.mockMvc.perform(delete("/todolist/items/"))
                .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/todolist/items/1"))
                .andExpect(status().isNotFound());
        this.mockMvc.perform(get("/todolist/items/2"))
                .andExpect(status().isNotFound());

        // Check that id counter has actually been reset
        String itemJson = json(new TodoItem("new todo item"));
        this.mockMvc.perform(post("/todolist/items")
                .contentType(contentType)
                .content(itemJson))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/todolist/items/1")));
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}