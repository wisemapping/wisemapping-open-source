# Database Configuration

## Overview

WiseMapping supports a wide variety of databases. However, we run intensively tests over HSQL and MySQL database.

* HyperSQL: Automatically configured when you compile WiseMapping and It's used primarily for testing. Additionally, the binary distribution already has an instance configured to provide a single click installation.
* MySQL: This version is the most tested database we support. MySQL is the database use in http://www.wisemapping.com and it's the suggested version for production environments.
* PostgreSQL: Scripts are distributed for the creation and configuration of the it. You will find them within "config/postgres" directory in the binary distribution. Additionally, JDBC driver need to be added to the container.
* Others: In spite of the fact that we don't provide yet initialization scripts for others databases, WiseMapping can be deployed in any relational database. Please, contact us if you have any particular question on this area.  

* In the following section, you are going to find a detailed explanation how to configure you WiseMapping using MySQL 5.5.

## MySQL Installation
### Prerequisites
* Download and install MySQL.  You can download it for free from: http://dev.mysql.com/downloads/
Running SQL Scripts
Inside the WiseMapping binary distribution, you will find a directory "config/mysql". It contains all the SQL script required to configure a new WiseMapping database instance.

You will find 4 scripts:
* create-database.sql: Create all wisemapping database and wisemapping user.
* create-schemas.sql: Create all database tables and index.
* apopulate-schemas.sql: Creates a mind map example and an a test user "test@wisemapping.org" with password "test".
* drop-schemas.sql: Drop all wisemapping tables in case you want to have a fresh installation.

There are a lot of good tools you can use to run this scripts (eg: MySQLWorkbench). However, the simples way is to use the command line tool that is distributed as part of the MySQL installation.
If you are one brave hearts that is not afraid of the command line tools, open a terminar and execute the following lines:
~~~~
cd <WISEMAPPING-DIR>/config/database/mysql
# Default MySQL installation creates a "root" user with empty password. You can connect to the database with this user if you are # logged in same machine where the database is installed and must be executed logged as "root" 
#
# If you have changed the default database "root" password , you need to specify an additional -p parameter and provide the
# new password.
mysql -uroot < create-database.sql

# Create tables and default tests user
mysql -uwisemapping -Dwisemapping -ppassword < create-schemas.sql 
mysql -uwisemapping -Dwisemapping -ppassword < apopulate-schemas.sql
~~~~




Great, you have configured you database !. Let's configure WiseMapping now.

