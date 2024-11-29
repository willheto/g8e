section .data
    startup db 'run program?', 0x0a, 0    ; String to print
    len equ $ - startup                     ; Length of the string

section .text
global _start
extern GetStdHandle, WriteConsoleA, ReadConsoleA, ExitProcess

_start:
    ; Get handle to stdout (STD_OUTPUT_HANDLE = -11)
    push -11                                ; STD_OUTPUT_HANDLE
    call GetStdHandle                      ; eax = handle to stdout
    mov ebx, eax                            ; Save stdout handle in ebx

    ; Write to stdout
    push 0                                  ; Reserved param (NULL)
    push len                                ; Length of string
    push startup                            ; Pointer to the string
    push ebx                                ; Handle to stdout
    call WriteConsoleA                      ; Write string to console

    ; Exit process
    push 0                                  ; Exit code (0)
    call ExitProcess                        ; Exit the program
