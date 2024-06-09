Type Demos

	Field tick%

	Field gt%
	
	Field px#
	Field py#
	Field pz#	

	Field hx#
	Field hy#
	Field hz#
	
	Field pitch#
	Field yaw#
	Field roll#
	
	Field stamina#
	
	Field blink#
	
	Field readDoorPosition%
	Field doorx#
	Field doory#
	Field doorz#	
		
	Field readNutPosition#
	Field nutX#
	Field nutY#
	Field nutZ#
		
End Type

Function DemoMain()

	Repeat 
			
		;This will track how many times the main loop runs in each second, and the average run time for each loop.
		If DebugHUD2 Then							
		
			If StartMainLoopTime = 0 Then
				StartMainLoopTime = MilliSecs()
				TimeThisMainLoop = MilliSecs()
			Else
				If EndMainLoopTime - StartMainLoopTime >= 1000
					FinalLoopCount = MainLoopCount
					If FinalLoopCount <> 0
						FinalLoopTime = AverageMainLoopTime / FinalLoopCount
					EndIf
					
					DebugLog "-----------------------------------------"
					DebugLog "StartMainLoopTime:" + StartMainLoopTime
					DebugLog "EndMainLoopTime: " + EndMainLoopTime
					DebugLog "MainLoopCount: " + MainLoopCount
					DebugLog "TimeThisMainLoop: " + TimeThisMainLoop
					DebugLog "AverageMainLoopTime: " + AverageMainLoopTime
					DebugLog "FinalLoopCount: " + FinalLoopCount
					DebugLog "FinalLoopTime: " + FinalLoopTime
					DebugLog "-----------------------------------------"
					
					;Stop	
					
					MainLoopCount = 0
					AverageMainLoopTime = 0
					StartMainLoopTime = MilliSecs()
					TimeThisMainLoop = MilliSecs()
				Else 				
				
					MainLoopCount = MainLoopCount + 1				
					AverageMainLoopTime = AverageMainLoopTime + (EndMainLoopTime - TimeThisMainLoop)							
					TimeThisMainLoop = MilliSecs()	
					
				EndIf
			EndIf
		EndIf
		
		cls
		
		If ShouldIncreasePlayTime Then
			PlayTime = MilliSecs() - RunStartTime
		EndIf
		
		If Not RunFinished Then
			GameTime = PlayTime - LoadTime; + LoadFromMenuGameTime
		EndIf		
				
		CurTime = MilliSecs2()
		ElapsedTime = (CurTime - PrevTime) / 1000.0
		PrevTime = CurTime
		PrevFPSFactor = FPSfactor
		FPSfactor = Max(Min(ElapsedTime * 70, 5.0), 0.2)
		FPSfactor2 = FPSfactor
		
		FPSFactor = FPSFactor * DemoTimescale
		
		If DemoPaused Or MenuOpen Or InvOpen Or OtherOpen<>Null Or ConsoleOpen Or SelectedDoor <> Null Or SelectedScreen <> Null Or Using294 Then FPSfactor = 0	
		
		If Framelimit > 0 Then
		    ;Framelimit
			Local WaitingTime% = (1000.0 / Framelimit) - (MilliSecs2() - LoopDelay)
			Delay WaitingTime%
			
			LoopDelay = MilliSecs2()
		EndIf
		
		;Counting the fps
		If CheckFPS < MilliSecs2() Then
			FPS = ElapsedLoops
			ElapsedLoops = 0
			CheckFPS = MilliSecs2()+1000
		EndIf
		ElapsedLoops = ElapsedLoops + 1
		
		If Input_ResetTime<=0.0
			DoubleClick = False
			MouseHit1 = MouseHit(1)
			If MouseHit1 Then
				If MilliSecs2() - LastMouseHit1 < 800 Then DoubleClick = True
				LastMouseHit1 = MilliSecs2()
			EndIf
			
			Local prevmousedown1 = MouseDown1
			MouseDown1 = MouseDown(1)
			If prevmousedown1 = True And MouseDown1=False Then MouseUp1 = True Else MouseUp1 = False
			
			MouseHit2 = MouseHit(2)
			
			If (Not MouseDown1) And (Not MouseHit1) Then GrabbedEntity = 0
		Else
			Input_ResetTime = Max(Input_ResetTime-FPSfactor,0.0)
		EndIf
		
		UpdateMusic()
		If EnableSFXRelease Then AutoReleaseSounds()
		
		If MainMenuOpen Then
		
			For demo.Demos = Each Demos
				Delete demo
			Next
		
			Goto EndOfMain
						
		Else
			UpdateStreamSounds()
			
			ShouldPlay = Min(PlayerZone,2)
			
			DrawHandIcon = False
			
			RestoreSanity = True
			ShouldEntitiesFall = True
			
			If FPSfactor > 0 And PlayerRoom\RoomTemplate\Name <> "dimension1499" Then UpdateSecurityCams()
			
			If PlayerRoom\RoomTemplate\Name <> "pocketdimension" And PlayerRoom\RoomTemplate\Name <> "gatea" And PlayerRoom\RoomTemplate\Name <> "exit1" And (Not MenuOpen) And (Not ConsoleOpen) And (Not InvOpen) Then 
				
				If Rand(1500) = 1 Then
					For i = 0 To 5
						If AmbientSFX(i,CurrAmbientSFX)<>0 Then
							If ChannelPlaying(AmbientSFXCHN)=0 Then FreeSound_Strict AmbientSFX(i,CurrAmbientSFX) : AmbientSFX(i,CurrAmbientSFX) = 0
						EndIf			
					Next
					
					PositionEntity (SoundEmitter, EntityX(Camera) + Rnd(-1.0, 1.0), 0.0, EntityZ(Camera) + Rnd(-1.0, 1.0))
					
					If Rand(3)=1 Then PlayerZone = 3
					
					If PlayerRoom\RoomTemplate\Name = "173" Then 
						PlayerZone = 4
					ElseIf PlayerRoom\RoomTemplate\Name = "room860"
						For e.Events = Each Events
							If e\EventName = "room860"
								If e\EventState = 1.0
									PlayerZone = 5
									PositionEntity (SoundEmitter, EntityX(SoundEmitter), 30.0, EntityZ(SoundEmitter))
								EndIf
								
								Exit
							EndIf
						Next
					EndIf
					
					CurrAmbientSFX = Rand(0,AmbientSFXAmount(PlayerZone)-1)
					
					Select PlayerZone
						Case 0,1,2
							If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\Zone"+(PlayerZone+1)+"\ambient"+(CurrAmbientSFX+1)+".ogg")
						Case 3
							If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\General\ambient"+(CurrAmbientSFX+1)+".ogg")
						Case 4
							If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\Pre-breach\ambient"+(CurrAmbientSFX+1)+".ogg")
						Case 5
							If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\Forest\ambient"+(CurrAmbientSFX+1)+".ogg")
					End Select
					
					AmbientSFXCHN = PlaySound2(AmbientSFX(PlayerZone,CurrAmbientSFX), Camera, SoundEmitter)
				EndIf
				UpdateSoundOrigin(AmbientSFXCHN,Camera, SoundEmitter)
				
				If Rand(50000) = 3 Then
					Local RN$ = PlayerRoom\RoomTemplate\Name$
					If RN$ <> "room860" And RN$ <> "room1123" And RN$ <> "173" And RN$ <> "dimension1499" Then
						If FPSfactor > 0 Then LightBlink = Rnd(1.0,2.0)
						PlaySound_Strict  LoadTempSound("SFX\SCP\079\Broadcast"+Rand(1,7)+".ogg")
					EndIf 
				EndIf
			EndIf
			
			UpdateCheckpoint1 = False
			UpdateCheckpoint2 = False
			
			
			If (Not DemoPaused) And (Not MenuOpen) And (Not InvOpen) And (OtherOpen=Null) And (SelectedDoor = Null) And (ConsoleOpen = False) And (Using294 = False) And (SelectedScreen = Null) And EndingTimer=>0 Then
			
				PlayDemo()
			
				LightVolume = CurveValue(TempLightVolume, LightVolume, 50.0)
				CameraFogRange(Camera, CameraFogNear*LightVolume,CameraFogFar*LightVolume)
				CameraFogColor(Camera, 0,0,0)
				CameraFogMode Camera,1
				CameraRange(Camera, 0.05, Min(CameraFogFar*LightVolume*1.5,28))	
				If PlayerRoom\RoomTemplate\Name<>"pocketdimension" Then
					CameraClsColor(Camera, 0,0,0)
				EndIf
				
				AmbientLight Brightness, Brightness, Brightness	
				PlayerSoundVolume = CurveValue(0.0, PlayerSoundVolume, 5.0)
				
				CanSave% = True
				UpdateDeafPlayer()
				UpdateEmitters()
				DemoMouseLook()
				If PlayerRoom\RoomTemplate\Name = "dimension1499" And QuickLoadPercent > 0 And QuickLoadPercent < 100
					ShouldEntitiesFall = False
				EndIf
				DemoMovePlayer()
				InFacility = CheckForPlayerInFacility()
				If PlayerRoom\RoomTemplate\Name = "dimension1499"
					If QuickLoadPercent = -1 Or QuickLoadPercent = 100
						UpdateDimension1499()
					EndIf
					UpdateLeave1499()
				ElseIf PlayerRoom\RoomTemplate\Name = "gatea" Or (PlayerRoom\RoomTemplate\Name="exit1" And EntityY(Collider)>1040.0*RoomScale)
					UpdateDoors()
					If QuickLoadPercent = -1 Or QuickLoadPercent = 100
						UpdateEndings()
					EndIf
					UpdateScreens()
					UpdateRoomLights(Camera)
				Else
					UpdateDoors()
					If QuickLoadPercent = -1 Or QuickLoadPercent = 100
						UpdateEvents()
					EndIf
					UpdateScreens()
					TimeCheckpointMonitors()
					Update294()
					UpdateRoomLights(Camera)
				EndIf
				UpdateDecals()
				UpdateMTF()
				DemoUpdateNPCs()
				UpdateItems()
				UpdateParticles()
				Use427()
				UpdateMonitorSaving()
				;Added a simple code for updating the Particles function depending on the FPSFactor (still WIP, might not be the final version of it) - ENDSHN
				UpdateParticles_Time# = Min(1,UpdateParticles_Time#+FPSfactor)
				If UpdateParticles_Time#=1
					UpdateDevilEmitters()
					UpdateParticles_Devil()
					UpdateParticles_Time#=0
				EndIf
			EndIf
			
			If InfiniteStamina% Then Stamina = Min(100, Stamina + (100.0-Stamina)*0.01*FPSfactor)
			
			; HERE
							
			If FPSfactor=0
				UpdateWorld(0)
			Else
				UpdateWorld()
				ManipulateNPCBones()
			EndIf				
			RenderWorld2()
			
			BlurVolume = Min(CurveValue(0.0, BlurVolume, 20.0),0.95)
			If BlurTimer > 0.0 Then
				BlurVolume = Max(Min(0.95, BlurTimer / 1000.0), BlurVolume)
				BlurTimer = Max(BlurTimer - FPSfactor, 0.0)
			End If
			
			UpdateBlur(BlurVolume)
			
			;[Block]
			
			Local darkA# = 0.0
			If (Not MenuOpen)  Then
				If Sanity < 0 Then
					If RestoreSanity Then Sanity = Min(Sanity + FPSfactor, 0.0)
					If Sanity < (-200) Then 
						darkA = Max(Min((-Sanity - 200) / 700.0, 0.6), darkA)
						If KillTimer => 0 Then 
							HeartBeatVolume = Min(Abs(Sanity+200)/500.0,1.0)
							HeartBeatRate = Max(70 + Abs(Sanity+200)/6.0,HeartBeatRate)
						EndIf
					EndIf
				End If
				
				If EyeStuck > 0 Then 
					BlinkTimer = BLINKFREQ
					EyeStuck = Max(EyeStuck-FPSfactor,0)
					
					If EyeStuck < 9000 Then BlurTimer = Max(BlurTimer, (9000-EyeStuck)*0.5)
					If EyeStuck < 6000 Then darkA = Min(Max(darkA, (6000-EyeStuck)/5000.0),1.0)
					If EyeStuck < 9000 And EyeStuck+FPSfactor =>9000 Then 
						Msg = "The eyedrops are causing your eyes to tear up."
						MsgTimer = 70*6
					EndIf
				EndIf
				
				If BlinkTimer < 0 Then
					If BlinkTimer > - 5 Then
						darkA = Max(darkA, Sin(Abs(BlinkTimer * 18.0)))
					ElseIf BlinkTimer > - 15
						darkA = 1.0
					Else
						darkA = Max(darkA, Abs(Sin(BlinkTimer * 18.0)))
					EndIf
					
					If BlinkTimer <= - 20 Then
						;Randomizes the frequency of blinking. Scales with difficulty.
						Select SelectedDifficulty\otherFactors
							Case EASY
								BLINKFREQ = Rnd(490,700)
							Case NORMAL
								BLINKFREQ = Rnd(455,665)
							Case HARD
								BLINKFREQ = Rnd(420,630)
						End Select 
						;BlinkTimer = BLINKFREQ
					EndIf
					
					;BlinkTimer = BlinkTimer - FPSfactor
				Else
					;BlinkTimer = BlinkTimer - FPSfactor * 0.6 * BlinkEffect
					;If EyeIrritation > 0 Then BlinkTimer=BlinkTimer-Min(EyeIrritation / 100.0 + 1.0, 4.0) * FPSfactor
					
					darkA = Max(darkA, 0.0)
				End If
				
				EyeIrritation = Max(0, EyeIrritation - FPSfactor)
				
				If BlinkEffectTimer > 0 Then
					BlinkEffectTimer = BlinkEffectTimer - (FPSfactor/70)
				Else
					If BlinkEffect <> 1.0 Then BlinkEffect = 1.0
				EndIf
				
				LightBlink = Max(LightBlink - (FPSfactor / 35.0), 0)
				If LightBlink > 0 Then darkA = Min(Max(darkA, LightBlink * Rnd(0.3, 0.8)), 1.0)
				
				If Using294 Then darkA=1.0
				
				If (Not WearingNightVision) Then darkA = Max((1.0-SecondaryLightOn)*0.9, darkA)
				
				If KillTimer < 0 Then				
				
					If DeathEndTime = 0 Then
						DeathEndTime = MilliSecs()
					EndIf
					
					If ShowDeathTime Then
						Local deathTime% = DeathEndTime - DeathStartTime
						
						Local sec% = deathTime / 1000
						Local msec% = deathTime Mod 1000
						
						AAText(MonitorWidth / 2, MonitorHeight * 0.90, "Died in " + Str(sec) + "." + Str(msec) + " seconds.", True, True)
					EndIf
					
					InvOpen = False
					SelectedItem = Null
					SelectedScreen = Null
					SelectedMonitor = Null
					BlurTimer = Abs(KillTimer*5)
					KillTimer=KillTimer-(FPSfactor*0.8)
					If KillTimer < - 360 Then 
						MenuOpen = True 
						If SelectedEnding <> "" Then EndingTimer = Min(KillTimer,-0.1)
					EndIf
					darkA = Max(darkA, Min(Abs(KillTimer / 400.0), 1.0))
				EndIf
				
				If FallTimer < 0 Then
					If SelectedItem <> Null Then
						If Instr(SelectedItem\itemtemplate\tempname,"hazmatsuit") Or Instr(SelectedItem\itemtemplate\tempname,"vest") Then
							If WearingHazmat=0 And WearingVest=0 Then
								DropItem(SelectedItem)
							EndIf
						EndIf
					EndIf
					InvOpen = False
					SelectedItem = Null
					SelectedScreen = Null
					SelectedMonitor = Null
					BlurTimer = Abs(FallTimer*10)
					FallTimer = FallTimer-FPSfactor
					darkA = Max(darkA, Min(Abs(FallTimer / 400.0), 1.0))				
				EndIf
				
				If SelectedItem <> Null Then
					If SelectedItem\itemtemplate\tempname = "navigator" Or SelectedItem\itemtemplate\tempname = "nav" Then darkA = Max(darkA, 0.5)
				End If
				If SelectedScreen <> Null Then darkA = Max(darkA, 0.5)
				
				EntityAlpha(Dark, darkA)	
			EndIf
			
			If LightFlash > 0 Then
				ShowEntity Light
				EntityAlpha(Light, Max(Min(LightFlash + Rnd(-0.2, 0.2), 1.0), 0.0))
				LightFlash = Max(LightFlash - (FPSfactor / 70.0), 0)
			Else
				HideEntity Light
				;EntityAlpha(Light, LightFlash)
			EndIf
			
			EntityColor Light,255,255,255
			
			;[End block]
			
			If KeyHit(25) And (Not ConsoleOpen) Then
				;If DemoUIOpen Then
				;	ResumeSounds()
				;	MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0
				;Else
				;	PauseSounds()
				;EndIf
				DemoUIOpen = Not DemoUIOpen
			EndIf
			
			If KeyHit(KEY_INV) And VomitTimer >= 0 Then
				If (Not UnableToMove) And (Not IsZombie) And (Not Using294) Then
					Local W$ = ""
					Local V# = 0
					If SelectedItem<>Null
						W$ = SelectedItem\itemtemplate\tempname
						V# = SelectedItem\state
					EndIf
					If (W<>"vest" And W<>"finevest" And W<>"hazmatsuit" And W<>"hazmatsuit2" And W<>"hazmatsuit3") Or V=0 Or V=100
						If InvOpen Then
							ResumeSounds()
							MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0
						Else
							PauseSounds()
						EndIf
						InvOpen = Not InvOpen
						If OtherOpen<>Null Then OtherOpen=Null
						SelectedItem = Null
					EndIf
				EndIf
			EndIf
			
			If KeyHit(KEY_SAVE) Then
				If SelectedDifficulty\saveType = SAVEANYWHERE Then
					RN$ = PlayerRoom\RoomTemplate\Name$
					If RN$ = "173" Or (RN$ = "exit1" And EntityY(Collider)>1040.0*RoomScale) Or RN$ = "gatea"
						Msg = "You cannot save in this location."
						MsgTimer = 70 * 4
						;SetSaveMSG("You cannot save in this location.")
					ElseIf (Not CanSave) Or QuickLoadPercent > -1
						Msg = "You cannot save at this moment."
						MsgTimer = 70 * 4
						;SetSaveMSG("You cannot save at this moment.")
						If QuickLoadPercent > -1
							Msg = Msg + " (game is loading)"
							;Save_MSG = Save_MSG + " (game is loading)"
						EndIf
					Else
						SaveGame(SavePath + CurrSave + "\")
					EndIf
				ElseIf SelectedDifficulty\saveType = SAVEONSCREENS
					If SelectedScreen=Null And SelectedMonitor=Null Then
						Msg = "Saving is only permitted on clickable monitors scattered throughout the facility."
						MsgTimer = 70 * 4
						;SetSaveMSG("Saving is only permitted on clickable monitors scattered throughout the facility.")
					Else
						RN$ = PlayerRoom\RoomTemplate\Name$
						If RN$ = "173" Or (RN$ = "exit1" And EntityY(Collider)>1040.0*RoomScale) Or RN$ = "gatea"
							Msg = "You cannot save in this location."
							MsgTimer = 70 * 4
							;SetSaveMSG("You cannot save in this location.")
						ElseIf (Not CanSave) Or QuickLoadPercent > -1
							Msg = "You cannot save at this moment."
							MsgTimer = 70 * 4
							;SetSaveMSG("You cannot save at this moment.")
							If QuickLoadPercent > -1
								Msg = Msg + " (game is loading)"
								;Save_MSG = Save_MSG + " (game is loading)"
							EndIf
						Else
							If SelectedScreen<>Null
								GameSaved = False
								Playable = True
								DropSpeed = 0
							EndIf
							SaveGame(SavePath + CurrSave + "\")
						EndIf
					EndIf
				Else
					Msg = "Quick saving is disabled."
					MsgTimer = 70 * 4
					;SetSaveMSG("Quick saving is disabled.")
				EndIf
			Else If SelectedDifficulty\saveType = SAVEONSCREENS And (SelectedScreen<>Null Or SelectedMonitor<>Null)
				If (Msg<>"Game progress saved." And Msg<>"You cannot save in this location."And Msg<>"You cannot save at this moment.") Or MsgTimer<=0 Then
					Msg = "Press "+KeyName(KEY_SAVE)+" to save."
					MsgTimer = 70*4
					;SetSaveMSG("Press "+KeyName(KEY_SAVE)+" to save.")
				EndIf
				
				If MouseHit2 Then SelectedMonitor = Null
			EndIf
			
			If KeyHit(KEY_CONSOLE) Then
				If CanOpenConsole
					If ConsoleOpen Then
						UsedConsole = True
						ResumeSounds()
						MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0 
					Else
						PauseSounds()
					EndIf
					
					;ConsoleOpen = (Not ConsoleOpen)
					If Not(ConsoleOpen) Then
						ConsoleOpen = True
						CursorIndex = Len(ConsoleInput)
					Else
						ConsoleOpen = False
					EndIf
					
					FlushKeys()
				EndIf
			EndIf
			
			DrawGUI()
			
			If EndingTimer < 0 Then
				If SelectedEnding <> "" Then DrawEnding()
			Else
				DrawMenu()			
			EndIf
			
			UpdateConsole()
			
			If PlayerRoom <> Null Then
				If PlayerRoom\RoomTemplate\Name = "173" Then
					For e.Events = Each Events
						If e\EventName = "173" Then
							If e\EventState3 => 40 And e\EventState3 < 50 Then
								If InvOpen Then
									Msg = "Double click on the document to view it."
									MsgTimer=70*7
									e\EventState3 = 50
								EndIf
							EndIf
						EndIf
					Next
				EndIf
			EndIf
			
			If MsgTimer > 0 Then
				Local temp% = False
				If (Not InvOpen%)
					If SelectedItem <> Null
						If SelectedItem\itemtemplate\tempname = "paper" Or SelectedItem\itemtemplate\tempname = "oldpaper"
							temp% = True
						EndIf
					EndIf
				EndIf
				
				If (Not temp%)
					Color 0,0,0
					AAText((GraphicWidth / 2)+1, (GraphicHeight / 2) + 201, Msg, True, False, Min(MsgTimer / 2, 255)/255.0)
					Color 255,255,255;Min(MsgTimer / 2, 255), Min(MsgTimer / 2, 255), Min(MsgTimer / 2, 255)
					If Left(Msg,14)="Blitz3D Error!" Then
						Color 255,0,0
					EndIf
					AAText((GraphicWidth / 2), (GraphicHeight / 2) + 200, Msg, True, False, Min(MsgTimer / 2, 255)/255.0)
				Else
					Color 0,0,0
					AAText((GraphicWidth / 2)+1, (GraphicHeight * 0.94) + 1, Msg, True, False, Min(MsgTimer / 2, 255)/255.0)
					Color 255,255,255;Min(MsgTimer / 2, 255), Min(MsgTimer / 2, 255), Min(MsgTimer / 2, 255)
					If Left(Msg,14)="Blitz3D Error!" Then
						Color 255,0,0
					EndIf
					AAText((GraphicWidth / 2), (GraphicHeight * 0.94), Msg, True, False, Min(MsgTimer / 2, 255)/255.0)
				EndIf
				MsgTimer=MsgTimer-FPSfactor2 
			End If
			
			Color 255, 255, 255
			If ShowFPS Then AASetFont ConsoleFont : AAText 20, 20, "FPS: " + FPS : AASetFont Font1
			
			DrawQuickLoading()
			
			UpdateAchievementMsg()
			;UpdateSaveMSG()
		End If
		
		If BorderlessWindowed Then
			If (RealGraphicWidth<>GraphicWidth) Or (RealGraphicHeight<>GraphicHeight) Then
				setbuffer TextureBuffer(fresize_texture)
				clscolor 0,0,0 : cls
				CopyRect 0,0,GraphicWidth,GraphicHeight,1024-GraphicWidth/2,1024-GraphicHeight/2,BackBuffer(),TextureBuffer(fresize_texture)
				setbuffer BackBuffer()
				clscolor 0,0,0 : cls
				ScaleRender(0,0,2050.0 / Float(GraphicWidth) * AspectRatioRatio, 2050.0 / Float(GraphicWidth) * AspectRatioRatio)
				;might want to replace Float(GraphicWidth) with Max(GraphicWidth,GraphicHeight) if portrait sizes cause issues
				;everyone uses landscape so it's probably a non-issue
			EndIf
		EndIf
		
		;not by any means a perfect solution
		;Not even proper gamma correction but it's a nice looking alternative that works in windowed mode
		If ScreenGamma>1.0 Then
			CopyRect 0,0,RealGraphicWidth,RealGraphicHeight,1024-RealGraphicWidth/2,1024-RealGraphicHeight/2,BackBuffer(),TextureBuffer(fresize_texture)
			EntityBlend fresize_image,1
			clscolor 0,0,0 : cls
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
			EntityFX fresize_image,1+32
			EntityBlend fresize_image,3
			EntityAlpha fresize_image,ScreenGamma-1.0
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
		ElseIf ScreenGamma<1.0 Then ;todo: maybe optimize this if it's too slow, alternatively give players the option to disable gamma
			CopyRect 0,0,RealGraphicWidth,RealGraphicHeight,1024-RealGraphicWidth/2,1024-RealGraphicHeight/2,BackBuffer(),TextureBuffer(fresize_texture)
			EntityBlend fresize_image,1
			clscolor 0,0,0 : cls
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
			EntityFX fresize_image,1+32
			EntityBlend fresize_image,2
			EntityAlpha fresize_image,1.0
			setbuffer TextureBuffer(fresize_texture2)
			clscolor 255*ScreenGamma,255*ScreenGamma,255*ScreenGamma
			cls
			setbuffer BackBuffer()
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
			setbuffer(TextureBuffer(fresize_texture2))
			clscolor 0,0,0
			cls
			setbuffer(BackBuffer())
		EndIf
		EntityFX fresize_image,1
		EntityBlend fresize_image,1
		EntityAlpha fresize_image,1.0
		
		OldPlayerRoom = PlayerRoom
		
		If DebugHUD2 Then			
			EndMainLoopTime = MilliSecs()
		EndIf
		
		CatchErrors("Demo Main loop / uncaught")
		
		If Vsync = 0 Then
			Flip 0
		Else 
			Flip 1
		EndIf
	Forever

.EndOfMain

End Function

Function DemoMouseLook()
	Local i%
	
	CameraShake = Max(CameraShake - (FPSfactor / 10), 0)
	
	;CameraZoomTemp = CurveValue(CurrCameraZoom,CameraZoomTemp, 5.0)
	CameraZoom(Camera, Min(1.0+(CurrCameraZoom/400.0),1.1))
	CurrCameraZoom = Max(CurrCameraZoom - FPSfactor, 0)
	
	If KillTimer >= 0 And FallTimer >=0 Then
		
		HeadDropSpeed = 0
		
		;If 0 Then 
		;fixing the black screen bug with some bubblegum code 
		Local Zero# = 0.0
		Local Nan1# = 0.0 / Zero
		If Int(EntityX(Collider))=Int(Nan1) Then
			
			PositionEntity Collider, EntityX(Camera, True), EntityY(Camera, True) - 0.5, EntityZ(Camera, True), True
			Msg = "EntityX(Collider) = NaN, RESETTING COORDINATES    -    New coordinates: "+EntityX(Collider)
			MsgTimer = 300				
		EndIf
		;EndIf
		
		Local up# = (Sin(Shake) / (20.0+CrouchState*20.0))*0.6;, side# = Cos(Shake / 2.0) / 35.0		
		Local roll# = Max(Min(Sin(Shake/2)*2.5*Min(Injuries+0.25,3.0),8.0),-8.0)
		
		;käännetään kameraa sivulle jos pelaaja on vammautunut
		;RotateEntity Collider, EntityPitch(Collider), EntityYaw(Collider), Max(Min(up*30*Injuries,50),-50)
		PositionEntity Camera, EntityX(Collider), EntityY(Collider), EntityZ(Collider)
		;RotateEntity Camera, 0, EntityYaw(Collider), roll*0.5
		
		MoveEntity Camera, side, up + 0.6 + CrouchState * -0.3, 0
		
		;RotateEntity Collider, EntityPitch(Collider), EntityYaw(Collider), 0
		;moveentity player, side, up, 0	
		; -- Update the smoothing que To smooth the movement of the mouse.
		mouse_x_speed_1# = CurveValue(MouseXSpeed() * (MouseSens + 0.6) , mouse_x_speed_1, (6.0 / (MouseSens + 1.0))*MouseSmooth) 
		If Int(mouse_x_speed_1) = Int(Nan1) Then mouse_x_speed_1 = 0
		If PrevFPSFactor>0 Then
            If Abs(FPSfactor/PrevFPSFactor-1.0)>1.0 Then
                ;lag spike detected - stop all camera movement
                mouse_x_speed_1 = 0.0
                mouse_y_speed_1 = 0.0
            EndIf
        EndIf
		If InvertMouse Then
			mouse_y_speed_1# = CurveValue(-MouseYSpeed() * (MouseSens + 0.6), mouse_y_speed_1, (6.0/(MouseSens+1.0))*MouseSmooth) 
		Else
			mouse_y_speed_1# = CurveValue(MouseYSpeed () * (MouseSens + 0.6), mouse_y_speed_1, (6.0/(MouseSens+1.0))*MouseSmooth) 
		EndIf
		If Int(mouse_y_speed_1) = Int(Nan1) Then mouse_y_speed_1 = 0
		
		Local the_yaw# = ((mouse_x_speed_1#)) * mouselook_x_inc# / (1.0+WearingVest)
		Local the_pitch# = ((mouse_y_speed_1#)) * mouselook_y_inc# / (1.0+WearingVest)
		
		TurnEntity Collider, 0.0, -the_yaw#, 0.0 ; Turn the user on the Y (yaw) axis.
		user_camera_pitch# = user_camera_pitch# + the_pitch#
		; -- Limit the user;s camera To within 180 degrees of pitch rotation. ;EntityPitch(); returns useless values so we need To use a variable To keep track of the camera pitch.
		If user_camera_pitch# > 70.0 Then user_camera_pitch# = 70.0
		If user_camera_pitch# < - 70.0 Then user_camera_pitch# = -70.0
		
		;RotateEntity Camera, WrapAngle(user_camera_pitch + Rnd(-CameraShake, CameraShake)), WrapAngle(EntityYaw(Collider) + Rnd(-CameraShake, CameraShake)), roll ; Pitch the user;s camera up And down.
		
		If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
			If EntityY(Collider)<2000*RoomScale Or EntityY(Collider)>2608*RoomScale Then
				RotateEntity Camera, WrapAngle(EntityPitch(Camera)),WrapAngle(EntityYaw(Camera)), roll+WrapAngle(Sin(MilliSecs2()/150.0)*30.0) ; Pitch the user;s camera up And down.
			EndIf
		EndIf
		
	Else
		HideEntity Collider
		PositionEntity Camera, EntityX(Head), EntityY(Head), EntityZ(Head)
		
		Local CollidedFloor% = False
		For i = 1 To CountCollisions(Head)
			If CollisionY(Head, i) < EntityY(Head) - 0.01 Then CollidedFloor = True
		Next
		
		If CollidedFloor = True Then
			HeadDropSpeed# = 0
		Else
			
			If KillAnim = 0 Then 
				MoveEntity Head, 0, 0, HeadDropSpeed
				RotateEntity(Head, CurveAngle(-90.0, EntityPitch(Head), 20.0), EntityYaw(Head), EntityRoll(Head))
				RotateEntity(Camera, CurveAngle(EntityPitch(Head) - 40.0, EntityPitch(Camera), 40.0), EntityYaw(Camera), EntityRoll(Camera))
			Else
				MoveEntity Head, 0, 0, -HeadDropSpeed
				RotateEntity(Head, CurveAngle(90.0, EntityPitch(Head), 20.0), EntityYaw(Head), EntityRoll(Head))
				RotateEntity(Camera, CurveAngle(EntityPitch(Head) + 40.0, EntityPitch(Camera), 40.0), EntityYaw(Camera), EntityRoll(Camera))
			EndIf
			
			HeadDropSpeed# = HeadDropSpeed - 0.002 * FPSfactor
		EndIf
		
		If InvertMouse Then
			TurnEntity (Camera, -MouseYSpeed() * 0.05 * FPSfactor, -MouseXSpeed() * 0.15 * FPSfactor, 0)
		Else
			TurnEntity (Camera, MouseYSpeed() * 0.05 * FPSfactor, -MouseXSpeed() * 0.15 * FPSfactor, 0)
		End If
		
	EndIf
	
	;pölyhiukkasia
	If ParticleAmount=2
		If Rand(35) = 1 Then
			Local pvt% = CreatePivot()
			PositionEntity(pvt, EntityX(Camera, True), EntityY(Camera, True), EntityZ(Camera, True))
			RotateEntity(pvt, 0, Rnd(360), 0)
			If Rand(2) = 1 Then
				MoveEntity(pvt, 0, Rnd(-0.5, 0.5), Rnd(0.5, 1.0))
			Else
				MoveEntity(pvt, 0, Rnd(-0.5, 0.5), Rnd(0.5, 1.0))
			End If
			
			Local p.Particles = CreateParticle(EntityX(pvt), EntityY(pvt), EntityZ(pvt), 2, 0.002, 0, 300)
			p\speed = 0.001
			RotateEntity(p\pvt, Rnd(-20, 20), Rnd(360), 0)
			
			p\SizeChange = -0.00001
			
			FreeEntity pvt
		End If
	EndIf
	
	; -- Limit the mouse;s movement. Using this method produces smoother mouselook movement than centering the mouse Each loop.
	If (MouseX() > mouse_right_limit) Or (MouseX() < mouse_left_limit) Or (MouseY() > mouse_bottom_limit) Or (MouseY() < mouse_top_limit)
		;MoveMouse viewport_center_x, viewport_center_y
	EndIf
	
	If WearingGasMask Or WearingHazmat Or Wearing1499 Then
		If Wearing714 = False Then
			If WearingGasMask = 2 Or Wearing1499 = 2 Or WearingHazmat = 2 Then
				Stamina = Min(100, Stamina + (100.0-Stamina)*0.01*FPSfactor)
			EndIf
		EndIf
		If WearingHazmat = 1 Then
			Stamina = Min(60, Stamina)
		EndIf
		
		ShowEntity(GasMaskOverlay)
	Else
		HideEntity(GasMaskOverlay)
	End If
	
	If (Not WearingNightVision=0) Then
		ShowEntity(NVOverlay)
		If WearingNightVision=2 Then
			EntityColor(NVOverlay, 0,100,255)
			AmbientLightRooms(15)
		ElseIf WearingNightVision=3 Then
			EntityColor(NVOverlay, 255,0,0)
			AmbientLightRooms(15)
		Else
			EntityColor(NVOverlay, 0,255,0)
			AmbientLightRooms(15)
		EndIf
		EntityTexture(Fog, FogNVTexture)
	Else
		AmbientLightRooms(0)
		HideEntity(NVOverlay)
		EntityTexture(Fog, FogTexture)
	EndIf
	
	For i = 0 To 5
		If SCP1025state[i]>0 Then
			Select i
				Case 0 ;common cold
					If FPSfactor>0 Then 
						If Rand(1000)=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							End If
						EndIf
					EndIf
					Stamina = Stamina - FPSfactor * 0.3
				Case 1 ;chicken pox
					If Rand(9000)=1 And Msg="" Then
						Msg="Your skin is feeling itchy."
						MsgTimer =70*4
					EndIf
				Case 2 ;cancer of the lungs
					If FPSfactor>0 Then 
						If Rand(800)=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							End If
						EndIf
					EndIf
					Stamina = Stamina - FPSfactor * 0.1
				Case 3 ;appendicitis
					;0.035/sec = 2.1/min
					If (Not I_427\Using And I_427\Timer < 70*360) Then
						SCP1025state[i]=SCP1025state[i]+FPSfactor*0.0005
					EndIf
					If SCP1025state[i]>20.0 Then
						If SCP1025state[i]-FPSfactor<=20.0 Then Msg="The pain in your stomach is becoming unbearable."
						Stamina = Stamina - FPSfactor * 0.3
					ElseIf SCP1025state[i]>10.0
						If SCP1025state[i]-FPSfactor<=10.0 Then Msg="Your stomach is aching."
					EndIf
				Case 4 ;asthma
					If Stamina < 35 Then
						If Rand(Int(140+Stamina*8))=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							End If
						EndIf
						CurrSpeed = CurveValue(0, CurrSpeed, 10+Stamina*15)
					EndIf
				Case 5;cardiac arrest
					If (Not I_427\Using And I_427\Timer < 70*360) Then
						SCP1025state[i]=SCP1025state[i]+FPSfactor*0.35
					EndIf
					;35/sec
					If SCP1025state[i]>110 Then
						HeartBeatRate=0
						BlurTimer = Max(BlurTimer, 500)
						If SCP1025state[i]>140 Then 
							DeathMSG = Chr(34)+"He died of a cardiac arrest after reading SCP-1025, that's for sure. Is there such a thing as psychosomatic cardiac arrest, or does SCP-1025 have some "
							DeathMSG = DeathMSG + "anomalous properties we are not yet aware of?"+Chr(34)
							Kill()
						EndIf
					Else
						HeartBeatRate=Max(HeartBeatRate, 70+SCP1025state[i])
						HeartBeatVolume = 1.0
					EndIf
			End Select 
		EndIf
	Next
	
	
End Function

Function DemoMovePlayer()
	CatchErrors("Uncaught (MovePlayer)")
	Local Sprint# = 1.0, Speed# = 0.018, i%, angle#
	
	If SuperMan Then
		Speed = Speed * 3
		
		SuperManTimer=SuperManTimer+FPSfactor
		
		CameraShake = Sin(SuperManTimer / 5.0) * (SuperManTimer / 1500.0)
		
		If SuperManTimer > 70 * 50 Then
			DeathMSG = "A Class D jumpsuit found in [DATA REDACTED]. Upon further examination, the jumpsuit was found to be filled with 12.5 kilograms of blue ash-like substance. "
			DeathMSG = DeathMSG + "Chemical analysis of the substance remains non-conclusive. Most likely related to SCP-914."
			Kill()
			ShowEntity Fog
		Else
			BlurTimer = 500		
			HideEntity Fog
		EndIf
	End If
	
	If DeathTimer > 0 Then
		DeathTimer=DeathTimer-FPSfactor
		If DeathTimer < 1 Then DeathTimer = -1.0
	ElseIf DeathTimer < 0 
		Kill()
	EndIf
		
	; Remove the players ability to drain and recoup stamina.
	; We just set the stamina to the value we get from the demo tick.
	;If CurrSpeed > 0 Then
    ;    Stamina = Min(Stamina + 0.15 * FPSfactor/1.25, 100.0)
    ;Else
    ;    Stamina = Min(Stamina + 0.15 * FPSfactor*1.25, 100.0)
    ;EndIf
	
	If StaminaEffectTimer > 0 Then
		StaminaEffectTimer = StaminaEffectTimer - (FPSfactor/70)
	Else
		If StaminaEffect <> 1.0 Then StaminaEffect = 1.0
	EndIf
	
	Local temp#
	
	If PlayerRoom\RoomTemplate\Name<>"pocketdimension" Then 
		If KeyDown(KEY_SPRINT) Then
			If Stamina < 5 Then
				temp = 0
				If WearingGasMask>0 Or Wearing1499>0 Then temp=1
				If ChannelPlaying(BreathCHN)=False Then BreathCHN = PlaySound_Strict(BreathSFX((temp), 0))
			ElseIf Stamina < 50
				If BreathCHN=0 Then
					temp = 0
					If WearingGasMask>0 Or Wearing1499>0 Then temp=1
					BreathCHN = PlaySound_Strict(BreathSFX((temp), Rand(1,3)))
					ChannelVolume BreathCHN, Min((70.0-Stamina)/70.0,1.0)*SFXVolume
				Else
					If ChannelPlaying(BreathCHN)=False Then
						temp = 0
						If WearingGasMask>0 Or Wearing1499>0 Then temp=1
						BreathCHN = PlaySound_Strict(BreathSFX((temp), Rand(1,3)))
						ChannelVolume BreathCHN, Min((70.0-Stamina)/70.0,1.0)*SFXVolume			
					EndIf
				EndIf
			EndIf
		EndIf
	EndIf
	
	For i = 0 To MaxItemAmount-1
		If Inventory(i)<>Null Then
			If Inventory(i)\itemtemplate\tempname = "finevest" Then Stamina = Min(Stamina, 60)
		EndIf
	Next
	
	If Wearing714 Then
		Stamina = Min(Stamina, 10)
		Sanity = Max(-850, Sanity)
	EndIf
	
	If IsZombie Then Crouch = False
	
	If Abs(CrouchState-Crouch)<0.001 Then 
		CrouchState = Crouch
	Else
		CrouchState = CurveValue(Crouch, CrouchState, 10.0)
	EndIf
	
	If (Not NoClip) Then 
		If ((KeyDown(KEY_DOWN) Or KeyDown(KEY_UP)) Or (KeyDown(KEY_RIGHT) Or KeyDown(KEY_LEFT)) And Playable) Or ForceMove>0 Then
			
			If Crouch = 0 And (KeyDown(KEY_SPRINT)) And Stamina > 0.0 And (Not IsZombie) Then
				Sprint = 2.5
				Stamina = Stamina - FPSfactor * 0.4 * StaminaEffect
				If Stamina <= 0 Then Stamina = -20.0
			End If
			
			If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then 
				If EntityY(Collider)<2000*RoomScale Or EntityY(Collider)>2608*RoomScale Then
					Stamina = 0
					Speed = 0.015
					Sprint = 1.0					
				EndIf
			EndIf	
			
			If ForceMove>0 Then Speed=Speed*ForceMove
			
			If SelectedItem<>Null Then
				If SelectedItem\itemtemplate\tempname = "firstaid" Or SelectedItem\itemtemplate\tempname = "finefirstaid" Or SelectedItem\itemtemplate\tempname = "firstaid2" Then 
					Sprint = 0
				EndIf
			EndIf
			
			temp# = (Shake Mod 360)
			Local tempchn%
			If (Not UnableToMove%) Then Shake# = (Shake + FPSfactor * Min(Sprint, 1.5) * 7) Mod 720
			If temp < 180 And (Shake Mod 360) >= 180 And KillTimer>=0 Then
				If CurrStepSFX=0 Then
					temp = GetStepSound(Collider)
					
					If Sprint = 1.0 Then
						PlayerSoundVolume = Max(4.0,PlayerSoundVolume)
						tempchn% = PlaySound_Strict(StepSFX(temp, 0, Rand(0, 7)))
						ChannelVolume tempchn, (1.0-(Crouch*0.6))*SFXVolume#
					Else
						PlayerSoundVolume = Max(2.5-(Crouch*0.6),PlayerSoundVolume)
						tempchn% = PlaySound_Strict(StepSFX(temp, 1, Rand(0, 7)))
						ChannelVolume tempchn, (1.0-(Crouch*0.6))*SFXVolume#
					End If
				ElseIf CurrStepSFX=1
					tempchn% = PlaySound_Strict(Step2SFX(Rand(0, 2)))
					ChannelVolume tempchn, (1.0-(Crouch*0.4))*SFXVolume#
				ElseIf CurrStepSFX=2
					tempchn% = PlaySound_Strict(Step2SFX(Rand(3,5)))
					ChannelVolume tempchn, (1.0-(Crouch*0.4))*SFXVolume#
				ElseIf CurrStepSFX=3
					If Sprint = 1.0 Then
						PlayerSoundVolume = Max(4.0,PlayerSoundVolume)
						tempchn% = PlaySound_Strict(StepSFX(0, 0, Rand(0, 7)))
						ChannelVolume tempchn, (1.0-(Crouch*0.6))*SFXVolume#
					Else
						PlayerSoundVolume = Max(2.5-(Crouch*0.6),PlayerSoundVolume)
						tempchn% = PlaySound_Strict(StepSFX(0, 1, Rand(0, 7)))
						ChannelVolume tempchn, (1.0-(Crouch*0.6))*SFXVolume#
					End If
				EndIf
				
			EndIf	
		EndIf
	Else ;noclip on
		If (KeyDown(KEY_SPRINT)) Then 
			Sprint = 2.5
		ElseIf KeyDown(KEY_CROUCH)
			Sprint = 0.5
		EndIf
	EndIf
	
	If KeyHit(KEY_CROUCH) And Playable Then Crouch = (Not Crouch)
	
	Local temp2# = (Speed * Sprint) / (1.0+CrouchState)
	
	If NoClip Then 
		Shake = 0
		CurrSpeed = 0
		CrouchState = 0
		Crouch = 0
		
		RotateEntity Collider, WrapAngle(EntityPitch(Camera)), WrapAngle(EntityYaw(Camera)), 0
		
		temp2 = temp2 * NoClipSpeed
		
		If KeyDown(KEY_DOWN) Then MoveEntity Collider, 0, 0, -temp2*FPSfactor
		If KeyDown(KEY_UP) Then MoveEntity Collider, 0, 0, temp2*FPSfactor
		
		If KeyDown(KEY_LEFT) Then MoveEntity Collider, -temp2*FPSfactor, 0, 0
		If KeyDown(KEY_RIGHT) Then MoveEntity Collider, temp2*FPSfactor, 0, 0	
		
		ResetEntity Collider
	Else
		temp2# = temp2 / Max((Injuries+3.0)/3.0,1.0)
		If Injuries > 0.5 Then 
			temp2 = temp2*Min((Sin(Shake/2)+1.2),1.0)
		EndIf
		
		temp = False
		If (Not IsZombie%)
			If KeyDown(KEY_DOWN) And Playable Then
				temp = True 
				angle = 180
				If KeyDown(KEY_LEFT) Then angle = 135 
				If KeyDown(KEY_RIGHT) Then angle = -135 
			ElseIf (KeyDown(KEY_UP) And Playable) Then; Or ForceMove>0
				temp = True
				angle = 0
				If KeyDown(KEY_LEFT) Then angle = 45 
				If KeyDown(KEY_RIGHT) Then angle = -45 
			ElseIf ForceMove>0 Then
				temp=True
				angle = ForceAngle
			Else If Playable Then
				If KeyDown(KEY_LEFT) Then angle = 90 : temp = True
				If KeyDown(KEY_RIGHT) Then angle = -90 : temp = True 
			EndIf
		Else
			temp=True
			angle = ForceAngle
		EndIf
		
		angle = WrapAngle(EntityYaw(Collider,True)+angle+90.0)
		
		If temp Then 
			CurrSpeed = CurveValue(temp2, CurrSpeed, 20.0)
		Else
			CurrSpeed = Max(CurveValue(0.0, CurrSpeed-0.1, 1.0),0.0)
		EndIf
		
		If (Not UnableToMove%) Then TranslateEntity Collider, Cos(angle)*CurrSpeed * FPSfactor, 0, Sin(angle)*CurrSpeed * FPSfactor, True
		
		Local CollidedFloor% = False
		For i = 1 To CountCollisions(Collider)
			If CollisionY(Collider, i) < EntityY(Collider) - 0.25 Then CollidedFloor = True
		Next
		
		If CollidedFloor = True Then
			If DropSpeed# < - 0.07 Then 
				If CurrStepSFX=0 Then
					PlaySound_Strict(StepSFX(GetStepSound(Collider), 0, Rand(0, 7)))
				ElseIf CurrStepSFX=1
					PlaySound_Strict(Step2SFX(Rand(0, 2)))
				ElseIf CurrStepSFX=2
					PlaySound_Strict(Step2SFX(Rand(3, 5)))
				ElseIf CurrStepSFX=3
					PlaySound_Strict(StepSFX(0, 0, Rand(0, 7)))
				EndIf
				PlayerSoundVolume = Max(3.0,PlayerSoundVolume)
			EndIf
			DropSpeed# = 0
		Else
			;DropSpeed# = Min(Max(DropSpeed - 0.006 * FPSfactor, -2.0), 0.0)
			If PlayerFallingPickDistance#<>0.0
				Local pick = LinePick(EntityX(Collider),EntityY(Collider),EntityZ(Collider),0,-PlayerFallingPickDistance,0)
				If pick
					DropSpeed# = Min(Max(DropSpeed - 0.006 * FPSfactor, -2.0), 0.0)
				Else
					DropSpeed# = 0
				EndIf
			Else
				DropSpeed# = Min(Max(DropSpeed - 0.006 * FPSfactor, -2.0), 0.0)
			EndIf
		EndIf
		PlayerFallingPickDistance# = 10.0
		
		If (Not UnableToMove%) And ShouldEntitiesFall Then TranslateEntity Collider, 0, DropSpeed * FPSfactor, 0
	EndIf
	
	ForceMove = False
	
	If Injuries > 1.0 Then
		temp2 = Bloodloss
		BlurTimer = Max(Max(Sin(MilliSecs2()/100.0)*Bloodloss*30.0,Bloodloss*2*(2.0-CrouchState)),BlurTimer)
		If (Not I_427\Using And I_427\Timer < 70*360) Then
			Bloodloss = Min(Bloodloss + (Min(Injuries,3.5)/300.0)*FPSfactor,100)
		EndIf
		
		If temp2 <= 60 And Bloodloss > 60 Then
			Msg = "You are feeling faint from the amount of blood you have lost."
			MsgTimer = 70*4
		EndIf
	EndIf
	
	UpdateInfect()
	
	If Bloodloss > 0 Then
		If Rnd(200)<Min(Injuries,4.0) Then
			pvt = CreatePivot()
			PositionEntity pvt, EntityX(Collider)+Rnd(-0.05,0.05),EntityY(Collider)-0.05,EntityZ(Collider)+Rnd(-0.05,0.05)
			TurnEntity pvt, 90, 0, 0
			EntityPick(pvt,0.3)
			de.decals = CreateDecal(Rand(15,16), PickedX(), PickedY()+0.005, PickedZ(), 90, Rand(360), 0)
			de\size = Rnd(0.03,0.08)*Min(Injuries,3.0) : EntityAlpha(de\obj, 1.0) : ScaleSprite de\obj, de\size, de\size
			tempchn% = PlaySound_Strict (DripSFX(Rand(0,2)))
			ChannelVolume tempchn, Rnd(0.0,0.8)*SFXVolume
			ChannelPitch tempchn, Rand(20000,30000)
			
			FreeEntity pvt
		EndIf
		
		CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs2())/20.0)+1.0)*Bloodloss*0.2)
		
		If Bloodloss > 60 Then Crouch = True
		If Bloodloss => 100 Then 
			Kill()
			HeartBeatVolume = 0.0
		ElseIf Bloodloss > 80.0
			HeartBeatRate = Max(150-(Bloodloss-80)*5,HeartBeatRate)
			HeartBeatVolume = Max(HeartBeatVolume, 0.75+(Bloodloss-80.0)*0.0125)	
		ElseIf Bloodloss > 35.0
			HeartBeatRate = Max(70+Bloodloss,HeartBeatRate)
			HeartBeatVolume = Max(HeartBeatVolume, (Bloodloss-35.0)/60.0)			
		EndIf
	EndIf
	
	If HealTimer > 0 Then
		DebugLog HealTimer
		HealTimer = HealTimer - (FPSfactor / 70)
		Bloodloss = Min(Bloodloss + (2 / 400.0) * FPSfactor, 100)
		Injuries = Max(Injuries - (FPSfactor / 70) / 30, 0.0)
	EndIf
		
	If Playable Then
		;If KeyHit(KEY_BLINK) Then BlinkTimer = 0
		;If KeyDown(KEY_BLINK) And BlinkTimer < - 10 Then BlinkTimer = -10
	EndIf
	
	
	If HeartBeatVolume > 0 Then
		If HeartBeatTimer <= 0 Then
			tempchn = PlaySound_Strict (HeartBeatSFX)
			ChannelVolume tempchn, HeartBeatVolume*SFXVolume#
			
			HeartBeatTimer = 70.0*(60.0/Max(HeartBeatRate,1.0))
		Else
			HeartBeatTimer = HeartBeatTimer - FPSfactor
		EndIf
		
		HeartBeatVolume = Max(HeartBeatVolume - FPSfactor*0.05, 0)
	EndIf
	
	CatchErrors("MovePlayer")
