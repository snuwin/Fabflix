# FABFLIX E-COMMERCE MOVIE WEBSITE

**Team Members & Contributions**

Serena Nguyen & Phillip Nguyen: Joint development of application features, frontend/UI design, backend integration (Java Servlets, REST APIs), and deployment infrastructure (Kubernetes, YAML, Load Balancing, Security)


 **Demo Video** : [Youtube Demo] https://youtu.be/Om9qPv4aE9A
 
**Project Overview**
This project is a full-stack e-commerce web application designed for movie data exploration and transaction simulation.
Developed in CS122B (Fall 2025) at University of California, Irvine.

# Build Instructions
- Ensure Maven is installed
- Run `mvn clean install` to build the WAR file
- Deploy to Tomcat or run locally with your preferred servlet container
- 
# System Architecture 
- BackEnd: Java Servlets, RESTful APIs, MySQL (Master-Slave Replication), Prepared Statements
- FrontEnd: HTML/CSS, JavaScript, AJAX
- Security: SQL Injection Prevention, Google reCAPTCHA Integration, Session-Based Login
- Infrastructure: Kubernetes Cluster with 1 Control Plane, Worker Nodes, Pod Auto-Scaling
- Build Tool: Maven - Manages Dependencies (MySQL Connector, Gson, Jasypt, Servlet API) and builds the WAR package for deployment



# Performance Metrics:
| Cluster Configuration                                  | Throughput (transactions/second) |
|--------------------------------------------------------|----------------------------------|
| 1 CP + 3 Workers + 2 Fabflix Pods                      | ~219.46 transactions/sec         |
| 1 CP + 4 Workers + 3 Fabflix Pods                      | ~222.95 transactions/sec         |
_Metrics originally reported per minute; converted to per second for industry-standard readability._
- Simulated high-concurrency user load using JMeter for stress testing and throughput benchmarking
- Increased worker nodes and Fabflix pods marginally improved throughput
- The project emphasized clean separation of backend (Java Servlets, RESTful APIs) and frontend (AJAX, HTML/CSS), promoting modular development and scalability.
