
./bin/elasticsearch
https://127.0.0.1:9200/

http://localhost:5601/

elastic
X3C74dZwgD_56xxa-N1R

spring.elasticsearch.uris=https://127.0.0.1:9200
spring.elasticsearch.username=elastic
spring.elasticsearch.password=X3C74dZwgD_56xxa-N1R
spring.elasticsearch.restclient.ssl.certificate-path=classpath:certs/http_ca.crt


curl -X GET "https://127.0.0.1:9200/_cluster/health?pretty" -u elastic:X3C74dZwgD_56xxa-N1R --cacert /Users/atul/Documents/run/main/_accounts/Java-Techie-jt/spring-boot-elasticsearch-example/src/main/resources/certs/http_ca.crt
keytool -import -trustcacerts -alias elasticsearch-ca -file /Users/atul/Documents/run/main/_accounts/Java-Techie-jt/spring-boot-elasticsearch-example/src/main/resources/certs/http_ca.crt -keystore $JAVA_HOME/lib/security/cacerts
to check the certificate in the keystore:
keytool -list -keystore $JAVA_HOME/lib/security/cacerts | grep elasticsearch-ca