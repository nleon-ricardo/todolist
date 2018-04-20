package ch.ricardo.screening.todolist;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import ch.ricardo.screening.todolist.model.TodoItem;
import ch.ricardo.screening.todolist.service.TodoListService;

@SpringBootApplication
public class TodoListApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoListApplication.class, args);
	}

	@Bean
	CommandLineRunner init(TodoListService todoListService) {
		return (evt) -> Arrays.asList("do this,do that".split(","))
				.forEach(
						content -> {
							todoListService.create(new TodoItem(content));
						});
	}
}
