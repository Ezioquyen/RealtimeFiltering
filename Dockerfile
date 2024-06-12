FROM confluentinc/cp-kafka
ADD kafka-jmx-metrics.yaml /opt/prometheus/
ADD jmx_prometheus_javaagent-0.3.1.jar /opt/prometheus/