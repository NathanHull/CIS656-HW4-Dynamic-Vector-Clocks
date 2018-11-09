all:
	javac src/*/* -cp "lib/*" -d out/

server:
	java -jar out/server.jar

client:
	javac src/client/Client.java -cp "lib/*" -d out/
	java out/client/Client

test:
	javac -cp out/:lib/junit-4.12.jar src/test/VectorClockTests.java -d out/
	java -cp "out/:lib/*" org.junit.runner.JUnitCore test.VectorClockTests
