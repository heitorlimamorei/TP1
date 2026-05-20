# EntrePares 1.0

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
  - classes: `User`, `Course`, `CourseUser` e enum `CourseStatus`
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
- O relacionamento N:N entre cursos e usuários foi implementado com um CRUD próprio de associação e duas árvores B+: uma por curso e outra por usuário.
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
- listar todos os cursos de outras pessoas com paginação de 10 itens
- visualizar dados completos do curso e do autor
- fazer inscrição em cursos abertos
- cancelar a própria inscrição
- manter a busca por palavras-chave como item reservado para o TP3

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

### Índices e utilitários

- `entrepairs.repository.index.UserEmailKey`
- `entrepairs.repository.index.CourseShareCodeKey`
- `entrepairs.repository.index.UserCourseKey`
- `entrepairs.repository.index.UserCourseNameKey`
- `entrepairs.repository.index.CourseEnrollmentKey`
- `entrepairs.repository.index.UserEnrollmentKey`
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
- teste de fumaça automatizado em diretório temporário criando autor, curso, aluno e inscrição
- verificação manual dos fluxos de autenticação, usuário, cursos e inscrições

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

- `LINK_DO_VIDEO_AQUI`

Sugestão de roteiro:

1. cadastro de usuário
2. login
3. criação de curso
4. login com outro usuário
5. listagem de cursos e inscrição
6. gerenciamento de inscritos pelo autor
7. exportação CSV

## Checklist solicitado

### Há um CRUD da entidade de associação CursoUsuario (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade) que funciona corretamente?

**Sim.** Dentro da arquitetura deste projeto, a entidade de associação foi implementada como `CourseUser` (`CursoUsuario`) com CRUD em `Aed3CourseUserRepository`. O repositório usa `aed3.Arquivo`, que mantém o índice direto com Hash Extensível, e acrescenta duas Árvores B+: `CourseEnrollmentKey` para `courseId -> courseUserId` e `UserEnrollmentKey` para `userId -> courseUserId`.

### A visão de inscrições está corretamente implementada e permite consultas aos cursos em que um usuário está inscrito?

**Sim.** `EnrollmentController` e `EnrollmentView` implementam o menu "Minhas inscrições", listam os cursos do usuário ativo, mostram dados completos do curso e permitem cancelar a própria inscrição.

### A visão de cursos funciona corretamente e permite a gestão dos usuários inscritos em um curso?

**Sim.** A opção "Gerenciar inscritos no curso" em "Meus cursos" lista usuários inscritos, mostra nome, e-mail e data de inscrição, permite cancelar uma inscrição e exporta a lista em CSV.

### Há uma visualização dos cursos de outras pessoas por meio de um código NanoID?

**Sim.** A opção "Buscar curso por código" consulta o índice `CourseShareCodeKey` e abre diretamente a tela completa do curso encontrado, sem passar pela tela de lista.

### A integridade do relacionamento entre cursos e usuários está mantida em todas as operações?

**Sim.** O sistema bloqueia inscrição duplicada, impede inscrição no próprio curso, só permite novas inscrições em cursos abertos, remove inscrições ao excluir usuário e remove inscrições de cursos removidos fisicamente durante exclusão de conta. O cancelamento de curso marca o curso como `CANCELED`, preservando as inscrições existentes para exibição do estado.

### O trabalho compila corretamente?

**Sim.** O projeto compila com `javac -d out $(find src -name '*.java' | sort)` sem erros.

### O trabalho está completo e funcionando sem erros de execução?

**Sim.** Foi executado teste de fumaça criando autor, curso, aluno e inscrição em diretório temporário, além da compilação completa do projeto.

### O trabalho é original e não a cópia de um trabalho de outro grupo?

**Sim.** O código foi desenvolvido sobre a base local existente do projeto e mantém a mesma arquitetura em camadas já usada no TP1.
