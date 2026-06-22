package net.minddevelopment.zipcodesearch.zipcode;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(name="zipcodes")
@EntityListeners(AuditingEntityListener.class)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Zipcode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String zipcode;

    @Column(length = 150)
    private String street;

    @Column(length = 100)
    private String complement;

    @Column(length = 50)
    private String unit;

    @Column(length = 100)
    private String neighborhood;

    @Column(nullable = false, length = 50)
    private String location;

    @Column(nullable = false, length = 2)
    private String stateCode;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(nullable = false, length = 50)
    private String region;

    @Column(nullable = false, length = 7)
    private String ibge;

    @Column(length = 4)
    private String gia;

    @Column(nullable = false, length = 2)
    private String ddd;

    @Column(length = 4)
    private String siafi;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
