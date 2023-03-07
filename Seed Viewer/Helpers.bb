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























