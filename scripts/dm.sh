#!/bin/bash

# Check for Java 17+
JAVA_VER=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 17 ]; then
    echo "You need Java version 17 or newer."
    exit 1
fi

# Collect inputs from the user
read -p "Enter project name (e.g., todo, my-todo-app): " PROJECT_NAME
read -p "Enter package name (e.g., org.ionatomics, org.acme, com.ed.ian): " PACKAGE_NAME
read -p "Enter name of the example (rest, entity, todo, etc.): " TEMPLATE_NAME
read -p "Enter Database Type (mysql, mariadb, etc.) or press Enter to skip: " DB_TYPE

# Execute the Java CLI
java -jar dm.jar -n $PROJECT_NAME -p $PACKAGE_NAME -t $TEMPLATE_NAME -d $DB_TYPE

# End of the script
