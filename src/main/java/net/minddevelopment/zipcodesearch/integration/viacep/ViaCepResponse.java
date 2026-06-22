package net.minddevelopment.zipcodesearch.integration.viacep;

public record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String unidade,
        String bairro,
        String localidade,
        String uf,
        String estado,
        String regiao,
        String ibge,
        String gia,
        String ddd,
        String siafi,
        String erro
) {
    public boolean isError() {
        return "true".equals(erro);
    }
}
