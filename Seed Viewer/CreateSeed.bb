Const gridsize = 10
Const deviation_chance% = 40 ;out of 100
Const branch_chance% = 65
Const branch_max_life% = 4
Const branch_die_chance% = 18
Const max_deviation_distance% = 3
Const return_chance% = 27
Const center = 5 ;(gridsize-1) / 2

Type Forest
	Field TileMesh%[6]
	Field DetailMesh%[6]
	Field TileTexture%[10]
	Field grid%[(gridsize*gridsize)+11]
	Field TileEntities%[(gridsize*gridsize)+1]
	Field Forest_Pivot%
	
	Field Door%[2]
	Field DetailEntities%[2]
	
	Field ID%
End Type

Const MaxRoomLights = 32

Const ROOM1% = 1, ROOM2% = 2, ROOM2C% = 3, ROOM3% = 4, ROOM4% = 5

Global RoomTempID%
Type RoomTemplates
	Field obj%, id%
	Field objPath$
	
	Field zone%[5]
	
	Field Shape%, Name$
	Field Commonness%, Large%
	Field DisableDecals%
	
	Field TempTriggerboxAmount
	Field TempTriggerbox[128]
	Field TempTriggerboxName$[128]
	
	Field UseLightCones%
	
	Field DisableOverlapCheck% = True
	
	Field MinX#, MinY#, MinZ#
	Field MaxX#, MaxY#, MaxZ#
End Type 	

Function CreateRoomTemplate.RoomTemplates(meshpath$)
	Local rt.RoomTemplates = New RoomTemplates
	
	rt\objPath = meshpath
	
	rt\id = RoomTempID
	RoomTempID=RoomTempID+1
	
	Return rt
End Function

Function LoadRoomTemplates(file$)
	CatchErrors("Uncaught (LoadRoomTemplates)")
	Local TemporaryString$, i%
	Local rt.RoomTemplates = Null
	Local StrTemp$ = ""
	
	Local f = OpenFile(file)
	
	DebugLog "Starting LoadRoomTemplates."
	While Not Eof(f)
		TemporaryString = Trim(ReadLine(f))
		If Left(TemporaryString,1) = "[" Then
			TemporaryString = Mid(TemporaryString, 2, Len(TemporaryString) - 2)
			StrTemp = GetINIString(file, TemporaryString, "mesh path")
			
			rt = CreateRoomTemplate(StrTemp)
			rt\Name = Lower(TemporaryString)
			
			StrTemp = Lower(GetINIString(file, TemporaryString, "shape"))
							
			Select StrTemp
				Case "room1", "1"
					rt\Shape = ROOM1
				Case "room2", "2"
					rt\Shape = ROOM2
				Case "room2c", "2c"
					rt\Shape = ROOM2C
				Case "room3", "3"
					rt\Shape = ROOM3
				Case "room4", "4"
					rt\Shape = ROOM4
				Default
			End Select
			
			For i = 0 To 4
				rt\zone[i]= GetINIInt(file, TemporaryString, "zone"+(i+1))
			Next
			
			rt\Commonness = Max(Min(GetINIInt(file, TemporaryString, "commonness"), 100), 0)
			rt\Large = GetINIInt(file, TemporaryString, "large")
			rt\DisableDecals = GetINIInt(file, TemporaryString, "disabledecals")
			rt\UseLightCones = GetINIInt(file, TemporaryString, "usevolumelighting")
			rt\DisableOverlapCheck = GetINIInt(file, TemporaryString, "disableoverlapcheck")
		EndIf
		; Temporary work around for an EOF error. UpdateINIFile is running when it should not I think :/
		roomsLineCount = roomsLineCount+ 1
		If roomsLineCount = 748 Then Exit
	Wend
	i = 1
	Repeat
		StrTemp = GetINIString(file, "room ambience", "ambience"+i)
		If StrTemp = "" Then Exit
		
		;RoomAmbience[i]=LoadSound_Strict(StrTemp)
		i=i+1
	Forever
	
	CloseFile f
	
	CatchErrors("LoadRoomTemplates")
End Function

Type Rooms
	Field zone%
	
	Field found%
	
	Field obj%
	Field x%, y%, z%
	Field angle%
	Field RoomTemplate.RoomTemplates
	
	Field dist#
	
	Field SoundCHN%
	
	Field fr.Forest
	
	Field grid.Grids
	
	Field Lights%[MaxRoomLights]
	
	Field Adjacent.Rooms[4]	
	Field NonFreeAble%[10]
	Field Textures%[10]
	
	Field MaxLights% = 0
	Field AlarmRotor%[1]
	Field AlarmRotorLight%[1]
	Field TriggerboxAmount
	Field Triggerbox[128]
	Field TriggerboxName$[128]
	Field MaxWayPointY#

	Field MinX#, MinY#, MinZ#
	Field MaxX#, MaxY#, MaxZ#
End Type 

Type Doors
	Field open%, angle%
End Type 

Const gridsz% = 19

Type Grids
	Field grid%[gridsz*gridsz]
	Field angles%[gridsz*gridsz]
	Field Meshes%[7]
	Field Entities%[gridsz*gridsz]
	Field waypoints.WayPoints[gridsz*gridsz]
End Type

Type LightTemplates
	Field roomtemplate.RoomTemplates
	Field ltype%
	Field x#, y#, z#
	Field range#
	Field r%, g%, b%
	
	Field pitch#, yaw#
	Field innerconeangle%, outerconeangle#
End Type 

