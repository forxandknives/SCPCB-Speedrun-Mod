
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

Function FlopFunction()

	FPrint("hello flop")

	Local dllNumber% = NotFlopFunction()
	FPrint("dllNumber: " + Str(dllNumber))

	Local cpuReturn% = CPUGenerator(9780);2536653)
	FPrint("CPU: " + Str(cpuReturn))


End Function