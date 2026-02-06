package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.AggregatorKafkaProperties;
import ru.practicum.service.SimilarityAggregator;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionConsumer implements SmartLifecycle {

    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final AggregatorKafkaProperties props;
    private final SimilarityAggregator aggregator;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread worker;

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
                ConsumerRecords<String, UserActionAvro> records =
                        consumer.poll(Duration.ofMillis(props.getPollTimeoutMs()));
                if (records.isEmpty()) {
                    continue;
                }

                for (var r : records) {
                    log.info("Aggregator: received user-action key={} topic={} offset={}",
                            r.key(), r.topic(), r.offset());
                    aggregator.onAction(r.value());
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
