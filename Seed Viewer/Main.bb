Include "CreateSeed.bb"
Include "Helpers.bb"

; TODO
; 
; Some rooms are generating different versions of themselves. room2scps2 -> room2_2
; PreventRoomOverlap is not working correctly for some reason.
; Draw colored background for zone. 
; Optimize PlaceForest() by having the load_texture stuff be globals so that it is only ran once, and not every time you place forest.
; Fix rooms generating in the wrong spot. Layout of entire map is good though.
; Some rooms in FillRoom() don't have a lightAmount.

Graphics3D 1280, 720, 0, 2

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

Global INFINITY = (999.0) ^ (99999.0)

Global Mesh_MinX# = INFINITY, Mesh_MinY# = -INFINITY, Mesh_MinZ# = -INFINITY
Global Mesh_MaxX# = INFINITY, Mesh_MaxY# = INFINITY, Mesh_MaxZ# = INFINITY

Global MinVecX#, MinVecY#, MinVecZ#
Global MaxVecX#, MaxVecY#, MaxVecZ#

Global RoomScale# = 8.0 / 2048.0

Cls

Global seed$ = "123"

Global seedCount = 1

.CreateWorld

Global roomCount = 0

Global I_Zone.MapZones = New MapZones

CreateMap(seed)

Global selectedRoom$ = "Out of bounds"

For r.Rooms = Each Rooms
	DebugLog "GETTING ROOM POSITION: " + r\RoomTemplate\Name + " X: " + r\x + " Y: " + r\y + " Z: " + r\z
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
	
	Text(1000, 360, selectedRoom, True, True)
	
	;If MapTemp(x, y) <> 0
		;Text(1000, 360, selectedRoom, True, True)
	;Else
		;selectedRoom = "OOB"
		;Text(1000, 360, selectedRoom, True, True)
	;EndIf

	Color 255,0,0
	If (x < 0 Or y > MapHeight) Then
		Text(1000, 400, "X: OOB" + " Y: OOB", True, True)
	Else
		Text(1000, 400, "X: " + x + " Y: " + y, True, True)
	EndIf

	For r.Rooms = Each Rooms
		Color 255,255,255
		;DebugLog r\RoomTemplate\Name + " X: " + r\x + " Y: " + r\z
		Select r\RoomTemplate\Name
			;Case "gatea"
			;Case "dimension1499" 
			;Case "pocketdimension"
			Default
			
				y_pos% = r\z * cellHeight
				x_pos% = (MapWidth - r\x) * cellWidth
				
				If r\RoomTemplate\Name = "room079" Then 
					Text(1000, 280, "SCP-079: " + r\x + " : " + r\z, True, True)
				EndIf
				
				If r\RoomTemplate\Name = "008" Then
					Text(1000, 240, "SCP-008: " + r\x + " : " + r\z, True, True)
					;Text(1000, 240, "SCP-008 Does not exist on this seed.", True, True)
				EndIf
				
				If r\RoomTemplate\Name = "room2ccont" Then
					Text(1000, 200, "room2ccont: " + r\x + " : " + r\z, True, True)
				EndIf
			
				If x * 8 = r\x And y * 8 = r\z Then
					selectedRoom = r\RoomTemplate\Name
				EndIf
								
				;DebugLog "Drawing " + r\RoomTemplate\Name + " at X: " + x_pos + " Y: " + y_pos
				Rect(x_pos, y_pos, cellWidth, cellHeight, 0)				
				Color 255,0,0
				Text(x_pos + halfCellWidth, y_pos + halfCellHeight, Int(r\x) + ":" + Int(r\z), True, True)

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
		;For i = 0 To MapHeight
		;	For j = 0 To MapWidth
		;		Color 255,0,0
		;		Rect(j * cellWidth, i * cellHeight, cellWidth, cellHeight, 1)
		;	Next
		;Next
		ResetMap()
		roomCount = 0
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