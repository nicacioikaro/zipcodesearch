package net.minddevelopment.zipcodesearch.zipcode;

import net.minddevelopment.zipcodesearch.shared.config.JpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import(JpaAuditingConfig.class)
class ZipcodeRepositoryTest {

    @Autowired
    private ZipcodeRepository repository;

    @Test
    void shouldFindZipcodeWhenItExists() {
        repository.save(newZipcode("12345678", "Rua das Flores", "São Paulo"));

        Optional<Zipcode> result = repository.getByZipcode("12345678");

        assertThat(result).isPresent();
        assertThat(result.get().getLocation()).isEqualTo("São Paulo");
    }

    @Test
    void shouldReturnEmptyWhenZipcodeDoesNotExist() {
        Optional<Zipcode> result = repository.getByZipcode("00000000");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindStreetBySimilarity() {
        repository.save(newZipcode("11111111", "Avenida Paulista", "São Paulo"));
        repository.save(newZipcode("22222222", "Rua Augusta", "São Paulo"));

        Page<Zipcode> result = repository.searchByStreet("Paulista", PageRequest.of(0, 50));

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getStreet()).contains("Paulista");
    }

    @Test
    void shouldReturnEmptyWhenNoStreetMatches() {
        repository.save(newZipcode("33333333", "Rua das Acácias", "Curitiba"));

        Page<Zipcode> result = repository.searchByStreet("xyzabcnonexistent", PageRequest.of(0, 50));

        assertThat(result.getContent()).isEmpty();
    }

    private Zipcode newZipcode(String zipcode, String street, String location) {
        Zipcode z = new Zipcode();
        z.setZipcode(zipcode);
        z.setStreet(street);
        z.setLocation(location);
        z.setNeighborhood("Centro");
        z.setStateCode("SP");
        z.setState("São Paulo");
        z.setRegion("Sudeste");
        z.setIbge("3550308");
        z.setDdd("11");
        z.setGia("");
        z.setSiafi("0000");
        z.setComplement("");
        return z;
    }
}