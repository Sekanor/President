/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package president;

import president.Cards.Packs.PlayedCards;
import president.Cards.Packs.CardSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import president.Cards.Card;
import president.Cards.Packs.CardPile;

/**
 *
 * @author Kévin
 */
public class Game {
    
    private Integer nbPlayers;
    private ArrayList<Player> playerList;
    private CardSet cardSet;
    private CardPile cardPile;
    private Player currentPlayer;
    private Integer currentIndex;
    private boolean shutUp;
    private boolean replay;
    private Integer nbFinishedPlayers;
    private Restricter restricter;
    private boolean textDisplay;
    
    public Game(ArrayList<Player> playerList) {
        this.playerList = playerList;
        this.nbPlayers = playerList.size();
        this.cardSet = new CardSet();
        this.cardPile = new CardPile();
        this.currentPlayer = null;
        this.currentIndex = 0;
        this.shutUp = false;
        this.replay = false;
        this.nbFinishedPlayers = 0;
        this.restricter = new Restricter(this, cardPile);
        this.textDisplay = true;
    }
    
    
    // --------------
    
    
    /**
     * Simulates a game of President.
     * 
     * Déroulement d'une partie :
        
        1. Tout le monde démarre neutre
        2. Les 52 cartes du paquet sont divisées en parts égales selon
           tous les joueurs
           (les cartes extra sont données au hasard)
        3. S'il y a des rôles (président/trouduc) ils échangent leurs cartes
        4. Le jeu commence - le trouduc commence a jouer (ou au hasard)
        5. Le joueur place soit :
           - 1 carte simple
           - 2 cartes de même valeur (double)
           - 3 cartes de même valeur (triple)
           - 4 cartes de même valeur (cause une révolution, + tard)
        6. Le prochain joueur doit placer une carte de valeur supérieure à celle
           déjà placée.
           - S'il place un 2 : le tas est remis à zéro
           - S'il place la même carte : le suivant doit replacer la même carte,
             sinon il ne peut jouer
           - Si la même carte est placée 4 fois : le tas est remis à zéro
           - S'il place une carte et que personne ne peut jouer : le tas est
             remis à zéro
        7. Un joueur n'ayant plus de cartes dans son jeu gagne.
           - Cependant : s'il finit par un 2, il perd.
        
        
        CHOSES A IMPLEMENTER :
        - Cartes extra données au hasard
        - Le trouduc commence a jouer
        - Révolution
        - Si on finit par un 2, on perd
     */
    public void play() {
        
        this.setupGame();
        this.distributeCards();
        this.exchangeCards();
        while (this.isPlaying()) {
            this.turn();
        }
        this.defineNewTitles();
        this.displayResults();
        this.addPoints();
        
    }
    
    
    private void setupGame() {
        this.nbPlayers = playerList.size();
        this.cardPile.clear();
        
        Random r = new Random();
        int low = 0;
        int high = nbPlayers;
        this.currentIndex = r.nextInt(high-low)+low;
        
        this.shutUp = false;
        this.replay = false;
        this.nbFinishedPlayers = 0;
        
        for (Player player : playerList) {
            player.setGameRank(null);
            player.setTurnPassed(false);
        }
    }
    
    
    /**
     * Distributes the cards.
     */
    private void distributeCards() {
        
        // Reset card lists
        for (Player player : this.playerList) {
            player.emptyCardList();
        }
        
        Integer receivingPlayerIndex = 0;
        Player receivingPlayer;
        
        Integer nbCards = this.cardSet.getCards().size();
        Integer distributedCards = 0;
        
        // Distribute all cards
        // To change here : randomize the last cards
        for (Card card : this.cardSet.getCards()) {
            
            // Change player
            receivingPlayer = this.playerList.get(receivingPlayerIndex);
            receivingPlayerIndex++;
            if (receivingPlayerIndex == this.playerList.size()) {
                receivingPlayerIndex = 0;
            }
            
            // Give card to player
            receivingPlayer.addCard(card);
            
            distributedCards++;
            
        }
        
    }
    
    /**
     * Exchanges the cards between president roles and dirt roles.
     */
    private void exchangeCards() {
        Player president = null;
        Player vicePresident = null;
        Player dirt = null;
        Player lowestDirt = null;
        
        // Reference titles
        for (Player player : this.playerList) {
            switch (player.getTitle()) {
                case President:
                    president = player;
                    break;
                case VicePresident:
                    vicePresident = player;
                    break;
                case Dirt:
                    dirt = player;
                    break;
                case LowestDirt:
                    lowestDirt = player;
                    break;
            }
        }
        
        boolean presidentExchange = (president != null) && (lowestDirt != null);
        boolean viceExchange = (vicePresident != null) && (dirt != null);
    
        if (presidentExchange) {
            // President and lowest dirt exchange 2 cards
            effectuateCardExchange(president, lowestDirt, 2);
        }
        if (viceExchange) {
            // Vice president and dirt exchange 1 card
            effectuateCardExchange(vicePresident, dirt, 1);
        }
        
    }
    
