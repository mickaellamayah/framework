mkdir temp
javac -d temp *.java
cd temp
jar -cvf framework.jar *
copy framework.jar ..\..\TEST\lib
cd ..\
rmdir /s /q temp