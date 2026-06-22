package net.minddevelopment.zipcodesearch.integration.cep;

public interface CepProvider {
    CepData fetch(String cep);   // lança exceção em falha ou não-encontrado
    String name();
}