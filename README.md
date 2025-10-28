# 🧠 Traductor de Máquina Virtual

**Proyecto:** Traductor de máquina virtual extendido para soportar funciones, llamadas y retornos  

---

## 👥 Integrantes del grupo
- **Salomé Serna Restrepo**
- **Juan David Velásquez Restrepo**
- **Luciana Hoyos Pérez**

---

## 🧩 Descripción breve del proyecto
Este proyecto implementa un **traductor de Máquina Virtual (VM Translator)** capaz de convertir código en lenguaje de máquina virtual (VM) del curso *Nand2Tetris* a código ensamblador del **Hack Computer**.  

La versión extendida del traductor incluye soporte completo para:
- Declaración y llamada de funciones (`function`, `call`, `return`)  
- Segmentos de memoria (`local`, `argument`, `this`, `that`, `constant`, `pointer`, `temp`, `static`)  
- Operaciones aritméticas y lógicas (`add`, `sub`, `eq`, `gt`, `lt`, `and`, `or`, `not`, etc.)

---

## 🧱 Estructura del código

| Módulo | Descripción |
|:-------|:-------------|
| **Parser** | Lee el archivo `.vm` línea por línea. Identifica el tipo de comando (aritmético, de memoria, de función, etc.) y extrae sus argumentos. |
| **CodeWriter** | Traduce los comandos VM a instrucciones en ensamblador Hack. Implementa las rutinas de salto, comparación, llamadas de función y retorno. |
| **Main** | Controlador principal del traductor. Recibe el archivo o directorio VM como argumento, crea las instancias de `Parser` y `CodeWriter`, y coordina el proceso de traducción. |
| **CommandType** | Enumera los tipos de comandos de la VM (`C_ARITHMETIC`, `C_PUSH`, `C_POP`, `C_LABEL`, `C_GOTO`, `C_IF`, `C_FUNCTION`, `C_RETURN`, `C_CALL`). Facilita la clasificación de las instrucciones para su correcta traducción. |

---

## 🧮 Explicación del programa **NestedSum**

El archivo **Main.vm** implementa un conjunto de funciones que realizan cálculos anidados, demostrando el uso de llamadas y retornos de funciones en la máquina virtual.

### 🔸 `Sys.init` - Archivo Distinto
- Punto de entrada del programa.  
- Inicializa el puntero del stack (`SP=256`) y realiza la primera llamada a `Main.computeSum`.

### 🔸 `Main.computeSum`
- Función principal que calcula la **suma acumulada** de los números del 1 al valor recibido como argumento.  
- Utiliza un bucle (`COMPUTE_LOOP`) para repetir operaciones de suma y llamadas a `Main.square` y `Main.addOne`.  

### 🔸 `Main.square`
- Calcula el **cuadrado de un número** mediante sumas repetidas.  
- Emplea un bucle (`SQR_LOOP`) para sumar el valor del argumento tantas veces como sea necesario.  
- Llama a `Main.addOne` al final para incrementar resultados parciales.  

### 🔸 `Main.addOne`
- Retorna el valor del argumento más 1.  
- Es utilizada por las demás funciones para incrementar contadores o resultados intermedios.  

---

## 🧾 Resultado generado
El traductor produce el archivo ensamblador:

```bash
vmfiles.asm
```

que contiene todas las instrucciones en **lenguaje ensamblador Hack**, listas para ser traducidas a código binario (`.hack`) y ejecutadas en el simulador **CPU Emulator** del proyecto Nand2Tetris.

---

## ⚙️ Cómo ejecutar

### 1️⃣ Compilar el traductor:

```bash
javac -d out src/com/stockxit/nand2tetris/*.java
```

### 2️⃣ Ejecutar el traductor sobre el archivo VM:

```bash
java -cp out com.stockxit.nand2tetris.Main vmfiles
```

### 3️⃣ Salida esperada:

```bash
vmfiles.asm
```

El archivo vmfiles.asm fue generado exitosamente y contiene todas las rutinas de inicialización, llamadas y retornos correctamente traducidas.
El código binario final (vmfiles.hack) puede ser cargado en el CPU Emulator para comprobar su funcionamiento.
