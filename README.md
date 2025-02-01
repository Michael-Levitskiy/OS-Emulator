# OS-Emulator
This project uses Java code to emulate how an Operating System (OS) executes different processes and tasks.

## Purpose:
- Learn and understand how an OS operates
- Practice using threads in Java
- Practice Object Oriented Programming in Java

## How the Project Works:
- To run the project, execute Main.java as that is where the Java main method exists
- Upon execution, OS.Startup() will be called with the Init Process (Init.java)
    - The Init Process initializes (calls OS.CreateProcess()) on the different processes within our program
- The Startup method initializes the Kernel (Kernel.java) and calls OS.CreateProcess on Init as well as an Idle Process (IdleProcess.java)
- Calling an OS method (like CreateProcess) will create a 'parameters' ArrayList containing all of the method parameters, use an enum to know what method we're executing, and start the kernel with all of this data
- In the kernel, first we determine the method call through the enum
    - Depending on the method, either the kernel will execute the operation or call the Scheduler (Scheduler.java) to execute the operation
    - For the CreateProcess method, we will create a Process Control Block (PCB) and call a Scheduler method (of the same name) using the given parameters
- The purpose of a Scheduler is to manage all of the different processes
    - The Scheduler is where all of the PCB's are stored and take turns running
    - If a process's quantum (or timer) runs out, then the process will change which process is currently running
    - The Scheduler would also switch which process is running it a process executes the 'Sleep' or 'Exit' methods
- The Scheduler's CreateProcess method will add the PCB to a linked list with a given priority
    - The purpose of a priority is to punish processes that use their entire quantum time too much. We will demote each process to a lower priority if the quantum expires more than 5 times in a row.
- With the Init process created and executed, our program now continues with all of its different processes taking turns running.
- Each created process has the ability to call different OS methods or created Devices for their own use. Below are list the different implemented OS Methods as well as the different Devices that can be used.

## OS Methods
- CreateProcess
- SwitchProcess
- Sleep
- Exit
- Open
- Close
- Read
- Seek
- Write
- GetPid
- GetPidByName
- SendMessage
- WaitForMessage
- GetMapping
- AllocateMemory
- FreeMemory

## Devices
- Random Device (RandomDevice.java)
- Fake File System (FakeFileSystem.java)
- Virtual File System (VFS.java)