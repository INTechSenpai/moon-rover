cmake_minimum_required (VERSION 2.6)
project(ax12)

set(${PROJ}_SRC)
AUX_SOURCE_DIRECTORY(cmsis_lib/source ${PROJ}_SRC)
AUX_SOURCE_DIRECTORY(src ${PROJ}_SRC)
AUX_SOURCE_DIRECTORY(cmsis_boot ${PROJ}_SRC)
AUX_SOURCE_DIRECTORY(cmsis_boot/startup ${PROJ}_SRC)
AUX_SOURCE_DIRECTORY(syscalls ${PROJ}_SRC)

add_executable(ax12.elf
	main.cpp
	${${PROJ}_SRC}
	../../library/Uart_IT.cpp
	)

include_directories(
	cmsis
	cmsis_boot
	cmsis_lib/include
	include/
	../../library
	)

add_definitions(
	-DUSE_STDPERIPH_DRIVER 
	-DSTM32F4XX
	-DUSE_USB_OTG_FS
	-DMEDIA_USB_KEY
	-DSTM32F407VG
)

# Compiler flags
set_target_properties(ax12.elf
	PROPERTIES COMPILE_FLAGS
	"-mcpu=cortex-m4 -mfpu=fpv4-sp-d16 -mfloat-abi=softfp -mthumb -Wall -ffunction-sections"
	)

#set(CMAKE_C_FLAGS "-std=c99")
#set(CMAKE_CXX_FLAGS "-std=c++11")

# Linker flags
set_target_properties(ax12.elf
	PROPERTIES LINK_FLAGS
	"-Wl,--gc-sections -Wl,--entry=Reset_Handler -nostartfiles -mcpu=cortex-m4 -mthumb -T${CMAKE_CURRENT_SOURCE_DIR}/../serie/arm-gcc-link.ld"
	#"--specs=nano.specs --specs=nosys.specs -Wl,--gc-sections -Wl,--entry=Reset_Handler -mcpu=cortex-m4 -mthumb -T${CMAKE_CURRENT_SOURCE_DIR}/../serie/arm-gcc-link.ld"
	)

# Binary file
add_custom_target(
	ax12.hex
	ALL DEPENDS ax12.elf
	COMMAND arm-none-eabi-objcopy -O ihex ax12.elf ax12.hex
	)

include(CMakeForceCompiler)
set(CMAKE_SYSTEM_NAME Generic)
cmake_force_c_compiler(arm-none-eabi-gcc GNU)
cmake_force_cxx_compiler(arm-none-eabi-g++ GNU)
set(CMAKE_SHARED_LIBRARY_LINK_C_FLAGS "")
set(CMAKE_SHARED_LIBRARY_LINK_CXX_FLAGS "")

