Include "CreateSeed.bb"
Include "Helpers.bb"
Include "../Source Code/AAText.bb"
Include "../Source Code/Blitz_Basic_Bank.bb"
Include "../Source Code/Blitz_File_FileName.bb"

; TODO
; 
; Fix errors in forest generation.
;
; Draw colored background for zone. 
; Fix 100% gpu usage. FPS cap maybe.

Graphics 1280, 720, 0, 2

SetBuffer BackBuffer()

ClsColor 0,0,0

Color 255,255,255

Global roomsLineCount% = 1
Global resetMap% = False

Global screenWidth% = GraphicsWidth()
Global screenHeight% = GraphicsHeight()

Global cellWidth% = screenHeight * 0.05; screenWidth * 0.05
Global cellHeight% = screenHeight * 0.05

Global halfCellWidth% = cellWidth / 2
Global halfCellHeight% = cellHeight / 2

Cls

Global seed$ = "123456"

.CreateWorld

Dim MapTemp%(MapWidth+1, MapHeight+1)
Dim MapFound%(MapWidth+1, MapHeight+1)
Dim MapName$(MapWidth, MapHeight)
Dim MapRoomID%(ROOM4 + 1)
Dim MapRoom$(ROOM4 + 1, 0)

Global roomCount = 0

Global I_Zone.MapZones = New MapZones

Local r.Rooms
For r.Rooms = Each Rooms
	Delete r
Next

CreateMap(seed)

Global selectedRoom$ = "Out of bounds"

For r.Rooms = Each Rooms
	r\x = r\x / 8
	r\z = r\z / 8
	roomCount = roomCount + 1
Next
	
DebugLog "---------------- ROOM COUNT " + roomCount + " ---------------------"

While Not KeyDown(1)

	Cls	
	
	mouse_x = MouseX()
	mouse_y = MouseY()	
	
	Color 255,0,0
			
	x% = MapWidth - (mouse_x / cellWidth);(mapWidthPixels - mouse_x) / cellWidth
	y% = mouse_y / cellHeight
	
	Text(1000, 320, seed, True, True)
	
	If MapTemp(x, y) <> 0
		Text(1000, 360, selectedRoom, True, True)
	Else
		selectedRoom = "OOB"
		Text(1000, 360, selectedRoom, True, True)
	EndIf
		

	Color 255,0,0
	If (x < 0 Or y > MapHeight) Then
		Text(1000, 400, "X: OOB" + " Y: OOB", True, True)
	Else
		Text(1000, 400, "X: " + x + " Y: " + y, True, True)
	EndIf

	For r.Rooms = Each Rooms
		Color 255,255,255
		;Print(r\RoomTemplate\Name + " X: " + r\x + " Y: " + r\z)
		Select r\RoomTemplate\Name
			Case "gatea"
			Case "dimension1499" 
			Case "pocketdimension"
			Default
				If x = r\x And y = r\z Then
					selectedRoom = r\RoomTemplate\Name
				EndIf
				
				y_pos% = r\z * cellHeight
				x_pos% = (MapWidth - r\x) * cellWidth
				;DebugLog "Drawing " + r\RoomTemplate\Name + " at X: " + x_pos + " Y: " + y_pos
				Rect(x_pos, y_pos, cellWidth, cellHeight, 1)				
				Color 255,0,0
				Text(x_pos + halfCellWidth, y_pos + halfCellHeight, r\x + ":" + r\z, True, True)
		End Select
	Next
	;For i = 0 To MapHeight
	;	For j = 0 To MapWidth
	;		Color 255,255,255
	;		Local xpos% = MapWidth - j
	;		If MapTemp(j, i) <> 0 Then
	;			Rect(xpos * cellWidth, i * cellHeight, cellWidth, cellHeight, 1)
	;		Else
	;			Color 255,0,0
	;			Rect(xpos * cellWidth, i * cellHeight, cellWidth, cellHeight, 1)
	;		EndIf
	;	Next
	;Next
	Flip
	
	If MouseHit(1) Then
		SeedRnd MilliSecs()
		GenerateSeed()
		;roomsLineCount = 1
		For i = 0 To MapHeight
			For j = 0 To MapWidth
				Color 255,0,0
				Rect(j * cellWidth, i * cellHeight, cellWidth, cellHeight, 1)
			Next
		Next
		ResetMap()
		roomCount = 0
		For r.Rooms = Each Rooms
			roomCount = roomCount + 1
		Next
		DebugLog "---------------- ROOM COUNT " + roomCount + " ---------------------"
		resetMap = True
		Exit
	EndIf
	
	Flip
Wend

If resetMap Then
	resetMap = False
	Goto CreateWorld
EndIf

WaitKey










