package com.ImageProcessor.ImageProcessor.service;

import com.ImageProcessor.ImageProcessor.model.Statistics;
import com.ImageProcessor.ImageProcessor.repository.StatisticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class StatisticsService {

    private StatisticsRepository statisticsRepository;

    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public void updateStatistic(Long compressedSize, Long originalSize) {
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
    }


}
