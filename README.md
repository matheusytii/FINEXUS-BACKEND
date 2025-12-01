# Finexus -- API Backend para Plataforma de Investimentos e Crowdfunding Financeiro

Finexus é uma API REST desenvolvida para gerenciar propostas de crédito,
investimentos, usuários, saldos, dívidas, parcelas e avaliações de
risco.\
O sistema foi projetado para plataformas financeiras e soluções de
crowdfunding, oferecendo segurança, escalabilidade e controle total
sobre operações financeiras.

Ideal para fintechs, plataformas de crédito, microinvestidores e
startups que precisam de uma estrutura sólida de backend financeiro.

------------------------------------------------------------------------

## 📌 API Pública (Render)

URL será adicionada após o deploy:\
https://finexus-backend.onrender.com

------------------------------------------------------------------------

## ⚙️ Funcionalidades

### 🔒 Autenticação e Usuários

-   Login e cadastro com JWT\
-   Perfis: **Investidor e Tomador**\
-   Validação de CPF/CNPJ\
-   CRUD completo de usuários

### 💸 Propostas e Investimentos

-   Cadastro e gerenciamento de propostas\
-   Registro de investimentos por proposta\
-   Cálculo de valores e retornos\
-   Relação **Proposta ↔ Investidor ↔ Parcela**

### 🏦 Gestão Financeira

-   Controle de saldos individuais\
-   Cadastro automático de dívidas\
-   Parcelamento automático\
-   Relação **Dívida ↔ Investidor**

### 📊 Formulário de Risco

-   Avaliação financeira do solicitante\
-   Análise de risco integrada

### 📈 Dashboard via API

-   Resumo financeiro\
-   Propostas por status\
-   Montantes investidos\
-   Dados para gráficos

### 📘 Documentação (Swagger)

Disponível em:\
`/swagger-ui.html`

------------------------------------------------------------------------

## 🧱 Estrutura do Projeto

    src/
    ├── controller/            # Endpoints REST
    ├── model/                 # Entidades JPA
    ├── repository/            # Acesso ao banco (JPA)
    ├── security/              # JWT + Spring Security
    ├── service/               # Regras de negócio
    ├── dto/                   # Objetos de transferência
    └── application.properties # Configurações

------------------------------------------------------------------------

## 🗄️ Banco de Dados (MySQL)

Tabelas principais: - usuarios\
- propostas\
- investimentos\
- saldos\
- dividas\
- parcelas_divida\
- formulario_risco\
- divida_investidores

As tabelas são criadas automaticamente pelo **Hibernate**.

------------------------------------------------------------------------

## 🛠️ Tecnologias

  Área           Tecnologia
  -------------- --------------------------
  Backend        Java 17 + Spring Boot
  Segurança      Spring Security + JWT
  Banco          MySQL (Railway / Render)
  ORM            Hibernate / JPA
  Build          Maven
  Documentação   Swagger (Springdoc)
  Deploy         Render / Railway

------------------------------------------------------------------------

## 🔐 Variáveis de Ambiente (Render)

Configure no painel:

    DB_URL=jdbc:mysql://<host>:<port>/<database>?useSSL=false&serverTimezone=UTC
    DB_USER=<usuario>
    DB_PASS=<senha>

    JWT_SECRET=<chave_jwt>

### Exemplo de application.properties (local ou produção)

    spring.datasource.url=${DB_URL}
    spring.datasource.username=${DB_USER}
    spring.datasource.password=${DB_PASS}

    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true

    springdoc.api-docs.enabled=true
    springdoc.swagger-ui.enabled=true

------------------------------------------------------------------------

# FINEXUS-BACKEND — Como Rodar Localmente

## 📌 Caminho rápido pra deixar tudo funcionando

| Passo | O que fazer | Detalhe útil |
|------|-------------|---------------|
| **1. Ter instalado** | Java 17+, Maven 3.9+, Gradle 8+ | O trio que faz o projeto respirar. |
| **2. Abrir a IDE** | Visual Studio Code | Não precisa luxo, só abrir o projeto. |
| **3. Clonar o projeto** | git clone https://github.com/matheusytii/FINEXUS-BACKEND.git | Traz o código pra sua máquina. |
| **4. Entrar na pasta** | cd FINEXUS-BACKEND| Agora você está dentro do projeto. |
| **5. Instalar dependências** | mvnw clean install | O Maven baixa tudo o que o projeto precisa. |
| **6. Ajustar o banco** | spring.datasource.url=jdbc:mysql://localhost:3306/seu_banco | Coloque o nome do seu banco aqui. |
| | `spring.datasource.username=SEU_USUARIO | Seu usuário do MySQL. |
| | `spring.datasource.password=SUA_SENHA | Sua senha do MySQL. |
| **7. Rodar o projeto** | mvnw spring-boot:run | Pronto, a aplicação sobe em http://localhost:8080 |






---

## ✔️ Verificação (no terminal)

```cmd
java -version
mvn -v
gradle -v



### 1. Clone o repositório

    git clone https://github.com/seu-usuario/finexus-backend.git

### 2. Acesse o diretório

    cd finexus-backend

### 3. Configure seu application.properties local

    spring.datasource.url=jdbc:mysql://localhost:3306/finexus_db
    spring.datasource.username=root
    spring.datasource.password=senha

    spring.jpa.hibernate.ddl-auto=update
    jwt.secret=teste123

### 4. Execute o servidor

    mvn spring-boot:run

Servidor disponível em:\
https://finexus-backend.onrender.com

------------------------------------------------------------------------

## 📦 Dependências Principais

-   spring-boot-starter-web\
-   spring-boot-starter-jpa\
-   spring-boot-starter-security\
-   spring-boot-starter-validation\
-   springdoc-openapi-starter-webmvc-ui\
-   mysql-connector-j\
-   jjwt

------------------------------------------------------------------------

## 🧪 Testes

    mvn test

------------------------------------------------------------------------

## 📜 Licença

MIT License
