# EntrePares 1.0 - TP3

## Integrantes

- Gabriel Mendonça
- Heitor Moreira
- Matheus Procopio
- Sergio Manso

## Descrição do sistema

O projeto implementa o sistema **EntrePares 1.0**, responsável pelo cadastro e autenticação de usuários da PUC Minas, pelo gerenciamento de cursos livres e pelas inscrições de usuários em cursos ofertados por outras pessoas.

Neste trabalho foram implementados:

- cadastro de usuários com autenticação por e-mail e senha
- recuperação de senha com pergunta e resposta secretas
- CRUD de usuários
- CRUD de cursos
- vínculo 1:N entre usuário e cursos
- vínculo N:N entre usuários e cursos por meio da entidade `CourseUser` (`CursoUsuario`)
- listagem dos cursos do usuário ativo em ordem alfabética
- busca de cursos de outras pessoas por código NanoID
- busca de cursos por palavras-chave do nome, ordenada por TF-IDF
- listagem completa de cursos de outras pessoas com paginação de 10 itens
- inscrição e cancelamento de inscrição pelo aluno
- gestão de inscritos pelo proponente do curso
- exportação CSV da lista de inscritos
- controle de estado do curso
- interface textual em CLI seguindo MVC

## Arquitetura

O projeto preserva a infraestrutura base fornecida em aula no pacote [`src/aed3`](src/aed3) e constrói a aplicação do domínio EntrePares no pacote [`src/entrepairs`](src/entrepairs), separando domínio, persistência, regras de negócio e interface.

### Organização por pacotes

- `src/aed3`
  - infraestrutura de persistência reaproveitada da disciplina
  - classes principais: `Arquivo`, `ArvoreBMais`, `HashExtensivel` e registros auxiliares
- `src/entrepairs/app`
  - bootstrap da aplicação
  - monta as dependências e inicia o fluxo principal em `Application`
- `src/entrepairs/controller`
  - coordena os fluxos da interface e delega regras aos serviços
  - classes: `ApplicationController`, `AuthController`, `UserController`, `CourseController`, `EnrollmentController`
- `src/entrepairs/view`
  - camada de CLI
  - renderiza menus, mensagens e coleta entradas do usuário
- `src/entrepairs/model`
  - entidades de domínio
  - classes: `User`, `Course`, `CourseUser`, `CourseSearchResult` e enum `CourseStatus`
- `src/entrepairs/service`
  - regras de negócio de autenticação, perfil e cursos
  - abstrações de suporte como `PasswordHasher` e `ShareCodeGenerator`
- `src/entrepairs/repository/adapter`
  - adaptadores concretos sobre a infraestrutura `aed3`
  - classes: `Aed3UserRepository`, `Aed3CourseRepository`, `Aed3CourseUserRepository`, `UserRecord`, `CourseRecord`, `CourseUserRecord`
- `src/entrepairs/repository/index`
  - chaves persistidas nas árvores B+
  - classes: `UserEmailKey`, `CourseShareCodeKey`, `UserCourseKey`, `UserCourseNameKey`, `CourseEnrollmentKey`, `UserEnrollmentKey`
- `src/entrepairs/util`
  - utilitários de normalização, datas e padronização de chaves
  - `CourseNameTerms` extrai termos válidos, remove stop words e calcula TF

### Fluxo entre camadas

1. `Application` cria repositórios, serviços, views e controllers.
2. Os controllers orquestram os casos de uso da CLI.
3. Os services aplicam as regras de negócio e validações.
4. Os repositories adaptam as entidades do domínio para os registros persistidos.
5. A persistência é realizada com `aed3.Arquivo`, `HashExtensivel` e `ArvoreBMais`.

### Decisões de projeto

- O conteúdo original em português do pacote `aed3` foi preservado.
- O código da aplicação foi estruturado em inglês, com separação explícita entre domínio e persistência.
- A classe acadêmica `aed3.ListaInvertida` mantém o dicionário de termos e os blocos encadeados persistidos.
- As entidades de domínio (`User` e `Course`) não implementam diretamente `Registro`; a serialização fica isolada em `UserRecord` e `CourseRecord`.
- Os índices secundários e o relacionamento 1:N foram implementados com árvores B+ persistidas em disco.
- O relacionamento N:N entre cursos e usuários foi implementado com um CRUD próprio de associação e duas árvores B+: uma por curso e outra por usuário.
- O hash da senha e da resposta secreta foi abstraído por `PasswordHasher`, com implementação atual em `Sha256PasswordHasher`.
- A geração do código compartilhável do curso foi abstraída por `ShareCodeGenerator`, com implementação atual em `NanoIdShareCodeGenerator`.
- O índice de nomes armazena somente `(courseId, TF)`; o IDF é calculado durante a consulta com a quantidade atual de cursos e o tamanho da lista do termo.
- Na primeira execução desta versão, cursos já persistidos são adicionados automaticamente ao novo índice quando os arquivos do índice ainda não existem.