End Function

Function DemoUpdateNPCs()
	CatchErrors("Uncaught (UpdateNPCs)")
	Local n.NPCs, n2.NPCs, d.Doors, de.Decals, r.Rooms, eo.ElevatorObj, eo2.ElevatorObj
	Local i%, dist#, dist2#, angle#, x#, y#, z#, prevFrame#, PlayerSeeAble%, RN$
	
	Local target
	
	For n.NPCs = Each NPCs
		;A variable to determine if the NPC is in the facility or not
		n\InFacility = CheckForNPCInFacility(n)
		
		Select n\NPCtype
			Case NPCtype173
				;[Block]
				
				If Curr173\Idle <> 3 Then
					dist# = EntityDistance(n\Collider, Collider)		
					
					n\State3 = 1
					
					If n\Idle < 2 Then
						If n\IdleTimer > 0.1
							n\Idle = 1
							n\IdleTimer = Max(n\IdleTimer-FPSfactor,0.1)
						ElseIf n\IdleTimer = 0.1
							n\Idle = 0
							n\IdleTimer = 0
						EndIf
						
						PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.32, EntityZ(n\Collider))
						RotateEntity (n\obj, 0, EntityYaw(n\Collider)-180, 0)
						
						If n\Idle = False Then
							Local temp% = False
							Local move% = True
							If dist < 15 Then
								If dist < 10.0 Then 
									If EntityVisible(n\Collider, Collider) Then
										temp = True
										n\EnemyX = EntityX(Collider, True)
										n\EnemyY = EntityY(Collider, True)
										n\EnemyZ = EntityZ(Collider, True)
									EndIf
								EndIf										
								
								Local SoundVol# = Max(Min((Distance(EntityX(n\Collider), EntityZ(n\Collider), n\PrevX, n\PrevZ) * 2.5), 1.0), 0.0)
								n\SoundChn = LoopSound2(StoneDragSFX, n\SoundChn, Camera, n\Collider, 10.0, n\State)
								
								n\PrevX = EntityX(n\Collider)
								n\PrevZ = EntityZ(n\Collider)				
								
								If (BlinkTimer < - 16 Or BlinkTimer > - 6) And (IsNVGBlinking=False) Then
									If EntityInView(n\obj, Camera) Then move = False
								EndIf
							EndIf
							
							If NoTarget Then move = True
							
							;player is looking at it -> doesn't move
							If move=False Then
								BlurVolume = Max(Max(Min((4.0 - dist) / 6.0, 0.9), 0.1), BlurVolume)
								CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs2())/20.0)+1.0)*15.0*Max((3.5-dist)/3.5,0.0))								
								
								If dist < 3.5 And MilliSecs2() - n\LastSeen > 60000 And temp Then
									PlaySound_Strict(HorrorSFX(Rand(3,4)))
									
									n\LastSeen = MilliSecs2()
								EndIf
								
								If dist < 1.5 And Rand(700) = 1 Then PlaySound2(Scp173SFX(Rand(0, 2)), Camera, n\obj)
								
								If dist < 1.5 And n\LastDist > 2.0 And temp Then
									CurrCameraZoom = 40.0
									HeartBeatRate = Max(HeartBeatRate, 140)
									HeartBeatVolume = 0.5
									
									Select Rand(5)
										Case 1
											PlaySound_Strict(HorrorSFX(1))
										Case 2
											PlaySound_Strict(HorrorSFX(2))
										Case 3
											PlaySound_Strict(HorrorSFX(9))
										Case 4
											PlaySound_Strict(HorrorSFX(10))
										Case 5
											PlaySound_Strict(HorrorSFX(14))
									End Select
								EndIf									
									
								n\LastDist = dist
								
								n\State = Max(0, n\State - FPSfactor / 20)
							Else 
								;more than 6 room lengths away from the player -> teleport to a room closer to the player
								;If dist > 50 Then
								;	If Rand(70)=1 Then
								;		If PlayerRoom\RoomTemplate\Name <> "exit1" And PlayerRoom\RoomTemplate\Name <> "gatea" And PlayerRoom\RoomTemplate\Name <> "pocketdimension" Then
								;			For w.waypoints = Each WayPoints
								;				If w\door=Null And Rand(5)=1 Then
								;					x = Abs(EntityX(Collider)-EntityX(w\obj,True))
								;					If x < 25.0 And x > 15.0 Then
								;						z = Abs(EntityZ(Collider)-EntityZ(w\obj,True))
								;						If z < 25 And z > 15.0 Then
								;							DebugLog "MOVING 173 TO "+w\room\roomtemplate\name
								;							PositionEntity n\Collider, EntityX(w\obj,True), EntityY(w\obj,True)+0.25,EntityZ(w\obj,True)
								;							ResetEntity n\Collider
								;							Exit
								;						EndIf
								;					EndIf
								;						
								;				EndIf
								;			Next
								;		EndIf
								;	EndIf
								;ElseIf dist > HideDistance*0.8 ;3-6 rooms away from the player -> move randomly from waypoint to another
								;	If Rand(70)=1 Then TeleportCloser(n)
								;Else ;less than 3 rooms away -> actively move towards the player
								;	n\State = CurveValue(SoundVol, n\State, 3)
								;	
								;	;try to open doors
								;	If Rand(20) = 1 Then
								;		For d.Doors = Each Doors
								;			If (Not d\locked) And d\open = False And d\Code = "" And d\KeyCard=0 Then
								;				For i% = 0 To 1
								;					If d\buttons[i] <> 0 Then
								;						If Abs(EntityX(n\Collider) - EntityX(d\buttons[i])) < 0.5 Then
								;							If Abs(EntityZ(n\Collider) - EntityZ(d\buttons[i])) < 0.5 Then
								;								If (d\openstate >= 180 Or d\openstate <= 0) Then
								;									pvt = CreatePivot()
								;									PositionEntity pvt, EntityX(n\Collider), EntityY(n\Collider) + 0.5, EntityZ(n\Collider)
								;									PointEntity pvt, d\buttons[i]
								;									MoveEntity pvt, 0, 0, n\Speed * 0.6
								;									
								;									If EntityPick(pvt, 0.5) = d\buttons[i] Then 
								;										PlaySound_Strict (LoadTempSound("SFX\Door\DoorOpen173.ogg"))
								;										UseDoor(d,False)
								;									EndIf
								;									
								;									FreeEntity pvt
								;								EndIf
								;							EndIf
								;						EndIf
								;					EndIf
								;				Next
								;			EndIf
								;		Next
								;	EndIf
								;	
								;	If NoTarget
								;		temp = False
								;		n\EnemyX = 0
								;		n\EnemyY = 0
								;		n\EnemyZ = 0
								;	EndIf
								;	
								;	;player is not looking and is visible from 173's position -> attack
								;	If temp Then 				
								;		If dist < 0.65 Then
								;			If KillTimer >= 0 And (Not GodMode) Then
								;				
								;				Select PlayerRoom\RoomTemplate\Name
								;					Case "lockroom", "room2closets", "coffin"
								;						DeathMSG = "Subject D-9341. Cause of death: Fatal cervical fracture. The surveillance tapes confirm that the subject was killed by SCP-173."	
								;					Case "173"
								;						DeathMSG = "Subject D-9341. Cause of death: Fatal cervical fracture. According to Security Chief Franklin who was present at SCP-173's containment "
								;						DeathMSG = DeathMSG + "chamber during the breach, the subject was killed by SCP-173 as soon as the disruptions in the electrical network started."
								;					Case "room2doors"
								;						DeathMSG = Chr(34)+"If I'm not mistaken, one of the main purposes of these rooms was to stop SCP-173 from moving further in the event of a containment breach. "
								;						DeathMSG = DeathMSG + "So, who's brilliant idea was it to put A GODDAMN MAN-SIZED VENTILATION DUCT in there?"+Chr(34)
								;					Default 
								;						DeathMSG = "Subject D-9341. Cause of death: Fatal cervical fracture. Assumed to be attacked by SCP-173."
								;				End Select
								;				
								;				If (Not GodMode) Then n\Idle = True
								;				PlaySound_Strict(NeckSnapSFX(Rand(0,2)))
								;				If Rand(2) = 1 Then 
								;					TurnEntity(Camera, 0, Rand(80,100), 0)
								;				Else
								;					TurnEntity(Camera, 0, Rand(-100,-80), 0)
								;				EndIf
								;				Kill()
								;				
								;			EndIf
								;		Else
								;			PointEntity(n\Collider, Collider)
								;			RotateEntity n\Collider, 0, EntityYaw(n\Collider), EntityRoll(n\Collider)
								;			MoveEntity(n\Collider, 0, 0, n\Speed * FPSfactor)
								;			TranslateEntity n\Collider,Cos(EntityYaw(n\Collider)+90.0)*n\Speed*FPSfactor,0.0,Sin(EntityYaw(n\Collider)+90.0)*n\Speed*FPSfactor
								;		EndIf
								;		
								;	Else ;player is not visible -> move to the location where he was last seen							
								;		If n\EnemyX <> 0 Then						
								;			If Distance(EntityX(n\Collider), EntityZ(n\Collider), n\EnemyX, n\EnemyZ) > 0.5 Then
								;				AlignToVector(n\Collider, n\EnemyX-EntityX(n\Collider), 0, n\EnemyZ-EntityZ(n\Collider), 3)
								;				MoveEntity(n\Collider, 0, 0, n\Speed * FPSfactor)
								;				If Rand(500) = 1 Then n\EnemyX = 0 : n\EnemyY = 0 : n\EnemyZ = 0
								;			Else
								;				n\EnemyX = 0 : n\EnemyY = 0 : n\EnemyZ = 0
								;			End If
								;		Else
								;			If Rand(400)=1 Then RotateEntity (n\Collider, 0, Rnd(360), 10)
								;			TranslateEntity n\Collider,Cos(EntityYaw(n\Collider)+90.0)*n\Speed*FPSfactor,0.0,Sin(EntityYaw(n\Collider)+90.0)*n\Speed*FPSfactor
								;			
								;		End If
								;	EndIf
								;	
								;EndIf ; less than 2 rooms away from the player
								
							EndIf
							
						EndIf ;idle = false
						
						PositionEntity(n\Collider, EntityX(n\Collider), Min(EntityY(n\Collider),0.35), EntityZ(n\Collider))
						
					Else ;idle = 2
						
						If n\Target <> Null Then
							Local tmp = False
							If dist > HideDistance*0.7
								If EntityVisible(n\obj,Collider)=False
									tmp = True
								EndIf
							EndIf
							If (Not tmp)
								PointEntity n\obj, n\Target\Collider
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),10.0), 0, True								
								dist = EntityDistance(n\Collider, n\Target\Collider)
								MoveEntity n\Collider, 0, 0, 0.008*FPSfactor*Max(Min((dist*2-1.0)*0.5,1.0),-0.5)
								MoveEntity n\Collider, 0, 0, 0.016*FPSfactor*Max(Min((dist*2-1.0)*0.5,1.0),-0.5)
								n\GravityMult = 1.0
							Else
								PositionEntity n\Collider,EntityX(n\Target\Collider),EntityY(n\Target\Collider)+0.3,EntityZ(n\Target\Collider)
								ResetEntity n\Collider
								n\DropSpeed = 0
								n\GravityMult = 0.0
								PointEntity n\Collider, n\Target\Collider
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),10.0), 0, True
								dist = EntityDistance(n\Collider, n\Target\Collider)
								MoveEntity n\Collider, 0, 0, dist-0.6
							EndIf
							
							;For r.Rooms = Each Rooms
							;	If r\RoomTemplate\Name = "start" Then
							;		If Distance(EntityX(n\Collider),EntityZ(n\Collider),EntityX(r\obj,True)+1024*RoomScale,EntityZ(r\obj,True)+384*RoomScale)<1.6 Then
							;			n\Idle = 3
							;			n\Target = Null
							;		EndIf
							;		Exit
							;	EndIf
							;Next
						EndIf
						
						PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) + 0.05 + Sin(MilliSecs2()*0.08)*0.02, EntityZ(n\Collider))
						RotateEntity (n\obj, 0, EntityYaw(n\Collider)-180, 0)
						
						ShowEntity n\obj2
						
						PositionEntity(n\obj2, EntityX(n\Collider), EntityY(n\Collider) - 0.05 + Sin(MilliSecs2()*0.08)*0.02, EntityZ(n\Collider))
						RotateEntity (n\obj2, 0, EntityYaw(n\Collider)-180, 0)
					EndIf
				EndIf
				
				;[End block]
			Case NPCtypeOldMan ;------------------------------------------------------------------------------------------------------------------
				;[Block]
				If Contained106 Then
					n\Idle = True
					HideEntity n\obj
					HideEntity n\obj2
					PositionEntity n\obj, 0,500.0,0, True
				Else
					
					dist = EntityDistance(n\Collider, Collider)
					
					Local spawn106% = True
					;checking if 106 is allowed to spawn
					If PlayerRoom\RoomTemplate\Name$ = "dimension1499" Then spawn106% = False
					For e.Events = Each Events
						If e\EventName = "room860"
							If e\EventState = 1
								spawn106% = False
							EndIf
							Exit
						EndIf
					Next
					If PlayerRoom\RoomTemplate\Name$ = "room049" And EntityY(Collider) <= -2848*RoomScale Then
						spawn106% = False
					EndIf
					;GateA event has been triggered - don't make 106 disapper!
					;The reason why this is a seperate For loop is because we need to make sure that room860 would not be able to overwrite the "spawn106%" variable
					For e.events = Each Events
						If e\EventName = "gatea"
							If e\EventState <> 0
								spawn106% = True
								If PlayerRoom\RoomTemplate\Name$ = "dimension1499" Then
									n\Idle = True
								Else
									n\Idle = False
								EndIf
							EndIf
							Exit
						EndIf
					Next
					If (Not spawn106%) And n\State <= 0 Then
						n\State = Rand(22000, 27000)
						PositionEntity n\Collider,0,500,0
					EndIf
					
					If (Not n\Idle) And spawn106%
						If n\State <= 0 Then	;attacking	
							If EntityY(n\Collider) < EntityY(Collider) - 20.0 - 0.55 Then
								If Not PlayerRoom\RoomTemplate\DisableDecals Then
									de.Decals = CreateDecal(0, EntityX(Collider), 0.01, EntityZ(Collider), 90, Rand(360), 0)
									de\Size = 0.05 : de\SizeChange = 0.001 : EntityAlpha(de\obj, 0.8) : UpdateDecals
								EndIf
								
								n\PrevY = EntityY(Collider)
								
								SetAnimTime n\obj, 110
								
								If PlayerRoom\RoomTemplate\Name <> "coffin"
									PositionEntity(n\Collider, EntityX(Collider), EntityY(Collider) - 15, EntityZ(Collider))
								EndIf
								
								PlaySound_Strict(DecaySFX(0))
							End If
							
							If Rand(500) = 1 Then PlaySound2(OldManSFX(Rand(0, 2)), Camera, n\Collider)
							n\SoundChn = LoopSound2(OldManSFX(4), n\SoundChn, Camera, n\Collider, 8.0, 0.8)
							
							If n\State > - 10 Then
								ShouldPlay = 66
								If n\Frame<259 Then
									PositionEntity n\Collider, EntityX(n\Collider), n\PrevY-0.15, EntityZ(n\Collider)
									PointEntity n\obj, Collider
									RotateEntity (n\Collider, 0, CurveValue(EntityYaw(n\obj),EntityYaw(n\Collider),100.0), 0, True)
									
									AnimateNPC(n, 110, 259, 0.15, False)
								Else
									n\State = -10
								EndIf
							Else
								If PlayerRoom\RoomTemplate\Name <> "gatea" Then ShouldPlay = 10
								
								Local Visible% = False
								If dist < 8.0 Then
									Visible% = EntityVisible(n\Collider, Collider)
								EndIf
								
								If NoTarget Then Visible = False
								
								If Visible Then
									If PlayerRoom\RoomTemplate\Name <> "gatea" Then n\PathTimer = 0
									If EntityInView(n\Collider, Camera) Then
										GiveAchievement(Achv106)
										
										;Achievements(Achv106) = True
										
										BlurVolume = Max(Max(Min((4.0 - dist) / 6.0, 0.9), 0.1), BlurVolume)
										CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs2())/20.0)+1.0) * 20.0 * Max((4.0-dist)/4.0,0))
										
										If MilliSecs2() - n\LastSeen > 60000 Then 
											CurrCameraZoom = 40
											PlaySound_Strict(HorrorSFX(6))
											n\LastSeen = MilliSecs2()
										EndIf
									EndIf
								Else
									n\State=n\State-FPSfactor
								End If
								
								If dist > 0.8 Then
									If (dist > 25.0 Or PlayerRoom\RoomTemplate\Name = "pocketdimension" Or Visible Or n\PathStatus <> 1) And PlayerRoom\RoomTemplate\Name <> "gatea" Then 
										
										If (dist > 40 Or PlayerRoom\RoomTemplate\Name = "pocketdimension") Then
											TranslateEntity n\Collider, 0, ((EntityY(Collider) - 0.14) - EntityY(n\Collider)) / 50.0, 0
										EndIf
										
										n\CurrSpeed = CurveValue(n\Speed,n\CurrSpeed,10.0)
										
										PointEntity n\obj, Collider
										RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 10.0), 0
										
										If KillTimer >= 0 Then
											prevFrame# = n\Frame
											AnimateNPC(n, 284, 333, n\CurrSpeed*43)
											;Animate2(n\obj, AnimTime(n\obj), 284, 333, n\CurrSpeed*43)
											
											If prevFrame =< 286 And n\Frame>286 Then
												PlaySound2(Step2SFX(Rand(0,2)),Camera, n\Collider, 6.0, Rnd(0.8,1.0))	
											ElseIf prevFrame=<311 And n\Frame>311.0 
												PlaySound2(Step2SFX(Rand(0,2)),Camera, n\Collider, 6.0, Rnd(0.8,1.0))
											EndIf
										Else 
											n\CurrSpeed = 0
										EndIf
										
										n\PathTimer = Max(n\PathTimer-FPSfactor,0)
										If n\PathTimer =< 0 Then
											n\PathStatus = FindPath (n, EntityX(Collider,True), EntityY(Collider,True), EntityZ(Collider,True))
											n\PathTimer = 70*10
										EndIf
									Else 
										If n\PathTimer <= 0 Then
											n\PathStatus = FindPath (n, EntityX(Collider,True), EntityY(Collider,True), EntityZ(Collider,True))
											n\PathTimer = 70*10
											n\CurrSpeed = 0
										Else
											n\PathTimer = Max(n\PathTimer-FPSfactor,0)
											
											If n\PathStatus = 2 Then
												n\CurrSpeed = 0
											ElseIf n\PathStatus = 1
												While n\Path[n\PathLocation]=Null
													If n\PathLocation > 19 Then 
														n\PathLocation = 0 : n\PathStatus = 0
														Exit
													Else
														n\PathLocation = n\PathLocation + 1
													EndIf
												Wend
												
												If n\Path[n\PathLocation]<>Null Then 
													TranslateEntity n\Collider, 0, ((EntityY(n\Path[n\PathLocation]\obj,True) - 0.11) - EntityY(n\Collider)) / 50.0, 0
													
													PointEntity n\obj, n\Path[n\PathLocation]\obj
													
													dist2# = EntityDistance(n\Collider,n\Path[n\PathLocation]\obj)
													
													RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), Min(20.0,dist2*10.0)), 0
													n\CurrSpeed = CurveValue(n\Speed,n\CurrSpeed,10.0)
													
													prevFrame# = AnimTime(n\obj)
													AnimateNPC(n, 284, 333, n\CurrSpeed*43)
													;Animate2(n\obj, AnimTime(n\obj), 284, 333, n\CurrSpeed*43)
													If prevFrame =< 286 And n\Frame>286 Then
														PlaySound2(Step2SFX(Rand(0,2)),Camera, n\Collider, 6.0, Rnd(0.8,1.0))	
													ElseIf prevFrame=<311 And n\Frame>311.0 
														PlaySound2(Step2SFX(Rand(0,2)),Camera, n\Collider, 6.0, Rnd(0.8,1.0))
													EndIf
													
													If dist2 < 0.2 Then n\PathLocation = n\PathLocation + 1
												EndIf
											ElseIf n\PathStatus = 0
												If n\State3=0 Then AnimateNPC(n, 334, 494, 0.3)
												;Animate2(n\obj, AnimTime(n\obj), 334, 494, 0.3)
												n\CurrSpeed = CurveValue(0,n\CurrSpeed,10.0)
											EndIf
										EndIf
										
									EndIf
									
								ElseIf PlayerRoom\RoomTemplate\Name <> "gatea" And (Not NoTarget) ;dist < 0.8
									
									If dist > 0.5 Then 
										n\CurrSpeed = CurveValue(n\Speed * 2.5,n\CurrSpeed,10.0)
									Else
										n\CurrSpeed = 0
									EndIf
									AnimateNPC(n, 105, 110, 0.15, False)
									;Animate2(n\obj, AnimTime(n\obj), 105, 110, 0.15, False)
									;If Floor(AnimTime(n\obj)) = 43 Then SetAnimTime(n\obj, 43)
									
									If KillTimer >= 0 And FallTimer >= 0 Then
										PointEntity n\obj, Collider
										RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 10.0), 0										
										
										If Ceil(n\Frame) = 110 And (Not GodMode) Then
											PlaySound_Strict(DamageSFX(1))
											PlaySound_Strict(HorrorSFX(5))											
											If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
												DeathMSG = "Subject D-9341. Body partially decomposed by what is assumed to be SCP-106's "+Chr(34)+"corrosion"+Chr(34)+" effect. Body disposed of via incineration."
												Kill()
											Else
												PlaySound_Strict(OldManSFX(3))
												FallTimer = Min(-1, FallTimer)
												PositionEntity(Head, EntityX(Camera, True), EntityY(Camera, True), EntityZ(Camera, True), True)
												ResetEntity (Head)
												RotateEntity(Head, 0, EntityYaw(Camera) + Rand(-45, 45), 0)
											EndIf
										EndIf
									EndIf
									
								EndIf
								
							EndIf 
							
							MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
							
							If n\State <= Rand(-3500, -3000) Then 
								If Not EntityInView(n\obj,Camera) And dist > 5 Then
									n\State = Rand(22000, 27000)
									PositionEntity n\Collider,0,500,0
								EndIf
							EndIf
							
							If FallTimer < -250.0 Then
								MoveToPocketDimension()
								n\State = 250 ;make 106 idle for a while
							EndIf
							
							If n\Reload = 0 Then
                                If dist > 10 And PlayerRoom\RoomTemplate\Name <> "pocketdimension" And PlayerRoom\RoomTemplate\Name <> "gatea" And n\State <-5 Then ;timer idea by Juanjpro
                                    If (Not EntityInView(n\obj,Camera))
                                        TurnEntity Collider,0,180,0
                                        pick = EntityPick(Collider,5)
                                        TurnEntity Collider,0,180,0
                                        If pick<>0
											TeleportEntity(n\Collider,PickedX(),PickedY(),PickedZ(),n\CollRadius)
                                            PointEntity(n\Collider,Collider)
                                            RotateEntity(n\Collider,0,EntityYaw(n\Collider),0)
                                            MoveEntity(n\Collider,0,0,-2)
                                            PlaySound2(OldManSFX(3),Camera,n\Collider)
											n\SoundChn2 = PlaySound2(OldManSFX(6+Rand(0,2)),Camera,n\Collider)
                                            n\PathTimer = 0
                                            n\Reload = (70*10.0)/(SelectedDifficulty\otherFactors+1)
                                            DebugLog "Teleported 106 (Distance: "+EntityDistance(n\Collider,Collider)+")"
                                        EndIf
                                    EndIf
                                EndIf
                            EndIf
                            n\Reload = Max(0, n\Reload - FPSfactor)
                            DebugLog "106 in... "+n\Reload 
							
							UpdateSoundOrigin(n\SoundChn2,Camera,n\Collider)
						Else ;idling outside the map
							n\CurrSpeed = 0
							MoveEntity n\Collider, 0, ((EntityY(Collider) - 30) - EntityY(n\Collider)) / 200.0, 0
							n\DropSpeed = 0
							n\Frame = 110
							;SetAnimTime n\obj, 110.0
							
							If (Not PlayerRoom\RoomTemplate\DisableDecals) Then
								If PlayerRoom\RoomTemplate\Name <> "gatea"
									If (SelectedDifficulty\aggressiveNPCs) Then
										n\State=n\State-FPSfactor*2
									Else
										n\State=n\State-FPSfactor
									EndIf
								EndIf
							EndIf
						End If
						
						ResetEntity(n\Collider)
						n\DropSpeed = 0
						PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.15, EntityZ(n\Collider))
						
						RotateEntity n\obj, 0, EntityYaw(n\Collider), 0
						
						PositionEntity(n\obj2, EntityX(n\obj), EntityY(n\obj) , EntityZ(n\obj))
						RotateEntity(n\obj2, 0, EntityYaw(n\Collider) - 180, 0)
						MoveEntity(n\obj2, 0, 8.6 * 0.11, -1.5 * 0.11)
						
						If PlayerRoom\RoomTemplate\Name = "pocketdimension" Or PlayerRoom\RoomTemplate\Name = "gatea" Then
							HideEntity n\obj2
						Else
							If dist < CameraFogFar*LightVolume*0.6 Then
								HideEntity n\obj2
							Else
								ShowEntity n\obj2
								EntityAlpha (n\obj2, Min(dist-CameraFogFar*LightVolume*0.6,1.0))
							EndIf
						EndIf						
					Else
						HideEntity n\obj2
					EndIf
					
				EndIf
				
				;[End Block]
			Case NPCtype096
				;[Block]
				dist = EntityDistance(Collider, n\Collider)
				
				Select n\State
					Case 0
						;[Block]
						If dist<8.0 Then
							GiveAchievement(Achv096)
							;If n\Sound = 0 Then
							;	n\Sound = LoadSound_Strict("SFX\Music\096.ogg")
							;Else
							;	n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 8.0, 1.0)
							;EndIf
							If n\SoundChn = 0
								n\SoundChn = StreamSound_Strict("SFX\Music\096.ogg",0)
								n\SoundChn_IsStream = True
							Else
								UpdateStreamSoundOrigin(n\SoundChn,Camera,n\Collider,8.0,1.0)
							EndIf
							
							If n\State3 = -1
								AnimateNPC(n,936,1263,0.1,False)
								If n\Frame=>1262.9
									n\State = 5
									n\State3 = 0
									n\Frame = 312
								EndIf
							Else
								AnimateNPC(n,936,1263,0.1)
								If n\State3 < 70*6
									n\State3=n\State3+FPSfactor
								Else
									If Rand(1,5)=1
										n\State3 = -1
									Else
										n\State3=70*(Rand(0,3))
									EndIf
								EndIf
							EndIf
							;AnimateNPC(n, 1085,1412, 0.1) ;sitting
							
							angle = WrapAngle(DeltaYaw(n\Collider, Collider));-EntityYaw(n\Collider,True))
							
							If (Not NoTarget)
								If angle<90 Or angle>270 Then
									CameraProject Camera,EntityX(n\Collider), EntityY(n\Collider)+0.25, EntityZ(n\Collider)
									
									If ProjectedX()>0 And ProjectedX()<GraphicWidth Then
										If ProjectedY()>0 And ProjectedY()<GraphicHeight Then
											If EntityVisible(Collider, n\Collider) Then
												If (BlinkTimer < - 16 Or BlinkTimer > - 6)
													PlaySound_Strict LoadTempSound("SFX\SCP\096\Triggered.ogg")
													
													CurrCameraZoom = 10
													
													n\Frame = 194
													;n\Frame = 307
													StopStream_Strict(n\SoundChn) : n\SoundChn=0
													n\Sound = 0
													n\State = 1
													n\State3 = 0
												EndIf
											EndIf									
										EndIf
									EndIf								
									
								EndIf
							EndIf
						EndIf
						;[End Block]
					Case 4
						;[Block]
						CurrCameraZoom = CurveValue(Max(CurrCameraZoom, (Sin(Float(MilliSecs2())/20.0)+1.0) * 10.0),CurrCameraZoom,8.0)
						
						If n\Target = Null Then 
							;If n\Sound = 0 Then
							;	n\Sound = LoadSound_Strict("SFX\SCP\096\Scream.ogg")
							;Else
							;	n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 7.5, 1.0)
							;EndIf
							If n\SoundChn = 0
								n\SoundChn = StreamSound_Strict("SFX\SCP\096\Scream.ogg",0)
								n\SoundChn_IsStream = True
							Else
								UpdateStreamSoundOrigin(n\SoundChn,Camera,n\Collider,7.5,1.0)
							EndIf
							
							;If n\Sound2 = 0 Then
							;	n\Sound2 = LoadSound_Strict("SFX\Music\096Chase.ogg")
							;Else
							;	If n\SoundChn2 = 0 Then
							;		n\SoundChn2 = PlaySound_Strict (n\Sound2)
							;	Else
							;		If (Not ChannelPlaying(n\SoundChn2)) Then n\SoundChn2 = PlaySound_Strict(n\Sound2)
							;		ChannelVolume(n\SoundChn2, Min(Max(8.0-dist,0.6),1.0)*SFXVolume#)
							;	EndIf
							;EndIf
							If n\SoundChn2 = 0
								n\SoundChn2 = StreamSound_Strict("SFX\Music\096Chase.ogg",0)
								n\SoundChn2_IsStream = 2
							Else
								SetStreamVolume_Strict(n\SoundChn2,Min(Max(8.0-dist,0.6),1.0)*SFXVolume#)
							EndIf
						EndIf
						
						If NoTarget And n\Target = Null Then n\State = 5
						
						If KillTimer =>0 Then
							
							If MilliSecs2() > n\State3 Then
								n\LastSeen=0
								If n\Target=Null Then
									If EntityVisible(Collider, n\Collider) Then n\LastSeen=1
								Else
									If EntityVisible(n\Target\Collider, n\Collider) Then n\LastSeen=1
								EndIf
								n\State3=MilliSecs2()+3000
							EndIf
							
							If n\LastSeen=1 Then
								n\PathTimer=Max(70*3, n\PathTimer)
								n\PathStatus=0
								
								If n\Target<> Null Then dist = EntityDistance(n\Target\Collider, n\Collider)
								
								If dist < 2.8 Or n\Frame<150 Then 
									If n\Frame>193 Then n\Frame = 2.0 ;go to the start of the jump animation
									
									AnimateNPC(n, 2, 193, 0.7)
									
									If dist > 1.0 Then 
										n\CurrSpeed = CurveValue(n\Speed*2.0,n\CurrSpeed,15.0)
									Else
										n\CurrSpeed = 0
										
										If n\Target=Null Then
											If (Not GodMode) Then 
												PlaySound_Strict DamageSFX(4)
												
												pvt = CreatePivot()
												CameraShake = 30
												BlurTimer = 2000
												DeathMSG = "A large amount of blood found in [DATA REDACTED]. DNA indentified as Subject D-9341. Most likely [DATA REDACTED] by SCP-096."
												Kill()
												KillAnim = 1
												For i = 0 To 6
													PositionEntity pvt, EntityX(Collider)+Rnd(-0.1,0.1),EntityY(Collider)-0.05,EntityZ(Collider)+Rnd(-0.1,0.1)
													TurnEntity pvt, 90, 0, 0
													EntityPick(pvt,0.3)
													
													de.Decals = CreateDecal(Rand(15,16), PickedX(), PickedY()+0.005, PickedZ(), 90, Rand(360), 0)
													de\Size = Rnd(0.2,0.6) : EntityAlpha(de\obj, 1.0) : ScaleSprite de\obj, de\Size, de\Size
												Next
												FreeEntity pvt
											EndIf
										EndIf				
									EndIf
									
									If n\Target=Null Then
										PointEntity n\Collider, Collider
									Else
										PointEntity n\Collider, n\Target\Collider
									EndIf
									
								Else
									If n\Target=Null Then 
										PointEntity n\obj, Collider
									Else
										PointEntity n\obj, n\Target\Collider
									EndIf
									
									RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 5.0), 0
									
									;1000
									If n\Frame>847 Then n\CurrSpeed = CurveValue(n\Speed,n\CurrSpeed,20.0)
									
									If n\Frame<906 Then ;1058
										AnimateNPC(n,737,906,n\Speed*8,False)
										;AnimateNPC(n, 892,1058, n\Speed*8, False)
									Else
										AnimateNPC(n,907,935,n\CurrSpeed*8)
										;AnimateNPC(n, 1059,1084, n\CurrSpeed*8)	
									EndIf
								EndIf
								
								RotateEntity n\Collider, 0, EntityYaw(n\Collider), 0, True
								MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor
								
							Else
								If n\PathStatus = 1 Then
									
									If n\Path[n\PathLocation]=Null Then 
										If n\PathLocation > 19 Then 
											n\PathLocation = 0 : n\PathStatus = 0
										Else
											n\PathLocation = n\PathLocation + 1
										EndIf
									Else
										PointEntity n\obj, n\Path[n\PathLocation]\obj
										
										RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 5.0), 0
										
										;1000
										If n\Frame>847 Then n\CurrSpeed = CurveValue(n\Speed*1.5,n\CurrSpeed,15.0)
										MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor
										
										If n\Frame<906 Then ;1058
											AnimateNPC(n,737,906,n\Speed*8,False)
											;AnimateNPC(n, 892,1058, n\Speed*8, False)
										Else
											AnimateNPC(n,907,935,n\CurrSpeed*8)
											;AnimateNPC(n, 1059,1084, n\CurrSpeed*8)	
										EndIf
										
										dist2# = EntityDistance(n\Collider,n\Path[n\PathLocation]\obj)
										If dist2 < 0.8 Then ;0.4
											If n\Path[n\PathLocation]\door <> Null Then
												If n\Path[n\PathLocation]\door\open = False Then
													n\Path[n\PathLocation]\door\open = True
													n\Path[n\PathLocation]\door\fastopen = 1
													PlaySound2(OpenDoorFastSFX, Camera, n\Path[n\PathLocation]\door\obj)
												EndIf
											EndIf							
											If dist2 < 0.7 Then n\PathLocation = n\PathLocation + 1 ;0.2
										EndIf 
									EndIf
									
								Else
									;AnimateNPC(n, 892,972, 0.2)
									AnimateNPC(n,737,822,0.2)
									
									n\PathTimer = Max(0, n\PathTimer-FPSfactor)
									If n\PathTimer=<0 Then
										If n\Target<>Null Then
											n\PathStatus = FindPath(n, EntityX(n\Target\Collider),EntityY(n\Target\Collider)+0.2,EntityZ(n\Target\Collider))	
										Else
											n\PathStatus = FindPath(n, EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))	
										EndIf
										n\PathTimer = 70*5
									EndIf
								EndIf
							EndIf
							
							If dist > 32.0 Or EntityY(n\Collider)<-50 Then
								If Rand(50)=1 Then TeleportCloser(n)
							EndIf
						Else ;play the eating animation if killtimer < 0 
							AnimateNPC(n, Min(27,AnimTime(n\obj)), 193, 0.5)
							
							;Animate2(n\obj, AnimTime(n\obj), Min(27,AnimTime(n\obj)), 193, 0.5)
						EndIf
						
						
						;[End Block]
					Case 1,2,3
						;[Block]
						;If n\Sound = 0 Then
						;	n\Sound = LoadSound_Strict("SFX\Music\096Angered.ogg")
						;Else
						;	n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 10.0, 1.0)
						;EndIf
						If n\SoundChn = 0
							n\SoundChn = StreamSound_Strict("SFX\Music\096Angered.ogg",0)
							n\SoundChn_IsStream = True
						Else
							UpdateStreamSoundOrigin(n\SoundChn,Camera,n\Collider,10.0,1.0)
						EndIf
						
						If n\State=1 Then ; get up
							If n\Frame<312
								AnimateNPC(n,193,311,0.3,False)
								If n\Frame > 310.9 Then n\State = 2 : n\Frame = 737
							ElseIf n\Frame>=312 And n\Frame<=422
								AnimateNPC(n,312,422,0.3,False)
								If n\Frame > 421.9 Then n\Frame = 677
							Else
								AnimateNPC(n,677,736,0.3,False)
								If n\Frame > 735.9 Then n\State = 2 : n\Frame = 737
							EndIf
							;If n\Frame>1085 Then
							;	AnimateNPC(n, 1085, 1412, 0.3,False)
							;	If n\Frame> 1411.9 Then n\Frame = 307
							;Else
							;	AnimateNPC(n, 307, 424, 0.3, False)
							;	If n\Frame > 423.9 Then n\State = 2 : n\Frame = 892
							;EndIf
						ElseIf n\State=2
							AnimateNPC(n,737,822,0.3,False)
							If n\Frame=>822 Then n\State=3 : n\State2=0
							;AnimateNPC(n, 833, 972, 0.3, False)
							;If n\Frame=>972 Then n\State = 3 : n\State2=0
						ElseIf n\State=3
							n\State2 = n\State2+FPSfactor
							If n\State2 > 70*18 Then
								AnimateNPC(n,823,847,n\Speed*8,False)
								;AnimateNPC(n, 973, 1001, 0.5, False)
								If n\Frame>846.9 Then ;1000.9 
									n\State = 4
									StopStream_Strict(n\SoundChn) : n\SoundChn=0
								EndIf
							Else
								AnimateNPC(n,737,822,0.3)
								;AnimateNPC(n, 892,978, 0.3)
							EndIf
						EndIf
						;[End Block]
					Case 5
						;[Block]
						If dist < 16.0 Then 
							
							If dist < 4.0 Then
								GiveAchievement(Achv096)
							EndIf
							
							;If n\Sound = 0 Then
							;	n\Sound = LoadSound_Strict("SFX\Music\096.ogg")
							;Else
							;	n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 14.0, 1.0)
							;EndIf
							If n\SoundChn = 0
								n\SoundChn = StreamSound_Strict("SFX\Music\096.ogg",0)
								n\SoundChn_IsStream = True
							Else
								UpdateStreamSoundOrigin(n\SoundChn,Camera,n\Collider,14.0,1.0)
							EndIf
							
							If n\Frame>=422
								n\State2=n\State2+FPSfactor
								If n\State2>1000 Then ;walking around
									If n\State2>1600 Then n\State2=Rand(0,500) ;: n\Frame = 1457 ;1652
									
									;1652
									If n\Frame<1382 Then ;idle to walk
										n\CurrSpeed = CurveValue(n\Speed*0.1,n\CurrSpeed,5.0)
										AnimateNPC(n,1369,1382,n\CurrSpeed*45,False)
										;AnimateNPC(n, 1638,1652, n\CurrSpeed*45,False)
									Else
										n\CurrSpeed = CurveValue(n\Speed*0.1,n\CurrSpeed,5.0)
										AnimateNPC(n,1383,1456,n\CurrSpeed*45)
										;AnimateNPC(n, 1653,1724, n\CurrSpeed*45) ;walk
									EndIf
									
									If MilliSecs2() > n\State3 Then
										n\LastSeen=0
										If EntityVisible(Collider, n\Collider) Then 
											n\LastSeen=1
										Else
											HideEntity n\Collider
											EntityPick(n\Collider, 1.5)
											If PickedEntity() <> 0 Then
												n\Angle = EntityYaw(n\Collider)+Rnd(80,110)
											EndIf
											ShowEntity n\Collider
										EndIf
										n\State3=MilliSecs2()+3000
									EndIf
									
									If n\LastSeen Then 
										PointEntity n\obj, Collider
										RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),130.0),0
										If dist < 1.5 Then n\State2=0
									Else
										RotateEntity n\Collider, 0, CurveAngle(n\Angle,EntityYaw(n\Collider),50.0),0
									EndIf
								Else
									;1638
									If n\Frame>472 Then ;walk to idle
										n\CurrSpeed = CurveValue(n\Speed*0.05,n\CurrSpeed,8.0)
										AnimateNPC(n,1383,1469,n\CurrSpeed*45,False)
										If n\Frame=>1468.9 Then n\Frame=423
										;AnimateNPC(n, 1652, 1638, -n\CurrSpeed*45,False)
									Else ;idle
										n\CurrSpeed = CurveValue(0,n\CurrSpeed,4.0)	
										AnimateNPC(n,423,471,0.2)
										;AnimateNPC(n, 585, 633, 0.2) ;idle
									EndIf
								EndIf
								
								MoveEntity n\Collider,0,0,n\CurrSpeed*FPSfactor
							Else
								AnimateNPC(n,312,422,0.3,False)
							EndIf
							
							angle = WrapAngle(DeltaYaw(n\Collider, Camera));-EntityYaw(n\Collider))
							If (Not NoTarget)
								If angle<55 Or angle>360-55 Then
									CameraProject Camera,EntityX(n\Collider), EntityY(Collider)+5.8*0.2-0.25, EntityZ(n\Collider)
									
									If ProjectedX()>0 And ProjectedX()<GraphicWidth Then
										If ProjectedY()>0 And ProjectedY()<GraphicHeight Then
											If EntityVisible(Collider, n\Collider) Then
												If (BlinkTimer < - 16 Or BlinkTimer > - 6)
													PlaySound_Strict LoadTempSound("SFX\SCP\096\Triggered.ogg")
													
													CurrCameraZoom = 10
													
													If n\Frame >= 422
														n\Frame = 677 ;833
													EndIf
													StopStream_Strict(n\SoundChn) : n\SoundChn=0
													n\Sound = 0
													n\State = 2
												EndIf
											EndIf									
										EndIf
									EndIf
									
								EndIf
							EndIf
						EndIf
						;[End Block]
				End Select
				
				;ResetEntity(n\Collider)
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider)-0.03, EntityZ(n\Collider)) ;-0.07
				
				RotateEntity n\obj, EntityPitch(n\Collider), EntityYaw(n\Collider), 0
				;[End Block]
			Case NPCtype049
				;[Block]
				;n\state = the "main state" of the NPC
				;n\state2 = attacks the player when the value is above 0.0
				;n\state3 = timer for updating the path again
				
				prevFrame# = n\Frame
				
				dist  = EntityDistance(Collider, n\Collider)
				
				n\BlinkTimer# = 1.0
				
				If n\Idle > 0.1
					If PlayerRoom\RoomTemplate\Name$ <> "room049"
						n\Idle = Max(n\Idle-(1+SelectedDifficulty\aggressiveNPCs)*FPSfactor,0.1)
					EndIf
					n\DropSpeed = 0
					If ChannelPlaying(n\SoundChn) Then StopChannel(n\SoundChn)
					If ChannelPlaying(n\SoundChn2) Then StopChannel(n\SoundChn2)
					PositionEntity n\Collider,0,-500,0
					PositionEntity n\obj,0,-500,0
				Else
					If n\Idle = 0.1 Then
						If PlayerInReachableRoom() Then
							For i = 0 To 3
								If PlayerRoom\Adjacent[i]<>Null Then
									For j = 0 To 3
										If PlayerRoom\Adjacent[i]\Adjacent[j]<>Null Then
											TeleportEntity(n\Collider,PlayerRoom\Adjacent[i]\Adjacent[j]\x,0.5,PlayerRoom\Adjacent[i]\Adjacent[j]\z,n\CollRadius,True)
											Exit
										EndIf
									Next
									Exit
								EndIf
							Next
							n\Idle = 0.0
							DebugLog "SCP-049 not idle"
						EndIf
					EndIf
					
					Select n\State
						Case 0 ;nothing (used for events)
						Case 1 ;looking around before getting active
							;[Block]
							If n\Frame=>538 Then
								AnimateNPC(n, 659, 538, -0.45, False)
								If n\Frame > 537.9 Then n\Frame = 37
								
								;Animate2(n\obj, AnimTime(n\obj), 659, 538, -0.45, False)
								;If AnimTime(n\obj)=538 Then SetAnimTime(n\obj, 37)
							Else
								AnimateNPC(n, 37, 269, 0.7, False)
								If n\Frame>268.9 Then n\State = 2
								
								;Animate2(n\obj, AnimTime(n\obj), 37, 269, 0.7, False)
								;If AnimTime(n\obj)=269 Then n\State = 2
							EndIf
							;[End Block]
						Case 2 ;being active
							;[Block]
							If (dist < HideDistance*2) And (Not n\Idle) And PlayerInReachableRoom(True) Then
								n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider)
								PlayerSeeAble% = MeNPCSeesPlayer(n)
								If PlayerSeeAble%=True Or n\State2>0 Then ;Player is visible for 049's sight - attacking
									GiveAchievement(Achv049)
									
									;Playing a sound after detecting the player
									If n\PrevState <= 1 And ChannelPlaying(n\SoundChn2)=False
										If n\Sound2 <> 0 Then FreeSound_Strict(n\Sound2)
										n\Sound2 = LoadSound_Strict("SFX\SCP\049\Spotted"+Rand(1,7)+".ogg")
										n\SoundChn2 = LoopSound2(n\Sound2,n\SoundChn2,Camera,n\obj)
										n\PrevState = 2
									EndIf
									n\PathStatus = 0
									n\PathTimer# = 0.0
									n\PathLocation = 0
									If PlayerSeeAble%=True Then n\State2 = 70*2
									
									PointEntity n\obj,Collider
									RotateEntity n\Collider,0,CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),10.0),0
									
									If dist < 0.5 Then
										If WearingHazmat>0 Then
											BlurTimer = BlurTimer+FPSfactor*2.5
											If BlurTimer>250 And BlurTimer-FPSfactor*2.5 <= 250 And n\PrevState<>3 Then
												If n\SoundChn2 <> 0 Then StopChannel(n\SoundChn2)
												n\SoundChn2 = PlaySound_Strict(LoadTempSound("SFX\SCP\049\TakeOffHazmat.ogg"))
												n\PrevState=3
											ElseIf BlurTimer => 500
												For i = 0 To MaxItemAmount-1
													If Inventory(i)<>Null Then
														If Instr(Inventory(i)\itemtemplate\tempname,"hazmatsuit") And WearingHazmat<3 Then
															If Inventory(i)\state2 < 3 Then
																Inventory(i)\state2 = Inventory(i)\state2 + 1
																BlurTimer = 260.0
																CameraShake = 2.0
															Else
																RemoveItem(Inventory(i))
																WearingHazmat = False
															EndIf
															Exit
														EndIf
													EndIf
												Next
											EndIf
										ElseIf Wearing714 Then
											BlurTimer = BlurTimer+FPSfactor*2.5
											If BlurTimer>250 And BlurTimer-FPSfactor*2.5 <= 250 And n\PrevState<>3 Then
												If n\SoundChn2 <> 0 Then StopChannel(n\SoundChn2)
												n\SoundChn2 = PlaySound_Strict(LoadTempSound("SFX\SCP\049\714Equipped.ogg"))
												n\PrevState=3
											ElseIf BlurTimer => 500
												Wearing714=False
											EndIf
										Else
											CurrCameraZoom = 20.0
											BlurTimer = 500.0
											
											If (Not GodMode) Then
												If PlayerRoom\RoomTemplate\Name$ = "room049"
													DeathMSG = "Three (3) active instances of SCP-049-2 discovered in the tunnel outside SCP-049's containment chamber. Terminated by Nine-Tailed Fox."
													For e.events = Each Events
														If e\EventName = "room049" Then e\EventState=-1 : Exit
													Next
												Else
													DeathMSG = "An active instance of SCP-049-2 was discovered in [REDACTED]. Terminated by Nine-Tailed Fox."
													Kill()
												EndIf
												PlaySound_Strict HorrorSFX(13)
												If n\Sound2 <> 0 Then FreeSound_Strict(n\Sound2)
												n\Sound2 = LoadSound_Strict("SFX\SCP\049\Kidnap"+Rand(1,2)+".ogg")
												n\SoundChn2 = LoopSound2(n\Sound2,n\SoundChn2,Camera,n\obj)
												n\State = 3
											EndIf										
										EndIf
									Else
										n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
										MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor	
										
										If n\PrevState = 3 Then n\PrevState = 2
										
										If dist < 3.0 Then
											AnimateNPC(n, Max(Min(AnimTime(n\obj),428.0),387), 463.0, n\CurrSpeed*38)
										Else
											If n\Frame>428.0 Then
												AnimateNPC(n, Min(AnimTime(n\obj),463.0), 498.0, n\CurrSpeed*38,False)
												If n\Frame>497.9 Then n\Frame = 358
											Else
												AnimateNPC(n, Max(Min(AnimTime(n\obj),358.0),346), 393.0, n\CurrSpeed*38)
											EndIf
										EndIf
									EndIf
								Else ;Finding a path to the player
									If n\PathStatus = 1 ;Path to player found
										While n\Path[n\PathLocation]=Null
											If n\PathLocation > 19
												n\PathLocation = 0 : n\PathStatus = 0 : Exit
											Else
												n\PathLocation = n\PathLocation + 1
											EndIf
										Wend
										If n\Path[n\PathLocation]<>Null Then
											;closes doors behind him
											If n\PathLocation>0 Then
												If n\Path[n\PathLocation-1] <> Null Then
													If n\Path[n\PathLocation-1]\door <> Null Then
														If (Not n\Path[n\PathLocation-1]\door\IsElevatorDoor) Then
															If EntityDistance(n\Path[n\PathLocation-1]\obj,n\Collider)>0.3 Then
																If (n\Path[n\PathLocation-1]\door\MTFClose) And (n\Path[n\PathLocation-1]\door\open) And (n\Path[n\PathLocation-1]\door\buttons[0]<>0 Or n\Path[n\PathLocation-1]\door\buttons[1]<>0) Then
																	UseDoor(n\Path[n\PathLocation-1]\door, False)
																EndIf
															EndIf
														EndIf
													EndIf
												EndIf
											EndIf
											
											n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
											PointEntity n\obj,n\Path[n\PathLocation]\obj
											RotateEntity n\Collider,0,CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),10.0),0
											MoveEntity n\Collider,0,0,n\CurrSpeed*FPSfactor
											
											;opens doors in front of him
											dist2# = EntityDistance(n\Collider,n\Path[n\PathLocation]\obj)
											If dist2 < 0.6 Then
												temp = True
												If n\Path[n\PathLocation]\door <> Null Then
													If (Not n\Path[n\PathLocation]\door\IsElevatorDoor)
														If (n\Path[n\PathLocation]\door\locked Or n\Path[n\PathLocation]\door\KeyCard<>0 Or n\Path[n\PathLocation]\door\Code<>"") And (Not n\Path[n\PathLocation]\door\open) Then
															temp = False
														Else
															If n\Path[n\PathLocation]\door\open = False And (n\Path[n\PathLocation]\door\buttons[0]<>0 Or n\Path[n\PathLocation]\door\buttons[1]<>0) Then
																UseDoor(n\Path[n\PathLocation]\door, False)
															EndIf
														EndIf
													EndIf
												EndIf
												If dist2#<0.2 And temp
													n\PathLocation = n\PathLocation + 1
												ElseIf dist2#<0.5 And (Not temp)
													;Breaking up the path when the door in front of 049 cannot be operated by himself
													n\PathStatus = 0
													n\PathTimer# = 0.0
												EndIf
											EndIf
											
											AnimateNPC(n, Max(Min(AnimTime(n\obj),358.0),346), 393.0, n\CurrSpeed*38)
											
											;Playing a sound if he hears the player
											If n\PrevState = 0 And ChannelPlaying(n\SoundChn2)=False
												If n\Sound2 <> 0 Then FreeSound_Strict(n\Sound2)
												If Rand(30)=1
													n\Sound2 = LoadSound_Strict("SFX\SCP\049\Searching7.ogg")
												Else
													n\Sound2 = LoadSound_Strict("SFX\SCP\049\Searching"+Rand(1,6)+".ogg")
												EndIf
												n\SoundChn2 = LoopSound2(n\Sound2,n\SoundChn2,Camera,n\obj)
												n\PrevState = 1
											EndIf
											
											;Resetting the "PrevState" value randomly, to make 049 talking randomly 
											If Rand(600)=1 And ChannelPlaying(n\SoundChn2)=False Then n\PrevState = 0
											
											If n\PrevState > 1 Then n\PrevState = 1
										EndIf
									Else ;No Path to the player found - stands still and tries to find a path
										;[Block]
										n\PathTimer# = n\PathTimer# + FPSfactor
										If n\PathTimer# > 70*(5-(2*SelectedDifficulty\aggressiveNPCs)) Then
											n\PathStatus = FindPath(n, EntityX(Collider),EntityY(Collider),EntityZ(Collider))
											n\PathTimer# = 0.0
											n\State3 = 0
											
											;Attempt to find a room (the Playerroom or one of it's adjacent rooms) for 049 to go to but select the one closest to him
											If n\PathStatus <> 1 Then
												Local closestdist# = EntityDistance(PlayerRoom\obj,n\Collider)
												Local closestRoom.Rooms = PlayerRoom
												Local currdist# = 0.0
												For i = 0 To 3
													If PlayerRoom\Adjacent[i]<>Null Then
														currdist = EntityDistance(PlayerRoom\Adjacent[i]\obj,n\Collider)
														If currdist < closestdist Then
															closestdist = currdist
															closestRoom = PlayerRoom\Adjacent[i]
														EndIf
													EndIf
												Next
												n\PathStatus = FindPath(n,EntityX(closestRoom\obj),0.5,EntityZ(closestRoom\obj))
												DebugLog "Find path for 049 in another room (pathstatus: "+n\PathStatus+")"
											EndIf
											
											;Making 3 attempts at finding a path
											While Int(n\State3) < 3
												;Breaking up the path if no "real" path has been found (only 1 waypoint and it is too close)
												If n\PathStatus = 1 Then
													If n\Path[1]<>Null Then
														If n\Path[2]=Null And EntityDistance(n\Path[1]\obj,n\Collider)<0.4 Then
															n\PathLocation = 0
															n\PathStatus = 0
															DebugLog "Breaking up path for 049 because no waypoint number 2 has been found and waypoint number 1 is too close."
														EndIf
													EndIf
													If n\Path[0]<>Null And n\Path[1]=Null Then
														n\PathLocation = 0
														n\PathStatus = 0
														DebugLog "Breaking up path for 049 because no waypoint number 1 has been found."
													EndIf
												EndIf
												
												;No path could still be found, just make 049 go to a room (further away than the very first attempt)
												If n\PathStatus <> 1 Then
													closestdist# = 100.0 ;Prevent the PlayerRoom to be considered the closest, so 049 wouldn't try to find a path there
													closestRoom.Rooms = PlayerRoom
													currdist# = 0.0
													For i = 0 To 3
														If PlayerRoom\Adjacent[i]<>Null Then
															currdist = EntityDistance(PlayerRoom\Adjacent[i]\obj,n\Collider)
															If currdist < closestdist Then
																closestdist = currdist
																For j = 0 To 3
																	If PlayerRoom\Adjacent[i]\Adjacent[j]<>Null Then
																		If PlayerRoom\Adjacent[i]\Adjacent[j]<>PlayerRoom Then
																			closestRoom = PlayerRoom\Adjacent[i]\Adjacent[j]
																			Exit
																		EndIf
																	EndIf
																Next
															EndIf
														EndIf
													Next
													n\PathStatus = FindPath(n,EntityX(closestRoom\obj),0.5,EntityZ(closestRoom\obj))
													DebugLog "Find path for 049 in another further away room (pathstatus: "+n\PathStatus+")"
												EndIf
												
												;Making 049 skip waypoints for doors he can't interact with, but only if the actual path is behind him
												If n\PathStatus = 1 Then
													If n\Path[1]<>Null Then
														If n\Path[1]\door<>Null Then
															If (n\Path[1]\door\locked Or n\Path[1]\door\KeyCard<>0 Or n\Path[1]\door\Code<>"") And (Not n\Path[1]\door\open) Then
																Repeat
																	If n\PathLocation > 19
																		n\PathLocation = 0 : n\PathStatus = 0 : Exit
																	Else
																		n\PathLocation = n\PathLocation + 1
																	EndIf
																	If n\Path[n\PathLocation]<>Null Then
																		If Abs(DeltaYaw(n\Collider,n\Path[n\PathLocation]\obj))>(45.0-Abs(DeltaYaw(n\Collider,n\Path[1]\obj))) Then
																			DebugLog "Skip until waypoint number "+n\PathLocation
																			n\State3 = 3
																			Exit
																		EndIf
																	EndIf
																Forever
															Else
																n\State3 = 3
															EndIf
														Else
															n\State3 = 3
														EndIf
													EndIf
												EndIf
												n\State3 = n\State3 + 1
											Wend
										EndIf
										AnimateNPC(n, 269, 345, 0.2)
										;[End Block]
									EndIf
								EndIf
								
								If n\CurrSpeed > 0.005 Then
									If (prevFrame < 361 And n\Frame=>361) Or (prevFrame < 377 And n\Frame=>377) Then
										PlaySound2(StepSFX(3,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.8,1.0))						
									ElseIf (prevFrame < 431 And n\Frame=>431) Or (prevFrame < 447 And n\Frame=>447) Then
										PlaySound2(StepSFX(3,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.8,1.0))
									EndIf
								EndIf
								
								If ChannelPlaying(n\SoundChn2)
									UpdateSoundOrigin(n\SoundChn2,Camera,n\obj)
								EndIf
							ElseIf (Not n\Idle)
								If ChannelPlaying(n\SoundChn) Then
									StopChannel(n\SoundChn)
								EndIf
								If PlayerInReachableRoom(True) And InFacility=1 Then ;Player is in a room where SCP-049 can teleport to
									If Rand(1,3-SelectedDifficulty\otherFactors)=1 Then
										TeleportCloser(n)
										DebugLog "SCP-049 teleported closer due to distance"
									Else
										n\Idle = 60*70
										DebugLog "SCP-049 is now idle"
									EndIf
								EndIf
							EndIf
							;[End Block]
						Case 3 ;The player was killed by SCP-049
							;[Block]
							AnimateNPC(n, 537, 660, 0.7, False)
							
							;Animate2(n\obj, AnimTime(n\obj), 537, 660, 0.7, False)
							PositionEntity n\Collider, CurveValue(EntityX(Collider),EntityX(n\Collider),20.0),EntityY(n\Collider),CurveValue(EntityZ(Collider),EntityZ(n\Collider),20.0)
							RotateEntity n\Collider, 0, CurveAngle(EntityYaw(Collider)-180.0,EntityYaw(n\Collider),40), 0
							;[End Block]
						Case 4 ;Standing on catwalk in room4
							;[Block]
							If dist < 8.0 Then
								AnimateNPC(n, 18, 19, 0.05)
								
								;Animate2(n\obj, AnimTime(n\obj), 18, 19, 0.05)
								PointEntity n\obj, Collider	
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 45.0), 0
								
								n\State3 = 1
							ElseIf dist > HideDistance*0.8 And n\State3 > 0 Then
								n\State = 2
								n\State3 = 0
								For r.Rooms = Each Rooms
									If EntityDistance(r\obj,n\Collider)<4.0 Then
										TeleportEntity(n\Collider,EntityX(r\obj),0.1,EntityZ(r\obj),n\CollRadius,True)
										Exit
									EndIf
								Next
							EndIf
							;[End Block]
						Case 5 ;used for "room2sl"
							;[Block]
							n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider)
							PlayerSeeAble% = MeNPCSeesPlayer(n,True)
							If PlayerSeeAble% = True
								n\State = 2
								n\PathStatus = 0
								n\PathLocation = 0
								n\PathTimer = 0
								n\State3 = 0
								n\State2 = 70*2
								n\PrevState = 0
								PlaySound_Strict LoadTempSound("SFX\Room\Room2SL049Spawn.ogg")
							ElseIf PlayerSeeAble% = 2 And n\State3 > 0.0
								n\PathStatus = FindPath(n,EntityX(Collider),EntityY(Collider),EntityZ(Collider))
							Else
								If n\State3 = 6.0
									If EntityDistance(n\Collider,Collider)>HideDistance
										n\State = 2
										n\PathStatus = 0
										n\PathLocation = 0
										n\PathTimer = 0
										n\State3 = 0
										n\PrevState = 0
									Else
										If n\PathStatus <> 1 Then n\PathStatus = FindPath(n,EntityX(Collider),EntityY(Collider),EntityZ(Collider))
									EndIf
								EndIf
								
								If n\PathStatus = 1
									If n\Path[n\PathLocation]=Null
										If n\PathLocation > 19 Then
											n\PathLocation = 0 : n\PathStatus = 0
										Else
											n\PathLocation = n\PathLocation + 1
										EndIf
									Else
										n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
										PointEntity n\obj,n\Path[n\PathLocation]\obj
										RotateEntity n\Collider,0,CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),10.0),0
										MoveEntity n\Collider,0,0,n\CurrSpeed*FPSfactor
										
										;closes doors behind him
										If n\PathLocation>0 Then
											If n\Path[n\PathLocation-1] <> Null
												If n\Path[n\PathLocation-1]\door <> Null Then
													If n\Path[n\PathLocation-1]\door\KeyCard=0
														If EntityDistance(n\Path[n\PathLocation-1]\obj,n\Collider)>0.3
															If n\Path[n\PathLocation-1]\door\open Then UseDoor(n\Path[n\PathLocation-1]\door, False)
														EndIf
													EndIf
												EndIf
											EndIf
										EndIf
										
										;opens doors in front of him
										dist2# = EntityDistance(n\Collider,n\Path[n\PathLocation]\obj)
										If dist2 < 0.6 Then
											If n\Path[n\PathLocation]\door <> Null Then
												If n\Path[n\PathLocation]\door\open = False Then UseDoor(n\Path[n\PathLocation]\door, False)
											EndIf
										EndIf
										
										If dist2#<0.2
											n\PathLocation = n\PathLocation + 1
										EndIf
										
										AnimateNPC(n, Max(Min(AnimTime(n\obj),358.0),346), 393.0, n\CurrSpeed*38)
									EndIf
								Else
									Select n\PrevState
										Case 0
											AnimateNPC(n, 269, 345, 0.2)
										Case 1
											AnimateNPC(n, 661, 891, 0.4, False)
										Case 2
											AnimateNPC(n, 892, 1119, 0.4, False)
									End Select
								EndIf
							EndIf
							
							If PlayerRoom\RoomTemplate\Name = "room2sl" Then
								ShouldPlay = 20
							EndIf
							
							If n\CurrSpeed > 0.005 Then
								If (prevFrame < 361 And n\Frame=>361) Or (prevFrame < 377 And n\Frame=>377) Then
									PlaySound2(StepSFX(3,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.8,1.0))						
								ElseIf (prevFrame < 431 And n\Frame=>431) Or (prevFrame < 447 And n\Frame=>447)
									PlaySound2(StepSFX(3,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.8,1.0))
								EndIf
							EndIf
							
							If ChannelPlaying(n\SoundChn2)
								UpdateSoundOrigin(n\SoundChn2,Camera,n\obj)
							EndIf
							;[End Block]
					End Select
				EndIf
				
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider)-0.22, EntityZ(n\Collider))
				
				RotateEntity n\obj, 0, EntityYaw(n\Collider), 0
				
				n\LastSeen = Max(n\LastSeen-FPSfactor,0)
				
				n\State2 = Max(n\State2-FPSfactor,0)
				
				;[End Block]
			Case NPCtypeZombie
				;[Block]
				
				If Abs(EntityY(Collider)-EntityY(n\Collider))<4.0 Then
					
					prevFrame# = n\Frame
					
					If (Not n\IsDead)
						Select n\State
							Case 0
								;[Block]
								AnimateNPC(n, 719, 777, 0.2, False)
								
								If n\Frame=777 Then
									If Rand(700)=1 Then
										If EntityDistance(Collider, n\Collider)<5.0 Then
											n\Frame = 719
										EndIf
									EndIf
								EndIf
								;[End Block]
							Case 1 ;stands up
								;[Block]
								If n\Frame=>682 Then 
									AnimateNPC(n, 926, 935, 0.3, False)
									If n\Frame = 935 Then n\State = 2
									
									;Animate2(n\obj, AnimTime(n\obj), 926, 935, 0.3, False)
									;If AnimTime(n\obj)=935 Then n\State = 2
								Else
									AnimateNPC(n, 155, 682, 1.5, False)
									;Animate2(n\obj, AnimTime(n\obj), 155, 682, 1.5, False)
								EndIf
								;[End Block]
							Case 2 ;following the player
								;[Block]
								If n\State3 < 0 Then ;check if the player is visible every three seconds
									If EntityDistance(Collider, n\Collider)<5.0 Then 
										If EntityVisible(Collider, n\Collider) Then n\State2 = 70*5
									EndIf
									n\State3=70*3
								Else
									n\State3=n\State3-FPSfactor
								EndIf
								
								If n\State2 > 0 And (Not NoTarget) Then ;player is visible -> attack
									n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 6.0, 0.6)
									
									n\PathStatus = 0
									
									dist = EntityDistance(Collider, n\Collider)
									
									PointEntity n\obj, Collider
									RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 30.0), 0
									
									If dist < 0.7 Then 
										n\State = 3
										If Rand(2)=1 Then
											n\Frame = 2
											;SetAnimTime n\obj, 2
										Else
											n\Frame = 66
											;SetAnimTime n\obj, 66
										EndIf
									Else
										n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
										MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
										
										AnimateNPC(n, 936, 1017, n\CurrSpeed*60)
										
										;Animate2(n\obj, AnimTime(n\obj), 936, 1017, n\CurrSpeed*60)
										
									EndIf
									
									n\State2=n\State2-FPSfactor
								Else
									If n\PathStatus = 1 Then ;path found
										If n\Path[n\PathLocation]=Null Then 
											If n\PathLocation > 19 Then 
												n\PathLocation = 0 : n\PathStatus = 0
											Else
												n\PathLocation = n\PathLocation + 1
											EndIf
										Else
											PointEntity n\obj, n\Path[n\PathLocation]\obj
											
											RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 30.0), 0
											n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
											MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
											
											AnimateNPC(n, 936, 1017, n\CurrSpeed*60)
											;Animate2(n\obj, AnimTime(n\obj), 936, 1017, n\CurrSpeed*60)
											
											If EntityDistance(n\Collider,n\Path[n\PathLocation]\obj) < 0.2 Then
												n\PathLocation = n\PathLocation + 1
											EndIf 
										EndIf
									Else ;no path to the player, stands still
										n\CurrSpeed = 0
										AnimateNPC(n, 778, 926, 0.1)
										;Animate2(n\obj, AnimTime(n\obj), 778, 926, 0.1)
										
										n\PathTimer = n\PathTimer-FPSfactor
										If n\PathTimer =< 0 Then
											n\PathStatus = FindPath(n, EntityX(Collider),EntityY(Collider)+0.1,EntityZ(Collider))
											n\PathTimer = n\PathTimer+70*5
										EndIf
									EndIf
								EndIf
								
								;65, 80, 93, 109, 123
								If n\CurrSpeed > 0.005 Then
									If (prevFrame < 977 And n\Frame=>977) Or (prevFrame > 1010 And n\Frame<940) Then
										;PlaySound2(StepSFX(0,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))
										PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))
									EndIf
								EndIf
								;[End Block]
							Case 3
								;[Block]
								If NoTarget Then n\State = 2
								If n\Frame < 66 Then
									AnimateNPC(n, 2, 65, 0.7, False)
									
									;Animate2(n\obj, AnimTime(n\obj), 2, 65, 0.7, False)
									If prevFrame < 23 And n\Frame=>23 Then
										If EntityDistance(n\Collider,Collider)<1.1
											If (Abs(DeltaYaw(n\Collider,Collider))<=60.0)
												PlaySound_Strict DamageSFX(Rand(5,8))
												Injuries = Injuries+Rnd(0.4,1.0)
												DeathMSG = "Subject D-9341. Cause of death: multiple lacerations and severe blunt force trauma caused by an instance of SCP-049-2."
											EndIf
										EndIf
									ElseIf n\Frame=65 Then
										n\State = 2
									EndIf							
								Else
									AnimateNPC(n, 66, 132, 0.7, False)
									;Animate2(n\obj, AnimTime(n\obj), 66, 132, 0.7, False)
									If prevFrame < 90 And n\Frame=>90 Then
										If EntityDistance(n\Collider,Collider)<1.1
											If (Abs(DeltaYaw(n\Collider,Collider))<=60.0)
												PlaySound_Strict DamageSFX(Rand(5,8))
												Injuries = Injuries+Rnd(0.4,1.0)
												DeathMSG = "Subject D-9341. Cause of death: multiple lacerations and severe blunt force trauma caused by an instance of SCP-049-2."
											EndIf
										EndIf
									ElseIf n\Frame=132 Then
										n\State = 2
									EndIf		
								EndIf
								;[End Block]
						End Select
					Else
						AnimateNPC(n, 133, 157, 0.5, False)
					EndIf
					
					PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.2, EntityZ(n\Collider))
					
					RotateEntity n\obj, -90, EntityYaw(n\Collider), 0
				EndIf
				
				;[End Block]
			Case NPCtypeGuard ;------------------------------------------------------------------------------------------------------------------
				;[Block]
				prevFrame# = n\Frame
				
				n\BoneToManipulate = ""
				;n\BoneToManipulate2 = ""
				n\ManipulateBone = False
				n\ManipulationType = 0
				n\NPCNameInSection = "Guard"
				
				Select n\State
					Case 1 ;aims and shoots at the player
						;[Block]
						If n\Frame < 39 Or (n\Frame > 76 And n\Frame < 245) Or (n\Frame > 248 And n\Frame < 302) Or n\Frame > 344
							AnimateNPC(n,345,357,0.2,False)
							If n\Frame >= 356 Then SetNPCFrame(n,302)
						EndIf
						;Animate2(n\obj, AnimTime(n\obj), 1539, 1553, 0.2, False)
						
						If KillTimer => 0 Then
							dist = EntityDistance(n\Collider,Collider)
							Local ShootAccuracy# = 0.4+0.5*SelectedDifficulty\aggressiveNPCs
							Local DetectDistance# = 11.0
							
							;If at Gate B increase his distance so that he can shoot the player from a distance after they are spotted.
							If PlayerRoom\RoomTemplate\Name = "exit1" Then
								DetectDistance = 21.0
								ShootAccuracy = 0.0
								If Rand(1,8-SelectedDifficulty\aggressiveNPCs*4)<2 Then ShootAccuracy = 0.03
								
								;increase accuracy if the player is going slow
								ShootAccuracy = ShootAccuracy + (0.5 - CurrSpeed*20)
							EndIf
							
							If dist < DetectDistance Then
								pvt% = CreatePivot()
								PositionEntity(pvt, EntityX(n\Collider), EntityY(n\Collider), EntityZ(n\Collider))
								PointEntity(pvt, Collider)
								RotateEntity(pvt, Min(EntityPitch(pvt), 20), EntityYaw(pvt), 0)
								
								RotateEntity(n\Collider, CurveAngle(EntityPitch(pvt), EntityPitch(n\Collider), 10), CurveAngle(EntityYaw(pvt), EntityYaw(n\Collider), 10), 0, True)
								
								PositionEntity(pvt, EntityX(n\Collider), EntityY(n\Collider)+0.8, EntityZ(n\Collider))
								PointEntity(pvt, Collider)
								RotateEntity(pvt, Min(EntityPitch(pvt), 40), EntityYaw(n\Collider), 0)
								
								If n\Reload = 0 ;And n\Frame>1550 Then
									DebugLog "entitypick"
									EntityPick(pvt, dist)
									If PickedEntity() = Collider Or n\State3=1 Then
										Local instaKillPlayer% = False
										
										If PlayerRoom\RoomTemplate\Name = "start" Then 
											DeathMSG = "Subject D-9341. Cause of death: Gunshot wound to the head. The surveillance tapes confirm that the subject was terminated by Agent Ulgrin shortly after the site lockdown was initiated."
											instaKillPlayer = True
										ElseIf PlayerRoom\RoomTemplate\Name = "exit1" Then
											DeathMSG = Chr(34)+"Agent G. to control. Eliminated a Class D escapee in Gate B's courtyard."+Chr(34)
										Else
											DeathMSG = ""
										EndIf
										
										PlaySound2(GunshotSFX, Camera, n\Collider, 35)
										
										RotateEntity(pvt, EntityPitch(n\Collider), EntityYaw(n\Collider), 0, True)
										PositionEntity(pvt, EntityX(n\obj), EntityY(n\obj), EntityZ(n\obj))
										MoveEntity (pvt,0.8*0.079, 10.75*0.079, 6.9*0.079)
										
										PointEntity pvt, Collider
										Shoot(EntityX(pvt), EntityY(pvt), EntityZ(pvt), ShootAccuracy, False, instaKillPlayer)
										n\Reload = 7
									Else
										n\CurrSpeed = n\Speed
									End If
								EndIf
								
								If n\Reload > 0 And n\Reload <= 7
									AnimateNPC(n,245,248,0.35,True)
								Else
									If n\Frame < 302
										AnimateNPC(n,302,344,0.35,True)
									EndIf
								EndIf
								
								FreeEntity(pvt)
							Else
								AnimateNPC(n,302,344,0.35,True)
							EndIf
							
							n\ManipulateBone = True
							
							If n\State2 = 10 Then ;Hacky way of applying spine pitch to specific guards.
								n\BoneToManipulate = "Chest"
								n\ManipulationType = 3
							Else
								n\BoneToManipulate = "Chest"
								n\ManipulationType = 0
							EndIf
						Else
							n\State = 0
						EndIf
						;[End Block]
					Case 2 ;shoots
						;[Block]
						AnimateNPC(n,245,248,0.35,True)
						;DebugLog "shoot"
						;Animate2(n\obj, AnimTime(n\obj), 1539, 1553, 0.35, False)
						If n\Reload = 0 ;And n\Frame > 1545 Then 
							PlaySound2(GunshotSFX, Camera, n\Collider, 20)
							p.Particles = CreateParticle(EntityX(n\obj, True), EntityY(n\obj, True), EntityZ(n\obj, True), 1, 0.2, 0.0, 5)
							PositionEntity(p\pvt, EntityX(n\obj), EntityY(n\obj), EntityZ(n\obj))
							RotateEntity(p\pvt, EntityPitch(n\Collider), EntityYaw(n\Collider), 0, True)
							MoveEntity (p\pvt,0.8*0.079, 10.75*0.079, 6.9*0.079)
							n\Reload = 7
						End If
						;[End Block]
					Case 3 ;follows a path
						;[Block]
						If n\PathStatus = 2 Then
							n\State = 0
							n\CurrSpeed = 0
						ElseIf n\PathStatus = 1
							If n\Path[n\PathLocation]=Null Then 
								If n\PathLocation > 19 Then 
									n\PathLocation = 0 : n\PathStatus = 0
								Else
									n\PathLocation = n\PathLocation + 1
								EndIf
							Else
								PointEntity n\obj, n\Path[n\PathLocation]\obj
								
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 20.0), 0
								
								AnimateNPC(n,1,38,n\CurrSpeed*40)
								;Animate2(n\obj, AnimTime(n\obj), 1614, 1641, n\CurrSpeed*30)
								n\CurrSpeed = CurveValue(n\Speed*0.7, n\CurrSpeed, 20.0)
								
								MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
								
								If EntityDistance(n\Collider,n\Path[n\PathLocation]\obj) < 0.2 Then
									n\PathLocation = n\PathLocation + 1
								EndIf 
							EndIf
						Else
							n\CurrSpeed = 0
							n\State = 4
						EndIf
						;[End Block]
					Case 4 ;standing and looking around randomly
						;[Block]
						AnimateNPC(n,77,201,0.2)
						;Animate2(n\obj, AnimTime(n\obj), 923, 1354, 0.2)
						
						If Rand(400) = 1 Then n\Angle = Rnd(-180, 180)
						
						RotateEntity(n\Collider, 0, CurveAngle(n\Angle + Sin(MilliSecs2() / 50) * 2, EntityYaw(n\Collider), 150.0), 0, True)
						
						dist# = EntityDistance(n\Collider, Collider)
						If dist < 15.0 Then
							
							If WrapAngle(EntityYaw(n\Collider)-DeltaYaw(n\Collider, Collider))<90 Then
								If EntityVisible(pvt, Collider) Then n\State = 1
							EndIf
							
						EndIf
						
						;[End Block]
					Case 5 ;following a target
						;[Block]
						
						RotateEntity n\Collider, 0, CurveAngle(VectorYaw(n\EnemyX-EntityX(n\Collider), 0, n\EnemyZ-EntityZ(n\Collider))+n\Angle, EntityYaw(n\Collider), 20.0), 0
						
						dist# = Distance(EntityX(n\Collider),EntityZ(n\Collider),n\EnemyX,n\EnemyZ)
						
						AnimateNPC(n,1,38,n\CurrSpeed*40)
						
						If dist > 2.0 Or dist < 1.0  Then
							n\CurrSpeed = CurveValue(n\Speed*Sgn(dist-1.5)*0.75, n\CurrSpeed, 10.0)
						Else
							n\CurrSpeed = CurveValue(0, n\CurrSpeed, 10.0)
						EndIf
						
						MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
						;[End Block]
					Case 7
						;[Block]
						AnimateNPC(n,77,201,0.2)
						;Animate2(n\obj, AnimTime(n\obj), 923, 1354, 0.2)
						;[End Block]
					Case 8
						
					Case 9
						;[Block]
						AnimateNPC(n,77,201,0.2)
						n\BoneToManipulate = "head"
						n\ManipulateBone = True
						n\ManipulationType = 0
						n\Angle = EntityYaw(n\Collider)
						;[End Block]
					Case 10
						;[Block]
						AnimateNPC(n, 1, 38, n\CurrSpeed*40)
						
						n\CurrSpeed = CurveValue(n\Speed*0.7, n\CurrSpeed, 20.0)
						
						MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
						;[End Block]
					Case 11
						;[Block]
						If n\Frame < 39 Or (n\Frame > 76 And n\Frame < 245) Or (n\Frame > 248 And n\Frame < 302) Or n\Frame > 344
							AnimateNPC(n,345,357,0.2,False)
							If n\Frame >= 356 Then SetNPCFrame(n,302)
						EndIf
						
						If KillTimer => 0 Then
							dist = EntityDistance(n\Collider,Collider)
							
							Local SearchPlayer% = False
							If dist < 11.0
								If EntityVisible(n\Collider,Collider)
									SearchPlayer = True
								EndIf
							EndIf
							
							If SearchPlayer
								pvt% = CreatePivot()
								PositionEntity(pvt, EntityX(n\Collider), EntityY(n\Collider), EntityZ(n\Collider))
								PointEntity(pvt, Collider)
								RotateEntity(pvt, Min(EntityPitch(pvt), 20), EntityYaw(pvt), 0)
								
								RotateEntity(n\Collider, CurveAngle(EntityPitch(pvt), EntityPitch(n\Collider), 10), CurveAngle(EntityYaw(pvt), EntityYaw(n\Collider), 10), 0, True)
								
								PositionEntity(pvt, EntityX(n\Collider), EntityY(n\Collider)+0.8, EntityZ(n\Collider))
								PointEntity(pvt, Collider)
								RotateEntity(pvt, Min(EntityPitch(pvt), 40), EntityYaw(n\Collider), 0)
								
								If n\Reload = 0
									DebugLog "entitypick"
									EntityPick(pvt, dist)
									If PickedEntity() = Collider Or n\State3=1 Then
										instaKillPlayer% = False
										
										DeathMSG = ""
										
										PlaySound2(GunshotSFX, Camera, n\Collider, 35)
										
										RotateEntity(pvt, EntityPitch(n\Collider), EntityYaw(n\Collider), 0, True)
										PositionEntity(pvt, EntityX(n\obj), EntityY(n\obj), EntityZ(n\obj))
										MoveEntity (pvt,0.8*0.079, 10.75*0.079, 6.9*0.079)
										
										PointEntity pvt, Collider
										Shoot(EntityX(pvt), EntityY(pvt), EntityZ(pvt), 1.0, False, instaKillPlayer)
										n\Reload = 7
									Else
										n\CurrSpeed = n\Speed
									End If
								EndIf
								
								If n\Reload > 0 And n\Reload <= 7
									AnimateNPC(n,245,248,0.35,True)
								Else
									If n\Frame < 302
										AnimateNPC(n,302,344,0.35,True)
									EndIf
								EndIf
								
								FreeEntity(pvt)
							Else
								If n\PathStatus = 1
									If n\Path[n\PathLocation]=Null Then 
										If n\PathLocation > 19 Then 
											n\PathLocation = 0 : n\PathStatus = 0
										Else
											n\PathLocation = n\PathLocation + 1
										EndIf
									Else
										AnimateNPC(n,39,76,n\CurrSpeed*40)
										n\CurrSpeed = CurveValue(n\Speed*0.7, n\CurrSpeed, 20.0)
										MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
										
										PointEntity n\obj, n\Path[n\PathLocation]\obj
										
										RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 20.0), 0
										
										If EntityDistance(n\Collider,n\Path[n\PathLocation]\obj) < 0.2 Then
											n\PathLocation = n\PathLocation + 1
										EndIf
									EndIf
								Else
									If n\PathTimer = 0 Then n\PathStatus = FindPath(n,EntityX(Collider),EntityY(Collider)+0.5,EntityZ(Collider))
									
									Local wayPointCloseToPlayer.WayPoints
									wayPointCloseToPlayer = Null
									
									For wp.WayPoints = Each WayPoints
										If EntityDistance(wp\obj,Collider)<2.0
											wayPointCloseToPlayer = wp
											Exit
										EndIf
									Next
									
									If wayPointCloseToPlayer<>Null
										n\PathTimer = 1
										If EntityVisible(wayPointCloseToPlayer\obj,n\Collider)
											If Abs(DeltaYaw(n\Collider,wayPointCloseToPlayer\obj))>0
												PointEntity n\obj, wayPointCloseToPlayer\obj
												RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 20.0), 0
											EndIf
										EndIf
									Else
										n\PathTimer = 0
									EndIf
									
									If n\PathTimer = 1
										AnimateNPC(n,39,76,n\CurrSpeed*40)
										n\CurrSpeed = CurveValue(n\Speed*0.7, n\CurrSpeed, 20.0)
										MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
									EndIf
								EndIf
								
								If prevFrame < 43 And n\Frame=>43 Then
									PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.5,0.7))						
								ElseIf prevFrame < 61 And n\Frame=>61
									PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.5,0.7))
								EndIf
							EndIf
							
						Else
							n\State = 0
						EndIf
						;[End Block]
					Case 12
						;[Block]
						If n\Frame < 39 Or (n\Frame > 76 And n\Frame < 245) Or (n\Frame > 248 And n\Frame < 302) Or n\Frame > 344
							AnimateNPC(n,345,357,0.2,False)
							If n\Frame >= 356 Then SetNPCFrame(n,302)
						EndIf
						If n\Frame < 345
							AnimateNPC(n,302,344,0.35,True)
						EndIf
						
						pvt% = CreatePivot()
						PositionEntity(pvt, EntityX(n\Collider), EntityY(n\Collider), EntityZ(n\Collider))
						If n\State2 = 1.0
							PointEntity(pvt, Collider)
						Else
							RotateEntity pvt,0,n\Angle,0
						EndIf
						RotateEntity(pvt, Min(EntityPitch(pvt), 20), EntityYaw(pvt), 0)
						
						RotateEntity(n\Collider, CurveAngle(EntityPitch(pvt), EntityPitch(n\Collider), 10), CurveAngle(EntityYaw(pvt), EntityYaw(n\Collider), 10), 0, True)
						
						PositionEntity(pvt, EntityX(n\Collider), EntityY(n\Collider)+0.8, EntityZ(n\Collider))
						If n\State2 = 1.0
							PointEntity(pvt, Collider)
							n\ManipulateBone = True
							n\BoneToManipulate = "Chest"
							n\ManipulationType = 0
						Else
							RotateEntity pvt,0,n\Angle,0
						EndIf
						RotateEntity(pvt, Min(EntityPitch(pvt), 40), EntityYaw(n\Collider), 0)
						
						FreeEntity(pvt)
						
						UpdateSoundOrigin(n\SoundChn,Camera,n\Collider,20)
						;[End Block]
					Case 13
						;[Block]
						AnimateNPC(n,202,244,0.35,True)
						;[End Block]
					Case 14
						;[Block]
						If n\PathStatus = 2 Then
							n\State = 13
							n\CurrSpeed = 0
						ElseIf n\PathStatus = 1
							If n\Path[n\PathLocation]=Null Then 
								If n\PathLocation > 19 Then 
									n\PathLocation = 0 : n\PathStatus = 0
								Else
									n\PathLocation = n\PathLocation + 1
								EndIf
							Else
								PointEntity n\obj, n\Path[n\PathLocation]\obj
								
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 20.0), 0
								
								AnimateNPC(n,39,76,n\CurrSpeed*40)
								n\CurrSpeed = CurveValue(n\Speed*0.7, n\CurrSpeed, 20.0)
								
								MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
								
								If EntityDistance(n\Collider,n\Path[n\PathLocation]\obj) < 0.2 Then
									n\PathLocation = n\PathLocation + 1
								EndIf 
							EndIf
						Else
							n\CurrSpeed = 0
							n\State = 13
						EndIf
						
						If prevFrame < 43 And n\Frame=>43 Then
							PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.5,0.7))						
						ElseIf prevFrame < 61 And n\Frame=>61
							PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.5,0.7))
						EndIf
						;[End Block]
					Default
						;[Block]
						If Rand(400) = 1 Then n\PrevState = Rnd(-30, 30)
						n\PathStatus = 0
						AnimateNPC(n,77,201,0.2)
						
						RotateEntity(n\Collider, 0, CurveAngle(n\Angle + n\PrevState + Sin(MilliSecs2() / 50) * 2, EntityYaw(n\Collider), 50), 0, True)
						;[End Block]
				End Select
				
				If n\CurrSpeed > 0.01 Then
					If prevFrame < 5 And n\Frame>=5
						PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.5,0.7))						
					ElseIf prevFrame < 23 And n\Frame>=23
						PlaySound2(StepSFX(2,0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.5,0.7))						
					EndIf
				EndIf
				
				If n\Frame > 286.5 And n\Frame < 288.5
					n\IsDead = True
				EndIf
				
				n\Reload = Max(0, n\Reload - FPSfactor)
				;RotateEntity(n\Collider, 0, EntityYaw(n\Collider), 0, True)
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.2, EntityZ(n\Collider))
				
				;RotateEntity n\obj, EntityPitch(n\Collider)-90, EntityYaw(n\Collider), 0
				RotateEntity n\obj, 0, EntityYaw(n\Collider)+180, 0
				;[End Block]
			Case NPCtypeMTF ;------------------------------------------------------------------------------------------------------------------
				;[Block]
				UpdateMTFUnit(n)
				
				;[End Block]
			Case NPCtypeD,NPCtypeClerk 	;------------------------------------------------------------------------------------------------------------------
				;[Block]
				RotateEntity(n\Collider, 0, EntityYaw(n\Collider), EntityRoll(n\Collider), True)
				
				prevFrame = AnimTime(n\obj)
				
				Select n\State
					Case 0 ;idle
						n\CurrSpeed = CurveValue(0.0, n\CurrSpeed, 5.0)
						Animate2(n\obj, AnimTime(n\obj), 210, 235, 0.1)
					Case 1 ;walking
						If n\State2 = 1.0
							n\CurrSpeed = CurveValue(n\Speed*0.7, n\CurrSpeed, 20.0)
						Else
							n\CurrSpeed = CurveValue(0.015, n\CurrSpeed, 5.0)
						EndIf
						Animate2(n\obj, AnimTime(n\obj), 236, 260, n\CurrSpeed * 18)
					Case 2 ;running
						n\CurrSpeed = CurveValue(0.03, n\CurrSpeed, 5.0)
						Animate2(n\obj, AnimTime(n\obj), 301, 319, n\CurrSpeed * 18)
				End Select
				
				If n\State2 <> 2
					If n\State = 1
						If n\CurrSpeed > 0.01 Then
							If prevFrame < 244 And AnimTime(n\obj)=>244 Then
								PlaySound2(StepSFX(GetStepSound(n\Collider),0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))						
							ElseIf prevFrame < 256 And AnimTime(n\obj)=>256
								PlaySound2(StepSFX(GetStepSound(n\Collider),0,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))
							EndIf
						EndIf
					ElseIf n\State = 2
						If n\CurrSpeed > 0.01 Then
							If prevFrame < 309 And AnimTime(n\obj)=>309
								PlaySound2(StepSFX(GetStepSound(n\Collider),1,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))
							ElseIf prevFrame =< 319 And AnimTime(n\obj)=<301
								PlaySound2(StepSFX(GetStepSound(n\Collider),1,Rand(0,2)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))
							EndIf
						EndIf
					EndIf
				EndIf
				
				If n\Frame = 19 Or n\Frame = 60
					n\IsDead = True
				EndIf
				If AnimTime(n\obj)=19 Or AnimTime(n\obj)=60
					n\IsDead = True
				EndIf
				
				MoveEntity(n\Collider, 0, 0, n\CurrSpeed * FPSfactor)
				
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.32, EntityZ(n\Collider))
				
				RotateEntity n\obj, EntityPitch(n\Collider), EntityYaw(n\Collider)-180.0, 0
				;[End Block]
			Case NPCtype5131
				;[Block]
				;If KeyHit(48) Then n\Idle = True : n\State2 = 0
				
				If PlayerRoom\RoomTemplate\Name <> "pocketdimension" Then 
					If n\Idle Then
						HideEntity(n\obj)
						HideEntity(n\obj2)
						If Rand(200)=1 Then
							For w.WayPoints = Each WayPoints
								If w\room<>PlayerRoom Then
									x = Abs(EntityX(Collider)-EntityX(w\obj,True))
									If x>3 And x < 9 Then
										z = Abs(EntityZ(Collider)-EntityZ(w\obj,True))
										If z>3 And z < 9 Then
											PositionEntity(n\Collider, EntityX(w\obj,True), EntityY(w\obj,True), EntityZ(w\obj,True))
											PositionEntity(n\obj, EntityX(w\obj,True), EntityY(w\obj,True), EntityZ(w\obj,True))
											ResetEntity n\Collider
											ShowEntity(n\obj)
											ShowEntity(n\obj2)
											
											n\LastSeen = 0
											
											n\Path[0]=w
											
											n\Idle = False
											n\State2 = Rand(15,20)*70
											n\State = Max(Rand(-1,2),0)
											n\PrevState = Rand(0,1)
											Exit
										EndIf
									EndIf
								EndIf
							Next
						End If
					Else
						dist = EntityDistance(Collider, n\Collider)
						
						;use the prev-values to do a "twitching" effect
						n\PrevX = CurveValue(0.0, n\PrevX, 10.0)
						n\PrevZ = CurveValue(0.0, n\PrevZ, 10.0)
						
						If Rand(100)=1 Then
							If Rand(5)=1 Then
								n\PrevX = (EntityX(Collider)-EntityX(n\Collider))*0.9
								n\PrevZ = (EntityZ(Collider)-EntityZ(n\Collider))*0.9
							Else
								n\PrevX = Rnd(0.1,0.5)
								n\PrevZ = Rnd(0.1,0.5)						
							EndIf
						EndIf
						
						temp = Rnd(-1.0,1.0)
						PositionEntity n\obj2, EntityX(n\Collider)+n\PrevX*temp, EntityY(n\Collider) - 0.2 + Sin((MilliSecs2()/8-45) Mod 360)*0.05, EntityZ(n\Collider)+n\PrevZ*temp
						RotateEntity n\obj2, 0, EntityYaw(n\obj), 0
						If (Floor(AnimTime(n\obj2))<>Floor(n\Frame)) Then SetAnimTime n\obj2, n\Frame
						
						If n\State = 0 Then
							If n\PrevState=0
								AnimateNPC(n,2,74,0.2)
							Else
								AnimateNPC(n,75,124,0.2)
							EndIf
							;AnimateNPC(n, 229, 299, 0.2)
							
							If n\LastSeen Then 	
								PointEntity n\obj2, Collider
								RotateEntity n\obj, 0, CurveAngle(EntityYaw(n\obj2),EntityYaw(n\obj),40), 0
								If dist < 4 Then n\State = Rand(1,2)
							Else
								If dist < 6 And Rand(5)=1 Then
									If EntityInView(n\Collider,Camera) Then
										If EntityVisible(Collider, n\Collider) Then
											n\LastSeen = 1
											PlaySound_Strict LoadTempSound("SFX\SCP\513\Bell"+Rand(2,3)+".ogg")
										EndIf
									EndIf
								EndIf								
							EndIf
							
						Else
							If n\Path[0]=Null Then
								
								;move towards a waypoint that is:
								;1. max 8 units away from 513-1
								;2. further away from the player than 513-1's current position 
								For w.WayPoints = Each WayPoints
									x = Abs(EntityX(n\Collider,True)-EntityX(w\obj,True))
									If x < 8.0 And x > 1.0 Then
										z = Abs(EntityZ(n\Collider,True)-EntityZ(w\obj,True))
										If z < 8.0 And z > 1.0 Then
											If EntityDistance(Collider, w\obj) > dist Then
												n\Path[0]=w
												Exit
											EndIf
										EndIf
									EndIf
								Next
								
								;no suitable path found -> 513-1 simply disappears
								If n\Path[0] = Null Then
									n\Idle = True
									n\State2 = 0
								EndIf
							Else
								
								If EntityDistance(n\Collider, n\Path[0]\obj) > 1.0 Then
									PointEntity n\obj, n\Path[0]\obj
									RotateEntity n\Collider, CurveAngle(EntityPitch(n\obj),EntityPitch(n\Collider),15.0), CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),15.0), 0, True
									n\CurrSpeed = CurveValue(0.05*Max((7.0-dist)/7.0,0.0),n\CurrSpeed,15.0)
									MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor
									If Rand(200)=1 Then MoveEntity n\Collider, 0, 0, 0.5
									RotateEntity n\Collider, 0, EntityYaw(n\Collider), 0, True
								Else
									For i = 0 To 4
										If n\Path[0]\connected[i] <> Null Then
											If EntityDistance(Collider, n\Path[0]\connected[i]\obj) > dist Then
												
												If n\LastSeen = 0 Then 
													If EntityInView(n\Collider,Camera) Then
														If EntityVisible(Collider, n\Collider) Then
															n\LastSeen = 1
															PlaySound_Strict LoadTempSound("SFX\SCP\513\Bell"+Rand(2,3)+".ogg")
														EndIf
													EndIf
												EndIf
												
												n\Path[0]=n\Path[0]\connected[i]
												Exit
											EndIf
										EndIf
									Next
									
									If n\Path[0]=Null Then n\State2 = 0
								EndIf
							EndIf
						EndIf
						
						PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.2 + Sin((MilliSecs2()/8) Mod 360)*0.1, EntityZ(n\Collider))
						
						Select n\State 
							Case 1
								If n\PrevState=0
									AnimateNPC(n,125,194,n\CurrSpeed*20)
								Else
									AnimateNPC(n,195,264,n\CurrSpeed*20)
								EndIf
								;AnimateNPC(n, 458, 527, n\CurrSpeed*20)
								RotateEntity n\obj, 0, EntityYaw(n\Collider), 0 
							Case 2
								If n\PrevState=0
									AnimateNPC(n,2,74,0.2)
								Else
									AnimateNPC(n,75,124,0.2)
								EndIf
								;AnimateNPC(n, 229, 299, 0.2)
								RotateEntity n\obj, 0, EntityYaw(n\Collider), 0						
						End Select
						
						If n\State2 > 0 Then
							If dist < 4.0 Then n\State2 = n\State2-FPSfactor*4
							n\State2 = n\State2-FPSfactor
						Else
							n\Path[0]=Null
							n\Idle = True
							n\State2=0
						EndIf
						
					End If
					
				EndIf
				
				n\DropSpeed = 0
				ResetEntity(n\Collider)						
				;[End Block]
			Case NPCtype372 ;------------------------------------------------------------------------------------------------------------------
				;[Block]
				RN$ = PlayerRoom\RoomTemplate\Name
				If RN$ <> "pocketdimension" And RN$ <> "dimension1499" Then 
					If n\Idle Then
						HideEntity(n\obj)
						If Rand(50) = 1 And (BlinkTimer < -5 And BlinkTimer > -15) Then
							ShowEntity(n\obj)
							angle# = EntityYaw(Collider)+Rnd(-90,90)
							
							dist = Rnd(1.5, 2.0)
							PositionEntity(n\Collider, EntityX(Collider) + Sin(angle) * dist, EntityY(Collider)+0.2, EntityZ(Collider) + Cos(angle) * dist)
							n\Idle = False
							n\State = Rand(20, 60)
							
							If Rand(300)=1 Then PlaySound2(RustleSFX(Rand(0,2)),Camera, n\obj, 8, Rnd(0.0,0.2))
						End If
					Else
						PositionEntity(n\obj, EntityX(n\Collider) + Rnd(-0.005, 0.005), EntityY(n\Collider)+0.3+0.1*Sin(MilliSecs2()/2), EntityZ(n\Collider) + Rnd(-0.005, 0.005))
						RotateEntity n\obj, 0, EntityYaw(n\Collider), ((MilliSecs2()/5) Mod 360)
						
						AnimateNPC(n, 32, 113, 0.4)
						;Animate2(n\obj, AnimTime(n\obj), 32, 113, 0.4)
						
						If EntityInView(n\obj, Camera) Then
							GiveAchievement(Achv372)
							
							If Rand(30)=1 Then 
								If (Not ChannelPlaying(n\SoundChn)) Then
									If EntityVisible(Camera, n\obj) Then 
										n\SoundChn = PlaySound2(RustleSFX(Rand(0,2)),Camera, n\obj, 8, 0.3)
									EndIf
								EndIf
							EndIf
							
							temp = CreatePivot()
							PositionEntity temp, EntityX(Collider), EntityY(Collider), EntityZ(Collider)
							PointEntity temp, n\Collider
							
							angle =  WrapAngle(EntityYaw(Collider)-EntityYaw(temp))
							If angle < 180 Then
								RotateEntity n\Collider, 0, EntityYaw(Collider)-80, 0		
							Else
								RotateEntity n\Collider, 0, EntityYaw(Collider)+80, 0
							EndIf
							FreeEntity temp
							
							MoveEntity n\Collider, 0, 0, 0.03*FPSfactor
							
							n\State = n\State-FPSfactor
						EndIf
						n\State=n\State-(FPSfactor/80.0)
						If n\State <= 0 Then n\Idle = True	
					End If
					
				EndIf
				
				n\DropSpeed = 0
				ResetEntity(n\Collider)						
				;[End Block]
			Case NPCtypeApache ;------------------------------------------------------------------------------------------------------------------
				;[Block]
				dist = EntityDistance(Collider, n\Collider)
				If dist<60.0 Then 
					If PlayerRoom\RoomTemplate\Name = "exit1" Then 
						dist2 = Max(Min(EntityDistance(n\Collider, PlayerRoom\Objects[3])/(8000.0*RoomScale),1.0),0.0)
					Else 
						dist2 = 1.0
					EndIf
					
					n\SoundChn = LoopSound2(ApacheSFX, n\SoundChn, Camera, n\Collider, 25.0, dist2)
				EndIf
				
				n\DropSpeed = 0
				
				Select n\State
					Case 0,1
						TurnEntity(n\obj2,0,20.0*FPSfactor,0)
						TurnEntity(n\obj3,20.0*FPSfactor,0,0)
						
						If n\State=1 And (Not NoTarget) Then
							If Abs(EntityX(Collider)-EntityX(n\Collider))< 30.0 Then
								If Abs(EntityZ(Collider)-EntityZ(n\Collider))<30.0 Then
									If Abs(EntityY(Collider)-EntityY(n\Collider))<20.0 Then
										If Rand(20)=1 Then 
											If EntityVisible(Collider, n\Collider) Then
												n\State = 2
												PlaySound2(AlarmSFX(2), Camera, n\Collider, 50, 1.0)
											EndIf
										EndIf									
									EndIf
								EndIf
							EndIf							
						EndIf
					Case 2,3 ;player located -> attack
						
						If n\State = 2 Then 
							target = Collider
						ElseIf n\State = 3
							target=CreatePivot()
							PositionEntity target, n\EnemyX, n\EnemyY, n\EnemyZ, True
						EndIf
						
						If NoTarget And n\State = 2 Then n\State = 1
						
						TurnEntity(n\obj2,0,20.0*FPSfactor,0)
						TurnEntity(n\obj3,20.0*FPSfactor,0,0)
						
						If Abs(EntityX(target)-EntityX(n\Collider)) < 55.0 Then
							If Abs(EntityZ(target)-EntityZ(n\Collider)) < 55.0 Then
								If Abs(EntityY(target)-EntityY(n\Collider))< 20.0 Then
									PointEntity n\obj, target
									RotateEntity n\Collider, CurveAngle(Min(WrapAngle(EntityPitch(n\obj)),40.0),EntityPitch(n\Collider),40.0), CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),90.0), EntityRoll(n\Collider), True
									PositionEntity(n\Collider, EntityX(n\Collider), CurveValue(EntityY(target)+8.0,EntityY(n\Collider),70.0), EntityZ(n\Collider))
									
									dist# = Distance(EntityX(target),EntityZ(target),EntityX(n\Collider),EntityZ(n\Collider))
									
									n\CurrSpeed = CurveValue(Min(dist-6.5,6.5)*0.008, n\CurrSpeed, 50.0)
									
									;If Distance(EntityX(Collider),EntityZ(Collider),EntityX(n\collider),EntityZ(n\collider)) > 6.5 Then
									;	n\currspeed = CurveValue(0.08,n\currspeed,50.0)
									;Else
									;	n\currspeed = CurveValue(0.0,n\currspeed,30.0)
									;EndIf
									MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor
									
									
									If n\PathTimer = 0 Then
										n\PathStatus = EntityVisible(n\Collider,target)
										n\PathTimer = Rand(100,200)
									Else
										n\PathTimer = Min(n\PathTimer-FPSfactor,0.0)
									EndIf
									
									If n\PathStatus = 1 Then ;player visible
										RotateEntity n\Collider, EntityPitch(n\Collider), EntityYaw(n\Collider), CurveAngle(0, EntityRoll(n\Collider),40), True
										
										If n\Reload =< 0 Then
											If dist<20.0 Then
												pvt = CreatePivot()
												
												PositionEntity pvt, EntityX(n\Collider),EntityY(n\Collider), EntityZ(n\Collider)
												RotateEntity pvt, EntityPitch(n\Collider), EntityYaw(n\Collider),EntityRoll(n\Collider)
												MoveEntity pvt, 0, 8.87*(0.21/9.0), 8.87*(1.7/9.0) ;2.3
												PointEntity pvt, target
												
												If WrapAngle(EntityYaw(pvt)-EntityYaw(n\Collider))<10 Then
													PlaySound2(Gunshot2SFX, Camera, n\Collider, 20)
													
													If PlayerRoom\RoomTemplate\Name = "exit1" Then
														DeathMSG = Chr(34)+"CH-2 to control. Shot down a runaway Class D at Gate B."+Chr(34)
													Else
														DeathMSG = Chr(34)+"CH-2 to control. Shot down a runaway Class D at Gate A."+Chr(34)
													EndIf
													
													Shoot( EntityX(pvt),EntityY(pvt), EntityZ(pvt),((10/dist)*(1/dist))*(n\State=2),(n\State=2))
													
													n\Reload = 5
												EndIf
												
												FreeEntity pvt
											EndIf
										EndIf
									Else 
										RotateEntity n\Collider, EntityPitch(n\Collider), EntityYaw(n\Collider), CurveAngle(-20, EntityRoll(n\Collider),40), True
									EndIf
									MoveEntity n\Collider, -EntityRoll(n\Collider)*0.002,0,0
									
									n\Reload=n\Reload-FPSfactor
									
									
								EndIf
							EndIf
						EndIf		
						
						If n\State = 3 Then FreeEntity target
					Case 4 ;crash
						If n\State2 < 300 Then
							
							TurnEntity(n\obj2,0,20.0*FPSfactor,0)
							TurnEntity(n\obj3,20.0*FPSfactor,0,0)
							
							TurnEntity n\Collider,0,-FPSfactor*7,0;Sin(MilliSecs2()/40)*FPSfactor
							n\State2=n\State2+FPSfactor*0.3
							
							target=CreatePivot()
							PositionEntity target, n\EnemyX, n\EnemyY, n\EnemyZ, True
							
							PointEntity n\obj, target
							MoveEntity n\obj, 0,0,FPSfactor*0.001*n\State2
							PositionEntity(n\Collider, EntityX(n\obj), EntityY(n\obj), EntityZ(n\obj))
							
							If EntityDistance(n\obj, target) <0.3 Then
								;If TempSound2 <> 0 Then FreeSound_Strict TempSound2 : TempSound2 = 0
								;TempSound2 = LoadSound_Strict("SFX\Character\Apache\Crash"+Rand(1,2)+".ogg")
								CameraShake = Max(CameraShake, 3.0)
								;PlaySound_Strict TempSound2
								PlaySound_Strict LoadTempSound("SFX\Character\Apache\Crash"+Rand(1,2)+".ogg")
								n\State = 5
							EndIf
							
							FreeEntity target
						EndIf
				End Select
				
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider), EntityZ(n\Collider))
				RotateEntity n\obj, EntityPitch(n\Collider), EntityYaw(n\Collider), EntityRoll(n\Collider), True
				;[End Block]
			Case NPCtypeTentacle
				;[Block]
				dist = EntityDistance(n\Collider,Collider)
				
				If dist < HideDistance
					
					Select n\State 
						Case 0 ;spawn
							
							If n\Frame>283 Then
								HeartBeatVolume = Max(CurveValue(1.0, HeartBeatVolume, 50),HeartBeatVolume)
								HeartBeatRate = Max(CurveValue(130, HeartBeatRate, 100),HeartBeatRate)
								
								PointEntity n\obj, Collider
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),25.0), 0
								
								AnimateNPC(n, 283, 389, 0.3, False)
								;Animate2(n\obj, AnimTime(n\obj), 283, 389, 0.3, False)
								
								If n\Frame>388 Then
									n\State = 1
									FreeSound_Strict n\Sound2 : n\Sound2 = 0
								EndIf
							Else
								If dist < 2.5 Then 
									SetNPCFrame(n, 284)
									n\Sound2 = LoadSound_Strict("SFX\Room\035Chamber\TentacleSpawn.ogg")
									PlaySound_Strict(n\Sound2)
								EndIf
							EndIf
							;spawn 283,389
							;attack 2, 32
							;idle 33, 174
						Case 1 ;idle
							If n\Sound2=0 Then
								FreeSound_Strict n\Sound2 : n\Sound2=0
								n\Sound2 = LoadSound_Strict("SFX\Room\035Chamber\TentacleIdle.ogg")
							EndIf
							n\SoundChn2 = LoopSound2(n\Sound2,n\SoundChn2,Camera,n\Collider)
							
							If dist < 1.8 Then 
								If Abs(DeltaYaw(n\Collider, Collider))<20 Then 
									n\State = 2
									If n\Sound<>0 Then FreeSound_Strict n\Sound : n\Sound = 0 
									;If n\Sound2<>0 Then FreeSound_Strict n\Sound2 : n\Sound2 = 0
								EndIf
							EndIf
							
							PointEntity n\obj, Collider
							RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),25.0), 0
							
							AnimateNPC(n, 33, 174, 0.3, True)
							;Animate2(n\obj, AnimTime(n\obj), 33, 174, 0.3, True)
						Case 2
							
							;finish the idle animation before playing the attack animation
							If n\Frame>33 And n\Frame<174 Then
								AnimateNPC(n, 33, 174, 2.0, False)
								;Animate2(n\obj, AnimTime(n\obj), 33, 174, 2.0, False)
							Else
								PointEntity n\obj, Collider
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),10.0), 0							
								
								If n\Frame>33 Then 
									;SetAnimTime(n\obj,2)
									If n\Sound2<>0 Then FreeSound_Strict n\Sound2 : n\Sound2 = 0
									n\Frame = 2
									n\Sound = LoadSound_Strict("SFX\Room\035Chamber\TentacleAttack"+Rand(1,2)+".ogg")
									PlaySound_Strict(n\Sound)
								EndIf
								AnimateNPC(n, 2, 32, 0.3, False)
								;Animate2(n\obj, AnimTime(n\obj), 2, 32, 0.3, False)
								
								If n\Frame>=5 And n\Frame<6 Then
									If dist < 1.8 Then
										If Abs(DeltaYaw(n\Collider, Collider))<20 Then 
											If WearingHazmat Then
												Injuries = Injuries+Rnd(0.5)
												PlaySound_Strict(LoadTempSound("SFX\General\BodyFall.ogg"))
											Else
												BlurTimer = 100
												Injuries = Injuries+Rnd(1.0,1.5)
												PlaySound_Strict DamageSFX(Rand(2,3))
												
												If Injuries > 3.0 Then
													If PlayerRoom\RoomTemplate\Name = "room2offices" Then
														DeathMSG = Chr(34)+"One large and highly active tentacle-like appendage seems "
														DeathMSG = DeathMSG + "to have grown outside the dead body of a scientist within office area [REDACTED]. Its level of aggression is "
														DeathMSG = DeathMSG + "unlike anything we've seen before - it looks like it has "
														DeathMSG = DeathMSG + "beaten some unfortunate Class D to death at some point during the breach."+Chr(34)
													Else
														DeathMSG = Chr(34)+"We will need more than the regular cleaning team to take care of this. "
														DeathMSG = DeathMSG + "Two large and highly active tentacle-like appendages seem "
														DeathMSG = DeathMSG + "to have formed inside the chamber. Their level of aggression is "
														DeathMSG = DeathMSG + "unlike anything we've seen before - it looks like they have "
														DeathMSG = DeathMSG + "beaten some unfortunate Class D to death at some point during the breach."+Chr(34)
													EndIf
													Kill()
												EndIf
											EndIf
											
										EndIf
									EndIf
									
									n\Frame = 6
									;SetAnimTime(n\obj, 6)
								ElseIf n\Frame=32
									n\State = 1
									n\Frame = 173
									;SetAnimTime(n\obj, 173)
								EndIf
							EndIf
							
					End Select
					
				EndIf
				
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider), EntityZ(n\Collider))
				RotateEntity n\obj, EntityPitch(n\Collider)-90, EntityYaw(n\Collider)-180, EntityRoll(n\Collider), True
				
				n\DropSpeed = 0
				
				ResetEntity n\Collider
				;[End Block]
			Case NPCtype860
				;[Block]
				If PlayerRoom\RoomTemplate\Name = "room860" Then
					Local fr.Forest=PlayerRoom\fr;Object.Forest(e\room\Objects[1])
					
					dist = EntityDistance(Collider,n\Collider)
					
					If ForestNPC<>0
						If ForestNPCData[2]=1
							ShowEntity ForestNPC
							If n\State<>1
								If (BlinkTimer<-8 And BlinkTimer >-12) Or (Not EntityInView(ForestNPC,Camera))
									ForestNPCData[2]=0
									HideEntity ForestNPC
								EndIf
							EndIf
						Else
							HideEntity ForestNPC
						EndIf
					EndIf
					
					Select n\State
						Case 0 ;idle (hidden)
							
							HideEntity n\Collider
							HideEntity n\obj
							HideEntity n\obj2
							
							n\State2 = 0
							PositionEntity(n\Collider, 0, -100, 0)
						Case 1 ;appears briefly behind the trees
							n\DropSpeed = 0
							
							If EntityY(n\Collider)<= -100 Then
								;transform the position of the player to the local coordinates of the forest
								TFormPoint(EntityX(Collider),EntityY(Collider),EntityZ(Collider),0,fr\Forest_Pivot)
								
								;calculate the indices of the forest cell the player is in
								x = Floor((TFormedX()+6.0)/12.0)
								z = Floor((TFormedZ()+6.0)/12.0)
								
								;step through nearby cells
								For x2 = Max(x-1,1) To Min(x+1,gridsize) Step 2
									For z2 = Max(z-1,1) To Min(z+1,gridsize) Step 2
										;choose an empty cell (not on the path)
										If fr\grid[(z2*gridsize)+x2]=0 Then
											;spawn the monster between the empty cell and the cell the player is in
											TFormPoint(((x2+x)/2)*12.0,0,((z2+z)/2)*12.0,fr\Forest_Pivot,0)
											
											;in view -> nope, keep searching for a more suitable cell
											If EntityInView(n\Collider, Camera) Then
												PositionEntity n\Collider, 0, -110, 0
												DebugLog("spawned monster in view -> hide")
											Else ; not in view -> all good
												DebugLog("spawned monster successfully")
												
												PositionEntity n\Collider, TFormedX(), EntityY(fr\Forest_Pivot,True)+2.3, TFormedZ()
												
												x2 = gridsize
												Exit												
											EndIf
										EndIf
									Next
								Next
								
								If EntityY(n\Collider)> -100 Then
									PlaySound2(Step2SFX(Rand(3,5)), Camera, n\Collider, 15.0, 0.5)
									
									If ForestNPCData[2]<>1 Then ForestNPCData[2]=0
									
									Select Rand(3)
										Case 1
											PointEntity n\Collider, Collider
											n\Frame = 2
											;SetAnimTime(n\obj, 2)
										Case 2
											PointEntity n\Collider, Collider
											n\Frame = 201
											;SetAnimTime(n\obj, 201)
										Case 3
											PointEntity n\Collider, Collider
											TurnEntity n\Collider, 0, 90, 0
											n\Frame = 299
											;SetAnimTime(n\obj, 299)
									End Select
									
									n\State2 = 0
								EndIf
							Else
								
								ShowEntity n\obj
								ShowEntity n\Collider
								
								PositionEntity n\Collider, EntityX(n\Collider), EntityY(fr\Forest_Pivot,True)+2.3, EntityZ(n\Collider)
								
								;[TODO]
								If ForestNPC<>0
									If ForestNPCData[2]=0
										Local docchance% = 0
										Local docamount% = 0
										For i = 0 To MaxItemAmount-1
											If Inventory(i)<>Null
												Local docname$ = Inventory(i)\itemtemplate\name
												If docname = "Log #1" Or docname = "Log #2" Or docname = "Log #3"
													;860,850,830,800
													docamount% = docamount% + 1
													docchance = docchance + 10*docamount%
												EndIf
											EndIf
										Next
										
										If Rand(1,860-docchance)=1
											ShowEntity ForestNPC
											ForestNPCData[2]=1
											If Rand(2)=1
												ForestNPCData[0]=0
											Else
												ForestNPCData[0]=2
											EndIf
											ForestNPCData[1]=0
											PositionEntity ForestNPC,EntityX(n\Collider),EntityY(n\Collider)+0.5,EntityZ(n\Collider)
											RotateEntity ForestNPC,0,EntityYaw(n\Collider),0
											MoveEntity ForestNPC,0.75,0,0
											RotateEntity ForestNPC,0,0,0
											EntityTexture ForestNPC,ForestNPCTex,ForestNPCData[0]
										Else
											ForestNPCData[2]=2
										EndIf
									ElseIf ForestNPCData[2]=1
										If ForestNPCData[1]=0.0
											If Rand(200)=1
												ForestNPCData[1]=FPSfactor
												EntityTexture ForestNPC,ForestNPCTex,ForestNPCData[0]+1
											EndIf
										ElseIf ForestNPCData[1]>0.0 And ForestNPCData[1]<5.0
											ForestNPCData[1]=Min(ForestNPCData[1]+FPSfactor,5.0)
										Else
											ForestNPCData[1]=0
											EntityTexture ForestNPC,ForestNPCTex,ForestNPCData[0]
										EndIf
									EndIf
								EndIf
								
								If n\State2 = 0 Then ;don't start moving until the player is looking
									If EntityInView(n\Collider, Camera) Then 
										n\State2 = 1
										If Rand(8)=1 Then
											PlaySound2(LoadTempSound("SFX\SCP\860\Cancer"+Rand(0,2)+".ogg"), Camera, n\Collider, 20.0)
										EndIf										
									EndIf
								Else
									If n\Frame<=199 Then
										AnimateNPC(n, 2, 199, 0.5,False)
										If n\Frame=199 Then n\Frame = 298 : PlaySound2(Step2SFX(Rand(3,5)), Camera, n\Collider, 15.0)
									ElseIf n\Frame <= 297
										PointEntity n\Collider, Collider
										
										AnimateNPC(n, 200, 297, 0.5, False)
										If n\Frame=297 Then n\Frame=298 : PlaySound2(Step2SFX(Rand(3,5)), Camera, n\Collider, 15.0)
									Else
										angle = CurveAngle(point_direction(EntityX(n\Collider),EntityZ(n\Collider),EntityX(Collider),EntityZ(Collider)),EntityYaw(n\Collider)+90,20.0)
										
										RotateEntity n\Collider, 0, angle-90, 0, True
										
										AnimateNPC(n, 298, 316, n\CurrSpeed*10)
										
										;Animate2(n\obj, AnimTime(n\obj), 298, 316, n\CurrSpeed*10)
										
										n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 10.0)
										MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor
										
										If dist>15.0 Then
											PositionEntity n\Collider, 0,-110,0
											n\State = 0
											n\State2 = 0
										EndIf
									EndIf									
								EndIf
								
							EndIf
							
							ResetEntity n\Collider
						Case 2 ;appears on the path and starts to walk towards the player
							ShowEntity n\obj
							ShowEntity n\Collider
							
							prevFrame = n\Frame
							
							If EntityY(n\Collider)<= -100 Then
								;transform the position of the player to the local coordinates of the forest
								TFormPoint(EntityX(Collider),EntityY(Collider),EntityZ(Collider),0,fr\Forest_Pivot)
								
								;calculate the indices of the forest cell the player is in
								x = Floor((TFormedX()+6.0)/12.0)
								z = Floor((TFormedZ()+6.0)/12.0)
								
								For x2 = Max(x-1,1) To Min(x+1,gridsize)
									For z2 = Max(z-1,1) To Min(z+1,gridsize)
										;find a nearby cell that's on the path and NOT the cell the player is in
										If fr\grid[(z2*gridsize)+x2]>0 And (x2<>x Or z2<>z) And (x2=x Or z2=z) Then
											
											;transform the position of the cell back to world coordinates
											TFormPoint(x2*12.0, 0,z2*12.0, fr\Forest_Pivot,0)
											
											PositionEntity n\Collider, TFormedX(), EntityY(fr\Forest_Pivot,True)+1.0,TFormedZ()
											
											DebugLog(TFormedX()+", "+TFormedZ())
											
											If EntityInView(n\Collider, Camera) Then
												BlinkTimer=-10
											Else
												x2 = gridsize
												Exit
											EndIf
										EndIf
									Next
								Next
							Else
								angle = CurveAngle(Find860Angle(n, fr),EntityYaw(n\Collider)+90,80.0)
								
								RotateEntity n\Collider, 0, angle-90, 0, True
								
								n\CurrSpeed = CurveValue(n\Speed*0.3, n\CurrSpeed, 50.0)
								MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor
								
								AnimateNPC(n, 494, 569, n\CurrSpeed*25)
								
								If n\State2 = 0 Then
									If dist<8.0 Then
										If EntityInView(n\Collider,Camera) Then
											PlaySound_Strict LoadTempSound("SFX\SCP\860\Chase"+Rand(1,2)+".ogg")
											
											PlaySound2(LoadTempSound("SFX\SCP\860\Cancer"+Rand(0,2)+".ogg"), Camera, n\Collider)	
											n\State2 = 1
										EndIf										
									EndIf
								EndIf
								
								If CurrSpeed > 0.03 Then ;the player is running
									n\State3 = n\State3 + FPSfactor
									If Rnd(5000)<n\State3 Then
										temp = True
										If n\SoundChn <> 0 Then
											If ChannelPlaying (n\SoundChn) Then temp = False
										EndIf
										If temp Then
											n\SoundChn = PlaySound2(LoadTempSound("SFX\SCP\860\Cancer"+Rand(0,2)+".ogg"), Camera, n\Collider)
										EndIf
									EndIf
								Else
									n\State3 = Max(n\State3 - FPSfactor,0)
								EndIf
								
								If dist<4.5 Or n\State3 > Rnd(200,250) Then
									n\SoundChn = PlaySound2(LoadTempSound("SFX\SCP\860\Cancer"+Rand(3,5)+".ogg"), Camera, n\Collider)
									n\State = 3
								EndIf
								
								If dist > 20.0 Then
									n\State = 0
									n\State2 = 0
									PositionEntity n\Collider, 0,-110,0
								EndIf
							EndIf
							
							;535, 568
							If (prevFrame < 533 And n\Frame=>533) Or (prevFrame > 568 And n\Frame<2) Then
								PlaySound2(Step2SFX(Rand(3,5)), Camera, n\Collider, 15.0, 0.6)
							EndIf
							
						Case 3 ;runs towards the player and attacks
							ShowEntity n\obj
							ShowEntity n\Collider
							
							prevFrame = n\Frame
							
							angle = CurveAngle(Find860Angle(n, fr),EntityYaw(n\Collider)+90,40.0)
							
							RotateEntity n\Collider, 0, angle-90, 0, True
							
							If n\Sound = 0 Then n\Sound = LoadSound_Strict("SFX\General\Slash1.ogg")
							If n\Sound2 = 0 Then n\Sound2 = LoadSound_Strict("SFX\General\Slash2.ogg")
							
							;if close enough to attack OR already attacking, play the attack anim
							If (dist<1.1 Or (n\Frame>451 And n\Frame<493) Or KillTimer < 0) Then
								DeathMSG = ""
								
								n\CurrSpeed = CurveValue(0.0, n\CurrSpeed, 5.0)
								
								AnimateNPC(n, 451,493, 0.5, False)
								
								;Animate2(n\obj, AnimTime(n\obj), 451,493, 0.5, False)
								If (prevFrame < 461 And n\Frame=>461) Then 
									If KillTimer => 0 Then Kill() : KillAnim = 0
									PlaySound_Strict(n\Sound)
								EndIf
								If (prevFrame < 476 And n\Frame=>476) Then PlaySound_Strict(n\Sound2)
								If (prevFrame < 486 And n\Frame=>486) Then PlaySound_Strict(n\Sound2)
							Else
								n\CurrSpeed = CurveValue(n\Speed*0.8, n\CurrSpeed, 10.0)
								
								AnimateNPC(n, 298, 316, n\CurrSpeed*10)
								;Animate2(n\obj, AnimTime(n\obj), 298, 316, n\CurrSpeed*10)
								
								If (prevFrame < 307 And n\Frame=>307) Then
									PlaySound2(Step2SFX(Rand(3,5)), Camera, n\Collider, 10.0)
								EndIf
							EndIf
							
							MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor
					End Select
					
					If n\State <> 0 Then
						RotateEntity n\Collider, 0, EntityYaw(n\Collider), 0, True	
						
						PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider)-0.1, EntityZ(n\Collider))
						RotateEntity n\obj, EntityPitch(n\Collider)-90, EntityYaw(n\Collider), EntityRoll(n\Collider), True
						
						If dist > 8.0 Then
							ShowEntity n\obj2
							EntityAlpha n\obj2, Min(dist-8.0,1.0)
							
							PositionEntity(n\obj2, EntityX(n\obj), EntityY(n\obj) , EntityZ(n\obj))
							RotateEntity(n\obj2, 0, EntityYaw(n\Collider) - 180, 0)
							MoveEntity(n\obj2, 0, 30.0*0.025, -33.0*0.025)
							
							;render distance is set to 8.5 inside the forest,
							;so we need to cheat a bit to make the eyes visible if they're further than that
							pvt = CreatePivot()
							PositionEntity pvt, EntityX(Camera),EntityY(Camera),EntityZ(Camera)
							PointEntity pvt, n\obj2
							MoveEntity pvt, 0,0,8.0
							PositionEntity n\obj2, EntityX(pvt),EntityY(pvt),EntityZ(pvt)
							FreeEntity pvt
						Else
							HideEntity n\obj2
						EndIf
					EndIf
				EndIf
				;[End Block] 
			Case NPCtype939
				;[Block]
				
				If PlayerRoom\RoomTemplate\Name <> "room3storage"
					n\State = 66
				EndIf
				
				;state is set to 66 in the room3storage-event if player isn't inside the room
				If n\State < 66 Then 
					Select n\State
						Case 0
							AnimateNPC(n, 290,405,0.1)
							
							;Animate2(n\obj,AnimTime(n\obj),290,405,0.1)
						Case 1
							
							If n\Frame=>644 And n\Frame<683 Then ;finish the walking animation
								;n\CurrSpeed = CurveValue(n\Speed*0.2, n\CurrSpeed, 10.0)
								n\CurrSpeed = CurveValue(n\Speed*0.05, n\CurrSpeed, 10.0)
								AnimateNPC(n, 644,683,28*n\CurrSpeed*4,False)
								If n\Frame=>682 Then n\Frame =175
								
								;Animate2(n\obj,AnimTime(n\obj),644,683,28*n\CurrSpeed,False)
								;If AnimTime(n\obj)=683 Then SetAnimTime(n\obj,175)
							Else
								n\CurrSpeed = CurveValue(0, n\CurrSpeed, 5.0)
								AnimateNPC(n, 175,297,0.22,False)
								If n\Frame=>296 Then n\State = 2
								
								;Animate2(n\obj,AnimTime(n\obj),175,297,0.22,False)
								;If AnimTime(n\obj)=297 Then n\State = 2
							EndIf
							
							n\LastSeen = 0
							
							MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor						
							
						Case 2
							n\State2 = Max(n\State2, (n\PrevState-3))
							
							dist = EntityDistance(n\Collider, PlayerRoom\Objects[n\State2])
							
							n\CurrSpeed = CurveValue(n\Speed*0.3*Min(dist,1.0), n\CurrSpeed, 10.0)
							MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor 
							
							prevFrame = n\Frame
							AnimateNPC(n, 644,683,28*n\CurrSpeed) ;walk
							
							;prevFrame = AnimTime(n\obj)
							;Animate2(n\obj,AnimTime(n\obj),644,683,28*n\CurrSpeed) ;walk
							
							If (prevFrame<664 And n\Frame=>664) Or (prevFrame>673 And n\Frame<654) Then
								PlaySound2(StepSFX(4, 0, Rand(0,3)), Camera, n\Collider, 12.0)
								If Rand(10)=1 Then
									temp = False
									If n\SoundChn = 0 Then 
										temp = True
									ElseIf Not ChannelPlaying(n\SoundChn)
										temp = True
									EndIf
									If temp Then
										If n\Sound <> 0 Then FreeSound_Strict n\Sound : n\Sound = 0
										n\Sound = LoadSound_Strict("SFX\SCP\939\"+(n\ID Mod 3)+"Lure"+Rand(1,10)+".ogg")
										n\SoundChn = PlaySound2(n\Sound, Camera, n\Collider)
									EndIf
								EndIf
							EndIf
							
							PointEntity n\obj, PlayerRoom\Objects[n\State2]
							RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),20.0), 0
							
							If dist<0.4 Then
								n\State2 = n\State2 + 1
								If n\State2 > n\PrevState Then n\State2 = (n\PrevState-3)
								n\State = 1
							EndIf
							
						Case 3
							If EntityVisible(Collider, n\Collider) Then
								If n\Sound2 = 0 Then n\Sound2 = LoadSound_Strict("SFX\General\Slash1.ogg")
								
								n\EnemyX = EntityX(Collider)
								n\EnemyZ = EntityZ(Collider)
								n\LastSeen = 10*7
							EndIf
							
							If n\LastSeen > 0 And (Not NoTarget) Then
								prevFrame = n\Frame
								
								If (n\Frame=>18.0 And n\Frame<68.0) Then
									n\CurrSpeed = CurveValue(0, n\CurrSpeed, 5.0)
									AnimateNPC(n, 18,68,0.5,True)
									;Animate2(n\obj,AnimTime(n\obj),18,68,0.5,True)
									
									;hasn't hit
									temp = False
									
									If prevFrame < 24 And n\Frame>=24 Then
										temp = True
									ElseIf prevFrame < 57 And n\Frame>=57
										temp = True
									EndIf
									
									If temp Then
										If Distance(n\EnemyX, n\EnemyZ, EntityX(n\Collider), EntityZ(n\Collider))<1.5 Then
											PlaySound_Strict n\Sound2
											Injuries = Injuries + Rnd(1.5, 2.5)-WearingVest*0.5
											BlurTimer = 500		
										Else
											n\Frame	 = 449
											;SetAnimTime(n\obj, 449)
										EndIf
									EndIf
									
									If Injuries>4.0 Then 
										DeathMSG=Chr(34)+"All four (4) escaped SCP-939 specimens have been captured and recontained successfully. "
										DeathMSG=DeathMSG+"Three (3) of them made quite a mess at Storage Area 6. A cleaning team has been dispatched."+Chr(34)
										Kill()
										If (Not GodMode) Then n\State = 5
									EndIf								
								Else
									If n\LastSeen = 10*7 Then 
										n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 20.0)
										
										AnimateNPC(n, 449,464,6*n\CurrSpeed) ;run
										;Animate2(n\obj,AnimTime(n\obj),449,464,6*n\CurrSpeed) ;run
										
										If (prevFrame<452 And n\Frame=>452) Or (prevFrame<459 And n\Frame=>459) Then
											PlaySound2(StepSFX(1, 1, Rand(0,7)), Camera, n\Collider, 12.0)
										EndIf										
										
										If Distance(n\EnemyX, n\EnemyZ, EntityX(n\Collider), EntityZ(n\Collider))<1.1 Then ;player is visible
											n\Frame = 18
											;SetAnimTime(n\obj, 18.0)
										EndIf
									Else
										n\CurrSpeed = CurveValue(0, n\CurrSpeed, 5.0)
										AnimateNPC(n, 175,297,5*n\CurrSpeed,True)	
										;Animate2(n\obj,AnimTime(n\obj),175,297,5*n\CurrSpeed,True)
									EndIf
									
								EndIf
								
								angle = VectorYaw(n\EnemyX-EntityX(n\Collider), 0.0, n\EnemyZ-EntityZ(n\Collider))
								RotateEntity n\Collider, 0, CurveAngle(angle,EntityYaw(n\Collider),15.0), 0									
								
								MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor							
								
								n\LastSeen = n\LastSeen - FPSfactor
							Else
								n\State = 2
							EndIf
							
						;Animate2(n\obj,AnimTime(n\obj),406,437,0.1) ;leap
						Case 5
							If n\Frame<68 Then
								AnimateNPC(n, 18,68,0.5,False) ;finish the attack animation
								
								;Animate2(n\obj,AnimTime(n\obj),18,68,0.5,False) ;finish the attack animation
							Else
								AnimateNPC(n, 464,473,0.5,False) ;attack to idle
								
								;Animate2(n\obj,AnimTime(n\obj),464,473,0.5,False) ;attack to idle
							EndIf
							
					End Select
					
					If n\State < 3 And (Not NoTarget) And (Not n\IgnorePlayer) Then
						dist = EntityDistance(n\Collider, Collider)
						
						If dist < 4.0 Then dist = dist - EntityVisible(Collider, n\Collider)
						If PlayerSoundVolume*1.2>dist Or dist < 1.5 Then
							If n\State3 = 0 Then
								If n\Sound <> 0 Then FreeSound_Strict n\Sound : n\Sound = 0
								n\Sound = LoadSound_Strict("SFX\SCP\939\"+(n\ID Mod 3)+"Attack"+Rand(1,3)+".ogg")
								n\SoundChn = PlaySound2(n\Sound, Camera, n\Collider)										
								
								PlaySound_Strict(LoadTempSound("SFX\SCP\939\attack.ogg"))
								n\State3 = 1
							EndIf
							
							n\State = 3
						ElseIf PlayerSoundVolume*1.6>dist
							If n\State<>1 And n\Reload <= 0 Then
								If n\Sound <> 0 Then FreeSound_Strict n\Sound : n\Sound = 0
								n\Sound = LoadSound_Strict("SFX\SCP\939\"+(n\ID Mod 3)+"Alert"+Rand(1,3)+".ogg")
								n\SoundChn = PlaySound2(n\Sound, Camera, n\Collider)	
								
								n\Frame = 175
								n\Reload = 70 * 3
								;SetAnimTime(n\obj, 175)	
							EndIf
							
							n\State = 1
							
						EndIf
						
						n\Reload = n\Reload - FPSfactor
						
					EndIf				
					
					RotateEntity n\Collider, 0, EntityYaw(n\Collider), 0, True	
					
					PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider)-0.28, EntityZ(n\Collider))
					RotateEntity n\obj, EntityPitch(n\Collider)-90, EntityYaw(n\Collider), EntityRoll(n\Collider), True					
				EndIf
				;[End Block]
			Case NPCtype066
				;[Block]
				dist = Distance(EntityX(Collider),EntityZ(Collider),EntityX(n\Collider),EntityZ(n\Collider))
				
				Select n\State
					Case 0 
						;idle: moves around randomly from waypoint to another if the player is far enough
						;starts staring at the player when the player is close enough
						
						If dist > 20.0 Then
							AnimateNPC(n, 451, 612, 0.2, True)
							;Animate2(n\obj, AnimTime(n\obj), 451, 612, 0.2, True)
							
							If n\State2 < MilliSecs2() Then
								For w.waypoints = Each WayPoints
									If w\door = Null Then
										If Abs(EntityX(w\obj,True)-EntityX(n\Collider))<4.0 Then
											If Abs(EntityZ(w\obj,True)-EntityZ(n\Collider))<4.0 Then
												PositionEntity n\Collider, EntityX(w\obj,True), EntityY(w\obj,True)+0.3, EntityZ(w\obj,True)
												ResetEntity n\Collider
												Exit
											EndIf
										EndIf
									EndIf
								Next
								n\State2 = MilliSecs2()+5000
							EndIf
						ElseIf dist < 8.0
							n\LastDist = Rnd(1.0, 2.5)
							n\State = 1
						EndIf
					Case 1 ;staring at the player
						
						If n\Frame<451 Then
							angle = WrapAngle(CurveAngle(DeltaYaw(n\Collider, Collider)-180, (AnimTime(n\obj)-2.0)/1.2445, 15.0))
							;0->360 = 2->450
							SetNPCFrame(n,angle*1.2445+2.0)
							
							;SetAnimTime(n\obj, angle*1.2445+2.0)							
						Else
							AnimateNPC(n, 636, 646, 0.4, False)
							If n\Frame = 646 Then SetNPCFrame(n,2)
							;Animate2(n\obj, AnimTime(n\obj), 636, 646, 0.4, False)
							;If AnimTime(n\obj)=646 Then SetAnimTime (n\obj, 2)
						EndIf
						dist = Distance(EntityX(Collider),EntityZ(Collider),EntityX(n\Collider),EntityZ(n\Collider))
						
						If Rand(700)=1 Then PlaySound2(LoadTempSound("SFX\SCP\066\Eric"+Rand(1,3)+".ogg"),Camera, n\Collider, 8.0)
						
						If dist < 1.0+n\LastDist Then n\State = Rand(2,3)
					Case 2 ;roll towards the player and make a sound, and then escape	
						If n\Frame < 647 Then 
							angle = CurveAngle(0, (AnimTime(n\obj)-2.0)/1.2445, 5.0)
							
							If angle < 5 Or angle > 355 Then 
								SetNPCFrame(n,647)
							Else
								SetNPCFrame(n,angle*1.2445+2.0)
							EndIf
							;SetAnimTime(n\obj, angle*1.2445+2.0)
							;If angle < 5 Or angle > 355 Then SetAnimTime(n\obj, 647)
						Else
							If n\Frame=683 Then 
								If n\State2 = 0 Then
									If Rand(2)=1 Then
										PlaySound2(LoadTempSound("SFX\SCP\066\Eric"+Rand(1,3)+".ogg"),Camera, n\Collider, 8.0)
									Else
										PlaySound2(LoadTempSound("SFX\SCP\066\Notes"+Rand(1,6)+".ogg"), Camera, n\Collider, 8.0)
									EndIf									
									
									Select Rand(1,6)
										Case 1
											If n\Sound2=0 Then n\Sound2=LoadSound_Strict("SFX\SCP\066\Beethoven.ogg")
											n\SoundChn2 = PlaySound2(n\Sound2, Camera, n\Collider)
											DeafTimer# = 70*(45+(15*SelectedDifficulty\aggressiveNPCs))
											DeafPlayer = True
											CameraShake = 10.0
										Case 2
											n\State3 = Rand(700,1400)
										Case 3
											For d.Doors = Each Doors
												If d\locked = False And d\KeyCard = 0 And d\Code = "" Then
													If Abs(EntityX(d\frameobj)-EntityX(n\Collider))<16.0 Then
														If Abs(EntityZ(d\frameobj)-EntityZ(n\Collider))<16.0 Then
															UseDoor(d, False)
														EndIf
													EndIf
												EndIf
											Next
										Case 4
											If PlayerRoom\RoomTemplate\DisableDecals = False Then
												CameraShake = 5.0
												de.Decals = CreateDecal(1, EntityX(n\Collider), 0.01, EntityZ(n\Collider), 90, Rand(360), 0)
												de\Size = 0.3 : UpdateDecals
												PlaySound_Strict(LoadTempSound("SFX\General\BodyFall.ogg"))
												If Distance(EntityX(Collider),EntityZ(Collider),EntityX(n\Collider),EntityZ(n\Collider))<0.8 Then
													Injuries = Injuries + Rnd(0.3,0.5)
												EndIf
											EndIf
									End Select
								EndIf
								
								n\State2 = n\State2+FPSfactor
								If n\State2>70 Then 
									n\State = 3
									n\State2 = 0
								EndIf
							Else
								n\CurrSpeed = CurveValue(n\Speed*1.5, n\CurrSpeed, 10.0)
								PointEntity n\obj, Collider
								;angle = CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 10);1.0/Max(n\CurrSpeed,0.0001))
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj)-180, EntityYaw(n\Collider), 10), 0
								
								AnimateNPC(n, 647, 683, n\CurrSpeed*25, False)
								;Animate2(n\obj, AnimTime(n\obj), 647, 683, n\CurrSpeed*25, False)
								
								MoveEntity n\Collider, 0,0,-n\CurrSpeed*FPSfactor
								
							EndIf
						EndIf
					Case 3
						PointEntity n\obj, Collider
						angle = CurveAngle(EntityYaw(n\obj)+n\Angle-180, EntityYaw(n\Collider), 10);1.0/Max(n\CurrSpeed,0.0001))
						RotateEntity n\Collider, 0, angle, 0
						
						n\CurrSpeed = CurveValue(n\Speed, n\CurrSpeed, 10.0)
						MoveEntity n\Collider, 0,0,n\CurrSpeed*FPSfactor
						
						;Animate2(n\obj, AnimTime(n\obj), 684, 647, -n\CurrSpeed*25)
						
						If Rand(100)=1 Then n\Angle = Rnd(-20,20)
						
						n\State2 = n\State2 + FPSfactor
						If n\State2>250 Then 
							AnimateNPC(n, 684, 647, -n\CurrSpeed*25, False)
							;Animate2(n\obj, AnimTime(n\obj), 684, 647, -n\CurrSpeed*25, False)
							If n\Frame=647 Then 
								n\State = 0
								n\State2=0
							EndIf
						Else
							AnimateNPC(n, 684, 647, -n\CurrSpeed*25)
							
							;Animate2(n\obj, AnimTime(n\obj), 684, 647, -n\CurrSpeed*25)
						EndIf
						
				End Select
				
				If n\State > 1 Then
					If n\Sound = 0 Then n\Sound = LoadSound_Strict("SFX\SCP\066\Rolling.ogg")
					If n\SoundChn<>0 Then
						If ChannelPlaying(n\SoundChn) Then
							n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, n\Collider, 20)
						EndIf
					Else
						n\SoundChn = PlaySound2(n\Sound, Camera, n\Collider, 20)
					EndIf					
				EndIf
				
				;If n\SoundChn2<>0 Then
				;	If ChannelPlaying(n\SoundChn2) Then
				;		n\SoundChn2 = LoopSound2(n\Sound2, n\SoundChn2, Camera, n\Collider, 20)
				;		BlurTimer = Max((5.0-dist)*300,0)
				;	EndIf
				;EndIf
				
				
				If n\State3 > 0 Then
					n\State3 = n\State3-FPSfactor
					LightVolume = TempLightVolume-TempLightVolume*Min(Max(n\State3/500,0.01),0.6)
					HeartBeatRate = Max(HeartBeatRate, 130)
					HeartBeatVolume = Max(HeartBeatVolume,Min(n\State3/1000,1.0))
				EndIf
				
				If ChannelPlaying(n\SoundChn2)
					UpdateSoundOrigin2(n\SoundChn2,Camera,n\Collider,20)
					BlurTimer = Max((5.0-dist)*300,0)
				EndIf
				
				PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.2, EntityZ(n\Collider))
				
				RotateEntity n\obj, EntityPitch(n\Collider)-90, EntityYaw(n\Collider), 0
				;[End Block]
			Case NPCtype966
				;[Block]
				dist = EntityDistance(n\Collider,Collider)
				
				If (dist<HideDistance) Then
					
					;n\state = the "general" state (idle/wander/attack/echo etc)
					;n\state2 = timer for doing raycasts
					
					prevFrame = n\Frame
					
					If n\Sound > 0 Then
						temp = 0.5
						;the ambient sound gets louder when the npcs are attacking
						If n\State > 0 Then temp = 1.0	
						
						n\SoundChn = LoopSound2(n\Sound, n\SoundChn, Camera, Camera, 10.0,temp)
					EndIf
					
					temp = Rnd(-1.0,1.0)
					PositionEntity n\obj,EntityX(n\Collider,True),EntityY(n\Collider,True)-0.2,EntityZ(n\Collider,True)
					RotateEntity n\obj,-90.0,EntityYaw(n\Collider),0.0
					
					If (WearingNightVision=0) Then
						HideEntity n\obj
						If dist<1 And n\Reload <= 0 And MsgTimer <= 0 Then
							Select Rand(6)
								Case 1
									Msg="You feel something breathing right next to you."
								Case 2
									Msg=Chr(34)+"It feels like something's in this room with me."+Chr(34)
								Case 3
									Msg="You feel like something is here with you, but you do not see anything."
								Case 4
									Msg=Chr(34)+"Is my mind playing tricks on me or is there someone else here?"+Chr(34)
								Case 5
									Msg="You feel like something is following you."
								Case 6
									Msg="You can feel something near you, but you are unable to see it. Perhaps its time is now."
							End Select
                            n\Reload = 20*70
							MsgTimer=8*70
						EndIf
						n\Reload = n\Reload - FPSfactor
						
					Else
						ShowEntity n\obj
					EndIf
					
					If n\State3>5*70 Then
						;n\State = 1
						If n\State3<1000.0 Then
							For n2.NPCs = Each NPCs	
								If n2\NPCtype = n\NPCtype Then n2\State3=1000.0 
							Next
						EndIf
						
						n\State = Max(n\State,8)
						n\State3 = 1000.0					
						
					EndIf
					
					If Stamina<10 Then 
						n\State3=n\State3+FPSfactor
					Else If n\State3 < 900.0
						n\State3=Max(n\State3-FPSfactor*0.2,0.0)
					EndIf
					
					If n\State <> 10
						n\LastSeen = 0
					EndIf
					
					Select n\State
						Case 0 ;idle, standing
							;If n\Frame>2300.0 Then
							If n\Frame>556.0
								;AnimateNPC(n, 2391, 2416, 1.0, False)	
								;If n\Frame>2415.0 Then SetNPCFrame(n, 201)
								AnimateNPC(n, 628, 652, 0.25, False)
								If n\Frame>651.0 Then SetNPCFrame(n, 2)
							Else
								;AnimateNPC(n, 201, 1015, 1.0, False)
								AnimateNPC(n, 2, 214, 0.25, False)
								
								;echo/stare/walk around periodically
								;If n\Frame>1014.0 Then
								If n\Frame>213.0
									If Rand(3)=1 And dist<4 Then
										n\State = Rand(1,4)
									Else
										n\State = Rand(5,6)								
									EndIf
								EndIf
								
								;echo if player gets close
								If dist<2.0 Then 
									n\State=Rand(1,4)
								EndIf 							
							EndIf
							
							n\CurrSpeed = CurveValue(0.0, n\CurrSpeed, 10.0)
							
							MoveEntity n\Collider,0,0,n\CurrSpeed
							
						Case 1,2 ;echo
