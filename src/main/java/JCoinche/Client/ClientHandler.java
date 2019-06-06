package JCoinche.Client;

import JCoinche.GameEngine.Card;
import JCoinche.GameEngine.Deal;
import JCoinche.protobuf.Proto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler extends SimpleChannelInboundHandler<Proto.CardResponse> {

    private Channel channel;
    BlockingQueue<Proto.CardResponse> resps = new LinkedBlockingQueue<Proto.CardResponse>();

    public void sendName(String name) {
        Proto.CardRequest.Builder builder = Proto.CardRequest.newBuilder()
                .setRequestMsg(Proto.CardRequest.Request.SEND_NAME)
                .setPlayer(name);

        channel.writeAndFlush(builder);
    }

    public void sendDeal(Proto.Deal deal) {
        Proto.CardRequest.Builder builder = Proto.CardRequest.newBuilder()
                .setRequestMsg(Proto.CardRequest.Request.SEND_DEAL)
                .setDeal(Proto.Deal.newBuilder().
                        setCount(deal.getCount()).setColor(deal.getColor()).build());

        channel.writeAndFlush(builder);
    }

    public void sendCardPlay(Proto.Card card) {
        Proto.CardRequest.Builder builder = Proto.CardRequest.newBuilder()
                .setRequestMsg(Proto.CardRequest.Request.SEND_CARD_PLAY)
                .setCard(Proto.Card.newBuilder().
                        setValue(card.getValue()).setColor(card.getColor()).build());

        channel.writeAndFlush(builder);
    }

    public void watchCards() {
        Proto.CardRequest.Builder builder = Proto.CardRequest.newBuilder()
                .setRequestMsg(Proto.CardRequest.Request.WATCH_CARDS);

        channel.writeAndFlush(builder);
    }

    public void watchHand() {
        Proto.CardRequest.Builder builder = Proto.CardRequest.newBuilder()
                .setRequestMsg(Proto.CardRequest.Request.WATCH_HAND);

        channel.writeAndFlush(builder);
    }

    public void watchPlayers() {
        Proto.CardRequest.Builder builder = Proto.CardRequest.newBuilder()
                .setRequestMsg(Proto.CardRequest.Request.WATCH_PLAYERS);

        channel.writeAndFlush(builder);
    }


    public Proto.color   getColorFromStr(String str)
    {
        //System.out.println("str : '" + str + "'");
        if (str.equalsIgnoreCase("PIQUE"))
            return Proto.color.PIQUE;
        if (str.equalsIgnoreCase("COEUR"))
            return Proto.color.COEUR;
        if (str.equalsIgnoreCase("CARREAU"))
            return Proto.color.CARREAU;
        if (str.equalsIgnoreCase("TREFLE"))
            return Proto.color.TREFLE;
        return null;
    }

    public Card.Color   getCColorFromStr(String str)
    {
        //System.out.println("str : '" + str + "'");
        if (str.equalsIgnoreCase("PIQUE"))
            return Card.Color.PIQUE;
        if (str.equalsIgnoreCase("COEUR"))
            return Card.Color.COEUR;
        if (str.equalsIgnoreCase("CARREAU"))
            return Card.Color.CARREAU;
        if (str.equalsIgnoreCase("TREFLE"))
            return Card.Color.TREFLE;
        return null;
    }

    public Card.Value   getValueFromString(String str)
    {
        if (str.equalsIgnoreCase("SEPT"))
            return Card.Value.SEPT;
        if (str.equalsIgnoreCase("HUIT"))
            return Card.Value.HUIT;
        if (str.equalsIgnoreCase("NEUF"))
            return Card.Value.NEUF;
        if (str.equalsIgnoreCase( "DIX"))
            return Card.Value.DIX;
        if (str.equalsIgnoreCase( "DAME"))
            return Card.Value.DAME;
        if (str.equalsIgnoreCase("VALET"))
            return Card.Value.VALET;
        if (str.equalsIgnoreCase("ROI"))
            return Card.Value.ROI;
        if (str.equalsIgnoreCase("AS"))
            return Card.Value.AS;
        return null;
    }

    public Proto.Card.value   getPValueFromString(String str)
    {
        if (str.equalsIgnoreCase("SEPT"))
            return Proto.Card.value.SEPT;
        if (str.equalsIgnoreCase("HUIT"))
            return Proto.Card.value.HUIT;
        if (str.equalsIgnoreCase("NEUF"))
            return Proto.Card.value.NEUF;
        if (str.equalsIgnoreCase("DIX"))
            return Proto.Card.value.DIX;
        if (str.equalsIgnoreCase("DAME"))
            return Proto.Card.value.DAME;
        if (str.equalsIgnoreCase("VALET"))
            return Proto.Card.value.VALET;
        if (str.equalsIgnoreCase("ROI"))
            return Proto.Card.value.ROI;
        if (str.equalsIgnoreCase("AS"))
            return Proto.Card.value.AS;
        return null;
    }

    public boolean isNumeric(String s)
    {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    public void myTurnCard()
    {
        System.out.println("C'est votre tour de jouer une carte: '[COULEUR : CARREAU, PIQUE, COEUR, TREFLE] [VALEUR : SEPT, HUIT, NEUF, DIX, DAME, VALET, ROI, AS]'");
        Scanner scan = new Scanner(System.in);
        String buff = scan.nextLine();

        List<String> items = Arrays.asList(buff.split(" "));
        if (items.size() != 2)
        {
            System.out.println("turn again 1");
            myTurnCard();
            return;
        }
        if (buff.equalsIgnoreCase("SHOW PLAYERS"))
        {
            System.out.println("ask a watch players");
            watchPlayers();
            return;
        }
        if (buff.equalsIgnoreCase("SHOW HAND"))
        {
            System.out.println("ask a watch hand");
            watchHand();
            return;
        }
        if (buff.equalsIgnoreCase("SHOW CARDS"))
        {
            System.out.println("ask a watch cards");
            watchCards();
            return;
        }
        if (getColorFromStr(items.get(0)) == null || getValueFromString(items.get(1)) == null)
        {
            System.out.println("turn again 5");
            myTurnCard();
            return;
        }
        Proto.Card nCard =  Proto.Card.newBuilder()
                .setColor(getColorFromStr(items.get(0)))
                .setValue(getPValueFromString(items.get(1))).build();
        System.out.println("sends card");
        sendCardPlay(nCard);
    }

    public void myTurnDeal()
    {
        System.out.println("C'est votre tour d'entrer un contrat: '[COULEUR : CARREAU, PIQUE, COEUR, TREFLE] [VALEUR : 80 - 160]'");
        Scanner scan = new Scanner(System.in);
        String buff = scan.nextLine();

        List<String> items = Arrays.asList(buff.split(" "));
        if (buff.equalsIgnoreCase("PASSE"))
        {
            sendDeal(Proto.Deal.newBuilder()
                    .setCount(0)
                    .setColor(Proto.color.PIQUE).build());
            return;
        }
        if (items.size() != 2)//|| !isNumeric(items.get(1)))
        {
            System.out.println("turn again 1");
            myTurnDeal();
            return;
        }
        /*if (buff.equalsIgnoreCase("SHOW PLAYERS"))
        {
            System.out.println("ask a watch players");
            watchPlayers();
            myTurnDeal();
            return;
        }*/
        if (getColorFromStr(items.get(0)) == null)
        {
            System.out.println("turn again 3");
            myTurnDeal();
            return;
        }
        Proto.Deal nDeal = Proto.Deal.newBuilder()
                            .setCount(Integer.parseInt(items.get(1)))
                            .setColor(getColorFromStr(items.get(0))).build();
        System.out.println("sends deal");
        sendDeal(nDeal);
    }

    public void myShowCards(Proto.CardResponse resp)
    {
        int i = 0;
        List<Proto.Card> Cards = resp.getCardList();
        System.out.println("Les cartes sur la table sont :");
        while (i != Cards.size())
        {
            System.out.println("Carte " + i + " : " + Cards.get(i).getColor() + " " + Cards.get(i).getValue());
            i++;
        }
        myTurnCard();
    }

    public void myShowHand(Proto.CardResponse resp)
    {
        int i = 0;
        List<Proto.Card> Cards = resp.getCardList();
        System.out.println("Les cartes dans votre main sont :");
        while (i != Cards.size())
        {
            System.out.println("Carte " + i + " : " + Cards.get(i).getColor() + " " + Cards.get(i).getValue());
            i++;
        }
        myTurnCard();
    }

    public void myShowPlayers(Proto.CardResponse resp)
    {
        int i = 0;
        System.out.println("Les joueurs connectés sont :");
        while (i != resp.getPlayersList().size())
        {
            System.out.println("Joueur " + i + " : " + resp.getPlayersList().get(i));
            i++;
        }
        myTurnCard();
    }

    public void myTurnOver(Proto.CardResponse resp)
    {
        System.out.println(resp.getPlayersList().get(0) + " a gagné le tour.\n Score: Team 1: " + resp.getScore(0) + " Team 2: " + resp.getScore(1));
    }

    public void myDealDone(Proto.CardResponse resp)
    {
        if (resp.getDeal().getCount() == 0)
            System.out.println(resp.getPlayersList().get(0) + " a passé");
        else
            System.out.println(resp.getPlayersList().get(0) + " a annoncé " + resp.getDeal().getColor() + " " + resp.getDeal().getCount());
    }

    public void myCardPlayed(Proto.CardResponse resp)
    {
        System.out.println(resp.getPlayersList().get(0) + " a joué " + resp.getCard(0).getValue() + " " + resp.getCard(0).getColor());
    }

    public void myGameOver(Proto.CardResponse resp)
    {
        System.out.println("L'équipe " + resp.getWinner() + " a remporté la partie" );
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Proto.CardResponse cardResponse) throws Exception {
        //resps.add(cardResponse);
        switch (cardResponse.getResponseMsg()) {
            case NO_RESPONSE:
                break;
            case WELCOME:
                System.out.println("Bienvenue sur le serveur de jeu JCoinche");
                break;
            case PLAYER_JOINED:
                System.out.println(cardResponse.getPlayersList().get(0) + " a rejoint la partie");
                break;
            case GAME_STARTED:
                System.out.println("4 joueurs sont connectés, la partie commence.\nPhase de contrats.");
                break;
            case YOUR_TURN_DEAL:
                myTurnDeal();
                break;
            case YOUR_TURN_CARD:
                myTurnCard();
                break;
            case DEAL_DONE:
                myDealDone(cardResponse);
                break;
            case CARD_PLAYED:
                myCardPlayed(cardResponse);
                break;
            case PLAY_CARDS_STARTED:
                System.out.println("Les contrats sont terminés");
                break;
            case TURN_OVER:
                myTurnOver(cardResponse);
                break;
            case GAME_OVER:
                myGameOver(cardResponse);
                break;
            case SHOW_CARDS:
                myShowCards(cardResponse);
                break;
            case SHOW_HAND:
                myShowHand(cardResponse);
                break;
            case SHOW_PLAYERS:
                myShowPlayers(cardResponse);
                break;
            case WRONG_DEAL:
                System.out.println("Vous ne pouvez pas annoncer ce contrat");
                myTurnDeal();
                break;
            case WRONG_CARD:
                System.out.println("Vous ne pouvez pas jouer cette carte");
                myTurnCard();
                break;
            case WRONG_PHASE:
                System.out.println("Ce n'est pas votre tour de jouer");
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
