Type Rectangle
    field x%
    field y%
    field width%
    field height%
End type

Function MouseInRect%(r.Rectangle)

    Local mx% = MouseX()
    local my% = MouseY()

    If mx < r\x Then Return False
    If mx > r\x + r\width Then Return False

    If my < r\y Then Return False
    If my > r\y + r\height Then Return false

    Return True
End Function