;							If n\State=1 Then
;								AnimateNPC(n, 1015, 1180, 1.0, False)
;								If n\Frame > 1179.0 Then n\State = 0
;							Else
;								AnimateNPC(n, 1180, 1379, 1.0, False)
;								If n\Frame > 1378.0 Then n\State = 0
;							EndIf
							AnimateNPC(n, 214, 257, 0.25, False)
							If n\Frame > 256.0 Then n\State = 0
							
							;If n\Frame>1029.0 And prevFrame<=1029.0 Or n\Frame>1203.0 And prevFrame<=1203.0 Then
							If n\Frame>228.0 And prevFrame<=228.0
								PlaySound2(LoadTempSound("SFX\SCP\966\Echo"+Rand(1,3)+".ogg"), Camera, n\Collider)
							EndIf
							
							angle = VectorYaw(EntityX(Collider)-EntityX(n\Collider),0,EntityZ(Collider)-EntityZ(n\Collider))
							RotateEntity n\Collider,0.0,CurveAngle(angle,EntityYaw(n\Collider),20.0),0.0
							
							If n\State3<900 Then
								BlurTimer = ((Sin(MilliSecs2()/50)+1.0)*200)/dist
								
								If (WearingNightVision>0) Then GiveAchievement(Achv966)
								
								If (Not Wearing714) And (WearingGasMask<3) And (WearingHazmat<3) And dist<16 Then
									BlinkEffect = Max(BlinkEffect, 1.5)
									BlinkEffectTimer = 1000
									
									StaminaEffect = 2.0
									StaminaEffectTimer = 1000
									
									If MsgTimer<=0 And StaminaEffect<1.5 Then
										Select Rand(4)
											Case 1
												Msg = "You feel exhausted."
											Case 2
												Msg = Chr(34)+"Could really go for a nap now..."+Chr(34)
											Case 3
												Msg = Chr(34)+"If I wasn't in this situation I would take a nap somewhere."+Chr(34)
											Case 4
												Msg = "You feel restless."
										End Select
										
										MsgTimer = 7*70
									EndIf
								EndIf							
							EndIf
							
						Case 3,4 ;stare at player