## Entidades e estruturas persistidas

### Entidades de domínio

#### Usuário (`User`)

- `id`
- `name`
- `email`
- `passwordHash`
- `secretQuestion`
- `secretAnswerHash`

#### Curso (`Course`)

- `id`
- `ownerUserId`
- `name`
- `startDate`
- `description`
- `shareCode`
- `status`

#### Inscrição (`CourseUser`)

- `id`
- `courseId`
- `userId`
- `enrollmentDate`

#### Estado do curso (`CourseStatus`)

- `0` `OPEN_FOR_ENROLLMENT`: curso aberto para inscrições
- `1` `ENROLLMENT_CLOSED`: curso ativo sem novas inscrições
- `2` `COMPLETED`: curso concluído
- `3` `CANCELED`: curso cancelado

### Registros de persistência

Os registros persistidos em arquivo binário foram desacoplados das entidades de domínio:

- `UserRecord`
  - serializa os dados de `User` para `aed3.Arquivo`
- `CourseRecord`
  - serializa os dados de `Course`
  - armazena a data como `epochDay`
  - armazena o estado como `statusCode`
- `CourseUserRecord`
  - serializa a associação entre curso e usuário
  - armazena a data de inscrição como `epochDay`

### Registros de índice

As árvores B+ armazenam chaves dedicadas para cada necessidade de busca:

- `UserEmailKey`
  - índice secundário de usuário por e-mail normalizado
- `CourseShareCodeKey`
  - índice secundário de curso por código compartilhável
- `UserCourseKey`
  - relacionamento `ownerUserId -> courseId`
- `UserCourseNameKey`
  - ordenação dos cursos do usuário por nome normalizado
- `CourseEnrollmentKey`
  - relacionamento `courseId -> courseUserId`
- `UserEnrollmentKey`
  - relacionamento `userId -> courseUserId`

## Persistência e índices

### Usuários

- arquivo principal: `data/users/users.db`
- índice direto por ID: `HashExtensivel`, mantido internamente por `aed3.Arquivo`
- índice secundário por e-mail: `data/indexes/users/email.idx`

### Cursos

- arquivo principal: `data/courses/courses.db`
- índice direto por ID: `HashExtensivel`, mantido internamente por `aed3.Arquivo`
- índice secundário por código compartilhável: `data/indexes/courses/share-code.idx`
- índice por nome do curso dentro do usuário: `data/indexes/courses/owner-name.idx`
- índice de relacionamento 1:N entre usuário e cursos: `data/indexes/courses/owner-relation.idx`
- dicionário do índice invertido: `data/indexes/courses/name-terms.dict`
- blocos do índice invertido: `data/indexes/courses/name-terms.blocks`

### Índice invertido e TF-IDF

O nome de cada curso passa pelo seguinte processamento:

1. remoção de acentos e conversão para letras minúsculas
2. separação das palavras e descarte de pontuação
3. remoção de stop words e numerais
4. contagem das ocorrências de cada termo
5. cálculo de `TF = ocorrências do termo / total de termos válidos`

Cada termo é associado a elementos `(courseId, TF)` na `ListaInvertida`. Na busca, os termos digitados recebem o mesmo tratamento. Para cada lista recuperada, o sistema calcula:

```text
IDF = log10(total de cursos / cursos que contêm o termo) + 1
pontuação do curso = soma(TF × IDF) dos termos consultados
```

Os resultados são ordenados pela pontuação decrescente. Empates são resolvidos pelo nome normalizado e, depois, pelo ID.

O exemplo numérico do enunciado informa `0,2 × 1,301 = 0,206`. O produto correto é aproximadamente `0,260`; a implementação segue a fórmula definida no próprio enunciado. Essa correção não altera a ordem esperada `[1, 3, 2]`.

