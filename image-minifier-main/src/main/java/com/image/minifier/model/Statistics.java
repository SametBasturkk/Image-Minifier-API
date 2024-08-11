package com.image.minifier.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(name = "statistics")
@Entity
public class Statistics {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private int id;
    @Column(name = "total_images_processed")
    private long totalImagesProcessed;
    @Column(name = "total_bytes_saved")
    private long totalBytesSaved;
    @Column(name = "total_bytes_processed")
    private long totalBytesProcessed;

    public Statistics(long totalImagesProcessed, long totalBytesSaved, long totalBytesProcessed) {
        this.totalImagesProcessed = totalImagesProcessed;
        this.totalBytesSaved = totalBytesSaved;
        this.totalBytesProcessed = totalBytesProcessed;
    }
}
