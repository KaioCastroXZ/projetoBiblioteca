# ProjetoPoo

## Entidades
1. Livro
2. Biblioteca

## Livro
- Atributos
  - id : int
  - nome : String
- Métodos
  - Construtor
    - Livro
    - Livro (id : int, nome : String)
  - getId : int
  - setId : void
  - getNome : String
  - setNome : void

## Biblioteca
- Atributos
  - livros : List<Livro>
  - quantidadeFixaDeDias : int 
  - dataDeRetirada : Date 
  - dataDeDevolucao : Date 
  - multa : boolean
- Métodos
  - Construtor
    - Biblioteca
    - Biblioteca (livros : List<Livro>, dataDeRetirada : Date, dataDeDevolucao : Date, multa : boolean)
  - getLivros : List<Livro>
  - setLivros : void
  - getDataDeRetirada : Date
  - setDataDeRetirada : void
  - getDataDeDevolucao : Date
  - setDataDeDevolucao : void
  - getMulta : boolean
  - setMulta : void
  - calcularDataDevolucao : Date
  - verificarMulta : void