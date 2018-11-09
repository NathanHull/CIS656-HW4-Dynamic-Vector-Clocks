all:
	javac src/*/* -cp "lib/*" -d out/

server: out/server.jar
	java -jar out/server.jar

client: src/client/client.java
	javac src/client/Client.java -cp "lib/*" -d out/
	java out/client/Client