;							If n\State=3 Then
;								AnimateNPC(n, 1379.0, 1692.0, 1.0, False)
;								
;								If n\Frame>1691.0 Then n\State = 0
;							Else
;								AnimateNPC(n, 1692.0, 2156.0, 1.0, False)
;								
;								If n\Frame>2155.0 Then n\State = 0
;							EndIf
							If n\State=3
								AnimateNPC(n, 257, 332, 0.25, False)
								If n\Frame > 331.0 Then n\State = 0
							Else
								AnimateNPC(n, 332, 457, 0.25, False)
								If n\Frame > 456.0 Then n\State = 0
							EndIf
							
							;If n\Frame>1393.0 And prevFrame<=1393.0 Or n\Frame>1589.0 And prevFrame<=1589.0 Or n\Frame>2000.0 And prevFrame<=2000.0 Then
							If n\Frame>271.0 And prevFrame<=271.0 Or n\Frame>354 Or n\Frame>314.0 And prevFrame<=314.0 Or n\Frame>301.0 And prevFrame<=301.0
								PlaySound2(LoadTempSound("SFX\SCP\966\Idle"+Rand(1,3)+".ogg"), Camera, n\Collider)
							EndIf
							
							angle = VectorYaw(EntityX(Collider)-EntityX(n\Collider),0,EntityZ(Collider)-EntityZ(n\Collider))
							RotateEntity n\Collider,0.0,CurveAngle(angle,EntityYaw(n\Collider),20.0),0.0
						Case 5,6,8 ;walking or chasing
							;If n\Frame<2343.0 Then
							If n\Frame<580.0 And n\Frame>214.0 ;start walking
								;AnimateNPC(n, 2319, 2343, 0.5, False)
								AnimateNPC(n, 556, 580, 0.25, False)
							Else
								;AnimateNPC(n, 2343, 2391, n\CurrSpeed*25.0)
								If n\CurrSpeed >= 0.005 Then
									AnimateNPC(n, 580, 628, n\CurrSpeed*25.0)
								Else
									AnimateNPC(n, 2, 214, 0.25)
								EndIf
								
								;chasing the player
								If n\State = 8 And dist<32 Then
									If n\PathTimer <= 0 Then
										n\PathStatus = FindPath (n, EntityX(Collider,True), EntityY(Collider,True), EntityZ(Collider,True))
										n\PathTimer = 40*10
										n\CurrSpeed = 0
									EndIf
									n\PathTimer = Max(n\PathTimer-FPSfactor,0)
									
									If (Not EntityVisible(n\Collider,Collider)) Then
										If n\PathStatus = 2 Then
											n\CurrSpeed = 0
											;SetNPCFrame(n,201)
											SetNPCFrame(n,2)
										ElseIf n\PathStatus = 1
											If n\Path[n\PathLocation]=Null Then 
												If n\PathLocation > 19 Then 
													n\PathLocation = 0 : n\PathStatus = 0
												Else
													n\PathLocation = n\PathLocation + 1
												EndIf
											Else
												n\Angle = VectorYaw(EntityX(n\Path[n\PathLocation]\obj,True)-EntityX(n\Collider),0,EntityZ(n\Path[n\PathLocation]\obj,True)-EntityZ(n\Collider))
												;RotateEntity n\Collider,0.0,CurveAngle(angle,EntityYaw(n\Collider),10.0),0.0
												
												dist2 = EntityDistance(n\Collider,n\Path[n\PathLocation]\obj)
												
												temp = True
												If dist2 < 0.8 Then 
													If n\Path[n\PathLocation]\door<>Null Then
														If (Not n\Path[n\PathLocation]\door\IsElevatorDoor)
															If (n\Path[n\PathLocation]\door\locked Or n\Path[n\PathLocation]\door\KeyCard<>0 Or n\Path[n\PathLocation]\door\Code<>"") And (Not n\Path[n\PathLocation]\door\open) Then
																temp = False
															Else
																If n\Path[n\PathLocation]\door\open = False And (n\Path[n\PathLocation]\door\buttons[0]<>0 Or n\Path[n\PathLocation]\door\buttons[1]<>0) Then
																	UseDoor(n\Path[n\PathLocation]\door, False)
																EndIf
															EndIf
														EndIf
													EndIf
													If dist2 < 0.3 Then n\PathLocation = n\PathLocation + 1
												EndIf
												
												If temp = False Then
													n\PathStatus = 0
													n\PathLocation = 0
													n\PathTimer = 40*10
												EndIf
											EndIf
											
											n\CurrSpeed = CurveValue(n\Speed,n\CurrSpeed,10.0)
										ElseIf n\PathStatus = 0
											n\CurrSpeed = CurveValue(0,n\CurrSpeed,10.0)
										EndIf
									Else
										n\Angle = VectorYaw(EntityX(Collider)-EntityX(n\Collider),0,EntityZ(Collider)-EntityZ(n\Collider))
										n\CurrSpeed = CurveValue(n\Speed,n\CurrSpeed,10.0)
										
										If dist<1.0 Then n\State=10
										
									EndIf
								Else
									If MilliSecs2() > n\State2 And dist<16.0 Then
										HideEntity n\Collider
										EntityPick(n\Collider, 1.5)
										If PickedEntity() <> 0 Then
											n\Angle = EntityYaw(n\Collider)+Rnd(80,110)
										EndIf
										ShowEntity n\Collider
										
										n\State2=MilliSecs2()+1000
										
										If Rand(5)=1 Then n\State=0
									EndIf	
									
									n\CurrSpeed = CurveValue(n\Speed*0.5, n\CurrSpeed, 20.0)
									
								EndIf
								
								If (prevFrame < 604 And n\Frame=>604) Or (prevFrame < 627 And n\Frame=>627) Then
                                    PlaySound2(StepSFX(4,0,Rand(0,3)),Camera, n\Collider, 7.0, Rnd(0.5,0.7))
                                EndIf
								
								RotateEntity n\Collider, 0, CurveAngle(n\Angle,EntityYaw(n\Collider),10.0),0
								
								MoveEntity n\Collider,0,0,n\CurrSpeed*FPSfactor
							EndIf
						Case 10 ;attack
							If n\LastSeen=0
								PlaySound2(LoadTempSound("SFX\SCP\966\Echo"+Rand(1,3)+".ogg"), Camera, n\Collider)
								n\LastSeen = 1
							EndIf
							
							;If n\Frame>2300.0 Then
							If n\Frame>557.0
								;AnimateNPC(n, 2391, 2416, 1.0, False
								AnimateNPC(n, 628, 652, 0.25, False)
								;If n\Frame>2415.0 Then
								If n\Frame>651.0
									Select Rand(3)
										Case 1
											;SetNPCFrame(n, 2160)
											SetNPCFrame(n, 458)
										Case 2
											;SetNPCFrame(n, 2192)
											SetNPCFrame(n, 488)
										Case 3
											;SetNPCFrame(n, 2221)
											SetNPCFrame(n, 518)
									End Select
									
								EndIf
							Else
;								If n\Frame <= 2191 Then
;									AnimateNPC(n, 2160, 2191, 0.3, False)
;									If n\Frame > 2190 Then n\State = 8
;								ElseIf n\Frame <= 2220
;									AnimateNPC(n, 2192, 2220, 0.3, False)
;									If n\Frame > 2219 Then n\State = 8
;								ElseIf n\Frame <= 2260
;									AnimateNPC(n, 2221, 2260, 0.3, False)
;									If n\Frame > 2259 Then n\State = 8
;								EndIf
								
								If n\Frame <= 487
									AnimateNPC(n, 458, 487, 0.3, False)
									If n\Frame > 486.0 Then n\State = 8
								ElseIf n\Frame <= 517
									AnimateNPC(n, 488, 517, 0.3, False)
									If n\Frame > 516.0 Then n\State = 8
								ElseIf n\Frame <= 557
									AnimateNPC(n, 518, 557, 0.3, False)
									If n\Frame > 556.0 Then n\State = 8
								EndIf
							EndIf
							
							If dist<1.0 Then
								;If n\Frame>2173.0 And prevFrame<=2173.0 Or n\Frame>2203.0 And prevFrame<=2203.0 Or n\Frame>2227.0 And prevFrame<=2227.0 Then
								If n\Frame>470.0 And prevFrame<=470.0 Or n\Frame>500.0 And prevFrame<=500.0 Or n\Frame>527.0 And prevFrame<=527.0
									PlaySound2(LoadTempSound("SFX\General\Slash"+Rand(1,2)+".ogg"), Camera, n\Collider)
									Injuries = Injuries + Rnd(0.5,1.0)								
								EndIf	
							EndIf
							
							n\Angle = VectorYaw(EntityX(Collider)-EntityX(n\Collider),0,EntityZ(Collider)-EntityZ(n\Collider))
							RotateEntity n\Collider, 0, CurveAngle(n\Angle,EntityYaw(n\Collider),30.0),0
							
					End Select
				Else
					HideEntity n\obj
					If (Rand(600)=1) Then
						TeleportCloser(n)
					EndIf
				EndIf
				
				;[End Block]
			Case NPCtype1048a
				;[Block]
				Select n\State	
						
					Case 1
						n\PathStatus = FindPath(n, n\EnemyX,n\EnemyY+0.1,n\EnemyZ)
						;649, 677
				End Select
				;[End block]
			Case NPCtype1499
				;[Block]
				;n\State: Current State of the NPC
				;n\State2: A second state variable (dependend on the current NPC's n\State)
				;n\State3: Determines if the NPC will always be aggressive against the player
				;n\PrevState: Determines the type/behaviour of the NPC
				;	0 = Normal / Citizen
				;	1 = Stair guard / Guard next to king
				;	2 = King
				;	3 = Front guard
				
				prevFrame# = n\Frame
				
				If (Not n\Idle) And EntityDistance(n\Collider,Collider)<HideDistance*3 Then
					If n\PrevState = 0 Then
						If n\State = 0 Or n\State = 2 Then
							For n2.NPCs = Each NPCs
								If n2\NPCtype = n\NPCtype And n2 <> n Then
									If n2\State <> 0 And n2\State <> 2 Then
										If n2\PrevState = 0 Then
											n\State = 1
											n\State2 = 0
											Exit
										EndIf
									EndIf
								EndIf
							Next
						EndIf
					EndIf
					
					Select n\State
						Case 0
							;[Block]
							If n\PrevState=0 Then
								If n\CurrSpeed = 0.0 Then
									If n\Reload=0 Then
										If n\State2 < 500.0*Rnd(1,3) Then
											n\CurrSpeed = 0.0
											n\State2 = n\State2 + FPSfactor
										Else
											If n\CurrSpeed = 0.0 Then n\CurrSpeed = n\CurrSpeed + 0.0001
										EndIf
									Else
										If n\State2 < 1500 Then
											n\CurrSpeed = 0.0
											n\State2 = n\State2 + FPSfactor
										Else
											If n\Target <> Null Then
												If n\Target\Target <> Null Then
													n\Target\Target = Null
												EndIf
												n\Target\Reload = 0
												n\Target\Angle = n\Target\Angle+Rnd(-45,45)
												n\Target = Null
												n\Reload = 0
												n\Angle = n\Angle+Rnd(-45,45)
											EndIf
											If n\CurrSpeed = 0.0 Then n\CurrSpeed = n\CurrSpeed + 0.0001
										EndIf
									EndIf
								Else
									If n\Target<>Null Then
										n\State2 = 0.0
									EndIf
									
									If n\State2 < 10000.0*Rnd(1,3)
										n\CurrSpeed = CurveValue(n\Speed,n\CurrSpeed,10.0)
										n\State2 = n\State2 + FPSfactor
									Else
										n\CurrSpeed = CurveValue(0.0,n\CurrSpeed,50.0)
									EndIf
									
									RotateEntity n\Collider,0,CurveAngle(n\Angle,EntityYaw(n\Collider),10.0),0
									
									If n\Target=Null Then
										If Rand(200) = 1 Then n\Angle = n\Angle + Rnd(-45,45)
										
										HideEntity n\Collider
										EntityPick(n\Collider, 1.5)
										If PickedEntity() <> 0 Then
											n\Angle = EntityYaw(n\Collider)+Rnd(80,110)
										EndIf
										ShowEntity n\Collider
									Else
										n\Angle = EntityYaw(n\Collider) + DeltaYaw(n\Collider,n\Target\Collider)
										If EntityDistance(n\Collider,n\Target\Collider)<1.5 Then
											n\CurrSpeed = 0.0
											n\Target\CurrSpeed = 0.0
											n\Reload = 1
											n\Target\Reload = 1
											temp = Rand(0,2)
											If temp=0 Then
												SetNPCFrame(n,296)
											ElseIf temp=1 Then
												SetNPCFrame(n,856)
											Else
												SetNPCFrame(n,905)
											EndIf
											temp = Rand(0,2)
											If temp=0 Then
												SetNPCFrame(n\Target,296)
											ElseIf temp=1 Then
												SetNPCFrame(n\Target,856)
											Else
												SetNPCFrame(n\Target,905)
											EndIf
										EndIf
									EndIf
								EndIf
							Else
								RotateEntity n\Collider,0,CurveAngle(n\Angle,EntityYaw(n\Collider),10.0),0
							EndIf
							
							If n\CurrSpeed = 0.0
								If n\Reload = 0 And n\PrevState<>2 Then
									AnimateNPC(n,296,320,0.2)
								ElseIf n\Reload = 0 And n\PrevState=2 Then
									;509-533
									;534-601
									If n\Frame <= 532.5 Then
										AnimateNPC(n,509,533,0.2,False)
									ElseIf n\Frame > 533.5 And n\Frame <= 600.5 Then
										AnimateNPC(n,534,601,0.2,False)
									Else
										temp = Rand(0,1)
										If temp=0 Then
											SetNPCFrame(n,509)
										Else
											SetNPCFrame(n,534)
										EndIf
									EndIf
									;SetNPCFrame(n,855)
								Else
									If n\PrevState=2 Then
										AnimateNPC(n,713,855,0.2,False)
										If n\Frame > 833.5 Then
											PointEntity n\obj,Collider
											RotateEntity n\Collider,0,CurveAngle(n\Angle,EntityYaw(n\Collider),10.0),0
										EndIf
									ElseIf n\PrevState=1 Then
										AnimateNPC(n,602,712,0.2,False)
										If n\Frame > 711.5 Then
											n\Reload = 0
										EndIf
									Else
										If n\Frame <= 319.5 Then
											AnimateNPC(n,296,320,0.2,False)
										;856-904
										ElseIf n\Frame > 320.5 And  n\Frame < 903.5 Then
											AnimateNPC(n,856,904,0.2,False)
										;905-953
										ElseIf n\Frame > 904.5 And n\Frame < 952.5 Then
											AnimateNPC(n,905,953,0.2,False)
										Else
											temp = Rand(0,2)
											If temp=0 Then
												SetNPCFrame(n,296)
											ElseIf temp=1 Then
												SetNPCFrame(n,856)
											Else
												SetNPCFrame(n,905)
											EndIf
										EndIf
									EndIf
								EndIf
							Else
								If (n\ID Mod 2 = 0) Then
									AnimateNPC(n,1,62,(n\CurrSpeed*28))
								Else
									AnimateNPC(n,100,167,(n\CurrSpeed*28))
								EndIf
								If n\PrevState = 0 Then
									If n\Target = Null Then
										If Rand(1,1200)=1 Then
											For n2.NPCs = Each NPCs
												If n2<>n Then
													If n2\NPCtype=n\NPCtype Then
														If n2\Target = Null Then
															If n2\PrevState=0 Then
																If EntityDistance(n\Collider,n2\Collider)<20.0 Then
																	n\Target = n2
																	n2\Target = n
																	n\Target\CurrSpeed = n\Target\CurrSpeed + 0.0001
																	Exit
																EndIf
															EndIf
														EndIf
													EndIf
												EndIf
											Next
										EndIf
									EndIf
								EndIf
							EndIf
							
							;randomly play the "screaming animation" and revert back to state 0
							If n\Target=Null And n\PrevState=0 Then
								If (Rand(5000)=1) Then
									n\State = 2
									n\State2 = 0
									
									If Not ChannelPlaying(n\SoundChn) Then
										dist = EntityDistance(n\Collider,Collider)
										If (dist < 20.0) Then
											If n\Sound <> 0 Then FreeSound_Strict n\Sound : n\Sound = 0
											n\Sound = LoadSound_Strict("SFX\SCP\1499\Idle"+Rand(1,4)+".ogg")
											n\SoundChn = PlaySound2(n\Sound, Camera, n\Collider, 20.0)
										EndIf
									EndIf
								EndIf
								
								If (n\ID Mod 2 = 0) And (Not NoTarget) Then
									dist = EntityDistance(n\Collider,Collider)
									If dist < 10.0 Then
										If EntityVisible(n\Collider,Collider) Then
											;play the "screaming animation"
											n\State = 2
											If dist < 5.0 Then
												If n\Sound <> 0 Then FreeSound_Strict n\Sound : n\Sound = 0
												n\Sound = LoadSound_Strict("SFX\SCP\1499\Triggered.ogg")
												n\SoundChn = PlaySound2(n\Sound, Camera, n\Collider,20.0)
												
												n\State2 = 1 ;if player is too close, switch to attack after screaming
												
												For n2.NPCs = Each NPCs
													;If n2\NPCtype = n\NPCtype And n2 <> n And (n\ID Mod 2 = 0) Then
													If n2\NPCtype = n\NPCtype And n2 <> n
														If n2\PrevState = 0 Then
															n2\State = 1
															n2\State2 = 0
														EndIf
													EndIf
												Next
											Else
												n\State2 = 0 ;otherwise keep idling
											EndIf
											
											n\Frame = 203
										EndIf
									EndIf
								EndIf
							ElseIf n\PrevState=1 Then
								dist = EntityDistance(n\Collider,Collider)
								If (Not NoTarget) Then
									If dist < 4.0 Then
										If EntityVisible(n\Collider,Collider) Then
											If n\Sound <> 0 Then FreeSound_Strict n\Sound : n\Sound = 0
											n\Sound = LoadSound_Strict("SFX\SCP\1499\Triggered.ogg")
											n\SoundChn = PlaySound2(n\Sound, Camera, n\Collider,20.0)
											
											n\State = 1
											
											n\Frame = 203
										EndIf
									EndIf
								EndIf
							EndIf
							;[End Block]
						Case 1 ;attacking the player
							;[Block]
							If NoTarget Then n\State = 0
							
							If PlayerRoom\RoomTemplate\Name = "dimension1499" And n\PrevState=0 Then
								ShouldPlay = 19
							EndIf
							
							PointEntity n\obj,Collider
							RotateEntity n\Collider,0,CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),20.0),0
							
							dist = EntityDistance(n\Collider,Collider)
							
							If n\State2 = 0.0
								If n\PrevState=1 Then
									n\CurrSpeed = CurveValue(n\Speed*2.0,n\CurrSpeed,10.0)
									If n\Target<>Null Then
										n\Target\State = 1
									EndIf
								Else
									n\CurrSpeed = CurveValue(n\Speed*1.75,n\CurrSpeed,10.0)
								EndIf
								
								If (n\ID Mod 2 = 0) Then
									AnimateNPC(n,1,62,(n\CurrSpeed*28))
								Else
									AnimateNPC(n,100,167,(n\CurrSpeed*28))
								EndIf
							EndIf
							
							If dist < 0.75
								If (n\ID Mod 2 = 0) Or n\State3 = 1 Or n\PrevState=1 Or n\PrevState=3 Or n\PrevState=4 Then
									n\State2 = Rand(1,2)
									n\State = 3
									If n\State2 = 1
										SetNPCFrame(n,63)
									Else
										SetNPCFrame(n,168)
									EndIf
								Else
									n\State = 4
								EndIf
							EndIf
							;[End Block]
						Case 2 ;play the "screaming animation" and switch to n\state2 after it's finished
							;[Block]
							n\CurrSpeed = 0.0
							AnimateNPC(n,203,295,0.1,False)
							
							If n\Frame > 294.0 Then
								n\State = n\State2
							EndIf
							;[End Block]
						Case 3 ;slashing at the player
							;[Block]
							n\CurrSpeed = CurveValue(0.0,n\CurrSpeed,5.0)
							dist = EntityDistance(n\Collider,Collider)
							If n\State2 = 1
								AnimateNPC(n,63,100,0.6,False)
								If prevFrame < 89 And n\Frame=>89
									If dist > 0.85 Or Abs(DeltaYaw(n\Collider,Collider))>60.0
										;Miss
									Else
										Injuries = Injuries + Rnd(0.75,1.5)
										PlaySound2(LoadTempSound("SFX\General\Slash"+Rand(1,2)+".ogg"), Camera, n\Collider)
										If Injuries > 10.0
											Kill()
											If PlayerRoom\RoomTemplate\Name$ = "dimension1499"
												DeathMSG = "All personnel situated within Evacuation Shelter LC-2 during the breach have been administered "
												DeathMSG = DeathMSG + "Class-B amnestics due to Incident 1499-E. The Class D subject involved in the event "
												DeathMSG = DeathMSG + "died shortly after being shot by Agent [REDACTED]."
											Else
												DeathMSG = "An unidentified male and a deceased Class D subject were discovered in [REDACTED] by the Nine-Tailed Fox. "
												DeathMSG = DeathMSG + "The man was described as highly agitated and seemed to only speak Russian. "
												DeathMSG = DeathMSG + "He's been taken into a temporary holding area at [REDACTED] while waiting for a translator to arrive."
											EndIf
										EndIf
									EndIf
								ElseIf n\Frame => 99
									n\State2 = 0.0
									n\State = 1
								EndIf
							Else
								AnimateNPC(n,168,202,0.6,False)
								If prevFrame < 189 And n\Frame=>189
									If dist > 0.85 Or Abs(DeltaYaw(n\Collider,Collider))>60.0
										;Miss
									Else
										Injuries = Injuries + Rnd(0.75,1.5)
										PlaySound2(LoadTempSound("SFX\General\Slash"+Rand(1,2)+".ogg"), Camera, n\Collider)
										If Injuries > 10.0
											Kill()
											If PlayerRoom\RoomTemplate\Name$ = "dimension1499"
												DeathMSG = "All personnel situated within Evacuation Shelter LC-2 during the breach have been administered "
												DeathMSG = DeathMSG + "Class-B amnestics due to Incident 1499-E. The Class D subject involved in the event "
												DeathMSG = DeathMSG + "died shortly after being shot by Agent [REDACTED]."
											Else
												DeathMSG = "An unidentified male and a deceased Class D subject were discovered in [REDACTED] by the Nine-Tailed Fox. "
												DeathMSG = DeathMSG + "The man was described as highly agitated and seemed to only speak Russian. "
												DeathMSG = DeathMSG + "He's been taken into a temporary holding area at [REDACTED] while waiting for a translator to arrive."
											EndIf
										EndIf
									EndIf
								ElseIf n\Frame => 201
									n\State2 = 0.0
									n\State = 1
								EndIf
							EndIf
							;[End Block]
						Case 4 ;standing in front of the player
							;[Block]
							dist = EntityDistance(n\Collider,Collider)
							n\CurrSpeed = CurveValue(0.0,n\CurrSpeed,5.0)
							AnimateNPC(n,296,320,0.2)
							
							PointEntity n\obj,Collider
							RotateEntity n\Collider,0,CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),20.0),0
							
							If dist > 0.85
								n\State = 1
							EndIf
							;[End Block]
					End Select
					
					If n\SoundChn <> 0 And ChannelPlaying(n\SoundChn) Then
						UpdateSoundOrigin(n\SoundChn,Camera,n\Collider,20.0)
					EndIf
					
					MoveEntity n\Collider,0,0,n\CurrSpeed*FPSfactor
					
					RotateEntity n\obj,0,EntityYaw(n\Collider)-180,0
					PositionEntity n\obj,EntityX(n\Collider),EntityY(n\Collider)-0.2,EntityZ(n\Collider)
					
					ShowEntity n\obj
				Else
					HideEntity n\obj
				EndIf
				
				;[End Block]
			Case NPCtype008
				;[Block]
				;n\State: Main State
				;n\State2: A timer used for the player detection
				;n\State3: A timer for making the NPC idle (if the player escapes during that time)
				
				If (Not n\IsDead)
					If n\State = 0
						EntityType n\Collider,HIT_DEAD
					Else
						EntityType n\Collider,HIT_PLAYER
					EndIf
					
					prevFrame = n\Frame
					
					n\BlinkTimer = 1
					
					Select n\State
						Case 0 ;Lying next to the wall
							SetNPCFrame(n,11)
						Case 1 ;Standing up
							AnimateNPC(n,11,32,0.1,False)
							If n\Frame => 29
								n\State = 2
							EndIf
						Case 2 ;Being active
							PlayerSeeAble = MeNPCSeesPlayer(n)
							If PlayerSeeAble=1 Or n\State2 > 0.0
								If PlayerSeeAble=1
									n\State2 = 70*2
								Else
									n\State2 = Max(n\State2-FPSfactor,0)
								EndIf
								PointEntity n\obj, Collider
								RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 20.0), 0
								
								AnimateNPC(n, 64, 93, n\CurrSpeed*30)
								n\CurrSpeed = CurveValue(n\Speed*0.7, n\CurrSpeed, 20.0)
								MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
								
								If EntityDistance(n\Collider,Collider)<1.0
									If (Abs(DeltaYaw(n\Collider,Collider))<=60.0)
										n\State = 3
									EndIf
								EndIf
								
								n\PathTimer = 0
								n\PathStatus = 0
								n\PathLocation = 0
								n\State3 = 0
							Else
								If n\PathStatus = 1
									If n\Path[n\PathLocation]=Null Then 
										If n\PathLocation > 19 Then 
											n\PathLocation = 0 : n\PathStatus = 0
										Else
											n\PathLocation = n\PathLocation + 1
										EndIf
									Else
										PointEntity n\obj, n\Path[n\PathLocation]\obj
										RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj), EntityYaw(n\Collider), 20.0), 0
										
										AnimateNPC(n, 64, 93, n\CurrSpeed*30)
										n\CurrSpeed = CurveValue(n\Speed*0.7, n\CurrSpeed, 20.0)
										MoveEntity n\Collider, 0, 0, n\CurrSpeed * FPSfactor
										
										;opens doors in front of him
										dist2# = EntityDistance(n\Collider,n\Path[n\PathLocation]\obj)
										If dist2 < 0.6 Then
											temp = True
											If n\Path[n\PathLocation]\door <> Null Then
												If (Not n\Path[n\PathLocation]\door\IsElevatorDoor)
													If n\Path[n\PathLocation]\door\locked Or n\Path[n\PathLocation]\door\KeyCard>0 Or n\Path[n\PathLocation]\door\Code<>"" Then
														temp = False
													Else
														If n\Path[n\PathLocation]\door\open = False Then UseDoor(n\Path[n\PathLocation]\door, False)
													EndIf
												EndIf
											EndIf
											If dist2#<0.2 And temp
												n\PathLocation = n\PathLocation + 1
											ElseIf dist2#<0.5 And (Not temp)
												n\PathStatus = 0
												n\PathTimer# = 0.0
											EndIf
										EndIf
										
										;If Rand(100)=3
										;	n\PathStatus = FindPath(n,EntityX(Collider),EntityY(Collider),EntityZ(Collider))
										;EndIf
									EndIf
								Else
									AnimateNPC(n, 323, 344, 0.2, True)
									n\CurrSpeed = 0
									If n\PathTimer < 70*5
										n\PathTimer = n\PathTimer + Rnd(1,2+(2*SelectedDifficulty\aggressiveNPCs))*FPSfactor
									Else
										n\PathStatus = FindPath(n,EntityX(Collider),EntityY(Collider),EntityZ(Collider))
										n\PathTimer = 0
									EndIf
								EndIf
								
								If EntityDistance(n\Collider,Collider)>HideDistance
									If n\State3 < 70*(15+(10*SelectedDifficulty\aggressiveNPCs))
										n\State3 = n\State3+FPSfactor
									Else
										DebugLog "SCP-008-1 IDLE"
										n\State3 = 70*(6*60)
										n\State = 4
									EndIf
								EndIf
							EndIf
							
							If n\CurrSpeed > 0.005 Then
								If (prevFrame < 80 And n\Frame=>80) Or (prevFrame > 92 And n\Frame<65)
									PlaySound2(StepSFX(0,0,Rand(0,7)),Camera, n\Collider, 8.0, Rnd(0.3,0.5))
								EndIf
							EndIf
							
							n\SoundChn = LoopSound2(n\Sound,n\SoundChn,Camera,n\Collider)
						Case 3 ;Attacking
							AnimateNPC(n, 126, 165, 0.4, False)
							If (n\Frame => 146 And prevFrame < 146)
								If EntityDistance(n\Collider,Collider)<1.1
									If (Abs(DeltaYaw(n\Collider,Collider))<=60.0)
										PlaySound_Strict DamageSFX(Rand(5,8))
										Injuries = Injuries+Rnd(0.4,1.0)
										Infect = Infect + (1+(1*SelectedDifficulty\aggressiveNPCs))
										DeathMSG = "Subject D-9341. Cause of death: multiple lacerations and severe blunt force trauma caused by [DATA EXPUNGED], who was infected with SCP-008. Said subject was located by Nine-Tailed Fox and terminated."
									EndIf
								EndIf
							ElseIf n\Frame => 164
								If EntityDistance(n\Collider,Collider)<1.1
									If (Abs(DeltaYaw(n\Collider,Collider))<=60.0)
										SetNPCFrame(n,126)
									Else
										n\State = 2
									EndIf
								Else
									n\State = 2
								EndIf
							EndIf
						Case 4 ;Idling
							HideEntity n\obj
							HideEntity n\Collider
							n\DropSpeed = 0
							PositionEntity n\Collider,0,500,0
							ResetEntity n\Collider
							If n\Idle > 0
								n\Idle = Max(n\Idle-(1+(1*SelectedDifficulty\aggressiveNPCs))*FPSfactor,0)
							Else
								If PlayerInReachableRoom() ;Player is in a room where SCP-008-1 can teleport to
									If Rand(50-(20*SelectedDifficulty\aggressiveNPCs))=1
										ShowEntity n\Collider
										ShowEntity n\obj
										For w.waypoints = Each WayPoints
											If w\door=Null And w\room\dist < HideDistance And Rand(3)=1 Then
												If EntityDistance(w\room\obj,n\Collider)<EntityDistance(Collider,n\Collider)
													x = Abs(EntityX(n\Collider)-EntityX(w\obj,True))
													If x < 12.0 And x > 4.0 Then
														z = Abs(EntityZ(n\Collider)-EntityZ(w\obj,True))
														If z < 12 And z > 4.0 Then
															If w\room\dist > 4
																DebugLog "MOVING 008-1 TO "+w\room\roomtemplate\name
																PositionEntity n\Collider, EntityX(w\obj,True), EntityY(w\obj,True)+0.25,EntityZ(w\obj,True)
																ResetEntity n\Collider
																n\PathStatus = 0
																n\PathTimer# = 0.0
																n\PathLocation = 0
																Exit
															EndIf
														EndIf
													EndIf
												EndIf
											EndIf
										Next
										n\State = 2
										n\State3 = 0
									EndIf
								EndIf
							EndIf
					End Select
				Else
					If n\SoundChn <> 0
						StopChannel n\SoundChn
						n\SoundChn = 0
						FreeSound_Strict n\Sound
						n\Sound = 0
					EndIf
					AnimateNPC(n, 344, 363, 0.5, False)
				EndIf
				
				RotateEntity n\obj,0,EntityYaw(n\Collider)-180,0
				PositionEntity n\obj,EntityX(n\Collider),EntityY(n\Collider)-0.2,EntityZ(n\Collider)
				;[End Block]
		End Select
		
		If n\IsDead
			EntityType n\Collider,HIT_DEAD
		EndIf
		
		Local gravityDist = Distance(EntityX(Collider),EntityZ(Collider),EntityX(n\Collider),EntityZ(n\Collider))
		
		If gravityDist<HideDistance*0.7 Or n\NPCtype = NPCtype1499 Then
			If n\InFacility = InFacility
				TranslateEntity n\Collider, 0, n\DropSpeed, 0
				
				Local CollidedFloor% = False
				For i% = 1 To CountCollisions(n\Collider)
					If CollisionY(n\Collider, i) < EntityY(n\Collider) - 0.01 Then CollidedFloor = True : Exit
				Next
				
				If CollidedFloor = True Then
					n\DropSpeed# = 0
				Else
					If ShouldEntitiesFall
