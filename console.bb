Graphics 1280,720,32,2

Const DEBUG_MODE% = False

If DEBUG_MODE Then AllocConsole()

Const SplitStringMaxAmount = 100

Type Split

	Field length%
	Field arr$[SplitStringMaxAmount] ;100 just to be safe
	
End Type

Function SplitString.Split(s$, delimiter$)

	Local splitArray.Split = New Split

	s = Trim(s)

	Local id% = MilliSecs()

	Local length% = Len(s)
	;Print("Length is " + Str(length))
	
	If length = 0 Then Return splitArray
	If delimiter = ""  Or Len(Delimiter) > 1 Return splitArray
			
	
	Local startIndex = -1
	Local endIndex%  = -1
	
	Local amount% = 0	
	
	For i = 1 To length	
	
		;Print(Mid(s, i, 1))
		If Mid(s, i, 1) <> delimiter Then
		
			If startIndex = -1
				startIndex = i
			EndIf
		
		EndIf
			
		If Mid(s, i, 1) = delimiter Then
			If endIndex = -1  And startIndex <> -1 Then
				endIndex = i-1
			EndIf						
		EndIf
		
		If endIndex = -1 And i = length
			endIndex = i
		EndIf
		
		If startIndex <> -1 And endIndex <> -1 Then
			;Print(s + " " + Str(startIndex) + " " + Str(endIndex))
			Local charsToRead = (endIndex - startIndex) + 1
			
			Local sub$ = Mid(s, startIndex, charsToRead)
			
			splitArray\arr[amount] = sub
			
			amount = amount + 1						
			
			If amount = 99 Then Exit
						
			startIndex = -1
			endIndex = -1	
			
		EndIf
	Next
		
	splitArray\length = amount
		
	Return splitArray
	
End Function

Function DeleteSplitStrings()

	For s.Split = Each Split
		Delete s
	Next

End Function

Function CheckForMemoryLeaks%()

	Local leaks% = False

	Color(255, 0, 0)

	;For string splitting.
	Local tempIndex% = 0
	For temp.Split = Each Split
		; This If is kind of redundant but whatever.
		If temp <> Null Then
			Print("MEMORY LEAK IN STRING SPLIT " + Str(tempIndex) + ": " + temp\arr[0])
			tempIndex = tempIndex + 1
			leaks = True
		EndIf
	Next
	;;;;;;;;;;;;;;;;;;;;;;;;;;
	
	tempIndex% = 0
	For tempCommand.ConsoleCommands = Each ConsoleCommands
		If tempCommand <> Null Then
			Print("MEMORY LEAK IN CONSOLE COMMAND " + Str(tempIndex) + ": " + tempCommand\command)
			tempIndex = tempIndex + 1
			leaks = True
		EndIf
	Next
	
	Color(255, 255, 255)
	
	Return leaks
End Function

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

Local n.Split = SplitString("    here we    are testing the split   ", " ")
Local a.Split = SplitString("fuck my ass please", " ")
Local b.Split = New Split

;For temp.Split = Each Split
;	
;	For i = 0 To temp\length-1
;		Print(temp\arr[i])
;	Next
;	Print("")
;	
;Next

DeleteSplitStrings()

Global Width% = GraphicsWidth()
Global Height% = GraphicsHeight()

Global FontMono% = LoadFont("GFX\font\mono\mono.tff", Width * 0.025)
SetFont(FontMono)

Global TargetFPS% = 60
Global frameDelay# = 1000.0 / Float(TargetFPS)
Global frameStart% = 0
Global frameTime# = 0.0
Global dt# = 0.0166
Global FPS% = 0

Local rectX# = 400.0
Local moveSpeed# = 1000.0

Global floppa = LoadImage("pic3.jpg")

Global flop.FlopConsole = CreateFlopConsole()

SetBuffer(BackBuffer())

Function StartMainLoop()
End Function

