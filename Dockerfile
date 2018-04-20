FROM openjdk:8-jre-alpine
COPY ./target/todolist-0.0.1-SNAPSHOT.jar /usr/src/todolist/
WORKDIR /usr/src/todolist
EXPOSE 8080
CMD ["java", "-jar", "todolist-0.0.1-SNAPSHOT.jar"]

