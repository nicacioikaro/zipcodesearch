package net.minddevelopment.zipcodesearch.zipcode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZipcodeRepository extends JpaRepository<Zipcode, Long> {
    public Optional<Zipcode> getByZipcode(String zipcode);

    // Street search combines two strategies:
    //  - ILIKE for substring matches
    //  - pg_trgm similarity() for fuzzy matching (typos, partial names)
    // The 0.1 threshold is intentionally low to stay permissive; results are
    // ordered by best similarity first.
    @Query(value = """
        SELECT *
         FROM zipcodes
         WHERE street ILIKE '%' || :street || '%'
            OR similarity(street, :street) > 0.1
         ORDER BY similarity(street, :street) DESC
    """, nativeQuery = true)
    Page<Zipcode> searchByStreet(@Param("street") String street, Pageable pageable);
}
