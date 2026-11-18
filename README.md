# Workshop JavaFX e JDBC

Abra o projeto com suporte a JavaFX e adicione o MySQL Connector/J ao classpath.
O aplicativo usa um banco MySQL existente com as tabelas `department` e `seller`.

A partir da raiz do projeto, crie a configuração local com permissão de leitura e escrita apenas para o seu usuário.

```sh
(umask 077 && cp -n db.properties.example db.properties)
chmod 600 db.properties
```

Em `db.properties`, informe o endereço e a porta do banco de desenvolvimento, seu usuário MySQL e a senha correspondente. O exemplo usa o MySQL local na porta 3308. Execute `application.Main` com a raiz do projeto como diretório de trabalho.
