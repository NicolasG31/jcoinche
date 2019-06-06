package JCoinche.GameEngine;

public class        Card
{
    public enum Color
    {
        PIQUE,
        COEUR,
        CARREAU,
        TREFLE
    }

    public enum Value
    {
        SEPT,
        HUIT,
        NEUF,
        DIX,
        DAME,
        VALET,
        ROI,
        AS
    }

    public Color    _Color;
    public Value    _Value;

    public Card(Color ncolor, Value nvalue)
    {
        this._Color = ncolor;
        this._Value = nvalue;
    }

}
