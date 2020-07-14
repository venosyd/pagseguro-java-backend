PagSeguro Java Backend
------------------

WebService em Java pra fornecer algumas funcoes basicas  
de integracao com o PagSeguro.

Recomendado usar com o [AIO Bequiend](https://github.com/venosyd/aio_bequiend)  

De início use:

```
./install-dependencies && ./build
```

e em outras compilações apenas:

```
./build
```

- **GET)** /token
    - Retorna seu token de usuario pagseguro configurado no assets/config.yaml

- **GET)** /credential
    - Retorna seu usuario pagseguro configurado no assets/config.yaml

- **GET)** /session
    - Retorna uma sesssionID

- **POST)** /create-plan
    - Cria um plano, para pagamento recorrente

- **POST)** /ccbrand
    - Retorna a bandeira do cartao de credito passado como numero nos parametros

- **POST)** /cctoken
    - Retorna o token do cartao de credito

- **POST)** /get-parcelas
    - Retorna um array com valores de parcelas, soma com juros e a quantidade

- **POST)** /do-checkout
    - Faz o checkout transparente

- **POST)** /see-checkout
    - Retorna os dados de um checkout

- **POST)** /do-subscription
    - Faz uma inscricao

- **POST)** /see-subscription
    - Retorna os dados de uma inscricao

- **POST)** /cancel-subscription
    - Cancela a inscricao de um aderente

## OBSERVACAO

Necessario coletar o **senderHash** (id especifico da  
operacao com o cliente) para fazer o checkout ou a  
inscricao (subscribe).

Para isso, colocoque o arquivo pagseguro.html e pagseguro.sandbox.html em  
algum servidor de arquivos estaticos e acesse usando a URL  

```
http(s)://{dominio.com}/pagseguro.html?sessionID={sessionID}
```

O sessionID é o resultado da consulta /session. A  
pagina ira carregar e mostrar o hash especifico.

