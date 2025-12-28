#!/bin/sh

rm -r classes examples/webapp/WEB-INF/lib examples/webapp/WEB-INF/classes
mkdir classes
javac -source 1.8 -target 1.8 -classpath cti-driver-@version@.jar:lib/javax.servlet-api-4.0.1.jar:lib/jakarta.servlet-api-5.0.0.jar -encoding UTF-8 -d classes\
	examples/src/jp/cssj/cti2/examples/*.java
mkdir examples/webapp/WEB-INF/lib
cp cti-driver-@version@.jar examples/webapp/WEB-INF/lib
cp -r classes examples/webapp/WEB-INF