Repeat 
	
	FlopTimerSetStartTimeToNow()
	
	;;;;;;;;;;;;;;;;;;;;;;;;LOGIC START;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	
	If KeyHit(1) Then Exit

	If KeyDown(203) Then rectX = rectX - (moveSpeed * dt)
	If KeyDown(205) Then rectX = rectX + (moveSpeed * dt)
	
	If KeyHit(200) Then 
		TargetFPS = TargetFPS + 10 
		If TargetFPS > 200 Then
			TargetFPS = 200
		EndIf
		frameDelay# = 1000.0 / Float(TargetFPS)
	EndIf
	If KeyHit(208) Then 
		TargetFPS = TargetFPS - 10
		If TargetFPS = 0 Then
			TargetFPS = 10
		EndIf
		frameDelay# = 1000.0 / Float(TargetFPS)
	EndIf

	;;;;;;;;;;;;;;;;;;;;;;;;LOGIC END;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
			
	Cls
	
	Text(0, 0, "TARGET FPS: " + Str(TargetFPS) + " FPS: " + Str(fps))
	
	UpdateFlopConsole(flop)
		
	;DrawImage(floppa, Abs(Sin(FlopMillisecs() / 10)) * rectX, Abs(Sin(FlopMillisecs() / 3.4)) * 300)
	
	Flip(False)	
	
	Local elapsed# = FlopTimerGetElapsedTime()
	Local remaining# = frameDelay - 2	
	
	While remaining > 2.0
		FlopDelay(1)
		elapsed = FlopTimerGetElapsedTime()
		remaining = frameDelay - elapsed
	Wend
	
	While FlopTimerGetElapsedTime() < frameDelay
	Wend
	
	;frameTime = Float(MilliSecs() - frameStart)
	;If frameTime < frameDelay Then
	;	Delay Int(frameDelay - frameTime)
	;EndIf
	
	frameTime = FlopTimerGetElapsedTime()
	
	If frameTime > 0 Then
		dt = frameTime / 1000.0
		fps = Int(1000.0 / frameTime)
	EndIf

Forever

DeleteFlopConsole(Flop)
FreeImage(Floppa)
FreeFont(FontMono)

Local leak% = CheckForMemoryLeaks()
If leak Then
	FlushKeys()
	WaitKey()
EndIf

Type FlopConsole

	Field consoleOpen%

	Field consoleWidth%
	Field consoleHeight%
	
	Field consoleColorR%
	Field consoleColorG%
	Field consoleColorB%
	
	Field inputBoxHeight%
	
	Field inputText$
	Field consolePadding%
	
	Field cursorIndex%
	Field cursorWidth%
	Field cursorHeight%

	Field commandHistory.ConsoleCommands

End Type

Type ConsoleCommands

	Field command$

End Type

Function CreateFlopConsole.FlopConsole()
	
	Local flop.FlopConsole = New FlopConsole
	
	flop\consoleOpen = False
	
	flop\consoleWidth  = Width
	flop\consoleHeight = Int(Height * 0.35)
	
	flop\consoleColorR = 42
	flop\consoleColorG = 8
	flop\consoleColorB = 69
	
	flop\inputBoxHeight = flop\consoleHeight * 0.15
	
	flop\inputText = ""
	flop\consolePadding = width * 0.01
	
	flop\cursorIndex  = 0
	flop\cursorWidth  = StringWidth("A") / 5
	flop\cursorHeight = StringHeight("A")
	
	Return flop

End Function

Function DeleteFlopConsole(flop.FlopConsole)

	ClearConsoleCommands()
	
	Delete flop

End Function