;						If n\FallingPickDistance>0
;							Local pick = LinePick(EntityX(n\Collider),EntityY(n\Collider),EntityZ(n\Collider),0,-n\FallingPickDistance,0)
;							If pick
;								n\DropSpeed# = Max(n\DropSpeed - 0.005*FPSfactor*n\GravityMult,-n\MaxGravity)
;							Else
;								n\DropSpeed# = 0
;							EndIf
;						Else
;							n\DropSpeed# = Max(n\DropSpeed - 0.005*FPSfactor*n\GravityMult,-n\MaxGravity)
;						EndIf
						Local UpdateGravity% = False
						Local MaxX#,MinX#,MaxZ#,MinZ#
						If n\InFacility=1
							If PlayerRoom\RoomTemplate\Name$ <> "173"
								For e.Events = Each Events
									If e\EventName = "room860"
										If e\EventState = 1.0
											UpdateGravity = True
											Exit
										EndIf
									EndIf
								Next
							Else
								UpdateGravity = True
							EndIf
							If (Not UpdateGravity)
								For r.Rooms = Each Rooms
									If r\MaxX<>0 Or r\MinX<>0 Or r\MaxZ<>0 Or r\MinZ<>0
										MaxX# = r\MaxX
										MinX# = r\MinX
										MaxZ# = r\MaxZ
										MinZ# = r\MinZ
									Else
										MaxX# = 4.0
										MinX# = 0.0
										MaxZ# = 4.0
										MinZ# = 0.0
									EndIf
									If Abs(EntityX(n\Collider)-EntityX(r\obj))<=Abs(MaxX-MinX)
										If Abs(EntityZ(n\Collider)-EntityZ(r\obj))<=Abs(MaxZ-MinZ)
											If r=PlayerRoom
												UpdateGravity = True
												Exit
											EndIf
											If IsRoomAdjacent(PlayerRoom,r)
												UpdateGravity = True
												Exit
											EndIf
											For i=0 To 3
												If (IsRoomAdjacent(PlayerRoom\Adjacent[i],r))
													UpdateGravity = True
													Exit
												EndIf
											Next
										EndIf
									EndIf
								Next
							EndIf
						Else
							UpdateGravity = True
						EndIf
						If UpdateGravity
							n\DropSpeed# = Max(n\DropSpeed - 0.005*FPSfactor*n\GravityMult,-n\MaxGravity)
						Else
							If n\FallingPickDistance>0
								n\DropSpeed = 0.0
							Else
								n\DropSpeed# = Max(n\DropSpeed - 0.005*FPSfactor*n\GravityMult,-n\MaxGravity)
							EndIf
						EndIf
					Else
						n\DropSpeed# = 0.0
					EndIf
				EndIf
			Else
				n\DropSpeed = 0
			EndIf
		Else
			n\DropSpeed = 0
		EndIf
		
		CatchErrors(Chr(34)+n\NVName+Chr(34)+" NPC")
		
	Next
	
	If MTF_CameraCheckTimer>0.0 And MTF_CameraCheckTimer<70*90
		MTF_CameraCheckTimer=MTF_CameraCheckTimer+FPSfactor
	ElseIf MTF_CameraCheckTimer>=70*90
		MTF_CameraCheckTimer=0.0
		If (Not PlayerDetected)
			If MTF_CameraCheckDetected
				PlayAnnouncement("SFX\Character\MTF\AnnouncCameraFound"+Rand(1,2)+".ogg")
				PlayerDetected=True
				MTF_CameraCheckTimer=70*60
			Else
				PlayAnnouncement("SFX\Character\MTF\AnnouncCameraNoFound.ogg")
			EndIf
		EndIf
		MTF_CameraCheckDetected=False
		If MTF_CameraCheckTimer=0.0
			PlayerDetected=False
		EndIf
	EndIf
	
