package com.image.minifier.service;

import com.image.minifier.model.Statistics;
import com.image.minifier.repository.StatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Service
public class StatisticsService {

    @Autowired
    private StatisticsRepository statisticsRepository;

    private final Lock lock = new ReentrantLock();



    public void updateCounterStatistic(Long compressedSize, Long originalSize) {
        lock.lock();
        try {
            Optional<Statistics> statistics = statisticsRepository.findById(1);

            if (statistics.isPresent()) {
                Statistics stats = statistics.get();
                stats.setTotalImagesProcessed(stats.getTotalImagesProcessed() + 1);
                stats.setTotalBytesProcessed(stats.getTotalBytesProcessed() + originalSize);
                stats.setTotalBytesSaved(stats.getTotalBytesSaved() + (originalSize - compressedSize));
                statisticsRepository.save(stats);
            } else {
                Statistics stats = new Statistics(1, originalSize - compressedSize, originalSize);
                statisticsRepository.save(stats);
            }
        } finally {
            lock.unlock();
        }
    }
}