Function UpdateFlopConsole(f.FlopConsole) 

	If f = Null Then Return

	If KeyHit(15) Then
		f\consoleOpen = Not f\consoleOpen
		FlushKeys()
	EndIf

	If f\consoleOpen Then						
		Local hitKey% = GetKey()
		If hitKey >= 32 And hitKey <= 255 Then
		
			Select(f\cursorIndex)
			
			;Beginning of string
			Case 0
				f\inputText = Chr(hitKey) + f\inputText
			
			;End of string
			Case Len(f\inputText)
				f\inputText = f\inputText + Chr(hitKey)				
				
			;Not at beginning or end of string
			Default
				Local leftSide$ = Left(f\inputText, f\cursorIndex)
				Local rightSide$ = Right(f\inputText, Len(f\inputText) - f\cursorIndex)
				f\inputText = leftSide + Chr(hitKey) + rightSide				
		
			End Select
		
			f\cursorIndex = f\cursorIndex + 1
			If DEBUG_MODE Then
				FPrint("Text: " + f\inputText + " CursorIndex: " + Str(f\cursorIndex))
			EndIf
		EndIf		
		
		;Enter
		If KeyHit(28) And f\inputText <> "" Then
			Local tempCommand.ConsoleCommands = New ConsoleCommands
			tempCommand\command = f\inputText
			f\inputText = ""			
			If DEBUG_MODE Then
				FPrint("ENTER Text: " + f\inputText + " CursorIndex: " + Str(f\cursorIndex))
			EndIf
			f\cursorIndex = 0
			
			DoConsoleCommand(tempCommand)
			
			Delete tempCommand
		EndIf
		
		;Backspace
		If KeyHit(14) And f\inputText <> "" Then
			
			Select(f\cursorIndex)
			;Beginning of string
			Case 0
				;Do nothing because we are at beginning of string
			
			;End of string
			Case Len(f\inputText)
				Local sub$ = Left(f\inputText, Len(f\inputText)-1)
				f\inputText = sub
				f\cursorIndex = f\cursorIndex - 1
				If f\cursorIndex < 0 Then f\cursorIndex = 0

			;Not at beginning or end of string
			Default
				Local l$ = Left(f\inputText, f\cursorIndex-1)
				
				Local r$ = Right(f\inputText, Len(f\inputText) - f\cursorIndex)				
				
				f\inputText = l + r
				
				f\cursorIndex = f\cursorIndex - 1
			
			End Select
			If DEBUG_MODE Then
				FPrint("BACKSPACE Text: " + f\inputText + " CursorIndex: " + Str(f\cursorIndex))
			EndIf
		EndIf
		
		;Delete
		If KeyHit(211) And f\inputText <> "" Then
			
			Select(f\cursorIndex)
			;Start of string
			Case 0
				f\inputText = Right(f\inputText, Len(f\inputText) - 1) 				
			
			;End of string
			Case Len(f\inputText)
				;Do nothing since nothing to delete
			
			Default
				Local ld$ = Left(f\inputText, f\cursorIndex)
				Local rd$ = Right(f\inputText, Len(f\inputText) - f\cursorIndex - 1)
				f\inputText = ld + rd
					
			End Select
			
		EndIf
		
		;Arrow left
		If KeyHit(203)  Then
			f\cursorIndex = f\cursorIndex - 1
			If f\cursorIndex < 0 Then f\cursorIndex = 0
		EndIf
		
		;Arrow right
		If KeyHit(205) Then
			f\cursorIndex = f\cursorIndex + 1
			If f\cursorIndex > Len(f\inputText) Then f\cursorIndex = Len(f\inputText)
		EndIf
		
		;Home
		If KeyHit(199) Then
			f\cursorIndex = 0
		EndIf
		
		;End
		If KeyHit(207) Then
			f\cursorIndex = Len(f\inputText)
		EndIf
		
		DrawFlopConsole(f)
			
	EndIf

End Function

Function DrawFlopConsole(f.FlopConsole)

	Color(f\consoleColorR, f\consoleColorG, f\consoleColorB)
	Rect(0, 0, f\consoleWidth, f\consoleHeight)
	
	Color 255, 0, 0
	Rect(0, f\consoleHeight - f\inputBoxHeight, f\consoleWidth, f\inputBoxHeight)
	
	Color 255, 255, 255				
	
	Local startX = f\consolePadding
	Local startY% = f\consoleHeight - f\inputBoxHeight
	Local inputTextY% = f\consoleHeight - f\inputBoxHeight
	
	;Input text
	Text(startX, startY, f\inputText)		
	
	Local c.ConsoleCommands = Last ConsoleCommands
	
	;Command history
	While c <> Null
		Local s$ = c\command
		
		Local sHeight% = StringHeight(s)
		
		startY = startY - sHeight
		
		Text(startX, startY, s)
		
		c = Before c

	Wend

	;Cursor
	Color(173, 216, 230)
	Local sub$ = Left(f\inputText, f\cursorIndex)
	Rect(startX + StringWidth(sub), inputTextY, f\cursorWidth, f\cursorHeight)
	
	Color(255, 255, 255)

End Function

Function ClearConsoleCommands()

	For c.ConsoleCommands = Each ConsoleCommands
		Delete c
	Next
	
End Function

Function PrintToConsole(s$)

	Local newCommand.ConsoleCommands = New ConsoleCommands
	newCommand\command = s

End Function

Function DoConsoleCommand(command.ConsoleCommands)

	Local one.Split = SplitString(command\command, " ")	
	Local actualCommand$ = one\arr[0]
	actualCommand = Lower(actualCommand)
	Delete one

	For c.ConsoleCommands = Each ConsoleCommands
	
		Select(actualCommand)
		Case "hello"
			Command_Hello()
			Return
		Case "clear"
			Command_Clear_Console()
			Return
		Case "date"
			Command_Date()
			Return
		Default
			;Nothing		
		
		End Select		
		
	Next
	
	Command_Unknown(actualCommand)

End Function

Function Command_Unknown(command$)
	PrintToConsole("Unknown command: " + command)
End Function

Function Command_Clear_Console()
	ClearConsoleCommands()
End Function

Function Command_Hello()

	Local time$ = CurrentTime()
	PrintToConsole("Hello. Current time is " + time)

End Function

Function Command_Date()
	Local date$ = CurrentDate()
	PrintToConsole("Current date is: " + date)
End Function
	












