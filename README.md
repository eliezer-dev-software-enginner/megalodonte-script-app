# Megalodonte Script App

Orquestra aplicações cujo código cliente contém apenas scripts Java por view.
Usa `megalodonte-jsonc-ui` para interpretar e renderizar os dados.

## Configurar uma vez

No consumidor, use `megalodonte:megalodonte-script-app:0.1.0`, Java 25 e o plugin
JavaFX 25. `application.mainClass` é `megalodonte.script.ScriptLauncher`.
Em desenvolvimento, use `includeBuild("../megalodonte-script-app")`.

Crie `src/main/resources/application.properties`:

```properties
app.name=Meu aplicativo
app.initialView=login
app.scriptsPackage=meu.app.scripts
app.views=/views
app.width=520
app.height=680
app.minWidth=420
app.minHeight=640
```

As dimensões são pixels da janela. O pacote dos scripts é obrigatório; as outras
propriedades têm defaults conforme o exemplo, exceto app.name (Megalodonte).
A configuração usa UTF-8. O launcher inicializa tema, janela e host das views.

## Adicionar uma tela

1. Crie `views/login.view.jsonc`, com id login e os estados/componentes.
2. Crie a classe pública `meu.app.scripts.Login`, estendendo `ViewScript`.
3. Implemente `public void onSubmit()` com a lógica após a validação.

Não há lista de telas nem registro manual de ações. Cada destino é carregado pelo
nome ao navegar. `login_screen.view.jsonc` resolve LoginScreen; login resolve Login.
Os scripts são Java compilado, com construtor público sem argumentos, e recebem
uma nova instância por sessão. Não há interpretação Java ou hot reload.

### Eventos e dados

- onSubmit: valida os campos antes de invocar o script.
- navigate_cadastro: abre cadastro.view.jsonc e resolve Cadastro.java.
- limpar: resolve um método público de instância `void limpar()` no script atual.

Nos componentes, use events.onClick para botões e events.onEnter para inputs.
onSubmit, o prefixo navigate_ e os callbacks de ciclo de vida são reservados.
As ações comuns precisam ser públicas, sem argumentos e retornar void. O runtime
rejeita métodos de Object, helpers da classe base e callbacks de ciclo de vida.
Apenas onSubmit passa pela validação automática.

ViewScript oferece helpers protegidos:

```java
String email = text("email");
Object valor = get("total");
State<BigDecimal> total = state("total", BigDecimal.class);
set("mensagem", "Pronto");
message("Pronto"); // atalho para o estado string mensagem
navigate("cadastro");
```

O tratamento padrão de validação usa o estado string mensagem, que a view deve
declarar e exibir. Para personalizar, sobrescreva onValidationFailed(Map<String,String>).
Operações demoradas devem sair da thread JavaFX; atualizações dos estados de uma
tela montada e navegação devem ocorrer na thread JavaFX.

### Ciclo de vida

onMount() é chamado ao montar a tela e onDestroy() antes de descartá-la.
A navegação mantém a janela e libera bindings, tema, handlers e script anteriores.
Um destino que falha ao carregar/renderizar preserva a tela atual.
Reabrir uma tela cria um formulário novo, sem preservação implícita dos dados.
ScriptApplication também pode ser hospedado por uma aplicação existente.

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-25.0.4.1'
./gradlew.bat test
./gradlew.bat publishToMavenLocal
```
