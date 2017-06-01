Running Fitnesse Server
=======================

* To run, simply run the maven task `mvn exec:java`.

Setup Instructions
------------------

- Download fitnesse-standalone: 
    - http://www.fitnesse.org/FitNesseDownload

- Download fitnesse-maven-classpath: 
    - https://github.com/amolenaar/fitnesse-maven-classpath
      
- Add fitnesse-maven-classpath to fitnesse as directed

- Add the following to /root:
    
    ```
    !define TEST_SYSTEM {slim} 
    !pomFile (fitnesse module pom location)
    ```