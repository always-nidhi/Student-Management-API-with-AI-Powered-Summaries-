# Student Management API with AI-Powered Summaries (Java, Spring Boot & Ollama)

This project is a simple REST API for performing CRUD (Create, Read, Update, Delete) operations on a list of students. It is built with Java and the Spring Boot framework and includes an integration with Ollama to generate AI-powered summaries of student profiles.

---

## Prerequisites

Before running this project, you will need the following installed:
* Java Development Kit (JDK) 17 or newer
* Apache Maven
* Ollama

---

## How to Run the Project

1.  **Start the Ollama AI Service:**
    This project is configured to use the `gemma:2b` model. Run the following command in a terminal and leave it running in the background.
    ```bash
    ollama run gemma:2b
    ```

2.  **Run the Java Application:**
    Navigate to the project's root directory and run the application using Maven.
    ```bash
    mvn spring-boot:run
    ```
    The API will be available at `http://localhost:8080`.

---

## API Endpoints

You can test the endpoints using `curl` or a tool like Postman.

#### Create a new student
* **POST** `/students`
    ```bash
    curl -X POST http://localhost:8080/students \
    -H "Content-Type: application/json" \
    -d '{"name": "Jane Doe", "age": 21, "email": "jane.doe@example.com"}'
    ```

#### Get all students
* **GET** `/students`
    ```bash
    curl http://localhost:8080/students
    ```

#### Get a student by ID
* **GET** `/students/{id}`
    ```bash
    curl http://localhost:8080/students/1
    ```

#### Generate an AI summary for a student
* **GET** `/students/{id}/summary`
    ```bash
    curl http://localhost:8080/students/1/summary
    ```

#### Update a student by ID
* **PUT** `/students/{id}`
    ```bash
    curl -X PUT http://localhost:8080/students/1 \
    -H "Content-Type: application/json" \
    -d '{"name": "Jane Smith", "age": 22, "email": "jane.smith@example.com"}'
    ```

#### Delete a student by ID
* **DELETE** `/students/{id}`
    ```bash
    curl -X DELETE http://localhost:8080/students/1
    ```
