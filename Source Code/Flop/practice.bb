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
    Local menu.PracticeMenu = new PracticeMenu
    menu\rect = new Rectangle
    menu\headerRect = new Rectangle

    menu\rect\x = x
    menu\rect\y = y
    menu\rect\width = width
    menu\rect\height = height

    menu\r = 30
    menu\g = 100
    menu\b = 90

    menu\headerRect\x = menu\rect\x
    menu\headerRect\y = menu\rect\y
    menu\headerRect\width = menu\rect\width
    menu\headerRect\height = menu\rect\height / 10
    menu\headerText = "Here is the header text yippee!"

    return menu
end function

Function DeletePracticeMenu(menu.PracticeMenu)
    delete menu\rect
    delete menu\headerRect
    delete menu
end function

Function UpdatePracticeMenu(menu.PracticeMenu)
    menu\menuIsHot = MouseInRect(menu\rect)
    menu\headerIsHot = MouseInRect(menu\headerRect)

    ;REMOVE THIS
    If menu\menuIsHot Then
        menu\r = 255    
    Else
        menu\r = 30
    EndIf    

    If menu\headerIsHot and MouseHit1 Then
        ;Click on header
        menu\headerIsActive = true
        menu\headerMX = MouseX()
        menu\headerMY = MouseY()    
        menu\storedX = menu\rect\x
        menu\storedY = menu\rect\y
    else if menu\headerIsActive and MouseDown1 then
        ;Dragging header around
        Local mx% = MouseX()
        Local my% = MouseY()

        ;Distance mouse moved since first click
        Local mdx% = mx - menu\headerMX
        Local mdy% = my - menu\headerMY

        menu\rect\x = menu\storedX + mdx
        menu\rect\y = menu\storedY + mdy
        menu\headerRect\x = menu\rect\x
        menu\headerRect\y = menu\rect\y
    elseif menu\headerIsActive and (Not MouseDown1) Then
        ;Released header    
        menu\headerIsActive = false
    endif

End Function

Function DrawPracticeMenu(menu.PracticeMenu)

    ;Menu base rectangle
    Color(menu\r, menu\g, menu\b)
    Rect(menu\rect\x, menu\rect\y, menu\rect\width, menu\rect\height)

    ;Header outlined rectangle
    Color(255, 0, 0)
    Rect(menu\headerRect\x, menu\headerRect\y, menu\headerRect\width, menu\headerRect\height, menu\headerIsHot)

    ;Reset back to white
    Color 255, 255, 255
End Function
