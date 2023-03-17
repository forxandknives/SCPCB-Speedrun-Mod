Function Min#(a#, b#)
	If a < b Then Return a Else Return b
End Function

Function Max#(a#, b#)
	If a > b Then Return a Else Return b
End Function 

Function chance%(chanc%)
	;perform a chance given a probability
	Return (Rand(0,100)<=chanc)
End Function

Function turn_if_deviating%(max_deviation_distance_%,pathx%,center_%,dir%,retval%=0)
	;check if deviating and return the answer. if deviating, turn around
	Local current_deviation% = center_ - pathx
	Local deviated% = False
	If (dir = 0 And current_deviation >= max_deviation_distance_) Or (dir = 2 And current_deviation <= -max_deviation_distance_) Then
		dir = (dir + 2) Mod 4
		deviated = True
	EndIf
	If retval=0 Then Return dir Else Return deviated
End Function

Function move_forward%(dir%,pathx%,pathy%,retval%=0)
	;move 1 unit along the grid in the designated direction
	If dir = 1 Then
		If retval=0 Then
			Return pathx
		Else
			Return pathy+1
		EndIf
	EndIf
	If retval=0 Then
		Return pathx-1+dir
	Else
		Return pathy
	EndIf
End Function

Function CatchErrors(location$)

	Local err$ = ErrorLog()
	Print(err)

End Function

Type INIFile
	Field name$
	Field bank%
	Field bankOffset% = 0
	Field size%
End Type

Function GetINIString$(file$, section$, parameter$, defaultvalue$="")
	Local TemporaryString$ = ""
	
	Local lfile.INIFile = Null
	For k.INIFile = Each INIFile
		If k\name = Lower(file) Then
			lfile = k
		EndIf
	Next
	
	If lfile = Null Then
		DebugLog "CREATE BANK FOR "+file
		lfile = New INIFile
		lfile\name = Lower(file)
		lfile\bank = 0
		UpdateINIFile(lfile\name)
	EndIf
	
	lfile\bankOffset = 0
	
	section = Lower(section)
	
	;While Not Eof(f)
	While lfile\bankOffset<lfile\size
		Local strtemp$ = ReadINILine(lfile)
		If Left(strtemp,1) = "[" Then
			strtemp$ = Lower(strtemp)
			If Mid(strtemp, 2, Len(strtemp)-2)=section Then
				Repeat
					TemporaryString = ReadINILine(lfile)
					If Lower(Trim(Left(TemporaryString, Max(Instr(TemporaryString, "=") - 1, 0)))) = Lower(parameter) Then
						;CloseFile f
						Return Trim( Right(TemporaryString,Len(TemporaryString)-Instr(TemporaryString,"=")) )
					EndIf
				Until (Left(TemporaryString, 1) = "[") Or (lfile\bankOffset>=lfile\size)
				
				;CloseFile f
				Return defaultvalue
			EndIf
		EndIf
	Wend
	
	Return defaultvalue
End Function

Function GetINIInt%(file$, section$, parameter$, defaultvalue% = 0)
	Local txt$ = GetINIString(file$, section$, parameter$, defaultvalue)
	If Lower(txt) = "true" Then
		Return 1
	ElseIf Lower(txt) = "false"
		Return 0
	Else
		Return Int(txt)
	EndIf
End Function

Function UpdateINIFile$(filename$)
	If roomsLineCount = 748 Then Return
	Local file.INIFile = Null
	For k.INIFile = Each INIFile
		If k\name = Lower(filename) Then
			file = k
		EndIf
	Next
	
	If file=Null Then Return
	
	If file\bank<>0 Then FreeBank file\bank
	Local f% = ReadFile(file\name)
	
	Local fleSize% = 1
	While fleSize<FileSize(file\name)
		fleSize=fleSize*2
	Wend
	file\bank = CreateBank(fleSize)
	file\size = 0
	While Not Eof(f)
		PokeByte(file\bank,file\size,ReadByte(f))
		file\size=file\size+1
	Wend
	CloseFile(f)
	
End Function

Function ReadINILine$(file.INIFile)
	Local rdbyte%
	Local firstbyte% = True
	Local offset% = file\bankOffset
	Local bank% = file\bank
	Local retStr$ = ""
	rdbyte = PeekByte(bank,offset)
	While ((firstbyte) Or ((rdbyte<>13) And (rdbyte<>10))) And (offset<file\size)
		rdbyte = PeekByte(bank,offset)
		If ((rdbyte<>13) And (rdbyte<>10)) Then
			firstbyte = False
			retStr=retStr+Chr(rdbyte)
		EndIf
		offset=offset+1
	Wend
	file\bankOffset = offset
	Return retStr
End Function