End Function


Function RecordDemo()

	If Not demoFile Then
		
		Local time$ = CurrentTime()
		;We have to replace here because windows does not allow : to be in folder names.
		time = Replace(time, ":", ".")
		
		Local date$ = CurrentDate()
		
		demoSavePath$ = DemoPath + date + " " + time
				
		CreateDir(demoSavePath)
				
		If FileType(demoSavePath) = 2 Then
		
			demoFile = WriteFile(demoSavePath + "\demo.txt")
			
		Else
		
			CreateConsoleMsg("Unable to write to file. Parent folder was not created.")
			CreateConsoleMsg("Stopping recording.")
			recordingDemo = False
			demoFile = 0
			
		EndIf
		
		WriteString(demoFile, RandomSeed)
		WriteByte(demoFile, SeedRNGDirectly)
		
		Local doorCount% = 0
		Local amongusDoor.Doors
		For amongusDoors.Doors = Each Doors
			doorCount = doorCount + 1			
		Next
		
		;We have to do this loop twice because I need the amount of doors on the map
		;so that I can re-dim the PrevDoorOpen array to the correct size.
		Dim PrevDoorOpen(doorCount)
		Local index% = 0
		For amongus.Doors = Each Doors
	
			PrevDoorOpen(index) = amongus\open
			index = index + 1
			
		Next
		
		WriteByte(demoFile, doorCount)		
		
		demoDelayTime = MilliSecs()
		
		;We do not close the demoPath file until we manually stop the demo.
				
	Else		
		
		If (MilliSecs() - demoDelayTime >= 60) Then ; Arbitrary amount of time (60 ms) to delay saving the user input and what not.		
		
			;WriteLine(demoFile, CurrentTime())
			
			WriteInt(demoFile, DemoTick)
			
			WriteInt(demoFile, GameTime)
			
			WriteFloat(demoFile, EntityX(Collider))
			WriteFloat(demoFile, EntityY(Collider))
			WriteFloat(demoFile, EntityZ(Collider))
			
			WriteFloat(demoFile, EntityX(Head))
			WriteFloat(demoFile, EntityY(Head))
			WriteFloat(demoFile, EntityZ(Head))	
			
			WriteFloat(demoFile, EntityPitch(Camera))
			WriteFloat(demoFile, EntityYaw(Collider))
			WriteFloat(demoFile, EntityRoll(Camera))		
			
			WriteFloat(demoFile, Stamina)
			
			WriteFloat(demoFile, BlinkTimer)
			
			;[Doors]
						
			Local reachedEnd% = True
			index% = 0 
			For d.Doors = Each Doors
			
				If d\open <> PrevDoorOpen(index) Then
					;This byte will signal to ReadDemo() that it must read another 3 floats.					
					WriteByte(demoFile, 1)
					WriteFloat(demoFile, EntityX(d\frameobj))
					WriteFloat(demoFile, EntityY(d\frameobj))
					WriteFloat(demoFile, EntityZ(d\frameobj))
					reachedEnd = False
					PrevDoorOpen(index) = d\open
					Exit
				EndIf
				index = index + 1				
			
			Next
			
			; If this is true it means that no door on the map had its open state changed
			; since the last time we logged its state.					
			; We will write a 0 to the file so that ReadDemo() knows that it does not have to 
			; read anothe 3 floats.
			If reachedEnd Then
				WriteByte(demoFile, 0)
			EndIf
									
			;[End Doors]
			
			; I will be re-using this a lot probably.
			reachedEnd = True
			
			;[NPCS]
			
			If Curr173\DemoPrevX <> EntityX(Curr173\Collider) Then
			
				Local nx# = EntityX(Curr173\Collider)
				Local ny# = EntityY(Curr173\Collider)
				Local nz# = EntityZ(Curr173\Collider)
			
				WriteByte(demoFile, 1)
				WriteFloat(demoFile, nx)
				WriteFloat(demoFile, ny)
				WriteFloat(demoFile, nz)
				
				Curr173\DemoPrevX = nx
				Curr173\DemoPrevY = ny
				Curr173\DemoPrevZ = nz

				
			Else
				
				WriteByte(demoFile, 0)
				
			EndIf

			
			;For n.NPCs = Each NPCs
			;	Select n\NPCtype
			;		Case NPCtype173
			;			If n\PrevX <> EntityX(n\Collider) Then
			;			
			;				WriteByte(demoFile, 1)
			;				WriteFloat(demoFile, EntityX(n\Collider))
			;				WriteFloat(demoFile, EntityY(n\Collider))
			;				WriteFloat(demoFile, EntityZ(n\Collider))
			;				reachedEnd = False
			;			
			;			EndIf
			;			
			;			If reachedEnd Then
			;				WriteByte(demoFile, 0)
			;			EndIf
			;			
			;			;Temoporary, I am only working with 173 right now.
			;			Exit
			;				
			;	End Select
			;Next
			
			;[End NPCS]
									
									
																
			demoDelayTime = MilliSecs()
		
			DemoTick = DemoTick + 1
		
		Else		
					
		EndIf
		
	EndIf
		
