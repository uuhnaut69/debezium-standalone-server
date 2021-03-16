cd debezium-standalone \
&& ./mvnw clean package -DskipTests=true \
&& docker build -t uuhnaut69/debezium-standalone:latest .