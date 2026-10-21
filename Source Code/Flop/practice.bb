Include "Source Code/Flop/flopFunctions.bb"

Type PracticeMenu

    field rect.Rectangle
    field storedX%
    field storedY%

    field r%
    field g%
    field b%

    field headerRect.Rectangle
    field headerText$
    field headerR%
    field headerG%
    field headerB%
    field headerIsHot% ;Hovering
    field headerIsActive ;Clicking on header
    field headerMX%
    field headerMY%

    field menuIsHot = false

end type

Function CreatePracticeMenu.PracticeMenu(x%, y%, width%, height%)
    Local m.PracticeMenu = new PracticeMenu
    m\rect = new Rectangle
    m\headerRect = new Rectangle

    m\rect\x = x
    m\rect\y = y
    m\rect\width = width
    m\rect\height = height

    m\r = 30
    m\g = 100
    m\b = 90

    m\headerRect\x = m\rect\x
    m\headerRect\y = m\rect\y
    m\headerRect\width = m\rect\width
    m\headerRect\height = m\rect\height / 10
    m\headerText = "Here is the header text yippee!"

    return m
end function

Function DeletePracticeMenu(m.PracticeMenu)
    delete m\rect
    delete m\headerRect
    delete m
end function

Function UpdatePracticeMenu(m.PracticeMenu)
    m\menuIsHot = MouseInRect(m\rect)
    m\headerIsHot = MouseInRect(m\headerRect)

    ;REMOVE THIS
    If m\menuIsHot Then
        m\r = 255    
    Else
        m\r = 30
    EndIf    

    If m\headerIsHot and MouseHit1 Then
        ;Click on header
        m\headerIsActive = true
        m\headerMX = MouseX()
        m\headerMY = MouseY()    
        m\storedX = m\rect\x
        m\storedY = m\rect\y
    else if m\headerIsActive and MouseDown1 then
        ;Dragging header around
        Local mx% = MouseX()
        Local my% = MouseY()

        ;Distance mouse moved since first click
        Local mdx% = mx - m\headerMX
        Local mdy% = my - m\headerMY

        m\rect\x = m\storedX + mdx
        m\rect\y = m\storedY + mdy
        m\headerRect\x = m\rect\x
        m\headerRect\y = m\rect\y
    elseif m\headerIsActive and (Not MouseDown1) Then
        ;Released header    
        m\headerIsActive = false
    endif

End Function

Function DrawPracticeMenu(m.PracticeMenu)

    ;Menu base rectangle
    Color(m\r, m\g, m\b)
    Rect(m\rect\x, m\rect\y, m\rect\width, m\rect\height)

    ;Header outlined rectangle
    Color(255, 0, 0)
    Rect(m\headerRect\x, m\headerRect\y, m\headerRect\width, m\headerRect\height, m\headerIsHot)

    ;Header Text
    AASetFont(FontMonoSmall)
    AAText(m\headerRect\x + (m\headerRect\width/2), m\headerRect\y + (m\headerRect\height / 2), m\headerText, true, true)

    ;Reset back to white
    Color 255, 255, 255
End Function