Function AddTempLight.LightTemplates(rt.RoomTemplates, x#, y#, z#, ltype%, range#, r%, g%, b%)
	lt.lighttemplates = New LightTemplates
	lt\roomtemplate = rt
	lt\x = x
	lt\y = y
	lt\z = z
	lt\ltype = ltype
	lt\range = range
	lt\r = r
	lt\g = g
	lt\b = b
	
	Return lt
End Function


Type WayPoints
	Field obj
	Field room.Rooms
	Field state%
	;Field tempDist#
	;Field tempSteps%
	Field connected.WayPoints[5]
	Field dist#[5]
	
	Field Fcost#, Gcost#, Hcost#
	
	Field parent.WayPoints
End Type

Type MapZones
	Field Transition%[1]
	Field HasCustomForest%
	Field HasCustomMT%
End Type

Function GenerateSeedNumber(seed$)
 	Local temp% = 0
 	Local shift% = 0
 	For i = 1 To Len(seed)
 		temp = temp Xor (Asc(Mid(seed,i,1)) Shl shift)
 		shift=(shift+1) Mod 24
	Next
	DebugLog "GENERATED SEED NUMBER OF: " + temp
 	Return temp
End Function

Function GenerateSeed()
	seed = ""
	n = Rand(4,8)
	For i = 1 To n
		If Rand(3)=1 Then
 			seed = seed + Rand(0,9)
		Else
			seed = seed + Chr(Rand(97,122))
		EndIf
	Next
End Function

LoadRoomTemplates("..\Data\rooms.ini")
Const ZONEAMOUNT = 3

Global MapHeight% = 18
Global MapWidth% = 18

Dim MapTemp%(MapWidth+1, MapHeight+1)
Dim MapFound%(MapWidth+1, MapHeight+1)
Dim MapName$(MapWidth, MapHeight)
Dim MapRoomID%(ROOM4 + 1)
Dim MapRoom$(ROOM4 + 1, 0)

Function GetZone(zoneNumber%)
	Return Min(Floor((Float(MapWidth-zoneNumber)/MapWidth*ZONEAMOUNT)),ZONEAMOUNT-1)
End Function

Function CreateMap(RandomSeed$)
	DebugLog ("Generating a map using the seed "+RandomSeed)
	
	I_Zone\Transition[0] = 13
	I_Zone\Transition[1] = 7
	I_Zone\HasCustomForest = False
	I_Zone\HasCustomMT = False
	
	Local x%, y%, temp%
	Local i%, x2%, y2%
	Local width%, height%
	
	Local zone%
	
	SeedRnd GenerateSeedNumber(RandomSeed)	
	
	Dim MapName$(MapWidth, MapHeight)
	
	Dim MapRoomID%(ROOM4 + 1)
	
	x = Floor(MapWidth / 2)
	y = MapHeight - 2;Rand(3, 5)
	
	For i = y To MapHeight - 1
		MapTemp(x, i) = True
	Next
	
	Repeat
		width = Rand(10, 15)
		
		If x > MapWidth*0.6 Then
			width = -width
		ElseIf x > MapWidth*0.4
			x = x-width/2
		EndIf
		
		;make sure the hallway doesn't go outside the array
		If x+width > MapWidth-3 Then
			;x = -width+MapWidth-4
			
			width=MapWidth-3-x
		ElseIf x+width < 2
			
			;x = 3-width
			width=-x+2
		EndIf
		
		x = Min(x, x + width)
		width = Abs(width)
		For i = x To x + width
			MapTemp(Min(i,MapWidth), y) = True
		Next
		
		height = Rand(3, 4)
		If y - height < 1 Then height = y-1
		
		yhallways = Rand(4,5)
		
		If GetZone(y-height)<>GetZone(y-height+1) Then height=height-1
		
		For i = 1 To yhallways
			
			x2 = Max(Min(Rand(x, x + width-1),MapWidth-2),2)
			While MapTemp(x2, y - 1) Or MapTemp(x2 - 1, y - 1) Or MapTemp(x2 + 1, y - 1)
				x2=x2+1
			Wend
			
			If x2<x+width Then
				If i = 1 Then
					tempheight = height 
					If Rand(2)=1 Then x2 = x Else x2 = x+width
				Else
					tempheight = Rand(1,height)
				EndIf
				
				For y2 = y - tempheight To y
					If GetZone(y2)<>GetZone(y2+1) Then ;a room leading from zone to another
						MapTemp(x2, y2) = 255
					Else
						MapTemp(x2, y2) = True
					EndIf
				Next
				
				If tempheight = height Then temp = x2
			End If
			
		Next
		
		x = temp
		y = y - height
	Until y < 2
	
	
	Local ZoneAmount=3
	Local Room1Amount%[3], Room2Amount%[3],Room2CAmount%[3],Room3Amount%[3],Room4Amount%[3]
	
	;count the amount of rooms
	For y = 1 To MapHeight - 1
		zone% = GetZone(y)
		
		For x = 1 To MapWidth - 1
			If MapTemp(x, y) > 0 Then
				temp = Min(MapTemp(x + 1, y),1) + Min(MapTemp(x - 1, y),1)
				temp = temp + Min(MapTemp(x, y + 1),1) + Min(MapTemp(x, y - 1),1)			
				If MapTemp(x,y)<255 Then MapTemp(x, y) = temp
				Select MapTemp(x,y)
					Case 1
						Room1Amount[zone]=Room1Amount[zone]+1
					Case 2
						If Min(MapTemp(x + 1, y),1) + Min(MapTemp(x - 1, y),1)= 2 Then
							Room2Amount[zone]=Room2Amount[zone]+1	
						ElseIf Min(MapTemp(x, y + 1),1) + Min(MapTemp(x , y - 1),1)= 2
							Room2Amount[zone]=Room2Amount[zone]+1	
						Else
							Room2CAmount[zone] = Room2CAmount[zone]+1
						EndIf
					Case 3
						Room3Amount[zone]=Room3Amount[zone]+1
					Case 4
						Room4Amount[zone]=Room4Amount[zone]+1
				End Select
			EndIf
		Next
	Next		
	
	;force more room1s (if needed)
	For i = 0 To 2
		;need more rooms if there are less than 5 of them
		temp = -Room1Amount[i]+5
		
		If temp > 0 Then
			
			For y = (MapHeight/ZoneAmount)*(2-i)+1 To ((MapHeight/ZoneAmount) * ((2-i)+1.0))-2
				
				For x = 2 To MapWidth - 2
					If MapTemp(x, y) = 0 Then
						
						If (Min(MapTemp(x + 1, y),1) + Min(MapTemp(x - 1, y),1) + Min(MapTemp(x, y + 1),1) + Min(MapTemp(x, y - 1),1)) = 1 Then
							;If Rand(4)=1 Then
							
							If MapTemp(x + 1, y) Then
								x2 = x+1 : y2 = y
							ElseIf MapTemp(x - 1, y)
								x2 = x-1 : y2 = y
							ElseIf MapTemp(x, y+1)
								x2 = x : y2 = y+1	
							ElseIf MapTemp(x, y-1)
								x2 = x : y2 = y-1
							EndIf
							
							placed = False
							If MapTemp(x2,y2)>1 And MapTemp(x2,y2)<4 Then 
								Select MapTemp(x2,y2)
									Case 2
										If Min(MapTemp(x2 + 1, y2),1) + Min(MapTemp(x2 - 1, y2),1)= 2 Then
											Room2Amount[i]=Room2Amount[i]-1
											Room3Amount[i]=Room3Amount[i]+1
											placed = True
										ElseIf Min(MapTemp(x2, y2 + 1),1) + Min(MapTemp(x2, y2 - 1),1)= 2
											Room2Amount[i]=Room2Amount[i]-1
											Room3Amount[i]=Room3Amount[i]+1
											placed = True
										EndIf
									Case 3
										Room3Amount[i]=Room3Amount[i]-1
										Room4Amount[i]=Room4Amount[i]+1	
										placed = True
								End Select
								
								If placed Then
									MapTemp(x2,y2)=MapTemp(x2,y2)+1
									
									MapTemp(x, y) = 1
									Room1Amount[i] = Room1Amount[i]+1	
									
									temp=temp-1
								EndIf
							EndIf
						EndIf
						
					EndIf
					If temp = 0 Then Exit
				Next
				If temp = 0 Then Exit
			Next
		EndIf
	Next
	
	
	
	
	
	;force more room4s and room2Cs
	For i = 0 To 2
		
		Select i
			Case 2
				zone=2
				temp2=MapHeight/3;-1
			Case 1
				zone=MapHeight/3+1
				temp2=MapHeight*(2.0/3.0)-1
			Case 0
				zone=MapHeight*(2.0/3.0)+1
				temp2=MapHeight-2
		End Select
		
		DebugLog "CHECKING ROOM AMOUNT"
		If Room4Amount[i]<1 Then ;we want at least 1 ROOM4
			DebugLog "forcing a ROOM4 into zone "+i
			temp=0
			
			For y = zone To temp2
				For x = 2 To MapWidth - 2
					If MapTemp(x,y)=3 Then
						Select 0 ;see if adding a ROOM1 is possible
							Case (MapTemp(x+1,y) Or MapTemp(x+1,y+1) Or MapTemp(x+1,y-1) Or MapTemp(x+2,y))
								MapTemp(x+1,y)=1
								temp=1
							Case (MapTemp(x-1,y) Or MapTemp(x-1,y+1) Or MapTemp(x-1,y-1) Or MapTemp(x-2,y))
								MapTemp(x-1,y)=1
								temp=1
							Case (MapTemp(x,y+1) Or MapTemp(x+1,y+1) Or MapTemp(x-1,y+1) Or MapTemp(x,y+2))
								MapTemp(x,y+1)=1
								temp=1
							Case (MapTemp(x,y-1) Or MapTemp(x+1,y-1) Or MapTemp(x-1,y-1) Or MapTemp(x,y-2))
								MapTemp(x,y-1)=1
								temp=1
						End Select
						If temp=1 Then
							MapTemp(x,y)=4 ;turn this room into a ROOM4
							DebugLog "ROOM4 forced into slot ("+x+", "+y+")"
							Room4Amount[i]=Room4Amount[i]+1
							Room3Amount[i]=Room3Amount[i]-1
							Room1Amount[i]=Room1Amount[i]+1
						EndIf
					EndIf
					If temp=1 Then Exit
				Next
				If temp=1 Then Exit
			Next
			
			If temp=0 Then DebugLog "Couldn't place ROOM4 in zone "+i
		EndIf
		
		If Room2CAmount[i]<1 Then ;we want at least 1 ROOM2C
			DebugLog "forcing a ROOM2C into zone "+i
			temp=0
			
			zone=zone+1
			temp2=temp2-1
			
			For y = zone To temp2
				For x = 3 To MapWidth - 3
					If MapTemp(x,y)=1 Then
						Select True ;see if adding some rooms is possible
							Case MapTemp(x-1,y)>0
								If (MapTemp(x,y-1)+MapTemp(x,y+1)+MapTemp(x+2,y))=0 Then
									If (MapTemp(x+1,y-2)+MapTemp(x+2,y-1)+MapTemp(x+1,y-1))=0 Then
										MapTemp(x,y)=2
										MapTemp(x+1,y)=2
										DebugLog "ROOM2C forced into slot ("+(x+1)+", "+(y)+")"
										MapTemp(x+1,y-1)=1
										temp=1
									Else If (MapTemp(x+1,y+2)+MapTemp(x+2,y+1)+MapTemp(x+1,y+1))=0 Then
										MapTemp(x,y)=2
										MapTemp(x+1,y)=2
										DebugLog "ROOM2C forced into slot ("+(x+1)+", "+(y)+")"
										MapTemp(x+1,y+1)=1
										temp=1
									EndIf
								EndIf
							Case MapTemp(x+1,y)>0
								If (MapTemp(x,y-1)+MapTemp(x,y+1)+MapTemp(x-2,y))=0 Then
									If (MapTemp(x-1,y-2)+MapTemp(x-2,y-1)+MapTemp(x-1,y-1))=0 Then
										MapTemp(x,y)=2
										MapTemp(x-1,y)=2
										DebugLog "ROOM2C forced into slot ("+(x-1)+", "+(y)+")"
										MapTemp(x-1,y-1)=1
										temp=1
									Else If (MapTemp(x-1,y+2)+MapTemp(x-2,y+1)+MapTemp(x-1,y+1))=0 Then
										MapTemp(x,y)=2
										MapTemp(x-1,y)=2
										DebugLog "ROOM2C forced into slot ("+(x-1)+", "+(y)+")"
										MapTemp(x-1,y+1)=1
										temp=1
									EndIf
								EndIf
							Case MapTemp(x,y-1)>0
								If (MapTemp(x-1,y)+MapTemp(x+1,y)+MapTemp(x,y+2))=0 Then
									If (MapTemp(x-2,y+1)+MapTemp(x-1,y+2)+MapTemp(x-1,y+1))=0 Then
										MapTemp(x,y)=2
										MapTemp(x,y+1)=2
										DebugLog "ROOM2C forced into slot ("+(x)+", "+(y+1)+")"
										MapTemp(x-1,y+1)=1
										temp=1
									Else If (MapTemp(x+2,y+1)+MapTemp(x+1,y+2)+MapTemp(x+1,y+1))=0 Then
										MapTemp(x,y)=2
										MapTemp(x,y+1)=2
										DebugLog "ROOM2C forced into slot ("+(x)+", "+(y+1)+")"
										MapTemp(x+1,y+1)=1
										temp=1
									EndIf
								EndIf
							Case MapTemp(x,y+1)>0
								If (MapTemp(x-1,y)+MapTemp(x+1,y)+MapTemp(x,y-2))=0 Then
									If (MapTemp(x-2,y-1)+MapTemp(x-1,y-2)+MapTemp(x-1,y-1))=0 Then
										MapTemp(x,y)=2
										MapTemp(x,y-1)=2
										DebugLog "ROOM2C forced into slot ("+(x)+", "+(y-1)+")"
										MapTemp(x-1,y-1)=1
										temp=1
									Else If (MapTemp(x+2,y-1)+MapTemp(x+1,y-2)+MapTemp(x+1,y-1))=0 Then
										MapTemp(x,y)=2
										MapTemp(x,y-1)=2
										DebugLog "ROOM2C forced into slot ("+(x)+", "+(y-1)+")"
										MapTemp(x+1,y-1)=1
										temp=1
									EndIf
								EndIf
						End Select
						If temp=1 Then
							Room2CAmount[i]=Room2CAmount[i]+1
							Room2Amount[i]=Room2Amount[i]+1
						EndIf
					EndIf
					If temp=1 Then Exit
				Next
				If temp=1 Then Exit
			Next
			
			If temp=0 Then DebugLog "Couldn't place ROOM2C in zone "+i
		EndIf
		
	Next
	
	Local MaxRooms% = 55*MapWidth/20
	MaxRooms=Max(MaxRooms,Room1Amount[0]+Room1Amount[1]+Room1Amount[2]+1)
	MaxRooms=Max(MaxRooms,Room2Amount[0]+Room2Amount[1]+Room2Amount[2]+1)
	MaxRooms=Max(MaxRooms,Room2CAmount[0]+Room2CAmount[1]+Room2CAmount[2]+1)
	MaxRooms=Max(MaxRooms,Room3Amount[0]+Room3Amount[1]+Room3Amount[2]+1)
	MaxRooms=Max(MaxRooms,Room4Amount[0]+Room4Amount[1]+Room4Amount[2]+1)
	Dim MapRoom$(ROOM4 + 1, MaxRooms)
	
	
	;zone 1 --------------------------------------------------------------------------------------------------
	
	Local min_pos = 1, max_pos = Room1Amount[0]-1
	
	MapRoom(ROOM1, 0) = "start"	
	SetRoom("roompj", ROOM1, Floor(0.1*Float(Room1Amount[0])),min_pos,max_pos)
	SetRoom("914", ROOM1, Floor(0.3*Float(Room1Amount[0])),min_pos,max_pos)
	SetRoom("room1archive",ROOM1,Floor(0.5*Float(Room1Amount[0])),min_pos,max_pos)
	SetRoom("room205", ROOM1, Floor(0.6*Float(Room1Amount[0])),min_pos,max_pos)
	
	MapRoom(ROOM2C, 0) = "lockroom"
	
	min_pos = 1
	max_pos = Room2Amount[0]-1
	
	MapRoom(ROOM2, 0) = "room2closets"
	SetRoom("room2testroom2", ROOM2, Floor(0.1*Float(Room2Amount[0])),min_pos,max_pos)
	SetRoom("room2scps", ROOM2, Floor(0.2*Float(Room2Amount[0])),min_pos,max_pos)
	SetRoom("room2storage", ROOM2, Floor(0.3*Float(Room2Amount[0])),min_pos,max_pos)
	SetRoom("room2gw_b", ROOM2, Floor(0.4*Float(Room2Amount[0])),min_pos,max_pos)
	SetRoom("room2sl", ROOM2, Floor(0.5*Float(Room2Amount[0])),min_pos,max_pos)
	SetRoom("room012", ROOM2, Floor(0.55*Float(Room2Amount[0])),min_pos,max_pos)
	SetRoom("room2scps2",ROOM2,Floor(0.6*Float(Room2Amount[0])),min_pos,max_pos)
	SetRoom("room1123",ROOM2,Floor(0.7*Float(Room2Amount[0])),min_pos,max_pos)
	SetRoom("room2elevator",ROOM2,Floor(0.85*Float(Room2Amount[0])),min_pos,max_pos)
	
	
	MapRoom(ROOM3, Floor(Rnd(0.2,0.8)*Float(Room3Amount[0]))) = "room3storage"
	
	MapRoom(ROOM2C, Floor(0.5*Float(Room2CAmount[0]))) = "room1162"
	
	MapRoom(ROOM4, Floor(0.3*Float(Room4Amount[0]))) = "room4info"
	
	;zone 2 --------------------------------------------------------------------------------------------------
	
	min_pos = Room1Amount[0]
	max_pos = Room1Amount[0]+Room1Amount[1]-1
	
	SetRoom("room079", ROOM1, Room1Amount[0]+Floor(0.15*Float(Room1Amount[1])),min_pos,max_pos)
    SetRoom("room106", ROOM1, Room1Amount[0]+Floor(0.3*Float(Room1Amount[1])),min_pos,max_pos)
    SetRoom("008", ROOM1, Room1Amount[0]+Floor(0.4*Float(Room1Amount[1])),min_pos,max_pos)
    SetRoom("room035", ROOM1, Room1Amount[0]+Floor(0.5*Float(Room1Amount[1])),min_pos,max_pos)
    SetRoom("coffin", ROOM1, Room1Amount[0]+Floor(0.7*Float(Room1Amount[1])),min_pos,max_pos)
	
	min_pos = Room2Amount[0]
	max_pos = Room2Amount[0]+Room2Amount[1]-1
	
	MapRoom(ROOM2, Room2Amount[0]+Floor(0.1*Float(Room2Amount[1]))) = "room2nuke"
	SetRoom("room2tunnel", ROOM2, Room2Amount[0]+Floor(0.25*Float(Room2Amount[1])),min_pos,max_pos)
	SetRoom("room049", ROOM2, Room2Amount[0]+Floor(0.4*Float(Room2Amount[1])),min_pos,max_pos)
	SetRoom("room2shaft",ROOM2,Room2Amount[0]+Floor(0.6*Float(Room2Amount[1])),min_pos,max_pos)
	SetRoom("testroom", ROOM2, Room2Amount[0]+Floor(0.7*Float(Room2Amount[1])),min_pos,max_pos)
	SetRoom("room2servers", ROOM2, Room2Amount[0]+Floor(0.9*Room2Amount[1]),min_pos,max_pos)
	
	MapRoom(ROOM3, Room3Amount[0]+Floor(0.3*Float(Room3Amount[1]))) = "room513"
	MapRoom(ROOM3, Room3Amount[0]+Floor(0.6*Float(Room3Amount[1]))) = "room966"
	
	MapRoom(ROOM2C, Room2CAmount[0]+Floor(0.5*Float(Room2CAmount[1]))) = "room2cpit"
	
	
	;zone 3  --------------------------------------------------------------------------------------------------
	
	MapRoom(ROOM1, Room1Amount[0]+Room1Amount[1]+Room1Amount[2]-2) = "exit1"
	MapRoom(ROOM1, Room1Amount[0]+Room1Amount[1]+Room1Amount[2]-1) = "gateaentrance"
	MapRoom(ROOM1, Room1Amount[0]+Room1Amount[1]) = "room1lifts"
	
	min_pos = Room2Amount[0]+Room2Amount[1]
	max_pos = Room2Amount[0]+Room2Amount[1]+Room2Amount[2]-1		
	
	MapRoom(ROOM2, min_pos+Floor(0.1*Float(Room2Amount[2]))) = "room2poffices"
	SetRoom("room2cafeteria", ROOM2, min_pos+Floor(0.2*Float(Room2Amount[2])),min_pos,max_pos)
	SetRoom("room2sroom", ROOM2, min_pos+Floor(0.3*Float(Room2Amount[2])),min_pos,max_pos)
	SetRoom("room2servers2", ROOM2, min_pos+Floor(0.4*Room2Amount[2]),min_pos,max_pos)	
	SetRoom("room2offices", ROOM2, min_pos+Floor(0.45*Room2Amount[2]),min_pos,max_pos)
	SetRoom("room2offices4", ROOM2, min_pos+Floor(0.5*Room2Amount[2]),min_pos,max_pos)	
	SetRoom("room860", ROOM2, min_pos+Floor(0.6*Room2Amount[2]),min_pos,max_pos)
	SetRoom("medibay", ROOM2, min_pos+Floor(0.7*Float(Room2Amount[2])),min_pos,max_pos)
	SetRoom("room2poffices2", ROOM2, min_pos+Floor(0.8*Room2Amount[2]),min_pos,max_pos)
	SetRoom("room2offices2", ROOM2, min_pos+Floor(0.9*Float(Room2Amount[2])),min_pos,max_pos)
	
	MapRoom(ROOM2C, Room2CAmount[0]+Room2CAmount[1]) = "room2ccont"	
	MapRoom(ROOM2C, Room2CAmount[0]+Room2CAmount[1]+1) = "lockroom2"		
	
	MapRoom(ROOM3, Room3Amount[0]+Room3Amount[1]+Floor(0.3*Float(Room3Amount[2]))) = "room3servers"
	MapRoom(ROOM3, Room3Amount[0]+Room3Amount[1]+Floor(0.7*Float(Room3Amount[2]))) = "room3servers2"
	;MapRoom(ROOM3, Room3Amount[0]+Room3Amount[1]) = "room3gw"
	MapRoom(ROOM3, Room3Amount[0]+Room3Amount[1]+Floor(0.5*Float(Room3Amount[2]))) = "room3offices"
	
	;----------------------- luodaan kartta --------------------------------
	
	DebugLog "GETTING RANDOM FLOAT BEFORE CREATING ROOMS: " + Rnd(0, 1000000)
	
	temp = 0
	Local r.Rooms, spacing# = 8.0
	For y = MapHeight - 1 To 1 Step - 1
		
		;zone% = GetZone(y)
		
		If y < MapHeight/3+1 Then
			zone=3
		ElseIf y < MapHeight*(2.0/3.0);-1
			zone=2
		Else
			zone=1
		EndIf
		
		For x = 1 To MapWidth - 2
			If MapTemp(x, y) = 255 Then
				If y>MapHeight/2 Then ;zone = 2
					r = CreateRoom(zone, ROOM2, x * 8, 0, y * 8, "checkpoint1")
				Else ;If zone = 3
					r = CreateRoom(zone, ROOM2, x * 8, 0, y * 8, "checkpoint2")
				EndIf
			ElseIf MapTemp(x, y) > 0
				
				temp = Min(MapTemp(x + 1, y),1) + Min(MapTemp(x - 1, y),1) + Min(MapTemp(x, y + 1),1) + Min(MapTemp(x, y - 1),1)
				
				Select temp ;viereisiss� ruuduissa olevien huoneiden m��r�
					Case 1
						If MapRoomID(ROOM1) < MaxRooms And MapName(x,y) = "" Then
							If MapRoom(ROOM1, MapRoomID(ROOM1)) <> "" Then MapName(x, y) = MapRoom(ROOM1, MapRoomID(ROOM1))	
						EndIf
						
						DebugLog "MapName(x,y): " + MapName(x,y) + " Zone: " + zone
						r = CreateRoom(zone, ROOM1, x * 8, 0, y * 8, MapName(x, y))
						DebugLog "r\zone is: " + r\zone
						If MapTemp(x, y + 1) Then
							r\angle = 180 
							;TurnEntity(r\obj, 0, r\angle, 0)
						ElseIf MapTemp(x - 1, y)
							r\angle = 270
							;TurnEntity(r\obj, 0, r\angle, 0)
						ElseIf MapTemp(x + 1, y)
							r\angle = 90
							;TurnEntity(r\obj, 0, r\angle, 0)
						Else 
							r\angle = 0
						End If
						MapRoomID(ROOM1)=MapRoomID(ROOM1)+1
					Case 2
						If MapTemp(x - 1, y)>0 And MapTemp(x + 1, y)>0 Then
							If MapRoomID(ROOM2) < MaxRooms And MapName(x,y) = ""  Then
								If MapRoom(ROOM2, MapRoomID(ROOM2)) <> "" Then MapName(x, y) = MapRoom(ROOM2, MapRoomID(ROOM2))	
							EndIf
							r = CreateRoom(zone, ROOM2, x * 8, 0, y * 8, MapName(x, y))
							If Rand(2) = 1 Then r\angle = 90 Else r\angle = 270
							;TurnEntity(r\obj, 0, r\angle, 0)
							MapRoomID(ROOM2)=MapRoomID(ROOM2)+1
						ElseIf MapTemp(x, y - 1)>0 And MapTemp(x, y + 1)>0
							If MapRoomID(ROOM2) < MaxRooms And MapName(x,y) = ""  Then
								If MapRoom(ROOM2, MapRoomID(ROOM2)) <> "" Then MapName(x, y) = MapRoom(ROOM2, MapRoomID(ROOM2))	
							EndIf
							r = CreateRoom(zone, ROOM2, x * 8, 0, y * 8, MapName(x, y))
							If Rand(2) = 1 Then r\angle = 180 Else r\angle = 0
							;TurnEntity(r\obj, 0, r\angle, 0)
							MapRoomID(ROOM2)=MapRoomID(ROOM2)+1
						Else
							If MapRoomID(ROOM2C) < MaxRooms And MapName(x,y) = ""  Then
								If MapRoom(ROOM2C, MapRoomID(ROOM2C)) <> "" Then MapName(x, y) = MapRoom(ROOM2C, MapRoomID(ROOM2C))	
							EndIf
							
							If MapTemp(x - 1, y)>0 And MapTemp(x, y + 1)>0 Then
								r = CreateRoom(zone, ROOM2C, x * 8, 0, y * 8, MapName(x, y))
								r\angle = 180
								;TurnEntity(r\obj, 0, r\angle, 0)
							ElseIf MapTemp(x + 1, y)>0 And MapTemp(x, y + 1)>0
								r = CreateRoom(zone, ROOM2C, x * 8, 0, y * 8, MapName(x, y))
								r\angle = 90
								;TurnEntity(r\obj, 0, r\angle, 0)
							ElseIf MapTemp(x - 1, y)>0 And MapTemp(x, y - 1)>0
								r = CreateRoom(zone, ROOM2C, x * 8, 0, y * 8, MapName(x, y))
								;TurnEntity(r\obj, 0, 270, 0)
								r\angle = 270
							Else
								r = CreateRoom(zone, ROOM2C, x * 8, 0, y * 8, MapName(x, y))
							EndIf
							MapRoomID(ROOM2C)=MapRoomID(ROOM2C)+1
						EndIf
					Case 3
						If MapRoomID(ROOM3) < MaxRooms And MapName(x,y) = ""  Then
							If MapRoom(ROOM3, MapRoomID(ROOM3)) <> "" Then MapName(x, y) = MapRoom(ROOM3, MapRoomID(ROOM3))	
						EndIf
						
						r = CreateRoom(zone, ROOM3, x * 8, 0, y * 8, MapName(x, y))
						If (Not MapTemp(x, y - 1)) Then
							;TurnEntity(r\obj, 0, 180, 0)
							r\angle = 180
						ElseIf (Not MapTemp(x - 1, y))
							;TurnEntity(r\obj, 0, 90, 0)
							r\angle = 90
						ElseIf (Not MapTemp(x + 1, y))
							;TurnEntity(r\obj, 0, -90, 0)
							r\angle = 270
						End If
						MapRoomID(ROOM3)=MapRoomID(ROOM3)+1
					Case 4
						If MapRoomID(ROOM4) < MaxRooms And MapName(x,y) = ""  Then
							If MapRoom(ROOM4, MapRoomID(ROOM4)) <> "" Then MapName(x, y) = MapRoom(ROOM4, MapRoomID(ROOM4))	
						EndIf
						
						r = CreateRoom(zone, ROOM4, x * 8, 0, y * 8, MapName(x, y))
						MapRoomID(ROOM4)=MapRoomID(ROOM4)+1
				End Select
				
			EndIf
			
		Next
	Next		
	
	DebugLog "GETTING RANDOM FLOAT AFTER CREATING ROOMS: " + Rnd(0, 1000000)
	
	r = CreateRoom(0, ROOM1, (MapWidth-1) * 8, 500, 8, "gatea")
	MapRoomID(ROOM1)=MapRoomID(ROOM1)+1
	
	r = CreateRoom(0, ROOM1, (MapWidth-1) * 8, 0, (MapHeight-1) * 8, "pocketdimension")
	MapRoomID(ROOM1)=MapRoomID(ROOM1)+1	
	
	If IntroEnabled
		r = CreateRoom(0, ROOM1, 8, 0, (MapHeight-1) * 8, "173")
		MapRoomID(ROOM1)=MapRoomID(ROOM1)+1
	EndIf
	
	r = CreateRoom(0, ROOM1, 8, 800, 0, "dimension1499")
	MapRoomID(ROOM1)=MapRoomID(ROOM1)+1
	
	For r.Rooms = Each Rooms
		PreventRoomOverlap(r)
	Next
	
	If 0 Then 
		Repeat
			Cls
			For x = 0 To MapWidth - 1
				For y = 0 To MapHeight - 1
					If MapTemp(x, y) = 0 Then
						
						zone=GetZone(y)
						
						Color 50*zone, 50*zone, 50*zone
						Rect(x * 32, y * 32, 30, 30)
					Else
						If MapTemp(x, y) = 255 Then
							Color 0,200,0
						Else If MapTemp(x,y)=4 Then
							Color 50,50,255
						Else If MapTemp(x,y)=3 Then
							Color 50,255,255
						Else If MapTemp(x,y)=2 Then
							Color 255,255,50
						Else
							Color 255, 255, 255
						EndIf
						Rect(x * 32, y * 32, 30, 30)
					End If
				Next
			Next	
			
			For x = 0 To MapWidth - 1
				For y = 0 To MapHeight - 1
					
					If MouseX()>x*32 And MouseX()<x*32+32 Then
						If MouseY()>y*32 And MouseY()<y*32+32 Then
							Color 255, 0, 0
						Else
							Color 200, 200, 200
						EndIf
					Else
						Color 200, 200, 200
					EndIf
					
					If MapTemp(x, y) > 0 Then
						Text x * 32 +2, (y) * 32 + 2,MapTemp(x, y) +" "+ MapName(x,y)
					End If
				Next
			Next			
			
			Flip
		Until KeyHit(28)		
	EndIf
	
	
	For y = 0 To MapHeight
		For x = 0 To MapWidth
			MapTemp(x, y) = Min(MapTemp(x, y),1)
		Next
	Next
	
	Local d.Doors
	Local shouldSpawnDoor%
	For y = MapHeight To 0 Step -1
		
		If y<I_Zone\Transition[1]-1 Then
			zone=3
		ElseIf y>=I_Zone\Transition[1]-1 And y<I_Zone\Transition[0]-1 Then
			zone=2
		Else
			zone=1
		EndIf
		
		For x = MapWidth To 0 Step -1
			If MapTemp(x,y) > 0 Then
				If zone = 2 Then temp=2 Else temp=0
                
                For r.Rooms = Each Rooms
					r\angle = WrapAngle(r\angle)
					If Int(r\x/8.0)=x And Int(r\z/8.0)=y Then
						shouldSpawnDoor = False
						Select r\RoomTemplate\Shape
							Case ROOM1
								If r\angle=90
									shouldSpawnDoor = True
								EndIf
							Case ROOM2
								If r\angle=90 Or r\angle=270
									shouldSpawnDoor = True
								EndIf
							Case ROOM2C
								If r\angle=0 Or r\angle=90
									shouldSpawnDoor = True
								EndIf
							Case ROOM3
								If r\angle=0 Or r\angle=180 Or r\angle=90
									shouldSpawnDoor = True
								EndIf
							Default
								shouldSpawnDoor = True
						End Select
						If shouldSpawnDoor
							If (x+1)<(MapWidth+1)
								If MapTemp(x + 1, y) > 0 Then
									ri = Rand(-3, 1);d.Doors = CreateDoor(r\zone, Float(x) * spacing + spacing / 2.0, 0, Float(y) * spacing, 90, r, Max(Rand(-3, 1), 0), temp)
									d = CreateDoor()
								EndIf
							EndIf
						EndIf
						
						shouldSpawnDoor = False
						Select r\RoomTemplate\Shape
							Case ROOM1
								If r\angle=180
									shouldSpawnDoor = True
								EndIf
							Case ROOM2
								If r\angle=0 Or r\angle=180
									shouldSpawnDoor = True
								EndIf
							Case ROOM2C
								If r\angle=180 Or r\angle=90
									shouldSpawnDoor = True
								EndIf
							Case ROOM3
								If r\angle=180 Or r\angle=90 Or r\angle=270
									shouldSpawnDoor = True
								EndIf
							Default
								shouldSpawnDoor = True
						End Select
						If shouldSpawnDoor
							If (y+1)<(MapHeight+1)
								If MapTemp(x, y + 1) > 0 Then
									ri = Rand(-3, 1)
									d = CreateDoor(); d.Doors = CreateDoor(r\zone, Float(x) * spacing, 0, Float(y) * spacing + spacing / 2.0, 0, r, Max(Rand(-3, 1), 0), temp)
								EndIf
							EndIf
						EndIf
						
						Exit
					EndIf
                Next
                
			End If
			
		Next
	Next
	
	For r.Rooms = Each Rooms
		;If r\angle >= 360
        ;    r\angle = r\angle-360
        ;EndIf
		r\angle = WrapAngle(r\angle)
		r\Adjacent[0]=Null
		r\Adjacent[1]=Null
		r\Adjacent[2]=Null
		r\Adjacent[3]=Null
		For r2.Rooms = Each Rooms
			If r<>r2 Then
				If r2\z=r\z Then
					If (r2\x)=(r\x+8.0) Then
						r\Adjacent[0]=r2
						;If r\AdjDoor[0] = Null Then r\AdjDoor[0] = r2\AdjDoor[2]
					ElseIf (r2\x)=(r\x-8.0)
						r\Adjacent[2]=r2
						;If r\AdjDoor[2] = Null Then r\AdjDoor[2] = r2\AdjDoor[0]
					EndIf
				ElseIf r2\x=r\x Then
					If (r2\z)=(r\z-8.0) Then
						;r\Adjacent[1]=r2
						;If r\AdjDoor[1] = Null Then r\AdjDoor[1] = r2\AdjDoor[3]
					ElseIf (r2\z)=(r\z+8.0)
						;r\Adjacent[3]=r2
						;If r\AdjDoor[3] = Null Then r\AdjDoor[3] = r2\AdjDoor[1]
					EndIf
				EndIf
			EndIf
			If (r\Adjacent[0]<>Null) And (r\Adjacent[1]<>Null) And (r\Adjacent[2]<>Null) And (r\Adjacent[3]<>Null) Then Exit
		Next
	Next
	
End Function

Function SetRoom(room_name$,room_type%,pos%,min_pos%,max_pos%) ;place a room without overwriting others
	
	If max_pos<min_pos Then DebugLog "Can't place "+room_name : Return False
	
	DebugLog "--- SETROOM: "+Upper(room_name)+" ---"
	Local looped%,can_place%
	looped = False
	can_place = True
	While MapRoom(room_type,pos)<>""
		DebugLog "found "+MapRoom(room_type,pos)
		pos=pos+1
		If pos>max_pos Then
			If looped=False Then
				pos=min_pos+1 : looped=True
			Else
				can_place=False
				Exit
			EndIf
		EndIf
	Wend
	DebugLog room_name+" "+Str(pos)
	If can_place=True Then
		DebugLog "--------------"
		MapRoom(room_type,pos)=room_name
		Return True
	Else
		DebugLog "couldn't place "+room_name
		Return False
	EndIf
End Function

Function CreateRoom.Rooms(zone%, roomshape%, x#, y#, z#, name$ = "")
CatchErrors("Uncaught (CreateRoom)")
	Local r.Rooms = New Rooms
	Local rt.RoomTemplates
	
	r\zone = zone
	
	r\x = x : r\y = y : r\z = z
	
	If name <> "" Then
		name = Lower(name)
		For rt.RoomTemplates = Each RoomTemplates
			If rt\Name = name Then
				r\RoomTemplate = rt
				
				;If rt\obj=0 Then LoadRoomMesh(rt)
				
				;r\obj = CopyEntity(rt\obj)
				;ScaleEntity(r\obj, RoomScale, RoomScale, RoomScale)
				;EntityType(r\obj, HIT_MAP)
				;EntityPickMode(r\obj, 2)
				
				;PositionEntity(r\obj, x, y, z)
				FillRoom(r)
				
				;If r\RoomTemplate\UseLightCones
				;	AddLightCones(r)
				;EndIf
				
				;CalculateRoomExtents(r)
				Return r
			EndIf
		Next
	EndIf
	
	Local temp% = 0
	For rt.RoomTemplates = Each RoomTemplates
		
		For i = 0 To 4
			If rt\zone[i]=zone Then 
				If rt\Shape = roomshape Then temp=temp+rt\Commonness : Exit
			EndIf
		Next
		
	Next
	
	Local RandomRoom% = Rand(temp)
	temp = 0
	For rt.RoomTemplates = Each RoomTemplates
		For i = 0 To 4
			If rt\zone[i]=zone And rt\Shape = roomshape Then
				temp=temp+rt\Commonness
				If RandomRoom > temp - rt\Commonness And RandomRoom <= temp Then
					r\RoomTemplate = rt
					
					;If rt\obj=0 Then LoadRoomMesh(rt)
					
					;r\obj = CopyEntity(rt\obj)
					;ScaleEntity(r\obj, RoomScale, RoomScale, RoomScale)
					;EntityType(r\obj, HIT_MAP)
					;EntityPickMode(r\obj, 2)
					
					;PositionEntity(r\obj, x, y, z)
					FillRoom(r)
					
					;If r\RoomTemplate\UseLightCones
					;	AddLightCones(r)
					;EndIf
					
					;CalculateRoomExtents(r)
					Return r	
				End If
			EndIf
		Next
	Next
	
	CatchErrors("CreateRoom")
End Function

Function CreateDoor.Doors(dopen% = False,  big% = False)
	Local d.Doors = New Doors
	
	d\angle = angle
	d\open = dopen		
	
	If d\open And big = False And Rand(8) = 1 Then ;d\AutoClose = True
	
	EndIf
		
	Return d
	
End Function


Function PreventRoomOverlap(r.Rooms)
	If r\RoomTemplate\DisableOverlapCheck Then Return
	
	Local r2.Rooms,r3.Rooms
	
	Local isIntersecting% = False
	
	;Just skip it when it would try to check for the checkpoints
	If r\RoomTemplate\Name = "checkpoint1" Or r\RoomTemplate\Name = "checkpoint2" Or r\RoomTemplate\Name = "start" Then Return True
	
	;First, check if the room is actually intersecting at all
	For r2 = Each Rooms
		If r2 <> r And (Not r2\RoomTemplate\DisableOverlapCheck) Then
			If CheckRoomOverlap(r, r2) Then
				isIntersecting = True
				Exit
			EndIf
		EndIf
	Next
	
	;If not, then simply return it as True
	If (Not isIntersecting)
		Return True
	EndIf
	
	;Room is interseting: First, check if the given room is a ROOM2, so we could potentially just turn it by 180 degrees
	isIntersecting = False
	Local x% = r\x/8.0
	Local y% = r\z/8.0
	If r\RoomTemplate\Shape = ROOM2 Then
		;Room is a ROOM2, let's check if turning it 180 degrees fixes the overlapping issue
		r\angle = r\angle + 180
		RotateEntity r\obj,0,r\angle,0
						
		For r2 = Each Rooms
			If r2 <> r And (Not r2\RoomTemplate\DisableOverlapCheck) Then
				If CheckRoomOverlap(r, r2) Then
					;didn't work -> rotate the room back and move to the next step
					isIntersecting = True
					r\angle = r\angle - 180
					RotateEntity r\obj,0,r\angle,0
					Exit
				EndIf
			EndIf
		Next
	Else
		isIntersecting = True
	EndIf
	
	;room is ROOM2 and was able to be turned by 180 degrees
	If (Not isIntersecting)
		DebugLog "ROOM2 turning succesful! "+r\RoomTemplate\Name
		Return True
	EndIf
	
	;Room is either not a ROOM2 or the ROOM2 is still intersecting, now trying to swap the room with another of the same type
	isIntersecting = True
	Local temp2,x2%,y2%,rot%,rot2%
	For r2 = Each Rooms
		If r2 <> r And (Not r2\RoomTemplate\DisableOverlapCheck)  Then
			If r\RoomTemplate\Shape = r2\RoomTemplate\Shape And r\zone = r2\zone And (r2\RoomTemplate\Name <> "checkpoint1" And r2\RoomTemplate\Name <> "checkpoint2" And r2\RoomTemplate\Name <> "start") Then
				x = r\x/8.0
				y = r\z/8.0
				rot = r\angle
				
				x2 = r2\x/8.0
				y2 = r2\z/8.0
				rot2 = r2\angle
				
				isIntersecting = False
				
				r\x = x2*8.0
				r\z = y2*8.0
				r\angle = rot2
				PositionEntity r\obj,r\x,r\y,r\z
				RotateEntity r\obj,0,r\angle,0
				
				r2\x = x*8.0
				r2\z = y*8.0
				r2\angle = rot
				PositionEntity r2\obj,r2\x,r2\y,r2\z
				RotateEntity r2\obj,0,r2\angle,0
				
				;make sure neither room overlaps with anything after the swap
				For r3 = Each Rooms
					If (Not r3\RoomTemplate\DisableOverlapCheck) Then
						If r3 <> r Then
							If CheckRoomOverlap(r, r3) Then
								isIntersecting = True
								Exit
							EndIf
						EndIf
						If r3 <> r2 Then
							If CheckRoomOverlap(r2, r3) Then
								isIntersecting = True
								Exit
							EndIf
						EndIf	
					EndIf
				Next
				
				;Either the original room or the "reposition" room is intersecting, reset the position of each room to their original one
				If isIntersecting Then
					r\x = x*8.0
					r\z = y*8.0
					r\angle = rot
					PositionEntity r\obj,r\x,r\y,r\z
					RotateEntity r\obj,0,r\angle,0
					
					r2\x = x2*8.0
					r2\z = y2*8.0
					r2\angle = rot2
					PositionEntity r2\obj,r2\x,r2\y,r2\z
					RotateEntity r2\obj,0,r2\angle,0
					
					isIntersecting = False
				EndIf
			EndIf
		EndIf
	Next
	
	;room was able to the placed in a different spot
	If (Not isIntersecting)
		DebugLog "Room re-placing successful! "+r\RoomTemplate\Name
		Return True
	EndIf
	
	DebugLog "Couldn't fix overlap issue for room "+r\RoomTemplate\Name
	Return False
End Function

Function WrapAngle#(angle#)
	If angle = INFINITY Then Return 0.0
	While angle < 0
		angle = angle + 360
	Wend 
	While angle >= 360
		angle = angle - 360
	Wend
	Return angle
End Function

Function CheckRoomOverlap(r1.Rooms, r2.Rooms)
	If (r1\MaxX	<= r2\MinX Or r1\MaxY <= r2\MinY Or r1\MaxZ <= r2\MinZ) Then Return False
	If (r1\MinX	>= r2\MaxX Or r1\MinY >= r2\MaxY Or r1\MinZ >= r2\MaxZ) Then Return False
	
	Return True
End Function

Function FillRoom(r.Rooms)

	;
	;	EVERY TIME WE CALL CreateItem() WE HAVE TO DO 1 Rand(360) 
	;	
	;
	
	Local lightAmount% = 0
	
	Local d.doors
	Local r2.Rooms
	Local i%
	Local xtemp%, ytemp%, ztemp%
	Local ri% = 0
	Local rf# = 0.0
	
	Local t1;, Bump	
	
	Select r\RoomTemplate\Name
		Case "room860"
			;[Block]
			
			d = CreateDoor(False,False)
			d = CreateDoor(True,False)

			d = CreateDoor(False,False)
			d = CreateDoor(False,False)
			
			;the forest
			If I_Zone\HasCustomForest = False Then
				Local fr.Forest = New Forest
				r\fr=fr
				GenForestGrid(fr)
				PlaceForest(fr,r\x,r\y+30.0,r\z,r)
			EndIf
			ri = Rand(360);it = CreateItem("Document SCP-860-1", "paper", r\x + 672.0 * RoomScale, r\y + 176.0 * RoomScale, r\z + 335.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Document SCP-860", "paper", r\x + 1152.0 * RoomScale, r\y + 176.0 * RoomScale, r\z - 384.0 * RoomScale)
			
			lightAmount = 4
			;[EndBlock]
		Case "lockroom"
			;[Block]
			d = CreateDoor(True)
			
			d = CreateDoor(True)
			
			lightAmount = 4 
			;[End Block]
		Case "lockroom2"
			;[Block]
			For i = 0 To 5
				ri = Rand(2, 3);de.Decals = CreateDecal(Rand(2,3), r\x+Rnd(-392,520)*RoomScale, 3.0*RoomScale+Rnd(0,0.001), r\z+Rnd(-392,520)*RoomScale,90,Rnd(360),0)
				rf# = Rnd(-392, 520)
				rf# = Rnd(0, 0.001)
				rf# = Rnd(-392, 520)
				rf# = Rnd(360)
				
				rf# = Rnd(0.3, 0.6); de\Size = Rnd(0.3,0.6)
				
				ri = Rand(15, 16);CreateDecal(Rand(15,16), r\x+Rnd(-392,520)*RoomScale, 3.0*RoomScale+Rnd(0,0.001), r\z+Rnd(-392,520)*RoomScale,90,Rnd(360),0)
				rf# = Rnd(-392, 520)
				rf# = Rnd(0, 0.001)
				rf# = Rnd(-392, 520)
				rf# = Rnd(360)
								
				rf# = Rnd(0.1, 0.6);de\Size = Rnd(0.1,0.6)
				
				ri = Rand(15, 16);CreateDecal(Rand(15,16), r\x+Rnd(-0.5,0.5), 3.0*RoomScale+Rnd(0,0.001), r\z+Rnd(-0.5,0.5),90,Rnd(360),0)				
				rf# = Rnd(-392, 520)
				rf# = Rnd(0, 0.001)
				rf# = Rnd(-392, 520)
				rf# = Rnd(360)

				rf# = Rnd(0.1, 0.6);de\Size = Rnd(0.1,0.6)
			Next
			
			lightAmount = 6
			;[End Block]
		Case "gatea"
			;[Block]	
			d = CreateDoor(False)					
			
			d = CreateDoor(False)					
			
			d = CreateDoor(False, False)
			
			d = CreateDoor(False, False)
			
			For r2.Rooms = Each Rooms
				If r\RoomTemplate\Name = "gateaentrance" Then
					d = CreateDoor(False)
				EndIf
			Next
			
			lightAmount = 12
			;[End Block]
		Case "gateaentrance"
			;[Block]
			d = CreateDoor(True)
			d = CreateDoor(False, True)
			
			lightAmount = 4
			;[End Block]
		Case "exit1"
			;[Block]
			d = CreateDoor(False, True)
			d = CreateDoor(True)
			d = CreateDoor(False)
			d = CreateDoor(False)
			d = CreateDoor(False)
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			lightAmount = 15
			;[End Block]
		Case "roompj"
			;[Block]
			ri = Rand(360);it = CreateItem("Document SCP-372", "paper", r\x + 800.0 * RoomScale, r\y + 176.0 * RoomScale, r\z + 1108.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Radio Transceiver", "radio", r\x + 800.0 * RoomScale, r\y + 112.0 * RoomScale, r\z + 944.0 * RoomScale)
			
			d = CreateDoor(True, True)
			
			lightAmount = 8
			;[End Block]
		Case "room079"
			;[Block]
			d = CreateDoor(False, True)
			rf# = Rnd(360);de.Decals = CreateDecal(3,  r\x + 1184.0*RoomScale, -448.0*RoomScale+0.01, r\z+1792.0*RoomScale,90,Rnd(360),0)
			d = CreateDoor(False, True)
			d = CreateDoor(False, False)
			
			lightAmount = 6
			;[End Block]
		Case "checkpoint1"
			;[Block]
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			If MapTemp(Floor(r\x / 8.0),Floor(r\z /8.0)-1)=0 Then
				d = CreateDoor(False)
			EndIf
			
			lightAmount = 2
			;[End Block]
		Case "checkpoint2"
			;[Block]
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			If MapTemp(Floor(r\x / 8.0),Floor(r\z /8.0)-1)=0 Then
				d = CreateDoor(False)
			EndIf		
			
			lightAmount = 2	
			;[End Block]
		Case "room2pit"
			;[Block]
			lightAmount = 6
			;[End Block]
		Case "room2testroom2"
			;[Block]
			
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			ri = Rand(360);it = CreateItem("Level 2 Key Card", "key2", r\x - 914.0 * RoomScale, r\y + 137.0 * RoomScale, r\z + 61.0 * RoomScale)
			ri = Rand(360);it = CreateItem("S-NAV 300 Navigator", "nav", r\x - 312.0 * RoomScale, r\y + 264.0 * RoomScale, r\z + 176.0 * RoomScale)
			
			lightAmount = 6
			;[End Block]
		Case "room3tunnel"
			;[Block]
			lightAmount = 5
			;[End Block]
		Case "room2toilets"
			;[Block]
			;[End Block]
		Case "room2storage"
			;[Block]
			d = CreateDoor()
			d = CreateDoor()
			d = CreateDoor()
			d = CreateDoor()
			d = CreateDoor()
			d = CreateDoor()
				
			ri = Rand(360);it = CreateItem("Document SCP-939", "paper", r\x + 352.0 * RoomScale, r\y + 176.0 * RoomScale, r\z + 256.0 * RoomScale)
			ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x + 352.0 * RoomScale, r\y + 112.0 * RoomScale, r\z + 448.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Empty Cup", "emptycup", r\x-672*RoomScale, 240*RoomScale, r\z+288.0*RoomScale)
			ri = Rand(360);it = CreateItem("Level 1 Key Card", "key1", r\x - 672.0 * RoomScale, r\y + 240.0 * RoomScale, r\z + 224.0 * RoomScale)
			
			lightAmount = 10
			;[End Block]
		Case "room2sroom"
			;[Block]
			d = CreateDoor(False, False)
			
			ri = Rand(360);it = CreateItem("Some SCP-420-J", "420", r\x + 1776.0 * RoomScale, r\y + 400.0 * RoomScale, r\z + 427.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Some SCP-420-J", "420", r\x + 1808.0 * RoomScale, r\y + 400.0 * RoomScale, r\z + 435.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Level 5 Key Card", "key5", r\x + 2232.0 * RoomScale, r\y + 392.0 * RoomScale, r\z + 387.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Nuclear Device Document", "paper", r\x + 2248.0 * RoomScale, r\y + 440.0 * RoomScale, r\z + 372.0 * RoomScale)
			ri = Rand(360);
			
			lightAmount = 4
			;[End Block]
		Case "room2shaft"
			;[Block]       
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			     
			ri = Rand(360);it = CreateItem("Level 3 Key Card", "key3", r\x + 1119.0 * RoomScale, r\y + 233.0 * RoomScale, r\z + 494.0 * RoomScale)	
			ri = Rand(360);it = CreateItem("First Aid Kit", "firstaid", r\x + 1035.0 * RoomScale, r\y + 145.0 * RoomScale, r\z + 56.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x + 1930.0 * RoomScale, r\y + 97.0 * RoomScale, r\z + 256.0 * RoomScale)
			ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x + 1061.0 * RoomScale, r\y + 161.0 * RoomScale, r\z + 494.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("ReVision Eyedrops", "eyedrops", r\x + 1930.0*RoomScale, r\y + 225.0 * RoomScale, r\z + 128.0*RoomScale)

            rf# = Rnd(360);de.Decals = CreateDecal(3,  r\x + 1334.0*RoomScale, -796.0*RoomScale+0.01, r\z-220.0*RoomScale,90,Rnd(360),0)
			
			lightAmount = 5
			;[End Block]
        Case "room2poffices"
			;[Block]
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			ri = Rand(360);it = CreateItem("Mysterious Note", "paper", r\x + 736.0 * RoomScale, r\y + 224.0 * RoomScale, r\z + 544.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Ballistic Vest", "vest", r\x + 608.0 * RoomScale, r\y + 112.0 * RoomScale, r\z + 32.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Incident Report SCP-106-0204", "paper", r\x + 704.0 * RoomScale, r\y + 183.0 * RoomScale, r\z - 576.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Journal Page", "paper", r\x + 912 * RoomScale, r\y + 176.0 * RoomScale, r\z - 160.0 * RoomScale)
			ri = Rand(360);it = CreateItem("First Aid Kit", "firstaid", r\x + 912.0 * RoomScale, r\y + 112.0 * RoomScale, r\z - 336.0 * RoomScale)
			
			lightAmount = 5
			;[End Block]
		Case "room2poffices2"
			;[Block]
			
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			ri = Rand(360);de.Decals = CreateDecal(0, r\x - 808.0 * RoomScale, 0.005, r\z - 72.0 * RoomScale, 90, Rand(360), 0)

			ri = Rand(360);de.Decals = CreateDecal(2, r\x - 808.0 * RoomScale, 0.01, r\z - 72.0 * RoomScale, 90, Rand(360), 0)
			
			ri = Rand(360);de.Decals = CreateDecal(0, r\x - 432.0 * RoomScale, 0.01, r\z, 90, Rand(360), 0)
			
			ri = Rand(360);it = CreateItem("Dr. L's Burnt Note", "paper", r\x - 688.0 * RoomScale, 1.0, r\z - 16.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Dr L's Burnt Note", "paper", r\x - 808.0 * RoomScale, 1.0, r\z - 72.0 * RoomScale)
			ri = Rand(360);it = CreateItem("The Modular Site Project", "paper", r\x + 622.0*RoomScale, r\y + 125.0*RoomScale, r\z - 73.0*RoomScale)
			
			lightAmount = 4
			;[End Block]
		Case "room2elevator"
			;[Block]
			d = CreateDoor(False)
			
			lightAmount = 3
			;[End Block]
		Case "room2cafeteria"
			;[Block]
			ri = Rand(360);it = CreateItem("cup", "cup", r\x-508.0*RoomScale, -187*RoomScale, r\z+284.0*RoomScale, 240,175,70)			
			ri = Rand(360);it = CreateItem("cup", "cup", r\x+1412 * RoomScale, -187*RoomScale, r\z-716.0 * RoomScale, 87,62,45)			
			ri = Rand(360);it = CreateItem("Empty Cup", "emptycup", r\x-540*RoomScale, -187*RoomScale, r\z+124.0*RoomScale)			
			ri = Rand(360);it = CreateItem("Quarter", "25ct", r\x-447.0*RoomScale, r\y-334.0*RoomScale, r\z+36.0*RoomScale)
			ri = Rand(360);it = CreateItem("Quarter", "25ct", r\x+1409.0*RoomScale, r\y-334.0*RoomScale, r\z-732.0*RoomScale)
			
			lightAmount = 7
			;[End Block]
		Case "room2nuke"
			;[Block]
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			d = CreateDoor(True)
			d = CreateDoor(False)
			
			ri = Rand(360);it = CreateItem("Nuclear Device Document", "paper", r\x - 768.0 * RoomScale, r\y + 1684.0 * RoomScale, r\z - 768.0 * RoomScale)		
			ri = Rand(360);it = CreateItem("Ballistic Vest", "vest", r\x - 944.0 * RoomScale, r\y + 1652.0 * RoomScale, r\z - 656.0 * RoomScale)
			
			lightAmount = 14
			;[End Block]
		Case "room2tunnel"
			;[Block]			
			d = CreateDoor(True)
			d = CreateDoor(True)
			d = CreateDoor(True, False)
			
			ri = Rand(360);de.Decals = CreateDecal(0, r\x + 64.0 * RoomScale, 0.005, r\z + 144.0 * RoomScale, 90, Rand(360), 0)

			ri = Rand(360);it = CreateItem("Scorched Note", "paper", r\x + 64.0 * RoomScale, r\y +144.0 * RoomScale, r\z - 384.0 * RoomScale)
			
			lightAmount = 4
			;[End Block]
		Case "008"
			;[Block]
			d = CreateDoor(True)
			d = CreateDoor(False)
			d = CreateDoor(False)
			
			ri = Rand(360);it = CreateItem("Hazmat Suit", "hazmatsuit", r\x - 76.0 * RoomScale, 0.5, r\z - 396.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Document SCP-008", "paper", r\x - 245.0 * RoomScale, r\y + 192.0 * RoomScale, r\z + 368.0 * RoomScale)
			
			lightAmount = 5
			;[End Block]
		Case "room035"
			;[Block]
			d = CreateDoor(True)
			d = CreateDoor(False)
			d = CreateDoor(False)
			d = CreateDoor(False)
			
			ri = Rand(360);it = CreateItem("SCP-035 Addendum", "paper", r\x + 248.0 * RoomScale, r\y + 220.0 * RoomScale, r\z + 576.0 * RoomScale)		
			ri = Rand(360);it = CreateItem("Radio Transceiver", "radio", r\x - 544.0 * RoomScale, 0.5, r\z + 704.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("SCP-500-01", "scp500", r\x + 1168*RoomScale, 224*RoomScale, r\z+576*RoomScale)			
			ri = Rand(360);it = CreateItem("Metal Panel", "scp148", r\x - 360 * RoomScale, 0.5, r\z + 644 * RoomScale)			
			ri = Rand(360);it = CreateItem("Document SCP-035", "paper", r\x + 1168.0 * RoomScale, 104.0 * RoomScale, r\z + 608.0 * RoomScale)
			
			lightAmount = 5
			;[End Block]
		Case "room513"
			;[Block]
			d = CreateDoor(False)
			
			ri = Rand(360);it = CreateItem("SCP-513", "scp513", r\x - 32.0 * RoomScale, r\y + 196.0 * RoomScale, r\z + 688.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Blood-stained Note", "paper", r\x + 736.0 * RoomScale,1.0, r\z + 48.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Document SCP-513", "paper", r\x - 480.0 * RoomScale, 104.0*RoomScale, r\z - 176.0 * RoomScale)
			
			lightAmount = 6
			;[End Block]
		Case "room966"
			;[Block]
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			ri = Rand(360);it = CreateItem("Night Vision Goggles", "nvgoggles", r\x + 320.0 * RoomScale, 0.5, r\z + 704.0 * RoomScale)
			
			lightAmount = 5
			;[End Block]
		Case "room3storage"
			;[Block]		
			d = CreateDoor(True)
			d = CreateDoor(False)
			d = CreateDoor(True)	
			d = CreateDoor(False)
			
			ri = Rand(3)
			;Select Rand(3)
			;	Case 1
			;		x# = 2312
			;		z#=-952
			;	Case 2
			;		x# = 3032
			;		z#=1288
			;	Case 3
			;		x# = 2824
			;		z#=2808
			;End Select					
			
			ri = Rand(360);it.Items = CreateItem("Black Severed Hand", "hand2", r\x + x*RoomScale, -5596.0*RoomScale+1.0, r\z+z*RoomScale)			
			ri = Rand(360);it = CreateItem("Night Vision Goggles", "nvgoggles", r\x + 1936.0 * RoomScale, r\y - 5496.0 * RoomScale, r\z - 944.0 * RoomScale)
			
			rf# = Rnd(360);de.Decals = CreateDecal(3,  r\x + x*RoomScale, -5632.0*RoomScale+0.01, r\z+z*RoomScale,90,Rnd(360),0)
			
			d = CreateDoor(False)
					
			d = CreateDoor(False)
						
			d = CreateDoor(False)
						
			d = CreateDoor(False)
			
			lightAmount = 32
			;[End Block]
		Case "room049"
			;[Block]
			d = CreateDoor(True)
			d = CreateDoor(False)
			d = CreateDoor(True)
			d = CreateDoor(False)
			
			d = CreateDoor(False)
			d = CreateDoor(False)
			d = CreateDoor(False)
			
			d = CreateDoor(False)
			
			ri = Rand(360);it = CreateItem("Document SCP-049", "paper", r\x - 608.0 * RoomScale, r\y - 3332.0 * RoomScale, r\z + 876.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Level 4 Key Card", "key4", r\x - 512.0 * RoomScale, r\y - 3412.0 * RoomScale, r\z + 864.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("First Aid Kit", "firstaid", r\x +385.0 * RoomScale, r\y - 3412.0 * RoomScale, r\z + 271.0 * RoomScale)
			
			d = CreateDoor(True, True)
			
			d = CreateDoor(False)
			d = CreateDoor(False)
			
			lightAmount = 26
			;[End Block]
		Case "room2_2"
			;[Block]
			;[End Block]
		Case "room012"
			;[Block]
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			ri = Rand(360);it = CreateItem("Document SCP-012", "paper", r\x - 56.0 * RoomScale, r\y - 576.0 * RoomScale, r\z - 408.0 * RoomScale)			
			ri = Rand(360);it.Items = CreateItem("Severed Hand", "hand", r\x - 784*RoomScale, -576*RoomScale+0.3, r\z+640*RoomScale)
						
			rf = Rnd(0, 1);de.Decals = CreateDecal(3,  r\x - 784*RoomScale, -768*RoomScale+0.01, r\z+640*RoomScale,90,Rnd(360),0)
			
			lightAmount = 4
			;[End Block]
		Case "tunnel2"
			;[Block]
			lightAmount = 5
			;[End Block]
		Case "room2pipes"
			;[Block]
			lightAmount = 6
			;[End Block]
		Case "room3pit"
			;[Block]
			lightAmount = 12
			;[End Block]
		Case "room2servers"
			;[Block]
			d = CreateDoor(False)
			
			d = CreateDoor(True, False)
			d = CreateDoor(True, False)
			d = CreateDoor(False, False)
			
			lightAmount = 5
			;[End Block]
		Case "room3servers"
			;[Block]			
			ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x - 132.0 * RoomScale, r\y - 368.0 * RoomScale, r\z - 648.0 * RoomScale)

			If Rand(2) = 1 Then
				ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x - 76.0 * RoomScale, r\y - 368.0 * RoomScale, r\z - 648.0 * RoomScale)
			EndIf
			
			If Rand(2) = 1 Then
				ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x - 196.0 * RoomScale, r\y - 368.0 * RoomScale, r\z - 648.0 * RoomScale)
			EndIf
			
			ri = Rand(360);it = CreateItem("S-NAV 300 Navigator", "nav", r\x + 124.0 * RoomScale, r\y - 368.0 * RoomScale, r\z - 648.0 * RoomScale)					
			
			lightAmount = 6
			;[End Block]
		Case "room3servers2"
			;[Block]	
			ri = Rand(360);it = CreateItem("Document SCP-970", "paper", r\x + 960.0 * RoomScale, r\y - 448.0 * RoomScale, r\z + 251.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Gas Mask", "gasmask", r\x + 954.0 * RoomScale, r\y - 504.0 * RoomScale, r\z + 235.0 * RoomScale)
			
			lightAmount = 6
			;[End Block]
		Case "testroom"
			;[Block]
			d = CreateDoor(False)
			d = CreateDoor(True)
			
			ri = Rand(360);it = CreateItem("Document SCP-682", "paper", r\x + 656.0 * RoomScale, r\y - 1200.0 * RoomScale, r\z - 16.0 * RoomScale)
			
			lightAmount = 23
			;[End Block]
		Case "room2closets"
			;[Block]
			
			ri = Rand(360);it = CreateItem("Document SCP-1048", "paper", r\x + 736.0 * RoomScale, r\y + 176.0 * RoomScale, r\z + 736.0 * RoomScale)						
			ri = Rand(360);it = CreateItem("Gas Mask", "gasmask", r\x + 736.0 * RoomScale, r\y + 176.0 * RoomScale, r\z + 544.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x + 736.0 * RoomScale, r\y + 176.0 * RoomScale, r\z - 448.0 * RoomScale)
			
			If Rand(2) = 1 Then
				ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x + 730.0 * RoomScale, r\y + 176.0 * RoomScale, r\z - 496.0 * RoomScale)
			EndIf
			
			If Rand(2) = 1 Then
				ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x + 740.0 * RoomScale, r\y + 176.0 * RoomScale, r\z - 560.0 * RoomScale)			
			EndIf
			
			ri = Rand(360);it = CreateItem("Level 1 Key Card", "key1", r\x + 736.0 * RoomScale, r\y + 240.0 * RoomScale, r\z + 752.0 * RoomScale)	
			ri = Rand(360);Local clipboard.Items = CreateItem("Clipboard","clipboard",r\x + 736.0 * RoomScale, r\y + 224.0 * RoomScale, r\z -480.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Incident Report SCP-1048-A", "paper",r\x + 736.0 * RoomScale, r\y + 224.0 * RoomScale, r\z -480.0 * RoomScale)			
			
			d = CreateDoor(False)
			
			lightAmount = 6
			;[End Block]
		Case "room2offices"
			;[Block]
			ri = Rand(360);it = CreateItem("Document SCP-106", "paper", r\x + 404.0 * RoomScale, r\y + 145.0 * RoomScale, r\z + 559.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Level 2 Key Card", "key2", r\x - 156.0 * RoomScale, r\y + 151.0 * RoomScale, r\z + 72.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("S-NAV 300 Navigator", "nav", r\x + 305.0 * RoomScale, r\y + 153.0 * RoomScale, r\z + 944.0 * RoomScale)		
			ri = Rand(360);it = CreateItem("Notification", "paper", r\x -137.0 * RoomScale, r\y + 153.0 * RoomScale, r\z + 464.0 * RoomScale)
			
			lightAmount = 4
			;[End Block]
		Case "room2offices2"
			;[Block]
			
			ri = Rand(360);it = CreateItem("Level 1 Key Card", "key1", r\x - 368.0 * RoomScale, r\y - 48.0 * RoomScale, r\z + 80.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Document SCP-895", "paper", r\x - 800.0 * RoomScale, r\y - 48.0 * RoomScale, r\z + 368.0 * RoomScale)
			
			If Rand(2) = 1 Then
				ri = Rand(360);it = CreateItem("Document SCP-860", "paper", r\x - 800.0 * RoomScale, r\y - 48.0 * RoomScale, r\z - 464.0 * RoomScale)
			Else
				ri = Rand(360);it = CreateItem("SCP-093 Recovered Materials", "paper", r\x - 800.0 * RoomScale, r\y - 48.0 * RoomScale, r\z - 464.0 * RoomScale)
			EndIf
			
			ri = Rand(360);it = CreateItem("S-NAV 300 Navigator", "nav", r\x - 336.0 * RoomScale, r\y - 48.0 * RoomScale, r\z - 480.0 * RoomScale)
			
			ri = Rand(1, 4);temp = Rand(1,4)
			;[End Block]
		Case "room2offices3"
			;[Block]
			
			If Rand(2)=1 Then 
				ri = Rand(360);it = CreateItem("Mobile Task Forces", "paper", r\x + 744.0 * RoomScale, r\y +240.0 * RoomScale, r\z + 944.0 * RoomScale)
			Else
				ri = Rand(360);it = CreateItem("Security Clearance Levels", "paper", r\x + 680.0 * RoomScale, r\y +240.0 * RoomScale, r\z + 944.0 * RoomScale)
			EndIf
			
			ri = Rand(360);it = CreateItem("Object Classes", "paper", r\x + 160.0 * RoomScale, r\y +240.0 * RoomScale, r\z + 568.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Document", "paper", r\x -1440.0 * RoomScale, r\y +624.0 * RoomScale, r\z + 152.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Radio Transceiver", "radio", r\x - 1184.0 * RoomScale, r\y + 480.0 * RoomScale, r\z - 800.0 * RoomScale)
			
			For i = 0 To Rand(0,1)
				ri = Rand(360);it = CreateItem("ReVision Eyedrops", "eyedrops", r\x - 1529.0*RoomScale, r\y + 563.0 * RoomScale, r\z - 572.0*RoomScale + i*0.05)
			Next
			
			ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x - 1545.0 * RoomScale, r\y + 603.0 * RoomScale, r\z - 372.0 * RoomScale)
			
			If Rand(2) = 1 Then
				ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x - 1540.0 * RoomScale, r\y + 603.0 * RoomScale, r\z - 340.0 * RoomScale)
			EndIf
			
			If Rand(2) = 1 Then
				ri = Rand(360);it = CreateItem("9V Battery", "bat", r\x - 1529.0 * RoomScale, r\y + 603.0 * RoomScale, r\z - 308.0 * RoomScale)
			EndIf

			d = CreateDoor(True)

			;[End Block]
		Case "start"
			;[Block]
			
			d = CreateDoor(True, True)
			
			d = CreateDoor(False)
			
			d = CreateDoor(True)
			d = CreateDoor(False)
			d = CreateDoor(True)
			d = CreateDoor(False)
			
			ri = Rand(360);de.Decals = CreateDecal(0, r\x + 272.0 * RoomScale, 0.005, r\z + 262.0 * RoomScale, 90, Rand(360), 0)
			
			ri = Rand(360);de.Decals = CreateDecal(0, r\x + 456.0 * RoomScale, 0.005, r\z + 135.0 * RoomScale, 90, Rand(360), 0)

			lightAmount = 9

			;[End Block]
		Case "room2scps"
			;[Block]
			
			d = CreateDoor(True, False)
			d = CreateDoor(True, False)
			
			d = CreateDoor(True, False)
			d = CreateDoor(True, False)
			d = CreateDoor(True, False)
			d = CreateDoor(True, False)
			
			ri = Rand(360);it = CreateItem("SCP-714", "scp714", r\x - 552.0 * RoomScale, r\y + 220.0 * RoomScale, r\z - 760.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("SCP-1025", "scp1025", r\x + 552.0 * RoomScale, r\y + 224.0 * RoomScale, r\z - 758.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("SCP-860", "scp860", r\x + 568.0 * RoomScale, r\y + 178.0 * RoomScale, r\z + 760.0 * RoomScale)
			
			ri = Rand(360);it = CreateItem("Document SCP-714", "paper", r\x - 728.0 * RoomScale, r\y + 288.0 * RoomScale, r\z - 360.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Document SCP-427", "paper", r\x - 608.0 * RoomScale, r\y + 66.0 * RoomScale, r\z + 636.0 * RoomScale)			

			For i = 0 To 14
				ri = Rand(15,16);de.Decals = CreateDecal(Rand(15,16),r\x+dx#*RoomScale,0.005,r\z+dz#*RoomScale,90,Rand(360),0)
				ri = Rand(360)
				
				If i > 10 Then
					rf# = Rnd(0.2,0.25)
				Else
					rf# = Rnd(0.1,0.17)
				EndIf
			Next
				
				
			lightAmount = 6		
			;[End Block]
		Case "room205"
			;[Block]	
			d = CreateDoor(True, False)
			d = CreateDoor(True, False)
			
			lightAmount = 5
			;[End Block]
		Case "endroom"
			;[Block]
			d = CreateDoor(False, True)
			
			lightAmount = 3
			;[End Block]
		Case "endroomc"
			;[Block]
			d = CreateDoor(False)
			;[End Block]
		Case "coffin"
			;[Block]
			
			d = CreateDoor(False, True)
			
			ri = Rand(360);it = CreateItem("Document SCP-895", "paper", r\x - 688.0 * RoomScale, r\y + 133.0 * RoomScale, r\z - 304.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Level 3 Key Card", "key3", r\x + 240.0 * RoomScale, r\y -1456.0 * RoomScale, r\z + 2064.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Night Vision Goggles", "nvgoggles", r\x + 280.0 * RoomScale, r\y -1456.0 * RoomScale, r\z + 2164.0 * RoomScale)
			
			lightAmount = 9
			;[End Block]
		Case "room2tesla","room2tesla_lcz","room2tesla_hcz"		
			;[Block]
			If r\RoomTemplate\Name = "room2tesla_lcz" Then
				lightAmount = 8
			ElseIf r\RoomTemplate\Name = "room2tesla_hcz" Then
				lightAmount = 10
			EndIf
			;NEED TO ADD ROOM2TESLA LIGHT AMOUNT IN HERE
			;[End Block]
		Case "room2doors"
			;[Block]
			d = CreateDoor(True)
			
			d = CreateDoor(True)
			
			lightAmount = 5
			;[End Block]
		Case "914"
			;[Block]
			
			d = CreateDoor(False, True)
			
			d = CreateDoor(True)
			d = CreateDoor(True)
			
			ri = Rand(360);it = CreateItem("Addendum: 5/14 Test Log", "paper", r\x +954.0 * RoomScale, r\y +228.0 * RoomScale, r\z + 127.0 * RoomScale)
			ri = Rand(360);it = CreateItem("First Aid Kit", "firstaid", r\x + 960.0 * RoomScale, r\y + 112.0 * RoomScale, r\z - 40.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Dr. L's Note", "paper", r\x - 928.0 * RoomScale, 160.0 * RoomScale, r\z - 160.0 * RoomScale)
			
			lightAmount = 9
			;[End Block]
		Case "173"
			;[Block]
			
			d = CreateDoor(False, True)
			
			ri = Rand(4, 5);de.Decals = CreateDecal(Rand(4, 5), EntityX(r\Objects[5], True), 0.002, EntityZ(r\Objects[5], True), 90, Rnd(360), 0)
			rf# = Rnd(360)
														
			For xtemp% = 0 To 1
				For ztemp% = 0 To 1
					ri = Rand(4,6);de.Decals = CreateDecal(Rand(4, 6), r\x + 700.0 * RoomScale + xtemp * 700.0 * RoomScale + Rnd(-0.5, 0.5), Rnd(0.001, 0.0018), r\z + 600 * ztemp * RoomScale + Rnd(-0.5, 0.5), 90, Rnd(360), 0)
					rf# = Rnd(-0.5, 0.5)
					rf# = Rnd(0.001, 0.0018)
					rf# = Rnd(-0.5, 0.5)
					rf# = Rnd(360)
				Next
			Next
			
			d = CreateDoor(True, False)
			
			d = CreateDoor(True)
			d = CreateDoor(True)
			d = CreateDoor(True)
			
			d = CreateDoor(False)
			d = CreateDoor(True)
			d = CreateDoor(False)
			d = CreateDoor(False)
			
			For ztemp = 0 To 1
				d = CreateDoor(False) 
				
				d = CreateDoor(False)
				
				For xtemp = 0 To 2
					d = CreateDoor(False)
				Next					
				
				For xtemp = 0 To 4
					d = CreateDoor(False)
				Next	
			Next
			
			ri = Rand(360);CreateItem("Class D Orientation Leaflet", "paper", r\x-(2914+1024)*RoomScale, 170.0*RoomScale, r\z+40*RoomScale)			
			;[End Block]
		Case "room2ccont"
			;[Block]
			
			d = CreateDoor(False, False)
			
			ri = Rand(360);it = CreateItem("Note from Daniel", "paper", r\x-400.0*RoomScale,1040.0*RoomScale,r\z+115.0*RoomScale)
			
			lightAmount = 9
			;[End Block]
		Case "room106"
			;[Block]
			ri = Rand(360);it = CreateItem("Level 5 Key Card", "key5", r\x - 752.0 * RoomScale, r\y - 592 * RoomScale, r\z + 3026.0 * RoomScale)						
			ri = Rand(360);it = CreateItem("Dr. Allok's Note", "paper", r\x - 416.0 * RoomScale, r\y - 576 * RoomScale, r\z + 2492.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Recall Protocol RP-106-N", "paper", r\x + 268.0 * RoomScale, r\y - 576 * RoomScale, r\z + 2593.0 * RoomScale)			
			
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			lightAmount = 14
			;[End Block]
		Case "room1archive"
			;[Block]
			For xtemp = 0 To 1
				For ytemp = 0 To 2
					For ztemp = 0 To 2
						chance% = Rand(-10,100)
						Select True
							Case (chance<0)
								Exit
							Case (chance<40) ;40% chance for a document
								ri = Rand(1,6);Select Rand(1,6)
							Case (chance>=40) And (chance<45) ;5% chance for a key card
								temp3%=Rand(1,2)
							Case (chance>=95) And (chance=<100) ;5% chance for misc
								temp3%=Rand(1,3)
						End Select
						
						rf# = Rnd(-96.0, 96.0);z# = (480.0 - 352.0*ztemp + Rnd(-96.0,96.0)) * RoomScale

						
						ri = Rand(360);it = CreateItem(tempstr,tempstr2,r\x+x,y,r\z+z)
					Next
				Next
			Next	
			
			d = CreateDoor(False, False)
				
			lightAmount = 2
			;[End Block]
		Case "room2test1074"
			;[Block]
			
			d = CreateDoor(False, False)
			d = CreateDoor(True, False)
			d = CreateDoor(True, False)
			d = CreateDoor(False, False)
			
			ri = Rand(360);it = CreateItem("Document SCP-1074","paper",r\x + 300.0 * RoomScale,r\y+20.0*RoomScale,r\z + 671.0*RoomScale)
			;[End Block]
		Case "room1123"
			;[Block]
			ri = Rand(360);it = CreateItem("Document SCP-1123", "paper", r\x + 511.0 * RoomScale, r\y + 125.0 * RoomScale, r\z - 936.0 * RoomScale)					
			ri = Rand(360);it = CreateItem("SCP-1123", "1123", r\x + 832.0 * RoomScale, r\y + 166.0 * RoomScale, r\z + 784.0 * RoomScale)			
			ri = Rand(360);it = CreateItem("Leaflet", "paper", r\x - 816.0 * RoomScale, r\y + 704.0 * RoomScale, r\z+ 888.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Gas Mask", "gasmask", r\x + 457.0 * RoomScale, r\y + 150.0 * RoomScale, r\z + 960.0 * RoomScale)
			
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			lightAmount = 20
			;[End Block]
		Case "pocketdimension"
			;[Block]		
			
			ri = Rand(360);CreateItem("Burnt Note", "paper", EntityX(r\obj),0.5,EntityZ(r\obj)+3.5)
			
			d = CreateDoor(False)
			d = CreateDoor(False)
			
			For i = 1 To 8
				If i < 6 Then 
					rf# = Rnd(0.5 , 0.5);de\Size = Rnd(0.5, 0.5)
				EndIf				
			Next
			
			lightAmount = 1
			;[End Block]
		Case "room3z3"
			;[Block]
			;[End Block]
		Case "room2_3","room3_3"
			;[Block]
			If r\RoomTemplate\Name = "room3_3" Then
				lightAmount = 5
			EndIf 
			;[End Block]
		;New rooms (in SCP:CB 1.3) - ENDSHN
		Case "room1lifts"
			;[Block]
			lightAmount = 1
			;[End Block]
		Case "room2servers2"
			;[Block]
			
			d = CreateDoor(False, False)
			
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			ri = Rand(360);it = CreateItem("Night Vision Goggles", "nvgoggles", r\x + 56.0154 * RoomScale, r\y - 648.0 * RoomScale, r\z + 749.638 * RoomScale)
			
			ri = Rand(245);RotateEntity it\collider, 0, r\angle+Rand(245), 0		
			
			lightAmount = 4
			;[End Block]
		Case "room2gw","room2gw_b"
		    ;[Block]
			If r\RoomTemplate\Name = "room2gw_b"
				
				rf# = Rnd(360);de.Decals = CreateDecal(3,  r\x - 156.825*RoomScale, -37.3458*RoomScale, r\z+121.364*RoomScale,90,Rnd(360),0)
				
				lightAmount = 2
				;Amogus()
			EndIf
			
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			If r\RoomTemplate\Name = "room2gw"
				
				ri = room2gw_brokendoor = 0 And Rand(0, 1)
				;If (room2gw_brokendoor = 0 And Rand(1,2)=1) Or bd_temp%
				;	r\Objects[1] = CopyEntity(DoorOBJ)
				;	ScaleEntity(r\Objects[1], (204.0 * RoomScale) / MeshWidth(r\Objects[1]), 312.0 * RoomScale / MeshHeight(r\Objects[1]), 16.0 * RoomScale / MeshDepth(r\Objects[1]))
				;	EntityType r\Objects[1], HIT_MAP
				;	PositionEntity r\Objects[1], r\x + 336.0 * RoomScale, 0.0, r\z + 462.0 * RoomScale
				;	RotateEntity(r\Objects[1], 0, 180 + 180, 0)
				;	EntityParent(r\Objects[1], r\obj)
				;	MoveEntity r\Objects[1],120.0,0,5.0
				;	room2gw_brokendoor = True
				;	room2gw_x# = r\x
				;	room2gw_z# = r\z
				;	FreeEntity r\RoomDoors[1]\obj2 : r\RoomDoors[1]\obj2 = 0
				;EndIf
				
				;NEED TO ADD LIGHT AMOUNT FOR ROOM2GW
				
			EndIf
			
			;[End Block]
		Case "room3gw"
	        ;[Block]
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
	        ;[End Block]
		Case "room1162"
			;[Block]
			
			d = CreateDoor(False, False)
			
			ri = Rand(360);it = CreateItem("Document SCP-1162", "paper", r\x + 863.227 * RoomScale, r\y + 152.0 * RoomScale, r\z - 953.231 * RoomScale)
			
			lightAmount = 5
			;[End Block]
		Case "room2scps2"
			;[Block]
			
			d = CreateDoor(False, False)
			
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			
			ri = Rand(360);it = CreateItem("SCP-1499", "scp1499", r\x + 600.0 * RoomScale, r\y + 176.0 * RoomScale, r\z - 228.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Document SCP-1499", "paper", r\x + 840.0 * RoomScale, r\y + 260.0 * RoomScale, r\z + 224.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Document SCP-500", "paper", r\x + 1152.0 * RoomScale, r\y + 224.0 * RoomScale, r\z + 336.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Emily Ross' Badge", "badge", r\x + 364.0 * RoomScale, r\y + 5.0 * RoomScale, r\z + 716.0 * RoomScale)
			
			lightAmount = 6
			;[End Block]
		Case "room3offices"
			;[Block]		
			d = CreateDoor(False, False)	
			
			lightAmount = 1
			;[End Block]
		Case "room2offices4"
			;[Block]
			
			d = CreateDoor(False)
			
			ri = Rand(360);it = CreateItem("Sticky Note", "paper", r\x - 991.0*RoomScale, r\y - 242.0*RoomScale, r\z + 904.0*RoomScale)

			lightAmount = 5
			;[End Block]
		Case "room2sl"
			;[Block]
			d = CreateDoor(False, False)
			d = CreateDoor(False, False)
			d = CreateDoor()
			
			lightAmount = 7
			;[End Block]
		Case "room2_4"
			;[Block]
			lightAmount = 3
			;[End Block]
		Case "room3z2"
			;[Block]
			lightAmount = 3
			;[End Block]
		Case "lockroom3"
			;[Block]
			d = CreateDoor(True)
			d = CreateDoor(True)
			;[End Block]
		Case "medibay"
			;[Block]
			ri = Rand(360);it = CreateItem("First Aid Kit", "firstaid", r\x - 506.0 * RoomScale, r\y + 192.0 * RoomScale, r\z - 322.0 * RoomScale)
			ri = Rand(360);it = CreateItem("Syringe", "syringe", r\x - 333.0 * RoomScale, r\y + 100.0 * RoomScale, r\z + 97.3 * RoomScale)
			ri = Rand(360);it = CreateItem("Syringe", "syringe", r\x - 340.0 * RoomScale, r\y + 100.0 * RoomScale, r\z + 52.3 * RoomScale)
			
			d = CreateDoor(False, False)
			
			lightAmount = 6
			;[End Block]
		Case "room2cpit"
			;[Block]
			
			d = CreateDoor(False)
			
			ri = Rand(360);it = CreateItem("Dr L's Note", "paper", r\x - 160.0 * RoomScale, 32.0 * RoomScale, r\z - 353.0 * RoomScale)
			
			lightAmount = 10
			;[End Block]
		Case "dimension1499"
			;[Block]
			lightAmount = 0
			;[End Block]
		Case "room3"
			;[Block]
			lightAmount = 4
			;[EndBlock]
		Case "room3_2"
			;[Block]
			lightAmount = 3
			;[EndBlock]
		Case "room2"
			;[Block]
			lightAmount = 2
			;[EndBlock]
		Case "room2_5"
			;[Block]
			lightAmount = 3
			;[EndBlock]
		Case "room4info"
			;[Block]
			lightAmount = 5
			;[EndBlock]
		Case "room2pipes2"
			;[Block]
			lightAmount = 6
			;[EndBlock]
		Case "tunnel"
			;[Block]
			lightAmount = 4
			;[EndBlock]
		Case "room4tunnels"
			;[Block]
			lightAmount = 6
			;[EndBlock]
		Case "room4pit"
			;[Block]
			lightAmount = 16
			;[EndBlock]
		Case "room3poffices"
			;[Block]
			lightAmount = 5
			;[EndBlock]
		Case "room4z3"
			;[Block]
			lightAmount = 2
			;[EndBlock]
	End Select	
	
	For lights = 0 To lightAmount-1
		newlt = AddLight(r)
	Next
	
	lightAmount = 0
	
	DebugLog "GETTING RANDOM FLOAT AT END OF FILLROOM " + r\RoomTemplate\Name + ": " + Rnd(0,100000) + "++++++++++++++++++++++++++++++++++++++++"
End Function

Function CalculateRoomExtents(r.Rooms)
	If r\RoomTemplate\DisableOverlapCheck Then Return
	
	;shrink the extents slightly - we don't care if the overlap is smaller than the thickness of the walls
	Local shrinkAmount# = 0.05
	
	;convert from the rooms local space to world space
	TFormVector(r\RoomTemplate\MinX, r\RoomTemplate\MinY, r\RoomTemplate\MinZ, r\obj, 0)
	r\MinX = TFormedX() + shrinkAmount + r\x
	r\MinY = TFormedY() + shrinkAmount
	r\MinZ = TFormedZ() + shrinkAmount + r\z
	
	;convert from the rooms local space to world space
	TFormVector(r\RoomTemplate\MaxX, r\RoomTemplate\MaxY, r\RoomTemplate\MaxZ, r\obj, 0)
	r\MaxX = TFormedX() - shrinkAmount + r\x
	r\MaxY = TFormedY() - shrinkAmount
	r\MaxZ = TFormedZ() - shrinkAmount + r\z
	
	If (r\MinX > r\MaxX) Then
		Local tempX# = r\MaxX
		r\MaxX = r\MinX
		r\MinX = tempX
	EndIf
	If (r\MinZ > r\MaxZ) Then
		Local tempZ# = r\MaxZ
		r\MaxZ = r\MinZ
		r\MinZ = tempZ
	EndIf
	
	DebugLog("roomextents: "+r\MinX+", "+r\MinY	+", "+r\MinZ	+", "+r\MaxX	+", "+r\MaxY+", "+r\MaxZ)
End Function

Function GenForestGrid(fr.Forest)
	CatchErrors("Uncaught (GenForestGrid)")
	fr\ID=LastForestID+1
	LastForestID=LastForestID+1
	
	Local door1_pos%,door2_pos%
	Local i%,j%
	door1_pos=Rand(3,7)
	door2_pos=Rand(3,7)
	
	;clear the grid
	For i=0 To gridsize-1
		For j=0 To gridsize-1
			fr\grid[(j*gridsize)+i]=0
		Next
	Next
	
	;set the position of the concrete and doors
	;For i=0 To gridsize-1
	;	fr\grid[i]=2
	;	fr\grid[((gridsize-1)*gridsize)+i]=2
	;Next
	fr\grid[door1_pos]=3
	fr\grid[((gridsize-1)*gridsize)+door2_pos]=3
	
	;generate the path
	Local pathx = door2_pos
	Local pathy = 1
	Local dir = 1 ;0 = left, 1 = up, 2 = right
	fr\grid[((gridsize-1-pathy)*gridsize)+pathx] = 1
	
	Local deviated%
	
	While pathy < gridsize -4
		If dir = 1 Then ;determine whether to go forward or to the side
			If chance(deviation_chance) Then
				;pick a branch direction
				dir = 2 * Rand(0,1)
				;make sure you have not passed max side distance
				dir = turn_if_deviating(max_deviation_distance,pathx,center,dir)
				deviated = turn_if_deviating(max_deviation_distance,pathx,center,dir,1)
				If deviated Then fr\grid[((gridsize-1-pathy)*gridsize)+pathx]=1
				pathx=move_forward(dir,pathx,pathy)
				pathy=move_forward(dir,pathx,pathy,1)
			EndIf
			
		Else
			;we are going to the side, so determine whether to keep going or go forward again
			dir = turn_if_deviating(max_deviation_distance,pathx,center,dir)
			deviated = turn_if_deviating(max_deviation_distance,pathx,center,dir,1)
			If deviated Or chance(return_chance) Then dir = 1
			
			pathx=move_forward(dir,pathx,pathy)
			pathy=move_forward(dir,pathx,pathy,1)
			;if we just started going forward go twice so as to avoid creating a potential 2x2 line
			If dir=1 Then
				fr\grid[((gridsize-1-pathy)*gridsize)+pathx]=1
				pathx=move_forward(dir,pathx,pathy)
				pathy=move_forward(dir,pathx,pathy,1)
			EndIf
		EndIf
		
		;add our position to the grid
		fr\grid[((gridsize-1-pathy)*gridsize)+pathx]=1
		
	Wend
	;finally, bring the path back to the door now that we have reached the end
	dir = 1
	While pathy < gridsize-2
		pathx=move_forward(dir,pathx,pathy)
		pathy=move_forward(dir,pathx,pathy,1)
		fr\grid[((gridsize-1-pathy)*gridsize)+pathx]=1
	Wend
	
	If pathx<>door1_pos Then
		dir=0
		If door1_pos>pathx Then dir=2
		While pathx<>door1_pos
			pathx=move_forward(dir,pathx,pathy)
			pathy=move_forward(dir,pathx,pathy,1)
			fr\grid[((gridsize-1-pathy)*gridsize)+pathx]=1
		Wend
	EndIf
	
	;attempt to create new branches
	Local new_y%,temp_y%,new_x%
	Local branch_type%,branch_pos%
	new_y=-3 ;used for counting off; branches will only be considered once every 4 units so as to avoid potentially too many branches
	While new_y<gridsize-6
		new_y=new_y+4
		temp_y=new_y
		new_x=0
		If chance(branch_chance) Then
			branch_type=-1
			If chance(cobble_chance) Then
				branch_type=-2
			EndIf
			;create a branch at this spot
			;determine if on left or on right
			branch_pos=2*Rand(0,1)
			;get leftmost or rightmost path in this row
			leftmost=gridsize
			rightmost=0
			For i=0 To gridsize
				If fr\grid[((gridsize-1-new_y)*gridsize)+i]=1 Then
					If i<leftmost Then leftmost=i
					If i>rightmost Then rightmost=i
				EndIf
			Next
			If branch_pos=0 Then new_x=leftmost-1 Else new_x=rightmost+1
			;before creating a branch make sure there are no 1's above or below
			If (temp_y<>0 And fr\grid[((gridsize-1-temp_y+1)*gridsize)+new_x]=1) Or fr\grid[((gridsize-1-temp_y-1)*gridsize)+new_x]=1 Then
				Exit ;break simply to stop creating the branch
			EndIf
			fr\grid[((gridsize-1-temp_y)*gridsize)+new_x]=branch_type ;make 4s so you don't confuse your branch for a path; will be changed later
			If branch_pos=0 Then new_x=leftmost-2 Else new_x=rightmost+2
			fr\grid[((gridsize-1-temp_y)*gridsize)+new_x]=branch_type ;branch out twice to avoid creating an unwanted 2x2 path with the real path
			i = 2
			While i<branch_max_life
				i=i+1
				If chance(branch_die_chance) Then
					Exit
				EndIf
				If Rand(0,3)=0 Then ;have a higher chance to go up to confuse the player
					If branch_pos = 0 Then
						new_x=new_x-1
					Else
						new_x=new_x+1
					EndIf
				Else
					temp_y=temp_y+1
				EndIf
				
				;before creating a branch make sure there are no 1's above or below
				n=((gridsize - 1 - temp_y + 1)*gridsize)+new_x
				If n < gridsize-1 Then 
					If temp_y <> 0 And fr\grid[n]=1 Then Exit
				EndIf
				n=((gridsize - 1 - temp_y - 1)*gridsize)+new_x
				If n>0 Then 
					If fr\grid[n]=1 Then Exit
				EndIf
				
				;If (temp_y <> 0 And fr\grid[((gridsize - 1 - temp_y + 1)*gridsize)+new_x]=1) Or fr\grid[((gridsize - 1 - temp_y - 1)*gridsize)+new_x] = 1 Then
				;	Exit
				;EndIf
				fr\grid[((gridsize-1-temp_y)*gridsize)+new_x]=branch_type ;make 4s so you don't confuse your branch for a path; will be changed later
				If temp_y>=gridsize-2 Then Exit
			Wend
		EndIf
	Wend
	
	;change branches from 4s to 1s (they were 4s so that they didn't accidently create a 2x2 path unintentionally)
	For i=0 To gridsize-1
		For j=0 To gridsize-1
			If fr\grid[(i*gridsize)+j]=-1 Then
				fr\grid[(i*gridsize)+j]=1
			ElseIf fr\grid[(i*gridsize)+j]=-2
				fr\grid[(i*gridsize)+j]=1
			;ElseIf fr\grid[(i*gridsize)+j]=0
				
			EndIf
		Next
	Next
	
	CatchErrors("GenForestGrid")	
End Function

Function PlaceForest(fr.Forest,x#,y#,z#,r.Rooms)
	CatchErrors("Uncaught (PlaceForest)")
	;local variables
	Local tx%,ty%
	Local tile_size#=12.0
	Local tile_type%
	Local tile_entity%,detail_entity%
	Local rf#
	
	Local tempf1#,tempf2#,tempf3#
	Local i%
	
	If fr\Forest_Pivot<>0 Then FreeEntity fr\Forest_Pivot : fr\Forest_Pivot=0
	For i%=0 To 3
		If fr\TileMesh[i]<>0 Then FreeEntity fr\TileMesh[i] : fr\TileMesh[i]=0
	Next
	For i%=0 To 4
		If fr\DetailMesh[i]<>0 Then FreeEntity fr\DetailMesh[i] : fr\DetailMesh[i]=0
	Next
	For i%=0 To 9
		If fr\TileTexture[i]<>0 Then FreeEntity fr\TileTexture[i] : fr\TileTexture[i]=0
	Next
	
	;fr\Forest_Pivot=CreatePivot()
	;PositionEntity fr\Forest_Pivot,x,y,z,True
	
	;load assets
	
	Local hmap[ROOM4], mask[ROOM4]
	;Local GroundTexture = LoadTexture_Strict("GFX\map\forest\forestfloor.jpg")
	;TextureBlend GroundTexture, FE_ALPHACURRENT
	;Local PathTexture = LoadTexture_Strict("GFX\map\forest\forestpath.jpg")
	;TextureBlend PathTexture, FE_ALPHACURRENT
	
	hmap[ROOM1]=LoadImage_Strict("..\GFX\map\forest\forest1h.png")
	;mask[ROOM1]=LoadTexture_Strict("GFX\map\forest\forest1h_mask.png",1+2)
	
	hmap[ROOM2]=LoadImage_Strict("..\GFX\map\forest\forest2h.png")
	;mask[ROOM2]=LoadTexture_Strict("GFX\map\forest\forest2h_mask.png",1+2)
	
	hmap[ROOM2C]=LoadImage_Strict("..\GFX\map\forest\forest2Ch.png")
	;mask[ROOM2C]=LoadTexture_Strict("GFX\map\forest\forest2Ch_mask.png",1+2)
	
	hmap[ROOM3]=LoadImage_Strict("..\GFX\map\forest\forest3h.png")
	;mask[ROOM3]=LoadTexture_Strict("GFX\map\forest\forest3h_mask.png",1+2)
	
	hmap[ROOM4]=LoadImage_Strict("..\GFX\map\forest\forest4h.png")
	;mask[ROOM4]=LoadTexture_Strict("GFX\map\forest\forest4h_mask.png",1+2)
	
	For i = ROOM1 To ROOM4
		;TextureBlend mask[i], FE_ALPHAMODULATE
		
		;fr\TileMesh[i]=load_terrain(hmap[i],0.03,GroundTexture,PathTexture,mask[i])
	Next
	
	;detail meshes
	;fr\DetailMesh[0]=LoadMesh_strict("GFX\map\forest\detail\860_1_tree1.b3d")
	;fr\DetailMesh[1]=LoadMesh_strict("GFX\map\forest\detail\860_1_tree1_leaves.b3d")
	;fr\DetailMesh[1]=LoadMesh_Strict("GFX\map\forest\detail\treetest4.b3d");1.b3d)
	;EntityParent fr\DetailMesh[1],fr\DetailMesh[0]
	;fr\DetailMesh[2]=LoadMesh_Strict("GFX\map\forest\detail\rock.b3d")
	;fr\DetailMesh[3]=LoadMesh_Strict("GFX\map\forest\detail\rock2.b3d")
	;fr\DetailMesh[4]=LoadMesh_Strict("GFX\map\forest\detail\treetest5.b3d")
	;fr\DetailMesh[5]=LoadMesh_Strict("GFX\map\forest\wall.b3d")
	
	For i%=ROOM1 To ROOM4
		;HideEntity fr\TileMesh[i]
	Next
	For i%=1 To 5
		;HideEntity fr\DetailMesh[i]
	Next
	
	tempf3 = 29.0;tempf3=MeshWidth(fr\TileMesh[ROOM1])
	tempf1=tile_size/tempf3
	
	For tx%=1 To gridsize-1
		For ty%=1 To gridsize-1
			If fr\grid[(ty*gridsize)+tx]=1 Then 
				
				tile_type = 0
				If tx+1<gridsize Then tile_type = (fr\grid[(ty*gridsize)+tx+1]>0)
				If tx-1=>0 Then tile_type = tile_type+(fr\grid[(ty*gridsize)+tx-1]>0)
				
				If ty+1<gridsize Then tile_type = tile_type+(fr\grid[((ty+1)*gridsize)+tx]>0)
				If ty-1=>0 Then tile_type = tile_type+(fr\grid[((ty-1)*gridsize)+tx]>0)
				
				;fr\grid[(ty*gridsize)+tx]=tile_type
				
				Local angle%=0
				Select tile_type
					Case 1
						;tile_entity = CopyEntity(fr\TileMesh[ROOM1])
						
						If fr\grid[((ty+1)*gridsize)+tx]>0 Then
							angle = 180
						ElseIf fr\grid[(ty*gridsize)+tx-1]>0
							angle = 270
						ElseIf fr\grid[(ty*gridsize)+tx+1]>0
							angle = 90
						End If
						
						tile_type = ROOM1
					Case 2
						If fr\grid[((ty-1)*gridsize)+tx]>0 And fr\grid[((ty+1)*gridsize)+tx]>0 Then
							;tile_entity = CopyEntity(fr\TileMesh[ROOM2])
							tile_type = ROOM2
						ElseIf fr\grid[(ty*gridsize)+tx+1]>0 And fr\grid[(ty*gridsize)+tx-1]>0
							;tile_entity = CopyEntity(fr\TileMesh[ROOM2])
							angle = 90
							tile_type = ROOM2
						Else
							;tile_entity = CopyEntity(fr\TileMesh[ROOM2C])
							If fr\grid[(ty*gridsize)+tx-1]>0 And fr\grid[((ty+1)*gridsize)+tx]>0 Then
								angle = 180
							ElseIf fr\grid[(ty*gridsize)+tx+1]>0 And fr\grid[((ty-1)*gridsize)+tx]>0
								
							ElseIf fr\grid[(ty*gridsize)+tx-1]>0 And fr\grid[((ty-1)*gridsize)+tx]>0
								angle = 270
							Else
								angle = 90
							EndIf
							tile_type = ROOM2C
						EndIf
					Case 3
						;tile_entity = CopyEntity(fr\TileMesh[ROOM3])
						
						If fr\grid[((ty-1)*gridsize)+tx]=0 Then
							angle = 180
						ElseIf fr\grid[(ty*gridsize)+tx-1]=0
							angle = 90
						ElseIf fr\grid[(ty*gridsize)+tx+1]=0
							angle = 270
						End If
						
						tile_type = ROOM3
					Case 4
						;tile_entity = CopyEntity(fr\TileMesh[ROOM4])	
						tile_type = ROOM4
					Default 
						DebugLog "tile_type: "+tile_type
				End Select
				
				If tile_type > 0 Then 
					
					Local itemPlaced[4]
					;2, 5, 8
					;Local it.Items = Null
					If (ty Mod 3)=2 And itemPlaced[Floor(ty/3)]=False Then
						itemPlaced[Floor(ty/3)]=True
						Local ri% = Rand(360);it.Items = CreateItem("Log #"+Int(Floor(ty/3)+1), "paper", 0,0.5,0)
						;EntityType(it\collider, HIT_ITEM)
						;EntityParent(it\collider, tile_entity)
					EndIf
					
					;place trees and other details
					;only placed on spots where the value of the heightmap is above 100
					SetBuffer ImageBuffer(hmap[tile_type])
					width = ImageWidth(hmap[tile_type])
					tempf4# = (tempf3/Float(width))
					For lx = 3 To width-2
						For ly = 3 To width-2
							GetColor lx,width-ly
							
							If ColorRed()>Rand(100,260) Then
								Select Rand(0,7)
									Case 0,1,2,3,4,5,6 ;create a tree
										;detail_entity=CopyEntity(fr\DetailMesh[1])
										;EntityType detail_entity,HIT_MAP
										tempf2=Rnd(0.25,0.4)
										
										For i = 0 To 3
											;d=CopyEntity(fr\DetailMesh[4])
											;ScaleEntity d,tempf2*1.1,tempf2,tempf2*1.1,True
											rf# = Rnd(-20, 20);RotateEntity d, 0, 90*i+Rnd(-20,20), 0
											;EntityParent(d,detail_entity)
											
											;EntityFX d, 1;+8
										Next
										
										;ScaleEntity detail_entity,tempf2*1.1,tempf2,tempf2*1.1,True
										rf# = Rnd(3.0, 3.2);PositionEntity detail_entity,lx*tempf4-(tempf3/2.0),ColorRed()*0.03-Rnd(3.0,3.2),ly*tempf4-(tempf3/2.0),True
										
										rf# = Rnd(-5, 5)
										rf# = Rnd(360.0);RotateEntity detail_entity,Rnd(-5,5),Rnd(360.0),0.0,True
										
										;EntityAutoFade(detail_entity,4.0,6.0)
									Case 7 ;add a rock
										;detail_entity=CopyEntity(fr\DetailMesh[2])
										;EntityType detail_entity,HIT_MAP
										tempf2=Rnd(0.01,0.012)
										
										;ScaleEntity detail_entity,tempf2,tempf2*Rnd(1.0,2.0),tempf2,True
										
										;PositionEntity detail_entity,lx*tempf4-(tempf3/2.0),ColorRed()*0.03-1.3,ly*tempf4-(tempf3/2.0),True
										
										;EntityFX detail_entity, 1
										
										rf# = Rnd(360.0);RotateEntity detail_entity,0.0,Rnd(360.0),0.0,True
									Case 6 ;add a stump
										;detail_entity=CopyEntity(fr\DetailMesh[4])
										;EntityType detail_entity,HIT_MAP
										tempf2=Rnd(0.1,0.12)
										;ScaleEntity detail_entity,tempf2,tempf2,tempf2,True
										
										;PositionEntity detail_entity,lx*tempf4-(tempf3/2.0),ColorRed()*0.03-1.3,ly*tempf4-(tempf3/2.0),True
								End Select
								
								;EntityFX detail_entity, 1
								;PositionEntity detail_entity,Rnd(0.0,tempf3)-(tempf3/2.0),ColorRed()*0.03-0.05,Rnd(0.0,tempf3)-(tempf3/2.0),True
								
								;EntityParent detail_entity,tile_entity
							EndIf
						Next
					Next
					SetBuffer BackBuffer()
					
					;TurnEntity tile_entity, 0, angle, 0
					
					;PositionEntity tile_entity,x+(tx*tile_size),y,z+(ty*tile_size),True
					
					;ScaleEntity tile_entity,tempf1,tempf1,tempf1
					;EntityType tile_entity,HIT_MAP
					;EntityFX tile_entity,1
					;EntityParent tile_entity,fr\Forest_Pivot
					;EntityPickMode tile_entity,2
					
					;If it<>Null Then EntityParent it\collider,0
					
					;fr\TileEntities[tx+(ty*gridsize)] = tile_entity
				Else
					DebugLog "INVALID TILE @ ("+tx+", "+ty+ "): "+tile_type
				EndIf
			EndIf
			
		Next
	Next
	
	;place the wall		
	For i = 0 To 1
		ty = ((gridsize-1)*i)
		
		For tx = 1 To gridsize-1
			If fr\grid[(ty*gridsize)+tx]=3 Then
				;fr\DetailEntities[i]=CopyEntity(fr\DetailMesh[5])
				;ScaleEntity fr\DetailEntities[i],RoomScale,RoomScale,RoomScale
				
				;fr\Door[i] = CopyEntity(r\Objects[3])
				;PositionEntity fr\Door[i],72*RoomScale,32.0*RoomScale,0,True
				;RotateEntity fr\Door[i], 0,180,0
				;ScaleEntity fr\Door[i],48*RoomScale,45*RoomScale,48*RoomScale,True
				;EntityParent fr\Door[i],fr\DetailEntities[i]
				;SetAnimTime fr\Door[i], 0
				
				;frame = CopyEntity(r\Objects[2],fr\Door[i])
				;PositionEntity frame,0,32.0*RoomScale,0,True
				;ScaleEntity frame,48*RoomScale,45*RoomScale,48*RoomScale,True
				;EntityParent frame,fr\DetailEntities[i]
				
				;EntityType fr\DetailEntities[i],HIT_MAP
				;EntityParent frame,fr\DetailEntities[i]
				;EntityPickMode fr\DetailEntities[i],2
				
				;PositionEntity fr\DetailEntities[i],x+(tx*tile_size),y,z+(ty*tile_size)+(tile_size/2)-(tile_size*i),True
				;RotateEntity fr\DetailEntities[i],0,180*i,0
				
				;EntityParent fr\DetailEntities[i],fr\Forest_Pivot
			EndIf		
		Next		
	Next
	
	CatchErrors("PlaceForest")
	
End Function

Function AddLight%(room.Rooms)
	Local i, ri%
	
	If room<>Null Then
		For i = 0 To MaxRoomLights-1
			If room\Lights[i]=0 Then
				ri = Rand(360);RotateEntity(room\LightSprites2[i],0,0,Rand(360))
				
				ri = Rand(1, 10);room\LightFlicker%[i] = Rand(1,10)
				
				Return room\Lights[i]
			EndIf
		Next
	Else
		Return light
	EndIf
End Function


Function ResetMap()
	Local r.Rooms, rt.RoomTemplates, z.MapZones, wp.Waypoints
	Local f.Forest, g.Grids, d.Doors, lt.LightTemplates
	
	For rt.RoomTemplates = Each RoomTemplates
		rt\obj = 0
	Next
	
	Delete Each Rooms	
	
	For d.Doors = Each Doors
		Delete d
	Next
	
	For z.MapZones = Each MapZones
		Delete z
	Next
	
	For wp.WayPoints = Each WayPoints
		Delete wp
	Next
	
	For f.Forest = Each Forest
		Delete f
	Next
	
	For g.Grids = Each Grids
		Delete g
	Next
	
	For lt.LightTemplates = Each LightTemplates
		Delete lt
	Next
	
End Function

Function LoadImage_Strict(file$)
	If FileType(file$)<>1 Then RuntimeError "Image " + Chr(34) + file$ + Chr(34) + " missing. "
	tmp = LoadImage(file$)
	Return tmp
	;attempt to load the image again
	If tmp = 0 Then tmp2 = LoadImage(file)
	DebugLog "Attempting to load again: "+file
	Return tmp2
End Function

Function EndOfPage()
End Function



















