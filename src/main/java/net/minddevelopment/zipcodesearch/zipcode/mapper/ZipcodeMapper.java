package net.minddevelopment.zipcodesearch.zipcode.mapper;

import net.minddevelopment.zipcodesearch.integration.cep.CepData;
import net.minddevelopment.zipcodesearch.integration.viacep.ViaCepResponse;
import net.minddevelopment.zipcodesearch.zipcode.Zipcode;
import net.minddevelopment.zipcodesearch.zipcode.request.ZipcodeRequest;
import net.minddevelopment.zipcodesearch.zipcode.response.ZipcodeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ZipcodeMapper {

    Zipcode toEntity(CepData cepData);

    ZipcodeResponse toResponse(Zipcode zipcode);
}
