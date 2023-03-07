Include "CreateSeed.bb"
Include "Helpers.bb"
Include "../Source Code/AAText.bb"
Include "../Source Code/Blitz_Basic_Bank.bb"
Include "../Source Code/Blitz_File_FileName.bb"

; TODO
; 
; x Convert 3D room coordinates to 2D coordinates and draw map on screen.
; x DIVIDE THE X AND Z ROOM COORDINATES BY 8 <---------------------------------------------	
; Fix errors in forest generation.

Graphics 1280, 720, 0, 2

SetBuffer FrontBuffer()

Color 255,255,255

Global roomsLineCount% = 1

Global screenWidth% = GraphicsWidth()
Global screenHeight% = GraphicsHeight()

Global cellWidth% = screenHeight * 0.05; screenWidth * 0.05
Global cellHeight% = screenHeight * 0.05

Cls

Global seed$ = "cld31"

Global I_Zone.MapZones = New MapZones

CreateMap(seed)

Global selectedRoom$ = "amogus"

Global mapWidthPixels% = cellWidth * MapWidth
Global mapHeightPixels% = cellHeight * MapHeight

For r.Rooms = Each Rooms
	r\x = r\x / 8
	r\z = r\z / 8
Next

While Not KeyDown(1)

	Cls
	
	mouse_x = MouseX()
	mouse_y = MouseY()	
	
	Color 255,0,0
	
	Text(1000, 360, selectedRoom, True, True)

	If (x > MapWidth Or y > MapWidth) Then
		Text(1000, 400, "X: OOB" + " Y: OOB", True, True)
	Else
		Text(1000, 400, "X: " + x + " Y: " + y, True, True)
		selectedRoom = MapName(x, y)
	EndIf

	Color 255,255,255
	For r.Rooms = Each Rooms
		
		x% = mouse_x / cellWidth
		y% = mouse_y / cellHeight
		
		If x = roomX And y = roomY Then
			selectedRoom = r\RoomTemplate\Name
		EndIf
		
		y_pos% = r\z * cellHeight
		x_pos% = mapWidthPixels - (r\x * cellWidth)
		
		Rect(x_pos, y_pos, cellWidth, cellHeight, 1)
	Next

	;For i% = 0 To MapHeight
	;	y_pos% = i * cellHeight
	;	For j% = 0 To MapWidth
	;		x_pos = (j * cellWidth)
	;		Color 255,255,255
	;		If MapTemp(j, i) <> 0 Then
	;			Rect(x_pos, y_pos, cellWidth, cellHeight, 1)
	;		Else
	;			Local zone% = GetZone(i)
	;			Select zone
	;				Case 0
	;					Color 255,0,0
	;					Rect(x_pos, y_pos, cellWidth, cellHeight, 1)
	;				Case 1
	;					Color 0,255,0
	;					Rect(x_pos, y_pos, cellWidth, cellHeight, 1)
	;				Case 2
	;					Color 0,0,255
	;					Rect(x_pos, y_pos, cellWidth, cellHeight, 1)
	;			End Select
	;		EndIf
	;		Color 0,0,0 ;Color Rnd(0,255), Rnd(0,255), Rnd(0,255)
	;		Text((x_pos + (cellWidth/2)), (y_pos + (cellHeight/2)), Str(MapTemp(j, i)), True, True)
	;	Next
	;Next
Wend

WaitKey