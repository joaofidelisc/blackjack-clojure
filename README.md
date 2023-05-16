# blackjack-clojure

O jogo Blackjack foi desenvolvido na linguagem de programação Clojure. Esse jogo, consiste em um jogo de cartas, onde o objetivo principal é atingir 21 pontos e derrotar o Dealer. Há duas formas de ganhar esse jogo, a primeira delas é você atingir 21 pontos e a segunda, é o Dealer ultrapassar 21 pontos.

As regras adotadas para o desenvolvimento do jogo foram:
- A ordem das cartas são A, 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, K;
- J, Q e K são iguais ao valor 10;
- Caso o jogador atual (jogador comum ou Dealer) tenha em mãos A e 10
- Caso o jogador atual (jogador comum ou Dealer) tenha em mãos A 5 7, a soma será 13, pois o A será considerado como 1;
- Será atribuido o valor 11 ao A, porém se passar de 21, ele passará a valer 1. Por exemplo, caso o jogador atual tenha em mãos A e 10, poderá valer 11 ou 21.

*O valor do recorde de acordo com o nº de vitórias seguidas é armazenado.
