create user testdb1 identified by testdb default tablespace app_data temporary tablespace temp quota unlimited on app_data;

grant FORCE ANY TRANSACTION, create session, create table, create view, create sequence, create procedure, create type, create trigger, create synonym to testdb1;

#-- Another db

create user testdb2 identified by testdb default tablespace app_data temporary tablespace temp quota unlimited on app_data;

grant FORCE ANY TRANSACTION, create session, create table, create view, create sequence, create procedure, create type, create trigger, create synonym to testdb2;

#-- Table 

   CREATE TABLE "TESTDB1".TEST_TX2 
   (
    ID VARCHAR2(30) NOT NULL ENABLE, 
   NAME VARCHAR2(30) NOT NULL ENABLE, 
	AGE NUMBER NOT NULL ENABLE
   ) TABLESPACE "APP_DATA" ;
  
   CREATE TABLE "TESTDB2".TEST_TX2 
   (
    ID VARCHAR2(30) NOT NULL ENABLE, 
   NAME VARCHAR2(30) NOT NULL ENABLE, 
	AGE NUMBER NOT NULL ENABLE
   ) TABLESPACE "APP_DATA" ;