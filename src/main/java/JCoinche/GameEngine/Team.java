package JCoinche.GameEngine;

import JCoinche.protobuf.Proto;
import com.google.protobuf.InvalidProtocolBufferException;

public class Team
{
    public int      _Id;
    public int      _Points;
    public Deal     _Deal;

    public Team(int id) {
        _Id = id;
        _Points = 0;
        _Deal = new Deal(Proto.color.PIQUE, 0);
    }
}
