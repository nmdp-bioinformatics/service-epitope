Fitnesse setup:

- Download fitnesse-standalone: 
    - http://www.fitnesse.org/FitNesseDownload

- Download fitnesse-maven-classpath: 
    - https://github.com/amolenaar/fitnesse-maven-classpath
      
- Add fitnesse-maven-classpath to fitnesse as directed

- Add the following to /root:

    !define TEST_SYSTEM {slim} 
    !pomFile (fitnesse module pom location)
