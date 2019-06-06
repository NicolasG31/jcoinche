package JCoinche.GameEngine;

import JCoinche.protobuf.Proto;

public class            Deal
{
    public Proto.color  _dealColor;
    public int          _dealCnt;

    public Deal(Proto.color color, int count) {
        _dealColor = color;
        _dealCnt = count;
    }
}