### Manutenção do índice

- inclusão: calcula os TFs e adiciona uma entrada em cada lista de termo
- alteração do nome: remove as entradas do nome anterior e indexa o novo nome
- alteração de outros dados/estado: mantém o índice consistente pela mesma rotina de reindexação
- exclusão física: remove o ID de todas as listas associadas ao nome
- exclusão de usuário com cursos inativos: a remoção dos cursos também atualiza o índice

### Inscrições

- arquivo principal: `data/course-users/course-users.db`
- índice direto por ID: `HashExtensivel`, mantido internamente por `aed3.Arquivo`
- índice N:N por curso: `data/indexes/course-users/course.idx`
- índice N:N por usuário: `data/indexes/course-users/user.idx`

## Fluxos implementados

### Acesso ao sistema

- login
- cadastro de novo usuário
- recuperação de senha por pergunta secreta

### Meus dados

- visualizar dados
- corrigir nome, e-mail e pergunta secreta
- alterar senha
- excluir conta, respeitando a regra de bloqueio por cursos ativos

### Meus cursos

- cadastrar novo curso
- listar cursos em ordem alfabética
- visualizar curso
- corrigir dados do curso
- encerrar inscrições
- concluir curso
- cancelar curso, marcando o estado como `CANCELED`
- gerenciar inscritos no curso
- visualizar nome, e-mail e data de inscrição de cada inscrito
- cancelar uma inscrição específica
- exportar a lista de inscritos em CSV

### Minhas inscrições

- listar cursos em que o usuário ativo está inscrito, exibindo estados especiais do curso
- buscar curso por código NanoID
- buscar cursos por palavras-chave, com ranking TF-IDF e paginação de 10 resultados
- listar todos os cursos de outras pessoas com paginação de 10 itens
- visualizar dados completos do curso e do autor
- fazer inscrição em cursos abertos
- cancelar a própria inscrição

### Regras de negócio

- o e-mail do usuário é normalizado e deve ser único
- o código compartilhável do curso é gerado automaticamente e deve ser único
- novos cursos sempre são vinculados ao usuário logado
- usuários não podem se inscrever nos próprios cursos
- inscrições duplicadas para o mesmo par usuário/curso são bloqueadas
- novas inscrições só são permitidas em cursos com estado `OPEN_FOR_ENROLLMENT`
- o cancelamento de curso preserva o curso com estado `CANCELED`, permitindo que inscrições existentes exibam esse estado
- um usuário não pode ser excluído enquanto possuir cursos ativos
- se o usuário só possuir cursos inativos, os cursos e suas inscrições são removidos antes da exclusão da conta
- ao excluir uma conta, inscrições feitas pelo usuário em cursos de outras pessoas também são removidas

## Principais classes do projeto

### Bootstrap

- `entrepairs.app.Application`

### Controllers

- `entrepairs.controller.ApplicationController`
- `entrepairs.controller.AuthController`
- `entrepairs.controller.UserController`
- `entrepairs.controller.CourseController`
- `entrepairs.controller.EnrollmentController`

### Views

- `entrepairs.view.ConsoleSupport`
- `entrepairs.view.AuthenticationView`
- `entrepairs.view.HomeView`
- `entrepairs.view.UserView`
- `entrepairs.view.CourseView`
- `entrepairs.view.EnrollmentView`

### Services

- `entrepairs.service.AuthService`
- `entrepairs.service.UserService`
- `entrepairs.service.CourseService`
- `entrepairs.service.EnrollmentService`
- `entrepairs.service.PasswordHasher`
- `entrepairs.service.Sha256PasswordHasher`
- `entrepairs.service.ShareCodeGenerator`
- `entrepairs.service.NanoIdShareCodeGenerator`
- `entrepairs.model.CourseSearchResult`

### Repositories e persistência

- `entrepairs.repository.adapter.Aed3UserRepository`
- `entrepairs.repository.adapter.Aed3CourseRepository`
- `entrepairs.repository.adapter.Aed3CourseUserRepository`
- `entrepairs.repository.adapter.UserRecord`
- `entrepairs.repository.adapter.CourseRecord`
- `entrepairs.repository.adapter.CourseUserRecord`
- `aed3.Arquivo`
- `aed3.HashExtensivel`
- `aed3.ArvoreBMais`
- `aed3.ListaInvertida`