End Function

Function StopRecordingDemo()

	If demoFile Then
		CloseFile(demoFile)
		demoFile = 0
		
		CreateConsoleMsg("Demo saved to: " + demoSavePath + " Total Ticks: " + DemoTick)
		
		DemoTick = 0
		
	EndIf

End Function

Function LoadSavedDemos()

	CatchErrors("Uncaught (LoadSavedDemos)")
	SavedDemosAmount = 0
	If FileType(DemoPath)=1 Then RuntimeError "Can't create dir "+Chr(34)+DemoPath+Chr(34)
	If FileType(DemoPath)=0 Then CreateDir(DemoPath)
	myDir=ReadDir(DemoPath) 
	Repeat 
		file$=NextFile$(myDir) 
		If file$="" Then Exit 
		If FileType(DemoPath+"\"+file$) = 2 Then 
			If file <> "." And file <> ".." Then 
				If (FileType(DemoPath + file + "\demo.txt")>0) Then
					SavedDemosAmount = SavedDemosAmount + 1
				EndIf
			EndIf
		End If 
	Forever 
	CloseDir myDir 
	
	Dim SavedDemos$(SavedDemosAmount+1) 
	
	myDir=ReadDir(DemoPath) 
	i = 0
	Repeat 
		file$=NextFile$(myDir) 
		If file$="" Then Exit 
		If FileType(DemoPath+"\"+file$) = 2 Then 
			If file <> "." And file <> ".." Then 
				If (FileType(DemoPath + file + "\demo.txt")>0) Then
					SavedDemos(i) = file
					i=i+1
				EndIf
			EndIf
		End If 
	Forever 
	CloseDir myDir 
	
	;Dim SaveGameTime$(SaveGameAmount + 1)
	;Dim SaveGameDate$(SaveGameAmount + 1)
	;Dim SaveGameVersion$(SaveGameAmount + 1)
	;For i = 1 To SaveGameAmount
	;	DebugLog (SavePath + SaveGames(i - 1) + "\save.txt")
	;	Local f% = ReadFile(SavePath + SaveGames(i - 1) + "\save.txt")
	;	SaveGameTime(i - 1) = ReadString(f)
	;	SaveGameDate(i - 1) = ReadString(f)
	;	;Skip all data until the CompatibleVersion number
	;	ReadInt(f)
	;	For j = 0 To 5
	;		ReadFloat(f)
	;	Next
	;	ReadString(f)
	;	ReadFloat(f)
	;	ReadFloat(f)
	;	;End Skip
	;	SaveGameVersion(i - 1) = ReadString(f)
	;	
	;	CloseFile f
	;Next
	
	CatchErrors("LoadSavedDemos")

End Function

Function ReadDemo(path$)

	If Not demoFile Then
		demoFile = ReadFile(path)
		
		If Not demoFile Then
			CreateConsoleMsg("Could not open file: " + path )
			ConsoleOpen = True
		EndIf
		
		;Here we will read all the stuff we need before we start reading game state.
		RandomSeed$ = ReadString(demoFile)
		SeedDemoDirectly% = ReadByte(demoFile)				
		
		DemoDoorCount% = ReadByte(demoFile)	
		
		; This is a small optimization. The door count for any map is usually bigger than 255.
		; Normally, we would save it as an int, but we can save it into a byte.
		; We can pick an arbitrarily high number (100 in this case). There are usually no more than 280 doors per map.
		; When we read the door count value, we add 256 to it to get the true number of doors on the map.
		If DemoDoorCount < 100 Then 
			DemoDoorCount = DemoDoorCount + 256
		EndIf
		
		While Not(Eof(demoFile))
	
			Local d.Demos = New Demos
		
			d\tick = ReadInt(demoFile)
		
			d\gt = ReadInt(demoFile)
			
			d\px = ReadFloat(demoFile)
			d\py = ReadFloat(demoFile)
			d\pz = ReadFloat(demoFile)
			
			d\hx = ReadFloat(demoFile)
			d\hy = ReadFloat(demoFile)
			d\hz = ReadFloat(demoFile)
			
			d\pitch = ReadFloat(demoFile)
			d\yaw   = ReadFloat(demoFile)
			d\roll  = ReadFloat(demoFile)
	
			d\stamina = ReadFloat(demoFile)
			
			d\blink = ReadFloat(demoFile)
	
			;[Doors]
	
			Local shouldReadDoorPosition% = ReadByte(demoFile)
			
			d\readDoorPosition = shouldReadDoorPosition
			
			If d\readDoorPosition Then	
				Local x# = ReadFloat(demoFile)
				Local y# = ReadFloat(demoFile)
				Local z$ = ReadFloat(demoFile)
				
				d\doorx = x
				d\doory = y
				d\doorz = z			
			EndIf					
	
			;[End Doors]
			
			;[NPCs]
			
			Local readNut% = ReadByte(demoFile)
			
			d\readNutPosition = readNut
			
			If readNut Then
				Local nx# = ReadFloat(demoFile)
				Local ny# = ReadFloat(demoFile)
				Local nz# = ReadFloat(demoFile)
				
				d\nutX = nx
				d\nutY = ny
				d\nutZ = nz
			EndIf
			
			;[End NPCs]
	
		Wend
								
		demo.Demos = First Demos
		prevDemo.Demos = demo
		lastDemo.Demos = Last Demos
		
		CloseFile(demoFile)
		demoFile = 0		
	;			
	;		EndIf			
	;	EndIf					
	EndIf
End Function

Function PlayDemo()
		
	If MilliSecs() - demoDelayTime >= 60 Then
	
		;If demo = Null Return
	
		PositionEntity(Collider, demo\px, demo\py, demo\pz)
		ResetEntity(Collider)
		
		PositionEntity(Head, demo\hx, demo\hy, demo\hz)
		ResetEntity(Head)
		
		RotateEntity(Collider, 0, demo\yaw, 0, 0)	
		RotateEntity(Camera, demo\pitch, demo\yaw, demo\roll, 0)		
		
		Stamina = demo\stamina
		
		BlinkTimer = demo\blink
		
		If demo\readDoorPosition Then
			For r.Doors = Each Doors
			
				; We have found the correct door that we have to do UseDoor() on.
				If EntityX(r\frameobj) = demo\doorx And EntityZ(r\frameobj) = demo\doorz And EntityY(r\frameobj) = demo\doory Then
					UseDoor(r, True)
					Exit
				EndIf
			Next		
		EndIf
		
		If demo\readNutPosition Then
			PositionEntity(Curr173\collider, demo\nutX, demo\nutY, demo\nutZ)
			ResetEntity(Curr173\collider)
			PointEntity(Curr173\Collider, Collider)
			;If PlayerRoom\RoomTemplate\Name <> "start"
			;	Local n.NPCs
			;	For n.NPCs = Each NPCs
			;		Select n\NPCtype
			;			Case NPCtype173
			;				PositionEntity(Curr173\collider, demo\nutX, demo\nutY, demo\nutZ)
			;				ResetEntity(Curr173\collider)
			;		End Select
			;	Next
			;EndIf
		EndIf 
		
		demoDelayTime = MilliSecs()
						
		If demo\tick = lastDemo\tick Then
				
			demoUIOpen = True
			demoPaused = True
				
		Else
			
			prevDemo.Demos = demo.Demos
			demo.Demos = After demo	
			
			ShouldLerp = True
			
		EndIf
	
	Else
		
		;We try to lerp the player position and camera pitch/yaw/roll in between tick updates.
		;We will start by doing this only once between ticks.
		
		If ShouldLerp And (MilliSecs() - demoDelayTime >= 30) Then
			
			;[Player]
			
			Local lpx# = Lerp(prevDemo\px, demo\px, 0.50)
			Local lpy# = Lerp(prevDemo\py, demo\py, 0.50)
			Local lpz# = Lerp(prevDemo\pz, demo\pz, 0.50)
			
			Local lhx# = Lerp(prevDemo\hx, demo\hx, 0.50)
			Local lhy# = Lerp(prevDemo\hy, demo\hy, 0.50)
			Local lhz# = Lerp(prevDemo\hz, demo\hz, 0.50)
			
			Local lpitch# = Lerp(prevDemo\pitch, demo\pitch, 0.50)
			Local lyaw#   = Lerp(prevDemo\yaw, demo\yaw, 0.50)
			Local lroll#  = Lerp(prevDemo\roll, demo\roll, 0.50)
			
			PositionEntity(Collider, lpx, lpy, lpz)
			ResetEntity(Collider)
			
			PositionEntity(Head, lhx, lhy, lhz)
			ResetEntity(Head)
			
			RotateEntity(Collider, 0, lyaw, 0, 0)
			RotateEntity(Camera, lpitch, lyaw, lroll, 0)
			
			;[End Player]
			
			ShouldLerp = False
			
		EndIf
	
	EndIf	


End Function

Function Lerp#(s#, e#, p#) 
	; We do a little lerping...
	Return (s + (e - s) * p)

End Function

Function EndOfFile()
End Function































