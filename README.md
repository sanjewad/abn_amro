# abn_amro
ABN Amro Test 

To Run the Application:
mvn clean spring-boot:run

Invoke URL:
Post request can be invoked by calling url: We need to provide inputdata file as multipart file 
localhost:8080/abn/v1/single-file-upload 

which will produce output.csv as attached above

Implementation Design:
Spring Batch - Async way of executing tasks with Reader, Processor and Writer
Spring Boot and Unit and Integration tests
