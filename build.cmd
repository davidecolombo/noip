@echo off
cls
call mvn dependency:tree > dependency_tree.txt
call mvn clean install
