
Const SEED_IS_GOOD%     = 0
Const NO_SEED_PROVIDED  = 1
Const SEED_NOT_NUMBER   = 2

Const SEED_NOT_BEATABLE = 3
Const SEED_IS_BEATABLE  = 4

Const SEED_NOT_99       = 5
Const SEED_CAN_BE_99    = 6

Const SEED_NOT_100      = 7
Const SEED_CAN_BE_100   = 8

Const INVALID_SEED      = 9

Const NO_CHECK_SPECIFIED = 10
Const INVALID_CHECK      = 11

Const CHECK_FOR_BEATABLE = 12
Const CHECK_FOR_99       = 13
Const CHECK_FOR_100      = 14

Const DO_NOT_REROLL      = 15

Function FlopFunction()

	Local cpuReturn% = CPUGenerator(Abs(MilliSecs()), CHECK_FOR_BEATABLE);2536653)
	FPrint("CPU: " + Str(cpuReturn))


End Function