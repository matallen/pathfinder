export PATHFINDER_SERVER=http://localhost:8080
export PATHFINDER_SELF=.
mvn clean package -DskipTests jetty:run -Djetty.port=8083
