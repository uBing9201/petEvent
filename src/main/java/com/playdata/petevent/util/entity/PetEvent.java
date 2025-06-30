package com.playdata.petevent.util.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter @ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pet_event")
public class PetEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source;
    private String eventTitle;
    private String eventUrl;
    private String location;
    private String eventDate;
    private String reservationDate;
    private String imagePath;
    private String eventTime;
    private String eventMoney;

    @Column(nullable = false, unique = true)
    private String hash;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void update(String source, String eventTitle, String eventUrl, String location, String eventDate, String reservationDate, String imagePath) {
        this.source = source;
        this.eventTitle = eventTitle;
        this.eventUrl = eventUrl;
        this.location = location;
        this.eventDate = eventDate;
        this.reservationDate = reservationDate;
        this.imagePath = imagePath;
    }
}