### Índices e utilitários

- `entrepairs.repository.index.UserEmailKey`
- `entrepairs.repository.index.CourseShareCodeKey`
- `entrepairs.repository.index.UserCourseKey`
- `entrepairs.repository.index.UserCourseNameKey`
- `entrepairs.repository.index.CourseEnrollmentKey`
- `entrepairs.repository.index.UserEnrollmentKey`
- `entrepairs.util.TextNormalizer`
- `entrepairs.util.CourseNameTerms`
- `entrepairs.util.IndexKeys`
- `entrepairs.util.DateFormats`

## Compilação e execução

### Requisitos

- Java JDK 11 ou superior

### Executar com script

```sh
chmod +x run.sh
./run.sh
```

### Compilar manualmente

```sh
rm -rf out
mkdir -p out
javac -d out $(find src -name '*.java' | sort)
java -cp out entrepairs.app.Application
```

## Testes

Os testes não dependem de bibliotecas externas. O script compila `src/` e `tests/` em um diretório temporário e executa os testes em outro diretório temporário, sem alterar os dados reais em `data/`.

```sh
./test.sh
```

Saída esperada:

```text
[OK] CourseNameTermsTest
[OK] ListaInvertidaTest
[OK] Aed3CourseRepositoryTest
[OK] EnrollmentControllerTest
Todos os testes passaram.
```

### Plano de testes

| ID | Tipo | Cenário | Resultado esperado | Cobertura automatizada |
|---|---|---|---|---|
| UT-01 | Unitário | Normalizar `Introdução à Inteligência Artificial` | Vetor `introducao, inteligencia, artificial` | Sim |
| UT-02 | Unitário | Remover stop words, pontuação e numerais | Somente termos relevantes permanecem | Sim |
| UT-03 | Unitário | Nome com termo repetido | TF de `inteligencia` igual a `2/5 = 0,4` | Sim |
| UT-04 | Unitário | Inserir três postagens em blocos de capacidade 2 | Criação de bloco encadeado sem perda de dados | Sim |
| UT-05 | Unitário | Inserir o mesmo ID duas vezes no mesmo termo | Duplicidade rejeitada | Sim |
| UT-06 | Unitário | Excluir postagem existente e inexistente | Remoção correta e retorno `false` para ausente | Sim |
| UT-07 | Unitário | Fechar e reabrir `ListaInvertida` | IDs e frequências permanecem persistidos | Sim |
| IT-01 | Integração | Cadastrar os quatro cursos do enunciado | Termos e TFs gravados no índice | Sim |
| IT-02 | Integração | Buscar `Inteligência Artificial` | Ordem dos IDs `[1, 3, 2]` | Sim |
| IT-03 | Integração | Validar pontuação TF-IDF | Curso 1 ≈ `0,808`; curso 3 ≈ `0,710` | Sim |
| IT-04 | Integração | Alterar nome do curso 3 para `Programação em Java` | Termos antigos removidos e novos termos pesquisáveis | Sim |
| IT-05 | Integração | Excluir fisicamente o curso 1 | ID removido das listas invertidas | Sim |
| IT-06 | Integração | Reabrir o repositório | Busca continua funcionando com dados persistidos | Sim |
| IT-07 | Integração | Consultar apenas stop words e numerais | Lista vazia, sem erro | Sim |
| IT-08 | Integração | Remover os arquivos do novo índice e reabrir cursos existentes | Índice é reconstruído automaticamente | Sim |
| IT-09 | Integração MVC | Acessar a opção B com entrada e saída simuladas | Tela de busca e relevância são exibidas | Sim |
| IT-10 | Integração MVC | Buscar termo presente em curso próprio e de terceiro | Somente o curso de terceiro aparece | Sim |
| IT-11 | Integração MVC | Selecionar um resultado da busca | Tela completa do curso é aberta | Sim |
| IT-12 | Integração MVC | Buscar 11 resultados e avançar a página | Segunda página é exibida corretamente | Sim |
| MT-01 | Manual | Repetir o fluxo completo na CLI antes da gravação | Telas e mensagens correspondem às evidências | Recomendado antes da entrega |

### Estratégia e critérios