    /**
     * Effectuates the final card exchange.
     * @param stronger The receiver of the cards.
     * @param weaker The sender of the cards.
     * @param nbCards The amount of cards that will be sent.
     */
    private void effectuateCardExchange(Player stronger, Player weaker, Integer nbCards) {
        weaker.sendBestCardsToPlayer(stronger, nbCards);
        stronger.sendChosenCardsToPlayer(weaker, nbCards);
    }
    
    
    private void defineNewTitles() {
        
        for (Player player : this.playerList) {
            if (player.getGameRank() == null || player.getGameRank() == 4) {
                // If Java sees the null condition first, he won't evaluate
                // the next condition. This allows the program to not crash.
                player.setGameRank(nbPlayers);
                player.setTitle(Title.LowestDirt);
            }
            else if (player.getGameRank() == 1) {
                player.setTitle(Title.President);
            }
            else if (player.getGameRank() == 2 && nbPlayers >= 4) {
                player.setTitle(Title.VicePresident);
            }
            else if (player.getGameRank() == nbPlayers-1 && nbPlayers >= 4) {
                player.setTitle(Title.Dirt);
            }
            else {
                player.setTitle(Title.Neutral);
            }
        }
        
    }
    
    
    private void displayResults() {
        write("");
        for (Player player : playerList) {
            write(". "+player+" raced rank "+player.getGameRank()+".");
        }
        write("");
    }
    
    
    private void addPoints() {
        Integer worstRank = this.nbPlayers;
        for (Player player : this.playerList) {
            player.updatePoints(playerList);
        }
        for (Player player : this.playerList) {
            player.getScore().updateElo();
        }
    }
    
    
    public void displayPoints() {
        write("");
        double elosum = 0.0;
        for (Player player : playerList) {
            //write(". "+player+" got "+player.getScore().getPreciseElo()+" elo.");
            elosum += player.getScore().getPreciseElo();
        }
        
        
        elosum /= this.nbPlayers;
        
        
        write("elosum:"+elosum);
        write("");
        for (Player player : playerList) {
            //write(". "+player+" got "+player.getScore().getNormal()+" points.");
            write(". "+player+" got "+player.getScore().getElo()+" elo.");
        }
        
    }
    
    
    // --------------
    
    
    /**
     * Simulates a player turn.
     */
    private void turn() {

        updateCurrentPlayer();
 
        do {
            replay = false;
            
            // Chooses cards to play
            PlayedCards playedCards = this.currentPlayer.chooseCardsToPlay();
            
            try {
                playPlayerCards(playedCards);
                
                for (Player player : this.playerList) {
                    player.setTurnPassed(false);
                }
                
                write(this.currentPlayer+" played "+playedCards+".");
                specialCardProperties(playedCards);
                
                
            } catch (InvalidCardException e) {
                if (!this.shutUp) {
                    this.currentPlayer.setTurnPassed(true);
                    write(this.currentPlayer+" passed his turn.");
                }
                else {
                    write(this.currentPlayer+" got shut up.");
                }
                
                this.shutUp = false;
                replay = false;
                
            } finally {
                
                if (allTurnsPassed()) {
                    resetPile(false);
                }
            }
            
        } while (replay && this.currentPlayer.hasCards());
        
        if (!this.currentPlayer.hasCards()) {
            nbFinishedPlayers++;
            this.currentPlayer.setGameRank(nbFinishedPlayers);
            write(this.currentPlayer+" finished at rank "+this.currentPlayer.getGameRank()+".");
        }
        
    }
    
    
    /**
     * Updates the player who will effectuate his turn.
     */
    private void updateCurrentPlayer() {
        do {
            this.currentPlayer = this.playerList.get(this.currentIndex);
            this.currentIndex++;
            if (this.currentIndex >= this.playerList.size()) {
                this.currentIndex = 0;
            }
        } while(!this.currentPlayer.hasCards() && !this.currentPlayer.isTurnPassed());
    }
    
    
    private void playPlayerCards(PlayedCards playedCards) throws InvalidCardException {
        if ((playedCards != null && playedCards.getCount() > 0) && (!shutUp || playedCards.getValue().equals(this.cardPile.getValue()))) {
            this.cardPile.addCard(playedCards);
            this.currentPlayer.removeCards(playedCards);
        }
        else {
            throw new InvalidCardException();
        }
}
    
    
    private void specialCardProperties(PlayedCards playedCards) {
        boolean resetPile = playedCards.getName().equals("2")
                            || cardPile.getStreakValue() >= 4;

        if (resetPile) {
            resetPile(true);
        }
        else if (cardPile.getStreakValue() >= 2 && cardPile.getCount() == 1) {
            this.shutUp = true;
        }
    }
    
    
    private void resetPile(boolean _replay) {
        write("Pile reset.\n");
        this.cardPile.clear();
        replay = _replay;
        this.shutUp = false;
        for (Player player : this.playerList) {
            player.setTurnPassed(false);
        }
    }
    
    
    private void write(String string) {
        if (this.textDisplay) {
            System.out.println(string);
        }
    }
    
    // --------------
    
    
    private boolean allTurnsPassed() {
        Integer nbPassedTurns = 0;
        for (Player player : this.playerList) {
            if (player.isTurnPassed() || !player.hasCards()) {
                nbPassedTurns++;
            }
        }
        
        return nbPassedTurns >= this.nbPlayers-1;
    }
    
    
    /**
     * Tells if the game is still going.
     * @return True if the game continues. 
     */
    private boolean isPlaying() {
        int nbPlayersPlaying = 0;
        
        for (Player player : this.playerList) {
            if (player.hasCards()) {
                nbPlayersPlaying++;
            } 
        }
        
        return (nbPlayersPlaying > 1);
    }

    
    public boolean isShutUp() {
        return shutUp;
    }

    
    public Restricter getRestricter() {
        return restricter;
    }

    
    public boolean isTextDisplay() {
        return textDisplay;
    }

    
    public void setTextDisplay(boolean textDisplay) {
        this.textDisplay = textDisplay;
    }
    
    
    
    
}
