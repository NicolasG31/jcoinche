package JCoinche.GameEngine;

import JCoinche.protobuf.Proto;
import io.netty.channel.ChannelId;

import java.net.SocketAddress;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.List;

public class Player
{
    public int                  _Id;
    public ChannelId            _ChannelId;
    public SocketAddress        _Ip;
    public String               _Name;
    public int                  _TeamId;
    public int                  _Points;
    public List<Proto.Card>     _Cards;

    public Player(String name, int id, ChannelId channelId, SocketAddress ip) {
        _Id = id;
        _ChannelId = channelId;
        _Ip = ip;
        _Name = name;
        _TeamId = id % 2;
        _Points = 0;
        _Cards = new ArrayList<Proto.Card>();
    }

    public void addCard(Proto.Card card) {
        _Cards.add(card);
    }
}
