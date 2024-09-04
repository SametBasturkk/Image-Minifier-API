package com.image.minifier.main.service;

import com.image.minifier.main.model.Statistics;
import com.image.minifier.main.repository.StatisticsRepository;
import com.image.minifier.main.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
@Service
public class StatisticsService {

    private StatisticsRepository statisticsRepository;
    private UserRepository userRepository;

    private final Lock lock = new ReentrantLock();

    public StatisticsService(StatisticsRepository statisticsRepository, UserRepository userRepository) {
        this.statisticsRepository = statisticsRepository;
        this.userRepository = userRepository;
    }


    public void updateCounterStatistic(Long compressedSize, Long originalSize) {
        lock.lock();
        try {
            Optional<Statistics> statistics = statisticsRepository.findById(1);

            log.info("Updating statistics");

            if (statistics.isPresent()) {
                Statistics stats = statistics.get();
                stats.setTotalImagesProcessed(stats.getTotalImagesProcessed() + 1);
                stats.setTotalBytesProcessed(stats.getTotalBytesProcessed() + originalSize);
                stats.setTotalBytesSaved(stats.getTotalBytesSaved() + (originalSize - compressedSize));
                statisticsRepository.save(stats);
                log.info("Statistics updated");
            } else {
                Statistics stats = new Statistics(1, originalSize - compressedSize, originalSize);
                statisticsRepository.save(stats);
                log.info("Statistics created");
            }
        } finally {
            lock.unlock();
        }
    }
}
