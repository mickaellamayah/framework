mkdir temp
javac -d temp *.java
cd temp
jar -cvf framework.jar *
copy framework.jar ..\..\test\lib
cd ..\
rmdir /s /q temp