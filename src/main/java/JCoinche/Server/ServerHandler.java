package JCoinche.Server;

import JCoinche.GameEngine.Deal;
import JCoinche.GameEngine.Player;
import JCoinche.GameEngine.Team;
import JCoinche.protobuf.Proto;
import JCoinche.protobuf.Proto.Card.value;
import JCoinche.protobuf.Proto.color;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ServerHandler extends SimpleChannelInboundHandler<Proto.CardRequest> { // (1)

    private static boolean gameStarted = false;
    private static color _Asset = null;
    private static List<Player> _Players = new ArrayList<>();
    private static Team[] _Teams = null;
    private static List<Proto.Card> _Cards = new ArrayList<>();
    private static int _Turn = 0;
    private static int _PlayerTurn = 0;
    private static int _DealTurn = 0;
    private static int _DealPass = 0;

    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private int returnPoints(Proto.Card cCard, color Asset) {
        // Vérification par rapport à la 1ère carte de la pile
        if (_Cards.size() != 0) {
            if ((cCard.getColor() != Asset) && (cCard.getColor() != _Cards.get(0).getColor()))
                return 0;
        }
        // SANS ATOUT
        if (cCard.getColor() != Asset) {
            if (cCard.getValue() == value.VALET)
                return 2;
            if (cCard.getValue() == value.NEUF)
                return 0;
            if (cCard.getValue() == value.AS)
                return 11;
            if (cCard.getValue() == value.DIX)
                return 10;
            if (cCard.getValue() == value.ROI)
                return 4;
            if (cCard.getValue() == value.DAME)
                return 3;
            if (cCard.getValue() == value.HUIT)
                return 0;
            if (cCard.getValue() == value.SEPT)
                return 0;
            return 0;
        } else {
            // AVEC ATOUT
            if (cCard.getValue() == value.VALET)
                return 20;
            if (cCard.getValue() == value.NEUF)
                return 14;
            if (cCard.getValue() == value.AS)
                return 11;
            if (cCard.getValue() == value.DIX)
                return 10;
            if (cCard.getValue() == value.ROI)
                return 4;
            if (cCard.getValue() == value.DAME)
                return 3;
            if (cCard.getValue() == value.HUIT)
                return 0;
            if (cCard.getValue() == value.SEPT)
                return 0;
            return 0;
        }
    }

    private void initializeCards() {
        _Cards.add(Proto.Card.newBuilder().setColor(color.CARREAU).setValue(value.AS).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.CARREAU).setValue(value.DAME).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.CARREAU).setValue(value.DIX).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.CARREAU).setValue(value.HUIT).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.CARREAU).setValue(value.NEUF).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.CARREAU).setValue(value.ROI).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.CARREAU).setValue(value.SEPT).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.CARREAU).setValue(value.VALET).build());

        _Cards.add(Proto.Card.newBuilder().setColor(color.PIQUE).setValue(value.AS).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.PIQUE).setValue(value.DAME).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.PIQUE).setValue(value.DIX).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.PIQUE).setValue(value.HUIT).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.PIQUE).setValue(value.NEUF).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.PIQUE).setValue(value.ROI).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.PIQUE).setValue(value.SEPT).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.PIQUE).setValue(value.VALET).build());

        _Cards.add(Proto.Card.newBuilder().setColor(color.TREFLE).setValue(value.AS).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.TREFLE).setValue(value.DAME).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.TREFLE).setValue(value.DIX).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.TREFLE).setValue(value.HUIT).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.TREFLE).setValue(value.NEUF).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.TREFLE).setValue(value.ROI).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.TREFLE).setValue(value.SEPT).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.TREFLE).setValue(value.VALET).build());

        _Cards.add(Proto.Card.newBuilder().setColor(color.COEUR).setValue(value.AS).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.COEUR).setValue(value.DAME).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.COEUR).setValue(value.DIX).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.COEUR).setValue(value.HUIT).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.COEUR).setValue(value.NEUF).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.COEUR).setValue(value.ROI).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.COEUR).setValue(value.SEPT).build());
        _Cards.add(Proto.Card.newBuilder().setColor(color.COEUR).setValue(value.VALET).build());
    }

    private void distributeCards() {
        int size = 31;
        int cnt;
        int pCnt = 0;
        Collections.shuffle(_Cards);
        while (pCnt != 4) {
            cnt = 0;
            while (cnt != 8) {
                _Players.get(pCnt).addCard(_Cards.get(size));
                _Cards.remove(size);
                size--;
                cnt++;
            }
            pCnt++;
        }
    }

    private boolean makeDeal(int playerId, int dCnt, color dColor) {
        // Si le nouveau contrat est > à 80 et > aux contrats déjà existants.
        System.out.println("cnt : " + dCnt + " ,color : " + dColor);
        if (dCnt == 0)
            return true;
        else if ((dCnt > _Teams[0]._Deal._dealCnt) &&
                (dCnt > _Teams[1]._Deal._dealCnt) &&
                (dCnt >= 80) && (dCnt <= 160))// && (dCnt % 10 == 0))
        {
            _Asset = dColor;
            _Teams[_Players.get(playerId)._TeamId]._Deal._dealCnt = dCnt;
            _Teams[_Players.get(playerId)._TeamId]._Deal._dealColor = dColor;
            return true;
        }
        return false;
    }

    private int highestDeal() {
        if (_Teams[0]._Deal._dealCnt > _Teams[1]._Deal._dealCnt)
            return _Teams[0]._Deal._dealCnt;
        return _Teams[1]._Deal._dealCnt;
    }

    private int dealTeamId() {
        if (_Teams[0]._Deal._dealCnt > _Teams[1]._Deal._dealCnt)
            return 0;
        return 1;
    }

    private boolean hasFirst(int pId) {
        int cnt = 0;
        int first = 0;
        int asset = 0;

        while (cnt != _Players.get(pId)._Cards.size()) {
            if (_Players.get(pId)._Cards.get(cnt).getColor() == _Asset)
                asset++;
            else if (_Players.get(pId)._Cards.get(cnt).getColor() == _Cards.get(0).getColor())
                first++;
            cnt++;
        }
        return first > 0;
    }

    private boolean playCard(int playerId, Proto.Card cCard) {

        if (_Cards.size() > 0 && hasFirst(playerId) &&
                (cCard.getColor() != _Cards.get(0).getColor()))
            return false;
        if (!_Players.get(playerId)._Cards.contains(cCard))
            return false;

        _Players.get(playerId)._Cards.remove(cCard);
        _Cards.add(cCard);
        return true;
    }

    private void replacePlayers(int winningId) {
        // Rotation des joueurs, le joueur ayant gagné la dernière pile devient le joueur 0
        List<Player> nList;

        if (winningId != 0) {
            nList = new ArrayList<>();

            nList.add(_Players.get(winningId));
            if (winningId == 1) {
                nList.add(_Players.get(2));
                nList.add(_Players.get(3));
                nList.add(_Players.get(0));
            } else if (winningId == 2) {
                nList.add(_Players.get(3));
                nList.add(_Players.get(0));
                nList.add(_Players.get(1));
            } else if (winningId == 3) {
                nList.add(_Players.get(0));
                nList.add(_Players.get(1));
                nList.add(_Players.get(2));
            }
            _Players = nList;
        }
    }

    private void countTurnScore()
    {
        int             winningPId = 0;
        int             winningPIdAsset = -1;
        int             score = 0;
        int             scoreAsset = 0;
        int             totalScore = 0;
        int             cCnt = 0;

        // Cherche la carte dans la pile qui a la plus grande valeur et retient l'Id du joueur gagnant de la pile
        while (cCnt != 3)
        {
            if (returnPoints(_Cards.get(cCnt), _Asset) > score)
            {
                score = returnPoints(_Cards.get(cCnt), _Asset);
                totalScore += score;
                winningPId = cCnt;
            }
            if (_Cards.get(cCnt).getColor() == _Asset &&
                    returnPoints(_Cards.get(cCnt), _Asset) > scoreAsset)
            {
                scoreAsset = returnPoints(_Cards.get(cCnt), _Asset);
                winningPIdAsset = cCnt;
            }
            cCnt++;
        }
        if (winningPIdAsset != -1)
            winningPId = winningPIdAsset;
        // Ajoute le score au joueur gagnant et à son équipe
        _Players.get(winningPId)._Points += totalScore;
        _Teams[_Players.get(winningPId)._TeamId]._Points += totalScore;
        // Clear la liste de la pile
        _Cards.clear();
        replacePlayers(winningPId);
    }

    private void countFinalScore() {
        int id = dealTeamId();

        if (_Teams[id]._Points >= _Teams[id]._Deal._dealCnt) {
            _Teams[id]._Points += _Teams[id]._Deal._dealCnt;
        } else {
            _Teams[id]._Points = 0;
            if (id == 0)
                _Teams[1]._Points = 160 + _Teams[0]._Deal._dealCnt;
            else
                _Teams[0]._Points = 160 + _Teams[1]._Deal._dealCnt;
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
        if (gameStarted) {
            Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder().
                    setResponseMsg(Proto.CardResponse.response.GAME_OVER);
            for (Channel channel : channels) {
                channel.writeAndFlush(builder);
                channels.remove(channel);
            }
        }
    }

    private void sendName(ChannelHandlerContext ctx, Proto.CardRequest req) {
        Channel incoming = ctx.channel();

        if (_Players.size() < 4) {
            _Players.add(new Player(req.getPlayer(), _Players.size(), incoming.id(), incoming.remoteAddress()));
            for (Channel channel : channels) {
                Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder();
                if (channel != incoming) {
                    builder.setResponseMsg(Proto.CardResponse.response.PLAYER_JOINED)
                            .addPlayers(req.getPlayer());
                } else {
                    builder.setResponseMsg(Proto.CardResponse.response.WELCOME);
                }
                channel.writeAndFlush(builder.build());
            }
        } else {
            Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder();
            builder.setResponseMsg(Proto.CardResponse.response.NO_RESPONSE);
            incoming.writeAndFlush(builder);
        }
        if (_Players.size() == 4) {
            gameStarted = true;
            for (Channel channel : channels) {
                Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder()
                        .setResponseMsg(Proto.CardResponse.response.GAME_STARTED);
                channel.writeAndFlush(builder);
            }
            initializeCards();
            distributeCards();
            _Teams = new Team[2];
            _Teams[0] = new Team(0);
            _Teams[1] = new Team(1);
            Channel channel = channels.find(_Players.get(_DealTurn)._ChannelId);
            Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder()
                    .setResponseMsg(Proto.CardResponse.response.YOUR_TURN_DEAL);
            channel.writeAndFlush(builder);
        }
    }

    private void sendDeal(ChannelHandlerContext ctx, Proto.CardRequest req) {
        boolean dealFinished = false;
        boolean dealPlayed = false;
        boolean ifDeal;

        Channel incoming = ctx.channel();
        Deal deal = new Deal(req.getDeal().getColor(), req.getDeal().getCount());
        if (req.getDeal().getCount() == 0)
            _DealPass += 1;
        else
            _DealPass = 0;
        ifDeal = makeDeal(_Players.get(_DealTurn)._Id, deal._dealCnt, deal._dealColor);
        Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder();
        if (ifDeal && incoming == channels.find(_Players.get(_DealTurn)._ChannelId)) {
            for (Channel channel : channels) {
                Proto.CardResponse.Builder dealDone = Proto.CardResponse.newBuilder()
                        .setResponseMsg(Proto.CardResponse.response.DEAL_DONE)
                        .addPlayers(_Players.get(_DealTurn)._Name)
                        .setDeal(Proto.Deal.newBuilder().setColor(deal._dealColor).setCount(deal._dealCnt).build())
                        .setWinner(_Players.get(_DealTurn)._Id);
                channel.writeAndFlush(dealDone);
            }
            dealPlayed = true;
            _DealTurn += 1;
            _DealTurn = _DealTurn % 4;
            if (_DealPass == 3 && (_Teams[0]._Deal._dealCnt > 0 || _Teams[1]._Deal._dealCnt > 0)) {
                dealFinished = true;
            } else if (_DealPass == 4) {
                for (Player _Player : _Players) {
                    _Player._Cards.clear();
                }
                initializeCards();
                distributeCards();
            }

        } else if (incoming != channels.find(_Players.get(_DealTurn)._ChannelId)) {
            builder = Proto.CardResponse.newBuilder().setResponseMsg(Proto.CardResponse.response.WRONG_PHASE);
            incoming.writeAndFlush(builder);
        } else {
            builder = Proto.CardResponse.newBuilder().setResponseMsg(Proto.CardResponse.response.WRONG_DEAL);
            incoming.writeAndFlush(builder);
        }

        builder.clear();

        if (dealPlayed && !dealFinished) {
            Channel channel = channels.find(_Players.get(_DealTurn)._ChannelId);
            builder = Proto.CardResponse.newBuilder()
                    .setResponseMsg(Proto.CardResponse.response.YOUR_TURN_DEAL);
            channel.writeAndFlush(builder);
        } else if (dealFinished) {
            for (Channel channel : channels) {
                Proto.CardResponse.Builder cardStart = Proto.CardResponse.newBuilder()
                        .setResponseMsg(Proto.CardResponse.response.PLAY_CARDS_STARTED);
                channel.writeAndFlush(cardStart);
            }
            Channel channel = channels.find(_Players.get(0)._ChannelId);
            builder = Proto.CardResponse.newBuilder()
                    .setResponseMsg(Proto.CardResponse.response.YOUR_TURN_CARD);
            channel.writeAndFlush(builder);
        }
    }

    private void sendCardPlay(ChannelHandlerContext ctx, Proto.CardRequest req) {
        Channel incoming = ctx.channel();

        Proto.Card card = Proto.Card.newBuilder()
                .setColor(req.getCard().getColor())
                .setValue(req.getCard().getValue()).build();

        boolean played = false;
        boolean ifCard;
        int position = 0;

        for (int i = 0; i < _Players.size(); i++) {
            if (incoming.id() == _Players.get(i)._ChannelId) {
                position = i;
            }
        }
        ifCard = playCard(position, card);

        Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder();
        if (ifCard && _PlayerTurn < _Players.size() && incoming == channels.find(_Players.get(_PlayerTurn)._ChannelId)) {
            for (Channel channel : channels) {
                builder = Proto.CardResponse.newBuilder().setResponseMsg(Proto.CardResponse.response.CARD_PLAYED)
                        .addPlayers(_Players.get(_PlayerTurn)._Name)
                        .addCard(Proto.Card.newBuilder().setColor(card.getColor()).setValue(card.getValue()).build())
                        .setWinner(_Players.get(_PlayerTurn)._Id);
                channel.writeAndFlush(builder);
            }
            _PlayerTurn++;
            played = true;
        } else if (_PlayerTurn < _Players.size() && incoming != channels.find(_Players.get(_PlayerTurn)._ChannelId)) {
            builder.setResponseMsg(Proto.CardResponse.response.WRONG_PHASE);
            incoming.writeAndFlush(builder);
        } else {
            builder.setResponseMsg(Proto.CardResponse.response.WRONG_CARD);
            incoming.writeAndFlush(builder);
        }

        builder.clear();
        builder = Proto.CardResponse.newBuilder();

        // Si des joueurs n'ont pas encore joué
        if (played && _PlayerTurn < _Players.size()) {
            Channel channel = channels.find(_Players.get(_PlayerTurn)._ChannelId);
            builder.setResponseMsg(Proto.CardResponse.response.YOUR_TURN_CARD);
            channel.writeAndFlush(builder);
        }
        // Si tout les joueurs ont joué pendant ce tour
        else if (played && _PlayerTurn == _Players.size()) {
            countTurnScore();
            for (Channel channel : channels) {
                Proto.CardResponse.Builder turnOver = Proto.CardResponse.newBuilder()
                        .setResponseMsg(Proto.CardResponse.response.TURN_OVER)
                        .addPlayers(_Players.get(0)._Name)
                        .addScore(_Teams[0]._Points)
                        .addScore(_Teams[1]._Points);
                channel.writeAndFlush(turnOver);
            }
            _PlayerTurn = 0;
            _Turn += 1;


            if (_Turn == 8) {
                countFinalScore();
                int score;
                if (_Teams[0]._Points > _Teams[1]._Points)
                    score = 0;
                else
                    score = 1;
                builder = Proto.CardResponse.newBuilder()
                        .setResponseMsg(Proto.CardResponse.response.GAME_OVER)
                        .setWinner(score);
                for (Channel channel : channels) {
                    channel.writeAndFlush(builder);
                }
            }
            else {
                Proto.CardResponse.Builder newTurn = Proto.CardResponse.newBuilder()
                        .setResponseMsg(Proto.CardResponse.response.YOUR_TURN_CARD);
                if (_Players.size() > 0) {
                    Channel channel = channels.find(_Players.get(0)._ChannelId);
                    channel.writeAndFlush(newTurn);
                }
            }
        }
    }

    private void watchCards(ChannelHandlerContext ctx, Proto.CardRequest req) {
        Channel incoming = ctx.channel();
        Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder();
        builder.setResponseMsg(Proto.CardResponse.response.SHOW_CARDS);
        for (Proto.Card _Card : _Cards) {
            builder.addCard(Proto.Card.newBuilder()
                    .setColor(_Card.getColor())
                    .setValue(_Card.getValue())
                    .build());
        }
        incoming.writeAndFlush(builder);
    }

    private void watchHand(ChannelHandlerContext ctx, Proto.CardRequest req) {
        Channel incoming = ctx.channel();
        Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder();
        builder.setResponseMsg(Proto.CardResponse.response.SHOW_HAND);
        for (Player _Player : _Players) {
            if (incoming.id() == _Player._ChannelId) {
                for (int j = 0; j < _Player._Cards.size(); j++) {
                    builder.addCard(Proto.Card.newBuilder()
                            .setColor(_Player._Cards.get(j).getColor())
                            .setValue(_Player._Cards.get(j).getValue())
                            .build());
                }
            }
        }
        incoming.writeAndFlush(builder);
    }

    private void watchPlayers(ChannelHandlerContext ctx, Proto.CardRequest req) {
        Channel incoming = ctx.channel();
        Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder();
        builder.setResponseMsg(Proto.CardResponse.response.SHOW_PLAYERS);
        for (Player _Player : _Players) {
            builder.addPlayers(_Player._Name);
        }
        incoming.writeAndFlush(builder);
    }

    private void noResponse(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();

        Proto.CardResponse.Builder builder = Proto.CardResponse.newBuilder();
        builder.setResponseMsg(Proto.CardResponse.response.NO_RESPONSE);
        incoming.writeAndFlush(builder);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Proto.CardRequest req) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("[" + incoming.remoteAddress() + "] " + req.getRequestMsg());

        switch (req.getRequestMsg()) {
            case SEND_NAME:
                sendName(ctx, req);
                break;
            case SEND_DEAL:
                sendDeal(ctx, req);
                break;
            case SEND_CARD_PLAY:
                sendCardPlay(ctx, req);
                break;
            case WATCH_CARDS:
                watchCards(ctx, req);
                break;
            case WATCH_HAND:
                watchHand(ctx, req);
                break;
            case WATCH_PLAYERS:
                watchPlayers(ctx, req);
                break;
            default:
                noResponse(ctx);
        }
    }

    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}