package net.minddevelopment.zipcodesearch.integration.viacep;

import net.minddevelopment.zipcodesearch.integration.cep.CepData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ViaCepMapper {
    @Mapping(source = "cep", target = "zipcode")
    @Mapping(source = "logradouro", target = "street")
    @Mapping(source = "complemento", target = "complement")
    @Mapping(source = "unidade", target = "unit")
    @Mapping(source = "bairro", target = "neighborhood")
    @Mapping(source = "localidade", target = "location")
    @Mapping(source = "uf", target = "stateCode")
    @Mapping(source = "estado", target = "state")
    @Mapping(source = "regiao", target = "region")
    CepData toCepData(ViaCepResponse response);
}
