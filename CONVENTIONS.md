# Convenções do projeto

As regras da casa. Curtas de propósito. Quando bater dúvida de "onde isso vai?"
ou "por que é assim?", a resposta está aqui. Decisão nova tomada num PR vira uma
linha aqui no mesmo dia — senão o arquivo envelhece e perde a autoridade.

---

## Estrutura

- Cada **feature** é uma pasta na raiz (`zipcode`, `integration`, `shared`...).
- Dentro de cada feature de domínio, **sempre a mesma forma**:
  ```
  feature/
  ├── FeatureController.java
  ├── FeatureService.java
  ├── FeatureRepository.java
  ├── Feature.java            (model / @Entity)
  ├── request/
  ├── response/
  └── mapper/
  ```
- O que faz a estrutura valer é a forma se repetir **igual** em toda feature.
  Pasta igual com recheio bagunçado não entrega benefício nenhum.

### O que é uma feature?

Tem **endpoint e regra próprios** → é feature, ganha pasta.
É só um dado pendurado em outra entidade → é campo, não vira pasta.

(Ex.: `zipcode` é feature porque tem regra própria — buscar, cachear, resolver
via fontes externas. Já `ddd` ou `siafi` são campos do CEP, não features.)

---

## Camadas

- **Controller é fino**: recebe, valida formato, chama o service. Sem regra.
- **Toda regra de negócio mora no service.** Um service por feature.
- **Controller nunca chama repository direto** — sempre via service.
- **O service não conhece fontes externas concretas.** Ele fala com a abstração
  de resolução (`CepResolver`); quem conhece o ViaCep (ou qualquer provedor) é a
  pasta de integração. O service cuida de cache e persistência, não de qual
  fonte respondeu.

---

## Nomenclatura

Cada camada fala a língua dela. A tradução acontece nas bordas (ex.: o campo
`stateCode` no Java vira coluna `state_code` no banco). Não existe um estilo
único pra tudo — existe o estilo certo de cada camada, e consistência dentro
dela.

### Código Java

| Item        | Convenção            | Exemplo                          |
|-------------|----------------------|----------------------------------|
| Pacote      | minúsculo, junto     | `zipcode`, `viacep`              |
| Classe      | PascalCase           | `ZipcodeController`, `ZipcodeMapper` |
| Método      | camelCase            | `getByCep`, `searchByStreet`     |
| Variável    | camelCase            | `stateCode`, `totalElements`     |
| Constante   | UPPER_SNAKE_CASE     | `MAX_RETRY_ATTEMPTS`             |

### Fora do código Java

| Camada      | Convenção            | Exemplo                          |
|-------------|----------------------|----------------------------------|
| URL / rota  | kebab-case, plural   | `/zipcodes`, `/zipcodes/search`  |
| Banco       | snake_case           | `state_code`, `created_at`       |
| JSON da API | camelCase            | `{ "stateCode": ... }`           |
| Git/artifact| kebab-case           | `zipcode-search`                 |

### Regras que pegam

- **Nunca** `dto`. É vago, não diz direção. Use `request`/`response`.
- **URL é plural e o composto vai junto**: `/zipcodes`, não `/zipcode`. O recurso
  inteiro pluraliza; o hífen separa palavras do mesmo conceito.
- **Banco é snake_case por motivo técnico, não estético**: SQL trata
  identificador sem aspas como case-insensitive, então `stateCode` viraria
  `statecode` e quebraria. snake_case sobrevive a isso.
- **JSON: escolha um e crave.** Padrão do projeto é camelCase (default do
  Jackson, casa com o Java). Misturar camelCase e snake_case na mesma API é o
  que pega mal.
- Hífen (`kebab-case`) **nunca** em nome de pacote — nem compila.
- Pacote composto → junte minúsculo. Se ficar horrível de ler, são dois
  pacotes, não um nome gigante.

---

## Mappers (MapStruct)

`request/`, `response/` e `mapper/` andam juntos dentro da feature — são as três
faces do contrato: o que entra, o que sai, e como traduz.

- `mapper/` é **pasta**, não arquivo solto — uma feature pode ter vários
  (entity, response...).
- Crie um mapper novo quando a **tradução** for de fato diferente, não só porque
  criou um request novo. Dois requests podem compartilhar um mapper com dois
  métodos.
- Use `@Mapper(componentModel = "spring")` — registra como bean e você injeta no
  service pelo construtor, sem `Mappers.getMapper(...)` na mão.
- **Mapper é burro**: traduz formato, não decide regra. Se aparecer `if` de
  negócio dentro de um `@Mapping`, a regra está vazando — ela pertence ao
  service.
- Garanta o annotation processor no build (`annotationProcessorPaths` no Maven),
  senão nada é gerado e você toma NPE em runtime. Com Lombok junto: Lombok
  **antes** do MapStruct.

