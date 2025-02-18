1 Control Plane + 3 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 2 Fabflix pods = 13,167.65/minute
1 Control Plane + 4 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 3 Fabflix pods = 13,377.193/minute
Demo: https://youtu.be/Om9qPv4aE9A

Overview:
Fabflix is a full-stack e-commerce web application designed for browsing and purchasing movies, similar to IMDb. It focuses on scalability, performance optimization, and cloud deployment using Kubernetes.

Tech Stack:
Backend: Java, Apache Tomcat, MySQL
Frontend: HTML, CSS, JavaScript, Bootstrap, jQuery
Database: MySQL (Master-Slave Replication)
Deployment: Kubernetes (1 control plane, 3-4 worker nodes)
