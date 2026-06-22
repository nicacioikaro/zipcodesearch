package net.minddevelopment.zipcodesearch.zipcode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minddevelopment.zipcodesearch.integration.cep.CepResolver;
import net.minddevelopment.zipcodesearch.shared.ZipcodeHelper;
import net.minddevelopment.zipcodesearch.zipcode.mapper.ZipcodeMapper;
import net.minddevelopment.zipcodesearch.zipcode.response.PageResponse;
import net.minddevelopment.zipcodesearch.zipcode.response.ZipcodeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZipcodeService {
    private final ZipcodeRepository zipcodeRepository;
    private final CepResolver cepResolver;
    private final ZipcodeMapper mapper;
    private final ZipcodeHelper zipcodeHelper;

    public PageResponse<ZipcodeResponse> getByStreet(String street, int page) {
        log.debug("street_search street={} page={}", street, page);
        Pageable pageable = PageRequest.of(page, 50);
        Page<Zipcode> pageResult = zipcodeRepository.searchByStreet(street, pageable);

        List<ZipcodeResponse> content = pageResult
                .map(mapper::toResponse)
                .getContent();

        return new PageResponse<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    public ZipcodeResponse getByCep(String zipcode) {
        zipcode = zipcodeHelper.normalizeZipcode(zipcode);
        Optional<Zipcode> zipcodeResult = zipcodeRepository.getByZipcode(zipcode);

        if (zipcodeResult.isPresent()) {
            log.debug("zipcode_resolved source=cache zipcode={}", zipcode);
            return mapper.toResponse(zipcodeResult.get());
        }

        log.debug("zipcode_resolved source=api zipcode={}", zipcode);
        var viaCepData = cepResolver.resolve(zipcode);

        Zipcode entity = mapper.toEntity(viaCepData);
        Zipcode saved = save(entity);

        log.info("cep_inserted zipcode={} location={} stateCode={} ibge={}",
                saved.getZipcode(), saved.getLocation(), saved.getStateCode(), saved.getIbge());

        return mapper.toResponse(saved);
    }

    @Transactional
    public Zipcode save(Zipcode zipcode) {
        return zipcodeRepository.save(zipcode);
    }

}