---

## Erros

Fluxo: o service **lança** → o handler **captura** → devolve um **ErrorResponse**.

- `shared/exception/` → os tipos de domínio que se lança (a causa). Ex.:
  `ZipcodeNotFound`.
- `integration/<fonte>/` → exceptions específicas da integração (ex.:
  `ViaCepUnavailableException`), que estendem uma base de provedor
  (`ProviderException`) quando devem disparar resiliência.
- `shared/handlers/` → captura global, `@RestControllerAdvice` (a tradução).
- `shared/response/` → o formato da resposta de erro (a forma).

Por que o erro é global e não fica na feature: o contrato de erro é o **mesmo
para a API inteira**. Um 404 de `zipcode` e um 400 de validação saem com o mesmo
shape, senão o cliente parseia erro de N jeitos.

- O nível do log segue a **natureza do evento**, não a camada: `ERROR` para o
  inesperado e "todas as fontes falharam"; `WARN` para erro de cliente (400/405)
  e provedor que caiu mas teve fallback; `DEBUG` para "não encontrado" (fluxo
  esperado).
- `ErrorResponse` é contrato público → mudar o formato é breaking change.

---

## Resiliência

- Retry e circuit breaker ficam **no provedor** (`@Retry`/`@CircuitBreaker` no
  `fetch`), não no service. O service não sabe que existe retry.
- O que **dispara** resiliência é o tipo da exception: falha técnica
  (`ProviderException`) re-tenta e conta pro circuito; "não encontrado"
  (`ZipcodeNotFound`) **não** re-tenta e **não** abre o circuito — senão CEPs
  inexistentes derrubariam a fonte à toa.
- A composição é: **provedor** cuida de retry + circuit breaker por fonte;
  **resolver** cuida do fallback entre fontes.

---

## Integração externa (fontes de CEP)

- Cada provedor em `integration/<nome>/` (`viacep`, ...).
- Todo provedor implementa `CepProvider` e devolve o DTO neutro `CepData`.
- **Nunca** espalhar chamada de provedor dentro do service. O service fala com o
  `CepResolver`; quem conhece o ViaCep é só a pasta do ViaCep.
- Adicionar uma fonte nova = implementar `CepProvider`. Service e resolver não
  mudam (estender sem modificar).

---

## Commits (Conventional Commits)

Formato: `tipo(escopo): descrição` — em **inglês**, no imperativo, minúsculo,
sem ponto final.

```
feat(zipcode): add multi-source cep resolver
fix(viacep): correct exception mapping for 404
docs(api): document zipcode endpoints
refactor(zipcode): plug resolver into service
test(zipcode): add repository similarity tests
chore(deps): add resilience4j dependency
```

### Tipos

| Tipo       | Quando usar                                              |
|------------|----------------------------------------------------------|
| `feat`     | nova funcionalidade                                      |
| `fix`      | correção de bug                                          |
| `docs`     | só documentação                                          |
| `refactor` | muda código sem mudar comportamento                     |
| `test`     | adiciona ou ajusta testes                               |
| `chore`    | build, deps, config — nada de código de negócio         |
| `perf`     | melhoria de performance                                  |

### Escopo

O escopo é o **nome da feature** (a pasta): `feat(zipcode)`, `fix(viacep)`.
Casa direto com a estrutura — quem lê o histórico sabe na hora qual parte do
sistema mudou. Para algo transversal, use a área: `chore(deps)`, `docs(api)`.

### Breaking change

Mudança que quebra contrato leva `!` após o escopo: `feat(zipcode)!: ...`.

---

## Evolução (práticas a adotar quando o projeto exigir)

Estas não estão implementadas hoje — o projeto não precisa delas no estágio
atual. Ficam documentadas como o caminho deliberado quando a necessidade
aparecer, não como dívida esquecida.

- **Versionamento de API (`/v1/...`)** — quando a API for pública e precisar
  evoluir sem quebrar clientes. A versão vive **só na URL** e **só na API
  pública**; por versão vivem apenas controller, `request/` e `response/` —
  service, model e repository nunca versionam. `v2` só para breaking change
  (remover/renomear campo, mudar tipo); campo novo opcional não bumpa versão.

- **Problem Details (RFC 9457)** — formato de erro padronizado
  (`{ type, title, status, detail, ... }`). O `ErrorResponse` atual é mais
  simples; migrar para o RFC seria o passo natural para uma API pública de
  contrato robusto.

- **ArchUnit** — testes que falham o build se a arquitetura for violada
  (ex.: "controller não chama repository", "nada fora de `integration` importa
  o SDK de um provedor"). Assim a convenção não depende de alguém lembrar de
  revisar no PR. Faz sentido quando o time crescer.
