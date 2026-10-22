Type Rectangle
    field x%
    field y%
    field width%
    field height%
End type

Type FlopButton
    field id%

    ;The offset is  the XY positon of the button relative to the current position
    ;that the menu rect is in. Goes from (0, 0) to (menuWidth, menuHeight)
    ;We will use it to keep track of where to draw button after menu is dragged around.
    field xOffset%
    field yOffset%
    field rect.Rectangle
    field r%
    field g%
    field b%

    field buttonText$
End Type    

Function CreateRect.Rectangle(x%, y%, width%, height%) 
    Local r.Rectangle = new Rectangle
    r\x = x
    r\y = y
    r\width = width
    r\height = height
    return r
End Function

Function CreateFlopButton.FlopButton(buttonText$, x%, y%, width%, height%, r%=0, g%=0, b%=0)
    Local f.FlopButton = new FlopButton
    f\id = FlopMillisecs()
    f\rect = CreateRect(x, y, width, height)
    f\r = r
    f\g = g
    f\b = b
    f\buttonText = buttonText

    ;MAKE IT SO MINIMUM BUTTON WIDTH IS NOT LOWER THAN STRING WIDTH

    return f
End Function

Function MouseInRect%(r.Rectangle)

    Local mx% = MouseX()
    local my% = MouseY()

    If mx < r\x Then Return False
    If mx > r\x + r\width Then Return False

    If my < r\y Then Return False
    If my > r\y + r\height Then Return false

    Return True
End Function