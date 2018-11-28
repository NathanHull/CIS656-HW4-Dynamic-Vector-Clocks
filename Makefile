all:
	javac src/*/* -cp "lib/*" -d out/

server:
	java -jar out/server.jar

client1:
	javac src/client/Client.java -cp "out/:lib/*" -d out/
	java -cp "out/:lib/*" client/Client nate 9898 localhost 8000

client2:
	javac src/client/Client.java -cp "out/:lib/*" -d out/
	java -cp "out/:lib/*" client/Client john 9897 localhost 8000

client3:
	javac src/client/Client.java -cp "out/:lib/*" -d out/
	java -cp "out/:lib/*" client/Client alex 9896 localhost 8000

test:
	javac -cp out/:lib/junit-4.12.jar src/test/VectorClockTests.java -d out/
	java -cp "out/:lib/*" org.junit.runner.JUnitCore test.VectorClockTests