Function GetTFormVectors(rt.RoomTemplates)
	Select rt\Name
		Case "start"
			MinVecX = -2.625
			MinVecY = -0.109375
			MinVecZ = -4.0
			MaxVecX = 21.5
			MaxVecY = 5.46875
			MaxVecZ = 11.125
		Case "roompj"
			MinVecX = -4.0
			MinVecY = -0.00219807
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 3.75
			MaxVecZ = 5.0
		Case "room3_2"
			MinVecX = -4.0
			MinVecY = 0.0
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 3.33918
			MaxVecZ = 1.75
		Case "room2closets"
			MinVecX = -7.70313
			MinVecY = -1.625
			MinVecZ = -4.0
			MaxVecX = 3.1875
			MaxVecY = 2.76563
			MaxVecZ = 4.0
		Case "room2testroom2"
			MinVecX = -4.0
			MinVecY = -0.1875
			MinVecZ = -4.0
			MaxVecX = 1.375
			MaxVecY = 2.5
			MaxVecZ = 4.0
		Case "room2doors"
			MinVecX = -4.0
			MinVecY = 0.0
			MinVecZ = -4.0
			MaxVecX = 1.0		
			MaxVecY = 2.5
			MaxVecZ = 4.0		
		Case "room2_5"
			MinVecX = -1.1875
			MinVecY = -0.00390625
			MinVecZ = -4.0
			MaxVecX = 1.25
			MaxVecY = 1.5112
			MaxVecZ = 4.0
		Case "room3"
			MinVecX = -4.0
			MinVecY = 0.0
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 3.33918
			MaxVecZ = 4.0
		Case "room2scps"
			MinVecX = -3.1875
			MinVecY = -0.00390625
			MinVecZ = -4.0
			MaxVecX = 3.1875
			MaxVecY = 2.78906
			MaxVecZ = 4.0
		Case "room2tesla_lcz"
			MinVecX = -1.1875
			MinVecY = -0.25
			MinVecZ = -4.0
			MaxVecX = 1.1875
			MaxVecY = 2.79231
			MaxVecZ = 4.0
		Case "room2storage"
			MinVecX = -5.1875
			MinVecY = -0.00390625
			MinVecZ = -4.0
			MaxVecX = 5.1875
			MaxVecY = 2.76563
			MaxVecZ = 4.0
		Case "lockroom"
			MinVecX = -3.375
			MinVecY = 0.0
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 2.5
			MaxVecZ = 3.375
		Case "room2gw_b"
			MinVecX = -1.88281
			MinVecY = -0.0703125
			MinVecZ = -4.03906
			MaxVecX = 1.89453
			MaxVecY = 2.24609
			MaxVecZ = 4.0
		Case "room2gw"
			MinVecX = -2.125
			MinVecY = -0.0703125
			MinVecZ = -4.04687
			MaxVecX = 2.125
			MaxVecY = 2.24609
			MaxVecZ = 4.0
		Case "914"
			MinVecX = -4.0
			MinVecY = -0.00219807
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 3.1875
			MaxVecZ = 4.0
		Case "room1archive"
			MinVecX = -3.0
			MinVecY = -0.0625
			MinVecZ = -4.0
			MaxVecX = 1.125
			MaxVecY = 2.34468
			MaxVecZ = 2.9375
		Case "room2sl"
			MinVecX = -1.1875
			MinVecY = -0.25
			MinVecZ = -4.0
			MaxVecX = 7.0
			MaxVecY = 3.75
			MaxVecZ = 4.0
		Case "room012"
			MinVecX = -4.0
			MinVecY = -3.125
			MinVecZ = -4.0
			MaxVecX = 3.1875
			MaxVecY = 2.76563
			MaxVecZ = 4.0
		Case "room2scps2"
			MinVecX = -1.1875
			MinVecY = -0.0375
			MinVecZ = -4.0
			MaxVecX = 4.9375
			MaxVecY = 2.7612
			MaxVecZ = 4.00898
		Case "room2_2"
			MinVecX = -3.125
			MinVecY = -0.00390625
			MinVecZ = -4.0
			MaxVecX = 1.1875
			MaxVecY = 2.83918
			MaxVecZ = 4.0
		Case "room205"
			MinVecX = -7.0
			MinVecY = -0.625
			MinVecZ = -4.0
			MaxVecX = 3.125
			MaxVecY = 4.625
			MaxVecZ = 3.375
		Case "room4info"
			MinVecX = -4.0
			MinVecY = -0.078125
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 2.32906
			MaxVecZ = 4.0
		Case "room1123"
			MinVecX = -3.625
			MinVecY = -0.000000238419
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 3.83145
			MaxVecZ = 4.0
		Case "room2elevator"
			MinVecX = -1.1875
			MinVecY = -0.125
			MinVecZ = -4.0
			MaxVecX = 4.125
			MaxVecY = 3.33918
			MaxVecZ = 4.0
		Case "room2_3"
			MinVecX = -1.625
			MinVecY = -0.078125
			MinVecZ = -4.0
			MaxVecX = 1.625
			MaxVecY = 2.32906
			MaxVecZ = 4.0
		Case "room1162"
			MinVecX = -1.25
			MinVecY = -0.125
			MinVecZ = -4.01485
			MaxVecX = 4.0
			MaxVecY = 2.94082
			MaxVecZ = 1.25
		Case "checkpoint1"
			MinVecX = -4.3125
			MinVecY = 0.0
			MinVecZ = -4.0
			MaxVecX = 4.30469
			MaxVecY = 2.76563
			MaxVecZ = 4.0
		Case "room2pipes"		
			MinVecX = -1.21912
			MinVecY = -1.75391
			MinVecZ = -4.00781
			MaxVecX = 1.0
			MaxVecY = 4.0
			MaxVecZ = 4.0
		Case "room2pipes2"
			MinVecX = -1.0
			MinVecY = -1.75
			MinVecZ = -4.00781
			MaxVecX = 2.62305
			MaxVecY = 3.07813
			MaxVecZ = 4.0
		Case "room2pit"
			MinVecX = -4.0
			MinVecY = -1.75
			MinVecZ = -4.0
			MaxVecX = 3.0
			MaxVecY = 1.75
			MaxVecZ = 4.0
		Case "room4pit"
			MinVecX = -4.0
			MinVecY = -3.75391
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 1.50391
			MaxVecZ = 4.0
		Case "room2nuke"
			MinVecX = -4.0
			MinVecY = -0.125
			MinVecZ = -4.0
			MaxVecX = 7.0625
			MaxVecY = 7.875
			MaxVecZ = 4.0
		Case "tunnel"
			MinVecX = -1.125
			MinVecY = -0.00390622
			MinVecZ = -4.0
			MaxVecX = 1.125
			MaxVecY = 1.75391
			MaxVecZ = 4.0
		Case "room079"
			MinVecX = -1.625
			MinVecY = -2.75391
			MinVecZ = -4.125
			MaxVecX = 8.75
			MaxVecY = 2.76563
			MaxVecZ = 8.0
		Case "room3z2"
			MinVecX = -4.0
			MinVecY = -0.125
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 1.625
			MaxVecZ = 0.999999
		Case "room2tunnel"
			MinVecX = -3.4375
			MinVecY = -0.125
			MinVecZ = -4.0
			MaxVecX = 3.4375
			MaxVecY = 2.0
			MaxVecZ = 4.0
		Case "room4tunnels"
			MinVecX = -4.0
			MinVecY = -0.25
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 2.8862
			MaxVecZ = 4.0
		Case "room3tunnel"
			MinVecX = -4.0
			MinVecY = -0.0000000298023
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 2.96418
			MaxVecZ = 1.625
		Case "room513"
			MinVecX = -4.0
			MinVecY = -0.00390638
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 1.83918
			MaxVecZ = 4.0
		Case "tunnel2"
			MinVecX = -1.3125
			MinVecY = -0.00370812
			MinVecZ = -4.0
			MaxVecX = 2.0625
			MaxVecY = 2.71875
			MaxVecZ = 4.0
		Case "room106"
			MinVecX = -5.09375
			MinVecY = -5.0625
			MinVecZ = -4.0
			MaxVecX = 8.8125
			MaxVecY = 6.59375
			MaxVecZ = 12.1875
		Case "008"
			MinVecX = -2.375
			MinVecY = -0.125
			MinVecZ = -4.0
			MaxVecX = 3.0625
			MaxVecY = 4.5
			MaxVecZ = 3.25
		Case "room035"
			MinVecX = -2.875
			MinVecY = -0.125
			MinVecZ = -4.0
			MaxVecX = 4.875
			MaxVecY = 1.94709
			MaxVecZ = 3.5625
		Case "room2shaft"
			MinVecX = -1.0
			MinVecY = -5.875
			MinVecZ = -4.0
			MaxVecX = 7.875
			MaxVecY = 6.93359
			MaxVecZ = 4.0
		Case "testroom"
			MinVecX = -4.0
			MinVecY = -5.00391
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 3.125
			MaxVecZ = 4.0
		Case "room2cpit"
			MinVecX = -2.21875
			MinVecY = -3.75391
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 3.41743
			MaxVecZ = 3.75
		Case "room2tesla_hcz"
			MinVecX = -1.1875
			MinVecY = -0.25
			MinVecZ = -4.0
			MaxVecX = 1.1875
			MaxVecY = 2.76563
			MaxVecZ = 4.0
		Case "room3pit"
			MinVecX = -4.0
			MinVecY = -3.75391
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 1.50391
			MaxVecZ = 1.375
		Case "room2servers"
			MinVecX = -6.5
			MinVecY = -0.0000853539
			MinVecZ = -4.0
			MaxVecX = 0.875
			MaxVecY = 1.50391
			MaxVecZ = 4.0
		Case "coffin"
			MinVecX = -3.75
			MinVecY = -6.00391
			MinVecZ = -4.0
			MaxVecX = 1.8125
			MaxVecY = 2.75
			MaxVecZ = 10.0
		Case "checkpoint2"
			MinVecX = -4.30469
			MinVecY = 0.0
			MinVecZ = -4.0
			MaxVecX = 4.3125
			MaxVecY = 2.76563
			MaxVecZ = 4.00781
		Case "room1lifts"
			MinVecX = -1.8125
			MinVecY = -0.140696
			MinVecZ = -4.0
			MaxVecX = 1.75
			MaxVecY = 1.82364
			MaxVecZ = 0.412867
		Case "endroom"
			MinVecX = -2.8125
			MinVecY = -0.00390625
			MinVecZ = -4.0
			MaxVecX = 3.0625
			MaxVecY = 2.76563
			MaxVecZ = 4.84375
		Case "room2poffices"
			MinVecX = -2.0
			MinVecY = -0.000000119209
			MinVecZ = -4.0
			MaxVecX = 3.8125
			MaxVecY = 1.71418
			MaxVecZ = 4.0
		Case "room2cafeteria"
			MinVecX = -7.0
			MinVecY = -1.50009
			MinVecZ = -4.0625
			MaxVecX = 7.625
			MaxVecY = 2.76563
			MaxVecZ = 4.0
		Case "room2ccont"
			MinVecX = -8.875
			MinVecY = -0.25
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 5.25
			MaxVecZ = 7.125
		Case "room2sroom"
			MinVecX = -1.1875
			MinVecY = -0.000000119209
			MinVecZ = -4.0
			MaxVecX = 9.0
			MaxVecY = 2.5
			MaxVecZ = 4.0
		Case "room4z3"
			MinVecX = -4.0
			MinVecY = -0.00390625
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 5.625
			MaxVecZ = 4.0
		Case "room2servers2"
			MinVecX = -4.22461
			MinVecY = -3.12509
			MinVecZ = -4.09765
			MaxVecX = 3.1875
			MaxVecY = 1.81582
			MaxVecZ = 4.0
		Case "room3servers"
			MinVecX = -4.0
			MinVecY = -6.0
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 1.50391
			MaxVecZ = 4.03125
		Case "room2offices"
			MinVecX = -2.375
			MinVecY = 0.0
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 1.65022
			MaxVecZ = 4.0
		Case "room3offices"
			MinVecX = -4.0
			MinVecY = -0.00000000745058
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 1.81582
			MaxVecZ = 4.04688
		Case "room2offices4"
			MinVecX = -6.125
			MinVecY = -1.625
			MinVecZ = -4.0
			MaxVecX = 2.32813
			MaxVecY = 2.76563
			MaxVecZ = 4.0
		Case "room3servers2"
			MinVecX = -4.0
			MinVecY = -6.0
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 1.50391
			MaxVecZ = 4.03125
		Case "lockroom2"
			MinVecX = -3.375
			MinVecY = -0.00390625
			MinVecZ = -4.0
			MaxVecX = 4.0
			MaxVecY = 1.69856
			MaxVecZ = 3.375
		Case "room860"
			MinVecX = -1.1875
			MinVecY = -0.125
			MinVecZ = -4.0
			MaxVecX = 5.0
			MaxVecY = 2.83918
			MaxVecZ = 4.0
		Case "medibay"
			MinVecX = -4.0
			MinVecY = -0.00720204
			MinVecZ = -4.00391
			MaxVecX = 1.125
			MaxVecY = 1.5112
			MaxVecZ = 4.0
		Case "room2poffices2"
			MinVecX = -4.625
			MinVecY = -0.0055954
			MinVecZ = -4.0
			MaxVecX = 4.75
			MaxVecY = 1.71418
			MaxVecZ = 4.0
		Case "gateaentrance"
			MinVecX = -2.8125
			MinVecY = -0.125
			MinVecZ = -4.4375
			MaxVecX = 5.3125
			MaxVecY = 5.1875
			MaxVecZ = 4.84375
		Case "pocketdimension"
			MinVecX = -2.0
			MinVecY = -0.125
			MinVecZ = -2.0
			MaxVecX = 2.0
			MaxVecY = 4.0
			MaxVecZ = 2.0
		Case "dimension1499"
			MinVecX = -29.332
			MinVecY = -2.625
			MinVecZ = -16.4348
			MaxVecX = 29.3331
			MaxVecY = 34.875
			MaxVecZ = 16.4336	
		Default
			RuntimeError "Unhandled CalculateRoomExtents() on room: " + rt\Name
	End Select
End Function






















