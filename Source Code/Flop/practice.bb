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

    field buttons.FlopButton
    field hotButton.FlopButton
    field activeButton.FlopButton

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
    m\g = 30
    m\b = 30

    m\headerRect\x = m\rect\x
    m\headerRect\y = m\rect\y
    m\headerRect\width = m\rect\width
    m\headerRect\height = m\rect\height / 10
    m\headerText = "Here is the header text yippee!"

    Local buttonWidth% = m\rect\width / 4
    Local buttonHeight% = m\rect\height / 10

    Local startY% = m\rect\y + m\headerRect\height

    m\buttons = CreateFlopButton("Amongus", m\rect\x, startY, buttonWidth, buttonHeight)
    m\buttons\xOffset = m\buttons\rect\x - m\rect\x
    m\buttons\yOffset = m\buttons\rect\y - m\rect\y

    m\hotButton = null
    m\activeButton = null

    return m
end function

Function DeletePracticeMenu(m.PracticeMenu)
    delete m\rect
    delete m\headerRect
    For m\buttons = Each FlopButton
        delete m\buttons\rect
        delete m\buttons
    Next    
    delete m
end function

Function UpdatePracticeMenu(m.PracticeMenu)
    m\menuIsHot = MouseInRect(m\rect)
    m\headerIsHot = MouseInRect(m\headerRect)

    ;REMOVE THIS
    ; If m\menuIsHot Then
    ;     m\r = 60
    ;     m\g = 60
    ;     m\b = 60
    ; Else
    ;     m\r = 30
    ;     m\g = 30
    ;     m\b = 30
    ; EndIf    

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

        ;Fix button position when dragging
        For m\buttons = each FlopButton
            m\buttons\rect\x = m\rect\x + m\buttons\xOffset
            m\buttons\rect\y = m\rect\y + m\buttons\yOffset
        Next


    elseif m\headerIsActive and (Not MouseDown1) Then
        ;Released header    
        m\headerIsActive = false        
    endif

    UpdateButtons(m)

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

    ;Buttons
    DrawFlopButtons(m)

    ;Reset back to white
    Color 255, 255, 255
End Function

Function DrawFlopButtons(m.PracticeMenu)
    For m\buttons = each FlopButton
        Color(m\buttons\r, m\buttons\g, m\buttons\b)
        Local bx% = m\rect\x + m\buttons\xOffset
        Local by% = m\rect\y + m\buttons\yOffset

        Local hot% = false
        if m\buttons = m\hotButton then 
            hot = true 
        endif    
        Rect(bx, by, m\buttons\rect\width, m\buttons\rect\height, hot)

        Local bmx% = m\buttons\rect\x + (m\buttons\rect\width / 2)
        Local bmy% = m\buttons\rect\y + (m\buttons\rect\height / 2)
        Color(255, 255, 255)
        AASetFont(FontMonoSmall)        
        AAText(bmx, bmy, m\buttons\buttonText, true, true)
    Next
End Function

Function UpdateButtons(m.PracticeMenu)

    m\hotButton = null
    m\activeButton = null

    for m\buttons = Each FlopButton

        if MouseInRect(m\buttons\rect) Then
            m\hotButton = m\buttons
        endif

    next

End Function