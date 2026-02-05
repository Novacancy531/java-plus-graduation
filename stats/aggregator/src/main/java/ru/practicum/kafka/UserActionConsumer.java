package ru.practicum.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.AggregatorKafkaProperties;
import ru.practicum.service.SimilarityAggregator;
import ru.practicum.util.AvroDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class UserActionConsumer implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(UserActionConsumer.class);

    private final KafkaConsumer<String, byte[]> consumer;
    private final AggregatorKafkaProperties props;
    private final SimilarityAggregator aggregator;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread worker;

    public UserActionConsumer(KafkaConsumer<String, byte[]> consumer,
                              AggregatorKafkaProperties props,
                              SimilarityAggregator aggregator) {
        this.consumer = consumer;
        this.props = props;
        this.aggregator = aggregator;
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        consumer.subscribe(List.of(props.getUserActionsTopic()));

        worker = new Thread(this::runLoop, "user-actions-consumer");
        worker.start();
    }

    private void runLoop() {
        try {
            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, byte[]> r : records) {
                    log.info("Aggregator: received user-action key={} topic={} offset={}",
                            r.key(), r.topic(), r.offset());
                    UserActionAvro action = AvroDeserializer.deserialize(r.value(), new UserActionAvro());
                    aggregator.onAction(action);
                }

                consumer.commitSync();
            }
        } catch (WakeupException e) {
            if (running.get()) {
                throw e;
            }
        } finally {
            try {
                consumer.commitSync();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        consumer.wakeup();
        try {
            if (worker != null) {
                worker.join(3000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
