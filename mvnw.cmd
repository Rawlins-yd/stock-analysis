@ECHO OFF
SETLOCAL
SET BASE_DIR=%~dp0
SET WRAPPER_DIR=%BASE_DIR%\.mvn
SET MVN_VERSION=3.9.9
SET MVN_DIR=%WRAPPER_DIR%\apache-maven-%MVN_VERSION%
SET MVN_BIN=%MVN_DIR%\bin\mvn.cmd

IF NOT EXIST "%MVN_BIN%" (
  ECHO Please run Maven Wrapper from Unix shell once to bootstrap Maven.
  EXIT /B 1
)

"%MVN_BIN%" %*