- testes unitários validam transformação textual e operações isoladas da lista invertida
- testes de integração validam persistência, manutenção do índice e ranking no repositório
- testes manuais validam navegação, mensagens e paginação da CLI
- aprovação automatizada exige compilação sem erros e todos os testes com estado `[OK]`
- aprovação manual exige que nenhuma operação gere exceção e que a ordem visual corresponda ao ranking calculado

Para reiniciar os dados persistidos antes de um novo teste manual:

```sh
rm -rf data out
./run.sh
```

## Capturas de tela

As saídas abaixo documentam as telas e mensagens usadas nos testes de fumaça e no teste automatizado do fluxo MVC.

### Cadastro e criação de curso

```text
EntrePares 1.0
--------------
> Início > Meus cursos

CURSOS
Nenhum curso cadastrado.

(A) Novo curso
(R) Retornar ao menu anterior
```

```text
EntrePares 1.0
--------------
> Início > Meus cursos > Curso de Java

CÓDIGO........: jxmagnrsdi
NOME..........: Curso de Java
DESCRIÇÃO.....: Curso intensivo
DATA DE INÍCIO: 20/05/2026
ESTADO........: 0 - Curso ativo e recebendo inscrições
```

### Inscrições

```text
EntrePares 1.0
--------------
> Início > Minhas inscrições

INSCRIÇÕES
Nenhuma inscrição cadastrada.

(A) Buscar curso por código
(B) Buscar curso por palavras-chave
(C) Listar todos os cursos
```

```text
EntrePares 1.0
--------------
> Início > Minhas inscrições > Busca por palavras-chave

Página 1 de 1

(1) Introdução à Inteligência Artificial - 20/06/2026 - relevância 0.808
(2) Inteligência no Trabalho por Meio da Inteligência Artificial - 21/06/2026 - relevância 0.710
(3) Inteligência Emocional para Gestores - 22/06/2026 - relevância 0.375
```

```text
EntrePares 1.0
--------------
> Início > Minhas inscrições > Lista de cursos

Página 1 de 1

(1) Curso Teste - 20/06/2026
```

```text
Inscrição realizada com sucesso.
```

### Regra de exclusão de usuário

```text
A conta não pode ser excluída enquanto houver cursos ativos vinculados a ela.
```

```text
Conta excluída com sucesso.
```

## Vídeo de demonstração

Adicionar aqui, antes da entrega, o link do vídeo de até 3 minutos:

- https://www.youtube.com/watch?v=Lg55x-LUR8M

Sugestão de roteiro:

1. cadastro de usuário
2. login
3. criação de curso
4. login com outro usuário
5. busca por `Inteligência Artificial` e demonstração da ordem TF-IDF
6. alteração do nome de um curso e nova busca para comprovar a atualização do índice
7. inscrição em um resultado e gerenciamento de inscritos pelo autor

## Implementação das funcionalidades solicitadas

O trabalho foca na implementação do índice invertido e busca de cursos por palavras-chave do nome, empregando a lógica TF-IDF. O projeto compila corretamente e funciona sem erros de execução, consistindo em um esforço original integrado à arquitetura estabelecida.

### Índice Invertido e Busca TF-IDF

A busca por palavras-chave está disponível no menu de inscrição e funciona com base nos seguintes princípios implementados:

- **Índice Invertido**: Construído utilizando a classe `aed3.ListaInvertida` (adaptada para armazenar também o valor TF de cada termo). O índice é atualizado dinamicamente pelo repositório sempre que um curso é inserido, removido ou tem seu nome alterado.
- **Processamento de Termos**: Tanto na indexação quanto na busca, os textos têm acentos removidos, são convertidos para minúsculas e têm sua pontuação, numerais e *stop words* ignorados.
- **Cálculo de TF-IDF**: O índice armazena o valor TF (frequência do termo no nome). Durante as buscas, o IDF é calculado dinamicamente considerando o total de cursos cadastrados utilizando a fórmula `log10(total_cursos / cursos_com_o_termo) + 1`. Os valores são multiplicados e as pontuações para um mesmo ID são somadas.
- **Ordenação**: Os resultados da busca são apresentados aos usuários de forma paginada (de 10 em 10) e ordenados decrescentemente pelo valor calculado de TFxIDF.

As demais funcionalidades de gerenciamento de inscrições e relacionamento de usuários e cursos implementadas nas etapas anteriores foram preservadas, com os devidos índices diretos (Hash Extensível) e secundários (Árvores B+) operando corretamente.
