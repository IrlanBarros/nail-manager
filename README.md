# 💅 Nail Manager - Sistema de Gestão para Nail Designers

Bem-vindo ao projeto **Nail Manager**. Este sistema foi desenvolvido utilizando **JavaFX**, **SQLite** e segue rigorosamente os princípios da **Clean Architecture** (Arquitetura Limpa), **SOLID** e os pilares da **Programação Orientada a Objetos**.

Como este projeto não utiliza gerenciadores de dependências automáticos (como Maven ou Gradle) e as bibliotecas nativas estão no `.gitignore`, siga este guia para configurar seu ambiente de desenvolvimento.

---

## 🛠️ 1. Pré-requisitos do Sistema

Antes de começar, certifique-se de que seu ambiente possui:
* **JDK 21** ou superior instalado.
* **SQLite3** instalado no sistema.
* **JavaFX 21.0.10** instalado de acordo com seu SO.
* **VS Code** com o "Extension Pack for Java" instalado.
* **Apache PDFBox** configurado no projeto (para exportação de relatórios em PDF).

---

## 🚀 2. Configuração do Ambiente (Setup)

### Passo 1: Clonar o Projeto
```bash
git clone [https://github.com/IrlanBarros/nail-manager.git](https://github.com/IrlanBarros/nail-manager.git)
cd nail-manager
```

### Passo 2: Dependências Externas (JARs e Nativas)

Como os arquivos binários não estão no repositório, você deve baixá-los e vinculá-los manualmente ao seu projeto:

* **JavaFX SDK:**
    * Baixe o SDK do JavaFX (versão 21+) em GluonHQ.
    * Extraia a pasta em um local seguro (ex: `~/lib/javafx-sdk`).
    * No VS Code, vá na aba **Java Projects > Referenced Libraries** e adicione todos os arquivos `.jar` da pasta `/lib` do SDK extraído.
* **SQLite JDBC Driver:**
    * Baixe o driver `.jar` do SQLite JDBC (recomendado: versão 3.45.1.0 ou superior).
    * Adicione-o também às **Referenced Libraries** no VS Code.
* **Bibliotecas Nativas (`.so` / `.dll`):**
    * O JavaFX depende de binários nativos. O `.gitignore` está configurado para ignorar arquivos `*.so`, `*.dll` e `*.dylib`. Certifique-se de que o Java reconhece o caminho das bibliotecas nativas do seu SDK.

---

### 🏗️ 3. Como Rodar o Projeto

Para executar o sistema via VS Code, você deve configurar os argumentos de módulo (VM Arguments). Edite seu arquivo `.vscode/launch.json` adicionando:

```json
"vmArgs": "--module-path /caminho/para/seu/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml"
```

### Banco de Dados e Auto-Seeding

Para preparar o ambiente na primeira execução, você deve inicializar e popular o banco de dados executando os seguintes arquivos na ordem abaixo:

1. **Criação do Banco de Dados e Tabelas:**
   * Execute o arquivo `DatabaseConnection`.
   * **Caminho:** `src/infrastructure/persistence/sqlite/DatabaseConnection.java`
   * **Ação:** Isso criará automaticamente a pasta `data/` na raiz do projeto e gerará o arquivo `nail-manager.db` com o esquema completo das tabelas.

2. **População do Banco (Auto-Seeding) e Inicialização:**
   * Em seguida, execute o arquivo principal da aplicação, `Main.java`.
   * **Caminho:** `src/presentation/Main.java`
   * **Ação:** Ao iniciar o sistema, a classe `DatabaseSeeder` será executada automaticamente, populando o banco de dados com as seguintes informações de teste para facilitar o desenvolvimento:
       * **Usuário Admin:** `admin@salao.com` | **Senha:** `123456`
       * **Conteúdo:** 10 Clientes, 3 Serviços, 5 Agendamentos e 5 Transações.

---

### 📂 4. Estrutura de Pastas (Arquitetura)

Mantenha a integridade da Clean Architecture ao criar novas funcionalidades:

* `domain/`: Entidades, interfaces e regras de negócio puras.
* `application/`: Casos de Uso (Intermediários entre UI e Infraestrutura).
* `infrastructure/`: Implementações de banco (SQLite), Hashers de senha e Repositórios.
* `presentation/`: Toda a interface JavaFX.
    * `view/`: Arquivos `.fxml`.
    * `controller/`: Classes controladoras (Injetadas via SceneManager).
    * `asset/`: Arquivos estáticos (`.css`, imagens, etc).
    * `asset/css/`: Arquivos `.css` modulares (ex: `login.css`, `dashboard.css`).

---

### 🎨 5. Padrões Visuais e Identidade

* **Cores Principais:** Azul Escuro (`#2B2D42`) e Vermelho Destaque (`#EF233C`).
* **Estilização:** Nunca use estilos "inline" no FXML. Extraia sempre para arquivos CSS dedicados para facilitar a manutenção.

---

### 📝 6. Fluxo de Trabalho Git

* Sempre crie uma branch para sua tarefa: `git switch -c feature/nome-da-tarefa`.
* **Importante:** Nunca remova as restrições do `.gitignore`. Arquivos `.db`, `.jar`, `.class` ou `.so` não devem ser commitados.
* Faça o commit e abra um Pull Request para revisão.

> Desenvolvido como parte do projeto de automação e arquitetura de software industrial.