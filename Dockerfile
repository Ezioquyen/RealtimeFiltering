FROM confluentinc/cp-kafka:7.2.1
ADD kafka-jmx-metrics.yaml /opt/prometheus/
ADD jmx_prometheus_javaagent-0.3.1.jar /opt/prometheus/