# EntrePares 1.0

## Integrantes

- Gabriel Mendonça
- Heitor Moreira
- Matheus Procopio
- Sergio Manso

## Descrição do sistema

O projeto implementa o TP1 do sistema **EntrePares 1.0**, responsável pelo cadastro e autenticação de usuários da PUC Minas e pelo gerenciamento de cursos livres ofertados por esses próprios usuários.

Neste trabalho foram implementados:

- cadastro de usuários com autenticação por e-mail e senha
- recuperação de senha com pergunta e resposta secretas
- CRUD de usuários
- CRUD de cursos
- vínculo 1:N entre usuário e cursos
- listagem dos cursos do usuário ativo em ordem alfabética
- controle de estado do curso
- interface textual em CLI seguindo MVC

As inscrições em cursos ainda não foram implementadas neste TP e aparecem na interface apenas como placeholder para o TP2.

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
  - classes: `ApplicationController`, `AuthController`, `UserController`, `CourseController`
- `src/entrepairs/view`
  - camada de CLI
  - renderiza menus, mensagens e coleta entradas do usuário
- `src/entrepairs/model`
  - entidades de domínio
  - classes: `User`, `Course` e enum `CourseStatus`
- `src/entrepairs/service`
  - regras de negócio de autenticação, perfil e cursos
  - abstrações de suporte como `PasswordHasher` e `ShareCodeGenerator`
- `src/entrepairs/repository/adapter`
  - adaptadores concretos sobre a infraestrutura `aed3`
  - classes: `Aed3UserRepository`, `Aed3CourseRepository`, `UserRecord`, `CourseRecord`
- `src/entrepairs/repository/index`
  - chaves persistidas nas árvores B+
  - classes: `UserEmailKey`, `CourseShareCodeKey`, `UserCourseKey`, `UserCourseNameKey`
- `src/entrepairs/util`
  - utilitários de normalização, datas e padronização de chaves

### Fluxo entre camadas

1. `Application` cria repositórios, serviços, views e controllers.
2. Os controllers orquestram os casos de uso da CLI.
3. Os services aplicam as regras de negócio e validações.
4. Os repositories adaptam as entidades do domínio para os registros persistidos.
5. A persistência é realizada com `aed3.Arquivo`, `HashExtensivel` e `ArvoreBMais`.

### Decisões de projeto

- O conteúdo original em português do pacote `aed3` foi preservado.
- O código da aplicação foi estruturado em inglês, com separação explícita entre domínio e persistência.
- As entidades de domínio (`User` e `Course`) não implementam diretamente `Registro`; a serialização fica isolada em `UserRecord` e `CourseRecord`.
- Os índices secundários e o relacionamento 1:N foram implementados com árvores B+ persistidas em disco.
- O hash da senha e da resposta secreta foi abstraído por `PasswordHasher`, com implementação atual em `Sha256PasswordHasher`.
- A geração do código compartilhável do curso foi abstraída por `ShareCodeGenerator`, com implementação atual em `NanoIdShareCodeGenerator`.

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
- cancelar curso

### Regras de negócio

- o e-mail do usuário é normalizado e deve ser único
- o código compartilhável do curso é gerado automaticamente e deve ser único
- novos cursos sempre são vinculados ao usuário logado
- um usuário não pode ser excluído enquanto possuir cursos ativos
- se o usuário só possuir cursos inativos, os cursos são removidos antes da exclusão da conta
- no escopo do TP1, cancelar um curso remove o curso do sistema

## Principais classes do projeto

### Bootstrap

- `entrepairs.app.Application`

### Controllers

- `entrepairs.controller.ApplicationController`
- `entrepairs.controller.AuthController`
- `entrepairs.controller.UserController`
- `entrepairs.controller.CourseController`

### Views

- `entrepairs.view.ConsoleSupport`
- `entrepairs.view.AuthenticationView`
- `entrepairs.view.HomeView`
- `entrepairs.view.UserView`
- `entrepairs.view.CourseView`

