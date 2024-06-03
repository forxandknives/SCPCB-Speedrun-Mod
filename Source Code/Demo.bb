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
				MouseLook()
				If PlayerRoom\RoomTemplate\Name = "dimension1499" And QuickLoadPercent > 0 And QuickLoadPercent < 100
					ShouldEntitiesFall = False
				EndIf
				MovePlayer()
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
				UpdateNPCs()
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
						BlinkTimer = BLINKFREQ
					EndIf
					
					BlinkTimer = BlinkTimer - FPSfactor
				Else
					BlinkTimer = BlinkTimer - FPSfactor * 0.6 * BlinkEffect
					If EyeIrritation > 0 Then BlinkTimer=BlinkTimer-Min(EyeIrritation / 100.0 + 1.0, 4.0) * FPSfactor
					
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
		
		demoDelayTime = MilliSecs()
		
		;We do not close the demoPath file until we manually stop the demo.
				
	Else		
		
		If (MilliSecs() - demoDelayTime >= 60) Then ; Arbitrary amount of time (60 ms) to delay saving the user input and what not.		
		
			WriteLine(demoFile, CurrentTime())
		
			demoDelayTime = MilliSecs()
		
		EndIf
		
	EndIf
		
End Function

Function StopRecordingDemo()

	If demoFile Then
		CloseFile(demoFile)
		demoFile = 0
		
		CreateConsoleMsg("Demo saved to: " + demoSavePath)
		
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