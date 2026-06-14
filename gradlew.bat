@echo off
"%JAVA_HOME%\bin\java.exe" -Xmx64m -Xms64m -Dorg.gradle.appname=gradlew -classpath "%~dp0gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*