### Services

- `entrepairs.service.AuthService`
- `entrepairs.service.UserService`
- `entrepairs.service.CourseService`
- `entrepairs.service.PasswordHasher`
- `entrepairs.service.Sha256PasswordHasher`
- `entrepairs.service.ShareCodeGenerator`
- `entrepairs.service.NanoIdShareCodeGenerator`

### Repositories e persistência

- `entrepairs.repository.adapter.Aed3UserRepository`
- `entrepairs.repository.adapter.Aed3CourseRepository`
- `entrepairs.repository.adapter.UserRecord`
- `entrepairs.repository.adapter.CourseRecord`
- `aed3.Arquivo`
- `aed3.HashExtensivel`
- `aed3.ArvoreBMais`

### Índices e utilitários

- `entrepairs.repository.index.UserEmailKey`
- `entrepairs.repository.index.CourseShareCodeKey`
- `entrepairs.repository.index.UserCourseKey`
- `entrepairs.repository.index.UserCourseNameKey`
- `entrepairs.util.TextNormalizer`
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

O projeto possui artefatos de build voltados a teste em `out-test/`, indicando que já houve compilação separada para esse fim. No estado atual do repositório, porém, os arquivos-fonte e o comando oficial de execução desses testes não estão documentados no workspace.

Assim, a validação descrita e reproduzível a partir deste repositório está sendo feita por:

- compilação com `javac`
- testes de fumaça executando a CLI
- verificação manual dos fluxos de autenticação, usuário e cursos

Para reiniciar os dados persistidos antes de um novo teste manual:

```sh
rm -rf data out
./run.sh
```

## Capturas de tela

As capturas abaixo reproduzem saídas reais do terminal obtidas durante os testes de fumaça.

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

### Regra de exclusão de usuário

```text
A conta não pode ser excluída enquanto houver cursos ativos vinculados a ela.
```

```text
Conta excluída com sucesso.
```

## Vídeo de demonstração

Adicionar aqui, antes da entrega, o link do vídeo de até 3 minutos:

- `LINK_DO_VIDEO_AQUI`

Sugestão de roteiro:

1. cadastro de usuário
2. login
3. criação de curso
4. alteração de estado do curso
5. tentativa de excluir usuário com curso ativo
6. conclusão do curso
7. exclusão do usuário

## Checklist solicitado

### Há um CRUD de usuários com persistência em arquivo e índices diretos e indiretos funcionando corretamente?

**Sim.** O CRUD de usuários usa `aed3.Arquivo` para persistência principal, índice direto por hash extensível e índice secundário por e-mail com árvore B+ no `Aed3UserRepository`.

### Há um CRUD de cursos com persistência em arquivo e índices diretos e indiretos funcionando corretamente?

**Sim.** O CRUD de cursos usa `aed3.Arquivo` para persistência principal e acrescenta índices B+ para código compartilhável, ordenação por nome e relacionamento por usuário.

### Os cursos estão vinculados aos usuários usando o `ownerUserId` como chave estrangeira lógica?

**Sim.** O campo `ownerUserId` em `Course` registra o dono do curso e é usado pelos índices de relacionamento e ordenação.

### Há uma árvore B+ que registre o relacionamento 1:N entre usuários e cursos?

**Sim.** O índice persistido `data/indexes/courses/owner-relation.idx` registra o par `ownerUserId -> courseId`.

### O trabalho compila corretamente?

**Sim.** O projeto compila com `javac` sem erros.

### O trabalho está completo e funcionando sem erros de execução, dentro do escopo do TP1?

**Sim.** Cadastro, autenticação, recuperação de senha, CRUD de usuários, CRUD de cursos, vínculo 1:N, ordenação alfabética e transições de estado foram validados com testes de fumaça. A funcionalidade de inscrições permanece como placeholder porque pertence ao TP2.
